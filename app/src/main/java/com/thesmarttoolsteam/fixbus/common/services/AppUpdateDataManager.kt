package com.thesmarttoolsteam.fixbus.common.services

import android.content.Context
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.thesmarttoolsteam.fixbus.FixBusApp
import com.thesmarttoolsteam.fixbus.R
import com.thesmarttoolsteam.fixbus.common.tools.getResString
import com.thesmarttoolsteam.fixbus.scan.tools.ArretsTransporteurRepositoryRoomImpl
import timber.log.Timber

class AppUpdateDataManager {

	private val arretsTransporteurRepository by lazy {
		ArretsTransporteurRepositoryRoomImpl(FixBusApp.appDatabaseService.arretsTransporteurDao)
	}

	//==============================================================================================
	/**
	 * Vérifier si le dernier fichier téléchargé a bien été intégré dans la base de données
	 * (par exemple, fichier téléchargé mais mise à jour de la base interrompue
	 */
	//----------------------------------------------------------------------------------------------
	private fun isPreviousDBUpdatedFinished(): Boolean {

		Timber.v("In")
		val currentUploadedDataFile = AppPreferences.idfmStoreFilePath
		val currentDBLoadedDatatFile = AppPreferences.dbStoreFilePath

		return (currentUploadedDataFile == currentDBLoadedDatatFile)
	}

	//==============================================================================================
	/**
	 * Vérifie s'il existe un nouveau fichier de données à télécharger
	 */
	//----------------------------------------------------------------------------------------------
	private fun isUpdateAvailable(context: Context): Boolean {

		Timber.v("In")
		val lastIDFMFile = getLastIDFMDataFilename(context) {
			return@getLastIDFMDataFilename
		}
		return true
	}

	//==========================================================================================
	/**
	 * Récupérer le nom du dernier fichier de données Open Data IDFM
	 * Le chemin est stocké dans Firebase RealDatabase, sous la clé idfmStoragePath
	 * Le chemin (valeur) est sous la forme : <directory>/<filename>
	 * Ex. : idfm/20220618_arrets-transporteur.csv
	 */
	//------------------------------------------------------------------------------------------
	fun getLastIDFMDataFilename(context: Context, callback: (String?) -> Unit) {
		Timber.v("In")

		val realtimeDatabase = FixBusApp.appFirebaseServices.getFirebaseRealtimeDatabase()
		val reference = realtimeDatabase?.reference

		reference
			?.child(
				getResString(
					context,
					R.string.gradle_configFirebaseSettingsChildName
				) ?: "gradle_configFirebaseSettingsChildName"
			)
			?.child(
				getResString(
					context,
					R.string.gradle_configFirebaseRealtimeBranch
				) ?: "gradle_configFirebaseRealtimeBranch"
			)
			?.child(
				getResString(
					context,
					R.string.gradle_configFirebaseRealtimeIDFMStoragePath
				) ?: "gradle_configFirebaseRealtimeIDFMStoragePath"
			)
			?.addValueEventListener(object: ValueEventListener {

				override fun onDataChange(snapshot: DataSnapshot) {
					Timber.d("Filepath du fichier IDFM : ${snapshot.value as? String}")
					callback((snapshot.value as? String)?.trim())
				}

				override fun onCancelled(error: DatabaseError) {
					Timber.w("Exception : ${error.message}")
				}
			})
	}
}