package com.example.newsapp.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.newsapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class RegActivity : AppCompatActivity() {
    private lateinit var userImage: ImageView
    private lateinit var userName: EditText
    private lateinit var userEmail: EditText
    private lateinit var userPassword: EditText
    private lateinit var userConfirmPassword: EditText
    private lateinit var buttonReg: Button
    private lateinit var linkToAuth: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var progressBar: ProgressBar

    private var pReqCode: Int = 1
    private var requescode: Int = 1
    private var pickedImage: Uri? = null

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, NewsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reg)

        userImage = findViewById(R.id.userImage)
        userName = findViewById(R.id.userName)
        userEmail = findViewById(R.id.userEMail)
        userPassword = findViewById(R.id.userPassword)
        userConfirmPassword = findViewById(R.id.confirmUserPassword)
        buttonReg = findViewById(R.id.buttonReg)
        linkToAuth = findViewById(R.id.label_link_to_auth)

        auth = FirebaseAuth.getInstance()
        progressBar = findViewById(R.id.reg_progress_bar)

        userImage.setOnClickListener {
            when {
                Build.VERSION.SDK_INT >= 24 -> checkAndRequestForPermission()
                else -> openGallery()
            }
        }

        buttonReg.setOnClickListener {
            buttonReg.visibility = View.INVISIBLE
            progressBar.visibility = View.VISIBLE
            val name = userName.text.toString().trim()
            val email = userEmail.text.toString().trim()
            val pass = userPassword.text.toString().trim()
            val pass2 = userConfirmPassword.text.toString().trim()

            if(TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(pass) || pass != pass2) {
                userName.error = "Fill the field"
                userEmail.error = "Fill the field"
                userPassword.error = "Fill the field"
                userConfirmPassword.error = "Fill the field"
                buttonReg.visibility = View.VISIBLE
                progressBar.visibility = View.INVISIBLE
            } else {
                createUserAccount(name, email, pass)
            }

        }
        linkToAuth.setOnClickListener {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
        }
    }

    private fun createUserAccount(name: String, email: String, pass: String) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(
                        this,
                        "Account $email created.",
                        Toast.LENGTH_SHORT,
                    ).show()

                    val currentUser = auth.currentUser

                    if (pickedImage.toString().isNotEmpty()) {
                        // Якщо користувач обрав картинку, викликаємо метод updateUserInformation
                        updateUserInformation(name, pickedImage, currentUser!!)
                    } else {
                        // Якщо картинка не обрана, викликаємо updateUserInformation зі значенням null
                        updateUserInformation(name, null, currentUser!!)
                    }

                    val intent = Intent(this, NewsActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(
                        this,
                        "Account creation failed. " + task.exception!!.message.toString(),
                        Toast.LENGTH_SHORT,
                    ).show()
                    buttonReg.visibility = View.VISIBLE
                    progressBar.visibility = View.INVISIBLE
                }
            }
    }


    private fun updateUserInformation(name: String, pickedImageUri: Uri?, currentUser: FirebaseUser) {
        val mStorage: StorageReference = FirebaseStorage.getInstance().reference.child("users_photos")
        if (pickedImageUri != null) {
            val imageFilePath: StorageReference = mStorage.child(pickedImageUri.lastPathSegment!!)

            imageFilePath.putFile(pickedImageUri)
                .addOnSuccessListener { _ ->
                    imageFilePath.downloadUrl
                        .addOnSuccessListener { uri ->
                            val profileUpdate = UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .setPhotoUri(uri)
                                .build()

                            currentUser.updateProfile(profileUpdate)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(this, "Register complete", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(this, NewsActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        Toast.makeText(this, "Failed to update profile: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            val profileUpdate = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .setPhotoUri(Uri.parse("android.resource://${packageName}/${R.drawable.default_user_image}"))
                .build()

            currentUser.updateProfile(profileUpdate)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Register complete", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, NewsActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Failed to update profile: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }



    private fun openGallery() {
        val galleryIntent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_OPEN_DOCUMENT
        }
        startActivityForResult(galleryIntent, requescode)
    }

    private fun checkAndRequestForPermission() {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "Please, accept the required permission", Toast.LENGTH_SHORT).show()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), pReqCode)
            }

        } else {
            openGallery()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == requescode && data != null) {
            pickedImage = data.data!!
            userImage.setImageURI(pickedImage)
        }
    }

}