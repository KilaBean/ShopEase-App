package com.example.shopease.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.shopease.AuthViewModel
import com.example.shopease.auth.LoginScreen
import com.example.shopease.auth.RegisterScreen
import com.example.shopease.cart.CartScreen
import com.example.shopease.cart.CartViewModel
import com.example.shopease.checkout.CheckoutScreen
import com.example.shopease.home.HomeScreen

@Composable
fun AppNav(authViewModel: AuthViewModel) {
    val navController: NavHostController = rememberNavController()
    val currentBackStack = navController.currentBackStackEntryAsState().value
    val currentRoute = currentBackStack?.destination?.route
    val showBottomBar = currentRoute == NavItem.Home.route || currentRoute == NavItem.Cart.route

    // Observe authentication state
    val isAuthenticated by authViewModel.isAuthenticated

    // Set start destination based on auth state
    val startDestination = if (isAuthenticated) NavItem.Home.route else "login"

    // Handle navigation when auth state changes
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            // User is authenticated, navigate to home
            navController.navigate(NavItem.Home.route) {
                // Clear back stack so user can't go back to login screen
                popUpTo("login") { inclusive = true }
                popUpTo("register") { inclusive = true }
            }
        } else {
            // User is not authenticated, navigate to login
            navController.navigate("login") {
                popUpTo(NavItem.Home.route) { inclusive = true }
            }
        }
    }

    // Lift CartViewModel to AppNav
    val cartViewModel: CartViewModel = viewModel()

    Scaffold(
        bottomBar = {
            if (showBottomBar && isAuthenticated) {
                BottomNavBar(navController, cartViewModel)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") { LoginScreen(navController, authViewModel) }
            composable("register") { RegisterScreen(navController, authViewModel) }
            composable(NavItem.Home.route) {
                HomeScreen(cartViewModel = cartViewModel, navController = navController, authViewModel = authViewModel)
            }
            composable(NavItem.Cart.route) { CartScreen(cartViewModel, navController) }
            composable("checkout") { CheckoutScreen(cartViewModel) }
        }
    }
}