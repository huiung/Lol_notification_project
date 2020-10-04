package com.example.lol_notification_project.util

import android.content.Context
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.example.lol_notification_project.data.local.Preferences
import com.example.lol_notification_project.data.remote.SummonerAPI
import com.example.lol_notification_project.ui.main.MainActivity
import kotlinx.coroutines.*

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
    if (Preferences.getString(context, key) != "NoID"
    ) {
        makeToastComment("이미 등록된 아이디 입니다.", context)

    } else {
        Preferences.setString(context, key, value)
        makeToastComment("등록 완료", context)
    }
}

suspend fun storeid(api_key: String?, id: String, myAPI: SummonerAPI): Pair<String?, String?> { //해당 id api호출

    var cryptedid: String? = null
    if (id != "") {
        api_key?.let { api_key ->
            val response = myAPI.getsummoner(id, api_key)
            if (response.isSuccessful) {
                cryptedid = response.body()?.id
            }
        }
    }
    return Pair<String?, String?>(id, cryptedid)
}

fun changeapi(context: Context, alert:AlertDialog.Builder) { //api 변경 관련 alertdialog
    val idText = EditText(context)
    alert.setTitle("API 키 변경").setMessage("변경할 키를 입력해 주세요.")
        .setView(idText)

        alert.setPositiveButton("변경") { p0, p1 ->
            val api_key = idText.text.toString()
            Log.d("mytag", api_key)
            Preferences.setAPI(context, "Api_key", api_key!!)
            makeToastComment("변경 완료", context)
        }
        .setNegativeButton("취소") { p0, p1 -> }
        .create().show()
}