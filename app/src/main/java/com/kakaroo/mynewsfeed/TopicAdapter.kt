package com.kakaroo.mynewsfeed

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kakaroo.mynewsfeed.entity.Topic
import androidx.recyclerview.widget.LinearLayoutManager

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
        holder.tv_topic.text = mTopicList?.get(position)?.title
    }

    override fun getItemCount(): Int = listData?.size ?: 0

    inner class TopicHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv_topic: TextView = itemView.findViewById(R.id.tv_topic)
        val rv_article: RecyclerView = itemView.findViewById(R.id.rv_articles)

        fun setItem(topic: Topic) {
            tv_topic.text = topic.title
        }
    }

}