package com.example.lol_notification_project

import android.content.Context
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lol_notification_project.util.getProgressDrwable
import com.example.lol_notification_project.util.loadImage
import kotlinx.android.synthetic.main.card_layout.view.*

class SummonerAdapter(var summonerInfo: ArrayList<SummonerInfo>, private val context: Context): RecyclerView.Adapter<SummonerAdapter.SummonerViewHolder>() {

    fun updateSummoner(newSummoner: List<SummonerInfo>) {
        summonerInfo.clear()
        summonerInfo.addAll(newSummoner)
        notifyDataSetChanged()
    }

    class SummonerViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {
        private val itemimage: ImageView = itemview.item_image
        private val itemid: TextView = itemview.item_id
        private val itemtitle: TextView = itemview.item_title
        private val itemleaguePoint: TextView = itemview.item_leaguePoint
        private val itemwinLoss: TextView = itemView.item_winLoss
        private val progressDrawable = getProgressDrwable(itemview.context)

        fun bind(summoner: SummonerInfo) {
            val title = "${summoner.tier} ${summoner.rank}"
            itemtitle.text = title
            val id = "${summoner.name}\nLV: ${summoner.summonerLevel}"
            itemid.text = id
            val points = summoner.leaguePoints.toString()+" 점"
            itemleaguePoint.text = points
            val winLoss =  "이번시즌 전적\n" +summoner.wins.toString() +"승 "+summoner.losses.toString() +"패"
            itemwinLoss.text = winLoss
            val uri = "https://ddragon.leagueoflegends.com/cdn/10.14.1/img/profileicon/${summoner.profileIconId}.png"
            itemimage.loadImage(uri, progressDrawable)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SummonerViewHolder = (SummonerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.card_layout, parent, false)))

    override fun getItemCount(): Int = summonerInfo.size


    override fun onBindViewHolder(holder: SummonerViewHolder, position: Int) {
        holder.bind(summonerInfo[position])
    }
}