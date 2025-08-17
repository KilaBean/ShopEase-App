package com.example.shopease.cart

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.lifecycle.ViewModel
import com.example.shopease.data.CartItem
import com.example.shopease.data.Product

class CartViewModel : ViewModel() {
    private val _cartItems = mutableStateListOf<CartItem>()
    val cartItems: State<List<CartItem>> = derivedStateOf { _cartItems }

    fun addToCart(product: Product) {
        val existingItem = _cartItems.find { it.product.name == product.name }
        if (existingItem != null) {
            existingItem.quantity++
        } else {
            _cartItems.add(CartItem(product, 1))
        }
    }

    fun increaseQuantity(item: CartItem) {
        item.quantity++
    }

    fun decreaseQuantity(item: CartItem) {
        if (item.quantity > 1) {
            item.quantity--
        } else {
            _cartItems.remove(item)
        }
    }

    fun removeItem(item: CartItem) {
        _cartItems.remove(item)
    }

    fun getTotal(): Double = _cartItems.sumOf { it.product.price * it.quantity }

    // Add a helper function to clear the cart
    fun clearCart() {
        _cartItems.clear()
    }
}