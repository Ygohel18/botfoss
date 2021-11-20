package `in`.planckstudio.foss.bot.ui.instagram

import `in`.planckstudio.foss.bot.R
import `in`.planckstudio.foss.bot.helper.AppHelper
import `in`.planckstudio.foss.bot.util.LocalStorage
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.shape.CornerFamily
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.DelicateCoroutinesApi
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*
import kotlin.math.abs

@DelicateCoroutinesApi
class InstagramRepostActivity : AppCompatActivity() {
    private lateinit var repostImage: ImageView
    private lateinit var profileImage: ImageView
    private lateinit var username: MaterialTextView
    private lateinit var holder: MaterialCardView
    private lateinit var repostCardView: MaterialCardView
    private lateinit var repostCard: LinearLayout
    private lateinit var positionTopLeftButton: Chip
    private lateinit var positionTopRightButton: Chip
    private lateinit var positionBottomLeftButton: Chip
    private lateinit var positionBottomRightButton: Chip
    private lateinit var positionRemoveButton: Chip
    private lateinit var shareButton: Chip
    private lateinit var saveButton: Chip
    private lateinit var mUsername: String
    private lateinit var mType: String
    private lateinit var ls: LocalStorage

    private fun init() {
        AppHelper(this).preLaunchTask()
        this.ls = LocalStorage(this)
        this.repostImage = findViewById(R.id.igRepostImage)
        this.profileImage = findViewById(R.id.igRepostProfile)
        this.username = findViewById(R.id.igRepostUsername)
        this.holder = findViewById(R.id.igRepostHolder)
        this.repostCardView = findViewById(R.id.repostCard)
        this.repostCard = findViewById(R.id.igRepostCard)
        this.positionTopLeftButton = findViewById(R.id.igRepostSetTl)
        this.positionTopRightButton = findViewById(R.id.igRepostSetTr)
        this.positionBottomLeftButton = findViewById(R.id.igRepostSetBl)
        this.positionBottomRightButton = findViewById(R.id.igRepostSetBr)
        this.positionRemoveButton = findViewById(R.id.igRepostSetRemove)
        this.shareButton = findViewById(R.id.igRepostShare)
        this.saveButton = findViewById(R.id.igRepostSave)
        mUsername = ""
        mType = ""
    }

    @SuppressLint("SetTextI18n")
    private fun main() {
        val intent = intent
        val extra = intent.extras

        if (extra != null) {
            mUsername = intent.getStringExtra("username").toString()
            val url = intent.getStringExtra("url").toString()
            val profile = intent.getStringExtra("profile").toString()
            val mType = intent.getStringExtra("type").toString()

            if (mType == "por") {
                val layoutParams = holder.layoutParams as ConstraintLayout.LayoutParams
                layoutParams.dimensionRatio = "4:5"
            }

            Glide.with(this)
                .load(url)
                .placeholder(R.drawable.placeholder_portrait)
                .into(this.repostImage)

            Glide.with(this)
                .load(profile)
                .placeholder(R.drawable.placeholder_thumb)
                .circleCrop()
                .into(this.profileImage)

            this.username.text = "@$mUsername"
            setCardCorners(0, CORNER_RADIUS, 0, 0)

        } else {
            Toast.makeText(this, "Invalid username", Toast.LENGTH_SHORT).show()
        }

        this.positionTopLeftButton.setOnClickListener {
            setCardPosition("tl")
        }

        this.positionTopRightButton.setOnClickListener {
            setCardPosition("tr")
        }

        this.positionBottomLeftButton.setOnClickListener {
            setCardPosition("bl")
        }

        this.positionBottomRightButton.setOnClickListener {
            setCardPosition("br")
        }

        this.positionRemoveButton.setOnClickListener {
            setCardPosition("remove")
        }

        this.saveButton.setOnClickListener {
            saveImage()
        }

        this.shareButton.setOnClickListener {
            saveImage(true)
        }
    }

    private fun saveImage(share: Boolean = false) {

        val mediaStorageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "BOT"
        )
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e("PBOT", "Failed to create directory")
            }
        }

        val timestamp = Date().time.toString()
        val title = ("REPOST_$mUsername$timestamp.jpg")
        val selectedOutputPath = mediaStorageDir.path + File.separator + title

        this.holder.isDrawingCacheEnabled = true
        this.holder.buildDrawingCache()
        var bitmap: Bitmap = Bitmap.createBitmap(this.holder.drawingCache)

        val maxSize = 1350

        val bWidth = bitmap.width
        val bHeight = bitmap.height

        bitmap = if (bWidth > bHeight) {
            val imageHeight = abs(
                maxSize * (bitmap.width.toFloat() / bitmap.height
                    .toFloat())
            ).toInt()
            Bitmap.createScaledBitmap(bitmap, maxSize, imageHeight, true)
        } else {
            val imageWidth = abs(
                maxSize * (bitmap.width.toFloat() / bitmap.height
                    .toFloat())
            ).toInt()
            Bitmap.createScaledBitmap(bitmap, imageWidth, maxSize, true)
        }
        this.holder.isDrawingCacheEnabled = false
        this.holder.destroyDrawingCache()

        var fOut: OutputStream? = null
        try {
            val file = File(selectedOutputPath)
            fOut = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
            fOut.flush()
            fOut.close()
            Toast.makeText(this, "Saved to Pictures -> BOT", Toast.LENGTH_SHORT).show()
            addToGallery(selectedOutputPath)

            if (share) {
                shareToInstagram(selectedOutputPath)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun shareToInstagram(path: String) {
        val type = "image/*"
        val share = Intent(Intent.ACTION_SEND)
        share.setPackage("com.instagram.android")
        share.type = type
        share.putExtra(Intent.EXTRA_STREAM, path)
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(share, "Share as"))
    }

    private fun addToGallery(path: String) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val f: File = File(path)
        val contentUri = Uri.fromFile(f)
        mediaScanIntent.data = contentUri
        this.sendBroadcast(mediaScanIntent)
    }

    private fun setCardPosition(loc: String) {
        val layoutParams = repostCard.layoutParams as RelativeLayout.LayoutParams
        layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_TOP)
        layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_LEFT)
        layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        repostCard.visibility = View.VISIBLE
        setCardCorners(0, 0, 0, 0)

        when (loc) {
            "tl" -> {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                setCardCorners(0, 0, CORNER_RADIUS, 0)
            }
            "tr" -> {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                setCardCorners(0, 0, 0, CORNER_RADIUS)
            }
            "bl" -> {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                setCardCorners(0, CORNER_RADIUS, 0, 0)
            }
            "br" -> {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                setCardCorners(CORNER_RADIUS, 0, 0, 0)
            }
            "remove" -> {
                repostCard.visibility = View.GONE
            }
        }

        repostCard.layoutParams = layoutParams
    }

    private fun setCardCorners(tl: Int, tr: Int, br: Int, bl: Int) {
        this.repostCardView.shapeAppearanceModel =
            repostCardView.shapeAppearanceModel
                .toBuilder()
                .setTopLeftCorner(CornerFamily.ROUNDED, tl.toFloat())
                .setTopRightCorner(CornerFamily.ROUNDED, tr.toFloat())
                .setBottomRightCorner(CornerFamily.ROUNDED, br.toFloat())
                .setBottomLeftCorner(CornerFamily.ROUNDED, bl.toFloat()).build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instagram_repost)
        init()
        main()
    }

    override fun onResume() {
        super.onResume()
        AppHelper(this).preLaunchTask()
    }

    companion object {
        const val CORNER_RADIUS = 24
    }
}