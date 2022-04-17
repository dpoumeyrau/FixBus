package com.thesmarttoolsteam.fixbus.common.services

import android.content.Context
import androidx.room.Room
import com.thesmarttoolsteam.fixbus.FixBusApp
import com.thesmarttoolsteam.fixbus.R
import com.thesmarttoolsteam.fixbus.common.FIXBUSDB_ASSETS_NAME
import com.thesmarttoolsteam.fixbus.common.FIXBUSDB_REF_NAME
import com.thesmarttoolsteam.fixbus.common.database.AppDatabaseRef
import com.thesmarttoolsteam.fixbus.common.database.model.ArretsTransporteurDao
import com.thesmarttoolsteam.fixbus.common.getResString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
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
            if (!FixBusApp.appPreferences.isDBRefInitialized) {
                Timber.d("Création de la base de référence")
                buildDatabaseRef()
                FixBusApp.appPreferences.isDBRefInitialized = true
            } else {
                Timber.d("Base de référence déjà créée")
            }
            isInitialized = true
        }
    }

    //==============================================================================================
    /**
     * Construction de la base de données
     */
    //----------------------------------------------------------------------------------------------
    private fun buildDatabaseRef() {
        Timber.v("In")

        val duration = measureTimeMillis {
            try {
                val databaseRef = getResString(
                    applicationContext,
                    R.string.gradle_configDatabaseRefName
                ) ?: FIXBUSDB_REF_NAME
                Timber.d("Nom de la base à créer : $databaseRef")

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

    //==============================================================================================
    /**
     * Initialisation de la base de données avec les données open data
     */
    //----------------------------------------------------------------------------------------------
    private fun initDatabase() {
        Timber.v("In")


//        val bufferedReader = FixBusApp.instance.assets.open(OPENDATA_FILENAME).bufferedReader()
//        val csvParser = CSVParser(bufferedReader, CSVFormat.DEFAULT.withDelimiter(';').withFirstRecordAsHeader())
//        val nRows = csvParser.count()
//        val test = csvParser.firstOrNull()
//        val header = csvParser.headerMap
//        val z = test
//        val zz = header
//        val liste = csvParser.records
//        CoroutineScope(Dispatchers.IO).launch {
//            liste.forEachIndexed { index, csvRecord ->
//                val arret = ArretTransporteurUi(
//                    artid = csvRecord.get(0),
//                    artversion = csvRecord.get(1),
//                    artcreated = csvRecord.get(2),
//                    artchanged = csvRecord.get(3),
//                    fournisseurid = csvRecord.get(4).toInt(),
//                    fournisseurname = csvRecord.get(5),
//                    artname = csvRecord.get(6),
//                    artxepsg2154 = csvRecord.get(7).toInt(),
//                    artyepsg2154 = csvRecord.get(8).toInt(),
//                    arrid = csvRecord.get(9).toInt(),
//                    arttype = csvRecord.get(10),
//                    artprivatecode = csvRecord.get(11),
//                    artfarezone = csvRecord.get(12),
//                    artaccessibility = csvRecord.get(13),
//                    artaudiblesignals = csvRecord.get(14),
//                    artvisualsigns = csvRecord.get(15),
//                    arttown = csvRecord.get(16),
//                    artpostalregion = csvRecord.get(17)
//                )
//                val rowId = arretsTransporteurDao.insertArretTransporteur(arret)
//                Timber.d("($index) : $arret ($rowId")
//            }
//            Timber.d("**** Done")
//        }
    }
}