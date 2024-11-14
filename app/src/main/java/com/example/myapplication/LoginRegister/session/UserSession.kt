package com.example.myapplication.LoginRegister.session

import org.json.JSONObject

data class UserSession(val name: String, val lastName: String, val avatar: String) {
    fun toJson(): String {
        return JSONObject().apply {
            put("name", name)
            put("lastName", lastName)
            put("avatar", avatar)
        }.toString()
    }

    companion object {
        fun fromJson(json: String): UserSession {
            val jsonObject = JSONObject(json)
            return UserSession(
                name = jsonObject.getString("name"),
                lastName = jsonObject.getString("lastName"),
                avatar = jsonObject.getString("avatar")
            )
        }
    }
}
