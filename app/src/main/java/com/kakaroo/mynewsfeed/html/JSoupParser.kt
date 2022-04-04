package com.kakaroo.mynewsfeed.html

import android.os.AsyncTask
import android.util.Log
import com.kakaroo.mynewsfeed.utility.Common
import com.kakaroo.mynewsfeed.MainActivity
import com.kakaroo.mynewsfeed.entity.Article
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.io.IOException
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONArray

//TODO : Coroutine
class JSoupParser(private val url: String, private val keyWord: String, private val urlType: Int, private val engineType: Int, private val maxCnt: Int, private val callback: MainActivity.onPostExecuteListener): AsyncTask<Void, Void, Void>() {

    var mList: ArrayList<Article> = ArrayList<Article>()
    var mPrice: String = ""

    override fun onPreExecute() {
        super.onPreExecute()
    }

    override fun doInBackground(vararg params: Void?): Void? {
        val clientId = "D6wAL9mAD4z5NAkitFWc" //애플리케이션 클라이언트 아이디값"
        val clientSecret = "20oYsPzDBm" //애플리케이션 클라이언트 시크릿값"


        try {
            val fullUrl = url + keyWord// + Common.PAGE_URL_START + "1"
            val doc: Document = Jsoup.connect(url+keyWord)
                .ignoreContentType(true)
                .get()

            Log.i(Common.MY_TAG, doc.toString())

            if(urlType == Common.ARTICLE_URL) {
                /*try {
                    //var body = doc.select("body").text()
                    //body = "{\"items\": [ { \"title\": \"대기업 중고차 매매 시장 진출… 소비자 찬성, 업계는 반대 팽팽\", \"originallink\": \"http://www.kyeongin.com/main/view.php?key=20220318010003738\", \"link\": \"http://www.kyeongin.com/main/view.php?key=20220318010003738\", \"description\": \"현대차는 출고 5년·10만 이내 자사 중고차만 판매하겠다는 계획인데, 해당 차량이 현대·기아 중고차... 수원지역의 한 중고차 매매상사 대표는 \"현대차가 판매하겠다는 중고차는 고장 한계가 아주 적은 차량이다.... \", \"pubDate\": \"Sat, 19 Mar 2022 10:32:00 +0900\" }]}\n"
                    //body = body.replace("'", "")
                    //body = body.replace("‘", "")
                    //body = body.replace("’", "")
                    while(true) {
                        val idx = body.indexOf("\"\"")
                        if(idx == -1) {
                            break
                        }
                        val idx2 = body.indexOf("\"", idx+2)
                        body = body.removeRange(idx2, idx2+1)
                        body = body.removeRange(idx, idx+1)
                    }

                    val jsonObject = JSONObject(body)

                    val jArray: JSONArray =
                        JSONObject(body).getJSONArray("items")
                    for (i in 0 until jArray.length()) {
                        val obj = jArray.getJSONObject(i)

                        var date = (obj.getString("pubDate"))
                        date = date.substring(0, date.lastIndexOf(" "))
                        val title = (obj.getString("title"))
                        val link = (obj.getString("link"))
                        val newsCorp = ""//(obj.getString("source"))
                        mList.add(Article(i, date, title, link, "", newsCorp))
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }*/
                val contentElements: Elements = doc.select("item")
                for ((i, elem) in contentElements.withIndex()) {
                    if(i == maxCnt) {   //i는 1부터
                        break
                    }
                    var date = elem.select("pubDate").text()
                    date = date.substring(0, date.lastIndexOf(" "))
                    val title = elem.select("title").text()
                    val link = elem.select("link").text()
                    //<media:thumbnail url="https://imgn...."/>
                    val thumbImg = elem.select("media|thumbnail").attr("url")
                    val newsCorp = elem.select("source").text()
                    mList.add(Article(i, date, title, link, thumbImg, newsCorp))
                }
            } else if(urlType == Common.STOCK_URL) {
                val contentElements: Elements = doc.select(".new_totalinfo dl")
                if(contentElements.isNotEmpty()) {
                    val element1: Element = contentElements[0].select(".blind dd")[1]   //종목명 삼성전자
                    val element2: Element = contentElements[0].select(".blind dd")[3]
                    //Log.d(Common.MY_TAG, element1.text() + " " + element2.text())
                    val stocks: String = element1.text().split(" ")[1]
                    mPrice = stocks + " : " + element2.text()//toString()
                }
            }
        } catch (e: IOException) {
            // HttpUrlConnection will throw an IOException if any 4XX
            // response is sent. If we request the status again, this
            // time the internal status will be properly set, and we'll be
            // able to retrieve it.
            Log.e(Common.MY_TAG, "Jsoup connection has error: $e")
        }

        return null
    }

    override fun onPostExecute(result: Void?) {
        callback.onPostExecute(mList, mPrice)
    }
}