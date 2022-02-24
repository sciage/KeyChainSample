package com.linktime.keychainsample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.linktime.keychainsample.utils.Constants.DataStore.SECURED_DATA
import com.linktime.keychainsample.utils.DataStoreUtil
import com.linktime.keychainsample.utils.SecurityUtil
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dataToStore = "Data"

        val secureUtil = SecurityUtil()

        val dataStore = preferencesDataStore(name = "data-store").getValue(this, String::javaClass)

        val dataStoreUtil = DataStoreUtil(dataStore, secureUtil)

        MainScope().launch {
            dataStoreUtil.setSecuredData(dataToStore, SECURED_DATA, this@MainActivity)

            val dataFromKeyStore = dataStoreUtil.getSecuredData(SECURED_DATA).first()

            Toast.makeText(this@MainActivity, dataFromKeyStore, Toast.LENGTH_LONG).show()
        }

    }
}