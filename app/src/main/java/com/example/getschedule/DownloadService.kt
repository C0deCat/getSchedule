package com.example.getschedule

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.RequestFuture
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.util.concurrent.ExecutionException

class VkDownloadService(context: Context) {
    val api_url = "https://api.vk.com/method/board.getComments"
    val token = "dfa9cba1dfa9cba1dfa9cba131dfdc346addfa9dfa9cba1bfaf71782f6116399c0ef601"
    val version = "5.126"

    var queue: RequestQueue
    var context: Context

    init {
        this.queue = Volley.newRequestQueue(context)
        this.context = context
    }

    fun downloadSchedule() {
        val links = retrieveLinks()
        val future = RequestFuture.newFuture<ByteArray>()
    }

    private fun retrieveLinks(linkscount: Int = 10, messageschecked: Int = 20):List<Pair<String, String>> {
        //https://api.vk.com/method/board.getComments?group_id=32678121&topic_id=40272010&count=10
        // &sort=desc
        // &access_token=dfa9cba1dfa9cba1dfa9cba131dfdc346addfa9dfa9cba1bfaf71782f6116399c0ef601&v=5.126
        val future = RequestFuture.newFuture<JSONObject>()
        val requestUrl = "$api_url?group_id=32678121&topic_id=40272010&count=$messageschecked&sort=desc&access_token=$token&v=$version"
        val request = JsonObjectRequest(Request.Method.GET, requestUrl, null, future, future)
        val linksarray = mutableListOf<Pair<String, String>>()

        queue.add(request)

        try {
            val messages = future.get().getJSONObject("response").getJSONArray("items")
            for (i in 0 until messageschecked) {
                var crawler = messages.getJSONObject(i)
                if (!crawler.has("attachments")) continue

                crawler = crawler.getJSONArray("attachments").getJSONObject(0)
                if (crawler.getString("type") != "doc") continue

                crawler = crawler.getJSONObject("doc")
                linksarray.add(Pair(crawler.getString("title"), crawler.getString("url")))

                if (linksarray.size == 10) break
            }
        } catch (e: InterruptedException) {
            throw e
        } catch (e: ExecutionException) {
            throw e
        }
        return linksarray
    }
}