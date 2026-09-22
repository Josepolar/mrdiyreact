package com.mrdiy.careers.data

import android.util.Log
import com.mrdiy.careers.BuildConfig
import kotlinx.coroutines.CancellationException

object SafeDiagnostics {
    fun record(operation: String, error: Throwable) {
        if (error is CancellationException) throw error
        // Never log exception messages, request objects or stack traces: SDK errors contain headers.
        if (BuildConfig.DEBUG) Log.w("Careers", "$operation failed (${error.javaClass.simpleName})")
    }
}
