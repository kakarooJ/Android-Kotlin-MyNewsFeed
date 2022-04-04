package com.kakaroo.mynewsfeed

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kakaroo.mynewsfeed.adapter.TopicAdapter
import com.kakaroo.mynewsfeed.databinding.ActivityMainBinding
import com.kakaroo.mynewsfeed.entity.Article
import com.kakaroo.mynewsfeed.entity.StockCode
import com.kakaroo.mynewsfeed.entity.Topic
import com.kakaroo.mynewsfeed.utility.Common
import com.kakaroo.mynewsfeed.utility.MyUtility
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.io.IOException
import java.lang.Integer.parseInt

class MainActivity : AppCompatActivity() {

    init{
        instance = this
    }
    companion object{
        private var instance:MainActivity? = null
        fun getInstance(): MainActivity? {
            return instance
        }
    }

    //전역변수로 binding 객체선언
    private var mBinding: ActivityMainBinding? = null
    // 매번 null 체크를 할 필요 없이 편의성을 위해 바인딩 변수 재 선언
    private val binding get() = mBinding!!

    var mTopicList: ArrayList<Topic> = ArrayList<Topic>()

    var mAdapter = TopicAdapter(this, mTopicList)
    lateinit var mRecyclerView: RecyclerView
    lateinit var mLayoutManager: RecyclerView.LayoutManager
    lateinit var mPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mRecyclerView = binding.recyclerView
                // use a linear layout manager
                mLayoutManager = LinearLayoutManager(this)
        mRecyclerView.layoutManager = mLayoutManager
        binding.recyclerView.adapter = mAdapter

        mPref = PreferenceManager.getDefaultSharedPreferences(this.applicationContext)

        checkInternetPermissions()

        registerListener()

        // editText에서 완료 클릭 시
        binding.etKeyword.setOnEditorActionListener  { v, actionId, event ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.btSearch.performClick()
                handled = true
            }
            handled
        }
    }

    override fun onDestroy() {
        Log.e(Common.MY_TAG, "onDestroy called")
        super.onDestroy()
    }

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.setting -> {
                val nextIntent = Intent(this, SettingsActivity::class.java)
                startActivity(nextIntent)
            }
            R.id.reset_list -> {
                mTopicList.clear()
                mAdapter.notifyDataSetChanged()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun registerListener() {
        binding.btSearch.setOnClickListener(ButtonListener())
    }

    inner class ButtonListener : View.OnClickListener {
        override fun onClick(v: View?) {
            if (v != null) {
                when(v.id) {
                    R.id.bt_search -> {
                        if(!MyUtility().isNetworkConnected(applicationContext)) {
                            Log.i(Common.MY_TAG, "인터넷이 연결되어 있지 않습니다.")
                            Toast.makeText(applicationContext, "인터넷이 연결되어 있지 않습니다.", Toast.LENGTH_SHORT).show()
                            return
                        }

                        //종목명과 종목코드 구하기
                        var bManualInput = false
                        val keyWordList: ArrayList<StockCode> = ArrayList()
                        if(binding.etKeyword.text.isNotEmpty()) {   //Editor가 비어 있지 않으면, 맨 위에 기사 추가
                            keyWordList.add(getStockCodeFrom(binding.etKeyword.text.toString()))
                            bManualInput = true
                        } else {    //Editor가 비어 있으면, 설정에서 다시 읽어오기
                            mTopicList.clear()  //새로 갱신
                            for(keyword in getKeywordFromPref()) {
                                keyWordList.add(getStockCodeFrom(keyword))
                            }
                        }

                        if(keyWordList.size == 0) {
                            Log.d(Common.MY_TAG, "입력된 키워드가 없습니다.")
                            Toast.makeText(applicationContext, "키워드를 입력하시거나 설정에서 키워드를 저장해 주세요.",
                                Toast.LENGTH_SHORT).show()
                            return
                        }

                        hideKeyboard()  //키보드를 내린다.
                        //binding.etKeyword.setText("")   //Editor를 지운다.

                        var bReverse = true //최신 기사가 가장 위로 올라오게 하기 위해
                        if(!bManualInput) { //Editor가 비어 있어 설정값에서 값을 가져오는 경우
                            bReverse = mPref.getBoolean("result_order_key", false)
                        }
                        val articleMaxCnt: Int = parseInt(mPref.getString("keyword_maxnum_key", Common.ARTICLE_MAX_NUM.toString()))
                        val engineType: Int = parseInt(mPref.getString("engine_key", Common.SearchEngine.ENGINE_NAVER.value.toString()))

                        binding.btSearch.isEnabled = false

                        var asyncTryCnt = 0
                        for( (idx, keyWord) in keyWordList.withIndex()) {
                                
                                var url = getSearchEngine(engineType).url

                                asyncTryCnt++
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        withTimeout(Common.HTTP_CRAWLING_TIMEOUT_MILLIS) {
                                            //Log.e(Common.MY_TAG, "Force to stop due to Coroutine's Timeout")
                                            val coroutine = async {
                                                executeCrawling(url, keyWord.stocks, keyWord.code, engineType, articleMaxCnt) }

                                            val result = coroutine.await()
                                            //Log.d(Common.MY_TAG, "asyncTryCnt[$asyncTryCnt], keyWord.stocks[${keyWord.stocks}")

                                            asyncTryCnt--
                                            val topic = Topic(idx, keyWord.stocks, keyWord.code, "", result.first)

                                            if(bReverse) {
                                                mTopicList.add(0, topic)    //맨 앞으로 추가
                                                mTopicList[0].code = keyWord.code
                                                mTopicList[0].price = result.second
                                            } else {
                                                mTopicList.add(topic)
                                                mTopicList[mTopicList.size-1].code = keyWord.code
                                                mTopicList[mTopicList.size-1].price = result.second
                                            }

                                            if(!bManualInput && asyncTryCnt == 0) {
                                                mTopicList.sortWith(compareBy<Topic> {it.idx})
                                            }

                                            withContext(Dispatchers.Main) {
                                                updateTextView(getSearchEngine(engineType).valueName)
                                                mAdapter.notifyDataSetChanged()

                                                if(asyncTryCnt == 0) {
                                                    Log.d(Common.MY_TAG, "CoroutineScope is completed")
                                                    binding.btSearch.isEnabled = true

                                                    if (mTopicList.isEmpty()) {
                                                        Toast.makeText(
                                                            applicationContext,
                                                            "기사가 없습니다.!!",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                            }
                                        }
                                    } catch(te: TimeoutCancellationException) {
                                        Log.e(Common.MY_TAG, "Timetout!!! - asyncTryCnt[$asyncTryCnt]")
                                        withContext(Dispatchers.Main) {
                                            binding.btSearch.isEnabled = true

                                            Toast.makeText(
                                                applicationContext,
                                                "시간 초과",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                            }
                                /*
                                val jsoupAsyncTask =
                                    JSoupParser(url, strKeyWord, type, engineType, articleMaxCnt, object : onPostExecuteListener {
                                        override fun onPostExecute(result: ArrayList<Article>, price: String) {
                                            //mTopicList.clear()
                                            var topic: Topic
                                            if(type == Common.ARTICLE_URL) {
                                                topic = Topic(mTopicList.size, keyWord.stocks, "", "", result)
                                                if(bReverse) {
                                                    mTopicList.add(0, topic)    //맨 앞으로 추가
                                                } else {
                                                    mTopicList.add(topic)
                                                }
                                                updateTextView(keyWord.stocks, result.size)
                                            } else if(type == Common.STOCK_URL) {
                                                if(bReverse) {
                                                    mTopicList[0].code = keyWord.code
                                                    mTopicList[0].price = price
                                                } else {
                                                    if(mTopicList.size > 0) {
                                                        mTopicList[mTopicList.size-1].code = keyWord.code
                                                        mTopicList[mTopicList.size-1].price = price
                                                    }
                                                }
                                            }
                                            mAdapter.notifyDataSetChanged()

                                            runOnUiThread {
                                                if (mTopicList.isEmpty()) {
                                                    Toast.makeText(
                                                        applicationContext,
                                                        "기사가 없습니다.!!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        }
                                    })
                                jsoupAsyncTask.execute()*/
                            //}   //end of for(type in Common.ARTICLE_URL..Common.STOCK_URL) {
                        }
                    }
                }
            }
        }
    }

    private fun executeCrawling(
        url: String, keyWord: String,
        stockCode: String, engineType: Int, maxCnt: Int
    ): Pair<ArrayList<Article>, String> {

        var mList: ArrayList<Article> = ArrayList<Article>()
        var mPrice = ""

        try {
            val doc: Document = Jsoup.connect(url + keyWord)
                .ignoreContentType(true)
                .get()

            val contentElements: Elements = doc.select("item")
            for ((i, elem) in contentElements.withIndex()) {
                if (i == maxCnt) {   //i는 1부터
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
            //stock code가 있으면
            if (stockCode.isNotEmpty()) {
                val doc2: Document = Jsoup.connect(Common.STOCK_URL_NAVER + stockCode)
                    .ignoreContentType(true)
                    .get()

                val contentElements2: Elements = doc2.select(".new_totalinfo dl")
                if (contentElements2.isNotEmpty()) {
                    val element1: Element =
                        contentElements2[0].select(".blind dd")[1]   //종목명 삼성전자
                    val element2: Element = contentElements2[0].select(".blind dd")[3]
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

        return Pair(mList, mPrice)
    }

    private fun printList() {
        if(mTopicList.size != 0) {
            Log.i(Common.MY_TAG, mTopicList.toString())
        }
    }

    private fun updateTextView(searchEngine: String) {
        binding.tvResult.text = "${searchEngine} 검색결과 입니다."
    }
    private fun updateTextView(key: String, size: Int) {
        binding.tvResult.text = "${key}에 관련된 ${size}개의 기사를 찾았습니다."
    }

    private fun checkInternetPermissions() {
        val permissionResult = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.INTERNET)
        if(permissionResult != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(applicationContext, "인터넷 권한이 없습니다.!!", Toast.LENGTH_SHORT).show()
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.INTERNET), Common.REQUEST_INTERNET_PERMISSION)
        }
    }
    override fun onRequestPermissionsResult( requestCode: Int, permissions: Array<out String>, grantResults: IntArray ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode === Common.REQUEST_INTERNET_PERMISSION) {
            if (grantResults.isNotEmpty()) {
                for (grant in grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED)
                        System.exit(0)
                }
            }
        }
    }

    interface onPostExecuteListener {
        fun onPostExecute(result: ArrayList<Article>, price: String)
    }

    private fun getStockCodeFrom(str: String) : StockCode {
        val stockCode: StockCode = StockCode("", "")

        val strList = str.split("#")
        stockCode.stocks = strList[0]
        if(strList.size >= 2) {
            stockCode.code = strList[1]
        }
        return stockCode
    }

    private fun getKeywordFromPref() : ArrayList<String> {
        var strList: ArrayList<String> = ArrayList()
        val keyword = mPref.getString("keyword_key", "")
        if (keyword != null) {
            if(keyword.isEmpty()) {
                //strList.add(Common.DEFAULT_PAGE_KEYWORD)
            } else {
                val keywordList = keyword.split(",")
                for(item in keywordList) {
                    strList.add(item)
                }
            }
        } else {
            strList.add(Common.DEFAULT_PAGE_KEYWORD)
        }

        return strList
    }

    private fun getSearchEngine(engineType: Int) = Common.SearchEngine.values()[engineType]
}
