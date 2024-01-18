package com.example.bop_it1

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)


            // Configura los listeners para las preferencias
            findPreference<EditTextPreference>("shake_threshold")?.setOnPreferenceChangeListener { _, newValue ->
                // Actualiza shakeThreshold cuando cambia la preferencia
                (activity as? GameActivity)?.shakeThreshold = newValue.toString().toFloat()
                true
            }

            findPreference<EditTextPreference>("rotation_threshold")?.setOnPreferenceChangeListener { _, newValue ->
                // Actualiza rotationThreshold cuando cambia la preferencia
                (activity as? GameActivity)?.rotationThreshold = newValue.toString().toFloat()
                true
            }
            findPreference<EditTextPreference>("difficulty_Settings")?.setOnPreferenceChangeListener { _, newValue ->
                // Actualiza difficultySettings cuando cambia la preferencia
                (activity as? GameActivity)?.difficultySettings = newValue.toString().toInt()
                true
            }
        }
    }
}