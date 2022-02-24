package com.linktime.keychainsample.utils

import android.content.Context
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties.*
import java.math.BigInteger
import java.security.*
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SecurityUtil {
    private val provider = "AndroidKeyStore"
    private val cipher by lazy {
        Cipher.getInstance("AES/GCM/NoPadding")
    }
    private val charset by lazy {
        charset("UTF-8")

    }
    private val keyStore by lazy {
        KeyStore.getInstance(provider).apply {
            load(null)
        }
    }
    private val keyGenerator by lazy {
        KeyGenerator.getInstance(KEY_ALGORITHM_AES, provider)
    }

    fun encryptData(keyAlias: String, text: String, context: Context): ByteArray {
        cipher.init(Cipher.ENCRYPT_MODE, generateSecretKey(keyAlias, context))
        return cipher.doFinal(text.toByteArray(charset))
    }

    fun decryptData(keyAlias: String, encryptedData: ByteArray): String {
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(keyAlias), GCMParameterSpec(128, cipher.iv))
        return cipher.doFinal(encryptedData).toString(charset)
    }

    private fun generateSecretKey(keyAlias: String, context: Context): SecretKey {
        val start: Calendar = GregorianCalendar()
        val end: Calendar = GregorianCalendar()
        end.add(Calendar.YEAR, 1)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // Below Android M, use the KeyPairGeneratorSpec.Builder.
            return keyGenerator.apply {
                init(
                    KeyPairGeneratorSpec.Builder(context) // You'll use the alias later to retrieve the key.  It's a key for the key!
                        .setAlias(keyAlias) // The subject used for the self-signed certificate of the generated pair
//                        .setSubject(X500Principal("CN=$mAlias")) // The serial number used for the self-signed certificate of the
                        // generated pair.
                        .setSerialNumber(BigInteger.valueOf(1337)) // Date range of validity for the generated pair.
                        .setStartDate(start.getTime())
                        .setEndDate(end.getTime())
                        .build()
                )
            }.generateKey()


        } else {
            // On Android M or above, use the KeyGenparameterSpec.Builder and specify permitted
            // properties  and restrictions of the key.
            return keyGenerator.apply {
                init(
                    KeyGenParameterSpec
                        .Builder(keyAlias, PURPOSE_ENCRYPT or PURPOSE_DECRYPT)
                        .setBlockModes(BLOCK_MODE_GCM)
                        .setEncryptionPaddings(ENCRYPTION_PADDING_NONE)
                        .build()
                )
            }.generateKey()
        }


    }

    private fun getSecretKey(keyAlias: String) =
        (keyStore.getEntry(keyAlias, null) as KeyStore.SecretKeyEntry).secretKey
}