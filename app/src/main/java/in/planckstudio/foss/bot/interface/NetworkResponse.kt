package `in`.planckstudio.foss.bot.`interface`

interface NetworkResponse {
    fun onFailure(message: String): String
    suspend fun onResponse(response: String, next: String = ""): String
}