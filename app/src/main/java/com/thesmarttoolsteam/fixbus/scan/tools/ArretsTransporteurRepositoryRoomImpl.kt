package com.thesmarttoolsteam.fixbus.scan.tools

import com.thesmarttoolsteam.fixbus.common.database.model.ArretTransporteurUi
import com.thesmarttoolsteam.fixbus.common.database.model.ArretsTransporteurDao
import timber.log.Timber

class ArretsTransporteurRepositoryRoomImpl(
	private val arretsTransporteurDao: ArretsTransporteurDao
): ArretsTransporteurRepository {

	//==============================================================================================
	/**
	 * Récupération des informations sur un arrêt à partir de son code [gipa]
	 */
	//----------------------------------------------------------------------------------------------
	override suspend fun getArretsTransporteurByGipa(gipa: String): ArretTransporteurUi? {
		Timber.v("In")
		Timber.d("Recherche du code gipa : $gipa")

		val result = arretsTransporteurDao.getArretsTransporteurByGipa(gipa)
		Timber.d("Nombre de lignes retournées : ${result.size}")

		return result.firstOrNull()
	}
}