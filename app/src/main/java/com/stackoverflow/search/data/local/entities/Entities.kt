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
package com.stackoverflow.search.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_searches")
data class RecentSearchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val query: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val questionId: Long,
    val title: String,
    val body: String,
    val score: Int,
    val answerCount: Int,
    val viewCount: Int,
    val isAnswered: Boolean,
    val acceptedAnswerId: Long?,
    val ownerDisplayName: String?,
    val ownerProfileImage: String?,
    val ownerReputation: Int,
    val tags: String,
    val creationDate: Long,
    val link: String,
    val bookmarkedAt: Long = System.currentTimeMillis()
)
