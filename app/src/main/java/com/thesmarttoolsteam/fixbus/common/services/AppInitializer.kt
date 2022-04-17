package com.thesmarttoolsteam.fixbus.common.services

import android.content.Context
import com.google.firebase.perf.metrics.AddTrace
import timber.log.Timber

//==================================================================================================
/**
 * Cette classe singleton initialise les différents services nécessaires dans l'application :
 * - Timber [Timber] : [initTimber]
 * - Préférences [AppPreferences] : [initAppPreferences]
 * - Firebase [AppFirebaseServices] : [initAppFirebaseServices]
 */
//==================================================================================================
object AppInitializer {

	private var isAppTimberInitialized: Boolean = false
	private var isAppPreferencesInitialized: Boolean = false
	private var isAppFirebaseServicesInitialized: Boolean = false
	private var isAppDatabaseServiceInitialised: Boolean = false

	private lateinit var appPreferences: AppPreferences
	private lateinit var appFirebaseServices: AppFirebaseServices
	private lateinit var appDatabase: AppDatabaseService

	//----------------------------------------------------------------------------------------------
	/**
	 * Initialisation de [Timber]
	 */
	//----------------------------------------------------------------------------------------------
	fun initTimber(applicationContext: Context) {
		if (!isAppTimberInitialized) {
			AppTimber.initAppTimber(applicationContext)
			isAppTimberInitialized = true
		} else {
			Timber.w("AppTimber déjà initialisé")
		}
	}

	//----------------------------------------------------------------------------------------------
	/**
	 * Initialisation de [AppPreferences]
	 *
	 * @param applicationContext Contexte de l'application
	 * @return Instance de [AppPreferences]
	 */
	//----------------------------------------------------------------------------------------------
	@AddTrace(name = "initAppPreferences")
	fun initAppPreferences(applicationContext: Context): AppPreferences {
		Timber.v("In")
		if (!isAppPreferencesInitialized) {
			appPreferences = AppPreferences
			appPreferences.initAppPreferences(applicationContext)
			isAppPreferencesInitialized = true
		} else {
			Timber.w("AppPreferences déjà initialisé")
		}
		Timber.d("appPreferences initialisé : $appPreferences")
		return appPreferences
	}

	//----------------------------------------------------------------------------------------------
	/**
	 * Initialisation de [AppFirebaseServices]
	 *
	 * @return Instance de [AppFirebaseServices]
	 */
	//----------------------------------------------------------------------------------------------
	@AddTrace(name = "initAppFirebaseServices")
	fun initAppFirebaseServices(applicationContext: Context): AppFirebaseServices {
		Timber.v("In")
		if (!isAppFirebaseServicesInitialized) {
			appFirebaseServices = AppFirebaseServices()
			appFirebaseServices.let {
				it.initAppFirebaseServices(applicationContext)
				isAppFirebaseServicesInitialized = true
			}
		} else {
			Timber.w("AppFirebaseServices déjà initialisé")
		}
		return appFirebaseServices
	}

	//==============================================================================================
	/**
	 * Initialisation du service de bases de données [AppDatabaseService]
	 *
	 * @param applicationContext Contexte de l'application
	 */
	//----------------------------------------------------------------------------------------------
	@AddTrace(name = "initAppDatabaseService")
	fun initAppDatabaseService(applicationContext: Context): AppDatabaseService {
		if (!isAppDatabaseServiceInitialised) {
			Timber.w("Initialisation de AppDatabaseService")
			appDatabase = AppDatabaseService(applicationContext)
			appDatabase.initDatabaseService()
			isAppDatabaseServiceInitialised = true
		} else {
			Timber.w("AppDatabaseService déjà initialisé")
		}
		return appDatabase
	}
}