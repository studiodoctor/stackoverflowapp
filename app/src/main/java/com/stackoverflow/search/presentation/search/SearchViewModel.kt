package com.stackoverflow.search.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stackoverflow.search.core.utils.Resource
import com.stackoverflow.search.domain.model.Question
import com.stackoverflow.search.domain.model.RecentSearch
import com.stackoverflow.search.domain.usecase.ClearRecentSearchesUseCase
import com.stackoverflow.search.domain.usecase.DeleteRecentSearchUseCase
import com.stackoverflow.search.domain.usecase.GetBookmarkedQuestionsUseCase
import com.stackoverflow.search.domain.usecase.GetRecentSearchesUseCase
import com.stackoverflow.search.domain.usecase.RemoveBookmarkUseCase
import com.stackoverflow.search.domain.usecase.SaveRecentSearchUseCase
import com.stackoverflow.search.domain.usecase.SearchQuestionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val questions: List<Question> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val isNoInternet: Boolean = false,
    val recentSearches: List<RecentSearch> = emptyList(),
    val showRecentSearches: Boolean = false,
    val currentPage: Int = 1,
    val hasMorePages: Boolean = true,
    val isRefreshing: Boolean = false,
    // Drawer state
    val bookmarkedQuestions: List<Question> = emptyList(),
    val isLoadingBookmarks: Boolean = false
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchQuestionsUseCase: SearchQuestionsUseCase,
    private val getRecentSearchesUseCase: GetRecentSearchesUseCase,
    private val saveRecentSearchUseCase: SaveRecentSearchUseCase,
    private val deleteRecentSearchUseCase: DeleteRecentSearchUseCase,
    private val clearRecentSearchesUseCase: ClearRecentSearchesUseCase,
    private val getBookmarkedQuestionsUseCase: GetBookmarkedQuestionsUseCase,
    private val removeBookmarkUseCase: RemoveBookmarkUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        observeRecentSearches()
    }

    private fun observeRecentSearches() {
        viewModelScope.launch {
            getRecentSearchesUseCase().collect { searches ->
                _uiState.update { it.copy(recentSearches = searches) }
            }
        }
    }

    fun loadBookmarks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingBookmarks = true) }
            val bookmarks = getBookmarkedQuestionsUseCase()
            _uiState.update { it.copy(bookmarkedQuestions = bookmarks, isLoadingBookmarks = false) }
        }
    }

    fun removeBookmark(questionId: Long) {
        viewModelScope.launch {
            removeBookmarkUseCase(questionId)
            // Refresh the list after removal
            val updated = getBookmarkedQuestionsUseCase()
            _uiState.update { it.copy(bookmarkedQuestions = updated) }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query, showRecentSearches = query.isBlank()) }
        if (query.isBlank()) {
            searchJob?.cancel()
            _uiState.update {
                it.copy(questions = emptyList(), error = null, isLoading = false)
            }
        }
    }

    fun onSearchFocused() {
        _uiState.update { it.copy(showRecentSearches = _uiState.value.query.isBlank()) }
    }

    fun onSearchFocusLost() {
        _uiState.update { it.copy(showRecentSearches = false) }
    }

    fun search(query: String = _uiState.value.query) {
        if (query.isBlank()) return
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    query = query,
                    isLoading = true,
                    error = null,
                    isNoInternet = false,
                    questions = emptyList(),
                    currentPage = 1,
                    hasMorePages = true,
                    showRecentSearches = false
                )
            }
            saveRecentSearchUseCase(query)
            performSearch(query, page = 1, append = false)
        }
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (state.isLoadingMore || !state.hasMorePages || state.query.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            performSearch(state.query, page = state.currentPage + 1, append = true)
        }
    }

    fun refresh() {
        val query = _uiState.value.query
        if (query.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null, isNoInternet = false) }
            performSearch(query, page = 1, append = false)
        }
    }

    private suspend fun performSearch(query: String, page: Int, append: Boolean) {
        when (val result = searchQuestionsUseCase(query, page)) {
            is Resource.Success -> {
                val newQuestions = result.data
                _uiState.update { state ->
                    state.copy(
                        questions = if (append) state.questions + newQuestions else newQuestions,
                        isLoading = false,
                        isLoadingMore = false,
                        isRefreshing = false,
                        error = null,
                        currentPage = page,
                        hasMorePages = newQuestions.size == 20
                    )
                }
            }
            is Resource.Error -> {
                val isNoInternet = result.message.contains("No internet", ignoreCase = true) ||
                        result.message.contains("network", ignoreCase = true)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        isRefreshing = false,
                        error = if (!isNoInternet) result.message else null,
                        isNoInternet = isNoInternet
                    )
                }
            }
            is Resource.Loading -> Unit
        }
    }

    fun deleteRecentSearch(id: Long) {
        viewModelScope.launch { deleteRecentSearchUseCase(id) }
    }

    fun clearRecentSearches() {
        viewModelScope.launch { clearRecentSearchesUseCase() }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null, isNoInternet = false) }
    }
}