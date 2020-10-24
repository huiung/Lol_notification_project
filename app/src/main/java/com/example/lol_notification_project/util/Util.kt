package com.example.lol_notification_project.util

import android.content.Context
import android.widget.Toast
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.example.lol_notification_project.data.local.Preferences
import com.example.lol_notification_project.ui.main.MainActivity
import org.koin.android.ext.android.inject
import org.koin.java.KoinJavaComponent.inject

fun getProgressDrwable(context: Context): CircularProgressDrawable { //이미지 로딩 표시
    return CircularProgressDrawable(context).apply {
        strokeWidth = 10f
        centerRadius = 50f
        start()
    }
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

    val Preferences = Preferences(context)
    if (Preferences.getString(key) != "NoID") {
        makeToastComment("이미 등록된 아이디 입니다.", context)
    } else {
        Preferences.setString(key, value)
        makeToastComment("등록 완료", context)
    }
}
