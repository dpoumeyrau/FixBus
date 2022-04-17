package com.thesmarttoolsteam.fixbus.scan.tools

import com.thesmarttoolsteam.fixbus.scan.ui.StopPlaceUi

interface GIPARepository {
	interface GIPARepositoryInterfaceCallbacks {
		//==========================================================================================
		/**
		 * Méthode appelée lorsque le détail d'un code GIPA a été trouvé
		 *
		 * @param stopPlace Détail de l'arrêt correspondant au code GIPA
		 */
		//------------------------------------------------------------------------------------------
		fun onGIPAFound(stopPlace: StopPlaceUi)

		//==========================================================================================
		/**
		 * Méthode appelé lorsque le code GIPA n'a pas été trouvé
		 *
		 * @param stopPlace StopPlace (uniquement code GIPA renseigné)
		 */
		//------------------------------------------------------------------------------------------
		fun onGIPANotFound(stopPlace: StopPlaceUi)

		//==========================================================================================
		/**
		 * Méthode appelée en cas d'erreur lors de la recherche du code GIPA
		 */
		//------------------------------------------------------------------------------------------
		fun onGIPASearchError(stopPlace: StopPlaceUi)

		//==========================================================================================
		/**
		 * Méthode appelée s'il n'y a pas de connexion internet
		 */
		//------------------------------------------------------------------------------------------
		fun onGIPANoInternetConnection(stopPlace: StopPlaceUi)

		//==========================================================================================
		/**
		 * Méthode appelée en cas de timeout (propriété dans le gradle)
		 */
		//------------------------------------------------------------------------------------------
		fun onGIPATimeout(stopPlace: StopPlaceUi)
	}

	//==============================================================================================
	/**
	 * Recherche des informations sur un arrêt à partir de son code GIPA
	 *
	 * @param gipa Code GIPA de l'arrêt à trouver
	 */
	//----------------------------------------------------------------------------------------------
	suspend fun getStopPlaceFromGIPA(gipa: Int)
}