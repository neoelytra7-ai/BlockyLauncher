package com.blocky.home

import android.app.Activity
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class MainActivity : Activity() {
    private lateinit var web: WebView
    private var started = false

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        web = WebView(this)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.webChromeClient = WebChromeClient()
        web.addJavascriptInterface(Bridge(), "Bridge")
        setContentView(web)
        web.loadUrl("file:///android_asset/index.html")
    }

    override fun onResume() {
        super.onResume()
        if (started) web.reload()
        started = true
    }

    @Deprecated("Launcher must not close on back")
    override fun onBackPressed() {}

    private fun cat(c: Int) = when (c) {
        ApplicationInfo.CATEGORY_GAME, ApplicationInfo.CATEGORY_AUDIO, ApplicationInfo.CATEGORY_VIDEO,
        ApplicationInfo.CATEGORY_IMAGE, ApplicationInfo.CATEGORY_SOCIAL -> "Fun"
        ApplicationInfo.CATEGORY_MAPS -> "Jalan"
        ApplicationInfo.CATEGORY_PRODUCTIVITY, ApplicationInfo.CATEGORY_NEWS -> "Kerja"
        else -> "Dasar"
    }

    inner class Bridge {
        @JavascriptInterface
        fun getApps(): String {
            val pm = packageManager
            val q = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            val arr = JSONArray()
            pm.queryIntentActivities(q, 0)
                .filter { it.activityInfo.packageName != packageName }
                .sortedBy { it.loadLabel(pm).toString().lowercase() }
                .forEach {
                    val big = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888)
                    val d = it.loadIcon(pm)
                    d.setBounds(0, 0, 96, 96)
                    d.draw(Canvas(big))
                    val bmp = Bitmap.createScaledBitmap(big, 24, 24, true)
                    val o = ByteArrayOutputStream()
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, o)
                    arr.put(
                        JSONObject()
                            .put("n", it.loadLabel(pm).toString())
                            .put("p", it.activityInfo.packageName)
                            .put("c", cat(it.activityInfo.applicationInfo.category))
                            .put("i", "data:image/png;base64," + Base64.encodeToString(o.toByteArray(), Base64.NO_WRAP))
                    )
                }
            return arr.toString()
        }

        @JavascriptInterface
        fun launch(p: String) {
            packageManager.getLaunchIntentForPackage(p)?.let { startActivity(it) }
        }
    }
}
