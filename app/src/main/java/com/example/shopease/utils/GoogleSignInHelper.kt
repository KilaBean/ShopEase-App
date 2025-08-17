package com.example.shopease.utils

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.example.shopease.R

class GoogleSignInHelper private constructor(context: Context) {
    private val googleSignInClient: GoogleSignInClient

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    private fun handleSignInResult(
        data: Intent?,
        auth: FirebaseAuth,
        context: Context,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            account?.let { firebaseAuthWithGoogle(it, auth, context, onSuccess, onFailure) }
        } catch (e: ApiException) {
            onFailure("Google sign in failed: ${e.message}")
        }
    }

    private fun firebaseAuthWithGoogle(
        account: GoogleSignInAccount,
        auth: FirebaseAuth,
        context: Context,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure(AuthUtils.getAuthErrorMessage(task.exception))
                }
            }
    }

    fun signOut() {
        googleSignInClient.signOut()
    }

    companion object {
        @Volatile private var instance: GoogleSignInHelper? = null

        fun getInstance(context: Context): GoogleSignInHelper {
            return instance ?: synchronized(this) {
                instance ?: GoogleSignInHelper(context.applicationContext).also { instance = it }
            }
        }

        fun handleSignInResult(
            data: Intent?,
            auth: FirebaseAuth,
            context: Context,
            onSuccess: () -> Unit,
            onFailure: (String) -> Unit
        ) {
            getInstance(context).handleSignInResult(data, auth, context, onSuccess, onFailure)
        }
    }
}