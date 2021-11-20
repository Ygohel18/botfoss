package `in`.planckstudio.foss.bot.helper

import `in`.planckstudio.foss.bot.R
import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class WebViewHelper : AppCompatActivity() {

    private lateinit var mWebView: WebView
    private lateinit var mWebUrl: String
    private lateinit var mWebViewClient: WebViewClient
    private lateinit var topAppBar: MaterialToolbar


    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view_helper)

        this.mWebViewClient = WebViewClient()
        this.mWebView = findViewById(R.id.web_view_holder)
        topAppBar = findViewById(R.id.webTopAppBar)
        this.mWebUrl = intent.getStringExtra("weburl").toString()
        this.mWebView.webViewClient = this.mWebViewClient

        val setting = this.mWebView.settings
        setting.javaScriptEnabled = true
        setting.setSupportZoom(false)
        this.mWebView.loadUrl(mWebUrl)

        if(intent.hasExtra("title")) {
            topAppBar.title = intent.getStringExtra("title")
        }

        topAppBar.setNavigationOnClickListener {
            finish()
        }
    }
}