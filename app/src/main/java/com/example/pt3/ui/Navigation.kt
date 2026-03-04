package com.example.pt3.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pt3.ui.screens.*

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController, viewModel) }
        composable("register") { RegisterScreen(navController, viewModel) }
        composable("home_daotao") { DaotaoHomeScreen(navController, viewModel) }
        composable("home_sinhvien/{studentId}") { backStackEntry ->
            val studentId = backStackEntry.arguments?.getString("studentId")
            SinhvienDetailScreen(navController, viewModel, studentId)
        }
        composable("edit_sinhvien/{studentId}") { backStackEntry ->
            val studentId = backStackEntry.arguments?.getString("studentId")
            EditSinhvienScreen(navController, viewModel, studentId)
        }
        composable("nganh_list") { NganhListScreen(navController, viewModel) }
    }
}
