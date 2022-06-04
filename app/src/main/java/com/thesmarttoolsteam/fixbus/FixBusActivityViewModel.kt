package com.thesmarttoolsteam.fixbus

import androidx.lifecycle.ViewModel
import com.thesmarttoolsteam.fixbus.scan.ui.StopPlaceUi

//==================================================================================================
/**
 * View Model transverse à l'activité et accessible depuis les fragments.
 */
//--------------------------------------------------------------------------------------------------
class FixBusActivityViewModel : ViewModel() {

	var stopPlace: StopPlaceUi? = null
	var userId: String? = null
}