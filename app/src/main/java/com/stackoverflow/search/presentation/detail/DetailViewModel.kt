/**
 * Project: StackOverflow Search App
 * Author: Manpreet Singh Mall
 * Created: 2026-03-22
 *
 * Description:
 * This file is part of the application and follows MVVM and Clean Architecture principles.
 *
 * Tech Stack:
 * Kotlin, Jetpack Compose, Coroutines, Flow
 */
package com.stackoverflow.search.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stackoverflow.search.core.utils.Resource
import com.stackoverflow.search.domain.model.Answer
import com.stackoverflow.search.domain.model.Question
import com.stackoverflow.search.domain.usecase.BookmarkQuestionUseCase
import com.stackoverflow.search.domain.usecase.GetAnswersUseCase
import com.stackoverflow.search.domain.usecase.IsBookmarkedUseCase
import com.stackoverflow.search.domain.usecase.RemoveBookmarkUseCase
import com.stackoverflow.search.presentation.navigation.NavArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val question: Question? = null,
    val answers: List<Answer> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isNoInternet: Boolean = false,
    val isBookmarked: Boolean = false,
    val answerSortOrder: AnswerSortOrder = AnswerSortOrder.VOTES
)

enum class AnswerSortOrder { ACTIVE, OLDEST, VOTES }

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val getAnswersUseCase: GetAnswersUseCase,
    private val bookmarkQuestionUseCase: BookmarkQuestionUseCase,
    private val removeBookmarkUseCase: RemoveBookmarkUseCase,
    private val isBookmarkedUseCase: IsBookmarkedUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private val questionId: Long = checkNotNull(savedStateHandle[NavArgs.QUESTION_ID])
    private var allAnswers: List<Answer> = emptyList()

    fun initialize(question: Question) {
        _uiState.update { it.copy(question = question) }
        checkBookmarkStatus()
        loadAnswers()
    }

    private fun checkBookmarkStatus() {
        viewModelScope.launch {
            val bookmarked = isBookmarkedUseCase(questionId)
            _uiState.update { it.copy(isBookmarked = bookmarked) }
        }
    }

    fun loadAnswers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, isNoInternet = false) }
            when (val result = getAnswersUseCase(questionId)) {
                is Resource.Success -> {
                    allAnswers = result.data
                    _uiState.update {
                        it.copy(
                            answers = sortAnswers(result.data, it.answerSortOrder),
                            isLoading = false
                        )
                    }
                }
                is Resource.Error -> {
                    val isNoInternet = result.message.contains("No internet", ignoreCase = true) ||
                            result.message.contains("network", ignoreCase = true)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = if (!isNoInternet) result.message else null,
                            isNoInternet = isNoInternet
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun setSortOrder(order: AnswerSortOrder) {
        _uiState.update {
            it.copy(
                answerSortOrder = order,
                answers = sortAnswers(allAnswers, order)
            )
        }
    }

    private fun sortAnswers(answers: List<Answer>, order: AnswerSortOrder): List<Answer> =
        when (order) {
            AnswerSortOrder.VOTES -> answers.sortedByDescending { it.score }
            AnswerSortOrder.OLDEST -> answers.sortedBy { it.creationDate }
            AnswerSortOrder.ACTIVE -> answers.sortedByDescending { it.creationDate }
        }

    fun toggleBookmark() {
        val question = _uiState.value.question ?: return
        viewModelScope.launch {
            if (_uiState.value.isBookmarked) {
                removeBookmarkUseCase(question.questionId)
                _uiState.update { it.copy(isBookmarked = false) }
            } else {
                bookmarkQuestionUseCase(question)
                _uiState.update { it.copy(isBookmarked = true) }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null, isNoInternet = false) }
    }
}
