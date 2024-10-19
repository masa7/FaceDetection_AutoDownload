package com.example.facedetection.fragment

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.example.facedetection.LogdataActivity
import com.example.facedetection.MainActivity.Global.Companion.emarthUrl
import com.example.facedetection.MainActivity.Global.Companion.videoDynamicUrl
import com.example.facedetection.MainActivity.Global.Companion.videoStaticUrl
import com.example.facedetection.ProfileActivity
import com.example.facedetection.R
import com.example.facedetection.authentication.LoginActivity
import com.example.facedetection.utils.SingletonContext.Companion.applicationContext
import com.example.facedetection.videodownload.AndroidDownloader
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SettingsFragment: PreferenceFragmentCompat(){
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // set Fragment defined by preference.xml
        val dlAsof = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
        val prefContext = applicationContext()
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(prefContext)

        setPreferencesFromResource(R.xml.preference, rootKey)
        this.findPreference<Preference>("videoDynamicPreference")?.summary = sharedPref.getString("videoDynamicPreference", "")
        this.findPreference<Preference>("videoStaticPreference")?.summary = sharedPref.getString("videoStaticPreference", "")
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

        this.findPreference<Preference>("videoDynamicPreference")?.setOnPreferenceClickListener {
            val downloader = AndroidDownloader(prefContext)
            downloader.execDownload(videoDynamicUrl, true)
            //this.findPreference<Preference>("videoDynamicPreference")?.summary = dlAsof
            true
        }

        this.findPreference<Preference>("videoStaticPreference")?.setOnPreferenceClickListener {
            //val fileName: String = videoStaticUrl.substring(videoStaticUrl.lastIndexOf('/') + 1)
            val downloader = AndroidDownloader(prefContext)
            downloader.execDownload(videoStaticUrl, true)
            this.findPreference<Preference>("videoStaticPreference")?.summary = dlAsof
            true
        }

        this.findPreference<Preference>("dlPreference")?.setOnPreferenceClickListener {
            val downloader = AndroidDownloader(prefContext)
            downloader.execDownload(emarthUrl, true)
            this.findPreference<Preference>("dlPreference")?.summary = dlAsof
            true
        }

        this.findPreference<Preference>("logPreference")?.setOnPreferenceClickListener {
            val intent = Intent(context, LogdataActivity::class.java)
            activity?.startActivity(intent)
            true
        }

        this.findPreference<Preference>("account")?.setOnPreferenceClickListener {
            val intent = Intent(context, ProfileActivity::class.java)
            activity?.startActivity(intent)
            true
        }

        this.findPreference<Preference>("logout")?.setOnPreferenceClickListener {
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(context, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            activity?.startActivity(intent)
            true
        }
//            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestEmail()
//                .build()
//            val googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
//
//            googleSignInClient.signOut().addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
//
//                    val intent = Intent(context, LoginActivity::class.java)
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//                    startActivity(intent)
//                } else {
//                    Toast.makeText(context, "Logout failed. Please try again.", Toast.LENGTH_SHORT).show()
//                }
//            }
//            true
//        }

    }
}