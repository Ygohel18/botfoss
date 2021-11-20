package `in`.planckstudio.foss.bot

import `in`.planckstudio.foss.bot.api.InstagramApi
import `in`.planckstudio.foss.bot.helper.AppHelper
import `in`.planckstudio.foss.bot.helper.CoinHelper
import `in`.planckstudio.foss.bot.helper.DatabaseHelper
import `in`.planckstudio.foss.bot.helper.WebViewHelper
import `in`.planckstudio.foss.bot.ui.*
import `in`.planckstudio.foss.bot.ui.instagram.*
import `in`.planckstudio.foss.bot.ui.whatsapp.WhatsappToolActivity
import `in`.planckstudio.foss.bot.util.LocalStorage
import `in`.planckstudio.wowdesign.views.cards.CardViewStyleThumbOne
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.DelicateCoroutinesApi
import org.json.JSONObject

@DelicateCoroutinesApi
class MainActivity : AppCompatActivity() {

    private lateinit var mInstagramApi: InstagramApi

    private lateinit var db: DatabaseHelper

    private lateinit var mToolHolder: LinearLayout

    private lateinit var bottomBar: BottomNavigationView
    private lateinit var mPromoCard: MaterialCardView
    private lateinit var mPromoImage: ImageView
    private lateinit var mPromoTitle: TextView
    private lateinit var mPromoCaption: TextView
    private lateinit var mPromoButton: MaterialButton
    private lateinit var mPromoLayout: MaterialCardView
    private lateinit var mInput: TextInputEditText
    private lateinit var mButton: MaterialCardView
    private lateinit var mProgress: ProgressBar

    private lateinit var mQuery: String

    private lateinit var mRequestQueue: RequestQueue
    private lateinit var instagramApi: InstagramApi
    private lateinit var ls: LocalStorage
    private lateinit var coinHelper: CoinHelper
    private lateinit var mAdView: AdView
    private var mInterstitialAd: InterstitialAd? = null
    private lateinit var mPremiumText: MaterialTextView

    private fun init() {

        AppHelper(this).preLaunchTask()

        AppHelper(this).getCoinData()
        var adRequest = AdRequest.Builder().build()
        coinHelper = CoinHelper(this)
        InterstitialAd.load(
            this,
            "ca-app-pub-6785982567420185/1424680933",
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    coinHelper.addCoin(50)
                    mInterstitialAd = interstitialAd
                }
            })

        mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
            }

            override fun onAdShowedFullScreenContent() {
                coinHelper.addCoin(200)
                mInterstitialAd = null
            }
        }
        mAdView = findViewById(R.id.frameBannerAd)

        mInstagramApi = InstagramApi(this)
        this.ls = LocalStorage(this)
        this.db = DatabaseHelper(this, null)

        mToolHolder = findViewById(R.id.mainToolHolder)
        mPremiumText = findViewById(R.id.mainPremiumBanner)
        bottomBar = findViewById(R.id.mainBottomNav)

        mPromoCard = findViewById(R.id.igPromoCard)
        mPromoImage = findViewById(R.id.igPromoImage)
        mPromoTitle = findViewById(R.id.igPromoTitle)
        mPromoCaption = findViewById(R.id.igPromoCaption)
        mPromoButton = findViewById(R.id.igPromoButton)
        mPromoLayout = findViewById(R.id.commonPromoLayout)

        this.mInput = findViewById(R.id.inputQuery)
        this.mButton = findViewById(R.id.btnSearch)
        this.mProgress = findViewById(R.id.progressSearch)

        this.mQuery = ""

        this.mButton.isClickable = true
        this.mInput.isEnabled = true
        this.mInput.text?.clear()
        this.mProgress.visibility = View.INVISIBLE

        this.mRequestQueue = Volley.newRequestQueue(this)
        instagramApi = InstagramApi(this)

        AppHelper(this).getFavourites()
    }

    private fun main() {

        if (!ls.getValueBoolean("isDisableAdsEnabled") || !AppHelper(this).isLimitedEnabled()) {
            MobileAds.initialize(this) {}
            val adRequest = AdRequest.Builder().build()
            mAdView.loadAd(adRequest)
            mAdView.visibility = View.VISIBLE
        } else {
            mAdView.visibility = View.GONE
            mPremiumText.visibility = View.VISIBLE
        }

        if (AppHelper(this).isLimitedEnabled()) {
            mAdView.visibility = View.GONE
            mPremiumText.visibility = View.VISIBLE
        }

        bottomBar.selectedItemId = R.id.pageHome

        bottomBar.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.pageHome -> {
                    true
                }
                R.id.pageFavorite -> {
                    startActivity(Intent(this, FavouriteActivity::class.java))
                    true
                }
                R.id.pageHistory -> {
                    startActivity(Intent(this, RecentActivity::class.java))
                    true
                }
                R.id.pageSettings -> {
                    startActivity(Intent(this, SettingActivity::class.java))
                    true
                }
//                R.id.pageCoins -> {
//                    startActivity(Intent(this, CoinActivity::class.java))
//                    true
//                }
                R.id.pageNews -> {
                    coinHelper.addCoin(10)

                    startActivity(Intent(
                        this,
                        WebViewHelper::class.java
                    ).putExtra("weburl", "https://blog.planckstudio.in/category/botapp/")
                        .putExtra("title", "Blog"))
                    true
                }
                else -> {
                    false
                }
            }
        }

        renderTool("instagram")
        renderTool("whatsapp")

        if (ls.getValueBoolean("appCurrentInstagramPromotionEnabled")) {
            mPromoLayout.visibility = View.VISIBLE

            Glide.with(this)
                .load(ls.getValueString("appCurrentInstagramPromotionImageUrl"))
                .placeholder(R.drawable.placeholder_thumb)
                .into(mPromoImage)

            mPromoTitle.text = ls.getValueString("appCurrentInstagramPromotionTitle")
            val promoCaption = ls.getValueString("appCurrentInstagramPromotionUsername")
            mPromoCaption.text = promoCaption
        } else {
            mPromoLayout.visibility = View.GONE
        }

        val favData = (ls.getValueString("promotedFavourites").split(","))
        (0 until favData.size).forEach { f ->
            updateFavouriteData(favData[f])
        }

        mPromoCard.setOnClickListener {
            coinHelper.addCoin(10)
            openPromoUrl(ls.getValueString("appCurrentInstagramPromotionDestUrl"))
        }

        this.mButton.setOnClickListener {
            val fInput = this.mInput.text.toString()
            if (fInput.isEmpty()) {
                showMessage("Enter url, username or query")
            } else {
                showMessage("Hold on processing request")
                this.mProgress.visibility = View.VISIBLE
                processRedirect(fInput)
            }
        }

        updateInstagramUserData()
    }

    private fun renderTool(type: String) {

        val card = CardViewStyleThumbOne(this)
        card.hideCardTitle()

        when (type) {
            "instagram" -> {
                card.setCardImage(R.drawable.ic_thumb_instagram)
            }
            "whatsapp" -> {
                card.setCardImage(R.drawable.ic_thumb_whatsapp)
            }
            "pinterest" -> {
                card.setCardImage(R.drawable.ic_thumb_pinterest)
            }
            "twitter" -> {
                card.setCardImage(R.drawable.ic_thumb_twitter)
            }
        }

        card.card.setOnLongClickListener {
            when (type) {
                "instagram" -> {
                    if (ls.getValueBoolean("isDisableAdsEnabled")) {
                        startActivity(Intent(this, InstagramTestSessionActivity::class.java))
                    }
                }
            }
            true
        }

        card.card.setOnClickListener {
            when (type) {
                "instagram" -> {
                    val down = JSONObject(ls.getValueString("settingDown"))
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
                        if (isInstagramConnected()) {
                            val intent = Intent(this, InstagramToolActivity::class.java)
                            startActivity(intent)
                        } else {
                            val intent = Intent(this, ConnectInstagramActivity::class.java)
                            startActivity(intent)
                        }
                    }
                }
                "whatsapp" -> {
                    val down = JSONObject(ls.getValueString("settingDown"))
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
                        startActivity(Intent(this, WhatsappToolActivity::class.java))
                    }
                }
            }
        }

        mToolHolder.addView(card.getCard())
        card.show()
    }

    private fun showCoinAd() {
        if (!ls.getValueBoolean("isDisableAdsEnabled")) {
            if (mInterstitialAd != null) {
                mInterstitialAd?.show(this)
            } else {
                Toast.makeText(this, "Running out of Ads", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "You are test user", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isInstagramConnected(): Boolean {
        return ls.getValueBoolean("ig_connected")
    }

    private fun processRedirect(query: String) {
        startActivity(
            Intent(this, DirectActivity::class.java).putExtra(
                "android.intent.extra.TEXT",
                query
            )
        )
    }

    @SuppressLint("ObsoleteSdkInt")
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

    private fun showMessage(msg: String) {
        Snackbar.make(
            findViewById(R.id.common_view),
            msg,
            Snackbar.LENGTH_LONG
        ).setTextColor(Color.parseColor("#F7D42E")).show()
    }

    private fun openPromoUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(url))
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        main()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {
        super.onDestroy()
        AppHelper(this).getCoinData()
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        AppHelper(this).preLaunchTask()
        bottomBar.selectedItemId = R.id.pageHome
        this.mProgress.visibility = View.GONE
        this.mInput.text?.clear()
        AppHelper(this).getCoinData()
    }

    private fun updateFavouriteData(username: String) {
        mInstagramApi.insertInstagramRecord(username)
    }

    @SuppressLint("Range")
    private fun updateInstagramUserData() {
        val accounts = db.getSessionList("instagram")

        if (accounts != null && accounts.moveToFirst()) {
            do {
                mInstagramApi.addInstagramRecord(
                    accounts.getString(
                        accounts.getColumnIndex(
                            DatabaseHelper.COLUMN_SESSION_USER
                        )
                    )
                )
            } while (accounts.moveToNext() && !accounts.isAfterLast)
        }
    }
}