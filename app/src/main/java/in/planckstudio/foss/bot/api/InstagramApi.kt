package `in`.planckstudio.foss.bot.api

import `in`.planckstudio.foss.bot.R
import `in`.planckstudio.foss.bot.`interface`.NetworkResponse
import `in`.planckstudio.foss.bot.helper.DatabaseHelper
import `in`.planckstudio.foss.bot.helper.NetworkHelper
import `in`.planckstudio.foss.bot.util.LocalStorage
import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.RequestQueue
import com.android.volley.AuthFailureError
import com.android.volley.RetryPolicy
import com.android.volley.DefaultRetryPolicy
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.*
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@DelicateCoroutinesApi
class InstagramApi(context: Context) : NetworkHelper(context), NetworkResponse {

    var res: NetworkResponse = this
    private var defaultHeader = HashMap<String, String>()
    private var privateHeader = HashMap<String, String>()
    val baseUrl = "https://instagram.com/"
    private lateinit var csrfToken: String
    private lateinit var cookie: String
    private var ls: LocalStorage
    private var db: DatabaseHelper
    private var mQuery: String
    private var mRequestQueue: RequestQueue
    private lateinit var currentInstagramUser: String
    var currentUserId: String = ""
    var loop_count = 0

    fun setCsrfToken(csrfToken: String) {
        this.csrfToken = csrfToken
    }

    fun getCsrfToken(): String {
        return this.csrfToken
    }

    fun setCookie(cookie: String) {
        this.cookie = cookie
    }

    fun getCookie(): String {
        return this.cookie
    }

    init {
        this.defaultHeader["Content-Type"] = "application/x-www-form-urlencoded"
        this.defaultHeader["Cookie"] = context.resources.getString(R.string.ig_cookie)
        this.defaultHeader["User-agent"] = context.resources.getString(R.string.ig_useragent)
        this.ls = LocalStorage(context)
        this.db = DatabaseHelper(context, null)
        this.mRequestQueue = Volley.newRequestQueue(context)
        this.mQuery = ""
        if (ls.getValueBoolean("ig_connected")) {
            this.currentInstagramUser = ls.getValueString("currentActiveInstagram")
            setCookie(db.getSessionValue("instagram", this.currentInstagramUser, "cookie"))
        } else {
            setCookie(context.resources.getString(R.string.ig_cookie))
        }
    }

    fun setPrivateHeader(csrfToken: String, cookie: String) {
        this.setCsrfToken(csrfToken)
        this.setCookie(cookie)
        this.privateHeader["Accept"] =
            "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"
        this.privateHeader["Access-Control-Allow-Origin"] = "true"
        this.privateHeader["x-csrftoken"] = this.getCsrfToken()
        this.privateHeader["x-requested-with"] = "XMLHttpRequest"
        this.privateHeader["User-Agent"] = context.resources.getString(R.string.ig_useragent)
        this.privateHeader["Accept-Language"] = "en-US,en;q=0.5"
        this.privateHeader["Upgrade-Insecure-Requests"] = "1"
        this.privateHeader["Cache-Control"] = "max-age=0, no-cache"
        this.privateHeader["x-instagram-ajax"] = "1"
        this.privateHeader["Pragma"] = "no-cache"
        this.privateHeader["Cookie"] = this.getCookie()
    }

    fun getDefaultHeader(cookie: String = context.resources.getString(R.string.ig_cookie)): HashMap<String, String> {
        this.setCookie(cookie)
        this.defaultHeader["Cookie"] = this.getCookie()
        return this.defaultHeader
    }

    fun getPrivateHeader(): HashMap<String, String> {
        return this.privateHeader
    }

    suspend fun removeFollowers(id: String, next: String = "") {
        classicRequest(
            "${BASE_API_URL}friendships/remove_follower/${id}/"
        )
    }

    suspend fun unfollowUser(id: String, next: String = "") {
        classicRequest("${BASE_API_URL}friendships/destroy/${id}/")
    }

    suspend fun followUser(id: String, next: String = "") {
        classicRequest("${BASE_API_URL}friendships/create/${id}/")
    }

    suspend fun getHashtagMedia(hashtag: String, next: String = ""): String {
        return classicRequest("${BASE_API_URL}tags/$hashtag/sections/")
    }

    suspend fun blockUser(id: String, next: String = "") {
        classicRequest("${BASE_API_URL}friendships/block/$id/", next)
    }

    suspend fun unblockUser(id: String, next: String = "") {
        classicRequest("${BASE_API_URL}friendships/unblock/$id/", next)
    }

    suspend fun blockFriendReel(id: String, next: String = "") {
        classicRequest("${BASE_API_URL}friendships/block_friend_reel/$id/", next)
    }

    suspend fun unblockFriendReel(id: String, next: String = "") {
        classicRequest("${BASE_API_URL}friendships/unblock_friend_reel/$id/", next)
    }

    suspend fun getBlockReel(id: String, next: String = "") {
        classicRequest("${BASE_API_URL}friendships/blocked_reels/", next)
    }

    suspend fun notificationsOn(id: String, next: String = "") {
        classicRequest("${BASE_API_URL}friendships/favorite/${id}", next)
    }

    suspend fun notificationsOff(id: String, next: String = "") {
        classicRequest("${BASE_API_URL}friendships/unfavorite/${id}", next)
    }

    suspend fun ignoreRequest(id: String, next: String = "") {
        classicRequest("${BASE_API_URL}friendships/ignore/${id}", next)
    }

    suspend fun getUserInfo(id: String, next: String = ""): String {
        return classicRequest("${BASE_API_URL}users/${id}/info/", next)
    }

    suspend fun getUserDetailedInfo(id: String, next: String = "") {
        classicRequest("${BASE_API_URL}users/${id}/full_detail_info/", next)
    }

    suspend fun getUsernameInfo(id: String, next: String = "") {
        classicRequest("${BASE_API_URL}users/${id}/usernameinfo/", next)
    }

    suspend fun getBlockedUsers(id: String, next: String = "") {
        currentUserId = id
        classicGetRequest("${BASE_API_URL}users/blocked_list/", next)
    }

    suspend fun getFollowings(id: String, next: String = "") {
        currentUserId = id
        classicGetRequest("${BASE_API_URL}friendships/$id/following/?count=12&max_id=12", next)
    }

    suspend fun getFollowers(id: String, next: String = "") {
        currentUserId = id
        classicGetRequest("${BASE_API_URL}friendships/$id/followers/", next)
    }

    suspend fun getFollowers(id: String, max: String, next: String = "") {
        currentUserId = id
        classicGetRequest("${BASE_API_URL}friendships/$id/followers/?next_max_id=${max}", next)
    }

    private suspend fun friendshipsShow(id: String, next: String = "") {
        classicGetRequest("${BASE_API_URL}friendships/show/$id/", next)
    }

    fun formatHashUrl(url: String, base: String): String {
        url.replace("{", "%7B")
        url.replace("}", "%7D")
        url.replace(":", "%3A")
        url.replace(",", "%2C")
        return "$base+$url"
    }

    suspend fun getUserBasicInfo(username: String): String {
        val url = "https://www.instagram.com/${username}/?__a=1"
        return defaultRequest(url)
    }

    suspend fun getSingleMedia(id: String): String {

        val url = when {
            id.contains("https://www.instagram.com/p/") -> {
                val i = id.split("?")
                "${i[0]}?__a=1"
            }
            id.contains("https://www.instagram.com/reel/") -> {
                val i = id.split("?")
                "${i[0]}?__a=1"
            }
            else -> {
                "https://www.instagram.com/p/$id?__a=1"
            }
        }

        return defaultRequest(url)
    }

    suspend fun apiGetPost(query: String) =
        suspendCoroutine<String> { r ->
            val waitFor = CoroutineScope(Dispatchers.IO).async {
                val stringRequest = object : StringRequest(
                    Method.POST,
                    "https://bot.planckstudio.in/api/v2/",

                    Response.Listener {
                        r.resume(it)
                    },
                    Response.ErrorListener {
                        r.resume(it.toString())
                    }) {

                    override fun getBody(): ByteArray {

                        val jsonBody = JSONObject()
                        val jsonQuery = JSONObject()

                        jsonQuery.put("key", query)
                        jsonQuery.put("service", "instagram")
                        jsonQuery.put("type", "media")
                        jsonQuery.put("firebaseId", ls.getValueString("device_uid"))

                        if (ls.getValueBoolean("isIgCustomSessionEnabled")) {
                            jsonQuery.put("igSession", ls.getValueString("igTestSession"))
                        } else {
                            if (ls.getValueBoolean("ig_connected")) {
                                jsonQuery.put("igSession", ls.getValueString("igSession"))
                            }
                        }

                        jsonBody.put("type", "scrape")
                        jsonBody.put("param", jsonQuery)
                        mQuery = jsonBody.toString()
                        return mQuery.toByteArray()
                    }

                    @Throws(AuthFailureError::class)
                    override fun getHeaders(): Map<String, String> {
                        val params = HashMap<String, String>()
                        params["Content-Type"] = "application/json"
                        params["Accept"] = "application/json"
                        params["Access-Control-Allow-Origin"] = "true"
                        return params
                    }
                }
                val retryPolicy: RetryPolicy = DefaultRetryPolicy(15000, 1, 1F)
                stringRequest.setShouldCache(false)
                mRequestQueue.add(stringRequest).retryPolicy = retryPolicy
            }
        }

    private suspend fun defaultRequest(url: String, next: String = "") =
        suspendCoroutine<String> { r ->
            val waitFor = CoroutineScope(Dispatchers.IO).async {
                val stringRequest = object : StringRequest(
                    Method.GET,
                    url,
                    Response.Listener {
                        r.resume(it)
                    },
                    Response.ErrorListener {
                        r.resume(it.toString())
                    }) {
                    @Throws(AuthFailureError::class)
                    override fun getHeaders(): Map<String, String> {
                        return getDefaultHeader(getCookie())
                    }
                }
                stringRequest.setShouldCache(false)
                mRequestQueue.add(stringRequest)
            }
        }

    private suspend fun classicGetRequest(url: String, next: String = "") =
        suspendCoroutine<String> { r ->
            val waitFor = CoroutineScope(Dispatchers.IO).async {
                val stringRequest = object : StringRequest(
                    Method.GET,
                    url,
                    Response.Listener {
                        r.resume(it)
                    },
                    Response.ErrorListener {
                        r.resume(it.toString())
                    }) {
                    @Throws(AuthFailureError::class)
                    override fun getHeaders(): Map<String, String> {
                        return getPrivateHeader()
                    }
                }
                stringRequest.setShouldCache(false)
                mRequestQueue.add(stringRequest)
            }
        }

    suspend fun classicRequest(url: String, next: String = "") =
        suspendCoroutine<String> { r ->
            val stringRequest = object : StringRequest(
                Method.POST,
                url,
                Response.Listener {
                    //res.onResponse(it, next)
                    r.resume(it)
                },
                Response.ErrorListener {
                    Log.e("BOT", "Error: $it")
                    res.onFailure(ERROR_MESSAGE)
                }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    return getPrivateHeader()
                }
            }
            stringRequest.setShouldCache(false)
            mRequestQueue.add(stringRequest)
        }

    private suspend fun myClassicRequest(url: String, next: String = ""): JSONObject {
        var r: JSONObject = JSONObject()

        val waitFor = CoroutineScope(Dispatchers.IO).async {
            val stringRequest = object : StringRequest(
                Method.POST,
                url,
                Response.Listener {
                    //res.onResponse(it, next)
                    this.async {
                        Log.e("BOT", it)
                        r = JSONObject(it)
                        r
                    }
                },
                Response.ErrorListener {
                    Log.e("BOT", "Error: $it")
                    res.onFailure(ERROR_MESSAGE)
                }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    return getPrivateHeader()
                }
            }
            stringRequest.setShouldCache(false)
            mRequestQueue.add(stringRequest)

        }

        waitFor.await()
        return r
    }

    companion object {
        const val BASE_API_URL = "https://i.instagram.com/api/v1/"
        const val ERROR_MESSAGE = "Something goes wrong"
    }

    override fun onFailure(message: String): String {
        return message
    }

    override suspend fun onResponse(response: String, next: String): String {
        when (next) {
            "nop" -> {
                Log.e("BOT", response)
                ls.save("last_ig_response", response)
            }
            "friendshipsShowAll" -> {
                friendshipsShowAll(response)
            }
            "getUserInfoAll" -> {
                getUserInfoAll(response)
            }
            "doRemoveFollowers" -> {
                doRemoveFollowers(response)
            }
            "checkGhostFollowers" -> {
                checkGhostFollowers(response)
            }
            "removeGhostFollower" -> {
                removeGhostFollowers(response)
            }
            else -> {
                //
            }
        }

        return response
    }

    private suspend fun doRemoveFollowers(response: String) {
        val data = JSONObject(response)
        val users = data.getJSONArray("users")
        (0 until users.length()).forEach { i ->
            val u = users.getJSONObject(i)
            removeFollowers(u.getString("pk"), "nop")
            Log.e("GHOST", u.getString("username"))
        }
    }

    private suspend fun removeGhostFollowers(response: String) {
        val user = JSONObject(response).getJSONObject("user")

        if (user.getInt("follower_count") == 0) {
            removeFollowers(user.getString("pk"), "nop")
            Log.e("GHOST", user.getString("username"))
        }
    }

    private suspend fun checkGhostFollowers(response: String) {
        val data = JSONObject(response)
        val users = data.getJSONArray("users")

        (0 until users.length()).forEach { i ->
            val u = users.getJSONObject(i)
            removeFollowers(u.getString("pk"), "nop")
            Log.e("GHOST", u.getString("username"))
        }

        if (data.has("next_max_id")) {
            getFollowers(currentUserId, data.getString("next_max_id"), "checkGhostFollowers")
        }
    }


    private suspend fun friendshipsShowAll(response: String) {
        val data = JSONObject(response).getJSONArray("users")
        (0 until data.length()).forEach { i ->
            val u = data.getJSONObject(i)
            friendshipsShow(u.getString("pk"), "nop")
        }
    }

    private suspend fun getUserInfoAll(response: String) {
        val data = JSONObject(response).getJSONArray("users")
        (0 until data.length()).forEach { i ->
            val u = data.getJSONObject(i)
            getUserInfo(u.getString("pk"), "nop")
        }
    }
}