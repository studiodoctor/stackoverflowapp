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
package com.stackoverflow.search.presentation.detail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.stackoverflow.search.core.utils.Resource
import com.stackoverflow.search.domain.model.Answer
import com.stackoverflow.search.domain.model.Owner
import com.stackoverflow.search.domain.model.Question
import com.stackoverflow.search.domain.usecase.*
import com.stackoverflow.search.presentation.navigation.NavArgs
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK lateinit var getAnswersUseCase: GetAnswersUseCase
    @MockK lateinit var bookmarkQuestionUseCase: BookmarkQuestionUseCase
    @MockK lateinit var removeBookmarkUseCase: RemoveBookmarkUseCase
    @MockK lateinit var isBookmarkedUseCase: IsBookmarkedUseCase

    private lateinit var viewModel: DetailViewModel

    private val testQuestionId = 12345L
    private val savedStateHandle = SavedStateHandle(mapOf(NavArgs.QUESTION_ID to testQuestionId))

    private val mockQuestion = Question(
        questionId = testQuestionId,
        title = "Presenting modal in iOS 13 fullscreen",
        body = "<p>In iOS 13 there is a new behaviour for modal view controller</p>",
        score = 430,
        answerCount = 27,
        viewCount = 678,
        isAnswered = true,
        acceptedAnswerId = 1L,
        owner = Owner("Lewis Dholpin", null, 2382, null),
        tags = listOf("ios", "swift", "modal"),
        creationDate = 1_561_596_180L,
        link = "https://stackoverflow.com/q/$testQuestionId"
    )

    private val mockAnswers = listOf(
        Answer(
            answerId = 1L,
            questionId = testQuestionId,
            body = "<p>You can set kind to Present Modally</p>",
            score = 528,
            isAccepted = true,
            owner = Owner("Alice", null, 10000, null),
            creationDate = 1_561_600_000L
        ),
        Answer(
            answerId = 2L,
            questionId = testQuestionId,
            body = "<p>Another solution is to use fullScreen</p>",
            score = 120,
            isAccepted = false,
            owner = Owner("Bob", null, 5000, null),
            creationDate = 1_561_700_000L
        ),
        Answer(
            answerId = 3L,
            questionId = testQuestionId,
            body = "<p>Old style trick that still works</p>",
            score = 45,
            isAccepted = false,
            owner = Owner("Charlie", null, 2000, null),
            creationDate = 1_560_000_000L
        )
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = DetailViewModel(
            getAnswersUseCase,
            bookmarkQuestionUseCase,
            removeBookmarkUseCase,
            isBookmarkedUseCase,
            savedStateHandle
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // --- Initialize ---

    @Test
    fun `initialize sets question in state`() = runTest {
        coEvery { isBookmarkedUseCase(testQuestionId) } returns false
        coEvery { getAnswersUseCase(testQuestionId) } returns Resource.Success(mockAnswers)

        viewModel.initialize(mockQuestion)

        assertEquals(mockQuestion, viewModel.uiState.value.question)
    }

    // --- Load answers success ---

    @Test
    fun `loadAnswers success populates answers`() = runTest {
        coEvery { isBookmarkedUseCase(testQuestionId) } returns false
        coEvery { getAnswersUseCase(testQuestionId) } returns Resource.Success(mockAnswers)

        viewModel.initialize(mockQuestion)

        val state = viewModel.uiState.value
        assertEquals(3, state.answers.size)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `loadAnswers defaults to votes sort order`() = runTest {
        coEvery { isBookmarkedUseCase(testQuestionId) } returns false
        coEvery { getAnswersUseCase(testQuestionId) } returns Resource.Success(mockAnswers)

        viewModel.initialize(mockQuestion)

        val answers = viewModel.uiState.value.answers
        assertTrue(answers[0].score >= answers[1].score)
        assertTrue(answers[1].score >= answers[2].score)
    }

    // --- Load answers error ---

    @Test
    fun `loadAnswers error sets error message`() = runTest {
        coEvery { isBookmarkedUseCase(testQuestionId) } returns false
        coEvery { getAnswersUseCase(testQuestionId) } returns Resource.Error("Failed to load answers")

        viewModel.initialize(mockQuestion)

        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun `loadAnswers no internet sets isNoInternet flag`() = runTest {
        coEvery { isBookmarkedUseCase(testQuestionId) } returns false
        coEvery { getAnswersUseCase(testQuestionId) } returns Resource.Error("No internet connection")

        viewModel.initialize(mockQuestion)

        val state = viewModel.uiState.value
        assertTrue(state.isNoInternet)
        assertNull(state.error)
    }

    // --- Sort order ---

    @Test
    fun `setSortOrder OLDEST sorts by creation date ascending`() = runTest {
        coEvery { isBookmarkedUseCase(testQuestionId) } returns false
        coEvery { getAnswersUseCase(testQuestionId) } returns Resource.Success(mockAnswers)
        viewModel.initialize(mockQuestion)

        viewModel.setSortOrder(AnswerSortOrder.OLDEST)

        val answers = viewModel.uiState.value.answers
        assertTrue(answers[0].creationDate <= answers[1].creationDate)
    }

    @Test
    fun `setSortOrder ACTIVE sorts by creation date descending`() = runTest {
        coEvery { isBookmarkedUseCase(testQuestionId) } returns false
        coEvery { getAnswersUseCase(testQuestionId) } returns Resource.Success(mockAnswers)
        viewModel.initialize(mockQuestion)

        viewModel.setSortOrder(AnswerSortOrder.ACTIVE)

        val answers = viewModel.uiState.value.answers
        assertTrue(answers[0].creationDate >= answers[1].creationDate)
    }

    @Test
    fun `setSortOrder VOTES sorts by score descending`() = runTest {
        coEvery { isBookmarkedUseCase(testQuestionId) } returns false
        coEvery { getAnswersUseCase(testQuestionId) } returns Resource.Success(mockAnswers)
        viewModel.initialize(mockQuestion)

        viewModel.setSortOrder(AnswerSortOrder.VOTES)

        val answers = viewModel.uiState.value.answers
        assertTrue(answers[0].score >= answers[1].score)
    }

    // --- Bookmark ---

    @Test
    fun `toggleBookmark bookmarks question when not bookmarked`() = runTest {
        coEvery { isBookmarkedUseCase(testQuestionId) } returns false
        coEvery { getAnswersUseCase(testQuestionId) } returns Resource.Success(mockAnswers)
        coEvery { bookmarkQuestionUseCase(mockQuestion) } just Runs
        viewModel.initialize(mockQuestion)

        viewModel.toggleBookmark()

        assertTrue(viewModel.uiState.value.isBookmarked)
        coVerify { bookmarkQuestionUseCase(mockQuestion) }
    }

    @Test
    fun `toggleBookmark removes bookmark when already bookmarked`() = runTest {
        coEvery { isBookmarkedUseCase(testQuestionId) } returns true
        coEvery { getAnswersUseCase(testQuestionId) } returns Resource.Success(mockAnswers)
        coEvery { removeBookmarkUseCase(testQuestionId) } just Runs
        viewModel.initialize(mockQuestion)

        viewModel.toggleBookmark()

        assertFalse(viewModel.uiState.value.isBookmarked)
        coVerify { removeBookmarkUseCase(testQuestionId) }
    }

    @Test
    fun `isBookmarked is correctly set on initialize`() = runTest {
        coEvery { isBookmarkedUseCase(testQuestionId) } returns true
        coEvery { getAnswersUseCase(testQuestionId) } returns Resource.Success(mockAnswers)

        viewModel.initialize(mockQuestion)

        assertTrue(viewModel.uiState.value.isBookmarked)
    }

    // --- Dismiss error ---

    @Test
    fun `dismissError clears error and noInternet state`() = runTest {
        coEvery { isBookmarkedUseCase(testQuestionId) } returns false
        coEvery { getAnswersUseCase(testQuestionId) } returns Resource.Error("No internet connection")
        viewModel.initialize(mockQuestion)

        viewModel.dismissError()

        assertNull(viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isNoInternet)
    }
}
