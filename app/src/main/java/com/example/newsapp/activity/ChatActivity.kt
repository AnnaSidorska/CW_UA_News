package com.example.newsapp.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.adapter.CommentRVAdapter
import com.example.newsapp.model.Comment
import com.example.newsapp.R
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class ChatActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, CommentRVAdapter.deleteClickListener {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar

    private lateinit var recyclerView: RecyclerView
    private lateinit var commentList: MutableList<Comment>
    private lateinit var adapter: CommentRVAdapter

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser

    private lateinit var commentUserImage: ImageView
    private lateinit var editTextComment: EditText
    private lateinit var btnAddComment: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
        toolbar = findViewById(R.id.toolbar)

        recyclerView = findViewById(R.id.rv_comment)
        recyclerView.isVerticalScrollBarEnabled = true

        auth = FirebaseAuth.getInstance()

        user = auth.currentUser!!
        firebaseDatabase = FirebaseDatabase.getInstance()

        commentUserImage = findViewById(R.id.chat_currentuser_img)
        editTextComment = findViewById(R.id.chat_comment_edit_text)
        btnAddComment = findViewById(R.id.chat_add_comment_btn)
        Picasso.get().load(user.photoUrl).into(commentUserImage)
        progressBar = findViewById(R.id.chat_progressbar)
        progressBar.visibility = View.INVISIBLE

        initCommentRV()

        setSupportActionBar(toolbar)

        navigationView.bringToFront()
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener(this)
        navigationView.setCheckedItem(R.id.chat)

        updateNavHeader()

        btnAddComment.setOnClickListener {
            btnAddComment.visibility = View.INVISIBLE
            progressBar.visibility = View.VISIBLE
            val commentReference: DatabaseReference = firebaseDatabase.getReference("Comment").child("news").push()
            val key = commentReference.key
            val commentContent = editTextComment.text.toString()
            val uid = user.uid
            val uname = user.displayName.toString()
            val uimg = user.photoUrl.toString()
            val comment= Comment(key!!, commentContent, uid, uimg, uname)

            commentReference.setValue(comment)
                .addOnSuccessListener {
                    Toast.makeText(this, "Comment added", Toast.LENGTH_SHORT).show()
                    editTextComment.setText("")
                    btnAddComment.visibility = View.VISIBLE
                    progressBar.visibility = View.INVISIBLE
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Fail to add comment" + e.message.toString(), Toast.LENGTH_SHORT).show()
                }

        }
    }

    private fun initCommentRV() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        val commentRef: DatabaseReference = firebaseDatabase.getReference("Comment").child("news")
        progressBar.visibility = View.VISIBLE

        commentRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                commentList = mutableListOf()
                for (snap in dataSnapshot.children) {
                    val comment = snap.getValue(Comment::class.java)
                    commentList.add(comment!!)
                }
                commentList.reverse()
                adapter = CommentRVAdapter(applicationContext, commentList, this@ChatActivity)
                recyclerView.adapter = adapter

                progressBar.visibility = View.INVISIBLE
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@ChatActivity, "Fail to get comments", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.INVISIBLE
            }
        })
    }

    override fun onDeleteClick(position: Int) {
        val key = commentList[position].key
        val commentRef: DatabaseReference = firebaseDatabase.getReference("Comment").child("news").child(key)
        val mTask = commentRef.removeValue()

        mTask.addOnSuccessListener {
            Toast.makeText(this, "Comment deleted", Toast.LENGTH_SHORT).show()
            adapter = CommentRVAdapter(applicationContext, commentList, this@ChatActivity)
            recyclerView.adapter = adapter
        }

        mTask.addOnFailureListener {
            Toast.makeText(this, "Comment is not deleted", Toast.LENGTH_SHORT).show()
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
                return true
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
        val email: TextView = headerView.findViewById(R.id.user_email_header)
        val name: TextView = headerView.findViewById(R.id.user_name_header)
        val image: ImageView = headerView.findViewById(R.id.profile_image_header)

        email.text = user.email
        name.text = user.displayName

        Picasso.get().load(user.photoUrl).into(image)
    }
}