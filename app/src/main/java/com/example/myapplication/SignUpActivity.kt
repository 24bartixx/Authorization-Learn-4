package com.example.myapplication

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import com.example.myapplication.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {

    // ViewBinding
    private lateinit var binding: ActivitySignUpBinding

    // ActionBar
    private lateinit var actionBar: ActionBar

    // ProgressDialog
    private lateinit var progressDialog: ProgressDialog

    private lateinit var firebaseAuth: FirebaseAuth

    private var email = ""
    private var password = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configure ActionBar, enable back button
        actionBar = supportActionBar!!
        actionBar.title = "SignUp"
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setDisplayShowHomeEnabled(true)

        // Configure progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setMessage("Creating account In...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        // handle click, begin sign up
        binding.signupButton.setOnClickListener {
            // validate data
            validateData()
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed() // go back to previous activity, when back button of actionbar is clicked
        return super.onSupportNavigateUp()
    }

    private fun validateData(){
        // get data
        email = binding.loginEditText.text.toString().trim()
        password = binding.passwordEditText.text.toString().trim()

        // validate data
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            // invalid email format
            binding.loginEditText.error = "Invalid email format"
        }
        else if(TextUtils.isEmpty(password)){
            // password isn't entered
            binding.passwordEditText.error = "Please enter password"
        }
        else if(password.length < 6){
            // password length is less than 6
            binding.passwordEditText.error = "Password must be at least 6 characters long"
        }
        else {
            // data is valid, continue to sign up
            firebaseSignUp()
        }
    }

    private fun firebaseSignUp(){
        // show progress dialog
        progressDialog.show()

        // create account
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                // signup success
                progressDialog.dismiss()
                //get current user
                val firebaseUser = firebaseAuth.currentUser
                val email = firebaseUser!!.email
                Toast.makeText(this, "Account created with email $email", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                // signup failed
                progressDialog.dismiss()
                Toast.makeText(this, "Signup Failed due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}