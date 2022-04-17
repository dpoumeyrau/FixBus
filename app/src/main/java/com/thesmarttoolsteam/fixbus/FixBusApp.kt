package com.thesmarttoolsteam.fixbus

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.thesmarttoolsteam.fixbus.common.services.AppDatabaseService
import com.thesmarttoolsteam.fixbus.common.services.AppFirebaseServices
import com.thesmarttoolsteam.fixbus.common.services.AppInitializer
import com.thesmarttoolsteam.fixbus.common.services.AppPreferences
import timber.log.Timber

//==================================================================================================
/**
 * Classe de gestion de l'application [FixBus][Application] pour gérer l'ensemble des services
 * transverses à l'application (Firebase, Préférences, ...)
 */
//--------------------------------------------------------------------------------------------------
class FixBusApp : Application() {

	private var areRequiredServicesInitialized = false
	private var areAdditionalServicesInitialized = false

	//==============================================================================================
	companion object {
		private val appInitializer by lazy { AppInitializer }

		/** Gestionnaire des services Firebase */
		lateinit var appFirebaseServices: AppFirebaseServices private set

		/** Gestionnaire des préférences */
		lateinit var appPreferences: AppPreferences private set

		/** Gestion de la base de données */
		lateinit var appDatabaseService: AppDatabaseService private set

		lateinit var instance: FixBusApp
	}

	//==============================================================================================
	override fun onCreate() {
		super.onCreate()

		appInitializer.initTimber(applicationContext)
		Timber.d("Timber initialisé")

		Timber.d("Activation des couleurs dynamiques Android")
		DynamicColors.applyToActivitiesIfAvailable(this)
		instance = this
	}

	//==============================================================================================
	/**
	 * Initialisation des services obligatoires pour l'exécution de l'application. En fin de
	 * traitement, la méthode callback [endOfJobCallback] est appelée.
	 */
	//----------------------------------------------------------------------------------------------
	fun initRequiredServices(endOfJobCallback: ()->Unit) {
		Timber.v("In")
		if (!areRequiredServicesInitialized) {
			synchronized(this) {
				if (!areRequiredServicesInitialized) {

					areRequiredServicesInitialized = true

					Timber.d("Initialisation des préférences")
					appPreferences = appInitializer.initAppPreferences(applicationContext)

					Timber.d("Initialisation des services Firebase")
					appFirebaseServices = appInitializer.initAppFirebaseServices(applicationContext)

					Timber.d("Initialisation de la base de données")
					appDatabaseService = appInitializer.initAppDatabaseService(applicationContext)

					Timber.d("Notification de fin des traitements obligatoires")
					endOfJobCallback()
				} else {
					Timber.d("Initialisation des services obligatoires déja déclenchée")
				}
			}
		} else {
			Timber.d("Initialisation des services obligatoires déja déclenchée")
		}
	}

	//==============================================================================================
	/**
	 * Initialisation des services additionnels (services dont il n'est pas obligatoire d'attendre
	 * la fin de l'initialisation pour lancer l'application)
	 */
	//----------------------------------------------------------------------------------------------
	fun initAdditionalServices() {
		Timber.v("In")

		if (!areAdditionalServicesInitialized) {
			synchronized(this) {
				if (!areAdditionalServicesInitialized) {
					areAdditionalServicesInitialized = true
					Timber.d("Pas de services additionnels à activer")
				}
			}
		}
	}
}