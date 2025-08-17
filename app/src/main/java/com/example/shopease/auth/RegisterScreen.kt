package com.example.shopease.auth

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.shopease.AuthViewModel
import com.example.shopease.R
import com.example.shopease.utils.AuthUtils
import com.example.shopease.utils.GoogleSignInHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.util.Log

@Composable
fun RegisterScreen(navController: NavController, authViewModel: AuthViewModel) {
    val context = LocalContext.current
    val auth = Firebase.auth

    // Track authentication state to satisfy Compose compiler
    val isAuthenticated by authViewModel.isAuthenticated

    // Handle navigation when authentication state changes
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            navController.navigate("home") {
                popUpTo("register") { inclusive = true }
            }
        }
    }

    // Email/Password Registration States
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var googleLoading by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var showVerificationDialog by remember { mutableStateOf(false) }

    // Resend Functionality States
    var lastResendTime by remember { mutableStateOf(0L) }
    var resendCountdown by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    val resendCooldown = 60_000L // 60 seconds cooldown

    // Google Sign-In Setup
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

    // Countdown Timer for Resend Email
    LaunchedEffect(lastResendTime) {
        if (lastResendTime > 0) {
            coroutineScope.launch {
                while (resendCountdown > 0) {
                    delay(1000)
                    resendCountdown = ((resendCooldown - (System.currentTimeMillis() - lastResendTime)) / 1000).toInt()
                }
            }
        }
    }

    // Password Requirements Check
    val passwordRequirements = remember(password) {
        AuthUtils.checkPasswordRequirements(password)
    }

    fun registerUser() {
        emailError = null
        when {
            email.isEmpty() -> {
                emailError = "Email is required"
                return
            }
            !AuthUtils.isValidEmail(email) -> {
                emailError = "Invalid email format"
                return
            }
            passwordRequirements.any { !it.value } -> {
                return
            }
        }
        isLoading = true
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                isLoading = false
                if (task.isSuccessful) {
                    // Send verification email
                    auth.currentUser?.sendEmailVerification()
                        ?.addOnCompleteListener { verificationTask ->
                            if (verificationTask.isSuccessful) {
                                // Sign out the user immediately after registration
                                auth.signOut()

                                // Show verification dialog
                                lastResendTime = System.currentTimeMillis()
                                resendCountdown = (resendCooldown / 1000).toInt()
                                showVerificationDialog = true

                                // DO NOT update auth state here - user is not verified yet
                                Toast.makeText(context, "Registration successful! Please check your email to verify your account.", Toast.LENGTH_LONG).show()
                            } else {
                                val error = AuthUtils.getAuthErrorMessage(verificationTask.exception)
                                Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    val error = AuthUtils.getAuthErrorMessage(task.exception)
                    Toast.makeText(context, "Registration failed: $error", Toast.LENGTH_LONG).show()
                }
            }
    }

    fun resendVerificationEmail() {
        // Note: We need to sign in again to resend verification email
        // This is a limitation of Firebase - you must be authenticated to send verification email
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    auth.currentUser?.sendEmailVerification()
                        ?.addOnSuccessListener {
                            auth.signOut() // Sign out again after sending
                            lastResendTime = System.currentTimeMillis()
                            resendCountdown = (resendCooldown / 1000).toInt()
                            Toast.makeText(context, "Verification email resent!", Toast.LENGTH_SHORT).show()
                        }
                        ?.addOnFailureListener { e ->
                            auth.signOut() // Sign out on failure too
                            Toast.makeText(context, "Failed to resend: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(context, "Failed to resend verification email", Toast.LENGTH_SHORT).show()
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
        Text("Create Account", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = emailError != null,
            supportingText = { emailError?.let { Text(it, color = Color.Red) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
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
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Password"
                )
            }
        )

        // Password Requirements
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Password Requirements:", style = MaterialTheme.typography.labelSmall)
            RequirementItem(text = "• 8+ characters", isMet = passwordRequirements["length"] == true)
            RequirementItem(text = "• 1 uppercase letter", isMet = passwordRequirements["uppercase"] == true)
            RequirementItem(text = "• 1 lowercase letter", isMet = passwordRequirements["lowercase"] == true)
            RequirementItem(text = "• 1 number", isMet = passwordRequirements["numeric"] == true)
            RequirementItem(text = "• 1 special character", isMet = passwordRequirements["special"] == true)
        }
        Spacer(Modifier.height(24.dp))

        // Register Button
        Button(
            onClick = { registerUser() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isLoading && AuthUtils.isValidEmail(email) &&
                    passwordRequirements.all { it.value }
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Register")
            }
        }

        Spacer(Modifier.height(16.dp))

        // Divider with OR
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

        // Already have an account
        TextButton(onClick = { navController.navigate("login") }) {
            Text("Already have an account? Sign In")
        }
    }

    // Email Verification Dialog
    if (showVerificationDialog) {
        AlertDialog(
            onDismissRequest = {
                showVerificationDialog = false
                // Navigate to login screen when dialog is dismissed
                navController.navigate("login") {
                    popUpTo("register") { inclusive = true }
                }
            },
            title = {
                Text(
                    text = "Verify Your Email",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "We've sent a verification email to:",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "Please verify your email before logging in.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))

                    // Spam folder guidance
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF8E1)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Can't find the email?",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFFF8F00)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Check your spam folder or promotions tab in Gmail",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = Color(0xFF5D4037)
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Resend button with countdown
                    val canResend = resendCountdown <= 0
                    Button(
                        onClick = { resendVerificationEmail() },
                        enabled = canResend,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (canResend) "Resend Email" else "Resend in ${resendCountdown}s")
                    }

                    Spacer(Modifier.height(8.dp))

                    // Expiration notice
                    Text(
                        text = "Verification link expires in 24 hours",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showVerificationDialog = false
                        // Navigate to login screen when OK is clicked
                        navController.navigate("login") {
                            popUpTo("register") { inclusive = true }
                        }
                    }
                ) {
                    Text("OK, I'll Check")
                }
            }
        )
    }
}

@Composable
fun RequirementItem(text: String, isMet: Boolean) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(
            color = if (isMet) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface
        )
    )
}