package `in`.planckstudio.foss.bot.ui

import `in`.planckstudio.foss.bot.MainActivity
import `in`.planckstudio.foss.bot.R
import `in`.planckstudio.foss.bot.helper.AppHelper
import `in`.planckstudio.foss.bot.ui.instagram.ConnectInstagramActivity
import `in`.planckstudio.foss.bot.ui.instagram.InstagramMediaActivity
import `in`.planckstudio.foss.bot.ui.instagram.InstagramProfileActivity
import `in`.planckstudio.foss.bot.ui.instagram.InstagramToolActivity
import `in`.planckstudio.foss.bot.ui.youtube.YoutubeDownloadActivity
import `in`.planckstudio.foss.bot.util.LocalStorage
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.DefaultRetryPolicy
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.DelicateCoroutinesApi
import org.json.JSONObject

@DelicateCoroutinesApi
class DirectActivity : AppCompatActivity() {

    private lateinit var ls: LocalStorage
    private lateinit var mRequestQueue: RequestQueue

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_direct)

        AppHelper(this).preLaunchTask()

        this.ls = LocalStorage(this)
        this.mRequestQueue = Volley.newRequestQueue(this)
        this.ls.save(
            "device_uid",
            Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        )

        val intent = intent
        val action = intent.action
        val extra = intent.extras
        val type = intent.type

        val stringRequest = object : StringRequest(
            Method.GET,
            "https://gist.githubusercontent.com/Ygohel18/48d82026f3e53120c36f71d14debea15/raw/",
            Response.Listener<String> {
                val jsonObject = JSONObject(it)
                val rStatus = jsonObject.getString("status")
                if (rStatus == "success") {

                    ls.removeValue("isUnderMaintenance")
                    ls.removeValue("redirectToClassic")

                    ls.save(
                        "isUnderMaintenance",
                        jsonObject.getBoolean("isUnderMaintenance")
                    )

                    ls.save(
                        "isUnderMaintenance",
                        jsonObject.getBoolean("redirectToClassic")
                    )

                    val isUnderMaintenance = jsonObject.getBoolean("isUnderMaintenance")

                    if (isUnderMaintenance) {
                        val i = Intent(this, UnderMaintenanceActivity::class.java)
                        startActivity(i)
                    } else {
                        if (("android.intent.action.SEND" == action && type != null && "text/plain" == type) || (extra != null)) {
                            val itext =
                                intent.getStringExtra("android.intent.extra.TEXT").toString()
                            when {
                                itext.contains("https://www.instagram.com/") -> {
                                    val fSplit = itext.split("?")
                                    val fUrl = fSplit[0]
                                    selectDownloadActivity("instagram", fUrl)
                                }
                                itext.contains("https://instagram.com/") -> {
                                    val fSplit = itext.split("?")
                                    val fUrl = fSplit[0].replace("https://instagram.com/", "")
                                    selectDownloadActivity("igprofile", fUrl)
                                }
                                itext.contains("https://youtu.be/") -> {
                                    selectDownloadActivity("youtube", itext)
                                }
                                itext.contains("https://pin.it/") -> {
                                    selectDownloadActivity("pinterest", itext)
                                }
                                itext.contains("dofollow:#") -> {
                                    val fSplit = itext.split("#")
                                    selectDownloadActivity("instagramFollowHashtagUser", fSplit[1])
                                }
                                itext.contains("doremove:") -> {
                                    val fSplit = itext.split(":")
                                    selectDownloadActivity("instagramRemove", fSplit[1])
                                }
                                itext.contains("dolike:#") -> {
                                    val fSplit = itext.split("#")
                                    selectDownloadActivity("instagramLikeHashtag", fSplit[1])
                                }
                                itext.contains("deleteallpost") -> selectDownloadActivity(
                                    "instagramDeleteAllPost",
                                    ""
                                )
                                itext.contains("unfollowuser") -> selectDownloadActivity(
                                    "instagramUnfollowUser",
                                    ""
                                )
                                itext.contains("get:#") -> {
                                    val fSplit = itext.split("#")
                                    selectDownloadActivity("instagramGetHashtag", fSplit[1])
                                }
                                itext.contains("send:") -> {
                                    val fSplit = itext.split(":")
                                    selectDownloadActivity("whatsappSend", fSplit[1])
                                }
                                itext.contains(" ") -> {
                                    selectDownloadActivity("space", "")
                                }
                                else -> {
                                    if (itext.contains("http")) {
                                        selectDownloadActivity("common", itext)
                                    } else {
                                        selectDownloadActivity("igprofile", itext)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            Response.ErrorListener {
                //
            }
        ) {
        }
        val retryPolicy: RetryPolicy = DefaultRetryPolicy(3000, 3, 1F)
        stringRequest.setShouldCache(false)
        mRequestQueue.add(stringRequest)
    }

    override fun onResume() {
        super.onResume()
        AppHelper(this).preLaunchTask()
    }

    private fun isInstagramConnected(): Boolean {
        return ls.getValueBoolean("ig_connected")
    }

    private fun checkDown(service: String): Boolean {
        val down = JSONObject(ls.getValueString("settingDown"))
        var flag = false

        when (service) {
            "instagram" -> {
                if (down.getBoolean("instagram")) {
                    val msg = "Instagram service is currently unavailable"
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    startActivity(
                        Intent(
                            this,
                            UnderMaintenanceActivity::class.java
                        ).putExtra("message", msg)
                    )
                } else {
                    flag = true
                }
            }
            "common" -> {
                if (down.getBoolean("common")) {
                    val msg = "Common service is currently unavailable"
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    startActivity(
                        Intent(
                            this,
                            UnderMaintenanceActivity::class.java
                        ).putExtra("message", msg)
                    )
                } else {
                    flag = true
                }
            }
            "pinterest" -> {
                if (down.getBoolean("pinterest")) {
                    val msg = "Pinterest service is currently unavailable"
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    startActivity(
                        Intent(
                            this,
                            UnderMaintenanceActivity::class.java
                        ).putExtra("message", msg)
                    )
                } else {
                    flag = true
                }
            }
            "twitter" -> {
                if (down.getBoolean("twitter")) {
                    val msg = "Twitter service is currently unavailable"
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    startActivity(
                        Intent(
                            this,
                            UnderMaintenanceActivity::class.java
                        ).putExtra("message", msg)
                    )
                } else {
                    flag = true
                }
            }
            "whatsapp" -> {
                if (down.getBoolean("whatsapp")) {
                    val msg = "Whatsapp service is currently unavailable"
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    startActivity(
                        Intent(
                            this,
                            UnderMaintenanceActivity::class.java
                        ).putExtra("message", msg)
                    )
                } else {
                    flag = true
                }
            }
            "youtube" -> {
                if (down.getBoolean("youtube")) {
                    val msg = "Youtube service is currently unavailable"
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    startActivity(
                        Intent(
                            this,
                            UnderMaintenanceActivity::class.java
                        ).putExtra("message", msg)
                    )
                } else {
                    flag = true
                }
            }
            "bot" -> {
                if (down.getBoolean("bot")) {
                    val msg = "Bot service is currently unavailable"
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    startActivity(
                        Intent(
                            this,
                            UnderMaintenanceActivity::class.java
                        ).putExtra("message", msg)
                    )
                } else {
                    flag = true
                }
            }
        }

        return flag
    }

    private fun selectDownloadActivity(type: String, query: String) {

        if (!checkPermission()) {
            requestPermission()
            selectDownloadActivity(type, query)
        } else {
            val down = JSONObject(ls.getValueString("settingDown"))
            checkRedirect(type, query)
            finish()
        }
    }

    private fun checkRedirect(type: String, query: String) {
        when (type) {
            "instagram" -> {
                if (checkDown("instagram")) {
                    startActivity(
                        Intent(this, InstagramMediaActivity::class.java).putExtra(
                            "query",
                            query
                        )
                    )
                }
            }
            "youtube" -> {
                if (checkDown("youtube")) {
                    startActivity(
                        Intent(
                            this,
                            YoutubeDownloadActivity::class.java
                        ).putExtra("query", query)
                    )
                }
            }
            "pinterest" -> {
                if (checkDown("pinterest")) {
                    startActivity(
                        Intent(
                            this,
                            PinterestDownloadActivity::class.java
                        ).putExtra("query", query)
                    )
                }
            }
            "common" -> {
                if (checkDown("common")) {
                    startActivity(
                        Intent(
                            this,
                            CommonDownloadActivity::class.java
                        ).putExtra("query", query)
                    )
                }
            }
            "igprofile" -> {
                if (checkDown("instagram")) {
                    startActivity(
                        Intent(
                            this,
                            InstagramProfileActivity::class.java
                        ).putExtra("query", query)
                    )
                }
            }
            "instagramRemove" -> {
                if (checkDown("instagram")) {
                    startActivity(
                        Intent(
                            this,
                            InstagramToolActivity::class.java
                        ).putExtra("task", "instagramRemove").putExtra("query", query)
                    )
                }
            }
            "instagramUnfollowUser" -> {
                if (checkDown("instagram")) {
                    if (ls.getValueBoolean("isIgCustomSessionEnabled") || isInstagramConnected()) {
                        startActivity(
                            Intent(
                                this,
                                InstagramToolActivity::class.java
                            ).putExtra("task", "instagramUnfollowUser").putExtra("query", query)
                        )
                    } else {
                        startActivity(
                            Intent(
                                this,
                                ConnectInstagramActivity::class.java
                            )
                        )
                    }
                }
            }
            "instagramDeleteAllPost" -> {
                if (checkDown("instagram")) {
                    if (ls.getValueBoolean("isIgCustomSessionEnabled") || isInstagramConnected()) {
                        startActivity(
                            Intent(
                                this,
                                InstagramToolActivity::class.java
                            ).putExtra("task", "instagramDeleteAllPost").putExtra("query", query)
                        )
                    } else {
                        startActivity(
                            Intent(
                                this,
                                ConnectInstagramActivity::class.java
                            )
                        )
                    }
                }
            }
            "instagramFollowHashtagUser" -> {
                if (checkDown("instagram")) {
                    if (ls.getValueBoolean("isIgCustomSessionEnabled") || isInstagramConnected()) {
                        startActivity(
                            Intent(
                                this,
                                InstagramToolActivity::class.java
                            ).putExtra("task", "followHashtag").putExtra("query", query)
                        )
                    } else {
                        startActivity(
                            Intent(
                                this,
                                ConnectInstagramActivity::class.java
                            )
                        )
                    }
                }
            }
            "instagramLikeHashtag" -> {
                if (checkDown("instagram")) {
                    if (ls.getValueBoolean("isIgCustomSessionEnabled") || isInstagramConnected()) {
                        startActivity(
                            Intent(
                                this,
                                InstagramToolActivity::class.java
                            ).putExtra("task", "likeHashtag").putExtra("query", query)
                        )
                    } else {
                        startActivity(
                            Intent(
                                this,
                                ConnectInstagramActivity::class.java
                            )
                        )
                    }
                }
            }
            "instagramGetHashtag" -> {
                if (checkDown("instagram")) {
                    if (ls.getValueBoolean("isIgCustomSessionEnabled") || isInstagramConnected()) {
                        startActivity(
                            Intent(
                                this,
                                InstagramToolActivity::class.java
                            ).putExtra("task", "instagramGetHashtag")
                                .putExtra("query", query)
                        )
                    } else {
                        startActivity(
                            Intent(
                                this,
                                ConnectInstagramActivity::class.java
                            )
                        )
                    }

                }
            }
            "whatsappSend" -> {
                if (checkDown("whatsapp")) {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://api.whatsapp.com/send?phone=$query&text=Hello,")
                        )
                    )
                }
            }
            "space" -> {
                Toast.makeText(this, "Unknown query or url", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
            }
            else -> {
                startActivity(Intent(this, MainActivity::class.java))
            }
        }
    }

    private fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= 23) {
            val result: Int = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            result == PackageManager.PERMISSION_GRANTED
        } else true
    }

    @Throws(Exception::class)
    fun requestPermission() {
        try {
            val code = 27
            ActivityCompat.requestPermissions(
                (this as Activity?)!!,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                code
            )
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}