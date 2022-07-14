package com.thesmarttoolsteam.fixbus.common.database.model

import androidx.room.*

@Dao
interface ArretsTransporteurDao {

	@Query("SELECT * FROM `arrets-transporteur` WHERE fournisseurid = 59 AND artprivatecode = :gipa")
	fun getArretsTransporteurByGipa(gipa: String) : List<ArretTransporteurUi>

	@Query ("SELECT COUNT(*) FROM `arrets-transporteur`")
	fun getArretTransporteurCount() : Int

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insertArretTransporteur(arret: ArretTransporteurUi) : Long
}