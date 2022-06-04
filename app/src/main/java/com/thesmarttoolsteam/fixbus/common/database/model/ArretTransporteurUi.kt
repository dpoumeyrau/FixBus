package com.thesmarttoolsteam.fixbus.common.database.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import com.thesmarttoolsteam.fixbus.scan.tools.Lambert93ToWGS84Converter
import timber.log.Timber

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
	fun getLatLng(): LatLng? {
		Timber.v("In")

		return Lambert93ToWGS84Converter.toLatLng(
			ArTXEpsg2154?.toDouble(),
			ArTYEpsg2154?.toDouble()
		)
	}
}
