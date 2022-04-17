package com.thesmarttoolsteam.fixbus.common.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.thesmarttoolsteam.fixbus.common.FIXBUSDB_VERSION
import com.thesmarttoolsteam.fixbus.common.database.model.ArretTransporteurUi
import com.thesmarttoolsteam.fixbus.common.database.model.ArretsTransporteurDao

//==================================================================================================
/**
 * Base de l'application [RoomDatabase]
 */
//--------------------------------------------------------------------------------------------------
@Database(entities = [ArretTransporteurUi::class], version = FIXBUSDB_VERSION)
abstract class AppDatabaseRef : RoomDatabase() {
	abstract fun arretsTransporteurDao() : ArretsTransporteurDao

}