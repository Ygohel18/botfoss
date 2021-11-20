package `in`.planckstudio.foss.bot.ui.view

import `in`.planckstudio.foss.bot.R
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView

class UserCardView(private val context: Context) {
    val card = MaterialCardView(context)
    private val cardLayout = LinearLayout(context)
    private val cardTextLayout = LinearLayout(context)
    private val cardImage = ImageView(context)
    val cardSubtitle = MaterialTextView(context)
    val cardTitle = MaterialTextView(context)

    init {
        val imageParams = LinearLayout.LayoutParams(150, 150)
        imageParams.marginEnd = 32
        cardImage.layoutParams = imageParams
        cardLayout.gravity = Gravity.CENTER

        val cParam = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        cardLayout.layoutParams = cParam
        cardTextLayout.layoutParams = cParam
        cardTitle.layoutParams = cParam
        cardSubtitle.layoutParams = cParam

        cardTextLayout.orientation = LinearLayout.VERTICAL
        cardLayout.orientation = LinearLayout.HORIZONTAL

        cardTitle.setTextColor(Color.BLACK)
        cardSubtitle.setTextColor(Color.BLACK)

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        layoutParams.apply {
            setMargins(0, 0, 0, 0)
        }
        card.layoutParams = layoutParams

        card.setCardBackgroundColor(Color.WHITE)
        card.elevation = 0f
        card.setContentPadding(32, 32, 32, 32)
        card.isClickable = true

        card.apply {
            radius = 16.toFloat()
            alpha = 0f
            visibility = View.VISIBLE
            animate().alpha(1f).setDuration(1000).setListener(null)
        }
    }

    fun getCardTitle(): String {
        return this.cardTitle.text.toString()
    }

    fun getCardSubtitle(): String {
        return this.cardSubtitle.text.toString()
    }

    fun setCardTitle(title: String) {
        this.cardTitle.text = title
    }

    fun setCardSubtitle(title: String) {
        this.cardSubtitle.text = title
    }

    fun setCardImage(uri: Bitmap?) {
        Glide.with(context)
            .load(uri)
            .circleCrop()
            .into(cardImage)
    }

    fun setCardImage(uri: Int) {
        Glide.with(context)
            .load(uri)
            .circleCrop()
            .into(cardImage)
    }

    fun setCardImage(uri: String) {
        Glide.with(context)
            .load(uri)
            .circleCrop()
            .into(cardImage)
    }


    @JvmName("getCard1")
    fun getCard(): MaterialCardView {
        return this.card
    }

    fun show() {
        this.cardTextLayout.addView(this.cardTitle)
        this.cardTextLayout.addView(this.cardSubtitle)
        cardLayout.addView(cardImage)
        cardLayout.addView(cardTextLayout)
        this.card.addView(this.cardLayout)
    }

    fun hideCard() {
        this.card.visibility = View.GONE
    }

    fun setCardBorder() {
        card.apply {
            strokeColor = resources.getColor(R.color.colorPrimary)
            strokeWidth = 4
            alpha = 0f
            visibility = View.VISIBLE
            animate().alpha(1f).setDuration(1000).setListener(null)
        }
    }
}