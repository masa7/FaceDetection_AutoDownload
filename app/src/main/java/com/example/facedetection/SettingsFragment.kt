package com.example.facedetection

import android.os.Bundle
import android.text.InputType
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat

class SettingsFragment: PreferenceFragmentCompat(){
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // set Fragment defined by preference.xml
        setPreferencesFromResource(R.xml.preference, rootKey)

        // set restriction on input to be an integer value for detection interval
        val logPreference1 = this.findPreference<EditTextPreference>("logPreference1")
        logPreference1?.setOnBindEditTextListener { editText ->
            editText.inputType = InputType.TYPE_CLASS_NUMBER
        }

        // set restriction on input to be an integer value for log update interval
        val logPreference2 = this.findPreference<EditTextPreference>("logPreference2")
        logPreference2?.setOnBindEditTextListener { editText ->
            editText.inputType = InputType.TYPE_CLASS_NUMBER
        }
    }

}