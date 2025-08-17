package com.example.shopease.checkout

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.shopease.cart.CartViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(cartViewModel: CartViewModel) {
    val context = LocalContext.current

    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Cash on Delivery") }
    var notes by remember { mutableStateOf("") }

    val totalPrice = cartViewModel.getTotal()

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Checkout") },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFB2DFDB))
        )
    }) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .background(Color(0xFFFFFFFF)),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Shipping Address") },
                    placeholder = { Text("Enter your delivery address") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )

                DropdownMenuBox(
                    current = paymentMethod,
                    options = listOf("Cash on Delivery", "Mobile Money", "Card Payment")
                ) { selected ->
                    paymentMethod = selected
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Additional Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Total: GHS ${String.format(Locale.US, "%.2f", totalPrice)}",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Button(
                onClick = {
                    if (fullName.isNotBlank() && phoneNumber.isNotBlank() && address.isNotBlank()) {
                        Toast.makeText(context, "Order Placed Successfully!", Toast.LENGTH_LONG).show()
                        // Optionally: cartViewModel.clearCart()
                    } else {
                        Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Text("Place Order")
            }
        }
    }
}

@Composable
fun DropdownMenuBox(current: String, options: List<String>, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedTextField(
            value = current,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Payment Method") },
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select")
                }
            }
        )

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach {
                DropdownMenuItem(
                    text = { Text(it) },
                    onClick = {
                        onSelected(it)
                        expanded = false
                    }
                )
            }
        }
    }
}
