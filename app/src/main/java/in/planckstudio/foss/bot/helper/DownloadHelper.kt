package `in`.planckstudio.foss.bot.helper

import `in`.planckstudio.foss.bot.util.LocalStorage
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import java.io.File
import java.util.*

open class DownloadHelper(private val context: Context) {
    private var destinationFolder: String = ""
    private var fileName: String = ""
    private var ls: LocalStorage = LocalStorage(context)

    fun setDestinationFolder(folder: String) {
        this.destinationFolder = folder
    }

    fun setFileName(name: String) {
        this.fileName = name
    }

    @SuppressLint("Range")
    fun download(url: String, fileName: String, title: String, dir: String = "") {
        var msg = ""
        val rootDir = File(Environment.DIRECTORY_DOWNLOADS)
        var myDir = File(rootDir.toString())

        if (ls.getValueBoolean("isNestedDownloadEnabled")) {
            myDir = File("$rootDir", "bot")

            if (dir.isNotEmpty()) {
                val dlist = dir.split("/")
                (0 until dlist.count()).forEach { d ->
                    myDir = File(myDir, dlist[d])
                }
            }

            if (!(myDir.exists())) {
                myDir.mkdirs()
            }
        }

        val timestamp = Date().time.toString()
        val fileTitle = timestamp + "__" + fileName

        val downloadManager: DownloadManager =
            context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadUri: Uri = Uri.parse(url)

        val request = DownloadManager.Request(downloadUri).apply {
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(true)
                .setTitle(title)
                .setDescription("Using PlanckBot")
                .setDestinationInExternalPublicDir(
                    myDir.toString(),
                    fileTitle
                )
        }

        val downloadId = downloadManager.enqueue(request)
        val query = DownloadManager.Query().setFilterById(downloadId)
        Thread {
            var downloading = true
            while (downloading) {
                val cursor: Cursor = downloadManager.query(query)
                cursor.moveToFirst()
                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                    downloading = false
                }
                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                cursor.close()
            }
        }.start()
    }
}