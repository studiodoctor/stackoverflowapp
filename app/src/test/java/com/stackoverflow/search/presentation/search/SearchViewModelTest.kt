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
package com.stackoverflow.search.presentation.search

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.stackoverflow.search.core.utils.Resource
import com.stackoverflow.search.domain.model.Owner
import com.stackoverflow.search.domain.model.Question
import com.stackoverflow.search.domain.model.RecentSearch
import com.stackoverflow.search.domain.usecase.*
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK lateinit var searchQuestionsUseCase: SearchQuestionsUseCase
    @MockK lateinit var getRecentSearchesUseCase: GetRecentSearchesUseCase
    @MockK lateinit var saveRecentSearchUseCase: SaveRecentSearchUseCase
    @MockK lateinit var deleteRecentSearchUseCase: DeleteRecentSearchUseCase
    @MockK lateinit var clearRecentSearchesUseCase: ClearRecentSearchesUseCase

    private lateinit var viewModel: SearchViewModel

    private val mockQuestions = listOf(
        Question(
            questionId = 1L,
            title = "How to use Kotlin coroutines?",
            body = "<p>Sample body</p>",
            score = 42,
            answerCount = 5,
            viewCount = 1000,
            isAnswered = true,
            acceptedAnswerId = 100L,
            owner = Owner("John", null, 5000, null),
            tags = listOf("kotlin", "coroutines"),
            creationDate = 1_700_000_000L,
            link = "https://stackoverflow.com/q/1"
        ),
        Question(
            questionId = 2L,
            title = "Android MVVM architecture",
            body = "<p>Another body</p>",
            score = 30,
            answerCount = 3,
            viewCount = 500,
            isAnswered = false,
            acceptedAnswerId = null,
            owner = Owner("Jane", null, 3000, null),
            tags = listOf("android", "mvvm"),
            creationDate = 1_700_000_000L,
            link = "https://stackoverflow.com/q/2"
        )
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        every { getRecentSearchesUseCase() } returns flowOf(emptyList())
        viewModel = SearchViewModel(
            searchQuestionsUseCase,
            getRecentSearchesUseCase,
            saveRecentSearchUseCase,
            deleteRecentSearchUseCase,
            clearRecentSearchesUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // --- Initial state ---

    @Test
    fun `initial state is correct`() {
        val state = viewModel.uiState.value
        assertTrue(state.query.isEmpty())
        assertTrue(state.questions.isEmpty())
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertFalse(state.isNoInternet)
    }

    // --- Search success ---

    @Test
    fun `search success updates questions list`() = runTest {
        coEvery { searchQuestionsUseCase("kotlin", 1) } returns Resource.Success(mockQuestions)
        coEvery { saveRecentSearchUseCase("kotlin") } just Runs

        viewModel.search("kotlin")

        val state = viewModel.uiState.value
        assertEquals(2, state.questions.size)
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertFalse(state.isNoInternet)
    }

    @Test
    fun `search success sets correct query`() = runTest {
        coEvery { searchQuestionsUseCase("kotlin", 1) } returns Resource.Success(mockQuestions)
        coEvery { saveRecentSearchUseCase("kotlin") } just Runs

        viewModel.search("kotlin")

        assertEquals("kotlin", viewModel.uiState.value.query)
    }

    @Test
    fun `search clears previous results before fetching`() = runTest {
        coEvery { searchQuestionsUseCase("kotlin", 1) } returns Resource.Success(mockQuestions)
        coEvery { saveRecentSearchUseCase("kotlin") } just Runs
        viewModel.search("kotlin")

        coEvery { searchQuestionsUseCase("android", 1) } returns Resource.Success(listOf(mockQuestions[1]))
        coEvery { saveRecentSearchUseCase("android") } just Runs
        viewModel.search("android")

        assertEquals(1, viewModel.uiState.value.questions.size)
    }

    // --- Search error ---

    @Test
    fun `search generic error sets error message`() = runTest {
        coEvery { searchQuestionsUseCase("kotlin", 1) } returns Resource.Error("Server error")
        coEvery { saveRecentSearchUseCase("kotlin") } just Runs

        viewModel.search("kotlin")

        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertEquals("Server error", state.error)
        assertFalse(state.isNoInternet)
    }

    @Test
    fun `search no internet error sets isNoInternet flag`() = runTest {
        coEvery { searchQuestionsUseCase("kotlin", 1) } returns Resource.Error("No internet connection")
        coEvery { saveRecentSearchUseCase("kotlin") } just Runs

        viewModel.search("kotlin")

        val state = viewModel.uiState.value
        assertTrue(state.isNoInternet)
        assertNull(state.error)
    }

    @Test
    fun `search with blank query does nothing`() = runTest {
        viewModel.search("")

        coVerify(exactly = 0) { searchQuestionsUseCase(any(), any()) }
        assertFalse(viewModel.uiState.value.isLoading)
    }

    // --- Empty results ---

    @Test
    fun `search with no results sets empty list`() = runTest {
        coEvery { searchQuestionsUseCase("xyznotfound", 1) } returns Resource.Success(emptyList())
        coEvery { saveRecentSearchUseCase("xyznotfound") } just Runs

        viewModel.search("xyznotfound")

        val state = viewModel.uiState.value
        assertTrue(state.questions.isEmpty())
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    // --- Pagination ---

    @Test
    fun `loadNextPage appends results correctly`() = runTest {
        coEvery { searchQuestionsUseCase("kotlin", 1) } returns Resource.Success(mockQuestions)
        coEvery { saveRecentSearchUseCase("kotlin") } just Runs
        viewModel.search("kotlin")

        val page2Questions = listOf(mockQuestions[0].copy(questionId = 3L))
        coEvery { searchQuestionsUseCase("kotlin", 2) } returns Resource.Success(page2Questions)

        viewModel.loadNextPage()

        assertEquals(3, viewModel.uiState.value.questions.size)
    }

    @Test
    fun `hasMorePages is false when result count is less than 20`() = runTest {
        coEvery { searchQuestionsUseCase("kotlin", 1) } returns Resource.Success(mockQuestions)
        coEvery { saveRecentSearchUseCase("kotlin") } just Runs

        viewModel.search("kotlin")

        assertFalse(viewModel.uiState.value.hasMorePages)
    }

    // --- Query change ---

    @Test
    fun `onQueryChange clears results when query is blank`() = runTest {
        coEvery { searchQuestionsUseCase("kotlin", 1) } returns Resource.Success(mockQuestions)
        coEvery { saveRecentSearchUseCase("kotlin") } just Runs
        viewModel.search("kotlin")

        viewModel.onQueryChange("")

        assertTrue(viewModel.uiState.value.questions.isEmpty())
    }

    // --- Dismiss error ---

    @Test
    fun `dismissError clears error and noInternet state`() = runTest {
        coEvery { searchQuestionsUseCase("kotlin", 1) } returns Resource.Error("No internet connection")
        coEvery { saveRecentSearchUseCase("kotlin") } just Runs
        viewModel.search("kotlin")

        viewModel.dismissError()

        val state = viewModel.uiState.value
        assertNull(state.error)
        assertFalse(state.isNoInternet)
    }

    // --- Recent searches ---

    @Test
    fun `recent searches are observed from use case`() = runTest {
        val recentSearches = listOf(RecentSearch(1L, "kotlin"), RecentSearch(2L, "android"))
        every { getRecentSearchesUseCase() } returns flowOf(recentSearches)

        viewModel = SearchViewModel(
            searchQuestionsUseCase,
            getRecentSearchesUseCase,
            saveRecentSearchUseCase,
            deleteRecentSearchUseCase,
            clearRecentSearchesUseCase
        )

        assertEquals(2, viewModel.uiState.value.recentSearches.size)
    }

    @Test
    fun `deleteRecentSearch calls use case with correct id`() = runTest {
        coEvery { deleteRecentSearchUseCase(1L) } just Runs

        viewModel.deleteRecentSearch(1L)

        coVerify { deleteRecentSearchUseCase(1L) }
    }

    @Test
    fun `clearRecentSearches calls use case`() = runTest {
        coEvery { clearRecentSearchesUseCase() } just Runs

        viewModel.clearRecentSearches()

        coVerify { clearRecentSearchesUseCase() }
    }

    // --- Refresh ---

    @Test
    fun `refresh reloads page 1 results`() = runTest {
        coEvery { searchQuestionsUseCase("kotlin", 1) } returns Resource.Success(mockQuestions)
        coEvery { saveRecentSearchUseCase("kotlin") } just Runs
        viewModel.search("kotlin")

        val refreshedQuestions = listOf(mockQuestions[0])
        coEvery { searchQuestionsUseCase("kotlin", 1) } returns Resource.Success(refreshedQuestions)

        viewModel.refresh()

        assertEquals(1, viewModel.uiState.value.questions.size)
        assertFalse(viewModel.uiState.value.isRefreshing)
    }
}
