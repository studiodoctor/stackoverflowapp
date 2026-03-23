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

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stackoverflow.search.domain.model.Answer
import com.stackoverflow.search.domain.model.Question
import com.stackoverflow.search.presentation.common.*
import com.stackoverflow.search.presentation.search.formatCount
import com.stackoverflow.search.core.utils.DateUtils
import androidx.compose.ui.graphics.Color
import com.stackoverflow.search.presentation.theme.SGreen
import com.stackoverflow.search.presentation.theme.SOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    question: Question,
    onBackClick: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(question.questionId) {
        viewModel.initialize(question)
    }

    if (uiState.isNoInternet) {
        NoInternetDialog(
            onRetry = viewModel::loadAnswers,
            onDismiss = viewModel::dismissError
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("More Info", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::toggleBookmark) {
                        Icon(
                            imageVector = if (uiState.isBookmarked) Icons.Default.Bookmark
                            else Icons.Default.BookmarkBorder,
                            contentDescription = if (uiState.isBookmarked) "Remove bookmark"
                            else "Bookmark",
                            tint = if (uiState.isBookmarked) SOrange
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { uriHandler.openUri(question.link) }) {
                        Icon(
                            Icons.Default.OpenInBrowser,
                            contentDescription = "Open in browser"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("detail_screen"),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Question header
            item { QuestionHeader(question = question) }

            // Question body
            item {
                HorizontalDivider()
                HtmlContent(
                    html = question.body,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            // Tags
            if (question.tags.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        question.tags.forEach { tag -> TagChip(tag) }
                    }
                }
            }

            // Owner info
            item {
                OwnerInfoCard(question = question)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // Answers header with sort
            item {
                AnswersHeader(
                    count = uiState.answers.size,
                    sortOrder = uiState.answerSortOrder,
                    onSortChange = viewModel::setSortOrder
                )
            }

            // Loading
            if (uiState.isLoading) {
                item { LoadingIndicator(Modifier.height(150.dp)) }
            }

            // Error
            if (uiState.error != null) {
                item {
                    ErrorMessage(
                        message = uiState.error!!,
                        onRetry = viewModel::loadAnswers
                    )
                }
            }

            // Answers list
            items(uiState.answers, key = { it.answerId }) { answer ->
                AnswerItem(answer = answer)
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            }

            // No answers yet
            if (!uiState.isLoading && uiState.answers.isEmpty() && uiState.error == null) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No answers yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionHeader(question: Question) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = question.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("question_title")
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            MetaStat(
                label = "Asked",
                value = DateUtils.formatRelativeTime(
                    question.creationDate
                )
            )
            MetaStat(
                label = "Viewed",
                value = "${formatCount(question.viewCount)} times"
            )
        }
    }
}

@Composable
private fun MetaStat(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun OwnerInfoCard(question: Question) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            OwnerRow(
                owner = question.owner,
                date = question.creationDate,
                prefix = "Asked"
            )
        }
    }
}

@Composable
private fun AnswersHeader(
    count: Int,
    sortOrder: AnswerSortOrder,
    onSortChange: (AnswerSortOrder) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$count Answers",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            AnswerSortOrder.entries.forEach { order ->
                FilterChip(
                    selected = sortOrder == order,
                    onClick = { onSortChange(order) },
                    label = {
                        Text(
                            text = order.name.lowercase()
                                .replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SOrange,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}

@Composable
fun AnswerItem(answer: Answer) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Vote / accepted column
            Column(
                modifier = Modifier.width(52.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = formatCount(answer.score),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (answer.score > 0) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Votes",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (answer.isAccepted) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Accepted answer",
                        tint = SGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Body
            Column(modifier = Modifier.weight(1f)) {
                HtmlContent(html = answer.body)
                Spacer(Modifier.height(8.dp))
                OwnerRow(
                    owner = answer.owner,
                    date = answer.creationDate,
                    prefix = "answered"
                )
            }
        }
    }
}
