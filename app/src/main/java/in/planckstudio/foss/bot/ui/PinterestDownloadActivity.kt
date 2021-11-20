package `in`.planckstudio.foss.bot.ui

import `in`.planckstudio.foss.bot.helper.AppHelper
import `in`.planckstudio.foss.bot.helper.DownloadHelper
import `in`.planckstudio.foss.bot.util.LocalStorage
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
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
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.DelicateCoroutinesApi
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

@DelicateCoroutinesApi
class PinterestDownloadActivity : AppCompatActivity() {

    private lateinit var mImageHolder: ImageView
    private lateinit var mTypeText: MaterialTextView
    private lateinit var mMuteText: MaterialTextView
    private lateinit var mTitleText: MaterialTextView
    private lateinit var mSizeText: MaterialTextView
    private lateinit var mQualityText: MaterialTextView
    private lateinit var mDownloadButton: ExtendedFloatingActionButton
    private lateinit var mAutoDownloadBanner: MaterialTextView
    private lateinit var clipboardManager: ClipboardManager
    private lateinit var clipData: ClipData
    private lateinit var mRequestQueue: RequestQueue
    private lateinit var mQuery: String
    private lateinit var mTitleButton: MaterialCardView
    private lateinit var dh: DownloadHelper
    private lateinit var ls: LocalStorage
    private var pin = ""
    private var source = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pinterest_download)
        AppHelper(this).preLaunchTask()
        this.mImageHolder = findViewById(R.id.defaultSquareImageHolder)
        this.mRequestQueue = Volley.newRequestQueue(this)
        mTypeText = findViewById(R.id.pinTypeText)
        mMuteText = findViewById(R.id.pinMuteText)
        mTitleText = findViewById(R.id.pinTitleText)
        mSizeText = findViewById(R.id.pinSizeText)
        mQualityText = findViewById(R.id.pinQualityText)
        mDownloadButton = findViewById(R.id.pinDownloadButton)
        mAutoDownloadBanner = findViewById(R.id.pinAutoDownloadBanner)
        mTitleButton = findViewById(R.id.pinTitleButton)
        mQuery = ""
        this.dh = DownloadHelper(this)
        this.ls = LocalStorage(this)

        val intent = intent
        val extra = intent.extras
        mDownloadButton.visibility = View.GONE

        if (extra != null) {
            val query = intent.getStringExtra("query").toString()
            this.pin = query
            getPinterestData(query)
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
                downloadData(JSONArray(this.source))
            }
        }

        this.mTitleButton.setOnClickListener {
            this.clipboardManager =
                getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            this.clipData = ClipData.newPlainText("title", this.mTitleText.text)
            this.clipboardManager.setPrimaryClip(clipData)
            Toast.makeText(this, "Title copied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun downloadData(m: JSONArray) {
        (0 until m.length()).forEach { i ->
            val media = m.getJSONObject(i)
            val title = "pin__." + media.getString("type")
            dh.download(
                media.getString("url"),
                title,
                pin,
                "Pinterest"
            )
        }
    }

    private fun getPinterestData(query: String) {
        Log.e("BOT", query)
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
                                .getJSONObject("pinterest")
                                .getJSONObject("scrape")

                            val title = result.getString("title")

                            val info = result.getJSONArray("links").getJSONObject(0)
                            this.source = result.getJSONArray("links").toString()
                            this.mTypeText.text = info.getString("type")
                            this.mQualityText.text = info.getString("quality")
                            this.mSizeText.text = info.getString("size")
                            this.mMuteText.text = info.getString("mute")
                            this.mTitleText.text = title

                            Glide.with(this)
                                .load(result.getString("thumbnail"))
                                .placeholder(R.drawable.placeholder_square)
                                .into(this.mImageHolder)

                            this.clipboardManager =
                                getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            this.clipData = ClipData.newPlainText("title", title)
                            this.clipboardManager.setPrimaryClip(clipData)
                            if (ls.getValueBoolean("isAutoDownloadEnabled")) {
                                downloadData(result.getJSONArray("links"))
                            }
                            mDownloadButton.visibility = View.VISIBLE
                        }
                    }

                } catch (e: JSONException) {
                    Toast.makeText(this, "Failed to filter data", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener {
                Toast.makeText(this, "Something goes wrong", Toast.LENGTH_SHORT).show()
            }) {

            override fun getBody(): ByteArray {

                val jsonBody = JSONObject()
                val jsonQuery = JSONObject()

                jsonQuery.put("key", query)
                jsonQuery.put("service", "pinterest")
                jsonQuery.put("type", "video")
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

    override fun onResume() {
        super.onResume()
        AppHelper(this).preLaunchTask()
    }
}