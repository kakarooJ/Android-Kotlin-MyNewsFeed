package com.kakaroo.mynewsfeed.entity

data class Topic(val idx: Int = 0,
                 val title: String = "",
                 val code: String = "",
                 val price: String = "",
                 val chartUrl: String = "",
                 val time: String = "",
                 val articles: ArrayList<Article>) {
    init {

    }
}
