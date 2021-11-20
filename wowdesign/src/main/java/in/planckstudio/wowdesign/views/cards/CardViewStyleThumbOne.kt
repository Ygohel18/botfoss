package `in`.planckstudio.wowdesign.views.cards

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView

class CardViewStyleThumbOne(private var context: Context) {
    val card = MaterialCardView(context)
    private val cardLayout = RelativeLayout(context)
    private val cardImage = ImageView(context)
    private val cardTitle = MaterialTextView(context)

    init {
        this.cardLayout.gravity = Gravity.CENTER

        val layoutParams = LinearLayout.LayoutParams(200, 200)

        this.card.layoutParams = layoutParams
        this.card.setCardBackgroundColor(Color.WHITE)
        this.card.isClickable = true

        this.card.apply {
            elevation = 0f
            radius = 32.toFloat()
            alpha = 0f
            visibility = View.VISIBLE
            animate().alpha(1f).setDuration(1000).setListener(null)
        }

        val cParam = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val titleParam = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )

        titleParam.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        titleParam.addRule(RelativeLayout.CENTER_HORIZONTAL)

        this.cardLayout.layoutParams = cParam

        this.cardTitle.apply {
            backgroundTintMode = PorterDuff.Mode.SCREEN
            setBackgroundColor(Color.WHITE)
            setPadding(8, 6, 8, 6)
            textAlignment = RelativeLayout.TEXT_ALIGNMENT_CENTER
        }

        this.cardTitle.setTextColor(Color.BLACK)
        cardTitle.layoutParams = titleParam

        val imageParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        layoutParams.apply {
            setMargins(8, 16, 8, 16)
        }

        this.cardImage.layoutParams = imageParams
    }

    fun getCardTitle(): String {
        return this.cardTitle.text.toString()
    }

    fun setCardTitle(title: String) {
        this.cardTitle.text = title
    }

    fun setCardImage(uri: String) {
        Glide.with(context).load(uri).into(this.cardImage)
    }

    fun setCardImage(uri: Int) {
        Glide.with(context).load(uri).into(this.cardImage)
    }

    @JvmName("getCard1")
    fun getCard(): MaterialCardView {
        return this.card
    }

    fun show() {
        this.cardLayout.addView(this.cardImage)
        this.cardLayout.addView(this.cardTitle)
        this.card.addView(this.cardLayout)
    }

    fun hideCardTitle() {
        this.cardTitle.visibility = View.GONE
    }

    fun hideCard() {
        this.card.visibility = View.GONE
    }
}