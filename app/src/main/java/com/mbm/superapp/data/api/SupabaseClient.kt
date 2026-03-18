package com.mbm.superapp.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * Lightweight Supabase REST client using OkHttp.
 * Call SupabaseClient.init(url, anonKey) once at app startup.
 */
object SupabaseClient {
    private var baseUrl: String = ""
    private var anonKey: String = ""
    private var accessToken: String = ""

    val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
        encodeDefaults = true
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    fun init(url: String, key: String) {
        baseUrl = url.trimEnd('/')
        anonKey = key
    }

    fun setAccessToken(token: String) {
        accessToken = token
    }

    val isConfigured get() = baseUrl.isNotEmpty() && anonKey.isNotEmpty()

    private fun buildRequest(path: String): Request.Builder {
        val authHeader = if (accessToken.isNotEmpty()) "Bearer $accessToken" else "Bearer $anonKey"
        return Request.Builder()
            .url("$baseUrl$path")
            .header("apikey", anonKey)
            .header("Authorization", authHeader)
            .header("Content-Type", "application/json")
            .header("Prefer", "return=representation")
    }

    suspend fun get(path: String): String = withContext(Dispatchers.IO) {
        val request = buildRequest(path).get().build()
        val response = client.newCall(request).execute()
        response.body?.string() ?: "[]"
    }

    suspend fun post(path: String, body: String): String = withContext(Dispatchers.IO) {
        val request = buildRequest(path)
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()
        val response = client.newCall(request).execute()
        response.body?.string() ?: "{}"
    }

    suspend fun patch(path: String, body: String): String = withContext(Dispatchers.IO) {
        val request = buildRequest(path)
            .patch(body.toRequestBody("application/json".toMediaType()))
            .build()
        val response = client.newCall(request).execute()
        response.body?.string() ?: "{}"
    }

    suspend fun delete(path: String): String = withContext(Dispatchers.IO) {
        val request = buildRequest(path).delete().build()
        val response = client.newCall(request).execute()
        response.body?.string() ?: "{}"
    }

    // Auth helpers
    suspend fun signUp(email: String, password: String): String =
        post("/auth/v1/signup", """{"email":"$email","password":"$password"}""")

    suspend fun signIn(email: String, password: String): String =
        post("/auth/v1/token?grant_type=password", """{"email":"$email","password":"$password"}""")

    // REST API (PostgREST)
    fun rest(table: String) = "/rest/v1/$table"
}
