package com.stackoverflow.search.presentation.detail

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.stackoverflow.search.domain.model.Owner
import com.stackoverflow.search.domain.model.Question
import com.stackoverflow.search.presentation.theme.StackOverflowTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.*
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class DetailScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    private val testQuestion = Question(
        questionId = 12345L,
        title = "How to use Jetpack Compose navigation?",
        body = "<p>I want to navigate between screens.</p>",
        score = 42,
        answerCount = 5,
        viewCount = 1000,
        isAnswered = true,
        acceptedAnswerId = null,
        owner = Owner("TestDev", null, 5000, null),
        tags = listOf("android", "compose", "navigation"),
        creationDate = 1_700_000_000L,
        link = "https://stackoverflow.com/q/12345"
    )

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun questionTitleIsDisplayed() {
        composeTestRule.setContent {
            StackOverflowTheme {
                DetailScreen(
                    question = testQuestion,
                    onBackClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithTag("question_title")
            .assertIsDisplayed()
    }

    @Test
    fun backButtonIsDisplayed() {
        composeTestRule.setContent {
            StackOverflowTheme {
                DetailScreen(
                    question = testQuestion,
                    onBackClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Back")
            .assertIsDisplayed()
    }

    @Test
    fun backButtonTriggerCallback() {
        var clicked = false
        composeTestRule.setContent {
            StackOverflowTheme {
                DetailScreen(
                    question = testQuestion,
                    onBackClick = { clicked = true }
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Back")
            .performClick()

        assert(clicked)
    }

    @Test
    fun moreInfoTitleIsDisplayed() {
        composeTestRule.setContent {
            StackOverflowTheme {
                DetailScreen(
                    question = testQuestion,
                    onBackClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("More Info")
            .assertIsDisplayed()
    }

    @Test
    fun bookmarkButtonIsDisplayed() {
        composeTestRule.setContent {
            StackOverflowTheme {
                DetailScreen(
                    question = testQuestion,
                    onBackClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Bookmark")
            .assertIsDisplayed()
    }

    @Test
    fun detailScreenIsDisplayed() {
        composeTestRule.setContent {
            StackOverflowTheme {
                DetailScreen(
                    question = testQuestion,
                    onBackClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithTag("detail_screen")
            .assertIsDisplayed()
    }
}
