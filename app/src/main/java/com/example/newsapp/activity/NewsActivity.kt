package com.example.newsapp.activity

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.adapter.NewsRVAdapter
import com.example.newsapp.R
import com.google.android.material.navigation.NavigationView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.kwabenaberko.newsapilib.NewsApiClient
import com.kwabenaberko.newsapilib.models.Article
import com.kwabenaberko.newsapilib.models.request.TopHeadlinesRequest
import com.kwabenaberko.newsapilib.models.response.ArticleResponse
import com.squareup.picasso.Picasso

class NewsActivity : AppCompatActivity(), OnClickListener,
    NavigationView.OnNavigationItemSelectedListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var articleList: MutableList<Article>
    private lateinit var adapter: NewsRVAdapter
    private lateinit var progressIndicator: LinearProgressIndicator
    private lateinit var btn1: Button
    private lateinit var btn2: Button
    private lateinit var btn3: Button
    private lateinit var btn4: Button
    private lateinit var btn5: Button
    private lateinit var btn6: Button
    private lateinit var btn7: Button
    private lateinit var searchView: SearchView

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news)

        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
        toolbar = findViewById(R.id.toolbar)

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


        recyclerView = findViewById(R.id.news_recycler_view)
        progressIndicator = findViewById(R.id.progress_bar)
        searchView = findViewById(R.id.search_view)

        articleList = mutableListOf()

        btn1 = findViewById(R.id.btn_1)
        btn2 = findViewById(R.id.btn_2)
        btn3 = findViewById(R.id.btn_3)
        btn4 = findViewById(R.id.btn_4)
        btn5 = findViewById(R.id.btn_5)
        btn6 = findViewById(R.id.btn_6)
        btn7 = findViewById(R.id.btn_7)
        btn1.setOnClickListener(this)
        btn2.setOnClickListener(this)
        btn3.setOnClickListener(this)
        btn4.setOnClickListener(this)
        btn5.setOnClickListener(this)
        btn6.setOnClickListener(this)
        btn7.setOnClickListener(this)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                getNews("GENERAL", query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })

        setupRecyclerView()
        getNews("GENERAL", null)
        updateNavHeader()
    }

    private fun setupRecyclerView() {
        if (::articleList.isInitialized) {
            recyclerView.layoutManager = LinearLayoutManager(this)
            adapter = NewsRVAdapter(articleList)
            recyclerView.adapter = adapter
        } else {
            Log.e("NewsActivity", "articleList is not initialized")
        }
    }

    fun changeInProgress(show: Boolean) {
        if(show)
            progressIndicator.visibility = View.VISIBLE
        else
            progressIndicator.visibility = View.GONE
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }

    private fun getNews(category: String, query: String?) {
        if (!isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet", Toast.LENGTH_SHORT).show()
            return
        }
        changeInProgress(true)
        val newsApiClient = NewsApiClient("acb71ba7c6144785aaa1c9b26d1341b5")
        newsApiClient.getTopHeadlines(
            TopHeadlinesRequest.Builder().language("uk").category(category).q(query).build(),
            object : NewsApiClient.ArticlesResponseCallback {
                override fun onSuccess(response: ArticleResponse) {
                    runOnUiThread {
                        changeInProgress(false)
                        articleList = response.articles
                        adapter.updateData(articleList)
                        adapter.notifyDataSetChanged()
                    }
                }


                override fun onFailure(throwable: Throwable) {
                    Toast.makeText(this@NewsActivity, "Got failure. No internet connection!", Toast.LENGTH_SHORT).show()
                    Log.i("GOT FAILURE", throwable.toString())
                }
            }
        )
    }

    override fun onClick(v: View?) {
        val btn: Button = v as Button
        val category: String = btn.text.toString()
        getNews(category, null)
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
            R.id.news -> return true
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
        val email:TextView = headerView.findViewById(R.id.user_email_header)
        val name: TextView = headerView.findViewById(R.id.user_name_header)
        val image: ImageView = headerView.findViewById(R.id.profile_image_header)

        email.text = user.email
        name.text = user.displayName

        Picasso.get().load(user.photoUrl).into(image)
    }

}


