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
package com.stackoverflow.search.data.repository

import com.stackoverflow.search.core.utils.NetworkUtils
import com.stackoverflow.search.core.utils.Resource
import com.stackoverflow.search.data.local.dao.BookmarkDao
import com.stackoverflow.search.data.local.dao.RecentSearchDao
import com.stackoverflow.search.data.local.entities.RecentSearchEntity
import com.stackoverflow.search.data.local.entities.toDomain
import com.stackoverflow.search.data.remote.api.StackOverflowApi
import com.stackoverflow.search.data.remote.dto.toDomain
import com.stackoverflow.search.data.remote.dto.toBookmarkEntity
import com.stackoverflow.search.domain.model.Answer
import com.stackoverflow.search.domain.model.Question
import com.stackoverflow.search.domain.model.RecentSearch
import com.stackoverflow.search.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepositoryImpl @Inject constructor(
    private val api: StackOverflowApi,
    private val recentSearchDao: RecentSearchDao,
    private val bookmarkDao: BookmarkDao,
    private val networkUtils: NetworkUtils
) : SearchRepository {

    override suspend fun searchQuestions(query: String, page: Int): Resource<List<Question>> {
        if (!networkUtils.isNetworkAvailable()) {
            return Resource.Error("No internet connection")
        }
        return try {
            val response = api.searchQuestions(query = query, page = page)
            Resource.Success(response.items.map { it.toDomain() })
        } catch (e: HttpException) {
            val errorMsg = when (e.code()) {
                400 -> "Bad request"
                429 -> "Too many requests. Please try again later."
                500 -> "Server error. Please try again."
                else -> "HTTP error ${e.code()}"
            }
            Resource.Error(errorMsg, e)
        } catch (e: IOException) {
            Resource.Error("Network error. Please check your connection.", e)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unexpected error occurred", e)
        }
    }

    override suspend fun getAnswers(questionId: Long): Resource<List<Answer>> {
        if (!networkUtils.isNetworkAvailable()) {
            return Resource.Error("No internet connection")
        }
        return try {
            val response = api.getAnswers(questionId)
            Resource.Success(response.items.map { it.toDomain() })
        } catch (e: HttpException) {
            Resource.Error("HTTP error ${e.code()}", e)
        } catch (e: IOException) {
            Resource.Error("Network error. Please check your connection.", e)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unexpected error occurred", e)
        }
    }

    override fun getRecentSearches(): Flow<List<RecentSearch>> {
        return recentSearchDao.getRecentSearches().map { list ->
            list.map { entity -> entity.toDomain() }
        }
    }

    override suspend fun saveRecentSearch(query: String) {
        val existing = recentSearchDao.findByQuery(query)
        if (existing != null) {
            recentSearchDao.deleteSearch(existing.id)
        }
        recentSearchDao.insertSearch(RecentSearchEntity(query = query))
    }

    override suspend fun deleteRecentSearch(id: Long) {
        recentSearchDao.deleteSearch(id)
    }

    override suspend fun clearAllRecentSearches() {
        recentSearchDao.clearAll()
    }

    override suspend fun getBookmarkedQuestions(): List<Question> {
        return bookmarkDao.getAllBookmarks().map { it.toDomain() }
    }

    override suspend fun bookmarkQuestion(question: Question) {
        bookmarkDao.insertBookmark(question.toBookmarkEntity())
    }

    override suspend fun removeBookmark(questionId: Long) {
        bookmarkDao.deleteBookmark(questionId)
    }

    override suspend fun isBookmarked(questionId: Long): Boolean {
        return bookmarkDao.isBookmarked(questionId) > 0
    }
}
