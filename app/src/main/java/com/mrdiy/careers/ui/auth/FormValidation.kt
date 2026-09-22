package com.mrdiy.careers.ui.auth

import androidx.core.widget.doAfterTextChanged
import com.google.android.material.textfield.TextInputLayout

object FormValidation {
    fun bind(layout: TextInputLayout, validate: (String) -> String?) {
        var touched = false
        val input = layout.editText ?: return
        input.setOnFocusChangeListener { _, focused ->
            if (!focused) { touched = true; layout.error = validate(input.text.toString()) }
        }
        input.doAfterTextChanged {
            // While editing, only clear an existing error when valid; no first-keystroke errors.
            if (touched || layout.error != null) {
                if (validate(it.toString()) == null) layout.error = null
            }
        }
    }
}
