package com.zxy.daydayplan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.zxy.daydayplan.domain.model.PlanItemType
import com.zxy.daydayplan.ui.components.ScheduleEditorDialog
import com.zxy.daydayplan.ui.components.TodoEditorDialog
import com.zxy.daydayplan.ui.screen.CompletedScreen
import com.zxy.daydayplan.ui.screen.OverviewScreen
import com.zxy.daydayplan.ui.screen.ReviewScreen
import com.zxy.daydayplan.ui.screen.ScheduleScreen
import com.zxy.daydayplan.ui.screen.TodoScreen
import com.zxy.daydayplan.ui.theme.BrandPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDayPlanApp(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentTab = AppTab.fromRoute(backStackEntry?.destination?.route ?: uiState.selectedTab.route)

    LaunchedEffect(uiState.selectedTab) {
        if (currentTab.route != uiState.selectedTab.route) {
            navController.navigate(uiState.selectedTab.route) {
                popUpTo(navController.graph.startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    Box(
        modifier = Modifier.background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    BrandPrimary.copy(alpha = 0.12f),
                    MaterialTheme.colorScheme.background,
                    MaterialTheme.colorScheme.background
                )
            )
        )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = currentTab.label,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "日日计划",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                    tonalElevation = BottomAppBarDefaults.ContainerElevation
                ) {
                    AppTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = currentTab == tab,
                            onClick = {
                                viewModel.selectTab(tab)
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            },
            floatingActionButton = {
                when (currentTab) {
                    AppTab.Schedule -> {
                        FloatingActionButton(
                            containerColor = BrandPrimary,
                            contentColor = Color.White,
                            onClick = { viewModel.openScheduleEditor() }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "添加日程")
                        }
                    }

                    AppTab.Todo -> {
                        FloatingActionButton(
                            containerColor = BrandPrimary,
                            contentColor = Color.White,
                            onClick = { viewModel.openTodoEditor() }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "添加待办")
                        }
                    }

                    else -> Unit
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = AppTab.Overview.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(AppTab.Overview.route) {
                    OverviewScreen(state = uiState)
                }
                composable(AppTab.Schedule.route) {
                    ScheduleScreen(
                        schedules = uiState.pendingSchedules,
                        overdueSchedules = uiState.overdueSchedules,
                        onToggle = { viewModel.toggleCompletion(it, PlanItemType.SCHEDULE) },
                        onEdit = { viewModel.openScheduleEditor(it) },
                        onDelete = viewModel::deleteSchedule
                    )
                }
                composable(AppTab.Todo.route) {
                    TodoScreen(
                        todos = uiState.pendingTodos,
                        onToggle = { viewModel.toggleCompletion(it, PlanItemType.TODO) },
                        onEdit = { viewModel.openTodoEditor(it) },
                        onDelete = viewModel::deleteTodo,
                        onMoveUp = { viewModel.moveTodo(it, MoveDirection.Up) },
                        onMoveDown = { viewModel.moveTodo(it, MoveDirection.Down) }
                    )
                }
                composable(AppTab.Review.route) {
                    ReviewScreen(
                        selectedDate = uiState.reviewDate,
                        dayPlan = uiState.reviewDayPlan,
                        onSelectDate = viewModel::selectReviewDate
                    )
                }
                composable(AppTab.Completed.route) {
                    CompletedScreen(
                        schedules = uiState.completedSchedules,
                        todos = uiState.completedTodos,
                        onRestoreSchedule = { viewModel.toggleCompletion(it, PlanItemType.SCHEDULE) },
                        onRestoreTodo = { viewModel.toggleCompletion(it, PlanItemType.TODO) }
                    )
                }
            }
        }
    }

    when (val dialog = uiState.activeDialog) {
        EditorDialogState.None -> Unit
        is EditorDialogState.Schedule -> {
            ScheduleEditorDialog(
                initialValue = dialog.item,
                onDismiss = viewModel::closeDialog,
                onSave = viewModel::saveSchedule
            )
        }

        is EditorDialogState.Todo -> {
            TodoEditorDialog(
                initialValue = dialog.item,
                onDismiss = viewModel::closeDialog,
                onSave = viewModel::saveTodo
            )
        }
    }
}
