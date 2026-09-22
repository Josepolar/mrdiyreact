package com.mrdiy.careers

import android.content.Context
import android.widget.CheckBox
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.textfield.TextInputLayout
import com.mrdiy.careers.data.auth.AuthManager
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class AuthWorkflowTest {
    @Before fun clearSession() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        AuthManager(context).signOut()
        // Simulate old preferences left by the formerly hardcoded demo login.
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE).edit()
            .putBoolean("is_logged_in", true).putString("user_id", "stale-demo-id").apply()
    }

    @Test fun stalePreferencesCannotBypassLoginAcrossRecreation() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            onView(withId(R.id.btnLogin)).check(matches(isDisplayed()))
            scenario.recreate()
            onView(withId(R.id.btnLogin)).check(matches(isDisplayed()))
        }
    }

    @Test fun registrationBlocksUncheckedTermsAndRestoresThatRule() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            onView(withId(R.id.tvRegister)).perform(scrollTo(), click())
            onView(withId(R.id.btnRegister)).perform(scrollTo(), click())
            scenario.onActivity { activity ->
                Assert.assertNotNull(activity.findViewById<CheckBox>(R.id.cbTerms).error)
                Assert.assertFalse(AuthManager(activity).isLoggedIn)
            }
            scenario.recreate()
            onView(withId(R.id.btnRegister)).perform(scrollTo(), click())
            scenario.onActivity { activity -> Assert.assertNotNull(activity.findViewById<CheckBox>(R.id.cbTerms).error) }
        }
    }

    @Test fun directRegistrationWithoutTermsNeverAuthenticates() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val latch = CountDownLatch(1)
        AuthManager(context).register("Test", "no-send@example.com", "test1234", false) { success, message ->
            Assert.assertFalse(success)
            Assert.assertTrue(message.contains("agree"))
            latch.countDown()
        }
        Assert.assertTrue(latch.await(3, TimeUnit.SECONDS))
        Assert.assertFalse(AuthManager(context).isLoggedIn)
    }

    @Test fun loginControlsRemainReachableAfterRotation() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { it.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
            onView(withId(R.id.btnLogin)).perform(scrollTo()).check(matches(isDisplayed()))
            onView(withId(R.id.tvRegister)).perform(scrollTo()).check(matches(isDisplayed()))
            scenario.onActivity { it.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT }
        }
    }

    @Test fun emailValidatesOnBlurAndClearsWhenCorrected() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            onView(withId(R.id.etEmail)).perform(click(), replaceText("a"))
            scenario.onActivity { Assert.assertNull(it.findViewById<TextInputLayout>(R.id.tilEmail).error) }
            onView(withId(R.id.etPassword)).perform(click())
            scenario.onActivity { Assert.assertNotNull(it.findViewById<TextInputLayout>(R.id.tilEmail).error) }
            onView(withId(R.id.etEmail)).perform(replaceText("user@example.com"), closeSoftKeyboard())
            scenario.onActivity { Assert.assertNull(it.findViewById<TextInputLayout>(R.id.tilEmail).error) }
        }
    }
}
