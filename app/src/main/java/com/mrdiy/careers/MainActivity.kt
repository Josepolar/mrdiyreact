package com.mrdiy.careers

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.handleDeeplinks
import com.mrdiy.careers.data.auth.SupabaseProvider
import com.mrdiy.careers.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainContainer) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = insets.top)
            binding.bottomNavigation.updatePadding(bottom = insets.bottom)
            windowInsets
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavigation.isVisible = when (destination.id) {
                R.id.loginFragment,
                R.id.registerFragment,
                R.id.phoneLoginFragment,
                R.id.welcomeFragment,
                R.id.emailVerificationFragment -> false
                else                           -> true
            }
        }

        handleDeepLink(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        intent ?: return
        if (intent.data?.scheme == "mrdiy") {
            lifecycleScope.launch {
                try {
                    SupabaseProvider.client.handleDeeplinks(intent)
                    SupabaseProvider.client.auth.refreshCurrentSession()
                } catch (_: Exception) { }
            }
        }
    }

    fun markAlertsRead()   { /* clear notification badges */ }
    fun incrementApplied() { /* TODO */ }
    fun incrementSaved()   { /* TODO */ }
}