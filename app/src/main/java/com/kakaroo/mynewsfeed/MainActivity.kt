package com.kakaroo.mynewsfeed

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_ENTER
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kakaroo.mynewsfeed.databinding.ActivityMainBinding
import com.kakaroo.mynewsfeed.entity.Article
import com.kakaroo.mynewsfeed.html.JSoupParser

class MainActivity : AppCompatActivity() {
    //전역변수로 binding 객체선언
    private var mBinding: ActivityMainBinding? = null
    // 매번 null 체크를 할 필요 없이 편의성을 위해 바인딩 변수 재 선언
    private val binding get() = mBinding!!

    var mArticleList: ArrayList<Article> = ArrayList<Article>()

    var mAdapter = RecyclerAdapter(this, mArticleList)
    lateinit var mRecyclerView: RecyclerView
    lateinit var mLayoutManager: RecyclerView.LayoutManager
    lateinit var mPref: SharedPreferences

    var keyword: String = ""

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

        mPref = getSharedPreferences(Common.SHARED_PREF_NAME, 0)

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

        mArticleList.add(Article(1, "2022-03-14 16:15","여기는 기사타이틀입니다.","http://naver.com"))

        var data: List<RecyclerAdapter.Item> = ArrayList<RecyclerAdapter.Item>()
    }

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0);
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
                        keyword = if(binding.etKeyword.text.isNotEmpty()) binding.etKeyword.text.toString()
                                else Common.DEFAULT_PAGE_KEYWORD
                        val url = Common.PAGE_URL_NAVER + keyword

                        hideKeyboard()

                        val jsoupAsyncTask = JSoupParser(url, object : onPostExecuteListener {
                            override fun onPostExecute(result: ArrayList<Article>) {
                                mArticleList.clear()
                                mArticleList.addAll(result)
                                mAdapter.notifyDataSetChanged()
                                updateTextView(keyword, result.size)

                                runOnUiThread {
                                    if(mArticleList.isEmpty()) {
                                        Toast.makeText(applicationContext, "기사가 없습니다.!!", Toast.LENGTH_SHORT).show()
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

    private fun printList() {
        if(mArticleList.size != 0) {
            Log.i(Common.MY_TAG, mArticleList.toString())
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
        } else {
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
        fun onPostExecute(result: ArrayList<Article>)
    }
}