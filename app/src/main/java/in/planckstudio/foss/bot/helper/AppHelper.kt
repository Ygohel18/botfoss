package `in`.planckstudio.foss.bot.helper

import `in`.planckstudio.foss.bot.util.LocalStorage
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.startActivity
import com.android.volley.Response
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.DelicateCoroutinesApi
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import java.util.*
import kotlin.collections.HashMap

@DelicateCoroutinesApi
class AppHelper(val context: Context) {

    private var ls = LocalStorage(context)
    private var mRequestQueue: RequestQueue = Volley.newRequestQueue(context)
    private var mQuery: String = ""
    private var mApiBaseUrl = "https://bot.planckstudio.in/v1/"
    private var db: DatabaseHelper = DatabaseHelper(context, null)

    fun saveOId() {
        ls.save("user_oid", this.generateMd5())
    }

    fun getOid(): String {
        return this.ls.getValueString("user_oid")
    }

    @SuppressLint("HardwareIds")
    fun getDeviceId(): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun isIgConnected(): Boolean {
        return ls.getValueBoolean("ig_connected")
    }

    fun getOneDayAfter(): Int {
        return Date().time.toInt() + 86400
    }

    fun getOneMonthAfter(): Int {
        return Date().time.toInt() + (86400 * 30)
    }

    fun openDeepLink(link: String) {
        val uri: Uri = Uri.parse(link)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(context, Intent.createChooser(intent, "bot"), null)
    }

    fun preLaunchTask() {
        if (ls.getValueBoolean("isUnderMaintenance")) {
            openDeepLink("bot://next/maintenance")
        } else {
            if (isRooted()) {
                openDeepLink("bot://next/root")
            }
        }
    }

    @SuppressLint("Range")
    @RequiresApi(Build.VERSION_CODES.O)
    fun localBackup() {
        val data = JSONObject()
        val dataDb = JSONObject()
        val objFav = JSONArray()
        val objSession = JSONArray()
        val appSettings = JSONObject()
        try {
            val dataFavourite = db.getAllFavouriteRecord()
            val dataSession = db.getAllSessionRecord()
            if (dataFavourite != null) {
                if (dataFavourite.moveToFirst()) {
                    do {
                        val obj = JSONObject()
                        obj.put(
                            DatabaseHelper.COLUMN_FAVOURITE_ID, dataFavourite.getInt(
                                dataFavourite.getColumnIndex(
                                    DatabaseHelper.COLUMN_FAVOURITE_ID
                                )
                            )
                        )
                        obj.put(
                            DatabaseHelper.COLUMN_FAVOURITE_NAME, dataFavourite.getString(
                                dataFavourite.getColumnIndex(
                                    DatabaseHelper.COLUMN_FAVOURITE_NAME
                                )
                            )
                        )
                        obj.put(
                            DatabaseHelper.COLUMN_FAVOURITE_TYPE, dataFavourite.getString(
                                dataFavourite.getColumnIndex(
                                    DatabaseHelper.COLUMN_FAVOURITE_TYPE
                                )
                            )
                        )
                        obj.put(
                            DatabaseHelper.COLUMN_FAVOURITE_VALUE, dataFavourite.getInt(
                                dataFavourite.getColumnIndex(
                                    DatabaseHelper.COLUMN_FAVOURITE_VALUE
                                )
                            )
                        )
                        objFav.put(obj)
                    } while (dataFavourite.moveToNext() && !dataFavourite.isAfterLast)
                }
            }
            if (dataSession != null) {
                if (dataSession.moveToFirst()) {
                    do {
                        val obj = JSONObject()
                        obj.put(
                            DatabaseHelper.COLUMN_SESSION_ID, dataSession.getInt(
                                dataSession.getColumnIndex(
                                    DatabaseHelper.COLUMN_SESSION_ID
                                )
                            )
                        )
                        obj.put(
                            DatabaseHelper.COLUMN_SESSION_NAME, dataSession.getString(
                                dataSession.getColumnIndex(
                                    DatabaseHelper.COLUMN_SESSION_NAME
                                )
                            )
                        )
                        obj.put(
                            DatabaseHelper.COLUMN_SESSION_TYPE, dataSession.getString(
                                dataSession.getColumnIndex(
                                    DatabaseHelper.COLUMN_SESSION_TYPE
                                )
                            )
                        )
                        obj.put(
                            DatabaseHelper.COLUMN_SESSION_USER, dataSession.getString(
                                dataSession.getColumnIndex(
                                    DatabaseHelper.COLUMN_SESSION_USER
                                )
                            )
                        )
                        obj.put(
                            DatabaseHelper.COLUMN_SESSION_VALUE, dataSession.getString(
                                dataSession.getColumnIndex(
                                    DatabaseHelper.COLUMN_SESSION_VALUE
                                )
                            )
                        )
                        objSession.put(obj)
                    } while (dataSession.moveToNext() && !dataSession.isAfterLast)
                }
            }

            val ti = Date().time
            data.put("backupId", UUID.randomUUID())
            data.put("backupTime", ti)
            data.put("appUserInfo", ls.getValueString("appUserInfo"))
            appSettings.put("isAutoDownloadEnabled", ls.getValueBoolean("isAutoDownloadEnabled"))
            appSettings.put(
                "isNestedDownloadEnabled",
                ls.getValueBoolean("isNestedDownloadEnabled")
            )
            appSettings.put(
                "isRecentHistoryDisabled",
                ls.getValueBoolean("isRecentHistoryDisabled")
            )
            appSettings.put("isPrivacyModeEnabled", ls.getValueBoolean("isPrivacyModeEnabled"))
            dataDb.put("dbSession", objSession)
            dataDb.put("dbFavourite", objFav)
            data.put("database", dataDb)
            data.put("settings", appSettings)
            val filename = "backup_${ti}.json"

            try {
                val backupDir = File(Environment.getExternalStorageDirectory(), "Backup")
                val dir = File(backupDir, "bot")
                if (!(dir.exists())) {
                    dir.mkdirs()
                }
                val backupFile = File(dir.toString(), filename)
                val fileOutputStream = FileOutputStream(backupFile)
                fileOutputStream.write(data.toString().toByteArray())
                fileOutputStream.flush()
                fileOutputStream.close()
                Toast.makeText(context, "Backup completed", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                Toast.makeText(context, "Failed to save backup", Toast.LENGTH_SHORT).show()
            }

        } catch (e: SQLiteException) {
            Log.e("BOT", "DB failed")
        }
    }

    fun getCommonApiHeader(): HashMap<String, String> {
        val params = HashMap<String, String>()
        params["Content-Type"] = "application/json"
        params["Accept"] = "application/json"
        params["Access-Control-Allow-Origin"] = "true"
        params["Crafty-Device-Id"] = this@AppHelper.getDeviceId()
        params["Crafty-Request-Id"] = UUID.randomUUID().toString()
        return params
    }

    fun getDefaultApiHeader(): HashMap<String, String> {
        val params = HashMap<String, String>()
        try {
            params["Content-Type"] = "application/json"
            params["Accept"] = "application/json"
            params["Access-Control-Allow-Origin"] = "true"

        } catch (e: JSONException) {
            //
        }

        return params
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun encodeBase64(text: String): String {
        val encoder: Base64.Encoder = Base64.getEncoder()
        return encoder.encode(text.toByteArray()).toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun decodeBase64(text: String): String {
        val decoder: Base64.Decoder = Base64.getDecoder()
        val decodedBytes: ByteArray = decoder.decode(text)
        return String(decodedBytes)
    }

    private fun String.toMD5(): String {
        val bytes = MessageDigest.getInstance("MD5").digest(this.toByteArray())
        return bytes.toHex()
    }

    private fun ByteArray.toHex(): String {
        return joinToString("") { "%02x".format(it) }
    }

    fun generateMd5(): String {
        return UUID.randomUUID().toString().toMD5()
    }

    fun getFavourites() {
        val stringRequest = object : StringRequest(
            Method.GET,
            "https://bot.planckstudio.in/.well-known/android/appfav.json",
            Response.Listener<String> {
                val jsonObject = JSONObject(it)
                val rStatus = jsonObject.getString("status")
                if (rStatus == "success") {
                    val fav = jsonObject.getString("favourites")
                    LocalStorage(context).removeValue("promotedFavourites")
                    LocalStorage(context).save("promotedFavourites", fav)
                }
            },
            Response.ErrorListener {
                //
            }
        ) {
        }
        stringRequest.setShouldCache(true)
        mRequestQueue.add(stringRequest)
    }

    fun findBinary(binaryName: String): Boolean {
        var found = false
        if (!found) {
            val places = arrayOf(
                "/sbin/", "/system/bin/", "/system/xbin/",
                "/data/local/xbin/", "/data/local/bin/",
                "/system/sd/xbin/", "/system/bin/failsafe/", "/data/local/"
            )
            for (where in places) {
                if (File(where + binaryName).exists()) {
                    found = true
                    break
                }
            }
        }
        return found
    }

    fun isRooted(): Boolean {
        return findBinary("su")
    }
}