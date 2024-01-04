package com.example.newsapp.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.adapter.SavesRVAdapter
import com.example.newsapp.model.Save
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class SavesActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, SavesRVAdapter.itemClickListener {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar

    private lateinit var recyclerView: RecyclerView
    private lateinit var savesList: MutableList<Save>
    private lateinit var adapter: SavesRVAdapter

    private lateinit var progressBar: ProgressBar

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saves)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
        toolbar = findViewById(R.id.toolbar)
        progressBar = findViewById(R.id.progress_bar)
        progressBar.visibility = View.INVISIBLE

        recyclerView = findViewById(R.id.rv_comment)
        recyclerView.isVerticalScrollBarEnabled = true

        savesList = mutableListOf()

        adapter = SavesRVAdapter(applicationContext, savesList, this)
        recyclerView.adapter = adapter

        setSupportActionBar(toolbar)

        navigationView.bringToFront()
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener(this)
        navigationView.setCheckedItem(R.id.saves)

        recyclerView = findViewById(R.id.rv_comment)
        recyclerView.isVerticalScrollBarEnabled = true

        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!
        firebaseDatabase = FirebaseDatabase.getInstance()

        initSaveRV()
        updateNavHeader()


    }

    private fun initSaveRV() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        val saveRed: DatabaseReference = firebaseDatabase.getReference("Saves").child(user.uid)
        progressBar.visibility = View.VISIBLE

        saveRed.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                savesList = mutableListOf()
                for(snap in snapshot.children) {
                    val save = snap.getValue(Save::class.java)
                    savesList.add(save!!)
                }
                adapter = SavesRVAdapter(applicationContext, savesList, this@SavesActivity)
                recyclerView.adapter = adapter
                progressBar.visibility = View.INVISIBLE
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SavesActivity, "Fail to get saves", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.INVISIBLE
            }
        })
    }

    override fun onDeleteClick(position: Int) {
        val key= savesList[position].key
        val saveRed: DatabaseReference = firebaseDatabase.getReference("Saves").child(user.uid).child(key!!)
        val mTask = saveRed.removeValue()

        mTask.addOnSuccessListener {
            Toast.makeText(this, "Save deleted", Toast.LENGTH_SHORT).show()
            adapter = SavesRVAdapter(applicationContext, savesList, this@SavesActivity)
            recyclerView.adapter = adapter
        }

        mTask.addOnFailureListener {
            Toast.makeText(this, "Save is not deleted", Toast.LENGTH_SHORT).show()
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
                val intent = Intent(this, UserActivity::class.java)
                startActivity(intent)
                finish()
            }
            R.id.chat -> {
                val intent = Intent(this, ChatActivity::class.java)
                startActivity(intent)
                finish()
            }
            R.id.saves -> {
                return true
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
        val email: TextView = headerView.findViewById(R.id.user_email_header)
        val name: TextView = headerView.findViewById(R.id.user_name_header)
        val image: ImageView = headerView.findViewById(R.id.profile_image_header)

        email.text = user.email
        name.text = user.displayName

        Picasso.get().load(user.photoUrl).into(image)
    }
}