package com.example.newsapp.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.newsapp.R
import com.example.newsapp.model.Save
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class NewsFullActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var webView: WebView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar

    private lateinit var button: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private lateinit var firebaseDatabase: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_full)

        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!
        firebaseDatabase = FirebaseDatabase.getInstance()

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
        toolbar = findViewById(R.id.toolbar)

        button = findViewById(R.id.save_article)

        button.setOnClickListener {
            btnSaveArticle()
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
        navigationView.setCheckedItem(R.id.news)

        val url: String = intent.getStringExtra("url")!!
        webView = findViewById(R.id.web_view)
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        webView.loadUrl(url)

        updateNavHeader()
    }

    private fun btnSaveArticle() {
        val saveReference: DatabaseReference = firebaseDatabase.getReference("Saves").child(user.uid).push()
        val key = saveReference.key
        val defaultImageUrl = "@drawable/no_image"
        val url: String = intent.getStringExtra("url")!!
        val title: String = intent.getStringExtra("title")!!
        val imageUrl: String = intent.getStringExtra("image")?.takeIf { it.isNotEmpty() } ?: defaultImageUrl
        val source: String = intent.getStringExtra("source")!!
        val save = Save(key, url, title, imageUrl, source)

        saveReference.setValue(save)
            .addOnSuccessListener {
                Toast.makeText(this, "Save added", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Fail to save article" + e.message.toString(), Toast.LENGTH_SHORT).show()
            }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.news -> {
                val intent = Intent(this, NewsActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.profile -> {
                val intent = Intent(this, UserActivity::class.java)
                startActivity(intent)
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
        val email: TextView = headerView.findViewById(R.id.user_email_header)
        val name: TextView = headerView.findViewById(R.id.user_name_header)
        val image: ImageView = headerView.findViewById(R.id.profile_image_header)

        email.text = user.email
        name.text = user.displayName

        Picasso.get().load(user.photoUrl).into(image)
    }
}