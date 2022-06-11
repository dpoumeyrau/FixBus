package com.thesmarttoolsteam.fixbus.common.database.model

data class ArretFixBusUi (
	var userPositionLat: Double?,
	var userPositionLng: Double?,
	var userPositionAccuracy: Float?,
	var mapDisplayed: Boolean?,
	var stopGIPA: String?,
	var stopName: String?,
	var stopPositionLat : Double?,
	var stopPositionLng : Double?,
	val stopManualPositionning: Boolean?,
	var stopType: String?,
	var stopBIVType: String?,
	var stopFareZone: String?,
	var stopAccessibility: Boolean?,
	var stopAudibleSignals: Boolean?,
	var stopVisualSigns: Boolean?,
	var stopUSBCharger: Boolean?,
	var stopInterventionNeeded: Boolean?,
	var stopComments: String?
)