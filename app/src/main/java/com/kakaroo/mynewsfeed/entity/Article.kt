package com.kakaroo.mynewsfeed.entity

data class Article(
    val idx: Int = 0,
    val date: String ="",
    var title: String ="",
    val url: String ="",
    val imgUrl: String ="",
    var newsCorp: String ="")