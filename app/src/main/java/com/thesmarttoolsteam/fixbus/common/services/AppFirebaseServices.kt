package com.thesmarttoolsteam.fixbus.common.services

import android.content.Context
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.thesmarttoolsteam.fixbus.FixBusApp
import com.thesmarttoolsteam.fixbus.R
import com.thesmarttoolsteam.fixbus.common.*
import com.thesmarttoolsteam.fixbus.common.tools.areGooglePlayServicesEnabled
import com.thesmarttoolsteam.fixbus.common.tools.getResBoolean
import com.thesmarttoolsteam.fixbus.common.tools.getResInteger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

//==================================================================================================
/**
 * Gestion des services Firebase
 */
//==================================================================================================
class AppFirebaseServices private constructor() {

	// Nécessité d'activer ou non le service AppCheck
	private var isFirebaseAppCheckServiceEnabled: Boolean
		= APPFIREBASESERVICES_DEFAULT_IS_FIREBASE_APPCHECK_SERVICE_ENABLED
	private var isFirebaseAppCheckAllowedDebugVersion: Boolean
		= APPFIREBASESERVICES_DEFAULT_IS_FIREBASE_APPCHECK_ALLOWEDDEBUGVERSION

	// Nécessité d'activer ou non le service RemoteConfig
	var isFirebaseRemoteConfigServiceEnabled: Boolean
		= APPFIREBASESERVICES_DEFAULT_IS_FIREBASE_REMOTECONFIG_SERVICE_ENABLED
		private set

	// Nécessité d'activer ou non le service Crashlytics
	var isFirebaseCrashlyticsServiceEnabled: Boolean
		= APPFIREBASESERVICES_DEFAULT_IS_FIREBASE_CRASHLYTICS_SERVICE_ENABLED
		private set

	// Nécessité d'activer ou non le service Analytics
	var isFirebaseAnalyticsServiceEnabled: Boolean
		= APPFIREBASESERVICES_DEFAULT_IS_FIREBASE_ANALYTICS_SERVICE_ENABLED
		private set

	// Nécessité d'activer ou non le service Firestore
	private var isFirebaseFirestoreServiceEnabled: Boolean
		= APPFIREBASESERVICES_DEFAULT_IS_FIREBASE_FIRESTORE_SERVICE_ENABLED

	// Nécessité d'activer ou non le service Anonymous Authentification
	private var isFirebaseAnonymousAuthServiceEnabled: Boolean
		= APPFIREBASESERVICES_DEFAULT_IS_FIREBASE_ANONYMOUSAUTH_SERVICE_ENABLED

	// Nécessité d'activer ou non le service CloudMessaging
	private var isFirebaseCloudMessagingServiceEnabled: Boolean
		= APPFIREBASESERVICES_DEFAULT_IS_FIREBASE_CLOUDMESSAGING_SERVICE_ENABLED

	// Nécessité d'activer ou non le service Performance
	private var isFirebasePerformanceServiceEnabled: Boolean
		= APPFIREBASESERVICES_DEFAULT_IS_FIREBASE_PERFORMANCE_SERVICE_ENABLED

	// Nécessité d'actier ou non le service Realtime Database
	private var isFirebaseRealtimeDatabaseServiceEnabled: Boolean
		= APPFIREBASESERVICES_DEFAULT_IS_FIREBASE_REALTIMEDATABASE_SERVICE_ENABLED

	// Nécessité d'actier ou non le service Storage
	private var isFirebaseStorageServiceEnabled: Boolean
			= APPFIREBASESERVICES_DEFAULT_IS_FIREBASE_STORAGE_SERVICE_ENABLED


	// Instance des services
	private var appFirebaseAppCheckService: AppFirebaseAppCheckService? = null
	private var appFirebaseCrashlyticsService: AppFirebaseCrashlyticsService? = null
	private var appFirebaseRemoteConfigService: AppFirebaseRemoteConfigService? = null
	private var appFirebaseAnalyticsService: AppFirebaseAnalyticsService? = null
	private var appFirebaseFirestoreService: AppFirebaseFirestoreService? = null
	private var appFirebaseAnonymousAuthService: AppFirebaseAnonymousAuthService? = null
	private var appFirebaseCloudMessagingService: AppFirebaseCloudMessagingService? = null
	private var appFirebasePerformanceService: AppFirebasePerformanceService? = null
	private var appFirebaseRealtimeDatabaseService: AppFirebaseRealtimeDatabaseService? = null
	private var appFirebaseStorageService: AppFirebaseStorageService? = null

	companion object {

		private var instance: AppFirebaseServices? = null

		operator fun invoke(): AppFirebaseServices {
			Timber.v("In")
			if (instance == null) {
				synchronized(this) {
					if (instance == null) {
						Timber.d("Création d'une nouvelle instance")
						instance = AppFirebaseServices()
					}
				}
			}
			return instance!!
		}
	}

	//----------------------------------------------------------------------------------------------
	/**
	 * Initialisation des services Firebase
	 */
	//----------------------------------------------------------------------------------------------
	fun initAppFirebaseServices(applicationContext: Context) {
		Timber.v("In")

		isFirebaseAppCheckServiceEnabled = getResBoolean(
			applicationContext,
			R.bool.gradle_configFirebaseAppCheckEnabled
		) ?: APPFIREBASESERVICES_DEFAULT_IS_FIREBASE_APPCHECK_SERVICE_ENABLED
		isFirebaseAppCheckAllowedDebugVersion = getResBoolean(
			applicationContext,
			R.bool.gradle_configFirebaseAppCheckAllowedDebugVersion
		) ?: APPFIREBASESERVICES_DEFAULT_IS_FIREBASE_APPCHECK_ALLOWEDDEBUGVERSION
		isFirebaseRemoteConfigServiceEnabled = getResBoolean(
			applicationContext,
			R.bool.gradle_configFirebaseRemoteConfigEnabled
		) ?: APPFIREBASESERVICES_DEFAULT_IS_FIREBASE_REMOTECONFIG_SERVICE_ENABLED
		isFirebaseCrashlyticsServiceEnabled = getResBoolean(
			applicationContext,
			R.bool.gradle_configFirebaseCrashlyticsEnabled
		) ?: APPFIREBASESERVICES_DEFAULT_IS_FIREBASE_CRASHLYTICS_SERVICE_ENABLED
		isFirebaseAnalyticsServiceEnabled = getResBoolean(
			applicationContext,
			R.bool.gradle_configFirebaseAnalyticsEnabled
		) ?: APPFIREBASESERVICES_DEFAULT_IS_FIREBASE_ANALYTICS_SERVICE_ENABLED
		isFirebaseFirestoreServiceEnabled = getResBoolean(
			applicationContext,
			R.bool.gradle_configFirebaseFirestoreEnabled
		) ?: APPFIREBASESERVICES_DEFAULT_IS_FIREBASE_FIRESTORE_SERVICE_ENABLED
		isFirebaseAnonymousAuthServiceEnabled = getResBoolean(
			applicationContext,
			R.bool.gradle_configFirebaseAnonymousAuthEnabled
		) ?: APPFIREBASESERVICES_DEFAULT_IS_FIREBASE_ANONYMOUSAUTH_SERVICE_ENABLED
		isFirebaseCloudMessagingServiceEnabled = getResBoolean(
			applicationContext,
			R.bool.gradle_configFirebaseCloudMessagingEnabled
		) ?: APPFIREBASESERVICES_DEFAULT_IS_FIREBASE_CLOUDMESSAGING_SERVICE_ENABLED
		isFirebasePerformanceServiceEnabled = getResBoolean(
			applicationContext,
			R.bool.gradle_configFirebasePerformanceEnabled
		) ?: APPFIREBASESERVICES_DEFAULT_IS_FIREBASE_PERFORMANCE_SERVICE_ENABLED
		isFirebaseRealtimeDatabaseServiceEnabled = getResBoolean(
			applicationContext,
			R.bool.gradle_configFirebaseRealtimeDatabaseEnabled
		) ?: APPFIREBASESERVICES_DEFAULT_IS_FIREBASE_REALTIMEDATABASE_SERVICE_ENABLED
		isFirebaseStorageServiceEnabled = getResBoolean(
			applicationContext,
			R.bool.gradle_configFirebaseRealtimeDatabaseEnabled
		) ?: APPFIREBASESERVICES_DEFAULT_IS_FIREBASE_STORAGE_SERVICE_ENABLED

		// Initialisation des services activés
		FirebaseApp.initializeApp(applicationContext)

		appFirebasePerformanceService = AppFirebasePerformanceService()
		Timber.d("Firebase Performance à activer (isFirebasePerformanceServiceEnabled " +
				"= $isFirebasePerformanceServiceEnabled")
		appFirebasePerformanceService?.enableFirebasePerformanceService(
			isFirebasePerformanceServiceEnabled
		)

		if (isFirebaseAppCheckServiceEnabled) {
			Timber.d("AppCheck à activer (isFirebaseRemoteConfigServiceEnabled = true)")
			appFirebaseAppCheckService = AppFirebaseAppCheckService()
		} else {
			Timber.d("AppCheck désactivé (isFirebaseRemoteConfigServiceEnabled = false)")
		}

		CoroutineScope(Dispatchers.Default).launch {
			appFirebaseCrashlyticsService = AppFirebaseCrashlyticsService()
		}

		CoroutineScope(Dispatchers.Default).launch {
			if (isFirebaseAnonymousAuthServiceEnabled) {
				Timber.d("Anonymous Auth à activer (gradle_configFirebaseAnonymousAuthEnabled = true)")
				appFirebaseAnonymousAuthService = AppFirebaseAnonymousAuthService()
			} else {
				Timber.d("Anonymous Auth désactivé (gradle_configFirebaseAnonymousAuthEnabled = false)")
			}
		}

		CoroutineScope(Dispatchers.Default).launch {
			if (isFirebaseAnalyticsServiceEnabled) {
				Timber.d("Analytics à activer (gradle_configFirebaseAnalyticsEnabled = true)")
				appFirebaseAnalyticsService = AppFirebaseAnalyticsService()
			} else {
				Timber.d("Analytics désactivé (gradle_configFirebaseAnalyticsEnabled = false)")
			}
		}

		CoroutineScope(Dispatchers.Default).launch {
			if (isFirebaseRemoteConfigServiceEnabled) {
				Timber.d("RemoteConfig à activer (gradle_configFirebaseRemoteConfigEnabled = true)")
				appFirebaseRemoteConfigService = AppFirebaseRemoteConfigService(applicationContext)
				appFirebaseRemoteConfigService?.loadRemoteConfigs()
			} else {
				Timber.d("RemoteConfig désactivé (gradle_configFirebaseRemoteConfigEnabled = false)")
			}
		}

		CoroutineScope(Dispatchers.Default).launch {
			if (isFirebaseFirestoreServiceEnabled) {
				Timber.d("Firestore à activer (gradle_configFirebaseFirestoreEnabled = true)")
				appFirebaseFirestoreService = AppFirebaseFirestoreService()
			} else {
				Timber.d("Firestore désactivé (gradle_configFirebaseFirestoreEnabled = false)")
			}
		}

		CoroutineScope(Dispatchers.Default).launch {
			if (isFirebaseRealtimeDatabaseServiceEnabled) {
				Timber.d("RealtimeDatabase à activer " +
						"(gradle_configFirebaseRealtimeDatabaseEnabled = true)")
				appFirebaseRealtimeDatabaseService = AppFirebaseRealtimeDatabaseService()
			} else {
				Timber.d("Firestore désactivé (gradle_configFirebaseFirestoreEnabled = false)")
			}
		}

		CoroutineScope(Dispatchers.Default).launch {
			if (isFirebaseStorageServiceEnabled) {
				Timber.d("Storage à activer " +
						"(gradle_configFirebaseStorageServiceEnabled = true)")
				appFirebaseStorageService = AppFirebaseStorageService()
			} else {
				Timber.d("Firestore désactivé (gradle_configFirebaseFirestoreEnabled = false)")
			}
		}

		CoroutineScope(Dispatchers.Default).launch {
			if (!areGooglePlayServicesEnabled(applicationContext)) {
				Timber.w("Google Play Services non disponibles")
			} else {
				if (isFirebaseCloudMessagingServiceEnabled) {
					Timber.d("Cloud Messaging à activer (isFirebaseCloudMessagingServiceEnabled = true)")
					appFirebaseCloudMessagingService = AppFirebaseCloudMessagingService()
					appFirebaseCloudMessagingService?.getToken {
						Timber.d("Token : $it")
					}
				} else {
					Timber.d("Cloud Messaging désactivé (isFirebaseCloudMessagingServiceEnabled = false)")
				}
			}
		}
	}

	//----------------------------------------------------------------------------------------------
	/**
	 * Simplification des appels à FirebaseAnalytics : SCREEN_VIEW
	 *
	 * @param screen_name argument SCREEN_NAME de Firebase Analytics
	 * @param screen_class argument SCREEN_CLASS de Firebase Analytics
	 */
	//----------------------------------------------------------------------------------------------
	fun trackScreenView (callingMethod: String,
	                     screen_name: String? = "?",
	                     screen_class: String? = screen_name) {
		Timber.v("In")
		if (appFirebaseAnalyticsService != null) {
			appFirebaseAnalyticsService?.trackScreenView(callingMethod, screen_name, screen_class)
		} else {
			Timber.w("Le service n'a pas été initialisé")
		}
	}

	//----------------------------------------------------------------------------------------------
	/**
	 * Simplification des appels à FirebaseAnalytics : SELECT_CONTENT
	 *
	 * @param callingMethod pour log Timber
	 * @param contentType argument CONTENT_TYPE de Firebase Analytics
	 * @param contentId argument ITEM_ID de Firebase Analytics
	 */
	//----------------------------------------------------------------------------------------------
	@Suppress("unused")
	fun trackSelectContent (callingMethod: String, contentType: String, contentId: String) {
		Timber.v("In")
		if (appFirebaseAnalyticsService != null) {
			appFirebaseAnalyticsService?.trackSelectContent(callingMethod, contentType, contentId)
		} else {
			Timber.w("Le service n'a pas été initialisé")
		}
	}

	//----------------------------------------------------------------------------------------------
	/**
	 * Simplification des appels à Firebase Performance startTrace
	 *
	 * @param traceName Libellé de la trace
	 * @return Trace (pour stopTrace)
	 */
	//----------------------------------------------------------------------------------------------
	@Suppress("unused")
	fun startPerformanceTrace(traceName: String): Trace? {
		Timber.v("In")
		return if (appFirebasePerformanceService == null) {
			Timber.w("Firebase Performance non initialisé")
			null
		} else
			appFirebasePerformanceService?.startPerformanceTrace(traceName)
	}

	//----------------------------------------------------------------------------------------------
	/**
	 * Simplification des appels à Firebase Performance stopTrace
	 *
	 * @param trace Trace à arrêter
	 */
	//----------------------------------------------------------------------------------------------
	@Suppress("unused")
	fun stopPerformanceTrace(trace: Trace) {
		Timber.v("In")
		if (appFirebasePerformanceService == null) {
			Timber.w("Firebase Performance non initialisé")
		} else
			appFirebasePerformanceService?.stopPerformanceTrace(trace)
	}


	//==============================================================================================
	/**
	 * Récupération de l'instance de Firebase Realtime Database
	 */
	//----------------------------------------------------------------------------------------------
	fun getFirebaseRealtimeDatabase(): FirebaseDatabase? {
		Timber.v("In")

		return appFirebaseRealtimeDatabaseService?.firebaseRealtimeDatabase
	}

	//==============================================================================================
	/**
	 * Gestion du service Google Firebase Analytics
	 */
	//==============================================================================================
	private inner class AppFirebaseAnalyticsService {

		private var firebaseAnalytics: FirebaseAnalytics? = null

		init {
			Timber.v("In")
			Timber.d("Activation d'Analytics : $isFirebaseAnalyticsServiceEnabled")
			firebaseAnalytics = Firebase.analytics
		}

		//------------------------------------------------------------------------------------------
		/**
		 * Simplification des appels à FirebaseAnalytics : SCREEN_VIEW
		 *
		 * @param callingMethod pour log Timber
		 * @param screen_name argument SCREEN_NAME de Firebase Analytics
		 * @param screen_class argument SCREEN_CLASS de Firebase Analytics
		 */
		//------------------------------------------------------------------------------------------
		fun trackScreenView (callingMethod: String,
		                     screen_name: String? = "?",
		                     screen_class: String? = screen_name) {

			Timber.v("In")
			if (isFirebaseAnalyticsServiceEnabled) {
				Timber.d(
					"Appel à Firebase Analytics " +
							"pour $callingMethod ($screen_name/$screen_class)"
				)
				try {
					firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
						param(
							FirebaseAnalytics.Param.SCREEN_NAME,
							screen_name ?: "?"
						)
						param(
							FirebaseAnalytics.Param.SCREEN_CLASS,
							screen_name ?: "?"
						)
					}
				} catch (e: Exception) {
					Timber.e(e, "Exception ${e.message}")
				}
			}
		}

		//------------------------------------------------------------------------------------------
		/**
		 * Simplification des appels à FirebaseAnalytics : SELECT_CONTENT
		 *
		 * @param callingMethod pour log Timber
		 * @param contentType argument CONTENT_TYPE de Firebase Analytics
		 * @param contentId argument ITEM_ID de Firebase Analytics
		 */
		//------------------------------------------------------------------------------------------
		fun trackSelectContent (callingMethod: String, contentType: String, contentId: String) {

			Timber.v("In")
			if (isFirebaseAnalyticsServiceEnabled) {
				Timber.d("Appel à Firebase Analytics pour $callingMethod")
				try {
					firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT) {
						param(
							FirebaseAnalytics.Param.CONTENT_TYPE,
							contentType
						)
						param(
							FirebaseAnalytics.Param.ITEM_ID,
							contentId
						)
					}
				} catch (e: Exception) {
					Timber.e(e, "Exception ${e.message}")
				}
			}
		}
	}

	//==============================================================================================
	/**
	 * Gestion du service Google Firebase Crashlytics
	 */
	//==============================================================================================
	private inner class AppFirebaseCrashlyticsService {

		init {
			Timber.v("In")
			Timber.d("Activation de Crashlytics : $isFirebaseCrashlyticsServiceEnabled")
			try {
				FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(
					isFirebaseCrashlyticsServiceEnabled
				)
			} catch (e: Exception) {
				Timber.e(e)
			}
		}
	}

	//==============================================================================================
	/**
	 * Gestion du service Firebase Remote Config
	 */
	//==============================================================================================
	private inner class AppFirebaseRemoteConfigService(private val applicationContext: Context) {
		private lateinit var remoteConfig: FirebaseRemoteConfig

		//==========================================================================================
		init {
			Timber.v("In")
			if (isFirebaseRemoteConfigServiceEnabled) {
				Timber.d("Activation de Remote Config")
				remoteConfig = Firebase.remoteConfig
				val configSettings = remoteConfigSettings {
					minimumFetchIntervalInSeconds =
						getResInteger(
							applicationContext,
							R.integer.gradle_configFirebaseRemoteConfigMinimumFetchIntervalInSeconds
						)?.toLong() ?: APPFIREBASESERVICES_REMOTECONFIG_DEFAULT_MINIMUM_FETCH_INTERVAL
				}
				remoteConfig.setDefaultsAsync(R.xml.firebase_remoteconfig_defaults)
				remoteConfig.setConfigSettingsAsync(configSettings)
			} else {
				Timber.w("Demande d'activation de Remote Config alors que le service est désactivé")
			}
		}

		//------------------------------------------------------------------------------------------
		/**
		 * Chargement initial des données de Firebase Remote Config
		 */
		//------------------------------------------------------------------------------------------
		fun loadRemoteConfigs() {
			Timber.v("In")
			if (isFirebaseRemoteConfigServiceEnabled) {
				remoteConfig.fetchAndActivate()
					.addOnCompleteListener { task ->
						if (task.isSuccessful) {
							Timber.d("Réponse Firebase RemoteConfig reçue")
						} else {
							Timber.e(
								task.exception, "Problème de chargement : " +
										"${task.exception?.message}"
							)
						}
					}
					.addOnFailureListener {
						Timber.e(it, "Problème de chargement : ${it.message}")
					}

			} else {
				Timber.d("RemoteConfig désactivé (gradle_configFirebaseRemoteConfigEnabled = false)")
			}
		}
	}

	//==============================================================================================
	/**
	 * Gestion du service Google Firebase Firestore
	 */
	//==============================================================================================
	inner class AppFirebaseFirestoreService {

		val firebaseFirestoreDB: FirebaseFirestore

		init {
			Timber.v("In")
			firebaseFirestoreDB = Firebase.firestore
		}
	}

	//==============================================================================================
	/**
	 * Gestion du service Google Firebase Anonymous Authentication
	 */
	//==============================================================================================
	inner class AppFirebaseAnonymousAuthService {

		private val firebaseAnonymousAuth: FirebaseAuth = Firebase.auth
		private var currentUser: FirebaseUser? = null

		init {
			Timber.v("In")

			currentUser = firebaseAnonymousAuth.currentUser
			if (currentUser == null) {
				firebaseAnonymousAuth.signInAnonymously()
					.addOnCompleteListener { task ->
						if (task.isSuccessful) {
							// Connexion OK
							Timber.d("Connexion anonyme OK")
							currentUser = firebaseAnonymousAuth.currentUser
						} else {
							// Connexion impossible
							Timber.e(task.exception,"Connexion anonyme KO :" +
									" ${task.exception?.message}")
						}
					}
			} else {
				Timber.d("Client déjà connecté")
			}
		}
	}

	//==============================================================================================
	/**
	 * Gestion du service Google Firebase AppCheck
	 */
	//==============================================================================================
	inner class AppFirebaseAppCheckService {

		private val firebaseAppCheck = FirebaseAppCheck.getInstance()

		init {
			Timber.v("In")

			if (isFirebaseAppCheckAllowedDebugVersion) {
				Timber.d("Activation de AppCheck en mode debug")
				firebaseAppCheck.installAppCheckProviderFactory(
					DebugAppCheckProviderFactory.getInstance()
				)
			} else {
				Timber.d("Activation de AppCheck en mode debug")
				firebaseAppCheck.installAppCheckProviderFactory(
					SafetyNetAppCheckProviderFactory.getInstance()
				)
			}
		}
	}

	//==============================================================================================
	/**
	 * Gestion du service Google Firebase Cloud Messaging
	 */
	//==============================================================================================
	class AppFirebaseCloudMessagingService  {

		//------------------------------------------------------------------------------------------
		/**
		 * Récupération du token du device
		 *
		 * @param callback Méthode callback à appeler en fun de récupération
		 */
		//------------------------------------------------------------------------------------------
		fun getToken(callback: (token: String)->Unit) {
			Timber.v("In")

			FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
				if (!task.isSuccessful) {
					Timber.w(task.exception,"Impossible de récupérer le token Firebase " +
							"Cloud Messaging")
					return@OnCompleteListener
				}

				val token = task.result

				Timber.d("Token Firebase Cloud Messaging récupéré : $token")
				FixBusApp.appPreferences.firebaseCloudMessagingServiceToken = token
				callback(token)
			})
		}
	}

	//==============================================================================================
	/**
	 * Gestion du service Google Firebase Performance
	 */
	//==============================================================================================
	class AppFirebasePerformanceService {

		private var isFirebasePerformanceServiceActive: Boolean = true

		//------------------------------------------------------------------------------------------
		/**
		 * Activation / Désactivation du service Firebase Performance
		 *
		 * @param enabled Activer ?
		 */
		//------------------------------------------------------------------------------------------
		fun enableFirebasePerformanceService(enabled: Boolean) {
			Timber.v("In")
			Timber.d("Activation de Firebase Performance Service : $enabled")
			FirebasePerformance.getInstance().isPerformanceCollectionEnabled = enabled
			isFirebasePerformanceServiceActive = enabled
		}

		//------------------------------------------------------------------------------------------
		/**
		 * Démarrage d'une trace de performance
		 *
		 * @param traceName Identifiant (titre) de la trace
		 * @return Trace (pour paramètre de stopPerformanceTrace())
		 */
		//------------------------------------------------------------------------------------------
		fun startPerformanceTrace(traceName: String): Trace? {
			Timber.v("In")

			return if (isFirebasePerformanceServiceActive) {
				val firebasePerformanceTrace = FirebasePerformance.getInstance().newTrace(traceName)
				firebasePerformanceTrace.start()
				firebasePerformanceTrace
			} else {
				Timber.w("Service non activé")
				null
			}
		}

		//------------------------------------------------------------------------------------------
		/**
		 * Fin de l'analyse de performance
		 *
		 * @param trace Trace activée
		 */
		//------------------------------------------------------------------------------------------
		fun stopPerformanceTrace(trace: Trace) {
			Timber.v("In")

			trace.stop()
		}
	}

	//==============================================================================================
	/**
	 * Gestion du service Firebase Realtime Database
	 */
	//==============================================================================================
	class AppFirebaseRealtimeDatabaseService {

		val firebaseRealtimeDatabase: FirebaseDatabase

		init {
			Timber.v("In")
			firebaseRealtimeDatabase = FirebaseDatabase.getInstance()
			Timber.d("Activation de la persistence")
			firebaseRealtimeDatabase.setPersistenceEnabled(true)
		}
	}

	//==============================================================================================
	/**
	 * Gestion du service Firebase Realtime Database
	 */
	//==============================================================================================
	inner class AppFirebaseStorageService {

		val firebaseStorage: FirebaseStorage

		init {
			Timber.v("In")
			firebaseStorage = Firebase.storage
		}
	}
}