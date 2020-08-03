package com.example.lol_notification_project

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.card_layout.view.*

class SummonerAdapter(var summonerInfo: ArrayList<SummonerInfo>, private val context: Context): RecyclerView.Adapter<SummonerAdapter.SummonerViewHolder>() {

    class SummonerViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {
        var itemimage: ImageView = itemview.item_image
        var itemid: TextView = itemview.item_id
        var itemtitle: TextView = itemview.item_title
        var itemleaguePoint: TextView = itemview.item_leaguePoint
        var itemwinLoss: TextView = itemView.item_winLoss
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SummonerViewHolder = (
        SummonerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.card_layout, parent, false))
            )

    override fun getItemCount(): Int = summonerInfo.size


    override fun onBindViewHolder(holder: SummonerViewHolder, position: Int) {
        GlideApp.with(context)
                .load("https://ddragon.leagueoflegends.com/cdn/10.14.1/img/profileicon/${summonerInfo.get(position).profileIconId}.png") //이미지 URL 파싱
                .into(holder.itemimage)
        var name_level = summonerInfo.get(position).name +"\nLV: " +summonerInfo.get(position).summonerLevel
        holder.itemid.setText(name_level)
        holder.itemtitle.setText(summonerInfo.get(position).tier+" "+summonerInfo.get(position).rank )
        var points = summonerInfo.get(position).leaguePoints.toString()+" 점"
        holder.itemleaguePoint.setText(points)

        var winLoss =  "이번시즌 전적\n" +summonerInfo.get(position).wins.toString() +"승 "+summonerInfo.get(position).losses.toString() +"패"
        holder.itemwinLoss.setText(winLoss)
    }
}