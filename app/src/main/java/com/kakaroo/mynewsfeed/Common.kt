package com.kakaroo.mynewsfeed

object Common {
    val MY_TAG                  =   "kakaroo-newsfeed"
    val SHARED_PREF_NAME        = "shared_pref"

    val PAGE_URL_NAVER          =   "http://newssearch.naver.com/search.naver?where=rss&query="
    val DEFAULT_PAGE_KEYWORD    =   "메이저리그"

    val REQUEST_INTERNET_PERMISSION     =   10

    //Settings
    val KEYWORD_MAX_NUM         =   10
    val CRAWLING_PERIOD_HOUR    =   2   //2시간마다
    val JSOUP_PARSING_DATA_MAX_NUM  =   10

    val DEFAULT_URL     :   String  = "http://3.35.40.166:8080"//"http://192.168.219.111:8080"//"http://127.0.0.1:8080"//"http://10.0.2.2:8080"//

    const val SUBJECT_VIEW = 1
    const val ARTICLE_VIEW = 2
}