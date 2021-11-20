package `in`.planckstudio.foss.bot.ui

import `in`.planckstudio.foss.bot.SearchModel
import `in`.planckstudio.foss.bot.adapter.OnSearchHistoryItemClickListner
import `in`.planckstudio.foss.bot.adapter.RecentAdapter
import `in`.planckstudio.foss.bot.helper.AppHelper
import `in`.planckstudio.foss.bot.helper.DatabaseHelper
import `in`.planckstudio.foss.bot.model.instagram.SearchHistory
import `in`.planckstudio.foss.bot.ui.instagram.InstagramProfileActivity
import `in`.planckstudio.foss.bot.ui.youtube.YoutubeDownloadActivity
import `in`.planckstudio.foss.bot.util.LocalStorage
import `in`.planckstudio.wowdesign.views.cards.CardViewStyleOne
import android.annotation.SuppressLint
import android.content.Intent
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import `in`.planckstudio.foss.bot.R
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.DelicateCoroutinesApi
import java.util.*


@DelicateCoroutinesApi
class RecentActivity : AppCompatActivity(), OnSearchHistoryItemClickListner {

    private lateinit var mRequestQueue: RequestQueue
    private lateinit var ls: LocalStorage
    private lateinit var db: DatabaseHelper

    private val mHistoryList = ArrayList<SearchHistory>()
    private val historyAdapter = RecentAdapter(mHistoryList, this)
    private lateinit var mSearchHistory: SearchHistory
    private lateinit var mEmptyScreen: LinearLayout
    private lateinit var mRecentListView: RecyclerView

    private lateinit var mPromoCard: MaterialCardView
    private lateinit var mPromoImage: ImageView
    private lateinit var mPromoTitle: TextView
    private lateinit var mPromoCaption: TextView
    private lateinit var mPromoButton: MaterialButton
    private lateinit var mSettingButton: MaterialButton
    private lateinit var mHolder: LinearLayout

    private lateinit var topAppBar: MaterialToolbar

    private fun init() {
        AppHelper(this).preLaunchTask()
        topAppBar = findViewById(R.id.recentTopAppBar)
        mPromoCard = findViewById(R.id.igPromoCard)
        mPromoImage = findViewById(R.id.igPromoImage)
        mPromoTitle = findViewById(R.id.igPromoTitle)
        mPromoCaption = findViewById(R.id.igPromoCaption)
        mPromoButton = findViewById(R.id.igPromoButton)
        mSettingButton = findViewById(R.id.recentOpenSettingButton)
        this.mEmptyScreen = findViewById(R.id.recentEmptyScreen)

        this.mRequestQueue = Volley.newRequestQueue(this)
        this.mHolder = findViewById(R.id.recentCardListView)

        this.ls = LocalStorage(this)
        this.db = DatabaseHelper(this, null)
    }

    private fun main() {
        topAppBar.setNavigationOnClickListener {
            finish()
        }

        mSettingButton.setOnClickListener {
            val i = Intent(this, SettingActivity::class.java)
            startActivity(i)
        }

        if (ls.getValueBoolean("appCurrentInstagramPromotionEnabled")) {
            mPromoCard.visibility = View.VISIBLE

            Glide.with(this)
                .load(ls.getValueString("appCurrentInstagramPromotionImageUrl"))
                .placeholder(R.drawable.default_imageholder_ig)
                .into(mPromoImage)

            mPromoTitle.text = ls.getValueString("appCurrentInstagramPromotionTitle")
            val promoCaption = ls.getValueString("appCurrentInstagramPromotionUsername")
            mPromoCaption.text = "Ad - $promoCaption"
        } else {
            mPromoCard.visibility = View.GONE
        }

        mPromoCard.setOnClickListener {
            openPromoUrl(ls.getValueString("appCurrentInstagramPromotionDestUrl"))
        }

        mPromoButton.setOnClickListener {
            openPromoUrl(ls.getValueString("appCurrentInstagramPromotionDestUrl"))
        }

        if (ls.getValueBoolean("isRecentHistoryDisabled")) {
            renderElements(false)
        } else {
            getSearchHistory()
        }
    }

    private fun renderRecentAccount(
        username: String,
        name: String,
        dp: String
    ) {
        val card = CardViewStyleOne(this)
        card.setCardTitle(name)
        card.setCardSubtitle(username)
        card.setCardImage(dp)
        card.card.setOnClickListener {
            when (username) {
                "instagram" -> {
                    startActivity(
                        Intent(this, InstagramProfileActivity::class.java).putExtra(
                            "query",
                            name
                        )
                    )
                }
                "youtube" -> {
                    startActivity(
                        Intent(this, YoutubeDownloadActivity::class.java).putExtra(
                            "query",
                            name
                        )
                    )
                }
            }

            startActivity(
                Intent(this, InstagramProfileActivity::class.java).putExtra(
                    "query",
                    name
                )
            )
        }
        card.card.setOnLongClickListener {
            val sm = SearchModel()
            when (username) {
                "instagram" -> {
                    sm.setSearchService("instagram")
                }
                "youtube" -> {
                    sm.setSearchService("youtube")
                }
            }

            sm.setSearchKey(name)
            db.removeSearch(sm)
            Toast.makeText(
                this,
                "$name removed from recent history",
                Toast.LENGTH_SHORT
            ).show()
            card.card.visibility = View.GONE
            true
        }

        mHolder.addView(card.getCard())
        card.show()
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recent)
        init()
        main()
    }

    @SuppressLint("Range")
    private fun getSearchHistory() {
        try {
            val cursor = db.getAllSearch()
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        val data = SearchModel()

                        data.setSearchID(
                            cursor.getInt(
                                cursor.getColumnIndex(
                                    DatabaseHelper.COLUMN_SEARCH_ID
                                )
                            )
                        )

                        data.setSearchType(
                            cursor.getString(
                                cursor.getColumnIndex(
                                    DatabaseHelper.COLUMN_SEARCH_TYPE
                                )
                            )
                        )

                        data.setSearchService(
                            cursor.getString(
                                cursor.getColumnIndex(
                                    DatabaseHelper.COLUMN_SEARCH_SERVICE
                                )
                            )
                        )

                        data.setSearchKey(
                            cursor.getString(
                                cursor.getColumnIndex(
                                    DatabaseHelper.COLUMN_SEARCH_KEY
                                )
                            )
                        )

                        data.setSearchValue(
                            cursor.getString(
                                cursor.getColumnIndex(
                                    DatabaseHelper.COLUMN_SEARCH_VALUE
                                )
                            )
                        )

                        data.setSearchImage(
                            cursor.getString(
                                cursor.getColumnIndex(
                                    DatabaseHelper.COLUMN_SEARCH_IMAGE
                                )
                            )
                        )

                        renderRecentAccount(
                            data.getSearchService(),
                            data.getSearchKey(),
                            data.getSearchImage()
                        )
                    } while (cursor.moveToNext() && !cursor.isAfterLast)
                } else {
                    renderElements(false)
                }
            }
        } catch (e: SQLiteException) {
            Log.e("PBOT", "DB failed")
        }
    }

    fun renderElements(action: Boolean) {
        if (action) {
            this.mEmptyScreen.visibility = View.GONE
            this.mHolder.visibility = View.VISIBLE
        } else {
            this.mEmptyScreen.visibility = View.VISIBLE
            this.mHolder.visibility = View.GONE
        }
    }

    override fun onItemClick(item: SearchHistory, position: Int) {
        //
    }

    fun openPromoUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(url))
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        AppHelper(this).preLaunchTask()
    }
}