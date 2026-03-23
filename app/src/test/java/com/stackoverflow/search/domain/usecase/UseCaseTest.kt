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
package com.stackoverflow.search.domain.usecase

import com.stackoverflow.search.core.utils.Resource
import com.stackoverflow.search.domain.model.Owner
import com.stackoverflow.search.domain.model.Question
import com.stackoverflow.search.domain.repository.SearchRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class SearchQuestionsUseCaseTest {

    @MockK lateinit var repository: SearchRepository
    private lateinit var useCase: SearchQuestionsUseCase

    private val mockQuestions = listOf(
        Question(
            questionId = 1L,
            title = "Kotlin coroutines",
            body = "<p>body</p>",
            score = 10,
            answerCount = 2,
            viewCount = 100,
            isAnswered = true,
            acceptedAnswerId = null,
            owner = Owner("dev", null, 100, null),
            tags = listOf("kotlin"),
            creationDate = 1_700_000_000L,
            link = "https://stackoverflow.com/q/1"
        )
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = SearchQuestionsUseCase(repository)
    }

    @After
    fun tearDown() = unmockkAll()

    @Test
    fun `invoke returns success when repository succeeds`() = runTest {
        coEvery { repository.searchQuestions("kotlin", 1) } returns Resource.Success(mockQuestions)

        val result = useCase("kotlin", 1)

        assertTrue(result is Resource.Success)
        assertEquals(1, (result as Resource.Success).data.size)
    }

    @Test
    fun `invoke returns error for blank query`() = runTest {
        val result = useCase("  ", 1)

        assertTrue(result is Resource.Error)
        assertEquals("Search query cannot be empty", (result as Resource.Error).message)
        coVerify(exactly = 0) { repository.searchQuestions(any(), any()) }
    }

    @Test
    fun `invoke trims whitespace from query`() = runTest {
        coEvery { repository.searchQuestions("kotlin", 1) } returns Resource.Success(mockQuestions)

        useCase("  kotlin  ", 1)

        coVerify { repository.searchQuestions("kotlin", 1) }
    }

    @Test
    fun `invoke returns error when repository fails`() = runTest {
        coEvery { repository.searchQuestions("kotlin", 1) } returns Resource.Error("Network error")

        val result = useCase("kotlin", 1)

        assertTrue(result is Resource.Error)
        assertEquals("Network error", (result as Resource.Error).message)
    }

    @Test
    fun `invoke uses page 1 by default`() = runTest {
        coEvery { repository.searchQuestions("kotlin", 1) } returns Resource.Success(mockQuestions)

        useCase("kotlin")

        coVerify { repository.searchQuestions("kotlin", 1) }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class GetAnswersUseCaseTest {

    @MockK lateinit var repository: SearchRepository
    private lateinit var useCase: GetAnswersUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = GetAnswersUseCase(repository)
    }

    @After
    fun tearDown() = unmockkAll()

    @Test
    fun `invoke delegates to repository`() = runTest {
        coEvery { repository.getAnswers(42L) } returns Resource.Success(emptyList())

        useCase(42L)

        coVerify { repository.getAnswers(42L) }
    }

    @Test
    fun `invoke returns error when repository fails`() = runTest {
        coEvery { repository.getAnswers(42L) } returns Resource.Error("Failed")

        val result = useCase(42L)

        assertTrue(result is Resource.Error)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SaveRecentSearchUseCaseTest {

    @MockK lateinit var repository: SearchRepository
    private lateinit var useCase: SaveRecentSearchUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = SaveRecentSearchUseCase(repository)
    }

    @After
    fun tearDown() = unmockkAll()

    @Test
    fun `invoke saves trimmed non-blank query`() = runTest {
        coEvery { repository.saveRecentSearch("kotlin") } just Runs

        useCase("  kotlin  ")

        coVerify { repository.saveRecentSearch("kotlin") }
    }

    @Test
    fun `invoke does not save blank query`() = runTest {
        useCase("   ")

        coVerify(exactly = 0) { repository.saveRecentSearch(any()) }
    }

    @Test
    fun `invoke does not save empty string`() = runTest {
        useCase("")

        coVerify(exactly = 0) { repository.saveRecentSearch(any()) }
    }
}
