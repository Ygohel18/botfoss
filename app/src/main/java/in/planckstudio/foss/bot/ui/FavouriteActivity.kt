package `in`.planckstudio.foss.bot.ui

import `in`.planckstudio.foss.bot.R
import `in`.planckstudio.foss.bot.helper.AppHelper
import `in`.planckstudio.foss.bot.helper.DatabaseHelper
import `in`.planckstudio.foss.bot.model.FavouriteModel
import `in`.planckstudio.foss.bot.ui.instagram.InstagramProfileActivity
import `in`.planckstudio.foss.bot.util.LocalStorage
import `in`.planckstudio.wowdesign.views.cards.CardViewStyleOne
import android.annotation.SuppressLint
import android.content.Intent
import android.database.sqlite.SQLiteException
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.DelicateCoroutinesApi
import org.json.JSONException
import org.json.JSONObject
import kotlin.random.Random


@DelicateCoroutinesApi
class FavouriteActivity : AppCompatActivity() {
    private lateinit var mHolder: LinearLayout
    private lateinit var db: DatabaseHelper
    private lateinit var ls: LocalStorage
    private lateinit var topAppBar: MaterialToolbar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favourite)
        init()
        main()
    }

    private fun main() {
        topAppBar.setNavigationOnClickListener {
            finish()
        }

        renderFav()
    }

    private fun init() {
        AppHelper(this).preLaunchTask()
        mHolder = findViewById(R.id.favouriteHolder)
        topAppBar = findViewById(R.id.favoriteTopAppBar)
        db = DatabaseHelper(this, null)
        ls = LocalStorage(this)
    }

    private fun renderPromoted() {
        val favData = (ls.getValueString("promotedFavourites").split(","))

        val u = Random.nextInt(favData.size)
        try {
            val data = db.getSessionValue("instagram", favData[u], "info")
            val user = JSONObject(data)
            val username = user.getString("username")
            val name = user.getString("name")
            val dp = user.getString("profile_pic_url")
            renderFavouriteAccount(username, name, dp, true)
        } catch (e: JSONException) {
            Log.e("PBOT", "Failed to render promoted")
        }
    }

    @SuppressLint("Range")
    private fun renderFav() {
        renderPromoted()
        try {
            val cursor = db.getFavourites()
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        val fav = FavouriteModel()

                        fav.setFavouriteID(
                            cursor.getInt(
                                cursor.getColumnIndex(
                                    DatabaseHelper.COLUMN_FAVOURITE_ID
                                )
                            )
                        )

                        fav.setFavouriteType(
                            cursor.getString(
                                cursor.getColumnIndex(
                                    DatabaseHelper.COLUMN_FAVOURITE_TYPE
                                )
                            )
                        )

                        fav.setFavouriteName(
                            cursor.getString(
                                cursor.getColumnIndex(
                                    DatabaseHelper.COLUMN_FAVOURITE_NAME
                                )
                            )
                        )

                        fav.setFavouriteValue(
                            cursor.getInt(
                                cursor.getColumnIndex(
                                    DatabaseHelper.COLUMN_FAVOURITE_VALUE
                                )
                            )
                        )

                        try {
                            val data =
                                db.getSessionValue("instagram", fav.getFavouriteName(), "info")
                            val user = JSONObject(data)
                            val username = user.getString("username")
                            val name = user.getString("name")
                            val dp = user.getString("profile_pic_url")
                            renderFavouriteAccount(username, name, dp)
                        } catch (e: JSONException) {
                            Log.e("PBOT", "Failed to render fav")
                        }
                    } while (cursor.moveToNext() && !cursor.isAfterLast)
                }
            }
        } catch (e: SQLiteException) {
            Log.e("PBOT", "DB failed")
        }
    }

    private fun renderFavouriteAccount(
        username: String,
        name: String,
        dp: String,
        promoted: Boolean = false
    ) {
        val card = CardViewStyleOne(this)
        card.setCardTitle(name)
        card.setCardSubtitle("@$username")
        card.setCardImage(dp)

        if (promoted) {
            card.setCardBorder(resources.getColor(R.color.colorPrimary))
        }

        card.card.setOnClickListener {
            startActivity(
                Intent(this, InstagramProfileActivity::class.java).putExtra(
                    "query",
                    username
                )
            )
        }
        card.card.setOnLongClickListener {
            startActivity(
                Intent(this, InstagramProfileActivity::class.java).putExtra(
                    "query",
                    username
                )
            )
            true
        }
        mHolder.addView(card.getCard())
        card.show()
    }

    override fun onResume() {
        super.onResume()
        AppHelper(this).preLaunchTask()
    }
}