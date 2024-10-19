package com.example.facedetection.fragment

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.example.facedetection.R
import com.example.facedetection.utils.SingletonContext.Companion.applicationContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ProfileFragment: PreferenceFragmentCompat(){
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // set Fragment defined by user_profile.xml
        val dlAsof = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
        val prefContext = applicationContext()
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(prefContext)

        setPreferencesFromResource(R.xml.user_profile, rootKey)
        this.findPreference<Preference>("accountIdPref")?.summary = sharedPref.getString("uniqueIdPref", "")
        this.findPreference<Preference>("accountEmailPref")?.summary = sharedPref.getString("userEmailPref", "")
        this.findPreference<Preference>("accountNamePref")?.summary = sharedPref.getString("userNamePref", "")
        this.findPreference<Preference>("accountPharmaPref")?.summary = sharedPref.getString("firmNamePref", "")
        this.findPreference<Preference>("accountTelPref")?.summary = sharedPref.getString("phoneNumberPref", "")
        this.findPreference<Preference>("accountAddressPref")?.summary = sharedPref.getString("officeAddressPref", "")
    }
}