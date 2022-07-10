package com.example.myapplication

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import com.example.myapplication.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import java.lang.Exception

class LoginActivity : AppCompatActivity() {

    // ViewBinding
    private lateinit var binding: ActivityLoginBinding


    /** Password variables */
    // ActionBar
    private lateinit var actionBar: ActionBar

    // ProgressDialog
    private lateinit var progressDialog: ProgressDialog

    // FirebaseAuth
    private lateinit var firebaseAuth: FirebaseAuth

    private var email = ""
    private var password = ""

    /** Google variables */

    // client for interacting with Google Sign In API
    private lateinit var googleSignInClient: GoogleSignInClient

    // constants
    private companion object {
        private const val RC_SIGN_IN = 100
        private const val TAG = "GOOGLE_SIGN_IN_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // configure actionbar
        actionBar = supportActionBar!!
        actionBar.title="Login"

        // configure progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setMessage("Logging In...")
        progressDialog.setCanceledOnTouchOutside(false)

        //init firebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()
        // check if user is already logged in
        checkUser()

        /** Login & password authorization */
        // handle click, open SignupActivity
        binding.notHaveAccountTextLabel.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        // handle click, begin login
        binding.loginButton.setOnClickListener {
            // before logging in, validate data
            validateData()
        }

        /** Google authorization */
        // configure Google SignIn
        // GoogleSignInOptions contains options to configure Google Sign In API
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // will be resolved when build first time
            .requestEmail() // we only need email from google account
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        // handle click, begin Google Sign In
        binding.googleLoginButton.setOnClickListener {
            // Begin Google SignIn
            Log.d(TAG, "onCreate: begin Google SignIn")
            // intent used to start sign-in flow
            val intent = googleSignInClient.signInIntent
            startActivityForResult(intent, RC_SIGN_IN)
        }

    }

    /** Google Sign In function */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == RC_SIGN_IN) {
            Log.d(TAG, "onActivityResult: Google SignIn intent result")
            val accountAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google sign in success, now auth with firebase
                val accountAccount = accountAccountTask.getResult(ApiException::class.java)
                firebaseAuthWithGoogleAccount(accountAccount)
            } catch(e: Exception) {
                // failed Google SignIn
                Log.d(TAG, "onActivityResult: ${e.message}")
            }
        }
    }

    /** Google Sign In function */
    private fun firebaseAuthWithGoogleAccount(account: GoogleSignInAccount?) {
        Log.d(TAG, "firebaseAuthWithGoogleAccount: begin firebase auth with Google account")

        val credential = GoogleAuthProvider.getCredential(account!!.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                // login success
                Log.d(TAG, "firebaseAuthWithGoogleAccount: LoggedIn")

                // get loggedIN user
                val firebaseUser = firebaseAuth.currentUser
                // get user info
                val uid = firebaseUser!!.uid
                val email = firebaseUser!!.email

                Log.d(TAG, "firebaseAuthWithGoogleAccount: Uid $uid")
                Log.d(TAG, "firebaseAuthWithGoogleAccount: Email $email")

                if(authResult.additionalUserInfo!!.isNewUser){
                    // user is new
                    Log.d(TAG, "firebaseAuthWithGoogleAccount: Account created... \n$email")
                    Toast.makeText(this,"LoggedIn.. \n$email", Toast.LENGTH_SHORT).show()
                }
                else {
                    // existing user
                    Log.d(TAG, "firebaseAuthWithGoogleAccount: Existing user... \n$email")
                    Toast.makeText(this, "LoggedIn... \n$email", Toast.LENGTH_SHORT).show()
                }

                // start app activity
                startActivity(Intent(this, MainActivity::class.java))
                finish()

            }
            .addOnFailureListener { e ->
                Log.d(TAG, "firebaseAuthWithGoogleAccount: Login Failed due to ${e.message}")
                Toast.makeText(this, "Login Failed due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /** Login & password function */
    private fun validateData(){
        // get data
        email = binding.loginEditText.text.toString().trim()
        password = binding.passwordEditText.text.toString().trim()

        // validate data
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            // invalid email format
            binding.loginEditText.error = "Invalid email format"
        }
        else if(TextUtils.isEmpty(password)) {
            binding.passwordEditText.error = "Please enter password"
        }
        else {
            // data is validated, begin login
            firebaseLogin()
        }


    }

    /** Login & password function */
    private fun firebaseLogin() {
        // show progress
        progressDialog.show()
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                // login success
                progressDialog.dismiss()
                //get user info
                val firebaseUser = firebaseAuth.currentUser
                val email = firebaseUser!!.email
                Toast.makeText(this, "LoggedIn as $email", Toast.LENGTH_SHORT).show()
                // open app
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener{ e ->
                // login failed
                progressDialog.dismiss()
                Toast.makeText(this, "Login failed dut to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /** overall function */
    private fun checkUser(){
        // if user is already logged in go to the profile activity
        // get current user
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser != null) {
            // user is already logged in
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}