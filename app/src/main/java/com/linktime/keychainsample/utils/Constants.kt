package com.linktime.keychainsample.utils

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey

class Constants {

    object DataStore {
        val DATA = stringPreferencesKey("data")
        val SECURED_DATA: Preferences.Key<String> = stringPreferencesKey("secured_data")
    }
}