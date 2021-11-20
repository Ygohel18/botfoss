package `in`.planckstudio.foss.bot.ui

import `in`.planckstudio.foss.bot.MainActivity
import `in`.planckstudio.foss.bot.R
import `in`.planckstudio.foss.bot.helper.AppHelper
import `in`.planckstudio.foss.bot.helper.DatabaseHelper
import `in`.planckstudio.foss.bot.ui.instagram.ConnectInstagramActivity
import `in`.planckstudio.foss.bot.util.LocalStorage
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.Settings.Secure
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.DelicateCoroutinesApi
import org.json.JSONObject

@DelicateCoroutinesApi
class SplashScreen : AppCompatActivity() {
    private lateinit var ls: LocalStorage
    private lateinit var mRequestQueue: RequestQueue
    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        this.mRequestQueue = Volley.newRequestQueue(this)
        this.ls = LocalStorage(this)
        init()

        val stringRequest = object : StringRequest(
            Method.GET,
            "https://bot.planckstudio.in/.well-known/android/appflags.json",
            Response.Listener<String> {
                val jsonObject = JSONObject(it)
                val rStatus = jsonObject.getString("status")
                if (rStatus == "success") {
                    this.ls.save(
                        "force_webview",
                        jsonObject.getBoolean("force_webview")
                    )
                    this.ls.save(
                        "is_ig_connect_open",
                        jsonObject.getBoolean("is_ig_connect_open")
                    )
                    this.ls.save(
                        "is_yt_connect_open",
                        jsonObject.getBoolean("is_ig_connect_open")
                    )
                    this.ls.save(
                        "is_ig_connect_require",
                        jsonObject.getBoolean("is_ig_connect_require")
                    )
                    this.ls.save(
                        "is_force_ig_connect_require",
                        jsonObject.getBoolean("is_force_ig_connect_require")
                    )
                }
            },
            Response.ErrorListener {
                //
            }
        ) {
        }
        mRequestQueue.add(stringRequest)
    }

    @SuppressLint("StringFormatInvalid", "HardwareIds")
    private fun init() {
        this.ls.save("device_uid", Secure.getString(contentResolver, Secure.ANDROID_ID))
        AppHelper(this).saveOId()

        if (AppHelper(this).isRooted()) {
            startActivity(
                Intent(this, UnderMaintenanceActivity::class.java).putExtra(
                    "message",
                    "Sorry superuser"
                ).setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                )
            )
        } else {
            isFirstLaunch()
            getSettings()
            getPromo()
            db = DatabaseHelper(this, null)
        }
    }

    private fun isFirstLaunch() {
        val fl = ls.getValueBoolean("isFirstLaunch")
        if (!fl) {
            this.ls.save("isFirstLaunch", true)
            this.ls.save("isAdsEnabled", true)
            this.ls.save("isPremiumEnabled", false)
        }
    }

    private fun checkPremium() {
        ls.save("isDisableAdsEnabled", ls.getValueBoolean("isUserPremium"))
        ls.save("isPremiumEnabled", ls.getValueBoolean("isUserPremium"))
    }

    private fun getSettings() {
        val stringRequest = object : StringRequest(
            Method.GET,
            "https://bot.planckstudio.in/.well-known/android/appsetting.json",
            Response.Listener<String> {
                val jsonObject = JSONObject(it)
                val rStatus = jsonObject.getString("status")
                if (rStatus == "success") {

                    ls.removeValue("isUnderMaintenance")
                    ls.removeValue("redirectToClassic")
                    ls.removeValue("settingDown")

                    ls.save(
                        "isUnderMaintenance",
                        jsonObject.getBoolean("isUnderMaintenance")
                    )
                    ls.save(
                        "settingDown",
                        jsonObject.getJSONObject("down").toString()
                    )
                    ls.save(
                        "isUnderMaintenance",
                        jsonObject.getBoolean("redirectToClassic")
                    )

                    appContinue(
                        jsonObject.getBoolean("isUnderMaintenance")
                    )
                }
            },
            Response.ErrorListener {
                //
            }
        ) {
        }
        stringRequest.setShouldCache(false)
        mRequestQueue.add(stringRequest)
    }

    private fun appContinue(isUnderMaintenance: Boolean) {
        if (isUnderMaintenance) {
            val i = Intent(this, UnderMaintenanceActivity::class.java).setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            )
            startActivity(i)
        } else {
            main()
        }
    }

    private fun getPromo() {
        val stringRequest = object : StringRequest(
            Method.GET,
            "https://bot.planckstudio.in/.well-known/android/apppromo.json",
            Response.Listener<String> {
                val jsonObject = JSONObject(it)
                val rStatus = jsonObject.getString("status")
                if (rStatus == "success") {

                    this.ls.removeValue("appCurrentInstagramPromotionUsername")
                    this.ls.removeValue("appCurrentInstagramPromotionCode")
                    this.ls.removeValue("appCurrentInstagramPromotionImageUrl")
                    this.ls.removeValue("appCurrentInstagramPromotionDestUrl")
                    this.ls.removeValue("appCurrentInstagramPromotionTitle")
                    this.ls.removeValue("appCurrentInstagramPromotionCaption")
                    this.ls.removeValue("appCurrentInstagramPromotionEnabled")
                    this.ls.removeValue("appGoogleAdsEnabled")

                    val isDisableAdsEnabled = this.ls.getValueBoolean("isDisableAdsEnabled")

                    this.ls.save(
                        "appCurrentInstagramPromotionUsername",
                        jsonObject.getString("appCurrentInstagramPromotionUsername")
                    )

                    this.ls.save(
                        "appCurrentInstagramPromotionCode",
                        jsonObject.getString("appCurrentInstagramPromotionCode")
                    )

                    this.ls.save(
                        "appCurrentInstagramPromotionImageUrl",
                        jsonObject.getString("appCurrentInstagramPromotionImageUrl")
                    )

                    this.ls.save(
                        "appCurrentInstagramPromotionDestUrl",
                        jsonObject.getString("appCurrentInstagramPromotionDestUrl")
                    )

                    this.ls.save(
                        "appCurrentInstagramPromotionTitle",
                        jsonObject.getString("appCurrentInstagramPromotionTitle")
                    )

                    this.ls.save(
                        "appCurrentInstagramPromotionCaption",
                        jsonObject.getString("appCurrentInstagramPromotionCaption")
                    )

                    if (isDisableAdsEnabled) {
                        this.ls.save(
                            "appCurrentInstagramPromotionEnabled",
                            false
                        )
                        this.ls.save(
                            "appGoogleAdsEnabled",
                            false
                        )
                    } else {
                        this.ls.save(
                            "appCurrentInstagramPromotionEnabled",
                            jsonObject.getBoolean("appCurrentInstagramPromotionEnabled")
                        )
                        this.ls.save(
                            "appGoogleAdsEnabled",
                            jsonObject.getBoolean("appGoogleAdsEnabled")
                        )
                    }
                }
            },
            Response.ErrorListener {
                //
            }
        ) {
        }
        stringRequest.setShouldCache(false)
        mRequestQueue.add(stringRequest)
    }

    private fun main() {
        val data = intent.data
        if (data != null) {
            when (data.host) {
                "next" -> {
                    when (data.path) {
                        "/maintenance" -> {
                            startActivity(
                                Intent(this, UnderMaintenanceActivity::class.java).setFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                )
                            )
                        }
                        "/root" -> {
                            startActivity(
                                Intent(this, UnderMaintenanceActivity::class.java).putExtra(
                                    "message",
                                    "Sorry superuser"
                                ).setFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                )
                            )
                        }
                        "/setting" -> {
                            startActivity(
                                Intent(this, SettingActivity::class.java).setFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                )
                            )
                        }
                        "/recent" -> {
                            startActivity(
                                Intent(this, RecentActivity::class.java).setFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                )
                            )
                        }
                        "/home" -> {
                            startActivity(
                                Intent(this, MainActivity::class.java).setFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                )
                            )
                        }
                    }
                }
                "add" -> {
                    when (data.path) {
                        "/instagram" -> {
                            startActivity(
                                Intent(
                                    this,
                                    ConnectInstagramActivity::class.java
                                ).setFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                )
                            )
                        }
                    }
                }
                else -> {
                    startMain()
                }
            }
        } else {
            startMain()
        }
    }

    private fun startMain() {
        startActivity(
            Intent(this, MainActivity::class.java).setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            )
        )
    }
}