package com.kakaroo.mynewsfeed

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.kakaroo.mynewsfeed.entity.Article
import androidx.core.content.ContextCompat.startActivity

import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat


class ArticleAdapter(private val context: Context, private val listData: ArrayList<Article>?)
    : RecyclerView.Adapter<ArticleAdapter.CustomViewHolder>() {

    private val mContext: Context = context
    private var mArticleList: ArrayList<Article>? = listData
    //private inflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.article_item_recycler, parent, false)

        return CustomViewHolder(view).apply {}
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val article: Article? = listData?.get(position)
        if (article != null) {
            holder.setItem(article)
        }
    }

    override fun getItemCount(): Int = listData?.size ?: 0

    inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tv_date: TextView = itemView.findViewById(R.id.tv_date)
        private val tv_title: TextView = itemView.findViewById(R.id.tv_title)

        fun setItem(article: Article) {
            tv_date.text = article.date.toString()
            tv_title.text = article.title.toString()

            // 아이템 클릭 이벤트 처리.
            itemView.setOnClickListener(View.OnClickListener() {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
                mContext.startActivity(intent)
            })
        }
    }
}