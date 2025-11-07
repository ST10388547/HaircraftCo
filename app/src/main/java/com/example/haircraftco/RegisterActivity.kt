package com.example.haircraftco

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Toast

class RegisterActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, LocaleHelper.getLanguage(newBase)))
    }

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val nameEt = findViewById<EditText>(R.id.et_name)
        val emailEt = findViewById<EditText>(R.id.et_email)
        val phoneEt = findViewById<EditText>(R.id.et_phone)
        val passwordEt = findViewById<EditText>(R.id.et_password)
        val registerBtn = findViewById<Button>(R.id.btn_register)

        registerBtn.setOnClickListener {
            val name = nameEt.text.toString()
            val email = emailEt.text.toString()
            val phone = phoneEt.text.toString()
            val password = passwordEt.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            if (user != null) {
                                val userMap = hashMapOf(
                                    "name" to name,
                                    "email" to email,
                                    "phone" to phone,
                                    "photo" to "",
                                    "loyaltyPoints" to 0,
                                    "isAdmin" to false
                                )
                                db.collection("users").document(user.uid).set(userMap)
                                    .addOnSuccessListener {
                                        // 🔔 Send a welcome notification
                                        NotificationsHelper.sendWelcomeNotification(this)

                                        startActivity(Intent(this, HomeActivity::class.java))
                                        finish()
                                    }
                            } else {
                                Toast.makeText(
                                    this,
                                    "Registration failed: ${task.exception?.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
            }
        }
    }
}