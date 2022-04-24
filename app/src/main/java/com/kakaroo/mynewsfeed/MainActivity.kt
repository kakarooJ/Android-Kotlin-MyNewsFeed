package com.kakaroo.mynewsfeed

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.kakaroo.mynewsfeed.adapter.KeywordAdapter
import com.kakaroo.mynewsfeed.adapter.TopicAdapter
import com.kakaroo.mynewsfeed.databinding.ActivityMainBinding
import com.kakaroo.mynewsfeed.entity.Article
import com.kakaroo.mynewsfeed.entity.KeyWord
import com.kakaroo.mynewsfeed.entity.StockInfo
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
import android.graphics.BitmapFactory

import android.graphics.Bitmap
import android.net.Uri
import android.widget.TextView
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    //전체 공유를 위해
    init {
        instance = this
    }

    companion object {
        private var instance: MainActivity? = null
        fun getInstance(): MainActivity? {
            return instance
        }
    }

    //전역변수로 binding 객체선언
    private var mBinding: ActivityMainBinding? = null

    // 매번 null 체크를 할 필요 없이 편의성을 위해 바인딩 변수 재 선언
    private val binding get() = mBinding!!

    lateinit var mPref: SharedPreferences

    lateinit var mDialog : Dialog

    var mTopicList: ArrayList<Topic> = ArrayList<Topic>()
    var mKeyWordList: ArrayList<KeyWord> = ArrayList<KeyWord>()

    var mTopicAdapter = TopicAdapter(this, mTopicList)
    var mKeyWordAdapter = KeywordAdapter(this, mKeyWordList)

    lateinit var mTopicRecyclerView: RecyclerView
    lateinit var mKeywordRecyclerView: RecyclerView

    lateinit var mTopicLayoutManager: RecyclerView.LayoutManager
    lateinit var mKeywordLayoutManager: RecyclerView.LayoutManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mTopicRecyclerView = binding.rvTopics
        mTopicLayoutManager = LinearLayoutManager(this)
        mTopicRecyclerView.layoutManager = mTopicLayoutManager
        binding.rvTopics.adapter = mTopicAdapter

        mPref = PreferenceManager.getDefaultSharedPreferences(this.applicationContext)

        mDialog = Dialog(this)
        mDialog.setContentView(androidx.preference.R.layout.custom_dialog)

        mKeywordRecyclerView = binding.rvKeywords
        //horizontal recycler view
        mKeywordLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mKeywordRecyclerView.layoutManager = mKeywordLayoutManager
        binding.rvKeywords.adapter = mKeyWordAdapter

        checkInternetPermissions()

        registerListener()
        initializeKeyWordList()

        // editText에서 완료 클릭 시
        binding.etKeyword.setOnEditorActionListener { v, actionId, event ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.btSearch.performClick()
                handled = true
            }
            handled
        }
    }

    private fun showTerribleBackground() {
        binding.ivBackground.setImageDrawable(getResources().getDrawable(R.drawable.error_background))

        thread(start = true) {
            Thread.sleep(200)
            runOnUiThread{
                binding.ivBackground.setImageDrawable(getResources().getDrawable(R.drawable.background_img))
            }
        }
    }

    private fun initializeKeyWordList() {
        val keyWord = mPref.getString("keyword_key", "") ?: ""
        if (keyWord.isNullOrEmpty()) {
            mPref.edit().putString("keyword_key", Common.DEFAULT_KEYWORD).apply()
        }
    }

    private fun setKeyWordListFromPref() {
        mKeyWordList.clear()
        for (keyword in getKeyWordFromPref()) {
            mKeyWordList.add(getKeyWordFrom(keyword))
        }
    }

    fun setKeyWordPrefFromList(keyWordList: ArrayList<KeyWord>) {
        val keyWordsStr = keyWordList.joinToString(Common.KEYWORDS_SEPARATOR) {
            it.keyWord + Common.KEYWORD_STOCKCODE_SEPARATOR + it.stockCode
        }
        mPref.edit().putString("keyword_key", keyWordsStr).apply()

        setKeyWordListFromPref()
    }

    override fun onResume() {
        setKeyWordListFromPref()
        mKeyWordAdapter.notifyDataSetChanged()
        setTopicRecyclerBackground()
        super.onResume()
    }

    override fun onBackPressed() {
        Log.d(Common.MY_TAG, "onBackPressed called")
        if (mKeyWordAdapter.mDeleteMode) {
            mKeyWordAdapter.mDeleteMode = false
            mKeyWordAdapter.notifyDataSetChanged()
            return
        }
        super.onBackPressed()

        //task 완전 종료
        if (Build.VERSION.SDK_INT >= 21)
            finishAndRemoveTask()
        else
            finish()
    }

    override fun onDestroy() {
        Log.d(Common.MY_TAG, "onDestroy called")
        super.onDestroy()
    }

    private fun hideKeyboard() {
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.setting -> {
                mKeyWordAdapter.mDeleteMode = false
                val nextIntent = Intent(this, SettingsActivity::class.java)
                startActivity(nextIntent)
            }
            R.id.reset_list -> {
                mKeyWordAdapter.mDeleteMode = false
                mKeyWordAdapter.notifyDataSetChanged()
                mTopicList.clear()
                mTopicAdapter.notifyDataSetChanged()
                binding.tvResult.text = ""
                setTopicRecyclerBackground()

                //showTerribleBackground()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setTopicRecyclerBackground() {
        if(mTopicList.isNullOrEmpty()) {
            binding.rvTopics.background = null
        } else {
            binding.rvTopics.setBackgroundResource(R.drawable.main_recycler_background)
        }
    }

    private fun recyclerScrollToPosition(position: Int) {
        binding.rvTopics.scrollToPosition(position)
    }

    private fun registerListener() {
        binding.btSearch.setOnClickListener(ButtonListener())
    }

    inner class ButtonListener : View.OnClickListener {
        override fun onClick(v: View?) {
            if (v != null) {
                when (v.id) {
                    R.id.bt_search -> {
                        if (!MyUtility().isNetworkConnected(applicationContext)) {
                            Log.i(Common.MY_TAG, "인터넷이 연결되어 있지 않습니다.")
                            Toast.makeText(
                                applicationContext,
                                "인터넷이 연결되어 있지 않습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                            return
                        }
                        fetchNews(binding.etKeyword.text.toString())
                    }
                }
            }
        }
    }   //end of inner class

    fun fetchNews(topic: String) {
        val searchList: ArrayList<KeyWord> = ArrayList<KeyWord>()
        //종목명과 종목코드 구하기
        var bManualInput = false
        if (topic.isNotEmpty()) {   //Editor가 비어 있지 않으면, 맨 위에 기사 추가
            val keyWord: KeyWord = getKeyWordFrom(topic)
            searchList.add(keyWord)
            if (!mKeyWordList.contains(keyWord)) {   //기존 리스트에 값이 없으면 저장
                saveKeyWordToPref(keyWord)
                mKeyWordList.add(0, keyWord)
                mKeyWordAdapter.notifyDataSetChanged()
            }
            bManualInput = true
        } else {    //Editor가 비어 있으면, 전체 리스트
            mTopicList.clear()  //새로 갱신
            searchList.addAll(mKeyWordList)
//            for (keyword in getKeyWordFromPref()) {
//                mKeyWordList.add(getKeyWordFrom(keyword))
//            }
        }

        if (searchList.size == 0) {
            Log.d(Common.MY_TAG, "입력된 키워드가 없습니다.")
            Toast.makeText(
                applicationContext, "키워드를 입력하시거나 설정에서 키워드를 저장해 주세요.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        hideKeyboard()  //키보드를 내린다.
        binding.etKeyword.setText("")   //Editor를 지운다.
        binding.etKeyword.clearFocus()

        var bReverse = true //최신 기사가 가장 위로 올라오게 하기 위해
        if (!bManualInput) { //Editor가 비어 있어 설정값에서 값을 가져오는 경우
            bReverse = mPref.getBoolean("result_order_key", false)
        }
        val articleMaxCnt: Int =
            parseInt(mPref.getString("keyword_maxnum_key", Common.ARTICLE_MAX_NUM.toString()))
        val engineType: Int = parseInt(
            mPref.getString(
                "engine_key",
                Common.SearchEngine.ENGINE_NAVER.value.toString()
            )
        )

        binding.btSearch.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        var asyncTryCnt = 0

        for ((idx, keyWord) in searchList.withIndex()) {

            var url = getSearchEngine(engineType).url

            asyncTryCnt++
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    withTimeout(Common.HTTP_CRAWLING_TIMEOUT_MILLIS) {
                        //Log.e(Common.MY_TAG, "Force to stop due to Coroutine's Timeout")
                        val coroutine = async {
                            executeCrawling(
                                url,
                                keyWord.keyWord,
                                keyWord.stockCode,
                                engineType,
                                articleMaxCnt
                            )
                        }

                        val result = coroutine.await()
                        //Log.d(Common.MY_TAG, "asyncTryCnt[$asyncTryCnt], keyWord.stocks[${keyWord.stocks}")

                        asyncTryCnt--
                        val topic = Topic(
                            idx = idx,
                            title = keyWord.keyWord,
                            code = keyWord.stockCode,
                            price= result.second.price,
                            chartUrl = result.second.chartUrl,
                            articles = result.first
                        )

                        if (bReverse) {
                            mTopicList.add(0, topic)    //맨 앞으로 추가
                            //mTopicList[0].code = keyWord.stockCode
                            //mTopicList[0].price = result.second.price
                            //mTopicList[0].chartUrl = result.second.chartUrl
                        } else {
                            mTopicList.add(topic)
                            //mTopicList[mTopicList.size - 1].code = keyWord.stockCode
                            //mTopicList[mTopicList.size - 1].price = result.second.price
                            //mTopicList[mTopicList.size - 1].chartUrl = result.second.chartUrl
                        }

                        if (!bManualInput && asyncTryCnt == 0) {
                            mTopicList.sortWith(compareBy<Topic> { it.idx })
                        }

                        withContext(Dispatchers.Main) {
                            updateTextView(getSearchEngine(engineType).valueName)
                            //mTopicAdapter.notifyDataSetChanged()
                            binding.progressBar.visibility = View.GONE

                            if (asyncTryCnt == 0) {
                                Log.d(Common.MY_TAG, "CoroutineScope is completed")
                                binding.btSearch.isEnabled = true
                                mTopicAdapter.notifyDataSetChanged()
                                setTopicRecyclerBackground()
                                recyclerScrollToPosition(0)


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
                } catch (te: TimeoutCancellationException) {
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
                        mTopicAdapter.notifyDataSetChanged()

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

    private fun executeCrawling(
        url: String, keyWord: String,
        stockKeyWord: String, engineType: Int, maxCnt: Int
    ): Pair<ArrayList<Article>, StockInfo> {

        var mList: ArrayList<Article> = ArrayList<Article>()
        var mPrice = ""
        var mChartUrl = ""

        do {
            var pageIndex = 1
            try {
                var pageUri = Common.SearchEngine.values()[engineType].pageNo
                if (engineType == Common.SearchEngine.ENGINE_NAVER.value) {
                    pageUri + (mList.size + 1).toString()   //start=1,2,11...
                } else if (engineType == Common.SearchEngine.ENGINE_DAUM.value) {
                    pageUri + pageIndex.toString()  //&p=1,2,3...
                    pageIndex++
                }

                val doc: Document = Jsoup.connect(url + keyWord + pageUri)
                    .ignoreContentType(true)
                    .get()

                if (engineType == Common.SearchEngine.ENGINE_GOOGLE.value) {
                    val contentElements: Elements =
                        doc.select(Common.SearchEngine.values()[engineType].tag.item)
                    for ((i, elem) in contentElements.withIndex()) {
                        if (i + mList.size > maxCnt) {   //i는 1부터
                            break
                        }
                        var date =
                            elem.select(Common.SearchEngine.values()[engineType].tag.date).text()
                        date = date.substring(0, date.lastIndexOf(" "))
                        val title =
                            elem.select(Common.SearchEngine.values()[engineType].tag.title).text()
                        val link =
                            elem.select(Common.SearchEngine.values()[engineType].tag.link).text()
                        //<media:thumbnail url="https://imgn...."/>
                        val thumbImg = elem.select(Common.SearchEngine.values()[engineType].tag.img)
                            .attr(Common.SearchEngine.values()[engineType].tag.imgAttr)   //<media:thumbnail url=".....jpg"/>
                        val newsCorp =
                            elem.select(Common.SearchEngine.values()[engineType].tag.corp).text()
                        mList.add(
                            Article(
                                idx = i,
                                date = date,
                                title = title,
                                url = link,
                                imgUrl = thumbImg,
                                newsCorp = newsCorp
                            )
                        )
                    }
                } else if (engineType == Common.SearchEngine.ENGINE_NAVER.value) {
                    val headElement: Elements =
                        doc.select(Common.SearchEngine.values()[engineType].tag.item)

                    val contentElements: Elements =
                        headElement.select(Common.SearchEngine.values()[engineType].tag.item2)

                    for ((i, elem) in contentElements.withIndex()) {
                        if (i + mList.size > maxCnt) {   //i는 0부터
                            break
                        }
                        val date =
                            elem.select(Common.SearchEngine.values()[engineType].tag.date).text()
                        val title =
                            elem.select(Common.SearchEngine.values()[engineType].tag.title).text()
                        val link =
                            elem.select(Common.SearchEngine.values()[engineType].tag.link).first()
                                .attr(Common.JSOUP_LINK_POSTFIX_TAG)
                        val newsCorp =
                            elem.select(Common.SearchEngine.values()[engineType].tag.corp)
                                .select(Common.JSOUP_LINK_TAG).text().split(" ")[0]
                        var thumbImg = elem.select(Common.SearchEngine.values()[engineType].tag.img)
                            .select(Common.JSOUP_IMG_TAG)
                            .attr(Common.SearchEngine.values()[engineType].tag.imgAttr)
                        mList.add(
                            Article(
                                idx = i,
                                date = date,
                                title = title,
                                url = link,
                                imgUrl = thumbImg,
                                newsCorp = newsCorp
                            )
                        )
                    }
                } else {
                    val contentElements: Elements =
                        doc.select(Common.SearchEngine.values()[engineType].tag.item)
                    val contentElements2: Elements =
                        doc.select(Common.SearchEngine.values()[engineType].tag.item2) //img
                    for ((i, elem) in contentElements.withIndex()) {
                        if (i + mList.size > maxCnt) {   //i는 0부터
                            break
                        }
                        val dateCorp =
                            elem.select(Common.SearchEngine.values()[engineType].tag.date).text()
                        val date = dateCorp.split(" ").getOrNull(1) ?: ""
                        val newsCorp = dateCorp.split(" ").getOrNull(0) ?: ""
                        //date = date.substring(date.lastIndexOf(" ")+1, date.length)
                        val title =
                            elem.select(Common.SearchEngine.values()[engineType].tag.title).text()
                        val link =
                            elem.select(Common.SearchEngine.values()[engineType].tag.link).first()
                                .attr("href")
                        //val newsCorp =
                        //    elem.select(Common.SearchEngine.values()[engineType].tag.corp).text()

                        var thumbImg = ""
                        if (i < contentElements2.size) {
                            val elem2 = contentElements2[i]
                            thumbImg =
                                if (elem2 != null) elem2.select(Common.SearchEngine.values()[engineType].tag.img)
                                    .attr(Common.SearchEngine.values()[engineType].tag.imgAttr)   //<media:thumbnail url=".....jpg"/>
                                else ""
                        }
                        mList.add(
                            Article(
                                idx = i,
                                date = date,
                                title = title,
                                url = link,
                                imgUrl = thumbImg,
                                newsCorp = newsCorp
                            )
                        )
                    }
                }

                //stock code가 있으면
                if (stockKeyWord.isNotEmpty() && mPrice.isEmpty()) {
                    val doc2: Document = Jsoup.connect(Common.STOCK_URL_NAVER + stockKeyWord)
                        .ignoreContentType(true)
                        .get()

                    val contentElements: Elements = doc2.select("#img_chart_area")
                    if (contentElements.isNotEmpty()) {
                        mChartUrl = contentElements.attr("src")
                    }
                    val contentElements2: Elements = doc2.select(".new_totalinfo dl")
                    if (contentElements2.isNotEmpty()) {
                        val element1: Element =
                            contentElements2[0].select(".blind dd")[1]   //종목명 삼성전자
                        val element2: Element = contentElements2[0].select(".blind dd")[3]
                        //Log.d(Common.MY_TAG, element1.text() + " " + element2.text())
                        val stocks: String = element1.text().split(" ")[1]
                        mPrice = stocks + " : " + element2.text()//toString()
                        val index = mPrice.indexOf("전일대비")
                        mPrice = mPrice.substring(startIndex = 0, endIndex = index) + "\n" + mPrice.substring(startIndex = index)
                    }
                }

            } catch (e: IOException) {
                // HttpUrlConnection will throw an IOException if any 4XX
                // response is sent. If we request the status again, this
                // time the internal status will be properly set, and we'll be
                // able to retrieve it.
                Log.e(Common.MY_TAG, "Jsoup connection has error: $e")
            }
        } while (mList.size < maxCnt)

        return Pair(mList, StockInfo(mPrice, mChartUrl))
    }

    private fun printList() {
        if (mTopicList.size != 0) {
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
        val permissionResult =
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.INTERNET)
        if (permissionResult != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(applicationContext, "인터넷 권한이 없습니다.!!", Toast.LENGTH_SHORT).show()
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.INTERNET), Common.REQUEST_INTERNET_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
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

    private fun getKeyWordFrom(str: String): KeyWord {
        val keyword: KeyWord = KeyWord("", "")

        val strList = str.split(Common.KEYWORD_STOCKCODE_SEPARATOR)
        keyword.keyWord = strList[0]
        if (strList.size >= 2) {
            keyword.stockCode = strList[1]
        }
        return keyword
    }

    private fun saveKeyWordToPref(keyWord: KeyWord) {
        var prefKeyWord = mPref.getString("keyword_key", "")
        var item =
            if (keyWord.stockCode.isNotEmpty()) keyWord.keyWord + Common.KEYWORD_STOCKCODE_SEPARATOR + keyWord.stockCode
            else keyWord.keyWord

        if (!prefKeyWord.isNullOrEmpty()) {
            item += Common.KEYWORDS_SEPARATOR + prefKeyWord
        }
        mPref.edit().putString("keyword_key", item).apply()
    }

    private fun getKeyWordFromPref(): ArrayList<String> {
        var strList: ArrayList<String> = ArrayList()
        val keyWord = mPref.getString("keyword_key", "") ?: ""

        //TEST
//        if(keyWord.isNullOrEmpty()) {
//            keyWord = Common.DEFAULT_KEYWORD
//        }
        if (keyWord.isNotEmpty()) {
            keyWord.split(Common.KEYWORDS_SEPARATOR).filter { it.isNotEmpty() }
                .map { strList.add(it) }
        }
        return strList
    }

    private fun getSearchEngine(engineType: Int) = Common.SearchEngine.values()[engineType]

    fun configureDialog(stockName: String, stockCode: String, imgUrl: String) {
        if(mDialog == null) {
            Log.e(Common.MY_TAG, "mDialog is null!! return!!")
            return
        }
        val window: Window? = mDialog.window
        if (window != null) {
            // 백그라운드 투명
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val params: WindowManager.LayoutParams = window.attributes
            // 화면에 가득 차도록
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            //params.height = WindowManager.LayoutParams.MATCH_PARENT

            // 열기&닫기 시 애니메이션 설정
            //androidx.preference.R.style.AnimationPopupStyle
            params.windowAnimations = R.style.AnimationPopupStyle
            window.attributes = params
            // UI 하단 정렬
            window.setGravity(Gravity.CENTER)
        }

        val textView: TextView = mDialog.findViewById(R.id.tv_stock_name)
        textView.text = "$stockName#$stockCode"

        textView.setOnClickListener(View.OnClickListener()  { _ ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Common.STOCK_URL_NAVER + stockCode))
            this.startActivity(intent)
        })

        //Image zoom in-out
        val imageView: SubsamplingScaleImageView = mDialog.findViewById(R.id.iv_stock_chart)
        CoroutineScope(Dispatchers.IO).launch {
            val coroutine = async {
                getBitmapFromURL(imgUrl)
            }
            val result = coroutine.await()
            withContext(Dispatchers.Main) {
                if(result != null) {
                     imageView.setImage(ImageSource.bitmap(result))
                }
            }
        }
    }

    private fun getBitmapFromURL(src: String?): Bitmap? {
        return try {
            val url = URL(src)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input: InputStream = connection.inputStream
            val myBitmap = BitmapFactory.decodeStream(input)
            myBitmap
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(Common.MY_TAG, "getBitmapFromURL error: ${e.message}")
            null
        }
    }
}
