package `in`.planckstudio.foss.bot.ui.instagram

import `in`.planckstudio.foss.bot.`interface`.NetworkResponse
import `in`.planckstudio.foss.bot.api.InstagramApi
import `in`.planckstudio.foss.bot.helper.AppHelper
import `in`.planckstudio.foss.bot.helper.DatabaseHelper
import `in`.planckstudio.foss.bot.util.LocalStorage
import `in`.planckstudio.wowdesign.views.cards.CardViewStyleOne
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteException
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.size
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import kotlinx.coroutines.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap

@DelicateCoroutinesApi
class InstagramToolActivity : AppCompatActivity(), NetworkResponse {

    private lateinit var mRequestQueue: RequestQueue
    private lateinit var mInstagramApi: InstagramApi
    private lateinit var ls: LocalStorage
    private lateinit var db: DatabaseHelper
    private lateinit var mQuery: String
    private lateinit var igCookie: String
    private lateinit var accountView: LinearLayout

    private lateinit var connectBtn: ExtendedFloatingActionButton
    private var currentId = ""

    init {
        mQuery = ""
        igCookie = ""
    }

    private fun init() {
        AppHelper(this).preLaunchTask()
        this.ls = LocalStorage(this)
        this.mRequestQueue = Volley.newRequestQueue(this)
        this.mQuery = ""
        this.db = DatabaseHelper(this, null)

        connectBtn = findViewById(R.id.connectInstagramButton)
        createNotificationChannel(CHANNEL_ID)

        this.accountView = findViewById(R.id.igToolAccounts)

        this.mInstagramApi = InstagramApi(this)
        val currentInstagramUser = ls.getValueString("currentActiveInstagram")
        this.currentId = currentInstagramUser.toString()
        igCookie = db.getSessionValue("instagram", currentInstagramUser, "cookie")
        mInstagramApi.setCookie(igCookie)
        try {
            mInstagramApi.setPrivateHeader(
                db.getSessionValue(
                    "instagram",
                    currentInstagramUser,
                    "csrftoken"
                ), igCookie
            )
        } catch (e: SQLiteException) {
            Log.e("PBOT", "Database closed")
        }
    }

    private fun getHashtagMedia(hashtag: String, action: String = "get") {
        val stringRequest = object : StringRequest(
            Method.POST,
            "https://i.instagram.com/api/v1/tags/$hashtag/sections/",
            Response.Listener<String> {
                val l = LocalStorage(this@InstagramToolActivity, "res_ig")
                l.save("hashtag_media", it.toString())
                when (action) {
                    "unfollow" -> {
                        unfollowHashtagUserCache()
                    }
                    "follow" -> {
                        followHashtagUserCache()
                    }
                    "like" -> {
                        likeHashtagUserCache()
                    }
                }
            },
            Response.ErrorListener {
                Log.e("PBOT", it.toString())
            }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                return mInstagramApi.getPrivateHeader()
            }
        }
        stringRequest.setShouldCache(false)
        mRequestQueue.add(stringRequest)
    }

    private fun likeHashtagUserCache() {

        val data = JSONObject(
            LocalStorage(
                this,
                "res_ig"
            ).getValueString("hashtag_media")
        ).getJSONArray("sections")

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText("Liking hashtag media")
            .setSmallIcon(R.drawable.ic_push)

        (0 until data.length()).forEach { i ->
            val sec = data.getJSONObject(i)
            val medias = sec.getJSONObject("layout_content").getJSONArray("medias")

            (0 until medias.length()).forEach { it ->
                val id = medias.getJSONObject(it)
                startLikePost(id.getJSONObject("media").getString("pk"))
            }
        }
    }

    private fun unfollowHashtagUserCache() {
        val data = JSONObject(
            LocalStorage(
                this,
                "res_ig"
            ).getValueString("hashtag_media")
        ).getJSONArray("sections")

        (0 until data.length()).forEach { i ->
            val sec = data.getJSONObject(i)
            val medias = sec.getJSONObject("layout_content").getJSONArray("medias")
            (0 until medias.length()).forEach { it ->
                val id = medias.getJSONObject(it)
                GlobalScope.launch {
                    mInstagramApi.unfollowUser(
                        id.getJSONObject("media").getJSONObject("user").getString("pk"), "nop"
                    )
                }
            }
        }
    }

    private fun followHashtagUserCache() {
        val data = JSONObject(
            LocalStorage(
                this,
                "res_ig"
            ).getValueString("hashtag_media")
        ).getJSONArray("sections")

        (0 until data.length()).forEach { i ->
            val sec = data.getJSONObject(i)
            val medias = sec.getJSONObject("layout_content").getJSONArray("medias")
            (0 until medias.length()).forEach { it ->
                val id = medias.getJSONObject(it)
                followUser(id.getJSONObject("media").getJSONObject("user").getString("pk"))
            }
        }
    }

    private fun followUser(id: String) {
        val stringRequest = object : StringRequest(
            Method.POST,
            "https://i.instagram.com/api/v1/friendships/create/$id/",
            Response.Listener<String> {
                Log.e("PBOT", it.toString())
            },
            Response.ErrorListener {
                Log.e("PBOT", it.toString())
            }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                return mInstagramApi.getPrivateHeader()
            }
        }
        val retryPolicy: RetryPolicy = DefaultRetryPolicy(15000, 3, 1F)
        stringRequest.setShouldCache(false)
        mRequestQueue.add(stringRequest).retryPolicy = retryPolicy
    }

    private fun main() {
        getConnectedAccounted()

        connectBtn.setOnClickListener {
            connectInstagram()
        }

        val intent = intent
        val extra = intent.extras
        var timeDifference = 1000
        if (extra != null) {
            val query = intent.getStringExtra("query").toString()
            val task = intent.getStringExtra("task").toString()

            when (task) {
                "instagramRemove" -> {
                    when (query) {
                        "followers" -> {
                            GlobalScope.launch {
                                mInstagramApi.getFollowers(currentId, "doRemoveFollowers")
                            }
                        }
                    }
                }
                "followHashtag" -> {
                    getHashtagMedia(query, "follow")
                    //apiFetchAllHashtagId(query)
                }
                "likeHashtag" -> {
                    getHashtagMedia(query, "like")
                    //apiFetchAllHashtagId(query)
                }
                "instagramGetHashtag" -> {
                    getHashtag(query)
                }
                "followHashtagUser" -> {
                    apiFollowAllHashtagUser(query)
                }
                "instagramDeleteAllPost" -> {
                    deleteAllPost()
                }
                "instagramUnfollowUser" -> {
                    startUnfollowUser()
                }
            }

            ls.save("lastIgTaskTime", Date().time.toString())
        }
        MainScope().launch {
            val r = async(Dispatchers.IO) {
                InstagramApi(this@InstagramToolActivity).getUserBasicInfo("ygohel18")
            }
            Log.e("AWAIT", r.await().toString())
        }
    }

    private fun startUnfollowUser() {
        val url =
            "https://i.instagram.com/api/v1/friendships/$currentId/following/?count=12&max_id=12"
        val stringRequest = object : StringRequest(
            Method.GET,
            url,
            Response.Listener {
                Log.e("PBOT", it)
                try {
                    val res = JSONObject(it)
                    val followers = res.getJSONArray("users")
                    (0 until followers.length()).forEach { i ->
                        val id = followers.getJSONObject(i).getString("pk")
                        GlobalScope.launch {
                            mInstagramApi.unfollowUser(id)
                        }

                    }
                } catch (e: JSONException) {
                    Log.e("PBOT", "$e")
                }
            },
            Response.ErrorListener {
                Log.e("PBOT", "Request error")
            }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                return mInstagramApi.getPrivateHeader()
            }
        }
        val retryPolicy: RetryPolicy = DefaultRetryPolicy(15000, 1, 1F)
        stringRequest.setShouldCache(false)

        Handler(Looper.getMainLooper()).postDelayed({
            mRequestQueue.add(stringRequest).retryPolicy = retryPolicy
        }, 3000)
    }

    private fun connectInstagram() {
        startActivity(Intent(this, ConnectInstagramActivity::class.java))
    }

    @SuppressLint("Range")
    private fun getConnectedAccounted() {
        val accounts = db.getSessionList("instagram")

        if (accounts != null && accounts.moveToFirst()) {
            do {
                getUserData(accounts.getString(accounts.getColumnIndex(DatabaseHelper.COLUMN_SESSION_USER)))
            } while (accounts.moveToNext() && !accounts.isAfterLast)
        }
    }

    private fun getUserData(id: String) {
        try {
            val data = db.getSessionValue("instagram", id, "info")
            val user = JSONObject(data)
            val username = user.getString("username")
            val name = user.getString("name")
            val dp = user.getString("profile_pic_url")
            renderConnectedAccount(username, name, dp, id)
        } catch (e: JSONException) {
            Log.e("BOT", "IG user data not found")
        }
    }

    private fun renderConnectedAccount(username: String, name: String, dp: String, id: String) {
        val card = CardViewStyleOne(this)
        card.setCardTitle(name)
        card.setCardSubtitle("@" + username)
        card.setCardImage(dp)

        card.card.setOnClickListener {
            val totalCards = accountView.size
            if (totalCards >= 2) {
                accountView.removeView(card.card)
                val firstCard = accountView.getChildAt(0) as MaterialCardView
                accountView.removeView(firstCard)
                firstCard.apply {
                    strokeColor = resources.getColor(R.color.text_color_white)
                    strokeWidth = 0
                    alpha = 0f
                    visibility = View.VISIBLE
                    animate().alpha(1f).setDuration(1000).setListener(null)
                }
                ls.removeValue("currentActiveInstagram")
                ls.save("currentActiveInstagram", id)
                Toast.makeText(this, "$username selected", Toast.LENGTH_SHORT).show()
                card.setCardBorder(resources.getColor(R.color.colorPrimary))
                accountView.addView(card.card, 0)
                accountView.addView(firstCard, 1)
            } else {
                startActivity(
                    Intent(this, InstagramProfileActivity::class.java).putExtra(
                        "query",
                        username
                    )
                )
            }
        }
        card.card.setOnLongClickListener {
            ls.removeValue("currentActiveInstagram")
            ls.save("currentActiveInstagram", id)
            startActivity(
                Intent(this, InstagramProfileActivity::class.java).putExtra(
                    "query",
                    username
                )
            )
            true
        }

        if (ls.getValueString("currentActiveInstagram") == id) {
            card.setCardBorder(resources.getColor(R.color.colorPrimary))
            accountView.addView(card.card, 0)
        } else {
            accountView.addView(card.card)
        }

        card.show()
    }

    private fun createNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Instagram task"
            val channelDescription = "Automation using bot"
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel(channelId, name, importance)
            channel.apply {
                description = channelDescription
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun deleteAllPost() {
        val userId = ls.getValueString("ds_user_id")
        apiFetchAndDeleteAllPost(userId, false)
    }

    private fun apiFollowAllHashtagUser(hashtag: String, next: String = "") {
        val stringRequest = object : StringRequest(
            Method.POST,
            "https://bot.planckstudio.in/api/v2/",

            Response.Listener<String> {
                Log.e("PBOT", it)
                try {
                    val jsonObject = JSONObject(it)
                    when (jsonObject.getInt("code")) {
                        200 -> {
                            val result = jsonObject
                                .getJSONObject("result")
                                .getJSONObject("instagram")
                                .getJSONObject("scrape")

                            val data = result.getJSONObject("data")
                            val code = data.getJSONArray("code")
                            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                                .setContentTitle("Hashtag Follow")
                                .setContentText("Following users")
                                .setSmallIcon(R.drawable.ic_push)

                            var max = 50
                            var progress = 0

                            max = if (code.length() >= 50) {
                                50
                            } else {
                                code.length()
                            }

                            with(NotificationManagerCompat.from(this)) {
                                builder.setProgress(max, progress, true)
                                notify(NOTIFICATION_ID, builder.build())
                                (0 until max).forEach { i ->
                                    progress += 1
                                    if (progress == max) {
                                        builder.setContentText("Task completed")
                                        builder.setProgress(0, 0, false)
                                    } else {
                                        val id = code.getJSONObject(i).getString("owner")
                                        startFollowHashtagUser(id)
                                        builder.setContentText("$progress/$max Followed")
                                        builder.setProgress(max, progress, true)
                                    }

                                    notify(NOTIFICATION_ID, builder.build())
                                }
                            }
                        }
                    }

                } catch (e: JSONException) {
                    Toast.makeText(this, "Unknown data", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener {
                Toast.makeText(this, "Something goes wrong", Toast.LENGTH_SHORT).show()
            }) {
            override fun getBody(): ByteArray {
                val jsonBody = JSONObject()
                val jsonQuery = JSONObject()
                jsonQuery.put("service", "instagram")
                jsonQuery.put("type", "hashtagMediaId")
                jsonQuery.put("key", hashtag)
                jsonQuery.put("end_cursour", next)
                jsonQuery.put("queryMode", "id")
                //jsonQuery.put("igSession", Base64.encodeToString(ls.getValueString("igSession").toByteArray(), Base64.NO_WRAP))
                jsonQuery.put("igSession", ls.getValueString("igSession"))

                jsonQuery.put("first", 50)
                jsonBody.put("type", "scrape")
                jsonBody.put("param", jsonQuery)
                mQuery = jsonBody.toString()
                return mQuery.toByteArray()
            }

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String>? {
                val params = HashMap<String, String>()
                params["Content-Type"] = "application/json"
                params["Accept"] = "application/json"
                params["Access-Control-Allow-Origin"] = "true"
                return params
            }
        }
        val retryPolicy: RetryPolicy = DefaultRetryPolicy(15000, 3, 1F)
        stringRequest.setShouldCache(false)
        mRequestQueue.add(stringRequest).retryPolicy = retryPolicy
    }

    private fun startFollowHashtagUser(id: String) {
        val stringRequest = object : StringRequest(
            Method.POST,
            "https://www.instagram.com/web/friendships/$id/follow/",

            Response.Listener<String> {
//
            },
            Response.ErrorListener {
                Log.e("PBOT", it.toString())
            }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                return mInstagramApi.getPrivateHeader()
            }
        }
        val retryPolicy: RetryPolicy = DefaultRetryPolicy(15000, 3, 1F)
        stringRequest.setShouldCache(false)
        mRequestQueue.add(stringRequest).retryPolicy = retryPolicy
    }


    private fun startLikePost(id: String) {
        val stringRequest = object : StringRequest(
            Method.POST,
            "https://www.instagram.com/web/likes/$id/like/",
            Response.Listener<String> {
                Log.e("PBOT", it)
            },
            Response.ErrorListener {
                Log.e("PBOT", "Request error")
            }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                return mInstagramApi.getPrivateHeader()
            }
        }
        val retryPolicy: RetryPolicy = DefaultRetryPolicy(15000, 3, 1F)
        stringRequest.setShouldCache(false)

        Handler(Looper.getMainLooper()).postDelayed({
            mRequestQueue.add(stringRequest).retryPolicy = retryPolicy
        }, 3000)
    }

    private fun apiFetchAndDeleteAllPost(username: String, latest: Boolean, next: String = "") {

        val stringRequest = object : StringRequest(
            Method.POST,
            "https://bot.planckstudio.in/api/v2/",

            Response.Listener<String> {
                try {
                    val jsonObject = JSONObject(it)

                    when (jsonObject.getInt("code")) {
                        200 -> {
                            val result = jsonObject
                                .getJSONObject("result")
                                .getJSONObject("instagram")
                                .getJSONObject("scrape")

                            val data = result.getJSONObject("data")

                            val hasNext = data.getBoolean("has_next")
                            val endCursor = data.getString("end_cursor")
                            val media = data.getJSONArray("media")

                            (0 until media.length()).forEach { i ->
                                val id = media.getJSONObject(i).getString("id")
                                startDeletePost(id)
                            }

                            if (!latest) {
                                if (hasNext) {
                                    apiFetchAndDeleteAllPost(username, false, endCursor)
                                }
                            }
                        }
                    }

                } catch (e: JSONException) {
                    Toast.makeText(this, "Unknown data", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener {
                Toast.makeText(this, "Something goes wrong", Toast.LENGTH_SHORT).show()
            }) {
            override fun getBody(): ByteArray {
                val jsonBody = JSONObject()
                val jsonQuery = JSONObject()
                jsonQuery.put("service", "instagram")
                jsonQuery.put("type", "allmedia")
                jsonQuery.put("key", username)
                jsonQuery.put("end_cursour", next)
                jsonQuery.put("queryMode", "id")
                jsonQuery.put("igSession", ls.getValueString("igSession"))

                if (latest) {
                    jsonQuery.put("first", 12)
                } else {
                    jsonQuery.put("first", 50)
                }

                jsonBody.put("type", "scrape")
                jsonBody.put("param", jsonQuery)
                mQuery = jsonBody.toString()
                return mQuery.toByteArray()
            }

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String>? {
                val params = HashMap<String, String>()
                params["Content-Type"] = "application/json"
                params["Accept"] = "application/json"
                params["Access-Control-Allow-Origin"] = "true"
                return params
            }
        }
        val retryPolicy: RetryPolicy = DefaultRetryPolicy(15000, 3, 1F)
        stringRequest.setShouldCache(false)
        mRequestQueue.add(stringRequest).retryPolicy = retryPolicy
    }

    private fun startDeletePost(id: String) {
        val stringRequest = object : StringRequest(
            Method.POST,
            "https://www.instagram.com/create/$id/delete/",

            Response.Listener<String> {
                Log.e("PBOT", it.toString())
                Toast.makeText(this, "$id deleted", Toast.LENGTH_SHORT).show()
            },
            Response.ErrorListener {
                Log.e("PBOT", "Request error")
            }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                return mInstagramApi.getPrivateHeader()
            }
        }
        val retryPolicy: RetryPolicy = DefaultRetryPolicy(15000, 3, 1F)
        stringRequest.setShouldCache(false)
        mRequestQueue.add(stringRequest).retryPolicy = retryPolicy
    }

    private fun getHashtag(hashtag: String) {
        val instagramApi = `in`.planckstudio.planckbot.api.InstagramApi(this)
        instagramApi.setCookie(this.igCookie)
        GlobalScope.launch(Dispatchers.IO) {
            val data = async { instagramApi.getHashtagMedia(hashtag, 50, "") }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instagram_tool)
        init()
        main()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        AppHelper(this).preLaunchTask()
    }

    companion object {
        const val CHANNEL_ID = "Instagram Likes"
        const val NOTIFICATION_ID = 1
        const val totalRequest = 24
    }

    override fun onFailure(message: String): String {
        TODO("Not yet implemented")
    }

    override suspend fun onResponse(response: String, next: String): String {
        TODO("Not yet implemented")
    }
}