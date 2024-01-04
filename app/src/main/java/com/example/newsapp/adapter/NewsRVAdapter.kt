package com.example.newsapp.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.activity.NewsFullActivity
import com.example.newsapp.R
import com.kwabenaberko.newsapilib.models.Article
import com.squareup.picasso.Picasso

class NewsRVAdapter(
    private var articleList: MutableList<Article>
) : RecyclerView.Adapter<NewsRVAdapter.NewsViewHolder>() {

    inner class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTV: TextView = itemView.findViewById(R.id.article_title)
        val sourceTV: TextView = itemView.findViewById(R.id.article_source)
        val newsIV: ImageView = itemView.findViewById(R.id.article_image_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.news_recycler_row, parent, false)
        return this.NewsViewHolder(view)
    }

    fun updateData(data: List<Article>) {
        articleList.clear()
        articleList.addAll(data)
    }

    override fun getItemCount(): Int {
        return articleList.size
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val articles: Article = articleList[position]
        holder.titleTV.text = articles.title
        holder.sourceTV.text = articles.source.name
        Picasso.get().load(articles.urlToImage).error(R.drawable.no_image).placeholder(R.drawable.no_image).into(holder.newsIV)

        holder.itemView.setOnClickListener { v ->
            val intent = Intent(v.context, NewsFullActivity::class.java)
            intent.putExtra("title", articles.title)
            intent.putExtra("url", articles.url)
            intent.putExtra("image", articles.urlToImage)
            intent.putExtra("source", articles.source.name)
            v.context.startActivity(intent)
        }

    }
}