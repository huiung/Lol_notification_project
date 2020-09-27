package com.example.lol_notification_project.adapter

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.lol_notification_project.R
import com.example.lol_notification_project.model.data.SummonerInfo
import com.example.lol_notification_project.util.getProgressDrwable
import com.example.lol_notification_project.viewmodel.CardViewModel

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

@BindingAdapter("viewModel")
fun setViewModel(view: RecyclerView, vm: CardViewModel) {
        view.adapter?.run {
            if(this is SummonerAdapter) {

            }
        } ?: run {
            SummonerAdapter().apply {
                view.adapter = this

            }
        }
}

@BindingAdapter("summoner")
fun setRepositories(view: RecyclerView, items: List<SummonerInfo>) {

    view.adapter?.run {
        if(this is SummonerAdapter) {
            this.summonerInfo = items
            this.notifyDataSetChanged()
        }
    } ?: run {
        SummonerAdapter(items).apply {
            view.adapter = this
        }
    }

}

