package com.kakaroo.mynewsfeed.html

import android.os.AsyncTask
import android.util.Log
import com.kakaroo.mynewsfeed.Common
import com.kakaroo.mynewsfeed.MainActivity
import com.kakaroo.mynewsfeed.entity.Article
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements


//TODO : Coroutine
class JSoupParser(val url: String, val callback: MainActivity.onPostExecuteListener): AsyncTask<Void, Void, Void>() {

    var mList: ArrayList<Article> = ArrayList<Article>()

    override fun doInBackground(vararg params: Void?): Void? {
        Log.i(Common.MY_TAG, "URL: $url")

        //val list: ArrayList<Article> = ArrayList()
        val doc: Document = Jsoup.connect(url).get()
        val contentElements: Elements = doc.select("item")
        for ((i, elem) in contentElements.withIndex()) {
            val date = elem.select("pubDate").text()
            val title = elem.select("title").text()
            val link = elem.select("link").text()
            Log.d(Common.MY_TAG, "------------------- $i ---------------")
            Log.d(Common.MY_TAG, date)
            Log.d(Common.MY_TAG, title)
            Log.d(Common.MY_TAG, link)
            mList.add(Article(i, date, title, link))
        }
        return null
    }

    override fun onPostExecute(result: Void?) {
        callback.onPostExecute(mList)
    }
}