package com.thesmarttoolsteam.fixbus.common.database.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

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
	val ArTPostalRegion: Int?
)
