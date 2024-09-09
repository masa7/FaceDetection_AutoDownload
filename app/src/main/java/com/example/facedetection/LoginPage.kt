package com.example.facedetection

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.facedetection.databinding.ActivityLoginPageBinding


class LoginPage : AppCompatActivity() {
    private lateinit var binding: ActivityLoginPageBinding
    lateinit var username : EditText
    lateinit var password: EditText
    lateinit var loginButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.setCustomView(R.layout.toolbar_title_layout)


        binding = ActivityLoginPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.loginButton.setOnClickListener(View.OnClickListener {
            if (binding.username.text.toString() == "admin" && binding.password.text.toString() == "Em@rth"){
                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()

                // Check if the user is already signed in or not
                if (!SessionManager.getInstance().hasSignIn()) {
                    // Start MainActivity if the login is successful
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()  // Optional: finish LoginPage activity to remove it from back stack
                }
            } else {
                Toast.makeText(this, "Login Failed!", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

class SessionManager private constructor() {

    // Singleton instance
    companion object {
        @Volatile private var instance: SessionManager? = null

        fun getInstance(): SessionManager {
            return instance ?: synchronized(this) {
                instance ?: SessionManager().also { instance = it }
            }
        }
    }

    // Example method to check sign-in status
    fun hasSignIn(): Boolean {
        // Add your sign-in checking logic here
        return false // For example purposes, returning false
    }
}