package com.thesmarttoolsteam.fixbus.scan.ui

import com.thesmarttoolsteam.fixbus.common.database.model.ArretFixBusUi
import com.thesmarttoolsteam.fixbus.common.database.model.ArretTransporteurUi

//==================================================================================================
/**
 * Classe de définition du détail d'un arrêt
 *
 * @property gipaCode Code GIPA
 * @property idfmData Données issues de l'open data IDFM
 * @property fixBusData Données de l'application
 */
//--------------------------------------------------------------------------------------------------
data class StopPlaceUi(
	val gipaCode: String,
	val idfmData: ArretTransporteurUi?,
	var fixBusData: ArretFixBusUi?,
)

