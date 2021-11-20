package `in`.planckstudio.tweetcreator.chroma

import androidx.annotation.ColorInt

interface ColorSelectListener {
    fun onColorSelected(@ColorInt color: Int)
}