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
import java.util.UUID

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

//        Retrieve email from intent
//        val email = intent.getStringExtra("email")
//        signupBinding.editTextEmailSignup.setText(email)

        signupBinding.buttonSignupUser.setOnClickListener {
            val userEmail = signupBinding.editTextEmailSignup.text.toString()
            val userPassword = signupBinding.editTextPasswordSignup.text.toString()
            val confirmPassword = signupBinding.editTextPasswordConfirm.text.toString()
            val userName = signupBinding.editTextName.text.toString()
            val phoneNumber = signupBinding.editTextPhoneNumber.text.toString()
            val firmName = signupBinding.editTextFirmName.text.toString()
            val officeAddress = signupBinding.editTextOfficeAddress.text.toString()

            // Validate inputs
            if (validateInputs(userEmail, userPassword, confirmPassword, userName, phoneNumber, firmName, officeAddress)) {
                signupWithFirebase(userEmail, userPassword, userName, phoneNumber, firmName, officeAddress)
            }
        }

    }

    private fun getCurrentDateFormatted(): String {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        return currentDate.format(formatter)
    }

//    private fun sanitizeFirmName(firmName: String): String {
//        // Get the current date in yyyyMMdd format
//        val currentDate = getCurrentDateFormatted()
//
//        // Sanitize firmName
//        return "${firmName.replace("\\s+".toRegex(), "_") // Replace spaces with underscores
//            //.replace("[^a-zA-Z0-9_]".toRegex(), "") // Remove special characters
//            .lowercase()}_$currentDate" // Append the current date
//    }

    fun signupWithFirebase(userEmail: String, userPassword: String, userName: String, phoneNumber: String, firmName: String, officeAddress: String) {
        auth.createUserWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Get the current user
                val userId = auth.currentUser?.uid
                Log.i("TAGY", "${userId}")

                val uniqueId = generateUniqueId()
                val currentDate = getCurrentDateFormatted()

                // Create a HashMap to store user details
                val userMap = hashMapOf(
                    "uniqueId" to uniqueId,
                    "userEmail" to userEmail,
                    "userName" to userName,
                    "firmName" to firmName,
                    "phoneNumber" to phoneNumber,
                    "officeAddress" to officeAddress,
                    "registrationDate" to currentDate
                )

                // Save the user details in Firestore
                userId?.let {
                    firestore.collection("Registration")
                        .document(uniqueId)
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

    private fun validateInputs(userEmail: String, userPassword: String, confirmPassword: String, userName: String, phoneNumber: String, firmName: String, officeAddress: String): Boolean {
        val validations = listOf(
            userEmail.isEmpty() to "Email is required",
            userPassword.isEmpty() to "Password is required",
            confirmPassword.isEmpty() to "Confirm your password",
            userName.isEmpty() to "Name is required",
            firmName.isEmpty() to "Company name is required",
            phoneNumber.isEmpty() to "Phone number is required",
            officeAddress.isEmpty() to "Company address is required"
        )

        // Check all validations
        for ((isInvalid, message) in validations) {
            if (isInvalid) {
                showToast(message)
                return false
            }
        }

        // Check if passwords match
        if (userPassword != confirmPassword) {
            showToast("Passwords do not match")
            return false
        }

        return true
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun generateUniqueId(): String {
        return (10000000..99999999).random().toString()
    }

//    private fun generateUniqueId(): String {
//        // Generate an 8-digit random number
//        val randomId = (10000000..99999999).random().toString()
//
//        // Get the current date in yyyyMMdd format
//        val currentDate = getCurrentDateFormatted()
//
//        // Concatenate the random ID and the current date
//        return "$randomId$currentDate"
//    }
//
//    private fun generateUniqueId(): String {
//        return UUID.randomUUID().toString()
//    }


}