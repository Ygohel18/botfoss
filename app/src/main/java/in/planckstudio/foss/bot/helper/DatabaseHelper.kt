package `in`.planckstudio.foss.bot.helper

import `in`.planckstudio.foss.bot.SearchModel
import `in`.planckstudio.foss.bot.model.FavouriteModel
import `in`.planckstudio.foss.bot.model.SessionModel
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.util.*

class DatabaseHelper(
    context: Context,
    factory: SQLiteDatabase.CursorFactory?
) :
    SQLiteOpenHelper(
        context, DATABASE_NAME,
        factory, DATABASE_VERSION
    ) {
    override fun onCreate(db: SQLiteDatabase?) {
        val createFavouriteTable = ("CREATE TABLE " +
                TABLE_NAME_FAVOURITE + "("
                + COLUMN_FAVOURITE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_FAVOURITE_TYPE + " TEXT NOT NULL," +
                COLUMN_FAVOURITE_NAME + " TEXT UNIQUE NOT NULL," +
                COLUMN_FAVOURITE_VALUE + " INTEGER NOT NULL DEFAULT 1" +
                ")")

        val createSessionTable = ("CREATE TABLE " +
                TABLE_NAME_SESSION + "("
                + COLUMN_SESSION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_SESSION_USER + " TEXT NOT NULL," +
                COLUMN_SESSION_TYPE + " TEXT NOT NULL," +
                COLUMN_SESSION_NAME + " TEXT NOT NULL," +
                COLUMN_SESSION_VALUE + " TEXT NOT NULL" +
                ")")

        val createLimitTable = ("CREATE TABLE " +
                TABLE_NAME_LIMIT + "("
                + COLUMN_LIMIT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_LIMIT_SID + " TEXT NOT NULL UNIQUE," +
                COLUMN_LIMIT_IS_LIMITED + " INTEGER NOT NULL DEFAULT 0," +
                COLUMN_LIMIT_UNTIL + " TEXT NULL DEFAULT NULL," +
                COLUMN_LIMIT_TIME + " TEXT NULL DEFAULT NULL" +
                ")")

        val createSearchTable = ("CREATE TABLE " +
                TABLE_NAME_SEARCH + "("
                + COLUMN_SEARCH_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_SEARCH_SERVICE + " TEXT NOT NULL," +
                COLUMN_SEARCH_TYPE + " TEXT NOT NULL," +
                COLUMN_SEARCH_KEY + " TEXT NOT NULL," +
                COLUMN_SEARCH_VALUE + " TEXT," +
                COLUMN_SEARCH_IMAGE + " TEXT" +
                ")")

        db?.execSQL(createFavouriteTable)
        db?.execSQL(createSessionTable)
        db?.execSQL(createLimitTable)
        db?.execSQL(createSearchTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_FAVOURITE")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_SESSION")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_LIMIT")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_SEARCH")
        onCreate(db)
    }

    fun getAllSearchRecord(): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery(
            "SELECT * FROM $TABLE_NAME_SEARCH",
            null
        )
    }

    fun getAllSessionRecord(): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery(
            "SELECT * FROM $TABLE_NAME_SESSION",
            null
        )
    }

    fun getAllFavouriteRecord(): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery(
            "SELECT * FROM $TABLE_NAME_FAVOURITE",
            null
        )
    }

    fun addSearch(searchModel: SearchModel) {
        val db = this.writableDatabase
        this.removeSearch(searchModel)
        try {
            val values = ContentValues()
            values.put(COLUMN_SEARCH_TYPE, searchModel.getSearchType())
            values.put(COLUMN_SEARCH_SERVICE, searchModel.getSearchService())
            values.put(COLUMN_SEARCH_KEY, searchModel.getSearchKey())
            values.put(COLUMN_SEARCH_VALUE, searchModel.getSearchValue())
            values.put(COLUMN_SEARCH_IMAGE, searchModel.getSearchImage())
            db.insert(TABLE_NAME_SEARCH, null, values)
        } catch (e: Exception) {
            //
        }
    }

    fun logoutUser() {
        val db = this.writableDatabase
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_FAVOURITE")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_SESSION")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_LIMIT")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_SEARCH")
        onCreate(db)
    }

    fun removeAccounts() {
        val db = this.writableDatabase
        db?.execSQL("DELETE FROM $TABLE_NAME_SESSION")
        db?.execSQL("DELETE FROM $TABLE_NAME_LIMIT")
    }

    @SuppressLint("Recycle")
    fun removeSearch(searchModel: SearchModel) {
        val db = this.writableDatabase
        try {
            db.delete(
                TABLE_NAME_SEARCH,
                "${COLUMN_SEARCH_SERVICE}=? AND ${COLUMN_SEARCH_KEY}=?",
                arrayOf(searchModel.getSearchService(), searchModel.getSearchKey())
            )

        } catch (e: SQLiteException) {
            Log.e(
                "PBOT",
                "Failed to remove search: [Service: ${searchModel.getSearchService()}, Key: ${searchModel.getSearchKey()}]"
            )
        }
    }

    fun getAllSearch(limit: Int = 50): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery(
            "SELECT * FROM $TABLE_NAME_SEARCH ORDER BY $COLUMN_SEARCH_ID DESC LIMIT ?",
            arrayOf(limit.toString())
        )
    }

    fun addFavourite(favouriteModel: FavouriteModel) {
        val db = this.writableDatabase
        try {
            val values = ContentValues()
            values.put(COLUMN_FAVOURITE_TYPE, favouriteModel.getFavouriteType())
            values.put(COLUMN_FAVOURITE_NAME, favouriteModel.getFavouriteName())
            values.put(COLUMN_FAVOURITE_VALUE, favouriteModel.getFavouriteValue())
            db.insert(TABLE_NAME_FAVOURITE, null, values)

        } catch (e: Exception) {
            //
        }
    }

    fun replaceFavourite(favouriteModel: FavouriteModel) {
        val db = this.writableDatabase
        try {
            val values = ContentValues()
            values.put(COLUMN_FAVOURITE_TYPE, favouriteModel.getFavouriteType())
            values.put(COLUMN_FAVOURITE_NAME, favouriteModel.getFavouriteName())
            values.put(COLUMN_FAVOURITE_VALUE, favouriteModel.getFavouriteValue())
            db.replace(TABLE_NAME_FAVOURITE, null, values)

        } catch (e: Exception) {
            Log.e("PBOT", "Failed to replace favourite")
        }
    }

    fun replaceSession(sessionModel: SessionModel) {
        val db = this.writableDatabase
        try {
            val values = ContentValues()
            values.put(COLUMN_SESSION_NAME, sessionModel.getSessionName())
            values.put(COLUMN_SESSION_TYPE, sessionModel.getSessionType())
            values.put(COLUMN_SESSION_USER, sessionModel.getSessionUser())
            values.put(COLUMN_SESSION_VALUE, sessionModel.getSessionValue())
            db.replace(TABLE_NAME_SESSION, null, values)

        } catch (e: SQLiteException) {
            Log.e(
                "PBOT",
                "Failed to replace session: [User: ${sessionModel.getSessionUser()}, Type: ${sessionModel.getSessionType()}, Name: ${sessionModel.getSessionName()}]"
            )
        }
    }

    fun updateSession(sessionModel: SessionModel) {
        val db = this.writableDatabase
        try {
            val values = ContentValues()
            values.put(COLUMN_SESSION_VALUE, sessionModel.getSessionValue())
            db.update(
                TABLE_NAME_SESSION,
                values,
                "$COLUMN_SESSION_TYPE=? AND $COLUMN_SESSION_USER=? AND $COLUMN_SESSION_NAME=?",
                arrayOf(
                    sessionModel.getSessionType(),
                    sessionModel.getSessionUser(),
                    sessionModel.getSessionName()
                )
            )

        } catch (e: SQLiteException) {
            Log.e(
                "PBOT",
                "Failed to update session: [User: ${sessionModel.getSessionUser()}, Type: ${sessionModel.getSessionType()}, Name: ${sessionModel.getSessionName()}]"
            )
        }
    }

    fun addSession(sessionModel: SessionModel) {
        val db = this.writableDatabase
        try {
            val values = ContentValues()
            values.put(COLUMN_SESSION_NAME, sessionModel.getSessionName())
            values.put(COLUMN_SESSION_TYPE, sessionModel.getSessionType())
            values.put(COLUMN_SESSION_USER, sessionModel.getSessionUser())
            values.put(COLUMN_SESSION_VALUE, sessionModel.getSessionValue())
            db.insert(TABLE_NAME_SESSION, null, values)
        } catch (e: SQLiteException) {
            Log.e(
                "PBOT",
                "Session insert failed: [User: ${sessionModel.getSessionUser()}, Type: ${sessionModel.getSessionType()}, Name: ${sessionModel.getSessionType()}, Value: ${sessionModel.getSessionValue()}]"
            )
        }
    }

    fun removeSession(sessionId: Int): Cursor? {
        val db = this.writableDatabase
        return db.rawQuery(
            "DELETE FROM $TABLE_NAME_SESSION WHERE $COLUMN_SESSION_ID LIKE ?",
            arrayOf(sessionId.toString())
        )
    }

    fun removeSession(sessionType: String): Cursor? {
        val db = this.writableDatabase
        return db.rawQuery(
            "DELETE FROM $TABLE_NAME_SESSION WHERE $COLUMN_SESSION_TYPE LIKE ?",
            arrayOf(sessionType)
        )
    }

    @SuppressLint("Recycle")
    fun removeSession(sessionType: String, sessionUser: String) {
        val db = this.writableDatabase
        try {
            db.delete(
                TABLE_NAME_SESSION,
                "${COLUMN_SESSION_TYPE}=? AND ${COLUMN_SESSION_USER}=?",
                arrayOf(sessionType, sessionUser)
            )

        } catch (e: SQLiteException) {
            Log.e("PBOT", "Failed to remove session: [Type: ${sessionType}, User: ${sessionUser}]")
        }
    }

    @SuppressLint("Recycle")
    fun removeSession(sessionType: String, sessionUser: String, sessionName: String) {
        val db = this.writableDatabase
        try {
            db.delete(
                TABLE_NAME_SESSION,
                "${COLUMN_SESSION_TYPE}=? AND ${COLUMN_SESSION_USER}=? AND ${COLUMN_SESSION_NAME}=?",
                arrayOf(sessionType, sessionUser, sessionName)
            )

        } catch (e: SQLiteException) {
            Log.e(
                "PBOT",
                "Failed to remove session: [Type: ${sessionType}, User: ${sessionUser}, Name: ${sessionName}]"
            )
        }
    }

    fun getSession(sessionId: Int): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery(
            "SELECT * FROM $TABLE_NAME_SESSION WHERE $COLUMN_SESSION_ID LIKE ?",
            arrayOf(sessionId.toString())
        )
    }

    fun getSession(sessionType: String): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery(
            "SELECT * FROM $TABLE_NAME_SESSION WHERE $COLUMN_SESSION_TYPE LIKE ?",
            arrayOf(sessionType)
        )
    }

    fun getSessionList(sessionType: String): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery(
            "SELECT * FROM $TABLE_NAME_SESSION WHERE $COLUMN_SESSION_TYPE LIKE ? AND $COLUMN_SESSION_NAME LIKE ?",
            arrayOf(sessionType, "cookie")
        )
    }

    fun getSession(sessionType: String, sessionUser: String): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery(
            "SELECT * FROM $TABLE_NAME_SESSION WHERE $COLUMN_SESSION_TYPE LIKE ? AND $COLUMN_SESSION_USER LIKE ?",
            arrayOf(sessionType, sessionUser)
        )
    }

    fun getSession(sessionType: String, sessionUser: String, sessionName: String): Cursor {
        val db = this.readableDatabase
        return db.rawQuery(
            "SELECT * FROM $TABLE_NAME_SESSION WHERE $COLUMN_SESSION_TYPE LIKE ? AND $COLUMN_SESSION_USER LIKE ? AND $COLUMN_SESSION_NAME LIKE ? LIMIT 1",
            arrayOf(sessionType, sessionUser, sessionName)
        )
    }

    fun getLimitBySid(sid: String): Cursor {
        val db = this.readableDatabase
        return db.rawQuery(
            "SELECT * FROM $TABLE_NAME_LIMIT WHERE $COLUMN_LIMIT_SID LIKE ? LIMIT 1",
            arrayOf(sid)
        )
    }

    private fun hourLimit(): Int {
        return (Date().time.toInt() + 10800000)
    }

    fun currentTime(): Int {
        return (Date().time.toInt() + 0)
    }

    fun updateLimitBySid(sid: String) {
        val db = this.writableDatabase
        try {
            val values = ContentValues()
            values.put(COLUMN_LIMIT_IS_LIMITED, 1)
            values.put(COLUMN_LIMIT_UNTIL, hourLimit())
            db.update(
                TABLE_NAME_LIMIT,
                values,
                "$COLUMN_LIMIT_SID=?",
                arrayOf(
                    sid
                )
            )
        } catch (e: SQLiteException) {
//
        }
    }

    @SuppressLint("Range")
    fun getSessionValue(sessionType: String, sessionUser: String, sessionName: String): String {
        val db = this.readableDatabase
        var result = ""
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_NAME_SESSION WHERE $COLUMN_SESSION_TYPE LIKE ? AND $COLUMN_SESSION_USER LIKE ? AND $COLUMN_SESSION_NAME LIKE ? LIMIT 1",
            arrayOf(sessionType, sessionUser, sessionName)
        )

        if (cursor != null && cursor.moveToFirst()) {
            result = cursor.getString(cursor.getColumnIndex(COLUMN_SESSION_VALUE))
        }
        return result
    }

    fun getSessionUser(sessionUser: String): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery(
            "SELECT * FROM $TABLE_NAME_SESSION WHERE $COLUMN_SESSION_USER LIKE ?",
            arrayOf(sessionUser)
        )
    }

    fun getFavourite(limit: Int = 10): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery(
            "SELECT * FROM $TABLE_NAME_FAVOURITE WHERE $COLUMN_FAVOURITE_VALUE LIKE ? LIMIT ?",
            arrayOf(1.toString(), limit.toString())
        )
    }

    fun getFavourites(): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery(
            "SELECT * FROM $TABLE_NAME_FAVOURITE WHERE $COLUMN_FAVOURITE_VALUE = 1",
            null
        )
    }

    fun getFavourite(favouriteType: String): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery(
            "SELECT * FROM $TABLE_NAME_FAVOURITE WHERE $COLUMN_FAVOURITE_TYPE LIKE ?",
            arrayOf(favouriteType)
        )
    }

    fun getFavourite(favouriteType: String, favouriteName: String): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery(
            "SELECT * FROM $TABLE_NAME_FAVOURITE WHERE $COLUMN_FAVOURITE_NAME LIKE ? AND $COLUMN_FAVOURITE_TYPE LIKE ? LIMIT 1",
            arrayOf(favouriteName, favouriteType)
        )
    }

    fun getFavouriteName(favouriteName: String): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery(
            "SELECT * FROM $TABLE_NAME_FAVOURITE WHERE $COLUMN_FAVOURITE_NAME LIKE ?",
            arrayOf(favouriteName)
        )
    }

    fun removeFavourite(favouriteId: Int): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery(
            "DELETE FROM $TABLE_NAME_FAVOURITE WHERE $COLUMN_FAVOURITE_ID LIKE ?",
            arrayOf(favouriteId.toString())
        )
    }

    fun setFavourite(favouriteId: Int) {
        val db = this.writableDatabase
        try {
            val values = ContentValues()
            values.put(COLUMN_FAVOURITE_VALUE, 1)
            db.update(
                TABLE_NAME_FAVOURITE,
                values,
                "$COLUMN_FAVOURITE_ID=?",
                arrayOf(favouriteId.toString())
            )

        } catch (e: SQLiteException) {
            Log.e("PBOT", "Failed to update favourite: [ID: ${favouriteId}]")
        }
        db.close()
    }

    fun unsetFavourite(favouriteId: Int) {
        val db = this.writableDatabase
        try {
            val values = ContentValues()
            values.put(COLUMN_FAVOURITE_VALUE, 0)
            db.update(
                TABLE_NAME_FAVOURITE,
                values,
                "$COLUMN_FAVOURITE_ID=?",
                arrayOf(favouriteId.toString())
            )

        } catch (e: SQLiteException) {
            Log.e("PBOT", "Failed to update favourite: [ID: ${favouriteId}]")
        }
        db.close()
    }

    fun deleteFavourite(favouriteName: String) {
        val db = this.writableDatabase
        try {
            val values = ContentValues()
            values.put(COLUMN_FAVOURITE_VALUE, 1)
            db.delete(TABLE_NAME_FAVOURITE, "$COLUMN_FAVOURITE_NAME=?", arrayOf(favouriteName))

        } catch (e: SQLiteException) {
            Log.e("PBOT", "Failed to delete favourite: [Name: ${favouriteName}]")
        }
        db.close()
    }

    fun deleteFavourite(favouriteId: Int) {
        val db = this.writableDatabase
        try {
            val values = ContentValues()
            values.put(COLUMN_FAVOURITE_VALUE, 1)
            db.delete(
                TABLE_NAME_FAVOURITE,
                "$COLUMN_FAVOURITE_ID = ?",
                arrayOf(favouriteId.toString())
            )

        } catch (e: SQLiteException) {
            Log.e("PBOT", "Failed to delete favourite: [ID: ${favouriteId}]")
        }
        db.close()

    }

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "in.planckstudio.foss.bot.db"
        const val TABLE_NAME_FAVOURITE = "bot_favourite"
        const val COLUMN_FAVOURITE_ID = "favourite_id"
        const val COLUMN_FAVOURITE_TYPE = "favourite_type"
        const val COLUMN_FAVOURITE_NAME = "favourite_name"
        const val COLUMN_FAVOURITE_VALUE = "favourite_value"

        const val TABLE_NAME_SESSION = "bot_session"
        const val COLUMN_SESSION_ID = "session_id"
        const val COLUMN_SESSION_USER = "session_user"
        const val COLUMN_SESSION_TYPE = "session_type"
        const val COLUMN_SESSION_NAME = "session_name"
        const val COLUMN_SESSION_VALUE = "session_value"

        const val TABLE_NAME_LIMIT = "bot_limit"
        const val COLUMN_LIMIT_ID = "limit_id"
        const val COLUMN_LIMIT_SID = "limit_sid"
        const val COLUMN_LIMIT_UNTIL = "limit_until"
        const val COLUMN_LIMIT_IS_LIMITED = "is_limited"
        const val COLUMN_LIMIT_TIME = "limit_time"

        const val TABLE_NAME_SEARCH = "bot_search"
        const val COLUMN_SEARCH_ID = "search_id"
        const val COLUMN_SEARCH_SERVICE = "search_service"
        const val COLUMN_SEARCH_TYPE = "search_type"
        const val COLUMN_SEARCH_KEY = "search_KEY"
        const val COLUMN_SEARCH_VALUE = "search_value"
        const val COLUMN_SEARCH_IMAGE = "session_image"
    }
}