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
package com.stackoverflow.search.domain.model

data class Question(
    val questionId: Long,
    val title: String,
    val body: String,
    val score: Int,
    val answerCount: Int,
    val viewCount: Int,
    val isAnswered: Boolean,
    val acceptedAnswerId: Long?,
    val owner: Owner?,
    val tags: List<String>,
    val creationDate: Long,
    val link: String
)

data class Owner(
    val displayName: String,
    val profileImage: String?,
    val reputation: Int,
    val link: String?
)

data class Answer(
    val answerId: Long,
    val questionId: Long,
    val body: String,
    val score: Int,
    val isAccepted: Boolean,
    val owner: Owner?,
    val creationDate: Long
)

data class RecentSearch(
    val id: Long = 0,
    val query: String,
    val timestamp: Long = System.currentTimeMillis()
)
