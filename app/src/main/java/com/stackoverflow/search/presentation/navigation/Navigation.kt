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
package com.stackoverflow.search.presentation.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.stackoverflow.search.domain.model.Question
import com.stackoverflow.search.presentation.detail.DetailScreen
import com.stackoverflow.search.presentation.search.SearchScreen
import com.google.gson.Gson

object NavArgs {
    const val QUESTION_ID = "questionId"
    const val QUESTION_JSON = "questionJson"
}

sealed class Screen(val route: String) {
    data object Search : Screen("search")
    data object Detail : Screen("detail/{${NavArgs.QUESTION_ID}}/{${NavArgs.QUESTION_JSON}}") {
        fun createRoute(question: Question): String {
            val json = Uri.encode(Gson().toJson(question))
            return "detail/${question.questionId}/$json"
        }
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Search.route
    ) {
        composable(Screen.Search.route) {
            SearchScreen(
                onQuestionClick = { question ->
                    navController.navigate(Screen.Detail.createRoute(question))
                }
            )
        }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument(NavArgs.QUESTION_ID) { type = NavType.LongType },
                navArgument(NavArgs.QUESTION_JSON) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val questionJson = backStackEntry.arguments?.getString(NavArgs.QUESTION_JSON) ?: ""
            val question = Gson().fromJson(Uri.decode(questionJson), Question::class.java)
            DetailScreen(
                question = question,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
