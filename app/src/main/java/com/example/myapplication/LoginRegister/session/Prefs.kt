package com.example.myapplication.LoginRegister.session

import android.content.Context
import android.content.SharedPreferences



class Prefs(context: Context) {
    private val PREFS_FILENAME = "edu.bo.ucb.pref"
    private val KEY_USER = "user"
    private val KEY_TOKEN = "token"
    private val KEY_ENABLED_FACEID = "faceId"

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, 0)

    var user: UserSession?
        get() = prefs.getString(KEY_USER, null)?.let { UserSession.fromJson(it) }
        set(value) = prefs.edit().putString(KEY_USER, value?.toJson()).apply()

    var token: String?
        get() = prefs.getString(KEY_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_TOKEN, value).apply()

    var faceId: Boolean
        get() = prefs.getBoolean(KEY_ENABLED_FACEID, false)
        set(value) = prefs.edit().putBoolean(KEY_ENABLED_FACEID, value).apply()

    fun clearSession() {
        prefs.edit().remove(KEY_USER).remove(KEY_TOKEN).apply()
    }
}

