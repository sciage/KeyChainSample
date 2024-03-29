package com.linktime.keychainsample.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.linktime.keychainsample.utils.Constants.DataStore.DATA
import com.linktime.keychainsample.utils.Constants.DataStore.SECURED_DATA
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DataStoreUtil constructor(
    private val dataStore: DataStore<Preferences>,
    private val security: SecurityUtil
) {
    private val securityKeyAlias = "data-store"
    private val bytesToStringSeparator = "|"

    fun getData() = dataStore.data
        .map { preferences ->
            preferences[DATA].orEmpty()
        }

    suspend fun setData(value: String) {
        dataStore.edit {
            it[DATA] = value
        }
    }

    fun getSecuredData(key: Preferences.Key<String>) = dataStore.data
        .secureMap<String> { preferences ->
            preferences[key].orEmpty()
        }

    suspend fun setSecuredData(value: String, key: Preferences.Key<String>, context: Context) {
        dataStore.secureEdit(value, context) { prefs, encryptedValue ->
            prefs[key] = encryptedValue
        }
    }

    suspend fun hasKey(key: Preferences.Key<*>) = dataStore.edit { it.contains(key) }

    suspend fun clearDataStore() {
        dataStore.edit {
            it.clear()
        }
    }

    private inline fun <reified T> Flow<Preferences>.secureMap(crossinline fetchValue: (value: Preferences) -> String): Flow<T> {
        return map {
            val decryptedValue = security.decryptData(
                securityKeyAlias,
                fetchValue(it).split(bytesToStringSeparator).map { it.toByte() }.toByteArray()
            )
            Json { encodeDefaults = true }.decodeFromString(decryptedValue)
        }
    }

    private suspend inline fun <reified T> DataStore<Preferences>.secureEdit(
        value: T,
        context: Context,
        crossinline editStore: (MutablePreferences, String) -> Unit
    ) {
        edit {
            val encryptedValue = security.encryptData(securityKeyAlias, Json.encodeToString(value), context)
            editStore.invoke(it, encryptedValue.joinToString(bytesToStringSeparator))
        }
    }
}
