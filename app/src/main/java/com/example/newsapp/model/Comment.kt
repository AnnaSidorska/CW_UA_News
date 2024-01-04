package com.example.newsapp.model

import com.google.firebase.database.ServerValue

data class Comment(
    val key: String,
    val content: String,
    val uid: String,
    val uimg: String,
    val uname: String,
    val timeStamp: Any = ServerValue.TIMESTAMP
) {
    // порожній конструктор без аргументів
    constructor() : this("", "", "", "", "", ServerValue.TIMESTAMP)
}
