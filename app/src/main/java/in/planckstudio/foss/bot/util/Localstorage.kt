package `in`.planckstudio.foss.bot.util

import android.content.Context
import android.content.SharedPreferences

open class LocalStorage(private val context: Context, private val name:String = "in.planckstudio.foss.bot") {
    //  Member Variables
    private var mName: String = name
    private var sharedPref: SharedPreferences =
        context.getSharedPreferences(this.mName, Context.MODE_PRIVATE)

    fun save(key: String, value: String) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun save(key: String, value: Int) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun save(key: String, value: Boolean) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getValueString(key: String): String {
        return sharedPref.getString(key, null).toString()
    }

    fun getValueInt(key: String): Int {
        return sharedPref.getInt(key, 0)
    }

    fun getValueBoolean(key: String): Boolean {
        return sharedPref.getBoolean(key, false)
    }

    fun clearSharedPreference() {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.clear()
        editor.apply()
    }

    fun removeValue(key: String) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.remove(key)
        editor.apply()
    }
}