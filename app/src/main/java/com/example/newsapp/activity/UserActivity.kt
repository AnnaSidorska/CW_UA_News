package com.example.newsapp.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.newsapp.R
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.squareup.picasso.Picasso

class UserActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener  {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar

    private lateinit var userName: EditText
    private lateinit var userEmail: EditText
    private lateinit var userImage: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var button: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
        toolbar = findViewById(R.id.toolbar)

        userName = findViewById(R.id.profile_name)
        userEmail = findViewById(R.id.profile_email)
        userImage = findViewById(R.id.profile_image)
        progressBar = findViewById(R.id.progress_changes)
        progressBar.visibility = View.GONE
        button = findViewById(R.id.bthSetChanges)

        button.setOnClickListener{
            updateBtnClick()
        }

        setSupportActionBar(toolbar)

        navigationView.bringToFront()
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener(this)
        navigationView.setCheckedItem(R.id.profile)

        updateNavHeader()
        getUserData()
    }

    private fun getUserData() {
        userName.setText(user.displayName)
        userEmail.setText(user.email)
        Picasso.get().load(user.photoUrl).into(userImage)
    }

    private fun updateBtnClick() {
        val newName = userName.text.toString().trim()

        if(TextUtils.isEmpty(newName) || newName.length < 3) {
            userName.error = "Fill the field"

        } else {
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build()

            progressBar.visibility = View.VISIBLE
            user.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
                    progressBar.visibility = View.GONE
                    if (task.isSuccessful) {
                        Toast.makeText(this, "User profile updated.", Toast.LENGTH_SHORT,).show()
                    } else {
                        Toast.makeText(this, "Failed to update account. " + task.exception.toString(), Toast.LENGTH_SHORT,).show()
                    }
                }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.news -> {
                val intent = Intent(this, NewsActivity::class.java)
                startActivity(intent)
                finish()
            }
            R.id.profile -> {
                return true
            }
            R.id.chat -> {
                val intent = Intent(this, ChatActivity::class.java)
                startActivity(intent)
                finish()
            }
            R.id.saves -> {
                val intent = Intent(this, SavesActivity::class.java)
                startActivity(intent)
                finish()
            }
            R.id.logout -> {
                FirebaseAuth.getInstance().signOut()
                Toast.makeText(this, "User logged out", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, AuthActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun updateNavHeader() {
        val navigationView: NavigationView = findViewById(R.id.navigation_view)
        val headerView: View = navigationView.getHeaderView(0)
        val email:TextView = headerView.findViewById(R.id.user_email_header)
        val name: TextView = headerView.findViewById(R.id.user_name_header)
        val image: ImageView = headerView.findViewById(R.id.profile_image_header)

        email.text = user.email
        name.text = user.displayName

        Picasso.get().load(user.photoUrl).into(image)
    }
}