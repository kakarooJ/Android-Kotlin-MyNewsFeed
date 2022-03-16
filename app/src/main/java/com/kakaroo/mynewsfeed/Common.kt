package com.kakaroo.mynewsfeed

object Common {
    val MY_TAG                  =   "kakaroo-newsfeed"

    val PAGE_URL_NAVER          =   "http://newssearch.naver.com/search.naver?where=rss&query="
    val STOCK_URL_NAVER         =   "https://finance.naver.com/item/main.naver?code="//"https://api.finance.naver.com/siseJson.naver"
    val SEARCH_URL_NAVER        =   "https://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=0&ie=utf8&query="

    const val ARTICLE_URL       = 0
    const val STOCK_URL         = 1

    val DEFAULT_PAGE_KEYWORD    =   "메이저리그"
    val STOCK_PRICE_PLUS_WORD   =   "플러스"
    val STOCK_PRICE_MINUS_WORD  =   "마이너스"

    val REQUEST_INTERNET_PERMISSION     =   10

    //Settings
    val ARTICLE_MAX_NUM         =   50
    val CRAWLING_PERIOD_HOUR    =   2   //2시간마다
    val JSOUP_PARSING_DATA_MAX_NUM  =   10

    val DEFAULT_URL     :   String  = "http://3.35.40.166:8080"//"http://192.168.219.111:8080"//"http://127.0.0.1:8080"//"http://10.0.2.2:8080"//

    const val SUBJECT_VIEW = 1
    const val ARTICLE_VIEW = 2
}