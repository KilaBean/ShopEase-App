package com.example.shopease.home

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.example.shopease.data.Product
import com.example.shopease.data.ProductRepository

class HomeViewModel : ViewModel() {
    private val allProducts = ProductRepository.getProducts()

    var searchQuery by mutableStateOf("")
    var selectedCategory by mutableStateOf("All")

    val filteredProducts: List<Product>
        get() = allProducts.filter {
            (selectedCategory == "All" || it.category == selectedCategory) &&
                    it.name.contains(searchQuery, ignoreCase = true)
        }

    val categories: List<String> = listOf("All") + allProducts.map { it.category }.distinct()
}
