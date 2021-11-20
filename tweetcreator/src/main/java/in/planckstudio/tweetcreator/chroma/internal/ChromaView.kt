package `in`.planckstudio.tweetcreator.chroma.internal

import `in`.planckstudio.tweetcreator.R
import `in`.planckstudio.tweetcreator.chroma.ColorMode
import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.annotation.ColorInt

internal class ChromaView : RelativeLayout {

    companion object {
        const val DefaultColor = Color.GRAY
        val DefaultModel = ColorMode.RGB
    }

    @ColorInt
    var currentColor: Int
        private set
    val colorMode: ColorMode

    constructor(context: Context) : this(DefaultColor, DefaultModel, context)

    constructor(@ColorInt initialColor: Int, colorMode: ColorMode, context: Context?) : super(
        context
    ) {
        this.currentColor = initialColor
        this.colorMode = colorMode
        init()
    }

    private fun init(): Unit {
        inflate(context, R.layout.chroma_view, this)
        clipToPadding = false

        val colorView: View = findViewById(R.id.color_view)
        colorView.setBackgroundColor(currentColor)

        val channelViews = colorMode.channels.map { ChannelView(it, currentColor, context) }

        val seekbarChangeListener: () -> Unit = {
            currentColor = colorMode.evaluateColor(channelViews.map { it.channel })
            colorView.setBackgroundColor(currentColor)
        }

        val channelContainer = findViewById<ViewGroup>(R.id.channel_container)
        channelViews.forEach { it ->
            channelContainer.addView(it)

            val layoutParams = it.layoutParams as LinearLayout.LayoutParams
            layoutParams.topMargin = 16
            layoutParams.bottomMargin = 4

            it.registerListener(seekbarChangeListener)
        }
    }

    internal interface ButtonBarListener {
        fun onPositiveButtonClick(color: Int)
        fun onNegativeButtonClick()
    }

    internal fun enableButtonBar(listener: ButtonBarListener?): Unit {
        val v: LinearLayout = findViewById(R.id.button_bar)
        with(v) {
            val positiveButton: Button = findViewById(R.id.positive_button)
            val negativeButton: Button = findViewById(R.id.negative_button)

            if (listener != null) {
                visibility = VISIBLE
                positiveButton.setOnClickListener { listener.onPositiveButtonClick(currentColor) }
                negativeButton.setOnClickListener { listener.onNegativeButtonClick() }
            } else {
                visibility = GONE
                positiveButton.setOnClickListener(null)
                negativeButton.setOnClickListener(null)
            }
        }
    }
}