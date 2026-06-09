package com.github.purofle.sandauschool.data

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val dataStore by lazy {
    createDataStore()
}

val CAMPUSHOY_SECRET = stringPreferencesKey("campushoy_secret")
val CAMPUSHOY_SESSION_TOKEN = stringPreferencesKey("campushoy_session_token")
val CAMPUSHOY_TGC = stringPreferencesKey("campushoy_tgc")
val SCHOOL_SESSION_TOKEN = stringPreferencesKey("school_session_token")

suspend fun <T> Preferences.Key<T>.get(): T? {
    return dataStore.data.map { it[this] }.first()
}

suspend fun <T> Preferences.Key<T>.set(data: T) {
    dataStore.edit {
        it[this] = data
    }
}