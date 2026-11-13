package com.example.gestion_des_contacts

import android.os.Bundle
import android.view.View
import androidx.preference.PreferenceFragmentCompat

class ParametresFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.setToolbarTitle("Paramètres", true)
    }
}
