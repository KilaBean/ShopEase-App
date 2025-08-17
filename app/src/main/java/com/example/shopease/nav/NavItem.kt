package com.example.shopease.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavItem(val title: String, val icon: ImageVector, val route: String) {
    object Home : NavItem("Home", Icons.Filled.Home, "home")
    object Cart : NavItem("Cart", Icons.Filled.ShoppingCart, "cart")
}
