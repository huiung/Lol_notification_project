package com.example.lol_notification_project.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.lol_notification_project.model.data.SummonerInfo
import com.example.lol_notification_project.R
import com.example.lol_notification_project.databinding.CardLayoutBinding


class SummonerAdapter(var summonerInfo: ArrayList<SummonerInfo>, private val context: Context): RecyclerView.Adapter<SummonerAdapter.SummonerViewHolder>() {

    fun updateSummoner(newSummoner: List<SummonerInfo>) {
        summonerInfo.clear()
        summonerInfo.addAll(newSummoner)
        notifyDataSetChanged()
    }

    //layout 이름에 따라 Binding 객체가 자동 생성됨 card_layout -> CardLayoutBinding
    class SummonerViewHolder(private val binding: CardLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(summoner: SummonerInfo) {
            binding.apply {
                summonerItem = summoner //Data Binding 적용전에는 모든 값을 set.... 을통해서 설정했었음
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SummonerViewHolder = (SummonerViewHolder(
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.context), R.layout.card_layout, parent, false)
        )
    )

    override fun getItemCount(): Int = summonerInfo.size


    override fun onBindViewHolder(holder: SummonerViewHolder, position: Int) {
        holder.bind(summonerInfo[position])
    }
}