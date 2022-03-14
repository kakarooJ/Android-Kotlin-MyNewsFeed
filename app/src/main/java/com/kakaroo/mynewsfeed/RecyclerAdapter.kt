package com.kakaroo.mynewsfeed

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kakaroo.mynewsfeed.entity.Article

class RecyclerAdapter(private val context: Context, private val listData: ArrayList<Article>?)
    : RecyclerView.Adapter<RecyclerAdapter.Holder>() {

    private val mContext: Context = context
    var mCurIdx = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recycler,parent,false)

        val pref = mContext.getSharedPreferences(Common.SHARED_PREF_NAME, 0)

        return Holder(view).apply {
            //삭제버튼 클릭시 이벤트
            itemView.findViewById<Button>(R.id.bt_delete).setOnClickListener {
                var cursor = adapterPosition
                //강제로 null을 허용하기 위해 !! 사용
                /*var id : Long = listData?.get(cursor)?.id ?: -1L
                if(id != -1L) {
                    MainActivity().executeHttpRequest(mStrUrl, Common.HTTP_DELETE, id)
                }*/
                listData?.remove(listData?.get(cursor))
                notifyDataSetChanged()
            }
        }
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val article: Article? = listData?.get(position)
        if (article != null) {
            holder.setItem(article)
        }
    }

    override fun getItemCount(): Int = listData?.size ?: 0

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tv_id: TextView = itemView.findViewById(R.id.tv_id)
        private val tv_date: TextView = itemView.findViewById(R.id.tv_date)
        private val tv_title: TextView = itemView.findViewById(R.id.tv_title)


        fun setItem(article: Article) {
            var blongClick = false
            //index += 1
            //tv_id.text = index.toString()
            tv_id.text = article.idx.toString()//footPrinter.id.toString()
            tv_date.text = article.date.toString()
            tv_title.text = article.title.toString()

            // 아이템 클릭 이벤트 처리.
            itemView.setOnClickListener(View.OnClickListener() {
                if(blongClick) {    //롱클릭이후 클릭이벤트도 불리기 때문에 중복처리 방지를 위해,, 다른 방법이 있을 것 같은데..
                    blongClick = false
                } else {
                    mCurIdx = adapterPosition
                    Log.d(Common.MY_TAG, "setOnClickListener:[$mCurIdx] called")
                    var intent: Intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(listData?.get(adapterPosition)?.url)
                    mContext.startActivity(intent)
                }
            })

            itemView.setOnLongClickListener(OnLongClickListener { _ ->
                blongClick = true
                /*val position = adapterPosition
                Log.d(Common.LOG_TAG, "setOnLongClickListener called, position: $position")
                if (position != RecyclerView.NO_POSITION) {
                    //a_itemLongClickListener.onItemLongClick(a_view, position)
                }
                false*/
                mCurIdx = adapterPosition
                Log.d(Common.MY_TAG, "setOnClickListener:[$mCurIdx] called")

                true
            })
        }
    }
}