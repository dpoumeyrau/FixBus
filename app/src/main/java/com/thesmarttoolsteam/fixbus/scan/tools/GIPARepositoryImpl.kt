package com.thesmarttoolsteam.fixbus.scan.tools

import android.content.Context
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.thesmarttoolsteam.fixbus.FixBusApp
import com.thesmarttoolsteam.fixbus.R
import com.thesmarttoolsteam.fixbus.common.getResInteger
import com.thesmarttoolsteam.fixbus.common.tools.isNetworkAvailable
import com.thesmarttoolsteam.fixbus.scan.ui.StopPlaceUi
import timber.log.Timber
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class GIPARepositoryImpl(
	private val context: Context,
	private val gipaRepositoryInterfaceCallbacks: GIPARepository.GIPARepositoryInterfaceCallbacks
) : GIPARepository {

	private var resultReceived = AtomicBoolean(false)

	//==============================================================================================
	override suspend fun getStopPlaceFromGIPA(  gipa: Int){
		Timber.v("In")

		val reference = FixBusApp.appFirebaseServices.getFirebaseRealtimeDatabase()
			?.getReference("arrets_transporteurs")
			?.orderByChild("ArTPrivateCode")
			?.equalTo("$gipa")

		if (reference == null) {
			Timber.d("Erreur d'initialisation de la requête Firebase Realtime Database")
			gipaRepositoryInterfaceCallbacks.onGIPASearchError(StopPlaceUi(ArTPrivateCode = gipa))
		}

		val dataFetchEventListener = object: ValueEventListener{

			//======================================================================================
			// Retour de Firebase
			//--------------------------------------------------------------------------------------
			override fun onDataChange(snapshot: DataSnapshot) {
				Timber.v("In")

				resultReceived.set(true)
				when (val rowsCount = snapshot.children.count()) {
					0 -> {
						Timber.w("Arrêt $gipa non trouvé")
						gipaRepositoryInterfaceCallbacks.onGIPANotFound(
							StopPlaceUi(ArTPrivateCode = gipa)
						)
					}
					1 ->  {
						Timber.d("Un arrêt $gipa trouvé")
						val stopPlace = snapshot.children.first().value as? Map<*,*>
						gipaRepositoryInterfaceCallbacks.onGIPAFound(getStopPlace(stopPlace!!))
					}
					else -> {
						Timber.w("$rowsCount arrêts $gipa trouvés (on prend le premier)")
						val stopPlace = snapshot.children.first().value as? Map<*,*>
						gipaRepositoryInterfaceCallbacks.onGIPAFound(getStopPlace(stopPlace!!))
					}
				}
			}

			//======================================================================================
			// Annulation de l'interrogation Firebase
			//--------------------------------------------------------------------------------------
			override fun onCancelled(error: DatabaseError) {
				Timber.v("In")

				resultReceived.set(true)
				Timber.e(error.toException(), "Exception : ${error.toException().message}")
				gipaRepositoryInterfaceCallbacks.onGIPASearchError(
					StopPlaceUi(ArTPrivateCode = gipa)
				)
			}

		}

		if (isNetworkAvailable(context)) {
			Timber.d("Mise en place du timeout")

			val timeoutDuration = getResInteger(
				context,
				R.integer.gradle_configFirebaseRelatimeDatabaseTimeout
			)!!.toLong()
			Timber.d("Durée du timeout : $timeoutDuration")

			reference?.addListenerForSingleValueEvent(dataFetchEventListener)
			val timeout = Timer()
			val timerTask = object: TimerTask() {
				override fun run() {
					Timber.v("In")

					timeout.cancel()
					if (!resultReceived.get()) {
						Timber.d("Interruption de la recherche")
						reference?.removeEventListener(dataFetchEventListener)
						gipaRepositoryInterfaceCallbacks.onGIPATimeout(
							StopPlaceUi(ArTPrivateCode = gipa)
						)
					}
				}
			}
			timeout.schedule(timerTask,timeoutDuration)
		} else {
			Timber.d("Pas de connexion Internet")
			gipaRepositoryInterfaceCallbacks.onGIPANoInternetConnection(
				StopPlaceUi(ArTPrivateCode = gipa)
			)
		}
	}

	//==============================================================================================
	/**
	 * Construction de l'object StopPlaceUi sur la base des données récupérées de Firebase
	 *
	 * @param stopPlace Données récupérées de Firebas
	 * @return Objet StopPlaceUi
	 */
	//----------------------------------------------------------------------------------------------
	private fun getStopPlace(stopPlace: Map<*,*>): StopPlaceUi {
		Timber.v("In")

		val result = StopPlaceUi(
			ArTPrivateCode = Integer.parseInt(stopPlace["ArTPrivateCode"] as String),
			ArTName = stopPlace["ArTName"] as String,
			ArTXEpsg2154 = stopPlace["ArTXEpsg2154"] as Long,
			ArTYEpsg2154 = stopPlace["ArTYEpsg2154"] as Long,
			ArTFareZone = Integer.parseInt(stopPlace["ArTFareZone"] as String),
			ArTAccessibility = (stopPlace["ArTFareZone"] as String == "true")
		)

		Timber.d("Arrêt retourné : $result")
		return result
	}
}