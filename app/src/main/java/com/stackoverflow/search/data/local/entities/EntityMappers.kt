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

import com.stackoverflow.search.domain.model.RecentSearch

fun RecentSearchEntity.toDomain(): RecentSearch = RecentSearch(
    id = id,
    query = query,
    timestamp = timestamp
)
