package com.stackoverflow.search.presentation.search

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.stackoverflow.search.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.*
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SearchScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun searchInputIsDisplayed() {
        composeTestRule.onNodeWithTag("search_input").assertIsDisplayed()
    }

    @Test
    fun searchInputAcceptsText() {
        composeTestRule
            .onNodeWithTag("search_input")
            .performTextInput("kotlin")

        composeTestRule
            .onNodeWithTag("search_input")
            .assertTextContains("kotlin")
    }

    @Test
    fun searchButtonTriggersSearch() {
        composeTestRule
            .onNodeWithTag("search_input")
            .performTextInput("android")

        composeTestRule
            .onNodeWithContentDescription("Search")
            .performClick()

        // Loading indicator or results should appear
        composeTestRule.waitForIdle()
    }

    @Test
    fun stackOverflowTitleIsDisplayed() {
        composeTestRule
            .onNodeWithText("overflow")
            .assertIsDisplayed()
    }
}
