package com.example.facedetection.authentication

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.facedetection.MainActivity
import com.example.facedetection.R
import com.example.facedetection.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    lateinit var loginBinding: ActivityLoginBinding

    lateinit var googleSignInClient: GoogleSignInClient
    lateinit var activityResultLauncher : ActivityResultLauncher<Intent>

    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginBinding = ActivityLoginBinding.inflate(layoutInflater)
        val view = loginBinding.root
        setContentView(view)

        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.setCustomView(R.layout.toolbar_title_layout)

        // Terms and Condition hyperlink
        val terms = loginBinding.termsCheckBox
        val fullText = "I agree to the Terms and Conditions"
        val termsStart = fullText.indexOf("Terms and Conditions")

        val spannableString = SpannableString(fullText).apply {
            setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    widget.cancelPendingInputEvents()
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://emadtech.jp/legal/"))
                    widget.context.startActivity(intent)
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = true
                }
            }, termsStart, termsStart + "Terms and Conditions".length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        terms.text = spannableString
        terms.movementMethod = LinkMovementMethod.getInstance()

        /*
        val tv = loginBinding.loginText
        val face: Typeface = Typeface.createFromAsset(
            assets,
            "Sacramento-Regular.ttf"
        )
        tv.setTypeface(face)
        */

        // google login
        val textOfGoogleButton = loginBinding.buttonGoogleSignin.getChildAt(0) as TextView
        textOfGoogleButton.text = "Continue with Google"
        textOfGoogleButton.setTextColor(Color.BLACK)
        textOfGoogleButton.textSize = 18F

        registerActivityForGoogleSignIn()

        loginBinding.buttonGoogleSignin.setOnClickListener {
            signInGoogle()
        }


        // anonymous login
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


    // google sign-in
    private fun signInGoogle(){

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("281673539231-pfthpnmvu755bbk3mthb31kqtcbqdvqd.apps.googleusercontent.com")
            .requestEmail().build()

        googleSignInClient = GoogleSignIn.getClient(this,gso)
        signIn()
    }
    private fun signIn(){

        val signInIntent : Intent = googleSignInClient.signInIntent
        activityResultLauncher.launch(signInIntent)
    }

    private fun registerActivityForGoogleSignIn(){

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback { result ->

                val resultCode = result.resultCode
                val data = result.data

                if (resultCode == RESULT_OK && data != null){
                    val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
                    firebaseSignInWithGoogle(task)
                }
            }
        )
    }

    private fun firebaseSignInWithGoogle(task : Task<GoogleSignInAccount>){

        try{
            val account : GoogleSignInAccount = task.getResult(ApiException::class.java)
            Toast.makeText(applicationContext, "Login successful",Toast.LENGTH_SHORT).show()
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
            firebaseGoogleAccount(account)
        }catch (e : ApiException){
            Toast.makeText(applicationContext,e.localizedMessage,Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseGoogleAccount(account: GoogleSignInAccount){
        val authCredential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(authCredential).addOnCompleteListener { task ->

            if(task.isSuccessful){
                val user = auth.currentUser
            }else{}
        }
    }

}