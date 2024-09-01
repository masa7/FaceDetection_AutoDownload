package com.example.facedetection

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.camera.core.impl.utils.ContextUtil.getApplicationContext
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.example.facedetection.MainActivity.Global.Companion.dateStr
import com.example.facedetection.MainActivity.Global.Companion.emarthUrl
import com.example.facedetection.utils.SingletonContext
import com.example.facedetection.utils.SingletonContext.Companion.applicationContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SettingsFragment: PreferenceFragmentCompat(){
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // set Fragment defined by preference.xml
        val dlAsof = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
        val prefContext = applicationContext()
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(prefContext)

        setPreferencesFromResource(R.xml.preference, rootKey)
        this.findPreference<Preference>("dlPreference")?.summary = sharedPref.getString("dlPreference", "")
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

        this.findPreference<Preference>("dlPreference")?.setOnPreferenceClickListener {
            val downloader = AndroidDownloader(prefContext)

            downloader.downloadFile(emarthUrl)
            Toast.makeText(prefContext, "Download Started", Toast.LENGTH_LONG).show()

            val editor = sharedPref.edit()
            editor.putString("dlPreference", dlAsof)
            editor.apply()

            this.findPreference<Preference>("dlPreference")?.summary = dlAsof

            true
        }
    }
}