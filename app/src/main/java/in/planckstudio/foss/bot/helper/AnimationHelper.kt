package `in`.planckstudio.foss.bot.helper

import android.content.Context
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation

class AnimationHelper(private val context: Context) {
    fun shake(view: View, duration: Int, offset: Int): View {
        val anim: Animation = TranslateAnimation((-offset).toFloat(), offset.toFloat(), 0f, 0f)
        anim.duration = duration.toLong()
        anim.repeatMode = Animation.REVERSE
        anim.repeatCount = 5
        view.startAnimation(anim)
        return view
    }
}