package com.kakaroo.mynewsfeed.adapter

import android.R.attr
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kakaroo.mynewsfeed.entity.Topic
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.kakaroo.mynewsfeed.utility.Common
import com.kakaroo.mynewsfeed.R
import com.kakaroo.mynewsfeed.MainActivity
import android.R.attr.popupLayout

import android.view.Gravity

import android.util.DisplayMetrics
import android.widget.LinearLayout
import androidx.core.view.marginLeft
import android.view.ViewGroup.MarginLayoutParams

import android.view.ViewGroup





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
        holder.rvArticle.adapter = ArticleAdapter(context, mTopicList?.get(position)?.articles)
        holder.rvArticle.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        holder.rvArticle.setHasFixedSize(true)
        //holder.tv_topic.text = mTopicList?.get(position)?.title
        holder.tvTopicNum.text = mTopicList?.get(position)?.articles?.size.toString() + " 개"
        //holder.tv_stockPrice.text = mTopicList?.get(position)?.price
    }

    override fun getItemCount(): Int = listData?.size ?: 0

    inner class TopicHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val frameLayout: FrameLayout = itemView.findViewById(R.id.frame_topic)
        private val tvTopic: TextView = itemView.findViewById(R.id.tv_topic)
        val tvTopicNum: TextView = itemView.findViewById(R.id.tv_topicNum)
        private val tvStockPrice: TextView = itemView.findViewById(R.id.tv_stock_price)
        private val ivStockChart: ImageView = itemView.findViewById(R.id.iv_stock_chart)
        val rvArticle: RecyclerView = itemView.findViewById(R.id.rv_articles)


        fun setItem(topic: Topic) {
            tvTopic.text = topic.title
            tvStockPrice.text = topic.price

            if(topic.chartUrl.isEmpty() || topic.price.isEmpty()) {
                tvStockPrice.visibility = View.GONE
                ivStockChart.visibility = View.GONE
                val height = frameLayout.layoutParams.height
                val width = frameLayout.layoutParams.width
                val marginLeft = frameLayout.marginLeft
                frameLayout.layoutParams = LinearLayout.LayoutParams(width, height - 100)
                (frameLayout.layoutParams as LinearLayout.LayoutParams).setMargins(20, 4, 20, 4)
            } else {
                Glide
                    .with(itemView.context)
                    .load(topic.chartUrl)   //img drawable
                    .transform(CenterCrop(), RoundedCorners(5))
                    .placeholder(android.R.drawable.stat_sys_upload)
                    .into(ivStockChart)

                //default는 검정(보합)
                if(topic.price.contains(Common.STOCK_PRICE_PLUS_WORD)){
                    tvStockPrice.setTextColor(Color.RED)
                } else if(topic.price.contains(Common.STOCK_PRICE_MINUS_WORD)){
                    tvStockPrice.setTextColor(Color.BLUE)
                } else {
                    tvStockPrice.setTextColor(Color.BLACK)
                }
            }

            // 아이템 클릭 이벤트 처리.
            tvTopic.setOnClickListener(View.OnClickListener() {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Common.SEARCH_URL_NAVER + topic.title))
                mContext.startActivity(intent)
            })

            tvStockPrice.setOnClickListener(View.OnClickListener()  { _ ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Common.STOCK_URL_NAVER + topic.code))
                mContext.startActivity(intent)
            })

            ivStockChart.setOnClickListener(View.OnClickListener()  { _ ->
                //val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Common.STOCK_URL_NAVER + topic.code))
                //mContext.startActivity(intent)
                MainActivity.getInstance()?.configureDialog(topic.title+" #"+topic.code, topic.chartUrl)
                MainActivity.getInstance()?.mDialog?.show()
            })
        }
    }

}