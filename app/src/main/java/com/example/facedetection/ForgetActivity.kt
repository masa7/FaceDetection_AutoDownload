package com.example.facedetection

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.facedetection.databinding.ActivityForgetBinding
import com.google.firebase.auth.FirebaseAuth

class ForgetActivity : AppCompatActivity() {

    lateinit var forgetBinding : ActivityForgetBinding

    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        forgetBinding = ActivityForgetBinding.inflate(layoutInflater)
        val view = forgetBinding.root
        setContentView(view)

        forgetBinding.buttonReset.setOnClickListener {

            val email = forgetBinding.editTextReset.text.toString()

            auth.sendPasswordResetEmail(email).addOnCompleteListener{ task ->

                if(task.isSuccessful){

                    Toast.makeText(applicationContext
                    , "Sent password reset mail"
                    , Toast.LENGTH_SHORT).show()

                    finish()

                }
            }
        }
    }
}