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
import com.kakaroo.mynewsfeed.databinding.ActivityMainBinding
import com.kakaroo.mynewsfeed.entity.Article
import com.kakaroo.mynewsfeed.entity.StockCode
import com.kakaroo.mynewsfeed.entity.Topic
import com.kakaroo.mynewsfeed.html.JSoupParser
import java.lang.Integer.parseInt

class MainActivity : AppCompatActivity() {
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

        //test code
        /*
        val article1 = Article(1, "2022-03-14 16:15","기사타이틀 1 입니다.","http://naver.com")
        val article2 = Article(2, "2022-03-15 12:24","기사타이틀 2 입니다.","http://daum.net")
        val article3 = Article(3, "2022-03-16 09:38","기사타이틀 3 입니다.","http://google.com")
        val topic = Topic(1, "SearchEngine", ArrayList<Article>())
        topic.articles.add(article1)
        topic.articles.add(article2)
        topic.articles.add(article3)
        mTopicList.add(topic)
         */
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
                        //종목명과 종목코드 구하기
                        val keywordList: ArrayList<StockCode> = ArrayList()
                        if(binding.etKeyword.text.isNotEmpty()) {   //Editor가 비어 있지 않으면, 맨 위에 기사 추가
                            keywordList.add(getStockCodeFrom(binding.etKeyword.text.toString()))
                        } else {    //Editor가 비어 있으면, 설정에서 다시 읽어오기
                            mTopicList.clear()  //새로 갱신
                            for(keyword in getKeywordFromPref()) {
                                keywordList.add(getStockCodeFrom(keyword))
                            }
                        }

                        hideKeyboard()  //키보드를 내린다.
                        binding.etKeyword.setText("")   //Editor를 지운다.

                        val bReverse: Boolean = mPref.getBoolean("result_order_key", false)
                        val articleMaxCnt: Int = parseInt(mPref.getString("keyword_maxnum_key", Common.ARTICLE_MAX_NUM.toString()))

                        for(keyword in keywordList) {
                            for(type in Common.ARTICLE_URL..Common.STOCK_URL) {
                                if(type == Common.STOCK_URL && keyword.code == "") {
                                    Log.i(Common.MY_TAG, "종목코드가 없습니다.")
                                    continue
                                }
                                
                                var url: String = if(type == Common.ARTICLE_URL) Common.PAGE_URL_NAVER + keyword.stocks
                                    else Common.STOCK_URL_NAVER +keyword.code

                                val jsoupAsyncTask =
                                    JSoupParser(url, type, articleMaxCnt, object : onPostExecuteListener {
                                        override fun onPostExecute(result: ArrayList<Article>, price: String) {
                                            //mTopicList.clear()
                                            var topic: Topic
                                            if(type == Common.ARTICLE_URL) {
                                                topic = Topic(mTopicList.size, keyword.stocks, "", "", result)
                                                if(bReverse) {
                                                    mTopicList.add(0, topic)    //맨 앞으로 추가
                                                } else {
                                                    mTopicList.add(topic)
                                                }
                                                updateTextView(keyword.stocks, result.size)
                                            } else if(type == Common.STOCK_URL) {
                                                if(bReverse) {
                                                    mTopicList[0].code = keyword.code
                                                    mTopicList[0].price = price
                                                } else {
                                                    if(mTopicList.size > 0) {
                                                        mTopicList[mTopicList.size-1].code = keyword.code
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
                                jsoupAsyncTask.execute()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun printList() {
        if(mTopicList.size != 0) {
            Log.i(Common.MY_TAG, mTopicList.toString())
        }
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
            if (grantResults.size > 0) {
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
                strList.add(Common.DEFAULT_PAGE_KEYWORD)
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
}
