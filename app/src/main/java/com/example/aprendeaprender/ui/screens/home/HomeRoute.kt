package com.example.aprendeaprender.ui.screens.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.aprendeaprender.ui.components.BottomNavDestination

@Composable
fun HomeRoute(
    uiState: HomeUiState,
    onNavigateToProfile: () -> Unit,
    onNavigateToSubjects: () -> Unit = {},
    onNavigateToTasks: () -> Unit = {},
    onNavigateToChallenges: () -> Unit = {},
    onEnrollSubjectClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    HomeScreen(
        uiState = uiState,
        selectedBottomNav = BottomNavDestination.HOME,
        onBottomNavSelected = { destination ->
            when (destination) {
                BottomNavDestination.HOME -> Unit
                BottomNavDestination.SUBJECTS -> onNavigateToSubjects()
                BottomNavDestination.TASKS -> onNavigateToTasks()
                BottomNavDestination.CHALLENGES -> onNavigateToChallenges()
                BottomNavDestination.PROFILE -> onNavigateToProfile()
            }
        },
        onSubjectsClick = onNavigateToSubjects,
        onTasksTodayClick = onNavigateToTasks,
        onEnrollSubjectClick = onEnrollSubjectClick,
        modifier = modifier
    )
}