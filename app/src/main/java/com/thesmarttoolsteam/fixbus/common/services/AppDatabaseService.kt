package com.thesmarttoolsteam.fixbus.common.services

import android.content.Context
import androidx.room.Room
import com.thesmarttoolsteam.fixbus.FixBusApp
import com.thesmarttoolsteam.fixbus.R
import com.thesmarttoolsteam.fixbus.common.FIXBUSDB_ASSETS_NAME
import com.thesmarttoolsteam.fixbus.common.FIXBUSDB_REF_NAME
import com.thesmarttoolsteam.fixbus.common.database.AppDatabaseRef
import com.thesmarttoolsteam.fixbus.common.database.model.ArretsTransporteurDao
import com.thesmarttoolsteam.fixbus.common.tools.getResString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.system.measureTimeMillis

class AppDatabaseService private constructor(private val applicationContext: Context) {

    private var isInitialized = false
    private lateinit var appDatabaseRef: AppDatabaseRef

    lateinit var arretsTransporteurDao: ArretsTransporteurDao
        private set

    //==============================================================================================
    companion object {

        private var instance: AppDatabaseService? = null

        //==========================================================================================
        /**
         * Création d'une nouvelle instance ou récupération de l'instance existante (surcharge du
         * constructeur)
         *
         * @return Instance de l'objet créé ou existant
         */
        //------------------------------------------------------------------------------------------
        operator fun invoke(applicationContext: Context): AppDatabaseService {
            Timber.v("In")
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        Timber.d("Création d'une nouvelle instance")
                        instance = AppDatabaseService(applicationContext)
                    }
                }
            }
            return instance!!
        }
    }

    //==============================================================================================
    /**
     * Initialisation du service de base de données
     */
    //----------------------------------------------------------------------------------------------
    fun initDatabaseService() {
        Timber.v("In")

        if (isInitialized) {
            Timber.w("Le service est déjà initialisé")
        } else {
            Timber.d("Chargement ou création de la base de référence")
            loadOrBuildDatabaseRef()
        }
    }

    //==============================================================================================
    /**
     * Construction de la base de données
     */
    //----------------------------------------------------------------------------------------------
    private fun loadOrBuildDatabaseRef() {
        Timber.v("In")

        val databaseRef = getResString(
            applicationContext,
            R.string.gradle_configDatabaseRefName
        ) ?: FIXBUSDB_REF_NAME
        Timber.d("Nom de la base à charger / créer : $databaseRef")

        if (!FixBusApp.appPreferences.isDBRefInitialized) {
            Timber.d("Création de la base de référence")
            val duration = measureTimeMillis {
                try {
                    val databaseAssets = getResString(
                        applicationContext,
                        R.string.gradle_configDatabaseAssetsName
                    ) ?: FIXBUSDB_ASSETS_NAME
                    Timber.d("Nom de la base à copier : $databaseAssets")

                    appDatabaseRef = Room
                        .databaseBuilder(
                            applicationContext,
                            AppDatabaseRef::class.java,
                            databaseRef
                        )
                        .createFromAsset(databaseAssets)
                        .fallbackToDestructiveMigration()
                        .build()
                } catch (e: Exception) {
                    Timber.e(e, "Exception : ${e.message}")
                    throw e
                }
            }

            Timber.d("Done : $duration ms")
            FixBusApp.appPreferences.isDBRefInitialized = true
        } else {
            Timber.d("Ouverture de la base de données existante")
            appDatabaseRef = Room
                .databaseBuilder(
                    applicationContext,
                    AppDatabaseRef::class.java,
                    databaseRef
                )
                .fallbackToDestructiveMigration()
                .build()
        }

        arretsTransporteurDao = appDatabaseRef.arretsTransporteurDao()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val stopCount = arretsTransporteurDao.getArretTransporteurCount()
                Timber.d("Nombre d'arrêts dans la base : $stopCount")

            } catch (e: Exception) {
                Timber.e(e, "Exception : ${e.message}")
                throw e
            }
        }
    }
}