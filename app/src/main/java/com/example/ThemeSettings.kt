package com.example

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object ThemeSettings {
    private val _isDarkMode = MutableStateFlow(true) // Default fits the premium dark "VoiceVault" theme
    val isDarkMode: StateFlow<Boolean> = _isDarkMode

    fun init(context: Context) {
        val prefs = context.getSharedPreferences("voice_vault_prefs", Context.MODE_PRIVATE)
        _isDarkMode.value = prefs.getBoolean("dark_mode", true)
    }

    fun setDarkMode(context: Context, dark: Boolean) {
        _isDarkMode.value = dark
        val prefs = context.getSharedPreferences("voice_vault_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("dark_mode", dark).apply()
    }
}
