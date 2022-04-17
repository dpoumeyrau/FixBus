package com.thesmarttoolsteam.fixbus.scan.ui

import com.google.android.gms.maps.model.LatLng
import com.thesmarttoolsteam.fixbus.scan.tools.Lambert93ToWGS84Converter

//==================================================================================================
/**
 * Classe de définition du détail d'un arrêt
 *
 * @property ArTPrivateCode Code GIPA
 * @property ArTName Nom de l'arrêt
 * @property ArTFareZone Zone de l'arrêt
 * @property ArTAccessibility Accessibilité de l'arrêt
 * @property newStopPlacePlaceType Type d'arrêt
 */
//--------------------------------------------------------------------------------------------------
data class StopPlaceUi(
	val ArTPrivateCode: Int,
	val ArTName: String? = null,
	val ArTXEpsg2154: Long? = null,
	val ArTYEpsg2154: Long? = null,
	val ArTFareZone: Int?  = null,
	val ArTAccessibility: Boolean? = null,
	val GPSLatLng: LatLng? = Lambert93ToWGS84Converter.toLatLng(
		ArTXEpsg2154?.toDouble(),
		ArTYEpsg2154?.toDouble()
	),
	var newUserLocationLat: Double? = null,
	var newUserLocationLng: Double? = null,
	var NewUserLocationAccuracy: Float? = null,
	var newArTName: String? = null,
	var newStopPlaceLat: Double? = null,
	var newStopPlaceLng: Double? = null,
	var newStopPlacePlaceType: StopPlaceTypeEnum? = null,
	var newArTFareZone: Int? = null,
	var newArTAccessibility: Boolean? = null,
	val newBIVType: BivEnum? = null,
	val newSoundSystem: Boolean? = null,
	val newCharger: Boolean? = null,
	val newComments: String? = null
)

