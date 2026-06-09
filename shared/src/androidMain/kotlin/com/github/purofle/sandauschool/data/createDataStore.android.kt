package com.github.purofle.sandauschool.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

private lateinit var appContext: Context

fun setSharedContext(context: Context) {
    appContext = context
}

actual fun createDataStore(): DataStore<Preferences> {
    return createDataStore(
        producePath = { appContext.filesDir.resolve(dataStoreFileName).absolutePath }
    )
}