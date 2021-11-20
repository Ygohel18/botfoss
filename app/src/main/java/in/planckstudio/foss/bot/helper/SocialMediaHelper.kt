package `in`.planckstudio.foss.bot.helper

import android.util.Log

class SocialMediaHelper {

    constructor() {
        //
    }

    fun getPrettyNumber(no: Int): String {

        var prettyNumber = ""
        var postfix = ""
        var myText = ""

        when {
            no >= 1000000000 -> {
                postfix = "B"
                val ans = (no / 1000000000).toFloat()
                myText = String.format("%.1f", ans)
            }
            no >= 1000000 -> {
                postfix = "M"
                val ans = (no / 1000000).toFloat()
                Log.e("PBOT", ans.toString())
                myText = String.format("%.1f", ans)
            }
            no >= 1000 -> {
                postfix = "K"
                val ans = (no / 1000).toFloat()
                myText = String.format("%.1f", ans)
            }
            else -> {
                postfix = ""
                myText = no.toString()
            }
        }

        prettyNumber = "$myText$postfix"
        return prettyNumber
    }
}