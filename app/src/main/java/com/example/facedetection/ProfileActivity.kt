package com.example.facedetection

import android.os.Bundle
import android.view.MenuItem
import com.example.facedetection.fragment.ProfileFragment
import com.example.facedetection.utils.BaseActivity

class ProfileActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        if(savedInstanceState == null){
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.profileFrameLayout, ProfileFragment())
                .commit()
        }

        supportActionBar?.setTitle(R.string.accountCatTitle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var returnVal = true

        if (item.itemId == android.R.id.home) {
            finish()
        } else {
            returnVal = super.onOptionsItemSelected(item)
        }
        return returnVal
    }
}