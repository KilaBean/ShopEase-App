package com.example.shopease.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.shopease.AuthViewModel
import com.example.shopease.R
import com.example.shopease.cart.CartViewModel
import com.example.shopease.data.ProductRepository
import com.example.shopease.ui.theme.PrimaryLight
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    cartViewModel: CartViewModel,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val auth = Firebase.auth
    val currentUser = auth.currentUser
    val allProducts = remember { ProductRepository.getProducts() }
    val categories = remember {
        listOf("All") + allProducts.map { it.category }.distinct()
    }
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    val filteredProducts = allProducts.filter {
        (selectedCategory == "All" || it.category == selectedCategory) &&
                it.name.contains(searchQuery, ignoreCase = true)
    }

    // Drawer state
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Navigation items - removed Home and Cart
    val navItems = listOf(
        NavItem("Categories", Icons.Outlined.Category, Icons.Filled.Category),
        NavItem("Deals", Icons.Outlined.LocalOffer, Icons.Filled.LocalOffer),
        NavItem("Orders", Icons.Outlined.Receipt, Icons.Filled.Receipt),
        NavItem("Wishlist", Icons.Outlined.FavoriteBorder, Icons.Filled.Favorite),
        NavItem("Settings", Icons.Outlined.Settings, Icons.Filled.Settings)
    )

    // Collect cart items state
    val cartItems by cartViewModel.cartItems
    val cartCount = cartItems.sumOf { it.quantity } // Sum of quantities instead of item count

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(280.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerContentColor = MaterialTheme.colorScheme.onSurface
            ) {
                // Drawer header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(24.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = currentUser?.displayName ?: "Guest User",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 18.sp
                            )
                            Text(
                                text = currentUser?.email ?: "Not signed in",
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                fontSize = 14.sp
                            )
                        }
                    }

                    if (currentUser == null) {
                        OutlinedButton(
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate("login")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text("Sign In")
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                // Sign out
                                auth.signOut()
                                authViewModel.updateAuthState(false)
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ExitToApp, contentDescription = "Sign Out")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sign Out")
                            }
                        }
                    }
                }

                // Navigation items
                Spacer(modifier = Modifier.height(12.dp))
                navItems.forEach { item ->
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = item.unselectedIcon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            // Handle navigation
                            when (item.label) {
                                "Categories" -> navController.navigate("categories")
                                "Deals" -> navController.navigate("deals")
                                "Orders" -> navController.navigate("orders")
                                "Wishlist" -> navController.navigate("wishlist")
                                "Settings" -> navController.navigate("settings")
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = Color.Transparent,
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                // App version
                Text(
                    text = "Version 1.0.0",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 16.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                Column(
                    Modifier
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(bottom = 8.dp)
                ) {
                    TopAppBar(
                        title = { /* Empty title - no ShopEase text */ },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        },
                        actions = {
                            // Notification icon with badge
                            IconButton(onClick = {
                                scope.launch {
                                    drawerState.close()
                                    navController.navigate("notifications")
                                }
                            }) {
                                BadgedBox(
                                    badge = {
                                        Badge {
                                            Text("3")
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Notifications,
                                        contentDescription = "Notifications",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    // Search Field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("🔍 Search products...") },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Gray,
                            unfocusedBorderColor = Color.LightGray,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Clear search",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    )
                    // Category Chips
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp, start = 12.dp, bottom = 6.dp)
                    ) {
                        items(categories.size) { index ->
                            val category = categories[index]
                            val emoji = when (category.lowercase()) {
                                "shoes" -> "👟"
                                "hoodie", "sweater", "jacket" -> "🧥"
                                "jeans", "pants" -> "👖"
                                "accessories", "wallet" -> "👝"
                                "watch" -> "⌚"
                                "backpack", "bag" -> "🎒"
                                else -> "🛍️"
                            }
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category },
                                label = { Text("$emoji $category") },
                                modifier = Modifier.padding(end = 8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryLight.copy(alpha = 1.0f),
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                leadingIcon = if (selectedCategory == category) {
                                    { Icon(Icons.Default.Check, contentDescription = null) }
                                } else null
                            )
                        }
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredProducts) { product ->
                    ProductCard(
                        product = product,
                        onAddToCart = { cartViewModel.addToCart(product) }
                    )
                }
            }
        }
    }
}

data class NavItem(
    val label: String,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector
)