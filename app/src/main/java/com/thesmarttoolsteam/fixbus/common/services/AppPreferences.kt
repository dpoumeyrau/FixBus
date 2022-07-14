package com.thesmarttoolsteam.fixbus.common.services

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.thesmarttoolsteam.fixbus.R
import com.thesmarttoolsteam.fixbus.common.*
import com.thesmarttoolsteam.fixbus.common.tools.getResString
import timber.log.Timber

//==================================================================================================
/**
 * Gestion des préférences de l'application.
 *
 * Utilisation d'un fichier chiffré des préférences (nom défini dans le gradle)
 */
//==================================================================================================
object AppPreferences {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var applicationContext: Context

    //==============================================================================================
    /**
     * Indique le nombre de fois que l'application a été lancée précédemment
     */
    //----------------------------------------------------------------------------------------------
    var launchesCount: Int
        get() {
            val value = sharedPreferences.getInt(
                getResString(applicationContext, R.string.prefs_launchescount_key),
                0
            )
            Timber.i("launchesCount -> $value")
            return value
        }
        set(count) {
            Timber.i("launchesCount <- $count")
            sharedPreferences.edit().putInt(
                getResString(applicationContext, R.string.prefs_launchescount_key),
                count
            ).apply()
        }

    //==============================================================================================
    /**
     * Chemin (Firebase Store) du fichier des données IDFM
     */
    //----------------------------------------------------------------------------------------------
    var idfmStoreFilePath: String
        get() {
            val value = sharedPreferences.getString(
                getResString(applicationContext, R.string.prefs_idfm_storefilepath_key ),
                PREFS_IDFM_STORAGEPATH_DEFAULT
            ) ?: PREFS_IDFM_STORAGEPATH_DEFAULT
            Timber.i("idfmStoreFilePath -> $value")
            return value
        }
        set(filepath) {
            Timber.i("idfmStoreFilePath <- $filepath")
            sharedPreferences.edit().putString(
                getResString(applicationContext, R.string.prefs_idfm_storefilepath_key ),
                filepath
            ).apply()
        }

    //==============================================================================================
    /**
     * Chemin du dernier fichier IDFM (Firebase Store) récupéré
     */
    //----------------------------------------------------------------------------------------------
    var localStoreFilePath: String
        get() {
            val value = sharedPreferences.getString(
                getResString(applicationContext, R.string.prefs_local_storefilepath_key ),
                PREFS_IDFM_STORAGEPATH_DEFAULT
            ) ?: PREFS_IDFM_STORAGEPATH_DEFAULT
            Timber.i("localStoreFilePath -> $value")
            return value
        }
        set(filepath) {
            Timber.i("localStoreFilePath <- $filepath")
            sharedPreferences.edit().putString(
                getResString(applicationContext, R.string.prefs_local_storefilepath_key ),
                filepath
            ).apply()
        }

    //==============================================================================================
    /**
     * Dernier fichier chargé en base
     */
    //----------------------------------------------------------------------------------------------
    var dbStoreFilePath: String
        get() {
            val value = sharedPreferences.getString(
                getResString(applicationContext, R.string.prefs_db_dbfilepath_key ),
                PREFS_IDFM_STORAGEPATH_DEFAULT
            ) ?: PREFS_IDFM_STORAGEPATH_DEFAULT
            Timber.i("prefs_db_dbfilepath_key -> $value")
            return value
        }
        set(filepath) {
            Timber.i("prefs_db_dbfilepath_key <- $filepath")
            sharedPreferences.edit().putString(
                getResString(applicationContext, R.string.prefs_db_dbfilepath_key ),
                filepath
            ).apply()
        }

    //==============================================================================================
    /**
     * Matricule de l'utilisateur
     */
    //----------------------------------------------------------------------------------------------
    var userPersonalNumber: String
        get() {
            val value = sharedPreferences.getString(
                getResString(applicationContext, R.string.prefs_user_personalnumber_key),
                PREFS_USER_PERSONALNUMBER_DEFAULT
            ) ?: PREFS_USER_PERSONALNUMBER_DEFAULT
            Timber.i("userPersonalNumber -> $value")
            return value
        }
        set(value) {
            Timber.i("userPersonalNumber <- $value")
            sharedPreferences.edit().putString(
                getResString(
                    applicationContext,
                    R.string.prefs_user_personalnumber_key
                ),
                value
            ).apply()
        }
    //==============================================================================================
    /**
     * Token de Firebase Cloud Messaging
     */
    //----------------------------------------------------------------------------------------------
    var firebaseCloudMessagingServiceToken: String
        get() {
            val value = sharedPreferences.getString(
                getResString(applicationContext, R.string.prefs_firebase_cloudmessaging_token_key),
                PREFS_FIREBASE_CLOUDMESSAGING_TOKEN
            ) ?: PREFS_FIREBASE_CLOUDMESSAGING_TOKEN
            Timber.i("firebaseCloudMessagingServiceToken -> $value")
            return value
        }
        set(token) {
            Timber.i("firebaseCloudMessagingServiceToken <- $token")
            sharedPreferences.edit().putString(
                getResString(
                    applicationContext,
                    R.string.prefs_firebase_cloudmessaging_token_key
                ),
                token
            ).apply()
        }

    //==============================================================================================
    /**
     * Nom du fichier de chargement des données OpenData IDFM
     */
    //----------------------------------------------------------------------------------------------
    var openDataRemoteConfigLocation: String
        get() {
            val value = sharedPreferences.getString(
                getResString(applicationContext, R.string.prefs_opendatalocation_key),
                PREFS_OPENDATALOCATION_DEFAULT
            ) ?: PREFS_OPENDATALOCATION_DEFAULT
            Timber.i("openDataRemoteConfigLocation -> $value")
            return value
        }
        set(value) {
            Timber.i("openDataRemoteConfigLocation <- $value")
            sharedPreferences.edit().putString(
                getResString(
                    applicationContext,
                    R.string.prefs_firebase_cloudmessaging_token_key
                ),
                value
            ).apply()
        }

    //==============================================================================================
    /**
     * Vérification de l'initialisation de la base de données
     */
    //----------------------------------------------------------------------------------------------
    var isDBRefInitialized: Boolean
        get() {
            val value = sharedPreferences.getBoolean(
                getResString(applicationContext, R.string.prefs_isdbrefinitialized_key),
                APPDATABASESERVICE_DEFAULT_ISDBREFINITIALIZED
            )
            Timber.i("isDBInitialized -> $value")
            return value
        }
        set(bool) {
            Timber.i("isDBInitialized <- $bool")
            sharedPreferences.edit().putBoolean(
                getResString(
                    applicationContext,
                    R.string.prefs_isdbrefinitialized_key
                ),
                bool
            ).apply()
        }

    //==============================================================================================
    /**
     * Initialisation de la classe.
     *
     * Création ou ouverture du fichier chiffré des préférences (défini dans le gradle) et
     * initialisation des propriétés MutableLiveData
     */
    //----------------------------------------------------------------------------------------------
    fun initAppPreferences(applicationContext: Context) {
        Timber.v("In")

        this.applicationContext = applicationContext

        Timber.d("Génération de la clé de chiffrement")
        val masterKey = createMasterKey()
        val prefsFilename = getResString(AppPreferences.applicationContext, R.string.gradle_prefsFilename)

        Timber.d("Ouverture du fichier de préférences $prefsFilename")
        prefsFilename?.let {
            sharedPreferences = EncryptedSharedPreferences.create(
                applicationContext,
                it,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }

    //==============================================================================================
    /**
     * Création de la clé de chiffrement.
     */
    //----------------------------------------------------------------------------------------------
    private fun createMasterKey(): MasterKey {
        Timber.v("In")
        try {
            val keyGenSpec = KeyGenParameterSpec.Builder (
                PREFS_KEYSTORE_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT.or(KeyProperties.PURPOSE_DECRYPT)
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
            return MasterKey
                .Builder (applicationContext)
                .setKeyGenParameterSpec(keyGenSpec)
                .build()
        } catch (e: Exception) {
            Timber.e(e, "Exception détectée : ${e.message}")
            throw e
        }
    }
}