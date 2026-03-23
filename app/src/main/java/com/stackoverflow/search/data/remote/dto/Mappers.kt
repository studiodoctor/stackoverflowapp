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

import com.stackoverflow.search.data.local.entities.BookmarkEntity
import com.stackoverflow.search.domain.model.Answer
import com.stackoverflow.search.domain.model.Owner
import com.stackoverflow.search.domain.model.Question

fun QuestionDto.toDomain(): Question = Question(
    questionId = questionId,
    title = title,
    body = body ?: "",
    score = score,
    answerCount = answerCount,
    viewCount = viewCount,
    isAnswered = isAnswered,
    acceptedAnswerId = acceptedAnswerId,
    owner = owner?.toDomain(),
    tags = tags,
    creationDate = creationDate,
    link = link
)

fun OwnerDto.toDomain(): Owner = Owner(
    displayName = displayName,
    profileImage = profileImage,
    reputation = reputation,
    link = link
)

fun AnswerDto.toDomain(): Answer = Answer(
    answerId = answerId,
    questionId = questionId,
    body = body ?: "",
    score = score,
    isAccepted = isAccepted,
    owner = owner?.toDomain(),
    creationDate = creationDate
)

fun Question.toBookmarkEntity(): BookmarkEntity = BookmarkEntity(
    questionId = questionId,
    title = title,
    body = body,
    score = score,
    answerCount = answerCount,
    viewCount = viewCount,
    isAnswered = isAnswered,
    acceptedAnswerId = acceptedAnswerId,
    ownerDisplayName = owner?.displayName,
    ownerProfileImage = owner?.profileImage,
    ownerReputation = owner?.reputation ?: 0,
    tags = tags.joinToString(","),
    creationDate = creationDate,
    link = link,
    bookmarkedAt = System.currentTimeMillis()
)

fun BookmarkEntity.toDomain(): Question = Question(
    questionId = questionId,
    title = title,
    body = body,
    score = score,
    answerCount = answerCount,
    viewCount = viewCount,
    isAnswered = isAnswered,
    acceptedAnswerId = acceptedAnswerId,
    owner = if (ownerDisplayName != null) Owner(
        displayName = ownerDisplayName,
        profileImage = ownerProfileImage,
        reputation = ownerReputation,
        link = null
    ) else null,
    tags = if (tags.isNotEmpty()) tags.split(",") else emptyList(),
    creationDate = creationDate,
    link = link
)
