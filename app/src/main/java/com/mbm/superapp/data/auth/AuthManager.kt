package com.mbm.superapp.data.auth

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mbm.superapp.data.api.BackendConfig
import com.mbm.superapp.data.api.SupabaseClient
import com.mbm.superapp.data.model.Student
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private val Context.authDataStore by preferencesDataStore(name = "auth_prefs")

class AuthManager(private val context: Context) {

    private object Keys {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_ID = stringPreferencesKey("user_id")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_NAME = stringPreferencesKey("user_name")
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
    }

    val isLoggedIn: Flow<Boolean> = context.authDataStore.data.map { it[Keys.IS_LOGGED_IN] ?: false }
    val userId: Flow<String> = context.authDataStore.data.map { it[Keys.USER_ID] ?: "" }
    val userName: Flow<String> = context.authDataStore.data.map { it[Keys.USER_NAME] ?: "" }
    val userEmail: Flow<String> = context.authDataStore.data.map { it[Keys.USER_EMAIL] ?: "" }

    suspend fun signIn(email: String, password: String): Result<String> {
        if (!BackendConfig.isConfigured) {
            return Result.failure(Exception("Backend not configured. Add Supabase credentials."))
        }
        return try {
            val response = SupabaseClient.signIn(email, password)
            val json = Json.parseToJsonElement(response).jsonObject
            val token = json["access_token"]?.jsonPrimitive?.content
            val user = json["user"]?.jsonObject
            val uid = user?.get("id")?.jsonPrimitive?.content

            if (token != null && uid != null) {
                SupabaseClient.setAccessToken(token)
                context.authDataStore.edit { prefs ->
                    prefs[Keys.IS_LOGGED_IN] = true
                    prefs[Keys.USER_ID] = uid
                    prefs[Keys.USER_EMAIL] = email
                    prefs[Keys.ACCESS_TOKEN] = token
                }
                Result.success(uid)
            } else {
                val error = json["error_description"]?.jsonPrimitive?.content
                    ?: json["msg"]?.jsonPrimitive?.content
                    ?: "Sign in failed"
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, password: String, name: String): Result<String> {
        if (!BackendConfig.isConfigured) {
            return Result.failure(Exception("Backend not configured. Add Supabase credentials."))
        }
        return try {
            val response = SupabaseClient.signUp(email, password)
            val json = Json.parseToJsonElement(response).jsonObject
            val user = json["user"]?.jsonObject
            val uid = user?.get("id")?.jsonPrimitive?.content

            if (uid != null) {
                context.authDataStore.edit { prefs ->
                    prefs[Keys.USER_NAME] = name
                }
                Result.success(uid)
            } else {
                val error = json["error_description"]?.jsonPrimitive?.content
                    ?: json["msg"]?.jsonPrimitive?.content
                    ?: "Sign up failed"
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut() {
        context.authDataStore.edit { it.clear() }
        SupabaseClient.setAccessToken("")
    }

    suspend fun setUserName(name: String) {
        context.authDataStore.edit { it[Keys.USER_NAME] = name }
    }
}
