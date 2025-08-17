package com.example.shopease.nav

import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Modifier
import com.example.shopease.cart.CartViewModel

@Composable
fun BottomNavBar(navController: NavController, cartViewModel: CartViewModel) {
    val items = listOf(NavItem.Home, NavItem.Cart)
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    // Get cart count for badge
    val cartItems by cartViewModel.cartItems
    val cartCount = cartItems.sumOf { it.quantity }

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    if (item == NavItem.Cart && cartCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                ) {
                                    Text(
                                        text = cartCount.coerceAtMost(99).toString(),
                                        fontSize = MaterialTheme.typography.labelSmall.fontSize
                                    )
                                }
                            }
                        ) {
                            Icon(imageVector = item.icon, contentDescription = item.title)
                        }
                    } else {
                        Icon(imageVector = item.icon, contentDescription = item.title)
                    }
                },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = Color.Gray,
                    indicatorColor = Color.White
                )
            )
        }
    }
}