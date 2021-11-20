package `in`.planckstudio.foss.bot.ui.instagram

import `in`.planckstudio.foss.bot.MainActivity
import `in`.planckstudio.foss.bot.R
import `in`.planckstudio.foss.bot.api.InstagramApi
import `in`.planckstudio.foss.bot.helper.AppHelper
import `in`.planckstudio.foss.bot.helper.DatabaseHelper
import `in`.planckstudio.foss.bot.model.ServiceAccount
import `in`.planckstudio.foss.bot.model.SessionModel
import `in`.planckstudio.foss.bot.util.LocalStorage
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.DelicateCoroutinesApi

@DelicateCoroutinesApi
class ConnectInstagramActivity : AppCompatActivity() {

    private lateinit var mWebView: WebView
    private lateinit var mInstagramSession: ServiceAccount
    private val url = "https://www.instagram.com/accounts/login/"
    private lateinit var ls: LocalStorage
    private lateinit var db: DatabaseHelper
    private lateinit var mInstagramApi: InstagramApi

    private lateinit var redUrl: String
    private lateinit var appUrl: String
    private lateinit var appRequest: String

    private lateinit var mRequestQueue: RequestQueue
    private var mQuery: String = ""

    override fun onResume() {
        super.onResume()
        AppHelper(this).preLaunchTask()
    }

    @RequiresApi(Build.VERSION_CODES.ECLAIR_MR1)
    @SuppressLint("SetJavaScriptEnabled", "ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect_instagram)

        AppHelper(this).preLaunchTask()

        // API URL
        this.redUrl = "file:///android_asset/html/connectig.html"
        this.appRequest = "addServiceAccount"

        this.mWebView = findViewById(R.id.ig_web_login)

        this.mRequestQueue = Volley.newRequestQueue(this)

        this.ls = LocalStorage(this)
        this.db = DatabaseHelper(this, null)
        this.mInstagramSession = ServiceAccount()
        val setting = this.mWebView.settings

        setting.javaScriptEnabled = true
        setting.domStorageEnabled = false
        setting.databaseEnabled = true
        setting.setSupportZoom(false)
        this.mWebView.clearCache(true)
        this.mWebView.clearHistory()
        this.mWebView.clearFormData()
        this.mWebView.clearSslPreferences()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().removeAllCookies(null)
        }

        this.mWebView.loadUrl(redUrl)
        val con = this

        this.mWebView.onCheckIsTextEditor()

        this.mWebView.webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView, url: String) {
                if (url == "https://www.instagram.com/") {
                    val cookieManager = CookieManager.getInstance()
                    val cookies = cookieManager.getCookie(url)
                    ls.save("igSession", cookies)
                    val csrftoken = getCookie(url, "csrftoken")
                    val ig_did = getCookie(url, "ig_did")
                    val ig_nrcb = getCookie(url, "ig_nrcb")
                    val shbid = getCookie(url, "shbid")
                    val shbts = getCookie(url, "shbts")
                    val rur = getCookie(url, "rur")

                    val ds_user_id = getCookie(url, "ds_user_id")
                    val sessionid: String? = getCookie(url, "sessionid")
                    val mid = getCookie(url, "mid")
                    var igSession = ""

                    db.addSession(
                        SessionModel(
                            ds_user_id.toString(),
                            "instagram",
                            "cookie",
                            cookies
                        )
                    )
                    db.addSession(
                        SessionModel(
                            ds_user_id.toString(),
                            "instagram",
                            "ig_did",
                            ig_did.toString()
                        )
                    )
                    db.addSession(
                        SessionModel(
                            ds_user_id.toString(),
                            "instagram",
                            "ig_nrcb",
                            ig_nrcb.toString()
                        )
                    )
                    db.addSession(
                        SessionModel(
                            ds_user_id.toString(),
                            "instagram",
                            "shbid",
                            shbid.toString()
                        )
                    )
                    db.addSession(
                        SessionModel(
                            ds_user_id.toString(),
                            "instagram",
                            "shbts",
                            shbts.toString()
                        )
                    )
                    db.addSession(
                        SessionModel(
                            ds_user_id.toString(),
                            "instagram",
                            "rur",
                            rur.toString()
                        )
                    )
                    db.addSession(
                        SessionModel(
                            ds_user_id.toString(),
                            "instagram",
                            "csrftoken",
                            csrftoken.toString()
                        )
                    )
                    db.addSession(
                        SessionModel(
                            ds_user_id.toString(),
                            "instagram",
                            "ds_user_id",
                            ds_user_id.toString()
                        )
                    )
                    db.addSession(
                        SessionModel(
                            ds_user_id.toString(),
                            "instagram",
                            "sessionid",
                            sessionid.toString()
                        )
                    )
                    db.addSession(
                        SessionModel(
                            ds_user_id.toString(),
                            "instagram",
                            "mid",
                            mid.toString()
                        )
                    )

                    ls.save("currentActiveInstagram", ds_user_id.toString())
                    val totalIg = ls.getValueInt("totalInstagram") + 1
                    ls.save("totalInstagram", totalIg)

                    ds_user_id?.let { mInstagramSession.setAccountKey("ds_user_id", it) }
                    csrftoken?.let { mInstagramSession.setAccountKey("csrftoken", it) }
                    sessionid?.let { mInstagramSession.setAccountKey("sessionid", it) }
                    mid?.let { mInstagramSession.setAccountKey("mid", it) }

                    ls.save("ig_connected", true)
                    ds_user_id?.let { ls.save("ds_user_id", it) }
                    mid?.let { ls.save("mid", it) }
                    sessionid?.let { ls.save("sessionid", it) }
                    csrftoken?.let { ls.save("csrftoken", it) }
                    showSuccessMessage()
                    mInstagramApi = InstagramApi(con)
                    mInstagramApi.setCookie(cookies)
                    closeWebView()
                }
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
            }
        }
    }

    private fun showSuccessMessage() {
        Toast.makeText(this, "Instagram Connected", Toast.LENGTH_SHORT).show()
    }

    private fun showFailedMessage() {
        Toast.makeText(this, "Something goes wrong, try again", Toast.LENGTH_SHORT).show()
    }

    private fun closeWebView() {
        mWebView.removeAllViews()
        mWebView.clearCache(true)
        mWebView.clearHistory()
        mWebView.onPause()
        mWebView.loadUrl("about:blanck")
        mWebView.pauseTimers()
        mWebView.destroy()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    fun getCookie(siteName: String?, cookieName: String?): String? {
        var cookieValue: String = ""
        val cookieManager = CookieManager.getInstance()
        val cookies = cookieManager.getCookie(siteName)
        val temp = cookies.split(";").toTypedArray()
        for (ar1 in temp) {
            if (ar1.contains(cookieName!!)) {
                val temp1 = ar1.split("=").toTypedArray()
                cookieValue = temp1[1]
                break
            }
        }
        return cookieValue
    }
}