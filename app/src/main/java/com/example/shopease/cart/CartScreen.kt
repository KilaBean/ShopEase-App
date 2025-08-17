package com.example.shopease.cart

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.shopease.data.CartItem
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(cartViewModel: CartViewModel, navController: NavController) {
    // Get the actual list from the State
    val cartItems by cartViewModel.cartItems

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Your Cart") },
            colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)) // Subtle primary tint
        )
    }) { paddingValues ->
        Column(
            Modifier
                .padding(paddingValues)
                .background(Color(0xFFFFFFFF))
                .fillMaxSize()
        ) {
            if (cartItems.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Your cart is empty.")
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    // Use the actual list for items
                    items(cartItems) { item ->
                        CartItemRow(item, cartViewModel)
                    }
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "GHS ${String.format(Locale.US, "%.2f", cartViewModel.getTotal())}",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Button(
                    onClick = { navController.navigate("checkout") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Checkout")
                }
            }
        }
    }
}

@Composable
fun CartItemRow(item: CartItem, cartViewModel: CartViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(
                    id = LocalContext.current.resources.getIdentifier(
                        item.product.imageRes, "drawable", LocalContext.current.packageName
                    )
                ),
                contentDescription = item.product.name,
                modifier = Modifier.size(64.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(item.product.name)
                Text("GHS ${item.product.price}")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { cartViewModel.decreaseQuantity(item) }) { Text("-") }
                // Access the quantity value directly
                Text(item.quantity.toString(), Modifier.padding(horizontal = 8.dp))
                IconButton(onClick = { cartViewModel.increaseQuantity(item) }) { Text("+") }
                IconButton(onClick = { cartViewModel.removeItem(item) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove")
                }
            }
        }
    }
}