package com.example.gestion_des_contacts

import android.content.Context
import android.graphics.Color
import androidx.preference.PreferenceManager

class PreferencesManager(context: Context) {
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    fun getFontSize(): Float {
        return prefs.getString("taille_police", "16")?.toFloat() ?: 16f
    }

    fun getPreferredColor(): Int {
        val colorString = prefs.getString("couleur_preferee", "#000000") ?: "#000000"
        return try {
            Color.parseColor(colorString)
        } catch (e: Exception) {
            Color.BLACK
        }
    }

    fun isCallFromListEnabled(): Boolean {
        return prefs.getBoolean("appel_depuis_liste", false)
    }

    fun getSortOrder(): String {
        return prefs.getString("ordre_tri", "FAVORI_FIRST") ?: "FAVORI_FIRST"
    }
}
