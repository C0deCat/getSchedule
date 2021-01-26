package com.example.getschedule

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.RequestFuture
import com.android.volley.toolbox.Volley
import com.pdftron.pdf.Convert
import com.pdftron.pdf.PDFDoc
import com.pdftron.sdf.SDFDoc
import org.json.JSONObject
import java.util.concurrent.ExecutionException
import kotlinx.coroutines.*
import java.io.File

class VkDownloadService(private var context: Context) {
    private val api_url = "https://api.vk.com/method/board.getComments"
    private val token = "dfa9cba1dfa9cba1dfa9cba131dfdc346addfa9dfa9cba1bfaf71782f6116399c0ef601"
    private val version = "5.126"

    private var queue: RequestQueue = Volley.newRequestQueue(context)

    fun downloadSchedule(): List<File> {
        val links = retrieveLinks()
        val future = RequestFuture.newFuture<ByteArray>()
        val fileNames = mutableListOf<File>()

        for (link in links) {
            val request = InputStreamVolleyRequest(Request.Method.GET, link.second, future, future, null)
            queue.add(request)
            try {
                val response = future.get()
                if (response != null) {
                    val outputStream = context.openFileOutput(link.first, Context.MODE_PRIVATE)
                    outputStream.write(response)
                    outputStream.close()

                    val doc = PDFDoc()
                    val path = context.filesDir.path+"/${link.first}"
                    Convert.officeToPdf(doc, path, null)

                    val filename = link.first + ".pdf"
                    doc.save(context.getExternalFilesDir(null)?.path + "/${filename}",
                            SDFDoc.SaveMode.COMPATIBILITY, null)
                    fileNames.add(File(context.getExternalFilesDir(null), filename))
                }
            }
            catch (e: InterruptedException) {
                throw e
            } catch (e: ExecutionException) {
                throw e
            }
        }
        return fileNames
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

                if (linksarray.size == linkscount) break
            }
        } catch (e: InterruptedException) {
            throw e
        } catch (e: ExecutionException) {
            throw e
        }
        return linksarray
    }
}