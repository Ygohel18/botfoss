package `in`.planckstudio.foss.bot.ui

import `in`.planckstudio.foss.bot.R
import `in`.planckstudio.foss.bot.helper.AppHelper
import `in`.planckstudio.foss.bot.helper.DatabaseHelper
import `in`.planckstudio.foss.bot.util.LocalStorage
import android.annotation.SuppressLint
import android.content.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textview.MaterialTextView
import com.google.android.play.core.review.ReviewManager
import kotlinx.coroutines.DelicateCoroutinesApi

@DelicateCoroutinesApi
class SettingActivity : AppCompatActivity() {

    private lateinit var cta: ExtendedFloatingActionButton
    private lateinit var statusText: MaterialTextView
    private lateinit var mAutoDownloadSwitch: SwitchMaterial
    private lateinit var mNestedDownloadSwitch: SwitchMaterial
    private lateinit var mRecentHistorySwitch: SwitchMaterial
    private lateinit var mPrivacyModeSwitch: SwitchMaterial
    private lateinit var mDisableAdsSwitch: SwitchMaterial
    private lateinit var mSettingAds: MaterialCardView
    private lateinit var idText: MaterialTextView
    private lateinit var ls: LocalStorage
    private lateinit var clipboardManager: ClipboardManager
    private lateinit var clipData: ClipData

    private lateinit var mSettingBackupButton: MaterialCardView
    private lateinit var mSettingRemoveButton: MaterialCardView
    private lateinit var mSettingDevPlanck: MaterialCardView
    private lateinit var mSettingReviewProduct: MaterialCardView
    private lateinit var mSettingReviewPlay: MaterialCardView
    private lateinit var mSettingDevYash: MaterialCardView
    private lateinit var mSettingAppCrafty: MaterialCardView
    private lateinit var mSettingAppAcademy: MaterialCardView
    private lateinit var mSettingIgLink: MaterialCardView
    private lateinit var mSettingPlanckLogo: ImageView
    private lateinit var mSettingAcademyLogo: ImageView
    private lateinit var mSettingYashLogo: ImageView
    private lateinit var mSettingBotLogo: ImageView
    private lateinit var mSettingCraftyLogo: ImageView
    private var mColor = 0
    private lateinit var reviewManager: ReviewManager

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        init()
        main()
    }

    override fun onResume() {
        super.onResume()
        AppHelper(this).preLaunchTask()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private fun main() {

        if (ls.getValueBoolean("isBrandColorSet")) {
            mColor = ls.getValueInt("myBrandColor")
        } else {
            mColor = resources.getColor(R.color.colorPrimary)
        }

        this.mAutoDownloadSwitch.isChecked = this.ls.getValueBoolean("isAutoDownloadEnabled")
        this.mNestedDownloadSwitch.isChecked = this.ls.getValueBoolean("isNestedDownloadEnabled")
        this.mRecentHistorySwitch.isChecked = this.ls.getValueBoolean("isRecentHistoryDisabled")
        this.mPrivacyModeSwitch.isChecked = this.ls.getValueBoolean("isPrivacyModeEnabled")
        this.mDisableAdsSwitch.isChecked = this.ls.getValueBoolean("isDisableAdsEnabled")

        cta.visibility = View.VISIBLE
        idText.text = "BOT ID: ${AppHelper(this).getDeviceId()}"

        if (intent.getStringExtra("bizarreMode") == "on") {
            mSettingAds.visibility = View.VISIBLE
        } else {
            mSettingAds.visibility = View.GONE
        }

        idText.setOnClickListener {
            this.clipboardManager =
                getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            this.clipData = ClipData.newPlainText("ID", idText.text)
            this.clipboardManager.setPrimaryClip(clipData)
            Toast.makeText(this, "Your ID Copied", Toast.LENGTH_SHORT).show()
        }

        this.mAutoDownloadSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            this.ls.save("isAutoDownloadEnabled", isChecked)
            showMsg()
        }

        this.mNestedDownloadSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            this.ls.save("isNestedDownloadEnabled", isChecked)
            showMsg()
        }

        this.mRecentHistorySwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            this.ls.save("isRecentHistoryDisabled", isChecked)
            showMsg()
        }

        this.mPrivacyModeSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            this.ls.save("isPrivacyModeEnabled", isChecked)
            showMsg()
        }

        this.mDisableAdsSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            this.ls.save("isDisableAdsEnabled", isChecked)
            showMsg()
        }

        this.mSettingBackupButton.setOnClickListener {
            AppHelper(this).localBackup()
        }

        Glide.with(this).load(R.drawable.logo_planck).circleCrop().into(mSettingPlanckLogo)
        Glide.with(this).load(R.drawable.ic_ygohel18).circleCrop().into(mSettingYashLogo)
        Glide.with(this).load(R.drawable.ic_bot_flate).circleCrop().into(mSettingBotLogo)
        Glide.with(this).load(R.drawable.ic_crafty).circleCrop().into(mSettingCraftyLogo)
        Glide.with(this).load(R.drawable.ic_academy).circleCrop().into(mSettingAcademyLogo)

        mSettingReviewPlay.setOnClickListener {
            openLink("https://links.planckstudio.in/botplayreview", "Rate us")
        }

        mSettingReviewProduct.setOnClickListener {
            openLink("https://www.producthunt.com/posts/bot-3", "BOT On Product Hunt")
        }

        mSettingAppCrafty.setOnClickListener {
            openLink("https://links.planckstudio.in/w6QPD", "Crafty On Play Store")
        }

        mSettingAppAcademy.setOnClickListener {
            openLink("https://links.planckstudio.in/LbfIo", "Academy On Play Store")
        }

        mSettingDevPlanck.setOnClickListener {
            openLink("https://www.linkedin.com/company/planckstudio", "Planck Studio On Linkedin")
        }

        mSettingDevYash.setOnClickListener {
            openLink("https://www.linkedin.com/in/ygohel18", "Yash Gohel On Linkedin")
        }

        mSettingIgLink.setOnClickListener {
            openLink("https://instagram.com/pbotapp", "BOT On Instagram")
        }

        this.mSettingRemoveButton.setOnClickListener {
            DatabaseHelper(this, null).removeAccounts()
            ls.removeValue("ig_connected")
            ls.removeValue("igSession")
            ls.removeValue("ds_user_id")
            ls.removeValue("csrftoken")
            ls.removeValue("sessionid")
            ls.removeValue("mid")
            Toast.makeText(this, "All connected accounts removed successfully", Toast.LENGTH_SHORT)
                .show()
        }

        statusText.text = "Free Subscription"

        idText.setOnClickListener {
            this.clipboardManager =
                getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            this.clipData = ClipData.newPlainText("ID", idText.text)
            this.clipboardManager.setPrimaryClip(clipData)
            Toast.makeText(this, "Your ID Copied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openLink(link: String, title: String) {
        val uri: Uri =
            Uri.parse(link)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(Intent.createChooser(intent, title))
    }

    private fun init() {
        AppHelper(this).preLaunchTask()
        this.ls = LocalStorage(this)
        cta = findViewById(R.id.settingLogoutBtn)
        this.statusText = findViewById(R.id.settingUserStatusText)
        this.mSettingAds = findViewById(R.id.settingAds)
        this.mAutoDownloadSwitch = findViewById(R.id.autoDownloadSwitch)
        this.mNestedDownloadSwitch = findViewById(R.id.folderDownloadSwitch)
        this.mRecentHistorySwitch = findViewById(R.id.searchHistorySwitch)
        this.mPrivacyModeSwitch = findViewById(R.id.privacyModeSwitch)
        this.mDisableAdsSwitch = findViewById(R.id.disableAdsSwitch)
        this.idText = findViewById(R.id.settingUid)

        this.mSettingAppCrafty = findViewById(R.id.settingAppCrafty)
        this.mSettingAppAcademy = findViewById(R.id.settingAppAcademy)
        this.mSettingDevPlanck = findViewById(R.id.settingDevPlanck)
        this.mSettingDevYash = findViewById(R.id.settingDevYash)
        this.mSettingIgLink = findViewById(R.id.settingInstagramLink)
        this.mSettingPlanckLogo = findViewById(R.id.settingLogoPlanck)
        this.mSettingAcademyLogo = findViewById(R.id.settingLogoAcademy)
        this.mSettingReviewProduct = findViewById(R.id.settingReviewProductHunt)
        this.mSettingReviewPlay = findViewById(R.id.settingReviewPlay)
        this.mSettingYashLogo = findViewById(R.id.settingLogoYash)
        this.mSettingBotLogo = findViewById(R.id.settingLogoBot)
        this.mSettingCraftyLogo = findViewById(R.id.settingLogoCrafty)
        this.mSettingBackupButton = findViewById(R.id.settingBackupButton)
        this.mSettingRemoveButton = findViewById(R.id.settingRemoveAccountButton)
    }


    fun showMsg() {
        Toast.makeText(this, "Restart the app to apply changes", Toast.LENGTH_SHORT).show()
    }
}