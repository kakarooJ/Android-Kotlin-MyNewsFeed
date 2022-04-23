package com.kakaroo.mynewsfeed.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kakaroo.mynewsfeed.entity.Article

import android.content.Intent
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.kakaroo.mynewsfeed.R


class ArticleAdapter(private val context: Context, private val listData: ArrayList<Article>?)
    : RecyclerView.Adapter<ArticleAdapter.CustomViewHolder>() {

    private val mContext: Context = context
    private var mArticleList: ArrayList<Article>? = listData
    //private inflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.article_item_cardview, parent, false)

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
        private val imgThumb: ImageView = itemView.findViewById(R.id.iv_thumb)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        private val tvCorp: TextView = itemView.findViewById(R.id.tv_corp)

        fun setItem(article: Article) {

            if(article.imgUrl == "") {
                Glide
                    .with(itemView.context)
                    .load(R.drawable.news_thumb_2)   //img drawable
                    .circleCrop()
                    .placeholder(android.R.drawable.stat_sys_upload)
                    .into(imgThumb)
            } else {
                Glide
                    .with(itemView.context)
                    .load(article.imgUrl)   //img drawable
                    .transform(CenterCrop(), RoundedCorners(20))
                    .placeholder(android.R.drawable.stat_sys_upload)
                    .into(imgThumb)
            }

            tvDate.text = article.date.toString()
            tvTitle.text = article.title.toString()
            tvCorp.text = article.newsCorp.toString()

            // 아이템 클릭 이벤트 처리.
            itemView.setOnClickListener(View.OnClickListener() {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
                mContext.startActivity(intent)
            })
        }
    }
}