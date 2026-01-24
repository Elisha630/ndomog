package com.ndomog.inventory.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ndomog.inventory.data.repository.AuthRepository
import com.ndomog.inventory.presentation.auth.AuthViewModel
import com.ndomog.inventory.presentation.auth.LoginScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ndomog.inventory.di.ViewModelFactory
import com.ndomog.inventory.presentation.categories.CategoriesScreen
import com.ndomog.inventory.presentation.dashboard.DashboardScreen
import com.ndomog.inventory.presentation.profile.ProfileScreen

object Routes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val PROFILE = "profile"
    const val CATEGORIES = "categories"
}

@Composable
fun AppNavigation(
    authRepository: AuthRepository
) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(factory = ViewModelFactory(authRepository))

    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreen(authViewModel = authViewModel) {
                navController.navigate(Routes.DASHBOARD) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
            }
        }
        composable(Routes.DASHBOARD) {
            DashboardScreen(onLogout = {
                // Handle logout logic if needed, then navigate to login
                navController.navigate(Routes.LOGIN) {
                    popUpTo(Routes.DASHBOARD) { inclusive = true }
                }
            })
        }
        composable(Routes.PROFILE) {
            ProfileScreen()
        }
        composable(Routes.CATEGORIES) {
            CategoriesScreen()
        }
    }
}
