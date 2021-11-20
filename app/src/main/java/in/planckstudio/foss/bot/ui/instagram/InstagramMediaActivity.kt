package `in`.planckstudio.foss.bot.ui.instagram

import `in`.planckstudio.foss.bot.api.InstagramApi
import `in`.planckstudio.foss.bot.helper.*
import `in`.planckstudio.foss.bot.util.LocalStorage
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import `in`.planckstudio.foss.bot.R
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

@DelicateCoroutinesApi
class InstagramMediaActivity : AppCompatActivity() {
    private lateinit var topAppBar: MaterialToolbar
    private lateinit var mImageHolder: ImageView
    private lateinit var mTotalLikesText: MaterialTextView
    private lateinit var mTotalCommentsText: MaterialTextView
    private lateinit var mTotalMentionsText: MaterialTextView
    private lateinit var mTotalHashtagsText: MaterialTextView
    private lateinit var mDownloadButton: ExtendedFloatingActionButton
    private lateinit var mRepostButton: ExtendedFloatingActionButton
    private lateinit var mMedia: String
    private lateinit var mUsername: String
    private lateinit var mProfileUrl: String
    private lateinit var mAutoDownloadBanner: MaterialTextView
    private lateinit var clipboardManager: ClipboardManager
    private lateinit var clipData: ClipData
    private lateinit var mRequestQueue: RequestQueue
    private lateinit var mQuery: String
    private lateinit var mHashtags: String
    private lateinit var mCaption: String
    private lateinit var mCaptionAll: String
    private lateinit var mMention: String
    private lateinit var dh: DownloadHelper
    private lateinit var ls: LocalStorage
    private lateinit var db: DatabaseHelper
    private lateinit var mHashtagText: MaterialTextView
    private lateinit var mHashtagButton: MaterialCardView
    private lateinit var mCaptionText: MaterialTextView
    private lateinit var mCaptionButton: MaterialCardView
    private lateinit var mMentionText: MaterialTextView
    private lateinit var mMentionButton: MaterialCardView
    private lateinit var mCaptionAllText: MaterialTextView
    private lateinit var mCaptionAllButton: MaterialCardView

    private fun init() {
        AppHelper(this).preLaunchTask()
        topAppBar = findViewById(R.id.igMediaTopAppBar)
        this.mAutoDownloadBanner = findViewById(R.id.igMediaAutoDownloadBanner)
        this.mImageHolder = findViewById(R.id.defaultSquareImageHolder)
        this.mTotalLikesText = findViewById(R.id.igMediaTotalLikes)
        this.mTotalCommentsText = findViewById(R.id.igMediaTotalComments)
        this.mTotalMentionsText = findViewById(R.id.igMediaTotalMentions)
        this.mTotalHashtagsText = findViewById(R.id.igMediaTotalHashtags)
        this.mDownloadButton = findViewById(R.id.igMediaDownloadButton)
        this.mRepostButton = findViewById(R.id.igMediaRepostButton)
        this.mRequestQueue = Volley.newRequestQueue(this)

        this.mHashtagText = findViewById(R.id.igMediaHashtagsText)
        this.mHashtagButton = findViewById(R.id.igMediaHashtagsButton)
        this.mCaptionText = findViewById(R.id.igMediaCaptionText)
        this.mCaptionButton = findViewById(R.id.igMediaCaptionButton)
        this.mMentionText = findViewById(R.id.igMediaMentionText)
        this.mMentionButton = findViewById(R.id.igMediaMentionButton)
        this.mCaptionAllText = findViewById(R.id.igMediaCaptionAllText)
        this.mCaptionAllButton = findViewById(R.id.igMediaCaptionAllButton)

        mQuery = ""
        mMedia = ""
        mUsername = ""
        mProfileUrl = ""
        mHashtags = ""
        mCaption = ""
        mMention = ""
        mCaptionAll = ""

        this.dh = DownloadHelper(this)
        this.ls = LocalStorage(this)
        this.db = DatabaseHelper(this, null)
    }

    private fun main() {

        topAppBar.setNavigationOnClickListener {
            finish()
        }

        topAppBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.igMediaDl -> {
                    if (ls.getValueBoolean("isAutoDownloadEnabled")) {
                        Toast.makeText(this, "Auto downloaded mode", Toast.LENGTH_SHORT).show()
                    } else {
                        downloadInstagramMedia(this.mUsername)
                    }
                    true
                }
                R.id.igMediaRepost -> {
                    processRepost()
                    true
                }
                else -> false
            }
        }

        val intent = intent
        val extra = intent.extras
        mDownloadButton.visibility = View.GONE
        mRepostButton.visibility = View.GONE
        if (extra != null) {
            val query = intent.getStringExtra("query").toString()
            getInstagramMedia(query)
        } else {
            Toast.makeText(this, "Invalid media", Toast.LENGTH_SHORT).show()
        }

        if (ls.getValueBoolean("isAutoDownloadEnabled")) {
            this.mAutoDownloadBanner.visibility = View.VISIBLE
        } else {
            this.mAutoDownloadBanner.visibility = View.GONE
        }

        this.mDownloadButton.setOnClickListener {
            if (ls.getValueBoolean("isAutoDownloadEnabled")) {
                Toast.makeText(this, "Auto downloaded mode", Toast.LENGTH_SHORT).show()
            } else {
                downloadInstagramMedia(this.mUsername)
            }
        }

        this.mRepostButton.setOnClickListener {
            processRepost()
        }

        this.mHashtagButton.setOnClickListener {
            copyToClipboard(mHashtags, "Hashtag(s)")
        }

        this.mCaptionButton.setOnClickListener {
            copyToClipboard(mCaption, "Caption")
        }

        this.mMentionButton.setOnClickListener {
            copyToClipboard(mMention, "Mention(s)")
        }

        this.mCaptionAllButton.setOnClickListener {
            copyToClipboard(mCaptionAll, "Caption")
        }
    }

    private fun processRepost() {
        val media = JSONArray(mMedia)
        val m = media.getJSONObject(0)

        if (m.getString("type") == "mp4") {
            Toast.makeText(this, "Currently only image repost supported", Toast.LENGTH_SHORT).show()
        } else {
            val i = Intent(this, InstagramRepostActivity::class.java)
            i.putExtra("username", mUsername)
            i.putExtra("url", m.getString("url"))
            i.putExtra("profile", mProfileUrl)

            if (m.getJSONObject("dimensions").getInt("height") > 1080) {
                i.putExtra("type", "por")
            } else {
                i.putExtra("type", "sq")
            }
            startActivity(i)
        }
    }

    private fun downloadInstagramMedia(username: String) {
        processDownload(username, JSONArray(this.mMedia))
    }

    private fun setDownload(data: JSONObject) {
        val media: JSONArray = JSONArray()
        var type = "jpg"

        if (data.getString("__typename") == "GraphSidecar") {
            val me = data.getJSONObject("edge_sidecar_to_children")
                .getJSONArray("edges")
            (0 until me.length()).forEach { s ->
                var item: JSONObject = JSONObject()
                item.put("name", data.getJSONObject("owner").getString("username"))
                val n = me.getJSONObject(s).getJSONObject("node")
                if (n.has("is_video") && n.getBoolean("is_video")) {
                    type = "mp4"
                    item.put("url", n.getString("video_url"))
                } else {
                    item.put("url", n.getString("display_url"))
                }
                item.put("type", type)
                media.put(item)
            }
        } else {
            var item: JSONObject = JSONObject()
            item.put("name", data.getJSONObject("owner").getString("username"))
            if (data.has("is_video") && data.getBoolean("is_video")) {
                type = "mp4"
                item.put("url", data.getString("video_url"))
            } else {
                item.put("url", data.getString("display_url"))
            }
            item.put("type", type)
            media.put(item)
        }

        this.mMedia = media.toString()
        Log.e("BOT", this.mMedia)
    }

    private fun processDownload(username: String, m: JSONArray) {
        Toast.makeText(this, "Download started", Toast.LENGTH_SHORT).show()
        (0 until m.length()).forEach { i ->
            val media = m.getJSONObject(i)
            val title = if (media.getString("type") == "mp4") {
                username + "__" + media.getString("name") + ".mp4"
            } else {
                username + "__" + media.getString("name") + ".jpg"
            }
            dh.download(
                media.getString("url"),
                title,
                media.getString("name"),
                "Instagram/$username"
            )
        }
    }

    private fun getInstagramMedia(query: String) {
        MainScope().launch {
            val r = async(Dispatchers.IO) {
                InstagramApi(this@InstagramMediaActivity).getSingleMedia(query)
            }

            try {
                val data =
                    JSONObject(r.await()).getJSONObject("graphql").getJSONObject("shortcode_media")
                setInstagramMedia(data)
            } catch (e: JSONException) {
                //
            }
        }
    }

    private fun copyToClipboard(text: String, label: String) {
        this.clipboardManager =
            getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        this.clipData = ClipData.newPlainText(label, text)
        this.clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(this, "$label copied", Toast.LENGTH_SHORT).show()
    }

    private fun setInstagramMedia(result: JSONObject) {
        try {
            val socialMediaHelper = SocialMediaHelper()

            val totalLikes = result.getJSONObject("edge_media_preview_like").getInt("count")
            val totalComments = result.getJSONObject("edge_media_preview_comment").getInt("count")
            val caption =
                result.getJSONObject("edge_media_to_caption").getJSONArray("edges").getJSONObject(0)
                    .getJSONObject("node").getString("text")
            val splitCaption = caption.split("#")
            val totalHashtags = splitCaption.size - 1
            val totalTaggedUser =
                result.getJSONObject("edge_media_to_tagged_user").getJSONArray("edges").length()
            val captions = caption.split(" ")
            var hashtags = ""

            (0 until captions.count()).forEach { c ->
                if (captions[c].startsWith("#")) {
                    hashtags = hashtags + captions[c] + " "
                }
            }

            (0 until captions.count()).forEach { m ->
                if (captions[m].startsWith("@")) {
                    mMention = mMention + captions[m] + " "
                }
            }

            mCaptionAll = caption
            mHashtags = hashtags
            mCaption = caption.split("\n")[0]

            setDownload(result)

            runOnUiThread {
                Glide.with(this)
                    .load(result.getString("display_url"))
                    .placeholder(R.drawable.placeholder_square)
                    .into(this.mImageHolder)

                this.mHashtagText.text = hashtags
                this.mCaptionText.text = mCaption
                this.mMentionText.text = mMention
                this.mCaptionAllText.text = mCaptionAll
                this.mTotalLikesText.text = socialMediaHelper.getPrettyNumber(totalLikes.toInt())
                this.mTotalCommentsText.text =
                    socialMediaHelper.getPrettyNumber(totalComments.toInt())
                this.mTotalHashtagsText.text = totalHashtags.toString()
                this.mTotalMentionsText.text = totalTaggedUser.toString()
            }
        } catch (e: JSONException) {
            //
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instagram_media)
        init()
        main()
    }

    override fun onResume() {
        super.onResume()
        AppHelper(this).preLaunchTask()
    }
}