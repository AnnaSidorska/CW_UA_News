package com.example.newsapp.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.newsapp.R
import com.google.firebase.auth.FirebaseAuth

class AuthActivity : AppCompatActivity() {
    private lateinit var userEmail: EditText
    private lateinit var userPassword: EditText
    private lateinit var button: Button
    private lateinit var auth: FirebaseAuth

    private lateinit var progressBar: ProgressBar

    private lateinit var linkToReg: TextView
    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            updateUI()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        userEmail = findViewById(R.id.userEMailAuth)
        userPassword = findViewById(R.id.userPasswordAuth)
        button = findViewById(R.id.buttonAuth)
        auth = FirebaseAuth.getInstance()

        progressBar = findViewById(R.id.auth_progress_bar)
        progressBar.visibility = View.INVISIBLE

        linkToReg = findViewById(R.id.label_link_to_reg)

        linkToReg.setOnClickListener {
            val intent = Intent(this, RegActivity::class.java)
            startActivity(intent)
            finish()
        }

        button.setOnClickListener {
            button.visibility = View.INVISIBLE
            progressBar.visibility = View.VISIBLE
            val email = userEmail.text.toString().trim()
            val pass = userPassword.text.toString().trim()

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
                userEmail.error = "Fill the field"
                userPassword.error = "Fill the field"
                button.visibility = View.VISIBLE
                progressBar.visibility = View.INVISIBLE
            } else {
                if(pass.isNotEmpty() || email.isNotEmpty()) {
                    auth.signInWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(this) { task ->
                            button.visibility = View.VISIBLE
                            progressBar.visibility = View.GONE
                            if (task.isSuccessful) {
                                Toast.makeText(baseContext, "Login successful.", Toast.LENGTH_SHORT)
                                    .show()
                                updateUI()
                            } else {
                                Toast.makeText(
                                    baseContext,
                                    "Login failed. " + task.exception!!.message.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()

                            }
                        }
                } else {
                    userPassword.error = "Password can't be empty"
                    userEmail.error = "Email can't be empty"
                    progressBar.visibility = View.INVISIBLE
                }

            }
        }
    }

    private fun updateUI() {
        val intent = Intent(this, NewsActivity::class.java)
        startActivity(intent)
        finish()
    }
}
