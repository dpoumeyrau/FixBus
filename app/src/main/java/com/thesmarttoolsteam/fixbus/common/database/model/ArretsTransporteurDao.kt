package com.thesmarttoolsteam.fixbus.common.database.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query

@Dao
interface ArretsTransporteurDao {

	@Query("SELECT * FROM `arrets-transporteur` WHERE fournisseurid = 59 AND artprivatecode = :gipa")
	suspend fun getArretTransporteurByGipa(gipa: String) : List<ArretTransporteurUi>

	@Query ("SELECT COUNT(*) FROM `arrets-transporteur`")
	suspend fun getArretTransporteurCount() : Int
}