package com.mrdiy.careers.data.auth

import android.content.Context
import android.content.SharedPreferences
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Phone
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

object SupabaseProvider {
    private const val SUPABASE_URL      = "https://sfjpiyevasnmvddgtofz.supabase.co"
    private const val SUPABASE_ANON_KEY =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                "eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNmanBpeWV2YXNubXZkZGd0b2Z6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzkyODAxNTIsImV4cCI6MjA5NDg1NjE1Mn0." +
                "ySYf_WZYJr_A7oXhH5wGkri5qW0OctsINK82716t0Ac"

    val supabaseJson: Json = Json {
        ignoreUnknownKeys  = true
        coerceInputValues  = true
        isLenient          = true
    }

    val client: SupabaseClient by lazy {
        createSupabaseClient(SUPABASE_URL, SUPABASE_ANON_KEY) {
            install(Auth) {
                scheme = "mrdiy"
                host = "login-callback"
            }
            install(Postgrest)
            install(Storage)
            defaultSerializer = KotlinXSerializer(supabaseJson)
        }
    }
}


class AuthManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    private val client = SupabaseProvider.client

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_NAME    = "user_name"
        private const val KEY_USER_ID      = "user_id"
    }

    val isLoggedIn: Boolean get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    val currentUser: String get() = prefs.getString(KEY_USER_NAME,  "") ?: ""

    fun getCurrentUserName(): String = prefs.getString(KEY_USER_NAME, "") ?: ""

    fun getCurrentUserId(): String? = prefs.getString(KEY_USER_ID, null)

    fun saveLoginState(isLoggedIn: Boolean) {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply()
    }

    fun clearDemoSession() {
        if (getCurrentUserId() == "debug-demo-user") prefs.edit().clear().apply()
    }

    fun enableDemoSession() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USER_ID, "debug-demo-user")
            .putString(KEY_USER_NAME, "Demo User")
            .apply()
    }

    fun logout() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_ID)
            .apply()

        CoroutineScope(Dispatchers.IO).launch {
            runCatching { client.auth.signOut() }
        }
    }

    fun login(email: String, password: String, callback: (Boolean, String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                client.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                val user = client.auth.currentUserOrNull()
                if (user != null) {
                    val userId = user.id
                    val userName = user.email ?: email
                    prefs.edit()
                        .putString(KEY_USER_ID, userId)
                        .putString(KEY_USER_NAME, userName)
                        .putBoolean(KEY_IS_LOGGED_IN, true)
                        .apply()
                    withContext(Dispatchers.Main) {
                        callback(true, "Login successful")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        callback(false, "Login failed: No user returned")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback(false, readableAuthError(e))
                }
            }
        }
    }

    fun register(fullName: String, email: String, password: String, callback: (Boolean, String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                client.auth.signUpWith(Email, redirectUrl = "mrdiy://login-callback") {
                    this.email = email
                    this.password = password
                }
                withContext(Dispatchers.Main) {
                    callback(true, "Registration successful. Please check your email for verification.")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback(false, readableRegistrationError(e))
                }
            }
        }
    }

    fun signInWithGoogle(callback: (Boolean, String) -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                client.auth.signInWith(Google)
                callback(true, "Opening Google sign-in...")
            } catch (e: Exception) {
                callback(false, "Google sign-in could not start. Please try again.")
            }
        }
    }

    fun requestPhoneOtp(phone: String, callback: (Boolean, String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                client.auth.signInWith(Phone) { this.phone = phone }
                withContext(Dispatchers.Main) { callback(true, "Verification code sent.") }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback(false, "Phone sign-in is unavailable. Check the phone provider settings.")
                }
            }
        }
    }

    fun verifyPhoneOtp(phone: String, token: String, callback: (Boolean, String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                client.auth.verifyPhoneOtp(
                    type = OtpType.Phone.SMS,
                    phone = phone,
                    token = token
                )
                val user = client.auth.currentUserOrNull()
                if (user == null) {
                    withContext(Dispatchers.Main) { callback(false, "Verification failed. Please request a new code.") }
                } else {
                    prefs.edit()
                        .putString(KEY_USER_ID, user.id)
                        .putString(KEY_USER_NAME, user.phone ?: phone)
                        .putBoolean(KEY_IS_LOGGED_IN, true)
                        .apply()
                    withContext(Dispatchers.Main) { callback(true, "Phone verified.") }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { callback(false, "The code is invalid or expired.") }
            }
        }
    }

    fun saveCurrentSupabaseUser(): Boolean {
        val user = client.auth.currentUserOrNull() ?: return false
        prefs.edit()
            .putString(KEY_USER_ID, user.id)
            .putString(KEY_USER_NAME, user.email ?: "Google User")
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
        return true
    }

    fun resetPassword(email: String, callback: (Boolean, String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                client.auth.resetPasswordForEmail(email)
                withContext(Dispatchers.Main) {
                    callback(true, "Password reset email sent")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback(false, "We could not send the reset email. Please try again.")
                }
            }
        }
    }

    fun updatePassword(password: String, callback: (Boolean, String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                client.auth.updateUser { this.password = password }
                withContext(Dispatchers.Main) { callback(true, "Password updated.") }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { callback(false, "Could not update the password. Please try again.") }
            }
        }
    }

    private fun readableAuthError(error: Exception): String {
        val message = error.message.orEmpty().lowercase()
        return when {
            "invalid login" in message || "invalid credentials" in message ->
                "Email or password is incorrect."
            "email not confirmed" in message ->
                "Please confirm your email before signing in."
            "network" in message || "timeout" in message ->
                "Connection problem. Check your internet and try again."
            else -> "Login failed. Check your details and try again."
        }
    }

    private fun readableRegistrationError(error: Exception): String {
        val message = error.message.orEmpty().lowercase()
        return when {
            "already registered" in message || "already exists" in message ->
                "That email is already registered. Try signing in instead."
            "rate limit" in message || "too many" in message ->
                "Too many email attempts. Please wait and try again later."
            "email" in message && "send" in message ->
                "Your account was not created because the verification email could not be sent."
            "network" in message || "timeout" in message ->
                "Connection problem. Check your internet and try again."
            else -> "Registration failed. Check your details and try again."
        }
    }
}