package com.github.purofle.sandauschool.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.github.purofle.sandauschool.MyApp

actual fun createDataStore(): DataStore<Preferences> {
    val context = MyApp.getContext()
    return createDataStore(
        producePath = { context.filesDir.resolve(dataStoreFileName).absolutePath }
    )
}