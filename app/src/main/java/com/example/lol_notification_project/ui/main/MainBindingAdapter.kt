package com.example.lol_notification_project.ui.main

import android.content.Context
import android.content.Intent
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.lol_notification_project.R
import com.example.lol_notification_project.data.local.Preferences
import com.example.lol_notification_project.data.model.SummonerInfo
import com.example.lol_notification_project.service.UndeadService
import com.example.lol_notification_project.util.getProgressDrwable

//layout단에서 App:annotaiont으로 접근해서 사용.
@BindingAdapter("image")
fun bindImage(view: ImageView, uri: String?) { //imageView에 값을 넣기위한 Adapter Layout단에서 넣어주는 값이 uri로 들어옴
    val progressDrawable = getProgressDrwable(view.context)

    val options = RequestOptions()
        .placeholder(progressDrawable)
        .error(R.mipmap.ic_launcher_round)

    Glide.with(view.context)
        .setDefaultRequestOptions(options)
        .load(uri)
        .into(view)
}

@BindingAdapter("refreshing")
fun SwipeRefreshLayout.refreshing(visible: Boolean) {

    isRefreshing = visible
}


@BindingAdapter("summoner")
fun setSummoner(view: RecyclerView, items: List<SummonerInfo>?) {

    view.adapter?.run {
        if(this is MainAdapter) {
            if (items != null) {
                this.summonerInfo = items
            }
            this.notifyDataSetChanged()
        }
    } ?: run {
        items?.let {
            MainAdapter(it).apply {
                view.adapter = this
            }
        }
    }
}
