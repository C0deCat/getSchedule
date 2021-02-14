package com.example.getschedule

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.RequestFuture
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.*
import java.io.File
import java.lang.Exception
import android.net.Uri
import android.widget.Button
import com.github.barteksc.pdfviewer.PDFView
import com.pdftron.pdf.Convert
import com.pdftron.pdf.PDFDoc
import com.pdftron.pdf.utils.Utils
import com.pdftron.sdf.SDFDoc

class MainActivity : AppCompatActivity() {
    lateinit var pdfView: PDFView
    lateinit var downloadService: VkDownloadService
    lateinit var textView: TextView
    lateinit var scheduleFiles: List<File>
    var cursor = 0
    lateinit var buttonForward: Button
    lateinit var buttonBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        scheduleFiles = listOf()
        buttonForward = findViewById(R.id.forwardButton)
        buttonBack = findViewById(R.id.backwardButton)
        textView = findViewById(R.id.textView)
        buttonControl()

        pdfView = findViewById(R.id.pdfView)
        downloadService = VkDownloadService(this)
        GlobalScope.launch {
            try {
                scheduleFiles = downloadService.downloadSchedule()
                runOnUiThread {
                    buttonControl()
                }
                pdfView.fromFile(scheduleFiles[cursor]).defaultPage(0).spacing(10).load()
            }
            catch (e: Exception) {
                runOnUiThread {
                    textView.text = getString(R.string.ErrorMessage)
                }
            }
        }
    }

    fun goForward(view: View) {
        cursor++
        buttonControl()
        pdfView.fromFile(scheduleFiles[cursor]).defaultPage(0).spacing(10).load()
    }
    fun goBack(view: View) {
        cursor--
        buttonControl()
        pdfView.fromFile(scheduleFiles[cursor]).defaultPage(0).spacing(10).load()
    }

    fun buttonControl() {
        when {
            scheduleFiles.isNullOrEmpty() -> {
                buttonForward.isEnabled = false
                buttonBack.isEnabled = false
            }
            cursor == 0 -> {
                buttonForward.isEnabled = true
                buttonBack.isEnabled = false
            }
            cursor+1 == scheduleFiles.size -> {
                buttonForward.isEnabled = false
                buttonBack.isEnabled = true
            }
            else -> {
                buttonForward.isEnabled = true
                buttonBack.isEnabled = true
            }
        }
    }
}
