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
package com.stackoverflow.search.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SearchResponseDto(
    @SerializedName("items") val items: List<QuestionDto> = emptyList(),
    @SerializedName("has_more") val hasMore: Boolean = false,
    @SerializedName("quota_max") val quotaMax: Int = 0,
    @SerializedName("quota_remaining") val quotaRemaining: Int = 0
)

data class QuestionDto(
    @SerializedName("question_id") val questionId: Long = 0,
    @SerializedName("title") val title: String = "",
    @SerializedName("body") val body: String? = null,
    @SerializedName("score") val score: Int = 0,
    @SerializedName("answer_count") val answerCount: Int = 0,
    @SerializedName("view_count") val viewCount: Int = 0,
    @SerializedName("is_answered") val isAnswered: Boolean = false,
    @SerializedName("accepted_answer_id") val acceptedAnswerId: Long? = null,
    @SerializedName("owner") val owner: OwnerDto? = null,
    @SerializedName("tags") val tags: List<String> = emptyList(),
    @SerializedName("creation_date") val creationDate: Long = 0,
    @SerializedName("last_activity_date") val lastActivityDate: Long = 0,
    @SerializedName("link") val link: String = ""
)

data class OwnerDto(
    @SerializedName("account_id") val accountId: Long? = null,
    @SerializedName("reputation") val reputation: Int = 0,
    @SerializedName("user_id") val userId: Long? = null,
    @SerializedName("user_type") val userType: String = "",
    @SerializedName("display_name") val displayName: String = "",
    @SerializedName("profile_image") val profileImage: String? = null,
    @SerializedName("link") val link: String? = null
)

data class AnswersResponseDto(
    @SerializedName("items") val items: List<AnswerDto> = emptyList(),
    @SerializedName("has_more") val hasMore: Boolean = false,
    @SerializedName("quota_max") val quotaMax: Int = 0,
    @SerializedName("quota_remaining") val quotaRemaining: Int = 0
)

data class AnswerDto(
    @SerializedName("answer_id") val answerId: Long = 0,
    @SerializedName("question_id") val questionId: Long = 0,
    @SerializedName("body") val body: String? = null,
    @SerializedName("score") val score: Int = 0,
    @SerializedName("is_accepted") val isAccepted: Boolean = false,
    @SerializedName("owner") val owner: OwnerDto? = null,
    @SerializedName("creation_date") val creationDate: Long = 0,
    @SerializedName("last_activity_date") val lastActivityDate: Long = 0
)
