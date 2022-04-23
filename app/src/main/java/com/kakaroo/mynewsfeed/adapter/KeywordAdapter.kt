package com.kakaroo.mynewsfeed.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kakaroo.mynewsfeed.MainActivity

import com.kakaroo.mynewsfeed.R
import com.kakaroo.mynewsfeed.entity.KeyWord
import com.kakaroo.mynewsfeed.utility.Common


class KeywordAdapter(private val context: Context, private val listData: ArrayList<KeyWord>?)
    : RecyclerView.Adapter<KeywordAdapter.CustomViewHolder>() {

    private val mContext: Context = context
    private var mKeyWordList: ArrayList<KeyWord>? = listData
    var mDeleteMode = false
        get() = field
    private var mPosition: Int = 0
    //private inflater: LayoutInflater = LayoutInflater.from(context)

    private var mViewItems = ArrayList<View>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.keyword_item_cardview, parent, false)

        return CustomViewHolder(view).apply {}
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val keyWord: KeyWord? = listData?.get(position)
        mPosition = position
        if (keyWord != null) {
            holder.setItem(keyWord)
            mViewItems.add(holder.itemView)
        }
    }

    override fun getItemCount(): Int = listData?.size ?: 0

    inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvKeyWord: TextView = itemView.findViewById(R.id.tv_keyword)
        private val ivKwDelete: ImageView = itemView.findViewById(R.id.iv_kw_delete)

        fun setItem(keyWord: KeyWord) {
            tvKeyWord.text = keyWord.keyWord.toString()
            ivKwDelete.visibility = if(mDeleteMode) View.VISIBLE else View.GONE

            tvKeyWord.setOnLongClickListener(View.OnLongClickListener {
                if(mDeleteMode/*ivKwDelete.visibility == View.VISIBLE*/) {
                    //ivKwDelete.visibility = View.GONE
                    mViewItems.forEach{
                        it.findViewById<ImageView>(R.id.iv_kw_delete).visibility = View.GONE
                    }
                    mDeleteMode = false
                } else {
                    //ivKwDelete.visibility = View.VISIBLE
                    val shake: Animation = AnimationUtils.loadAnimation(mContext, R.anim.shake)
                    //itemView.startAnimation(shake)
                    mViewItems.forEach{
                        it.findViewById<ImageView>(R.id.iv_kw_delete).visibility = View.VISIBLE
                        it.startAnimation(shake)
                    }
                    mDeleteMode = true
                }
                true
            })

            // 아이템 클릭 이벤트 처리.
            tvKeyWord.setOnClickListener(View.OnClickListener() {
                if(!mDeleteMode) {
                    MainActivity.getInstance()?.fetchNews(keyWord.keyWord + Common.KEYWORD_STOCKCODE_SEPARATOR + keyWord.stockCode)
                }
            })

            ivKwDelete.setOnClickListener(View.OnClickListener() {
                //ivKwDelete.visibility = View.GONE

                mKeyWordList?.remove(keyWord)
                //mViewItems?.removeAt(mPosition)

                mKeyWordList?.let { it -> MainActivity.getInstance()?.setKeyWordPrefFromList(it) }
                if(mKeyWordList?.size == 0) {
                    mDeleteMode = false
                }

                notifyDataSetChanged()
            })

        }
    }
}