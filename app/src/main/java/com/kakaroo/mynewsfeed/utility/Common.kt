package com.kakaroo.mynewsfeed.utility

object Common {
    const val MY_TAG = "kakaroo-newsfeed"

    //val PAGE_URL_NAVER          =   "https://search.naver.com/search.naver?where=news&sm=tab_pge&query="
    const val PAGE_URL_START = "&start="
    const val PAGE_URL_NAVER =
        "https://search.naver.com/search.naver?where=news&sort=1&query="//"http://newssearch.naver.com/search.naver?where=rss&query="//"https://openapi.naver.com/v1/search/news.json?query="
    const val PAGE_NAVER_TAG = "&start=1"

    const val PAGE_URL_GOOGLE =
        "https://news.google.com/rss/search?hl=ko&gl=KR&ie=UTF-8&q="
    const val PAGE_GOOGLE_TAG = ""

    const val PAGE_URL_DAUM =
        "https://search.daum.net/search?w=news&DA=STC&enc=utf8&cluster=y&cluster_page=1&sort=recency&q="
    const val PAGE_DAUM_TAG = "&p="

    const val STOCK_URL_NAVER =
        "https://finance.naver.com/item/main.naver?code="//"https://api.finance.naver.com/siseJson.naver"
    const val SEARCH_URL_NAVER =
        "https://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=0&ie=utf8&query="

    const val JSOUP_LINK_TAG= "a[href]"
    const val JSOUP_LINK_POSTFIX_TAG= "href"
    const val JSOUP_IMG_TAG= "img"

    data class JSoupTag(
        val item: String = "",
        val item2: String = "",
        val date: String = "",
        val title: String = "",
        val link: String = "",
        val corp: String = "",
        val img: String = "",
        val imgAttr: String = ""
    )

    enum class SearchEngine(
        val value: Int,
        val valueName: String,
        val url: String,
        val pageNo: String,
        val tag: JSoupTag
    ) {
        ENGINE_NAVER(
            0,
            "Naver",
            PAGE_URL_NAVER,
            PAGE_NAVER_TAG,
            JSoupTag(
                item = "div.group_news",
                item2 = "li.bx",
                date = "span.info",
                title = "a.news_tit",
                link = "a.news_tit",
                corp = "div.info_group",
                img = "a.dsc_thumb",
                imgAttr = "src"
            )
        ),
        ENGINE_GOOGLE(
            1,
            "Google",
            PAGE_URL_GOOGLE,
            PAGE_GOOGLE_TAG,
            JSoupTag(
                item = "item",
                date = "pubDate",
                title = "title",
                link = "link",
                corp = "source",
                img = "media|thumbnail",
                imgAttr = "url"
            )
        ),
        ENGINE_DAUM(
            2,
            "Daum",
            PAGE_URL_DAUM,
            PAGE_DAUM_TAG,
            JSoupTag(
                item = "div.wrap_cont",
                item2 = "div.wrap_thumb",
                date = "span.cont_info",
                title = "p.desc",
                link = "a",
                corp = "span.cont_info",
                img = "img",
                imgAttr = "src"
            )
        )
    }

    const val ARTICLE_URL = 0
    const val STOCK_URL = 1

    const val STOCK_PRICE_PLUS_WORD = "플러스"
    const val STOCK_PRICE_MINUS_WORD = "마이너스"

    const val HTTP_CRAWLING_TIMEOUT_MILLIS = 8000L
    const val JOSUP_CRAWLING_TRY_CNT = 500

    const val REQUEST_INTERNET_PERMISSION = 10

    //Settings
    const val ARTICLE_DEFAULT_NUM = 15
    const val ARTICLE_MAX_NUM = 50
    const val CRAWLING_PERIOD_HOUR = 2   //2시간마다
    const val JSOUP_PARSING_DATA_MAX_NUM = 10

    const val DEFAULT_URL: String =
        "http://127.0.0.1:8080"//http://3.35.40.166:8080"//"http://192.168.219.111:8080"//"http://10.0.2.2:8080"//

    const val SUBJECT_VIEW = 1
    const val ARTICLE_VIEW = 2

    const val SERVER_FUNCTION_ENABLE = 0

    const val KEYWORD_STOCKCODE_SEPARATOR = "#"
    const val KEYWORDS_SEPARATOR = ","
    const val KEYOWRD_HINT =
        "복수의 키워드는 \",\"로 구분하고, 주식종목코드는 \"#\"을 붙여주세요. (예.삼성전자#005930,코로나19,LG#003550)"
    const val DEFAULT_KEYWORD =
        "SK이노베이션#096770,현대차#005380,SK#034730,하이트진로#000080,LG에너지솔루션#373220,카카오#035720,원익피앤이#131390,코스모신소재#005070,신성델타테크#065350"

    //STOCK RECYCLER VIEW LAYOUT
    const val TOPIC_STOCK_SMALL_TEXT_SIZE = 8.0f //주식 가격 보정

    const val TOPIC_LAYOUT_HEIGHT  =   225 //Topic Name, Stock Price, Stock Num 이 포함된 높이
    const val TOPIC_MIN_LAYOUT_HEIGHT  =   125 //Topic Name, Stock Num 이 포함된 높이
    const val TOPIC_MIN_LAYOUT_HEIGHT_SMALL_SIZE  =   175 //Topic Name, Stock Num 이 포함된 높이

    const val TOPIC_STOCK_LAYOUT_HEIGHT  =   100 //Stock Price 의 높이
    const val TOPIC_STOCK_LAYOUT_HEIGHT_SMALL_SIZE  =   100 //Stock Price 의 높이

    const val TOPIC_STOCK_MARGIN_LEFT_RIGHT  =   23
}