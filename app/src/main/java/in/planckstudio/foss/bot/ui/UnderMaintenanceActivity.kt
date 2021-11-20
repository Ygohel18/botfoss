package `in`.planckstudio.foss.bot.ui

import `in`.planckstudio.foss.bot.R
import `in`.planckstudio.foss.bot.helper.AppHelper
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.util.*

class UnderMaintenanceActivity : AppCompatActivity() {

    private lateinit var mSupportBtn: MaterialButton
    private lateinit var title: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_under_maintenance)

        this.mSupportBtn = findViewById(R.id.supportButton)
        this.title = findViewById(R.id.maintenanceTitle)

        val intent = intent
        val extra = intent.extras
        if (extra != null) {
            this.title.text = intent.getStringExtra("message").toString()
        }

        this.mSupportBtn.setOnClickListener {
            val uri: Uri =
                Uri.parse(
                    "mailto:bot@planckstudio.in?subject=Need%20Support&body=User%20ID:%20${
                        AppHelper(
                            this
                        ).getDeviceId()
                    }%0A%0ARequest%20ID:%20${UUID.randomUUID()}%0A%0AWrite%20your%20query%20or%20feedback%20here%0A%0AHello,%0A"
                )
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(Intent.createChooser(intent, "Get support"))
        }
    }
}