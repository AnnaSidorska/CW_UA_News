package com.example.newsapp.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.activity.NewsFullActivity
import com.example.newsapp.activity.SavesActivity
import com.example.newsapp.model.Save
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class SavesRVAdapter(
    context: Context,
    private var savesList: MutableList<Save>,
    private val clickListener: itemClickListener
): RecyclerView.Adapter<SavesRVAdapter.SaveViewHolder>() {

    inner class SaveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTV: TextView = itemView.findViewById(R.id.save_title)
        val sourceTV: TextView = itemView.findViewById(R.id.save_source)
        val articleIV: ImageView = itemView.findViewById(R.id.save_image)
        val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaveViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.save_recycler_row, parent, false)
        return this.SaveViewHolder(view)

    }

    override fun getItemCount(): Int {
        return savesList.size
    }

    override fun onBindViewHolder(holder: SaveViewHolder, position: Int) {
        val saves: Save = savesList[position]

        holder.titleTV.text = saves.title
        holder.sourceTV.text = saves.source
        Picasso.get().load(saves.imageUrl).error(R.drawable.no_image).placeholder(R.drawable.no_image).into(holder.articleIV)

        holder.itemView.setOnClickListener{v ->
            val intent = Intent(v.context, NewsFullActivity::class.java)
            intent.putExtra("url", saves.url)
            v.context.startActivity(intent)
        }

        holder.btnDelete.setOnClickListener {
            clickListener.onDeleteClick(position)
        }
    }

    interface itemClickListener {
        fun onDeleteClick(position: Int)
    }
}