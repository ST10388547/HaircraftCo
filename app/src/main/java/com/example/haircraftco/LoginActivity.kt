package com.example.haircraftco

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, LocaleHelper.getLanguage(newBase)))
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        setupGoogleSignIn()
        setupGoogleSignInLauncher()
        setupViews()
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setupGoogleSignInLauncher() {
        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val data = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    firebaseAuthWithGoogle(idToken)
                } else {
                    Toast.makeText(this, "Google sign-in failed: no ID token", Toast.LENGTH_LONG).show()
                    Log.e("LoginActivity", "Google sign-in failed: ID token is null")
                }
            } catch (e: ApiException) {
                Log.e("LoginActivity", "Google sign-in failed", e)
                Toast.makeText(
                    this,
                    "Google sign-in failed: ${e.statusCode} - ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setupViews() {
        val emailEt = findViewById<EditText>(R.id.et_email)
        val passwordEt = findViewById<EditText>(R.id.et_password)
        val loginBtn = findViewById<Button>(R.id.btn_login)
        val registerBtn = findViewById<Button>(R.id.btn_register)
        val googleBtn = findViewById<Button>(R.id.btn_google)
        val forgotPasswordTv = findViewById<TextView>(R.id.tv_forgot_password)

        loginBtn.setOnClickListener {
            val email = emailEt.text.toString().trim()
            val password = passwordEt.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            loginWithEmail(email, password)
        }

        registerBtn.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        googleBtn.setOnClickListener {
            startGoogleSignIn()
        }

        forgotPasswordTv.setOnClickListener {
            showForgotPasswordDialog()
        }
    }

    private fun loginWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    checkUserRoleAndNavigate(auth.currentUser?.uid)
                } else {
                    Log.e("LoginActivity", "Email login failed", task.exception)
                    Toast.makeText(
                        this,
                        "Login failed: ${task.exception?.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun startGoogleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) saveGoogleUserToFirestore(user)
                } else {
                    Log.e("LoginActivity", "Firebase auth with Google failed", task.exception)
                    Toast.makeText(
                        this,
                        "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun saveGoogleUserToFirestore(user: com.google.firebase.auth.FirebaseUser) {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account == null) {
            Toast.makeText(this, "Google account info missing", Toast.LENGTH_SHORT).show()
            return
        }
        val userMap = hashMapOf(
            "name" to (account.displayName ?: ""),
            "email" to user.email,
            "phone" to "",
            "photo" to (account.photoUrl?.toString() ?: ""),
            "loyaltyPoints" to 0,
            "isAdmin" to false
        )
        db.collection("users").document(user.uid)
            .set(userMap)
            .addOnSuccessListener { checkUserRoleAndNavigate(user.uid) }
            .addOnFailureListener {
                Log.e("LoginActivity", "Failed to save user in Firestore", it)
                Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkUserRoleAndNavigate(userId: String?) {
        if (userId == null) return
        db.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                val isAdmin = doc.getBoolean("isAdmin") ?: false
                val nextActivity = if (isAdmin) AdminActivity::class.java else HomeActivity::class.java
                startActivity(Intent(this, nextActivity))
                finish()
            }
            .addOnFailureListener {
                Log.e("LoginActivity", "Failed to fetch user role", it)
                Toast.makeText(this, "Failed to determine user role", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showForgotPasswordDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Forgot Password")

        val input = EditText(this)
        input.hint = "Enter your email"
        input.inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        builder.setView(input)

        builder.setPositiveButton("Send") { _, _ ->
            val email = input.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Password reset email sent to $email",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Log.e("LoginActivity", "Failed to send reset email", task.exception)
                        Toast.makeText(
                            this,
                            "Failed to send reset email: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }
}
