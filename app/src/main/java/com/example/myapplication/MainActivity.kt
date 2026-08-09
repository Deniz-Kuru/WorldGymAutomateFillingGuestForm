package com.example.myapplication

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.PersonRepository
import com.example.myapplication.data.UserPreferencesRepository
import com.example.myapplication.data.dataStore
import com.example.myapplication.ui.MainViewModel
import com.example.myapplication.ui.MainViewModelFactory
import com.example.myapplication.ui.screens.AutomationScreen
import com.example.myapplication.ui.screens.EditPersonScreen
import com.example.myapplication.ui.screens.HomeScreen
import com.example.myapplication.ui.screens.SettingsScreen
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
                MainNavigation(viewModel)
            }
        }
    }
}

@Composable
fun MainNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onAddPerson = { navController.navigate("edit_person/-1") },
                onEditPerson = { id -> navController.navigate("edit_person/$id") },
                onAutomate = { id, code, type -> 
                    Log.d("MainActivity", "Navigating to automation. id: $id, code: $code, type: $type")
                    val encodedCode = Uri.encode(code)
                    val encodedType = Uri.encode(type)
                    val route = "automation/$id?dailyPassCode=$encodedCode&visitType=$encodedType"
                    Log.d("MainActivity", "Route: $route")
                    navController.navigate(route) 
                },
                onSettings = { navController.navigate("settings") }
            )
        }
        composable(
            route = "edit_person/{personId}",
            arguments = listOf(navArgument("personId") { type = NavType.IntType })
        ) { backStackEntry ->
            val personId = backStackEntry.arguments?.getInt("personId")
            EditPersonScreen(
                viewModel = viewModel,
                personId = personId,
                onNavigateBack = { navController.popBackStack() }
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
                navArgument("visitType") { type = NavType.StringType; defaultValue = "VIP Guest" }
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
