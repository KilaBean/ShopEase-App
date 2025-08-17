package com.example.shopease.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.example.shopease.utils.GoogleSignInHelper

class GoogleAuthResultContract : ActivityResultContract<Context, Intent?>() {
    override fun createIntent(context: Context, input: Context): Intent {
        return GoogleSignInHelper.getInstance(input).getSignInIntent()
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Intent? {
        return if (resultCode == Activity.RESULT_OK) intent else null
    }
}