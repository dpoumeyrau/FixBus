package com.thesmarttoolsteam.fixbus.scan.tools

import com.thesmarttoolsteam.fixbus.common.database.model.ArretTransporteurUi

interface ArretsTransporteurRepository {
	suspend fun getArretsTransporteurByGipa(gipa: String): ArretTransporteurUi?
	suspend fun addArretTransporteur(arretTransporteurUi: ArretTransporteurUi) : Long
}