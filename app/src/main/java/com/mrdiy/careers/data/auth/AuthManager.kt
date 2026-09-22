package com.mrdiy.careers.data.auth

import android.content.Context
import android.content.SharedPreferences
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.Google
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
                    callback(false, "Login failed: ${e.message}")
                }
            }
        }
    }

    fun register(fullName: String, email: String, password: String, callback: (Boolean, String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
                withContext(Dispatchers.Main) {
                    callback(true, "Registration successful. Please check your email for verification.")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback(false, "Registration failed: ${e.message}")
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
                callback(false, "Google sign-in failed: ${e.message}")
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
                    callback(false, "Failed to send reset email: ${e.message}")
                }
            }
        }
    }
}