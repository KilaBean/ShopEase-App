package com.example.shopease.auth

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.shopease.AuthViewModel
import com.example.shopease.R
import com.example.shopease.utils.AuthUtils
import com.example.shopease.utils.GoogleSignInHelper
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import android.util.Log

@Composable
fun LoginScreen(navController: NavController, authViewModel: AuthViewModel) {
    val context = LocalContext.current
    val auth = Firebase.auth

    // Track authentication state to satisfy Compose compiler
    val isAuthenticated by authViewModel.isAuthenticated

    // Handle navigation when authentication state changes
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var googleLoading by remember { mutableStateOf(false) }
    var showUnverifiedDialog by remember { mutableStateOf(false) }
    var showResetPasswordDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var resetEmailError by remember { mutableStateOf<String?>(null) }
    var isResettingPassword by remember { mutableStateOf(false) }
    var showResetSuccessDialog by remember { mutableStateOf(false) }

    // Google Sign-In launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = GoogleAuthResultContract(),
        onResult = { result ->
            googleLoading = false
            Log.d("GoogleSignIn", "Google Sign-In result received: $result")
            result?.let {
                GoogleSignInHelper.handleSignInResult(
                    data = it,
                    auth = auth,
                    context = context,
                    onSuccess = {
                        Log.d("GoogleSignIn", "Google Sign-In successful")
                        // Update auth state on successful Google sign-in
                        authViewModel.updateAuthState(true)
                    },
                    onFailure = { error ->
                        Log.e("GoogleSignIn", "Google Sign-In failed: $error")
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    }
                )
            } ?: run {
                Log.d("GoogleSignIn", "Google Sign-In cancelled by user")
                Toast.makeText(context, "Google sign in cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )

    fun loginUser() {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        isLoading = true
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                isLoading = false
                if (task.isSuccessful) {
                    if (auth.currentUser?.isEmailVerified == true) {
                        // Only update auth state if email is verified
                        authViewModel.updateAuthState(true)
                    } else {
                        showUnverifiedDialog = true
                        // Sign out the user since email is not verified
                        auth.signOut()
                    }
                } else {
                    val error = AuthUtils.getAuthErrorMessage(task.exception)
                    Toast.makeText(context, "Login failed: $error", Toast.LENGTH_LONG).show()
                }
            }
    }

    fun resetPassword() {
        resetEmailError = null

        if (resetEmail.isEmpty()) {
            resetEmailError = "Email is required"
            return
        }

        if (!AuthUtils.isValidEmail(resetEmail)) {
            resetEmailError = "Invalid email format"
            return
        }

        isResettingPassword = true
        auth.sendPasswordResetEmail(resetEmail)
            .addOnCompleteListener { task ->
                isResettingPassword = false
                if (task.isSuccessful) {
                    showResetPasswordDialog = false
                    showResetSuccessDialog = true
                } else {
                    val error = AuthUtils.getAuthErrorMessage(task.exception)
                    when (error) {
                        "There is no user record corresponding to this identifier. The user may have been deleted." -> {
                            resetEmailError = "No account found with this email address"
                        }
                        else -> {
                            resetEmailError = error
                        }
                    }
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome Back",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(32.dp))

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email"
                )
            }
        )

        Spacer(Modifier.height(16.dp))

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Password"
                )
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            }
        )

        Spacer(Modifier.height(8.dp))

        // Forgot Password Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = {
                    resetEmail = email // Pre-fill with current email
                    resetEmailError = null
                    showResetPasswordDialog = true
                },
                modifier = Modifier.padding(0.dp)
            ) {
                Text("Forgot Password?")
            }
        }

        Spacer(Modifier.height(16.dp))

        // Login Button
        Button(
            onClick = { loginUser() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isLoading && email.isNotEmpty() && password.isNotEmpty()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Login")
            }
        }

        Spacer(Modifier.height(16.dp))

        // Divider with "OR" text
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Divider(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
            Text(
                modifier = Modifier.padding(horizontal = 8.dp),
                text = "OR",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Divider(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
        }

        Spacer(Modifier.height(16.dp))

        // Google Sign-In Button
        OutlinedButton(
            onClick = {
                Log.d("GoogleSignIn", "Google Sign-In button clicked")
                googleLoading = true
                try {
                    googleSignInLauncher.launch(context)
                } catch (e: Exception) {
                    googleLoading = false
                    Log.e("GoogleSignIn", "Error launching Google Sign-In", e)
                    Toast.makeText(context, "Error launching Google Sign-In: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            enabled = !googleLoading
        ) {
            if (googleLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_google_logo),
                        contentDescription = "Google Logo",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Continue with Google")
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Sign Up Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Don't have an account?")
            Spacer(Modifier.width(4.dp))
            TextButton(
                onClick = { navController.navigate("register") },
                modifier = Modifier.padding(0.dp)
            ) {
                Text("Sign Up")
            }
        }
    }

    // Unverified Email Dialog
    if (showUnverifiedDialog) {
        AlertDialog(
            onDismissRequest = { showUnverifiedDialog = false },
            title = { Text("Email Not Verified") },
            text = {
                Column {
                    Text("Please verify your email address before logging in.")
                    Spacer(Modifier.height(8.dp))
                    Text("Check your inbox for the verification email we sent to $email")
                }
            },
            confirmButton = {
                Button(onClick = {
                    // Sign in again to resend verification email
                    auth.signInWithEmailAndPassword(email, password)
                        ?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                auth.currentUser?.sendEmailVerification()
                                    ?.addOnCompleteListener { verificationTask ->
                                        if (verificationTask.isSuccessful) {
                                            Toast.makeText(
                                                context,
                                                "Verification email resent",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            showUnverifiedDialog = false
                                            // Sign out again
                                            auth.signOut()
                                        }
                                    }
                            }
                        }
                }) {
                    Text("Resend Verification")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showUnverifiedDialog = false
                    // Sign out if user cancels
                    auth.signOut()
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reset Password Dialog
    if (showResetPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isResettingPassword) {
                    showResetPasswordDialog = false
                }
            },
            title = { Text("Reset Password") },
            text = {
                Column {
                    Text("Enter your email address and we'll send you a link to reset your password.")
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = {
                            resetEmail = it
                            resetEmailError = null
                        },
                        label = { Text("Email") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        isError = resetEmailError != null,
                        supportingText = { resetEmailError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email"
                            )
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { resetPassword() },
                    enabled = !isResettingPassword && resetEmail.isNotEmpty()
                ) {
                    if (isResettingPassword) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Send Reset Link")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        if (!isResettingPassword) {
                            showResetPasswordDialog = false
                        }
                    },
                    enabled = !isResettingPassword
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reset Success Dialog
    if (showResetSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showResetSuccessDialog = false },
            title = { Text("Reset Email Sent") },
            text = {
                Column {
                    Text("We've sent a password reset link to:")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = resetEmail,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("Please check your email and follow the instructions to reset your password.")
                    Spacer(Modifier.height(8.dp))
                    Text("Don't forget to check your spam folder if you don't see the email.")
                }
            },
            confirmButton = {
                Button(onClick = { showResetSuccessDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}