package com.example.facedetection.authentication

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.facedetection.R
import com.example.facedetection.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SignupActivity : AppCompatActivity() {


    lateinit var signupBinding: ActivitySignupBinding

    val auth : FirebaseAuth = FirebaseAuth.getInstance()
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

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
            val userName = signupBinding.editTextName.text.toString() // Assuming you have an EditText for name
            val phoneNumber = signupBinding.editTextPhoneNumber.text.toString() // Assuming you have an EditText for phone number
            val firmName = signupBinding.editTextFirmName.text.toString() // Assuming you have an EditText for firm name
            val officeAddress = signupBinding.editTextOfficeAddress.text.toString() // Assuming you have an EditText for office address

            signupWithFirebase(userEmail, userPassword, userName, phoneNumber, firmName, officeAddress)
        }

    }

    private fun getCurrentDateFormatted(): String {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        return currentDate.format(formatter)
    }

    private fun sanitizeFirmName(firmName: String): String {
        // Get the current date in yyyyMMdd format
        val currentDate = getCurrentDateFormatted()

        // Sanitize firmName
        return "${firmName.replace("\\s+".toRegex(), "_") // Replace spaces with underscores
            .replace("[^a-zA-Z0-9_]".toRegex(), "") // Remove special characters
            .lowercase()}_$currentDate" // Append the current date
    }

    fun signupWithFirebase(userEmail: String, userPassword: String, userName: String, phoneNumber: String, firmName: String, officeAddress: String) {
        auth.createUserWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Get the current user
                val userId = auth.currentUser?.uid
                Log.i("TAGY", "${userId}")

                // Sanitize firmName for Firestore document ID
                val sanitizedFirmName = sanitizeFirmName(firmName)

                // Create a HashMap to store user details
                val userMap = hashMapOf(
                    "userName" to userName,
                    "userEmail" to userEmail,
                    "phoneNumber" to phoneNumber,
                    "firmName" to firmName,
                    "officeAddress" to officeAddress
                )

                // Save the user details in Firestore
                userId?.let {
                    firestore.collection("Registration")
                        .document(sanitizedFirmName)
                        .set(userMap)
                        .addOnSuccessListener {
                            Toast.makeText(applicationContext, "Account has been created and data saved", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(applicationContext, "Error saving user data: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }

            } else {
                Toast.makeText(applicationContext, task.exception?.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }
}