package com.example.creativecompetitionaggregator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.creativecompetitionaggregator.ui.RemindersScreen
import com.example.creativecompetitionaggregator.ui.RecentCompetitionsScreen
import com.example.creativecompetitionaggregator.ui.theme.CreativeCompetitionAggregatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CreativeCompetitionAggregatorTheme {
                AppNavigation()
            }
        }
    }
}

private object Routes {
    const val RECENT = "recent"
    const val REMINDERS = "reminders"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == Routes.RECENT,
                    onClick = {
                        navController.navigate(Routes.RECENT) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Недавние") },
                    label = { Text("Недавние") }
                )
                NavigationBarItem(
                    selected = currentRoute == Routes.REMINDERS,
                    onClick = {
                        navController.navigate(Routes.REMINDERS) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Напоминания") },
                    label = { Text("Напоминания") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.RECENT,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Routes.RECENT) { RecentCompetitionsScreen() }
            composable(Routes.REMINDERS) { RemindersScreen() }
        }
    }
}
