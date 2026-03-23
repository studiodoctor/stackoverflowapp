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
package com.stackoverflow.search.data.remote.api

import com.stackoverflow.search.data.remote.dto.AnswersResponseDto
import com.stackoverflow.search.data.remote.dto.SearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface StackOverflowApi {

    @GET("search/advanced")
    suspend fun searchQuestions(
        @Query("title") query: String,
        @Query("page") page: Int = 1,
        @Query("pagesize") pageSize: Int = 20,
        @Query("order") order: String = "desc",
        @Query("sort") sort: String = "activity",
        @Query("site") site: String = "stackoverflow",
        @Query("filter") filter: String = "withbody"
    ): SearchResponseDto

    @GET("questions/{questionId}/answers")
    suspend fun getAnswers(
        @Path("questionId") questionId: Long,
        @Query("order") order: String = "desc",
        @Query("sort") sort: String = "activity",
        @Query("site") site: String = "stackoverflow",
        @Query("filter") filter: String = "withbody"
    ): AnswersResponseDto

    companion object {
        const val BASE_URL = "https://api.stackexchange.com/2.2/"
    }
}
