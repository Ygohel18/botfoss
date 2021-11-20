@file:Suppress("DEPRECATION")

package `in`.planckstudio.foss.bot.ui.instagram

import `in`.planckstudio.foss.bot.SearchModel
import `in`.planckstudio.foss.bot.adapter.OnGridImageItemClickListner
import `in`.planckstudio.foss.bot.adapter.SocialPostAdapter
import `in`.planckstudio.foss.bot.api.InstagramApi
import `in`.planckstudio.foss.bot.helper.*
import `in`.planckstudio.foss.bot.model.FavouriteModel
import `in`.planckstudio.foss.bot.model.SocialPostModel
import `in`.planckstudio.foss.bot.util.LocalStorage
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import `in`.planckstudio.foss.bot.R
import com.android.volley.Response
import com.android.volley.RequestQueue
import com.android.volley.AuthFailureError
import com.android.volley.RetryPolicy
import com.android.volley.DefaultRetryPolicy
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import kotlinx.coroutines.*
import org.json.JSONException
import org.json.JSONObject
import java.lang.Runnable

@DelicateCoroutinesApi
class InstagramProfileActivity : AppCompatActivity(), OnGridImageItemClickListner {

    private lateinit var layout: RelativeLayout
    private lateinit var imageHolder: MaterialCardView

    private lateinit var mInstagramApi: InstagramApi
    private lateinit var mStarButton: MaterialCardView
    private lateinit var mStarIcon: ImageView
    private lateinit var mProfileImg: ImageView
    private lateinit var mUsernameTxt: TextView
    private lateinit var mNameTxt: TextView
    private lateinit var mBioTxt: TextView
    private lateinit var mPostTxt: TextView
    private lateinit var mFollowersTxt: TextView
    private lateinit var mFollowingTxt: TextView
    private lateinit var mEngageTxt: TextView
    private lateinit var mLatestPostList: RecyclerView
    private lateinit var mQuery: String
    private lateinit var mUserId: String

    private lateinit var mDownloadProfileButton: Chip
    private lateinit var mDownloadAllPostsButton: Chip
    private lateinit var mDownloadLatestPostsButton: Chip
    private lateinit var mDownloadStoryButton: Chip

    private lateinit var dh: DownloadHelper
    private lateinit var ls: LocalStorage
    private lateinit var mRequestQueue: RequestQueue
    private lateinit var mFavouriteModel: FavouriteModel
    private lateinit var db: DatabaseHelper

    private var mSocialPostList = ArrayList<SocialPostModel>()
    private var mSocialPostAdapter = SocialPostAdapter(mSocialPostList, this)
    private lateinit var mSocialPostModel: SocialPostModel

    private lateinit var mProfilePictureUrl: String
    private var mTotalPosts: Int = 0
    private lateinit var mUsername: String

    private fun init() {
        AppHelper(this).preLaunchTask()

        this.layout = findViewById(R.id.igProfileLayout)
        this.imageHolder = findViewById(R.id.igProfileImageHolder)
        this.mRequestQueue = Volley.newRequestQueue(this)
        this.db = DatabaseHelper(this, null)
        this.dh = DownloadHelper(this)
        this.ls = LocalStorage(this)
        this.mInstagramApi = InstagramApi(this)

        val currentInstagramUser = ls.getValueString("currentActiveInstagram")

        val igCookie = db.getSessionValue("instagram", currentInstagramUser, "cookie")
        mInstagramApi.setCookie(igCookie)

        mQuery = ""
        mUserId = ""
        mProfilePictureUrl = ""
        mUsername = ""
        this.mStarIcon = findViewById(R.id.igProfileFavIcon)
        this.mStarButton = findViewById(R.id.igProfileStarButton)

        this.mProfileImg = findViewById(R.id.ig_profile_img)
        this.mUsernameTxt = findViewById(R.id.ig_username)
        this.mNameTxt = findViewById(R.id.ig_name)
        this.mBioTxt = findViewById(R.id.ig_bio)
        this.mPostTxt = findViewById(R.id.ig_post)
        this.mFollowersTxt = findViewById(R.id.ig_followers)
        this.mFollowingTxt = findViewById(R.id.ig_following)
        this.mEngageTxt = findViewById(R.id.igProfileEngageTxt)
        this.mLatestPostList = findViewById(R.id.ig_latest_post_list)

        this.mDownloadProfileButton = findViewById(R.id.igProfileDownloadDpButton)
        this.mDownloadAllPostsButton = findViewById(R.id.igProfileDownloadAllButton)
        this.mDownloadLatestPostsButton = findViewById(R.id.igProfileDownloadLatestButton)
        this.mDownloadStoryButton = findViewById(R.id.igProfileDownloadStoryButton)
    }

    override fun onResume() {
        super.onResume()
        AppHelper(this).preLaunchTask()
    }

    private fun main() {
        val intent = intent
        val extra = intent.extras
        mStarButton.visibility = View.GONE
        mDownloadProfileButton.visibility = View.GONE
        mDownloadAllPostsButton.visibility = View.GONE
        mDownloadLatestPostsButton.visibility = View.GONE
        mDownloadStoryButton.visibility = View.GONE
        this.mFavouriteModel = FavouriteModel()

        if (extra != null) {
            val query = intent.getStringExtra("query").toString()
            setInstagramProfile(query)
        } else {
            Toast.makeText(this, "Invalid username", Toast.LENGTH_SHORT).show()
        }

        this.mDownloadProfileButton.setOnClickListener {
            startDownload("profile")
        }

        this.mDownloadAllPostsButton.setOnClickListener {
            showConfirmDownloadDialog(mTotalPosts)
        }

        this.mDownloadLatestPostsButton.setOnClickListener {
            startDownload("latest")
        }

        this.mDownloadStoryButton.setOnClickListener {
            startDownload("storyAll")
        }

        this.mStarButton.setOnClickListener {
            if (this.mFavouriteModel.getFavouriteValue() == 0) {
                this.mFavouriteModel.setFavouriteValue(1)
                setStar(true)
                db.setFavourite(this.mFavouriteModel.getFavouriteId())
                Toast.makeText(
                    this,
                    "${this.mFavouriteModel.getFavouriteName()} added to favourite",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                this.mFavouriteModel.setFavouriteValue(0)
                setStar(false)
                db.unsetFavourite(this.mFavouriteModel.getFavouriteId())
                Toast.makeText(
                    this,
                    "${this.mFavouriteModel.getFavouriteName()} removed from favourite",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun startDownload(type: String) {
        processDownload(type)
    }

    private fun processDownload(type: String) {
        Toast.makeText(this, "Task started", Toast.LENGTH_LONG).show()
        Thread {
            when (type) {
                "profile" -> {
                    val title = this.mUsername + ".jpg"
                    this.dh.download(
                        this.mProfilePictureUrl,
                        title,
                        "Profile picture ${this.mUsername}"
                    )
                }
                "all" -> {
                    apiFetchAllPost(this.mUsername, false)
                }
                "latest" -> {
                    apiFetchAllPost(this.mUsername, true)
                }
                "storyAll" -> {
                    apiFetchAllStory(this.mUserId)
                }
            }
        }.start()
    }

    private fun apiFetchAllStory(id: String) {
        val stringRequest = object : StringRequest(
            Method.GET,
            "https://i.instagram.com/api/v1/feed/reels_media/?reel_ids=$id",
            Response.Listener<String> {
                val data = JSONObject(it)
                this.ls.save("storyMedia", it.toString())
                val items = data.getJSONObject("reels").getJSONObject(id).getJSONArray("items")

                (0 until items.length()).forEach { i ->
                    val s = items.getJSONObject(i)
                    val sid = s.getString("id")
                    val stype = s.getInt("media_type")
                    var mytitle = ""
                    var url = ""

                    when (stype) {
                        1 -> {
                            url = s.getJSONObject("image_versions2").getJSONArray("candidates")
                                .getJSONObject(0).getString("url")
                            mytitle = this.mUsername + "__" + sid + ".jpg"
                        }
                        2 -> {
                            url = s.getJSONArray("video_versions").getJSONObject(0)
                                .getString("url")
                            mytitle = this.mUsername + "__" + sid + ".mp4"
                        }
                    }

                    val dirList = "Instagram/" + this.mUsername

                    dh.download(
                        url,
                        mytitle,
                        "Instagram Story(s) $mUsername",
                        dirList
                    )
                }

            },
            Response.ErrorListener {
                Log.e("PBOT", "Request error")
            }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                return mInstagramApi.getDefaultHeader(mInstagramApi.getCookie())
            }
        }
        val retryPolicy: RetryPolicy = DefaultRetryPolicy(15000, 1, 1F)
        stringRequest.setShouldCache(false)
        mRequestQueue.add(stringRequest).retryPolicy = retryPolicy
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instagram_profile)
        init()
        main()
    }

    private fun apiFetchAllPost(username: String, latest: Boolean, next: String = "") {
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
                            val media = result.getJSONArray("download")

                            val handler = Handler()

                            Thread {
                                handler.post(Runnable {
                                    (0 until media.length()).forEach { i ->
                                        val mediaItem = media.getJSONArray(i)

                                        (0 until mediaItem.length()).forEach { ii ->
                                            val mi = mediaItem.getJSONObject(ii)

                                            val id = mi.getString("name")
                                            val url = mi.getString("url")
                                            val type = mi.getString("type")
                                            var mytitle: String = ""

                                            if (type == "mp4") {
                                                mytitle = username + "__" + id + ".mp4"
                                            } else if (type == "jpg") {
                                                mytitle = username + "__" + id + ".jpg"
                                            }

                                            val dirList = "Instagram/$username"

                                            dh.download(
                                                url,
                                                mytitle,
                                                "Instagram Media $username",
                                                dirList
                                            )
                                        }
                                    }
                                })
                            }.start()
                            if (!latest) {
                                if (hasNext) {
                                    apiFetchAllPost(username, false, endCursor)
                                }
                            }
                        }
                    }
                } catch (e: JSONException) {
                    //
                }
            },
            Response.ErrorListener {
                //
            }) {
            override fun getBody(): ByteArray {
                val jsonBody = JSONObject()
                val jsonQuery = JSONObject()
                jsonQuery.put("service", "instagram")
                jsonQuery.put("type", "allmedia")
                jsonQuery.put("key", username)
                jsonQuery.put("end_cursour", next)
                jsonQuery.put("mode", "download")

                if (latest) {
                    jsonQuery.put("first", 12)
                } else {
                    jsonQuery.put("first", 50)
                }

                if (ls.getValueBoolean("ig_connected")) {
                    jsonQuery.put("igSession", ls.getValueString("igSession"))
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
        val retryPolicy: RetryPolicy = DefaultRetryPolicy(15000, 3, 1F)
        stringRequest.setShouldCache(false)
        mRequestQueue.add(stringRequest).retryPolicy = retryPolicy
    }

    private fun openPreviewImageDialog(url: String) {
        val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_preview_holder, null)
        val img = view.findViewById<ImageView>(R.id.dialog_image_holder)
        Glide.with(img.context).load(url).into(img)
        view.findViewById<ImageView>(R.id.dialog_image_holder)
        AlertDialog.Builder(this).setView(view).show()
    }

    override fun onItemClick(item: SocialPostModel, position: Int) {
        //openPreviewImageDialog(item.getPostSrcUrl())
        val i = Intent(this, InstagramMediaActivity::class.java)
        val query = "https://www.instagram.com/p/" + item.getPostId() + "/"
        i.putExtra("query", item.getPostId())
        startActivity(i)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setInstagramProfile(username: String) {
        MainScope().launch {
            val r = async(Dispatchers.IO) {
                InstagramApi(this@InstagramProfileActivity).getUserBasicInfo(username)
            }

            try {

                val result = JSONObject(r.await()).getJSONObject("graphql").getJSONObject("user")

                mProfilePictureUrl = result.getString("profile_pic_url_hd")
                mUsername = result.getString("username")
                result.getString("id")
                mUserId = result.getString("id")

                var latestLikes = 0
                var latestComments = 0
                var totalFollowers = 0
                var totalFollowings = 0
                var engage: Float = 0f

                totalFollowings = result.getJSONObject("edge_follow").getInt("count")
                totalFollowers = result.getJSONObject("edge_followed_by").getInt("count")

                if (!(ls.getValueBoolean("isPrivacyModeEnabled"))) {
                    val searchModel = SearchModel()
                    searchModel.setSearchService("instagram")
                    searchModel.setSearchType("profile")
                    searchModel.setSearchKey(username)
                    searchModel.setSearchValue(result.toString())
                    searchModel.setSearchImage(mProfilePictureUrl)
                    db.addSearch(searchModel)
                }

                if (checkFav(mUsername) == 0) {
                    setStar(false)
                    mFavouriteModel = FavouriteModel(
                        "instagram",
                        mUsername,
                        1
                    )
                    db.addFavourite(mFavouriteModel)
                    setStar(true)
                } else {
                    setStar(true)
                }

                runOnUiThread {
                    val mLayoutManager =
                        GridLayoutManager(
                            applicationContext,
                            3,
                            GridLayoutManager.VERTICAL,
                            false
                        )

                    Glide.with(this@InstagramProfileActivity)
                        .load(mProfilePictureUrl)
                        .placeholder(R.drawable.placeholder_thumb)
                        .circleCrop()
                        .into(mProfileImg)

                    imageHolder.isDrawingCacheEnabled = true
                    imageHolder.buildDrawingCache()
                    val bitmap: Bitmap = Bitmap.createBitmap(imageHolder.drawingCache)
                    val palette: Palette =
                        Palette.from(
                            Bitmap.createScaledBitmap(
                                bitmap, 100, 100, true
                            )
                        ).generate()

                    // this.layout.setBackgroundColor(palette.getLightVibrantColor(resources.getColor(R.color.white)))

                    mNameTxt.setTextColor(
                        palette.getDarkMutedColor(
                            resources.getColor(
                                R.color.text_color
                            )
                        )
                    )
                    mUsernameTxt.setTextColor(
                        palette.getDarkMutedColor(
                            resources.getColor(
                                R.color.text_color
                            )
                        )
                    )
                    mPostTxt.setTextColor(
                        palette.getDarkMutedColor(
                            resources.getColor(
                                R.color.text_color
                            )
                        )
                    )
                    mFollowersTxt.setTextColor(
                        palette.getDarkMutedColor(
                            resources.getColor(
                                R.color.text_color
                            )
                        )
                    )
                    mFollowingTxt.setTextColor(
                        palette.getDarkMutedColor(
                            resources.getColor(
                                R.color.text_color
                            )
                        )
                    )
                    mEngageTxt.setTextColor(
                        palette.getDarkMutedColor(
                            resources.getColor(
                                R.color.text_color
                            )
                        )
                    )

                    mLayoutManager.isUsingSpansToEstimateScrollbarDimensions = false
                    mLatestPostList.layoutManager = mLayoutManager

                    val socialMediaHelper = SocialMediaHelper()

                    mLatestPostList.adapter = mSocialPostAdapter
                    mSocialPostAdapter.notifyDataSetChanged()

                    mNameTxt.text = "@" + mUsername
                    mUsernameTxt.text = result.getString("full_name")
                    mBioTxt.text = result.getString("biography")
                    mTotalPosts =
                        result.getJSONObject("edge_owner_to_timeline_media").getInt("count")
                    mPostTxt.text =
                        socialMediaHelper.getPrettyNumber(mTotalPosts)

                    mFollowersTxt.text = socialMediaHelper.getPrettyNumber(
                        totalFollowers
                    )
                    mFollowingTxt.text = socialMediaHelper.getPrettyNumber(
                        totalFollowings
                    )
                }

                mProfileImg.setOnClickListener {
                    openPreviewImageDialog(mProfilePictureUrl)
                }

                val media =
                    result.getJSONObject("edge_owner_to_timeline_media").getJSONArray("edges")

                (0 until media.length()).forEach { i ->
                    val mediaItem =
                        media.getJSONObject(i).getJSONObject("node")

                    mSocialPostModel = SocialPostModel()
                    mSocialPostModel.setPostId(mediaItem.getString("shortcode"))
                    mSocialPostModel.setPostSrcUrl(mediaItem.getString("display_url"))
                    mSocialPostModel.setPostThumbUrl(
                        mediaItem.getJSONArray("thumbnail_resources").getJSONObject(0)
                            .getString("src")
                    )
                    mSocialPostModel.setPostMediaType(mediaItem.getString("__typename"))
                    mSocialPostList.add(mSocialPostModel)

                    runOnUiThread {
                        mSocialPostAdapter.notifyDataSetChanged()
                    }

                    latestLikes += if (mediaItem.isNull("edge_liked_by")) {
                        0
                    } else {
                        mediaItem.getJSONObject("edge_liked_by").getInt("count")
                    }

                    latestComments += if (mediaItem.isNull("edge_media_to_comment")) {
                        0
                    } else {
                        mediaItem.getJSONObject("edge_media_to_comment").getInt("count")
                    }
                }

                runOnUiThread {
                    mDownloadProfileButton.visibility = View.VISIBLE
                    mDownloadAllPostsButton.visibility = View.VISIBLE
                    mDownloadLatestPostsButton.visibility = View.VISIBLE
                    mDownloadStoryButton.visibility = View.VISIBLE
                    mStarButton.visibility = View.VISIBLE

                    engage =
                        ((latestLikes + latestComments / 12).toFloat() / totalFollowers)
                    mEngageTxt.text = String.format("%.02f", engage)
                }
            } catch (e: JSONException) {
                //
            }
        }
    }

    private fun setStar(flag: Boolean = false) {
        if (flag) {
            this.mStarIcon.setImageResource(R.drawable.ic_heart_red)
        } else {
            this.mStarIcon.setImageResource(R.drawable.ic_heart_disabled)
        }
    }

    @SuppressLint("Range")
    private fun checkFav(username: String): Int {
        val cursor = this.db.getFavourite("instagram", username)
        return if (cursor != null && cursor.moveToFirst()) {
            cursor.moveToFirst()
            this.mFavouriteModel.setFavouriteID(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_FAVOURITE_ID)))
            this.mFavouriteModel.setFavouriteType(
                cursor.getString(
                    cursor.getColumnIndex(
                        DatabaseHelper.COLUMN_FAVOURITE_TYPE
                    )
                )
            )
            this.mFavouriteModel.setFavouriteName(
                cursor.getString(
                    cursor.getColumnIndex(
                        DatabaseHelper.COLUMN_FAVOURITE_NAME
                    )
                )
            )
            this.mFavouriteModel.setFavouriteValue(
                cursor.getInt(
                    cursor.getColumnIndex(
                        DatabaseHelper.COLUMN_FAVOURITE_VALUE
                    )
                )
            )
            this.mFavouriteModel.getFavouriteValue()
        } else {
            0
        }
    }

    private fun showConfirmDownloadDialog(total: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm download all posts")
        builder.setMessage("Total $total posts")
        builder.setPositiveButton("DOWNLOAD") { dialogInterface, which ->
            startDownload("all")
        }
        builder.setNegativeButton("CANCEL") { dialogInterface, which ->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    private fun showMessage(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }
}