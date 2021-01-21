package com.example.getschedule

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    val reqUrl = "https://api.vk.com/method/board.getComments?group_id=32678121&topic_id=40272010&count=10&sort=desc&access_token=dfa9cba1dfa9cba1dfa9cba131dfdc346addfa9dfa9cba1bfaf71782f6116399c0ef601&v=5.126"
    lateinit var queue: RequestQueue
    lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.textView2)
        queue = Volley.newRequestQueue(this)
    }

    fun openSchedule(view: View) {
        //Request
        //https://api.vk.com/method/board.getComments?group_id=32678121&topic_id=40272010&count=10&sort=desc&access_token=dfa9cba1dfa9cba1dfa9cba131dfdc346addfa9dfa9cba1bfaf71782f6116399c0ef601&v=5.126
        //webView.loadUrl("https://www.google.com/")
        GlobalScope.launch {
            val dUrl = retrieveLink()
            val future = RequestFuture.newFuture<ByteArray>()
            val downloadRequest = InputStreamVolleyRequest(Request.Method.GET, dUrl, future, future, null)
            queue.add(downloadRequest)

            try {
                val response = future.get()
                if (response != null) {
                    val filename = "downloaded.doc"
                    var outputStream = openFileOutput(filename, Context.MODE_PRIVATE)
                    outputStream.write(response)
                    outputStream.close()
                    runOnUiThread {
                        textView.text = filesDir.absolutePath
                    }
                }
            } catch (e: Exception) {
                textView.text = "Download Failed"
            }
        }
    }

    suspend fun retrieveLink():String? {
        val future = RequestFuture.newFuture<JSONObject>()
        val request = JsonObjectRequest(Request.Method.GET, reqUrl, null, future, future)
        var str = ""
        queue.add(request)

        try {
            val response = future.get()
            str = response.getJSONObject("response").getJSONArray("items").getJSONObject(0)
                    .getJSONArray("attachments").getJSONObject(0).getJSONObject("doc").getString("url")
        } catch (e: Exception) {
            return null
        }
        return str
    }
}
