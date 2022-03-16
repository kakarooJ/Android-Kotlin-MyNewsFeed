package com.kakaroo.mynewsfeed.entity

data class Topic(val idx: Int, var title: String, var code: String, var price: String, var articles: ArrayList<Article>)
