package com.example.newsapp.model

data class Save(
    val key: String?,
    val url: String,
    val title: String,
    val imageUrl: String,
    val source: String
) {
    constructor() : this("", "", "", "", "")
}