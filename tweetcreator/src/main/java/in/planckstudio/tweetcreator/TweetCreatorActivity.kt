package `in`.planckstudio.tweetcreator

import `in`.planckstudio.tweetcreator.chroma.ChromaDialog
import `in`.planckstudio.tweetcreator.chroma.ColorMode
import `in`.planckstudio.tweetcreator.chroma.ColorSelectListener
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*
import kotlin.math.abs

class TweetCreatorActivity : AppCompatActivity() {

    private lateinit var mSettingCardCornerButton: MaterialCardView
    private lateinit var mSettingTextColorButton: MaterialCardView

    private lateinit var mSettingAddImageView: MaterialCardView
    private lateinit var mSettingCardCornerView: MaterialCardView
    private lateinit var mSettingImageCornerView: MaterialCardView
    private lateinit var mSettingTextColorView: MaterialCardView
    private lateinit var mSettingHideImageView: MaterialCardView

    private lateinit var mContentSaveButton: MaterialButton
    private lateinit var mContentHolder: MaterialCardView
    private lateinit var mContentCardHolder: MaterialCardView

    private lateinit var mContentTitle: MaterialTextView
    private lateinit var mContentSubtitle: MaterialTextView
    private lateinit var mContentCaption: MaterialTextView
    private lateinit var mContentImageHolder: ImageView
    private lateinit var mContentImageCard: MaterialCardView

    private lateinit var mContentTitleImage: MaterialTextView
    private lateinit var mContentSubtitleImage: MaterialTextView
    private lateinit var mContentCaptionImage: MaterialTextView
    private lateinit var mContentExtraProfileImage: ImageView
    private lateinit var mContentImageHolderExtraImage: ImageView
    private lateinit var mContentExtraProfileCard: MaterialCardView
    private lateinit var mContentExtraImageCard: MaterialCardView

    private lateinit var mContentSingleView: LinearLayout
    private lateinit var mContentImageView: LinearLayout

    private lateinit var mContentMode: SwitchMaterial
    private lateinit var mContentImage: SwitchMaterial
    private lateinit var mContentCorner: SwitchMaterial
    private lateinit var mContentImageCorner: SwitchMaterial
    private lateinit var mContentTextColor: MaterialCardView

    private val PICKIMAGE = 27
    private var mColor = 0
    private var mColorCard = 0
    private var mColorText = 0
    private var imageType = "main"
    private var imageUri: Uri? = null

    private lateinit var mImageOne: ImageView
    private lateinit var mImageThree: ImageView
    private lateinit var mQuery: String

    private lateinit var mRequestQueue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tweet_creator)
        init()
        main()
    }

    private fun init() {

        mQuery = ""

        this.mRequestQueue = Volley.newRequestQueue(this)

        mSettingCardCornerButton = findViewById(R.id.tweetSettingCardCornerButton)
        mSettingTextColorButton = findViewById(R.id.tweetSettingTextColorButton)

        mSettingCardCornerView = findViewById(R.id.tweetCreatorSettingCardCornerContent)
        mSettingImageCornerView = findViewById(R.id.tweetCreatorSettingImageCornerContent)
        mSettingTextColorView = findViewById(R.id.tweetCreatorSettingTextColorContent)

        mContentSaveButton = findViewById(R.id.toolTextCreatorSaveButton)
        mContentHolder = findViewById(R.id.tweetCreatorHolder)
        mContentCardHolder = findViewById(R.id.tweetCreatorCardHolder)
        mContentImageHolder = findViewById(R.id.cardTextCreatorImageHolder)

        mContentTitle = findViewById(R.id.cardTextCreatorTitle)
        mContentSubtitle = findViewById(R.id.cardTextCreatorSubtitle)
        mContentCaption = findViewById(R.id.cardTextCreatorCaption)
        mContentImageCard = findViewById(R.id.cardTextCreatorImageCard)

        mContentTitleImage = findViewById(R.id.cardTextCreatorImageTitleImage)
        mContentSubtitleImage = findViewById(R.id.cardTextCreatorImageSubtitle)
        mContentCaptionImage = findViewById(R.id.cardTextCreatorImageCaption)
        mContentExtraProfileImage = findViewById(R.id.cardTextCreatorExtraProfileImage)
        mContentImageHolderExtraImage = findViewById(R.id.holderTweetImage)
        mContentExtraProfileCard = findViewById(R.id.cardTextCreatorExtraProfileImageHolder)
        mContentExtraImageCard = findViewById(R.id.holderTweetImageImageHolder)

        mContentSingleView = findViewById(R.id.toolTweetCreatorTypeSingle)
        mContentImageView = findViewById(R.id.toolTweetCreatorTypeImage)
        mContentImage = findViewById(R.id.toolTextCreatorAddImageSwitch)

        mContentCorner = findViewById(R.id.toolTextCreatorDisableBorderSwitch)
        mContentImageCorner = findViewById(R.id.toolTextCreatorDisableBorderImageSwitch)
        mContentTextColor = findViewById(R.id.toolTextCreatorTextColorButton)

        mImageOne = findViewById(R.id.toolTextCreatorImageOne)
        mImageThree = findViewById(R.id.toolTextCreatorImageThree)
    }

    private fun main() {

        renderTweetView()

        mColor = resources.getColor(R.color.colorPrimary)
        mColorCard = resources.getColor(R.color.white)
        mColorText = resources.getColor(R.color.black)

        val intent = intent
        val extra = intent.extras

        if (extra != null) {
            val query = intent.getStringExtra("query").toString()
            getTweetData(query)
        }

        mSettingCardCornerButton.setOnClickListener {
            changeSettingView("corner")
        }

        mSettingTextColorButton.setOnClickListener {
            changeSettingView("textcolor")
        }

        mContentSaveButton.setOnClickListener {
            saveImage()
        }

        mContentTextColor.setOnClickListener {
            showColorPickerDialogForText()
        }

        mContentHolder.setOnClickListener {
            showColorPickerDialog()
        }

        mContentCardHolder.setOnClickListener {
            showColorPickerDialog(true)
        }

        mContentImageHolder.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            imageType = "main"
            startActivityForResult(gallery, PICKIMAGE)
        }

        mContentExtraProfileImage.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            imageType = "extra main"
            startActivityForResult(gallery, PICKIMAGE)
        }

        mContentImageHolderExtraImage.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            imageType = "extra"
            startActivityForResult(gallery, PICKIMAGE)
        }

        mContentTitle.setOnClickListener {
            showChangeTextDialog("title")
        }

        mContentSubtitle.setOnClickListener {
            showChangeTextDialog("subtitle")
        }

        mContentCaption.setOnClickListener {
            showChangeTextDialog("caption")
        }

        mContentTitleImage.setOnClickListener {
            showChangeTextDialog("title extra")
        }

        mContentSubtitleImage.setOnClickListener {
            showChangeTextDialog("subtitle extra")
        }

        mContentCaptionImage.setOnClickListener {
            showChangeTextDialog("caption extra")
        }

        this.mContentImageCorner.setOnCheckedChangeListener { buttonView, isChecked ->

            if (isChecked) {
                Glide.with(this).load(mImageOne.drawable.toBitmap()).into(mContentImageHolder)
                Glide.with(this).load(mImageOne.drawable.toBitmap()).into(mContentExtraProfileImage)
                mContentImageCard.apply {
                    radius = 0.toFloat()
                }
                mContentExtraProfileCard.apply {
                    radius = 0.toFloat()
                }
                mContentExtraImageCard.apply {
                    radius = 0.toFloat()
                }
            } else {
                Glide.with(this).load(mImageOne.drawable.toBitmap()).circleCrop()
                    .into(mContentImageHolder)
                Glide.with(this).load(mImageOne.drawable.toBitmap()).circleCrop()
                    .into(mContentExtraProfileImage)
                mContentImageCard.apply {
                    radius = 200.toFloat()
                }
                mContentExtraProfileCard.apply {
                    radius = 200.toFloat()
                }
                mContentExtraImageCard.apply {
                    radius = 32.toFloat()
                }
            }
        }

        this.mContentCorner.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                mContentCardHolder.apply {
                    radius = 0.toFloat()
                }
            } else {
                mContentCardHolder.apply {
                    radius = 32.toFloat()
                }
            }
        }

        this.mContentImage.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                mContentSingleView.visibility = View.GONE
                mContentImageView.visibility = View.VISIBLE
                mContentTitleImage.text = mContentTitle.text
                mContentSubtitleImage.text = mContentSubtitle.text
                mContentCaptionImage.text = mContentCaption.text
            } else {
                mContentSingleView.visibility = View.VISIBLE
                mContentImageView.visibility = View.GONE
                mContentMode.isChecked = false
                mContentMode.isEnabled = true
                mContentTitle.text = mContentTitleImage.text
                mContentSubtitle.text = mContentSubtitleImage.text
                mContentCaption.text = mContentCaptionImage.text
            }
        }

    }

    private fun getTweetData(query: String) {
        val stringRequest = object : StringRequest(
            Method.POST,
            "https://bot.planckstudio.in/api/v2/",
            Response.Listener<String> {
                try {
                    val jsonObject = JSONObject(it)
                    Log.e("crafty", it.toString())
                    when (jsonObject.getInt("code")) {
                        200 -> {
                            val result = jsonObject
                                .getJSONObject("result")
                                .getJSONObject("twitter")
                                .getJSONObject("data")

                            val tweetUser = result.getJSONObject("includes").getJSONArray("users")
                                .getJSONObject(0)
                            val tweetIncludes = result.getJSONObject("includes")
                            val tweetData = result.getJSONObject("data")
                            val tweetUserProfileImage = tweetUser.getString("profile_image_url")

                            this.mContentTitle.text = tweetUser.getString("name")
                            this.mContentSubtitle.text = tweetUser.getString("username")
                            this.mContentCaption.text = tweetData.getString("text")
                            setDefaultImages("one", tweetUserProfileImage)

                            if (tweetIncludes.has("media")) {
                                val tweetMedia =
                                    tweetIncludes.getJSONArray("media").getJSONObject(0)
                                val tweetMediaImage = tweetMedia.getString("url")
                                setDefaultImages("three", tweetMediaImage)

                                mContentSingleView.visibility = View.GONE
                                mContentImageView.visibility = View.VISIBLE
                                mContentImage.isEnabled = true
                                mContentImage.isChecked = true
                                mContentTitleImage.text = mContentTitle.text
                                mContentSubtitleImage.text = mContentSubtitle.text
                                mContentCaptionImage.text = mContentCaption.text
                            }
                        }
                        404 -> {
                            Log.e("crafty", "Request not found")
                        }
                        401 -> {
                            Log.e("crafty", "Access Denaid")
                        }
                    }

                } catch (e: JSONException) {
                    Log.e("crafty", "Request data unknown")
                }
            },
            Response.ErrorListener {
                Log.e("crafty", "Request not found")
            }) {

            override fun getBody(): ByteArray {

                val jsonBody = JSONObject()
                val jsonQuery = JSONObject()
                jsonQuery.put("id", query)
                jsonBody.put("type", "tweet")
                jsonBody.put("param", jsonQuery)
                mQuery = jsonBody.toString()
                return mQuery.toByteArray()
            }

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String>? {
                val params = HashMap<String, String>()
                params["Content-Type"] = "application/json"
                params["Accept"] = "application/json"
                params["Access-Control-Allow-Origin"] = "true"
                return params
            }
        }
        val retryPolicy: RetryPolicy = DefaultRetryPolicy(15000, 3, 1F)
        stringRequest.setShouldCache(false)
        mRequestQueue.add(stringRequest).retryPolicy = retryPolicy
    }

    public fun setDefaultImages(type: String, url: String) {
        when (type) {
            "one" -> {
                Glide.with(this).load(url).into(mImageOne)
            }
            "three" -> {
                Glide.with(this).load(url).into(mImageThree)
            }
        }

        if (mContentImageCorner.isChecked) {
            when (type) {
                "one" -> {
                    Glide.with(this).load(url)
                        .into(mContentImageHolder)
                    Glide.with(this).load(url)
                        .into(mContentExtraProfileImage)
                }
                "three" -> {
                    Glide.with(this).load(url)
                        .into(mContentImageHolderExtraImage)
                }
            }
        } else {
            when (type) {
                "one" -> {
                    Glide.with(this).load(url).circleCrop()
                        .into(mContentImageHolder)
                    Glide.with(this).load(url).circleCrop()
                        .into(mContentExtraProfileImage)
                }
                "three" -> {
                    Glide.with(this).load(url)
                        .into(mContentImageHolderExtraImage)
                }
            }
        }
    }

    private fun changeSettingView(s: String) {
        mSettingCardCornerView.visibility = View.GONE
        mSettingImageCornerView.visibility = View.GONE
        mSettingTextColorView.visibility = View.GONE

        when (s) {
            "addimage" -> {
                mSettingAddImageView.visibility = View.VISIBLE
                mSettingHideImageView.visibility = View.VISIBLE
            }
            "corner" -> {
                mSettingCardCornerView.visibility = View.VISIBLE
                mSettingImageCornerView.visibility = View.VISIBLE
            }
            "textcolor" -> {
                mSettingTextColorView.visibility = View.VISIBLE
            }
        }
    }

    private fun renderTweetView() {
        when (intent.getStringExtra("type")) {
            "single" -> {
                mContentSingleView.visibility = View.VISIBLE
                mContentImageView.visibility = View.GONE
                mContentImage.isEnabled = true
                mContentImage.isChecked = false
                mContentMode.isChecked = false
                mContentMode.isEnabled = true

            }
            "image" -> {
                mContentSingleView.visibility = View.GONE
                mContentImageView.visibility = View.VISIBLE
                mContentImage.isChecked = true
                mContentImage.isEnabled = true
                mContentMode.isChecked = false
                mContentMode.isEnabled = false
            }
        }

    }

    fun forceChangeTextColor(color: Int) {
        mColorText = color
        mContentTextColor.setCardBackgroundColor(color)
        mContentTitle.setTextColor(color)
        mContentSubtitle.setTextColor(color)
        mContentCaption.setTextColor(color)
        mContentTitleImage.setTextColor(color)
        mContentSubtitleImage.setTextColor(color)
        mContentCaptionImage.setTextColor(color)
    }

    private fun showColorPickerDialogForText() {
        ChromaDialog.Builder()
            .initialColor(mColorText)
            .colorMode(ColorMode.RGB)
            .onColorSelected(object : ColorSelectListener {
                override fun onColorSelected(color: Int) {
                    mColorText = color
                    mContentTextColor.setCardBackgroundColor(color)
                    mContentTitle.setTextColor(color)
                    mContentSubtitle.setTextColor(color)
                    mContentCaption.setTextColor(color)
                    mContentTitleImage.setTextColor(color)
                    mContentSubtitleImage.setTextColor(color)
                    mContentCaptionImage.setTextColor(color)
                }
            })
            .create()
            .show(supportFragmentManager, "dialog")
    }

    private fun showColorPickerDialog(card: Boolean = false) {
        var cl = 0
        cl = if (card) {
            mColorCard
        } else {
            this.mColor
        }

        ChromaDialog.Builder()
            .initialColor(cl)
            .colorMode(ColorMode.RGB)
            .onColorSelected(object : ColorSelectListener {
                override fun onColorSelected(color: Int) {
                    if (card) {
                        mColorCard = color

                        mContentCardHolder.setCardBackgroundColor(color)
                        mContentCardHolder.isDrawingCacheEnabled = true
                        mContentCardHolder.buildDrawingCache()
                        val bitmap: Bitmap = Bitmap.createBitmap(mContentCardHolder.drawingCache)
                        val palette: Palette =
                            Palette.from(
                                Bitmap.createScaledBitmap(
                                    bitmap, 100, 100, true
                                )
                            )
                                .generate()
                        val vibrant = palette.vibrantSwatch
                        vibrant?.let {
                            forceChangeTextColor(it.bodyTextColor)
                            mColorText = it.bodyTextColor
                        }
                    } else {
                        mColor = color
                        mContentHolder.setCardBackgroundColor(color)
                    }
                }
            })
            .create()
            .show(supportFragmentManager, "dialog")
    }

    @SuppressLint("InflateParams")
    @Suppress("NAME_SHADOWING")
    fun showChangeTextDialog(type: String) {
        val v: View = LayoutInflater.from(this).inflate(
            R.layout.dialog_text_creator_input,
            null
        )
        val input = v.findViewById<TextInputEditText>(R.id.dialogTweetCreatorInput)

        var current = ""
        when (type) {
            "title" -> {
                current = mContentTitle.text.toString()
            }
            "subtitle" -> {
                current = mContentSubtitle.text.toString()
            }
            "caption" -> {
                current = mContentCaption.text.toString()
            }
            "title extra" -> {
                current = mContentTitleImage.text.toString()
            }
            "subtitle extra" -> {
                current = mContentSubtitleImage.text.toString()
            }
            "caption extra" -> {
                current = mContentCaptionImage.text.toString()
            }
        }

        input.setText(current)

        val title = "Change $type"
        val builder: AlertDialog.Builder =
            AlertDialog
                .Builder(this).setView(v)
                .setView(v)
                .setTitle(title)
                .setPositiveButton("Change") { dialog, which ->
                    val inputValue = input.text.toString()
                    when (type) {
                        "title" -> {
                            mContentTitle.text = inputValue
                        }
                        "subtitle" -> {
                            mContentSubtitle.text = inputValue
                        }
                        "caption" -> {
                            mContentCaption.text = inputValue
                        }
                        "title extra" -> {
                            mContentTitleImage.text = inputValue
                        }
                        "subtitle extra" -> {
                            mContentSubtitleImage.text = inputValue
                        }
                        "caption extra" -> {
                            mContentCaptionImage.text = inputValue
                        }
                    }
                }
                .setNegativeButton("CANCEL") { dialog: DialogInterface, which: Int -> }
        builder.show()
    }

    private fun setTweetData(url: String, type: String) {

        val baseUrl = "https://api.twitter.com/2/tweets?"
        val tweeturl = url.split("/")
        val tweetid = tweeturl[5].split("?")
        val id = tweetid[0]

        val stringRequest = object : StringRequest(
            Method.GET,
            "https://bot.planckstudio.in/api/app/flags/promo.php",
            Response.Listener<String> {
                val jsonObject = JSONObject(it)
                //val rStatus = jsonObject.getString("status")
            },
            Response.ErrorListener {
                Log.e("crafty", "api failed")
            }
        ) {
        }
        val retryPolicy: RetryPolicy = DefaultRetryPolicy(6000, 3, 1F)
        stringRequest.setShouldCache(false)
        mRequestQueue.add(stringRequest)
    }

    private fun saveImage() {

        val mediaStorageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "BOT"
        )
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e("BOT", "Failed to create directory")
            }
        }

        val timestamp = Date().time.toString()
        val title = ("TWEET_CREATOR_$timestamp.jpg")
        val selectedOutputPath = mediaStorageDir.path + File.separator + title

        mContentHolder.isDrawingCacheEnabled = true
        mContentHolder.buildDrawingCache()
        var bitmap: Bitmap = Bitmap.createBitmap(mContentHolder.drawingCache)
        val maxSize = 1080
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
        mContentHolder.isDrawingCacheEnabled = false
        mContentHolder.destroyDrawingCache()

        var fOut: OutputStream? = null
        try {
            val file = File(selectedOutputPath)
            fOut = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
            fOut.flush()
            fOut.close()
            Toast.makeText(this, "Saved to Pictures -> BOT", Toast.LENGTH_SHORT).show()
            addToGallery(selectedOutputPath)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Saving failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addToGallery(path: String) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val f: File = File(path)
        val contentUri = Uri.fromFile(f)
        mediaScanIntent.data = contentUri
        this.sendBroadcast(mediaScanIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == PICKIMAGE) {
            imageUri = data?.data

            when (imageType) {
                "main" -> {
                    mImageOne.setImageURI(imageUri)
                }
                "extra main" -> {
                    mImageOne.setImageURI(imageUri)
                }
                "extra" -> {
                    mImageThree.setImageURI(imageUri)
                }
            }

            if (mContentImageCorner.isChecked) {
                when (imageType) {
                    "main" -> {
                        Glide.with(this).load(imageUri).into(mContentImageHolder)
                        Glide.with(this).load(imageUri).into(mContentExtraProfileImage)
                    }
                    "extra main" -> {
                        Glide.with(this).load(imageUri).into(mContentImageHolder)
                        Glide.with(this).load(imageUri).into(this.mContentExtraProfileImage)
                    }

                }
            } else {
                when (imageType) {
                    "main" -> {
                        Glide.with(this).load(mImageOne.drawable.toBitmap()).circleCrop()
                            .into(mContentImageHolder)
                        Glide.with(this).load(mImageOne.drawable.toBitmap()).circleCrop()
                            .into(mContentExtraProfileImage)

                    }
                    "extra main" -> {
                        Glide.with(this).load(mImageOne.drawable.toBitmap()).circleCrop()
                            .into(mContentImageHolder)
                        Glide.with(this).load(mImageOne.drawable.toBitmap()).circleCrop()
                            .into(this.mContentExtraProfileImage)
                    }
                    "extra" -> {
                        Glide.with(this).load(mImageThree.drawable.toBitmap())
                            .into(mContentImageHolderExtraImage)
                    }
                }
            }
        }
    }
}