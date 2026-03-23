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
package com.stackoverflow.search.domain.usecase

import com.stackoverflow.search.core.utils.Resource
import com.stackoverflow.search.domain.model.Answer
import com.stackoverflow.search.domain.model.Question
import com.stackoverflow.search.domain.model.RecentSearch
import com.stackoverflow.search.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchQuestionsUseCase @Inject constructor(
    private val repository: SearchRepository
) {
    suspend operator fun invoke(query: String, page: Int = 1): Resource<List<Question>> {
        if (query.isBlank()) return Resource.Error("Search query cannot be empty")
        return repository.searchQuestions(query.trim(), page)
    }
}

class GetAnswersUseCase @Inject constructor(
    private val repository: SearchRepository
) {
    suspend operator fun invoke(questionId: Long): Resource<List<Answer>> {
        return repository.getAnswers(questionId)
    }
}

class GetRecentSearchesUseCase @Inject constructor(
    private val repository: SearchRepository
) {
    operator fun invoke(): Flow<List<RecentSearch>> = repository.getRecentSearches()
}

class SaveRecentSearchUseCase @Inject constructor(
    private val repository: SearchRepository
) {
    suspend operator fun invoke(query: String) {
        if (query.isNotBlank()) repository.saveRecentSearch(query.trim())
    }
}

class DeleteRecentSearchUseCase @Inject constructor(
    private val repository: SearchRepository
) {
    suspend operator fun invoke(id: Long) = repository.deleteRecentSearch(id)
}

class ClearRecentSearchesUseCase @Inject constructor(
    private val repository: SearchRepository
) {
    suspend operator fun invoke() = repository.clearAllRecentSearches()
}

class BookmarkQuestionUseCase @Inject constructor(
    private val repository: SearchRepository
) {
    suspend operator fun invoke(question: Question) = repository.bookmarkQuestion(question)
}

class RemoveBookmarkUseCase @Inject constructor(
    private val repository: SearchRepository
) {
    suspend operator fun invoke(questionId: Long) = repository.removeBookmark(questionId)
}

class IsBookmarkedUseCase @Inject constructor(
    private val repository: SearchRepository
) {
    suspend operator fun invoke(questionId: Long): Boolean = repository.isBookmarked(questionId)
}

class GetBookmarkedQuestionsUseCase @Inject constructor(
    private val repository: SearchRepository
) {
    suspend operator fun invoke(): List<Question> = repository.getBookmarkedQuestions()
}
