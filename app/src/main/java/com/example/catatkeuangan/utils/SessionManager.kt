package com.example.catatkeuangan.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.catatkeuangan.models.User

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME  = "CatatKeuanganSession"
        private const val KEY_LOGIN  = "isLoggedIn"
        private const val KEY_ID     = "userId"
        private const val KEY_NAMA   = "userName"
        private const val KEY_EMAIL  = "userEmail"
        private const val KEY_TOKEN  = "token"
    }

    fun saveUser(user: User) {
        prefs.edit().apply {
            putBoolean(KEY_LOGIN, true)
            putInt(KEY_ID, user.id)
            putString(KEY_NAMA, user.nama)
            putString(KEY_EMAIL, user.email)
            putString(KEY_TOKEN, user.token)
            apply()
        }
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_LOGIN, false)
    fun getUserId(): Int      = prefs.getInt(KEY_ID, 0)
    fun getUserName(): String = prefs.getString(KEY_NAMA, "") ?: ""
    fun getUserEmail(): String = prefs.getString(KEY_EMAIL, "") ?: ""
    fun getToken(): String    = prefs.getString(KEY_TOKEN, "") ?: ""

    fun logout() = prefs.edit().clear().apply()
}
