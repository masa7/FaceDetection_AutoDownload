package com.example.facedetection.authentication

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.facedetection.MainActivity
import com.example.facedetection.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth


class LoginActivity : AppCompatActivity() {

    lateinit var loginBinding: ActivityLoginBinding

    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginBinding = ActivityLoginBinding.inflate(layoutInflater)
        val view = loginBinding.root
        setContentView(view)

        val tv = loginBinding.loginText
        val face: Typeface = Typeface.createFromAsset(
            assets,
            "Sacramento-Regular.ttf"
        )
        tv.setTypeface(face)


        loginBinding.buttonSignin.setOnClickListener {

            val userEmail = loginBinding.editTextEmailSignin.text.toString().trim()
            val userPassword = loginBinding.editTextPasswordSignin.text.toString().trim()
            val termsCheckBox = loginBinding.termsCheckBox

            if (userEmail.isEmpty()) {
                Toast.makeText(applicationContext, "Please enter your username.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (userPassword.isEmpty()) {
                Toast.makeText(applicationContext,"Please enter your password.",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!termsCheckBox.isChecked) {
                Toast.makeText(
                    applicationContext,
                    "You must agree to the Terms and Conditions.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            signinWithFirebase(userEmail, userPassword)

        }

        loginBinding.buttonSignup.setOnClickListener {

            val termsCheckBox = loginBinding.termsCheckBox

            if (!termsCheckBox.isChecked) {
                Toast.makeText(
                    applicationContext,
                    "You must agree to the Terms and Conditions.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val intent = Intent(this@LoginActivity, SignupActivity::class.java)
            startActivity(intent)

        }

        loginBinding.buttonForgot.setOnClickListener {

            val intent = Intent(this, ForgetActivity::class.java)
            startActivity(intent)

        }



    }

    fun signinWithFirebase(userEmail: String, userPassword: String) {

        auth.signInWithEmailAndPassword(userEmail, userPassword)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information

                    Toast.makeText(applicationContext, "Login successful.", Toast.LENGTH_SHORT)
                        .show()
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()

                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(applicationContext, "Please check login details.", Toast.LENGTH_SHORT
                    ).show()

                }
            }

    }

    override fun onStart() {
        super.onStart()

        val user = auth.currentUser

        if (user != null) {

            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
            finish()

        } else {
            FirebaseAuth.getInstance().signOut()
        }
    }


}