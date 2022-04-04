package com.kakaroo.mynewsfeed.utility

object Common {
    const val MY_TAG                  =   "kakaroo-newsfeed"

    //val PAGE_URL_NAVER          =   "https://search.naver.com/search.naver?where=news&sm=tab_pge&query="
    const val PAGE_URL_START          =   "&start="
    const val PAGE_URL_NAVER          =   "http://newssearch.naver.com/search.naver?where=rss&query="//"https://openapi.naver.com/v1/search/news.json?query="
    const val PAGE_URL_GOOGLE         =   "https://news.google.com/rss/search?hl=ko&gl=KR&ie=UTF-8&q="
    const val STOCK_URL_NAVER         =   "https://finance.naver.com/item/main.naver?code="//"https://api.finance.naver.com/siseJson.naver"
    const val SEARCH_URL_NAVER        =   "https://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=0&ie=utf8&query="

    enum class SearchEngine(val value: Int, val valueName: String, val url: String) {
        ENGINE_NAVER(0, "Naver", PAGE_URL_NAVER),
        ENGINE_GOOGLE(1, "Google", PAGE_URL_GOOGLE)
    }

    const val ARTICLE_URL       = 0
    const val STOCK_URL         = 1

    const val DEFAULT_PAGE_KEYWORD    =   "메이저리그"
    const val STOCK_PRICE_PLUS_WORD   =   "플러스"
    const val STOCK_PRICE_MINUS_WORD  =   "마이너스"

    const val HTTP_CRAWLING_TIMEOUT_MILLIS  =   10000L

    const val REQUEST_INTERNET_PERMISSION     =   10

    //Settings
    const val ARTICLE_MAX_NUM         =   50
    const val CRAWLING_PERIOD_HOUR    =   2   //2시간마다
    const val JSOUP_PARSING_DATA_MAX_NUM  =   10

    const val DEFAULT_URL     :   String  = "http://127.0.0.1:8080"//http://3.35.40.166:8080"//"http://192.168.219.111:8080"//"http://10.0.2.2:8080"//

    const val SUBJECT_VIEW = 1
    const val ARTICLE_VIEW = 2

    const val SERVER_FUNCTION_ENABLE    =   0

    const val KEYOWRD_HINT  =   "복수의 키워드는 \",\"로 구분하고, 주식종목코드는 \"#\"을 붙여주세요. (예.삼성전자#005930,코로나19,LG#003550)"
}