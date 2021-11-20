package `in`.planckstudio.foss.bot.ui

import `in`.planckstudio.foss.bot.helper.DownloadHelper
import `in`.planckstudio.foss.bot.util.LocalStorage
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textview.MaterialTextView
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class CommonDownloadActivity : AppCompatActivity() {

    private lateinit var mImageHolder: ImageView
    private lateinit var mDownloadButton: ExtendedFloatingActionButton
    private lateinit var mAutoDownloadBanner: MaterialTextView
    private lateinit var mRequestQueue: RequestQueue
    private lateinit var mQuery: String
    private lateinit var dh: DownloadHelper
    private lateinit var ls: LocalStorage
    private var source = ""
    private var title = ""
    private var q = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_common_download)

        this.mImageHolder = findViewById(R.id.defaultSquareImageHolder)
        this.mRequestQueue = Volley.newRequestQueue(this)
        mDownloadButton = findViewById(R.id.commonDownloadButton)
        mAutoDownloadBanner = findViewById(R.id.commonAutoDownloadBanner)
        mQuery = ""
        this.dh = DownloadHelper(this)
        this.ls = LocalStorage(this)

        val intent = intent
        val extra = intent.extras
        mDownloadButton.visibility = View.GONE

        if (extra != null) {
            val query = intent.getStringExtra("query").toString()
            this.q = query
            getCommonData(query)
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

    }

    private fun getCommonData(query: String) {
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
                                .getJSONObject("common")
                                .getJSONObject("scrape")
                                .getJSONObject("data")

                            val thumb = result.getString("thumb")
                            this.title = result.getString("title")

                            Glide.with(this)
                                .load(thumb)
                                .placeholder(R.drawable.placeholder_square)
                                .into(this.mImageHolder)

                            if (ls.getValueBoolean("isAutoDownloadEnabled")) {
                                downloadData(result.getJSONArray("media"))
                            } else {
                                this.source = result.getJSONArray("media").toString()
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
                jsonQuery.put("service", "common")
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

    private fun downloadData(m: JSONArray) {
        (0 until m.length()).forEach { i ->
            val media = m.getJSONObject(i)
            val title = media.getString("name") + "__" + media.getString("type")
            dh.download(
                media.getString("url"),
                title,
                this.title,
                "Common"
            )
        }
    }
}