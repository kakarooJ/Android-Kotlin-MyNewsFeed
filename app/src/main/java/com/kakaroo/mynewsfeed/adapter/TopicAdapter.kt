package com.kakaroo.mynewsfeed.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kakaroo.mynewsfeed.entity.Topic
import androidx.recyclerview.widget.LinearLayoutManager
import com.kakaroo.mynewsfeed.utility.Common
import com.kakaroo.mynewsfeed.R

class TopicAdapter(private val context: Context, private val listData: ArrayList<Topic>?)
    : RecyclerView.Adapter<TopicAdapter.TopicHolder>() {

    private val mContext: Context = context
    private var mTopicList = listData

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.topic_item_recycler,parent,false)

        return TopicHolder(view).apply {}
    }

    override fun onBindViewHolder(holder: TopicHolder, position: Int) {
        val topic: Topic? = listData?.get(position)
        if (topic != null) {
            holder.setItem(topic)
        }
        holder.rv_article.adapter = ArticleAdapter(context, mTopicList?.get(position)?.articles)
        holder.rv_article.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        holder.rv_article.setHasFixedSize(true)
        //holder.tv_topic.text = mTopicList?.get(position)?.title
        holder.tv_topicNum.text = mTopicList?.get(position)?.articles?.size.toString() + " 개"
        //holder.tv_stockPrice.text = mTopicList?.get(position)?.price
    }

    override fun getItemCount(): Int = listData?.size ?: 0

    inner class TopicHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv_topic: TextView = itemView.findViewById(R.id.tv_topic)
        val tv_topicNum: TextView = itemView.findViewById(R.id.tv_topicNum)
        val tv_stockPrice: TextView = itemView.findViewById(R.id.tv_stock_price)
        val rv_article: RecyclerView = itemView.findViewById(R.id.rv_articles)


        fun setItem(topic: Topic) {
            tv_topic.text = topic.title
            tv_stockPrice.text = topic.price

            //default는 검정(보합)
            if(topic.price.contains(Common.STOCK_PRICE_PLUS_WORD)){
                tv_stockPrice.setTextColor(Color.RED)
            } else if(topic.price.contains(Common.STOCK_PRICE_MINUS_WORD)){
                tv_stockPrice.setTextColor(Color.BLUE)
            } else {
                tv_stockPrice.setTextColor(Color.BLACK)
            }


            // 아이템 클릭 이벤트 처리.
            tv_topic.setOnClickListener(View.OnClickListener() {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Common.SEARCH_URL_NAVER + topic.title))
                mContext.startActivity(intent)
            })

            tv_stockPrice.setOnClickListener(View.OnClickListener()  { _ ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Common.STOCK_URL_NAVER + topic.code))
                mContext.startActivity(intent)
            })
        }
    }

}