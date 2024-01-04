package com.example.newsapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.model.Comment
import com.example.newsapp.R
import android.text.format.DateFormat
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import java.util.Calendar
import java.util.Locale

class CommentRVAdapter(
    context: Context,
    private var commentList: MutableList<Comment>,
    private val clickListener: deleteClickListener
): RecyclerView.Adapter<CommentRVAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val imgUserIV: ImageView = itemView.findViewById(R.id.comment_user_img)
        val nameTV: TextView = itemView.findViewById(R.id.comment_username)
        val contentTV: TextView = itemView.findViewById(R.id.comment_content)
        val dateTV: TextView = itemView.findViewById(R.id.comment_date)
        val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val row: View = LayoutInflater.from(parent.context).inflate(R.layout.comment_recycler_row, parent, false)
        return this.CommentViewHolder(row)
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comments: Comment = commentList[position]
        holder.nameTV.text = comments.uname
        holder.contentTV.text = comments.content
        holder.dateTV.text = timeStampToString(comments.timeStamp)
        Picasso.get().load(comments.uimg).error(R.drawable.no_image).placeholder(R.drawable.no_image).into(holder.imgUserIV)

        val isCurrentUserAuthor = comments.uid == FirebaseAuth.getInstance().currentUser?.uid

        holder.btnDelete.visibility = if (isCurrentUserAuthor) View.VISIBLE else View.GONE


        holder.btnDelete.setOnClickListener {
            clickListener.onDeleteClick(position)
        }
    }

    private fun timeStampToString(time: Any): String {
        val calendar: Calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = time as Long
        return DateFormat.format("HH:mm", calendar).toString()
    }

    interface deleteClickListener {
        fun onDeleteClick(position: Int)
    }

}