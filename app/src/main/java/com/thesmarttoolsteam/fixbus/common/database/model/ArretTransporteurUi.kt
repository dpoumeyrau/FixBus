package com.thesmarttoolsteam.fixbus.common.database.model

import android.content.Context
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.thesmarttoolsteam.fixbus.FixBusApp
import com.thesmarttoolsteam.fixbus.R
import com.thesmarttoolsteam.fixbus.common.services.AppPreferences
import com.thesmarttoolsteam.fixbus.common.tools.getResString
import com.thesmarttoolsteam.fixbus.scan.tools.ArretsTransporteurRepositoryRoomImpl
import com.thesmarttoolsteam.fixbus.scan.tools.Lambert93ToWGS84Converter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

@Entity(tableName = "arrets-transporteur", indices = [Index(value = ["ArTPrivateCode"])])
data class ArretTransporteurUi (
	@PrimaryKey val ArTId: Int?,
	val ArTVersion: String?,
	val ArTCreated: String?,
	val ArTChanged: String?,
	val FournisseurId: Int?,
	val FournisseurName: String?,
	val ArTName: String?,
	val ArTXEpsg2154: Int?,
	val ArTYEpsg2154: Int?,
	val ArRId: Int?,
	val ArTType: String?,
	val ArTPrivateCode: String?,
	val ArTFareZone: String?,
	val ArTAccessibility: String?,
	val ArTAudibleSignals: String?,
	val ArTVisualSigns: String?,
	val ArTTown: String?,
	val ArTPostalRegion: Int?,
) {
	//==============================================================================================
	/**
	 * Conversion des coordonnées Lambert3 en coordonnées standard GPS
	 */
	//----------------------------------------------------------------------------------------------
	fun getLatLng(): LatLng? {
		Timber.v("In")

		return Lambert93ToWGS84Converter.toLatLng(
			ArTXEpsg2154?.toDouble(),
			ArTYEpsg2154?.toDouble()
		)
	}

	//==============================================================================================
	companion object {

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
		//==========================================================================================
		/**
		 * Chargement du fichier de données IDFM (dans Firebase Cloud Storage)
		 */
		//------------------------------------------------------------------------------------------
		fun loadIDFMDataFile(idfmStoragePath: String) {
			Timber.d("In")

			val storageDB = FixBusApp.appFirebaseServices.AppFirebaseStorageService().firebaseStorage
			val reference = storageDB.reference.child(idfmStoragePath)

			val tmpFile = File.createTempFile("idfm-", ".csv")
			Timber.d("Création du fichier temporaire : ${tmpFile.absolutePath}")

			reference.getFile(tmpFile)
				.addOnSuccessListener {
					Timber.d("Fichier Chargé")
					loadIDFMDataIntoDB(idfmStoragePath, tmpFile)
				}
				.addOnFailureListener { exception ->
					Timber.e(exception, "Exception : ${exception.message}")
				}
		}

		//==========================================================================================
		/**
		 * Chargement des données dans la base de données
		 * La base n'est pas réinitialisée ; les données sont écrasées
		 * Les données qui ne sont plus d'actualités restent dans la base
		 */
		//------------------------------------------------------------------------------------------
		private fun loadIDFMDataIntoDB(idfmStoragePath: String, tmpFile: File) {
			Timber.v("In")

			val arretsTransporteurRepository = ArretsTransporteurRepositoryRoomImpl(
				FixBusApp.appDatabaseService.arretsTransporteurDao
			)
			var updateSuccess = true

			CoroutineScope(Dispatchers.IO).launch {

				try {
					val bufferedReader = tmpFile.bufferedReader()
					var rowNumber = 0
					@Suppress("BlockingMethodInNonBlockingContext") 
					var dataRow = bufferedReader.readLine()

					// Parcours ligne à ligne du fichier
					readFile@ while (dataRow != null) {
						// On passe le header
						if (rowNumber == 0) {
							rowNumber++
							@Suppress("BlockingMethodInNonBlockingContext")
							dataRow = bufferedReader.readLine()
							continue@readFile
						}

						Timber.d("Ligne $rowNumber : $dataRow")
						val fields = dataRow.split(";")

						val arret = ArretTransporteurUi(
							ArTId               = fields[0].toInt(),
							ArTVersion          = fields[1],
							ArTCreated          = fields[2],
							ArTChanged          = fields[3],
							FournisseurId       = fields[4].toInt(),
							FournisseurName     = fields[5],
							ArTName             = fields[6],
							ArTXEpsg2154        = fields[7].toInt(),
							ArTYEpsg2154        = fields[8].toInt(),
							ArRId               = fields[9].toInt(),
							ArTType             = fields[10],
							ArTPrivateCode      = fields[11],
							ArTFareZone         = fields[12],
							ArTAccessibility    = fields[13],
							ArTAudibleSignals   = fields[14],
							ArTVisualSigns      = fields[15],
							ArTTown             = fields[16],
							ArTPostalRegion     = fields[17].toInt()
						)

						arretsTransporteurRepository.addArretTransporteur(arret)

						@Suppress("BlockingMethodInNonBlockingContext")
						dataRow = bufferedReader.readLine()
						rowNumber++
					}
				} catch (exception: Exception) {
					Timber.e(exception, "Exception : ${exception.message}")
					updateSuccess = false
				}

				if (updateSuccess) {
					AppPreferences.idfmStoreFilePath = idfmStoragePath
					Timber.d("Mise à jour terminée avec succès")
				}
			}
		}
	}
}
