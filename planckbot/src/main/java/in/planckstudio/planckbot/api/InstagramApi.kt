package `in`.planckstudio.planckbot.api

import android.content.Context
import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

final class InstagramApi(var context: Context) : NetworkResponse {
    private var requestQueue = Volley.newRequestQueue(context)
    private val networkResponse = this
    private lateinit var username: String
    private lateinit var password: String
    private lateinit var id: String
    private lateinit var cookie: String
    private lateinit var response: String
    private var header = HashMap<String, String>()

    init {
        this.header["User-Agent"] = userAgent
    }

    fun setHeader(key: String, value: String) {
        this.header[key] = value
    }

    fun getHeader():HashMap<String, String> {
        return this.header
    }

    fun setResponse(response: String) {
        this.response = response
    }

    fun setCookie(cookie: String) {
        this.setHeader("Cookie", cookie)
        this.cookie = cookie
    }

    fun setUsername(username: String) {
        this.username = username
    }

    fun setPassword(password: String) {
        this.password = password
    }

    fun setId(id: String) {
        this.id = id
    }

    fun getUsername(): String {
        return this.username
    }

    fun getPassword(): String {
        return this.password
    }

    fun getId(): String {
        return this.id
    }

    fun getResponse(): String {
        return this.response
    }

    private fun getCookie(): String {
        return this.cookie
    }

    suspend fun getHashtagMedia(hashtag: String, first: Int, next: String) {
        val queryUrl = "https://www.instagram.com/graphql/query/?query_hash=$hashHashtag&variables="
        val paramUrl = "{\"id\":\"$hashtag\",\"first\":$first,\"after\":\"$next\"}"
        return sendGetNetworkRequest(this.formatHashUrl(paramUrl, queryUrl))
    }

    fun formatHashUrl(url: String, base: String): String {
        url.replace("{", "%7B")
        url.replace("}", "%7D")
        url.replace(":", "%3A")
        url.replace(",", "%2C")
        return "$base+$url"
    }

//    private fun sendHashRequest(url: String, callback: Callback): Call {
//        val request = Request.Builder().url(url).addHeader("Cookie", this.getCookie())
//            .addHeader("User-Agent", userAgent).build()
//        val call = client.newCall(request)
//        call.enqueue(callback)
//        return call
//    }

    private fun sendGetNetworkRequest(
        url: String
    ) {
        val stringRequest = object : StringRequest(
            Method.GET,
            url,
            Response.Listener<String> {
                onResponse(it)
            },
            Response.ErrorListener {
                onFailure()
            }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                return getHeader()
            }
        }
        val retryPolicy: RetryPolicy = DefaultRetryPolicy(60000, 3, 1F)
        stringRequest.setShouldCache(true)
        requestQueue.add(stringRequest).retryPolicy = retryPolicy
    }

    override fun onFailure(): Boolean {
        return false
    }

    override fun onResponse(response: String): String {
        return response
    }

    companion object {
//        val client = OkHttpClient()
        const val baseUrl = "https://www.instagram.com/"
        const val hashHashtag = "3dec7e2c57367ef3da3d987d89f9dbc8"
        const val userAgent =
            "Mozilla/5.0 (Linux; Android 5.0.1; LG-H342 Build/LRX21Y; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/65.0.3325.109 Mobile Safari/537.36 Instagram 40.0.0.14.95 Android (21/5.0.1; 240dpi; 480x786; LGE/lge; LG-H342; c50ds; c50ds; pt_BR; 102221277)"
    }

}

interface NetworkResponse {
    fun onFailure(): Boolean
    fun onResponse(response: String): String
}