package `in`.planckstudio.foss.bot.helper

import android.content.Context
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

open class NetworkHelper(open var context: Context) : NetworkResponse {

    private val networkResponse = this
    private var requestQueue = Volley.newRequestQueue(context)
    private var response = ""
    private var responseStatus = false

    fun setResponseStatus(status: Boolean) {
        this.responseStatus = status
    }

    fun getResponseStatus(): Boolean {
        return this.responseStatus
    }

    fun setResponse(res: String) {
        this.response = res
    }

    fun getResponse(): String {
        return this.response
    }

    suspend fun sendPostNetworkRequest(url: String, header: HashMap<String, String>) {
        GlobalScope.launch(Dispatchers.IO) {
            val stringRequest = object : StringRequest(
                Method.POST,
                url,
                Response.Listener<String> {
                    setResponseStatus(true)
                    setResponse(it.toString())
                    networkResponse.onCallback(it.toString())
                },
                Response.ErrorListener {
                    setResponse(it.toString())
                    setResponseStatus(false)
                    networkResponse.onCallback(it.toString())
                }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String>? {
                    return header
                }
            }

            val retryPolicy: RetryPolicy = DefaultRetryPolicy(30000, 3, 1F)
            stringRequest.setShouldCache(true)
            requestQueue.add(stringRequest).retryPolicy = retryPolicy
        }
    }

    suspend fun sendGetNetworkRequest(
        url: String,
        header: HashMap<String, String>
    ) {
        GlobalScope.async(Dispatchers.IO) {
            val stringRequest = object : StringRequest(
                Method.GET,
                url,
                Response.Listener<String> {
                    setResponseStatus(true)
                    setResponse(it.toString())
                    networkResponse.onCallback(it.toString())
                },
                Response.ErrorListener {
                    setResponse(it.toString())
                    setResponseStatus(false)
                    networkResponse.onCallback(it.toString())
                }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String>? {
                    return header
                }
            }
            val retryPolicy: RetryPolicy = DefaultRetryPolicy(60000, 3, 1F)
            stringRequest.setShouldCache(true)
            requestQueue.add(stringRequest).retryPolicy = retryPolicy
        }.await()
    }

    override fun onCallback(res: String): String {
        return res
    }
}

interface NetworkResponse {
    fun onCallback(res: String): String
}