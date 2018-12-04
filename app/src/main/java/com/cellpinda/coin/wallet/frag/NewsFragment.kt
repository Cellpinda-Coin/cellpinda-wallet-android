package com.cellpinda.coin.wallet.frag

import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import com.cellpinda.coin.wallet.MainActivity
import com.cellpinda.coin.wallet.R
import kotlinx.android.synthetic.main.frag_webview.*

/**
 * Created by https://github.com/method76 on 2017-11-06.
 */
class NewsFragment : Fragment() {

    private val TAG: String = this.javaClass.simpleName
    private lateinit var ctx: MainActivity
    private var isFirst = true

    companion object {
        var lastCallTimeSales: Long = 0
        val TIME_DIFF = 1000 * 20
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        ctx = this.activity as MainActivity
        return LayoutInflater.from(context)
                .inflate(R.layout.frag_webview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ctx.hideProgress()
        ctx.showProgress()
        webview.settings.javaScriptEnabled = true
        webview.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                view.visibility = View.INVISIBLE
            }
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                try {
                    ctx.hideProgress()
                    refresh_layout.isRefreshing = false
                    isFirst = false
                    view.visibility = View.VISIBLE
                } catch(e: Exception) {
                    Log.w(TAG, "" + e.message)
                }
            }
        }
        refresh_layout.setOnRefreshListener { refreshNews() }
        webview.loadUrl("file:///android_asset/twitter.html")
    }

    fun refreshNews() {
        // Todo: 1분이 넘었으면
        if (!isFirst) webview.reload()
    }

}