package com.example.facedetection.authentication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.facedetection.R
import com.example.facedetection.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth

class SignupActivity : AppCompatActivity() {

    lateinit var signupBinding: ActivitySignupBinding

    val auth : FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        signupBinding = ActivitySignupBinding.inflate(layoutInflater)
        val view = signupBinding.root
        setContentView(view)

        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.setCustomView(R.layout.toolbar_title_layout)

        signupBinding.buttonSignupUser.setOnClickListener {
            val userEmail = signupBinding.editTextEmailSignup.text.toString()
            val userPassword = signupBinding.editTextPasswordSignup.text.toString()
            val userName = signupBinding.editTextName.text.toString()
            val firmName = signupBinding.editTextFirmName.text.toString()
            val phoneNumber = signupBinding.editTextPhoneNumber.text.toString()
            val officeAddress = signupBinding.editTextOfficeAddress.text.toString()

            signupWithFirebase(userEmail, userPassword, userName, firmName, phoneNumber, officeAddress)
        }

    }

    fun signupWithFirebase(userEmail: String, userPassword: String, userName: String, firmName: String, phoneNumber: String, officeAddress: String) {

        auth.createUserWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener { task ->

            if (task.isSuccessful){

                val userInfo = "Name: $userName\nFirm Name: $firmName\nEmail: $userEmail\nPhone Number: $phoneNumber\nOffice Address: $officeAddress"
                sendEmailWithUserInfo(userInfo)

                Toast.makeText(applicationContext,"Account has been created",Toast.LENGTH_SHORT).show()
                finish()

            }else{
                Toast.makeText(applicationContext,task.exception?.toString(),Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendEmailWithUserInfo(userInfo: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("kuraken.tok@gmail.com"))
        intent.putExtra(Intent.EXTRA_SUBJECT, "New User Registration")
        intent.putExtra(Intent.EXTRA_TEXT, userInfo)

        try {
            startActivity(Intent.createChooser(intent, "Send Email"))
        } catch (e: android.content.ActivityNotFoundException) {
            Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show()
        }
    }

}