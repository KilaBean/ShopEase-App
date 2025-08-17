package com.example.shopease.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class Product(
    val name: String,
    val imageRes: String,
    val price: Double,
    val description: String,
    val category: String  // <-- NEW
)

class CartItem(
    val product: Product,
    quantity: Int
) {
    var quantity by mutableStateOf(quantity)
}

object ProductRepository {
    fun getProducts(): List<Product> {
        return listOf(
            Product("Red Sneakers", "shoe1", 99.99, "Trendy red sneakers", "Shoes"),
            Product("Sneakers", "sneaker1", 99.99, "Trendy sneakers", "Shoes"),
            Product("Red Sneakers", "sneaker2", 99.99, "Trendy sneakers", "Shoes"),
            Product("Black Hoodie", "hoodie", 59.49, "Cozy cotton hoodie", "Clothing"),
            Product("Blue Jeans", "jeans", 75.0, "Slim-fit blue jeans", "Clothing"),
            Product("Leather Wallet", "wallet", 30.0, "Stylish wallet for men", "Accessories"),
            Product("Smart Watch", "watch", 120.0, "Fitness smart watch", "Accessories"),
            Product("Smart Watch", "smartwatch1", 120.0, "Fitness smart watch", "Accessories"),
            Product("Smart Watch", "smartwatch2", 120.0, "Fitness smart watch", "Accessories"),
            Product("Laptop", "laptop1", 120.0, "High performance PC", "Accessories"),
            Product("Laptop", "laptop2", 120.0, "High performance PC", "Accessories"),
            Product("Backpack", "backpack", 40.0, "Durable travel backpack", "Bags"),
            Product("Backpack", "bagpack1", 40.0, "Durable travel backpack", "Bags"),
            Product("Backpack", "bagpack3", 40.0, "Durable travel backpack", "Bags")

        )
    }
}

