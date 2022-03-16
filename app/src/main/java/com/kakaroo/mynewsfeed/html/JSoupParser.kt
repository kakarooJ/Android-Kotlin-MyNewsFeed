package com.kakaroo.mynewsfeed.html

import android.os.AsyncTask
import android.view.View
import android.widget.Button
import com.kakaroo.mynewsfeed.Common
import com.kakaroo.mynewsfeed.MainActivity
import com.kakaroo.mynewsfeed.entity.Article
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements


//TODO : Coroutine
class JSoupParser(private val url: String, private val type: Int, private val maxCnt: Int, private val callback: MainActivity.onPostExecuteListener): AsyncTask<Void, Void, Void>() {

    var mList: ArrayList<Article> = ArrayList<Article>()
    var mPrice: String = ""

    override fun onPreExecute() {
        super.onPreExecute()
    }

    override fun doInBackground(vararg params: Void?): Void? {
        //Log.i(Common.MY_TAG, "URL: $url")
        val doc: Document = Jsoup.connect(url).get()

        if(type == Common.ARTICLE_URL) {
            val contentElements: Elements = doc.select("item")
            for ((i, elem) in contentElements.withIndex()) {
                if(i == maxCnt) {   //i는 1부터
                    break
                }
                var date = elem.select("pubDate").text()
                date = date.substring(0, date.lastIndexOf(" "))
                val title = elem.select("title").text()
                val link = elem.select("link").text()
                mList.add(Article(i, date, title, link))
            }
        } else if(type == Common.STOCK_URL) {
            val contentElements: Elements = doc.select(".new_totalinfo dl")
            if(contentElements.isNotEmpty()) {
                val element1: Element = contentElements[0].select(".blind dd")[1]   //종목명 삼성전자
                val element2: Element = contentElements[0].select(".blind dd")[3]
                //Log.d(Common.MY_TAG, element1.text() + " " + element2.text())
                val stocks: String = element1.text().split(" ")[1]
                mPrice = stocks + " : " + element2.text()//toString()
            }
        }
        return null
    }

    override fun onPostExecute(result: Void?) {
        callback.onPostExecute(mList, mPrice)
    }
}