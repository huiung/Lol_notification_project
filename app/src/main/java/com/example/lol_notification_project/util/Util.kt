package com.example.lol_notification_project.util

import android.content.Context
import android.widget.ImageView
import android.widget.Toast
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.lol_notification_project.Model.Preferences
import com.example.lol_notification_project.R
import com.example.lol_notification_project.View.MainActivity

fun getProgressDrwable(context: Context): CircularProgressDrawable {
    return CircularProgressDrawable(context).apply {
        strokeWidth = 10f
        centerRadius = 50f
        start()
    }
}

fun ImageView.loadImage(uri: String?, progressDrawable: CircularProgressDrawable) {

    val options = RequestOptions()
        .placeholder(progressDrawable)
        .error(R.mipmap.ic_launcher_round)

    Glide.with(this.context)
        .setDefaultRequestOptions(options)
        .load(uri)
        .into(this)
}

fun makeToastComment(str: String, context: Context) {
    if (MainActivity.mToast != null) {
        MainActivity.mToast!!.cancel()
        MainActivity.mToast = Toast.makeText(context, str, Toast.LENGTH_SHORT)
    } else MainActivity.mToast =
        Toast.makeText(context, str, Toast.LENGTH_SHORT)
    MainActivity.mToast?.show()
}

fun makeToast(key: String, value: String, context: Context) {
    if (Preferences.getString(
            context,
            key
        ) != "NoID"
    ) {
        makeToastComment("이미 등록된 아이디 입니다.", context)

    } else {
        Preferences.setString(
            context,
            key,
            value
        )
        makeToastComment("등록 완료", context)
    }
}

