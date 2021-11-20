package `in`.planckstudio.foss.bot.ui.whatsapp

import `in`.planckstudio.foss.bot.R
import `in`.planckstudio.foss.bot.helper.AppHelper
import `in`.planckstudio.foss.bot.util.LocalStorage
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText

class WhatsappToolActivity : AppCompatActivity() {

    private lateinit var ls: LocalStorage
    private lateinit var sendBtn: MaterialCardView
    private lateinit var contactNo: TextInputEditText

    private fun init() {
        AppHelper(this).preLaunchTask()
        this.ls = LocalStorage(this)
        this.sendBtn = findViewById(R.id.waToolSendBtn)
        this.contactNo = findViewById(R.id.waContactInput)
    }

    private fun main() {
        this.sendBtn.setOnClickListener {
            sendWhatsappMessage()
        }
    }

    private fun sendWhatsappMessage() {
        val no = this.contactNo.text
        if (no.isNullOrEmpty()) {
            Toast.makeText(this, "Enter mobile no.", Toast.LENGTH_SHORT).show()
        } else {
            startWhatsapp(no.toString())
        }
    }

    private fun startWhatsapp(no: String) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://api.whatsapp.com/send?phone=$no&text=Hello,")
            )
        )
    }

    override fun onResume() {
        super.onResume()
        AppHelper(this).preLaunchTask()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_whatsapp_tool)
        init()
        main()
    }
}