package com.example.myapplication

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.PersonRepository
import com.example.myapplication.data.UserPreferencesRepository
import com.example.myapplication.data.dataStore
import com.example.myapplication.ui.MainViewModel
import com.example.myapplication.ui.MainViewModelFactory
import com.example.myapplication.ui.screens.*
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = AppDatabase.getDatabase(applicationContext)
        val personRepository = PersonRepository(database.personDao())
        val userPreferencesRepository = UserPreferencesRepository(applicationContext.dataStore)
        val viewModel: MainViewModel by viewModels {
            MainViewModelFactory(personRepository, userPreferencesRepository)
        }

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainScreen(viewModel)
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Guest,
        Screen.Membership
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            
            // Show bottom bar for top-level destinations
            val isTopLevel = items.any { it.route == currentDestination?.route }

            if (isTopLevel) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(screen.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        MainNavigation(viewModel, navController, Modifier.padding(innerPadding))
    }
}

sealed class Screen(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Guest : Screen("guest", "Guest", Icons.Default.Group)
    object Membership : Screen("membership", "Membership", Icons.Default.CreditCard)
}

@Composable
fun MainNavigation(viewModel: MainViewModel, navController: androidx.navigation.NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController, 
        startDestination = Screen.Guest.route,
        modifier = modifier
    ) {
        composable(Screen.Guest.route) {
            HomeScreen(
                viewModel = viewModel,
                onAddPerson = { navController.navigate("edit_person/-1") },
                onEditPerson = { id -> navController.navigate("edit_person/$id") },
                onAutomate = { id, code, type -> 
                    Log.d("MainActivity", "Navigating to automation. id: $id, code: $code, type: $type")
                    val encodedCode = Uri.encode(code)
                    val encodedType = Uri.encode(type)
                    val route = "automation/$id?dailyPassCode=$encodedCode&visitType=$encodedType"
                    navController.navigate(route) 
                },
                onSettings = { navController.navigate("settings") },
            )
        }
        composable(Screen.Membership.route) {
            MembershipScreen(viewModel = viewModel)
        }
        composable(
            route = "edit_person/{personId}",
            arguments = listOf(navArgument("personId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val personId = backStackEntry.arguments?.getInt("personId")
            EditPersonScreen(
                viewModel = viewModel,
                personId = personId,
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable("settings") {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "automation/{personId}?dailyPassCode={dailyPassCode}&visitType={visitType}",
            arguments = listOf(
                navArgument("personId") { type = NavType.IntType },
                navArgument("dailyPassCode") { type = NavType.StringType; defaultValue = "" },
                navArgument("visitType") { type = NavType.StringType; defaultValue = "VIP Guest" },
            )
        ) { backStackEntry ->
            val personId = backStackEntry.arguments?.getInt("personId") ?: -1
            val dailyPassCode = backStackEntry.arguments?.getString("dailyPassCode") ?: ""
            val visitType = backStackEntry.arguments?.getString("visitType") ?: ""
            AutomationScreen(
                viewModel = viewModel,
                personId = personId,
                dailyPassCode = dailyPassCode,
                visitType = visitType,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
