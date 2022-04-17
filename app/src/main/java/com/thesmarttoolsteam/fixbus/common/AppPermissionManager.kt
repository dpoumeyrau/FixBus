package com.thesmarttoolsteam.fixbus.common

import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import timber.log.Timber


//==================================================================================================
/**
 * Gestionnaire de permission simplifié
 *
 * @property fragment Fragment concerné par la demande de permission
 * @property requestedPermission Permission demandée
 * @property permissionManagerInterfaceCallbacks Callbacks (voir interface)
 */
//--------------------------------------------------------------------------------------------------
class AppPermissionManager(
	private val fragment: Fragment,
	private val requestedPermission: String,
	private var permissionManagerInterfaceCallbacks: AppPermissionManagerInterface
) {

	interface AppPermissionManagerInterface {
		//==========================================================================================
		/**
		 * Méthode appelée lorsque le système demande l'affichage d'explications pour la demande
		 * de permission
		 *
		 * @param continueWithPermissionCallback Méthode appelée pour continuer le workflow
		 */
		//------------------------------------------------------------------------------------------
		fun onPermissionRationaleRequested(continueWithPermissionCallback: ()->Unit)

		//==========================================================================================
		/**
		 * Méthode appelée lorsque les droits sont accordés
		 */
		//------------------------------------------------------------------------------------------
		fun onPermissionGrantedCallback()

		//==========================================================================================
		/**
		 * Méthode appelée lorsque les droits ont été refusés
		 */
		//------------------------------------------------------------------------------------------
		fun onPermissionDeniedCallback()
	}

	private var requestPermissionLauncher: ActivityResultLauncher<String>

	//==============================================================================================
	init {
		Timber.v("In")

		requestPermissionLauncher = getRequestPermissionLauncher()
		managePermission()
	}

	//==============================================================================================
	/**
	 * Récupération du gestionnaire de permission
	 *
	 * @return Launcher
	 */
	//----------------------------------------------------------------------------------------------
	private fun getRequestPermissionLauncher(): ActivityResultLauncher<String> {

		Timber.v("In")

		val launcher =
			fragment.registerForActivityResult(
				ActivityResultContracts.RequestPermission()
			) { isGranted: Boolean ->

				Timber.v("In")

				when (isGranted) {
					true -> {
						Timber.d("Permission $requestedPermission : OK")
						permissionManagerInterfaceCallbacks.onPermissionGrantedCallback()
					}
					false -> {
						Timber.d("Permission $requestedPermission : KO")
						permissionManagerInterfaceCallbacks.onPermissionDeniedCallback()
					}
				}
			}

		return launcher
	}

	//==============================================================================================
	/**
	 * Workflow de gestion des permissions
	 */
	//----------------------------------------------------------------------------------------------
	private fun managePermission() {
		Timber.v("In")

		when {
			// Permission déjà accordée ?
			ContextCompat.checkSelfPermission(
				fragment.requireContext(),
				requestedPermission
			) == PackageManager.PERMISSION_GRANTED -> {
				Timber.d("Permission OK")
				permissionManagerInterfaceCallbacks.onPermissionGrantedCallback()
			}

			// Demande du système de rationale ?
			ActivityCompat.shouldShowRequestPermissionRationale(
				fragment.requireActivity(),
				requestedPermission
			) -> {
				Timber.d("Affichage de l'explication (rationalePermission)")
				permissionManagerInterfaceCallbacks.onPermissionRationaleRequested {
					requestPermissionLauncher.launch(requestedPermission)
				}
			}

			// Demande de permission
			else -> {
				Timber.d("Demande de permission")
				requestPermissionLauncher.launch(requestedPermission)
			}
		}
	}
}