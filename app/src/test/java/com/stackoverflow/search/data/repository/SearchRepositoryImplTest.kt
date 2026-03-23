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
import com.stackoverflow.search.data.remote.api.StackOverflowApi
import com.stackoverflow.search.data.remote.dto.*
import com.stackoverflow.search.domain.model.Owner
import com.stackoverflow.search.domain.model.Question
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.Assert.*
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class SearchRepositoryImplTest {

    @MockK lateinit var api: StackOverflowApi
    @MockK lateinit var recentSearchDao: RecentSearchDao
    @MockK lateinit var bookmarkDao: BookmarkDao
    @MockK lateinit var networkUtils: NetworkUtils

    private lateinit var repository: SearchRepositoryImpl

    private val mockOwnerDto = OwnerDto(
        accountId = 1L,
        reputation = 5000,
        userId = 1L,
        userType = "registered",
        displayName = "TestUser",
        profileImage = null,
        link = null
    )

    private val mockQuestionDto = QuestionDto(
        questionId = 1L,
        title = "Test Question",
        body = "<p>body</p>",
        score = 10,
        answerCount = 3,
        viewCount = 100,
        isAnswered = true,
        acceptedAnswerId = null,
        owner = mockOwnerDto,
        tags = listOf("kotlin"),
        creationDate = 1_700_000_000L,
        link = "https://stackoverflow.com/q/1"
    )

    private val mockSearchResponse = SearchResponseDto(
        items = listOf(mockQuestionDto),
        hasMore = false,
        quotaMax = 300,
        quotaRemaining = 290
    )

    private val mockAnswerDto = AnswerDto(
        answerId = 100L,
        questionId = 1L,
        body = "<p>answer body</p>",
        score = 42,
        isAccepted = true,
        owner = mockOwnerDto,
        creationDate = 1_700_100_000L
    )

    private val mockAnswersResponse = AnswersResponseDto(
        items = listOf(mockAnswerDto),
        hasMore = false,
        quotaMax = 300,
        quotaRemaining = 290
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        repository = SearchRepositoryImpl(api, recentSearchDao, bookmarkDao, networkUtils)
    }

    @After
    fun tearDown() = unmockkAll()

    // --- Search questions ---

    @Test
    fun `searchQuestions returns success with mapped domain models`() = runTest {
        every { networkUtils.isNetworkAvailable() } returns true
        coEvery { api.searchQuestions(query = "kotlin", page = 1) } returns mockSearchResponse

        val result = repository.searchQuestions("kotlin", 1)

        assertTrue(result is Resource.Success)
        val questions = (result as Resource.Success).data
        assertEquals(1, questions.size)
        assertEquals(1L, questions[0].questionId)
        assertEquals("Test Question", questions[0].title)
        assertEquals("TestUser", questions[0].owner?.displayName)
    }

    @Test
    fun `searchQuestions returns error when no internet`() = runTest {
        every { networkUtils.isNetworkAvailable() } returns false

        val result = repository.searchQuestions("kotlin", 1)

        assertTrue(result is Resource.Error)
        assertEquals("No internet connection", (result as Resource.Error).message)
    }

    @Test
    fun `searchQuestions returns error on IOException`() = runTest {
        every { networkUtils.isNetworkAvailable() } returns true
        coEvery { api.searchQuestions(query = "kotlin", page = 1) } throws IOException("Network failure")

        val result = repository.searchQuestions("kotlin", 1)

        assertTrue(result is Resource.Error)
        assertTrue((result as Resource.Error).message.contains("Network error"))
    }

    @Test
    fun `searchQuestions returns error on generic exception`() = runTest {
        every { networkUtils.isNetworkAvailable() } returns true
        coEvery { api.searchQuestions(query = "kotlin", page = 1) } throws RuntimeException("Unexpected")

        val result = repository.searchQuestions("kotlin", 1)

        assertTrue(result is Resource.Error)
        assertEquals("Unexpected", (result as Resource.Error).message)
    }

    // --- Get answers ---

    @Test
    fun `getAnswers returns success with mapped domain models`() = runTest {
        every { networkUtils.isNetworkAvailable() } returns true
        coEvery { api.getAnswers(1L) } returns mockAnswersResponse

        val result = repository.getAnswers(1L)

        assertTrue(result is Resource.Success)
        val answers = (result as Resource.Success).data
        assertEquals(1, answers.size)
        assertEquals(100L, answers[0].answerId)
        assertTrue(answers[0].isAccepted)
        assertEquals(42, answers[0].score)
    }

    @Test
    fun `getAnswers returns error when no internet`() = runTest {
        every { networkUtils.isNetworkAvailable() } returns false

        val result = repository.getAnswers(1L)

        assertTrue(result is Resource.Error)
        assertEquals("No internet connection", (result as Resource.Error).message)
    }

    // --- Recent searches ---

    @Test
    fun `getRecentSearches returns mapped flow from dao`() = runTest {
        val entities = listOf(
            RecentSearchEntity(1L, "kotlin", 1_700_000_000L),
            RecentSearchEntity(2L, "android", 1_700_000_001L)
        )
        every { recentSearchDao.getRecentSearches() } returns flowOf(entities)

        val result = repository.getRecentSearches().toList()

        assertEquals(1, result.size)
        assertEquals(2, result[0].size)
        assertEquals("kotlin", result[0][0].query)
    }

    @Test
    fun `saveRecentSearch deletes existing entry then inserts new one`() = runTest {
        val existing = RecentSearchEntity(1L, "kotlin")
        coEvery { recentSearchDao.findByQuery("kotlin") } returns existing
        coEvery { recentSearchDao.deleteSearch(1L) } just Runs
        coEvery { recentSearchDao.insertSearch(any()) } just Runs

        repository.saveRecentSearch("kotlin")

        coVerify { recentSearchDao.deleteSearch(1L) }
        coVerify { recentSearchDao.insertSearch(match { it.query == "kotlin" }) }
    }

    @Test
    fun `saveRecentSearch inserts without deleting when not existing`() = runTest {
        coEvery { recentSearchDao.findByQuery("kotlin") } returns null
        coEvery { recentSearchDao.insertSearch(any()) } just Runs

        repository.saveRecentSearch("kotlin")

        coVerify(exactly = 0) { recentSearchDao.deleteSearch(any()) }
        coVerify { recentSearchDao.insertSearch(match { it.query == "kotlin" }) }
    }

    // --- Bookmarks ---

    @Test
    fun `isBookmarked returns true when count is greater than 0`() = runTest {
        coEvery { bookmarkDao.isBookmarked(1L) } returns 1

        assertTrue(repository.isBookmarked(1L))
    }

    @Test
    fun `isBookmarked returns false when count is 0`() = runTest {
        coEvery { bookmarkDao.isBookmarked(1L) } returns 0

        assertFalse(repository.isBookmarked(1L))
    }

    @Test
    fun `bookmarkQuestion calls dao insert`() = runTest {
        val question = Question(
            questionId = 1L, title = "Q", body = "body", score = 5,
            answerCount = 1, viewCount = 50, isAnswered = false,
            acceptedAnswerId = null,
            owner = Owner("Dev", null, 100, null),
            tags = listOf("android"), creationDate = 0L, link = ""
        )
        coEvery { bookmarkDao.insertBookmark(any()) } just Runs

        repository.bookmarkQuestion(question)

        coVerify { bookmarkDao.insertBookmark(match { it.questionId == 1L }) }
    }

    @Test
    fun `removeBookmark calls dao delete`() = runTest {
        coEvery { bookmarkDao.deleteBookmark(1L) } just Runs

        repository.removeBookmark(1L)

        coVerify { bookmarkDao.deleteBookmark(1L) }
    }
}
