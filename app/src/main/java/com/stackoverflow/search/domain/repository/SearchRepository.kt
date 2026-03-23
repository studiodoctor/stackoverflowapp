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
package com.stackoverflow.search.domain.repository

import com.stackoverflow.search.core.utils.Resource
import com.stackoverflow.search.domain.model.Answer
import com.stackoverflow.search.domain.model.Question
import com.stackoverflow.search.domain.model.RecentSearch
import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    suspend fun searchQuestions(query: String, page: Int): Resource<List<Question>>
    suspend fun getAnswers(questionId: Long): Resource<List<Answer>>
    fun getRecentSearches(): Flow<List<RecentSearch>>
    suspend fun saveRecentSearch(query: String)
    suspend fun deleteRecentSearch(id: Long)
    suspend fun clearAllRecentSearches()
    suspend fun getBookmarkedQuestions(): List<Question>
    suspend fun bookmarkQuestion(question: Question)
    suspend fun removeBookmark(questionId: Long)
    suspend fun isBookmarked(questionId: Long): Boolean
}
