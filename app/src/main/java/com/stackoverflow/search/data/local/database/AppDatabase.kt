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
package com.stackoverflow.search.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.stackoverflow.search.data.local.dao.BookmarkDao
import com.stackoverflow.search.data.local.dao.RecentSearchDao
import com.stackoverflow.search.data.local.entities.BookmarkEntity
import com.stackoverflow.search.data.local.entities.RecentSearchEntity

@Database(
    entities = [RecentSearchEntity::class, BookmarkEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recentSearchDao(): RecentSearchDao
    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        const val DATABASE_NAME = "stackoverflow_db"
    }
}
