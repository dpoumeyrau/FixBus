package com.thesmarttoolsteam.fixbus.scan

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.thesmarttoolsteam.fixbus.FixBusActivityViewModel
import com.thesmarttoolsteam.fixbus.R
import com.thesmarttoolsteam.fixbus.common.AppFragment
import com.thesmarttoolsteam.fixbus.common.AppPermissionManager
import com.thesmarttoolsteam.fixbus.common.database.model.ArretFixBusUi
import com.thesmarttoolsteam.fixbus.common.tools.getResString
import com.thesmarttoolsteam.fixbus.common.tools.isLocationAvailable
import com.thesmarttoolsteam.fixbus.common.tools.isNetworkAvailable
import com.thesmarttoolsteam.fixbus.databinding.FragmentScandetailBinding
import timber.log.Timber


class ScanDetailFragment : AppFragment() {

	private val viewModel: FixBusActivityViewModel by activityViewModels()
	private lateinit var binding: FragmentScandetailBinding
	private lateinit var fusedLocationClient: FusedLocationProviderClient
	private lateinit var locationRequest: LocationRequest
	private lateinit var googleMap: GoogleMap
	private var moveMapOnLocationUpdate = true
	private lateinit var lastLocation: Location
	private var manualPositionning = false
	private var mapDisplayed = false

	//==============================================================================================
	/**
	 * Fin d'initialisation de la carte : Traitements complémentaires
	 * - Récupération de la localisation de l'utilisateur
	 * - Mise à jour de l'interface et mise en place des listeners.
	 */
	//----------------------------------------------------------------------------------------------
	@SuppressLint("MissingPermission")
	private val onMapReadyCallback = OnMapReadyCallback { googleMap ->
		Timber.v("In")

		this.googleMap = googleMap
		fusedLocationClient.getCurrentLocation(
			Priority.PRIORITY_HIGH_ACCURACY,
			CancellationTokenSource().token
		)
			.addOnSuccessListener { location: Location? ->
				Timber.v("Location : $location")

				if (location != null) {
					lastLocation = location
				}

				if ((location != null) && (moveMapOnLocationUpdate)){
					Timber.d("Déplacement de la caméra de la carte")
					googleMap.moveCamera(
						CameraUpdateFactory.newLatLng(
							LatLng(
								location.latitude,
								location.longitude
							)
						)
					)
				}

				if (viewModel.stopPlace?.idfmData?.getLatLng() != null) {
					Timber.d("Ajout du marqueur (position courante de l'utilisateur)")
					googleMap.addMarker(
						MarkerOptions()
							.position(viewModel.stopPlace?.idfmData?.getLatLng()!!)
							.title(viewModel.stopPlace?.idfmData?.ArTName ?: "Indéterminé")
					)
				}

				// Affichage du pin sur la position de l'utilisateur
				googleMap.isMyLocationEnabled = true

				binding.ivPin.visibility = View.VISIBLE
				binding.flMaptype.visibility = View.VISIBLE

				// Mise en place des listeners
				setGoogleMapListeners()

				mapDisplayed = true
			}
	}

	//==============================================================================================
	/**
	 * Mise en place des listeners sur la carte
	 */
	//----------------------------------------------------------------------------------------------
	private fun setGoogleMapListeners() {
		Timber.v("In")

		// Désactivation de la mise à jour automatique de la position si l'utilisateur déplace la carte
		googleMap.setOnCameraMoveStartedListener  { reason ->
			Timber.v("In")
			if ((moveMapOnLocationUpdate) && (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE)) {
				Timber.d("Déplacement de la map par l'utilisateur")
				moveMapOnLocationUpdate = false
				manualPositionning = true
			}
			Timber.d("${googleMap.cameraPosition.target.latitude} / ${googleMap.cameraPosition.target.longitude}")
		}

		// Clic sur le bouton "Position courante"
		googleMap.setOnMyLocationButtonClickListener {
			Timber.d("In")
			moveMapOnLocationUpdate = true
			manualPositionning = false
			false        // On continue le traitement par défaut
		}

		// Gestion du bouton de choix du type de carte
		binding.ivMaptype.setOnClickListener {
			Timber.v("In")

			if (googleMap.mapType == GoogleMap.MAP_TYPE_NORMAL) {
				Timber.d("Activation de la vue satellite")

				googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
				binding.ivMaptype.setImageResource(R.drawable.ic_map)
			} else {
				Timber.d("Activation de la vue normale")

				googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
				binding.ivMaptype.setImageResource(R.drawable.ic_satellite)
			}
		}
	}

	//==============================================================================================
	/**
	 * Mise à jour de la position de l'utilisateur
	 */
	//----------------------------------------------------------------------------------------------
	@SuppressLint("MissingPermission")
	private val locationUpdatesCallback = object : LocationCallback() {
		var mapCircle: Circle? = null

		override fun onLocationResult(locationResult: LocationResult) {
			Timber.v("In")

			for (location in locationResult.locations) {
				Timber.v("Location : $location")
				Timber.v("Précision : ${location.accuracy}")

				if (location != null) {
					Timber.d("Sauvegarde de la position de l'utilisateur")
					lastLocation = location
					binding.btnOk.isEnabled = true
				}

				if (mapDisplayed) {
					googleMap.apply {
						if (moveMapOnLocationUpdate) {
							moveCamera(
								CameraUpdateFactory.newLatLng(
									LatLng(
										location.latitude,
										location.longitude
									)
								)
							)
						}

						mapCircle?.remove()
						mapCircle = addCircle(
							CircleOptions()
								.center(LatLng(location.latitude, location.longitude))
								.radius(location.accuracy.toDouble())
								.strokeColor(
									if (location.accuracy < 5) Color.GREEN else Color.RED
								)
						)
					}
				}
			}
		}
	}

	//==============================================================================================
	/**
	 * Initialisation du fragment : Création de la vue
	 *
	 * @param inflater Gestionnaire de construction de la vue
	 * @param container Container de la vue
	 * @param savedInstanceState Données sauvegardées le cas échéant
	 * @return Vue créée
	 */
	//----------------------------------------------------------------------------------------------
	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		Timber.v("In")

		binding = FragmentScandetailBinding.inflate(inflater)
		fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
		locationRequest = LocationRequest.create().apply {
			interval = 500L
			fastestInterval = 250L
			priority =  Priority.PRIORITY_HIGH_ACCURACY
		}

		Timber.d("Contrôle des permissions")
		AppPermissionManager(
			this,
			Manifest.permission.ACCESS_FINE_LOCATION,
			object: AppPermissionManager.AppPermissionManagerInterface {

				//==================================================================================
				// Affichage des explications complémentaires
				//----------------------------------------------------------------------------------
				override fun onPermissionRationaleRequested(continueWithPermissionCallback: ()->Unit) {
					Timber.v("In")
					showLocationPermissionRationaleDialogBox {
						continueWithPermissionCallback()
					}
				}

				//==================================================================================
				// La permission a été acceptée, on déclenche le traitement
				//----------------------------------------------------------------------------------
				override fun onPermissionGrantedCallback() {
					Timber.v("In")

					askForLocation()
					askForConnection()
					onMapReady()
					startLocationUpdates()
				}

				//==================================================================================
				// Traitement du refus de permission
				//----------------------------------------------------------------------------------
				override fun onPermissionDeniedCallback() {
					Timber.v("In")
					showLocationPermissionDeniedDialogBox()
				}
			}
		)


		return binding.root
	}

	//==============================================================================================
	/**
	 * Demande d'activation de la localisation dans le cas où elle n'est pas activée
	 */
	//----------------------------------------------------------------------------------------------
	private fun askForLocation() {
		Timber.v("In")

		if (!isLocationAvailable(requireContext())) {
			Timber.d("Localisation non activée => Demande d'activation")
			val locationDialogBoxBuilder = MaterialAlertDialogBuilder(requireContext())
				.setTitle(
					getResString(requireContext(),
						R.string.scandetailfragment_location_title)
				)
				.setMessage(getResString(
					requireContext(),
					R.string.scandetailfragment_location_message
				))
				.setCancelable(false)
				.setPositiveButton(
					getResString(requireContext(), R.string.scandetailfragment_location_action_ok),
					null
				)
				.setNegativeButton(
					getResString(requireContext(), R.string.scandetailfragment_location_action_cancel)
				) { _, _ -> Timber.v("In")
					requireActivity().finish()
				}
			val locationDialogBox = locationDialogBoxBuilder.show()

			val positiveButton = locationDialogBox.getButton(DialogInterface.BUTTON_POSITIVE)
			positiveButton.setOnClickListener {
				locationDialogBox.dismiss()
				if (!isLocationAvailable(requireContext())) {
					Timber.d("La localisation n'est toujours pas activée :-(")
					askForLocation()
				}
			}
		}
	}

	//==============================================================================================
	/**
	 * Demande d'activation de la localisation dans le cas où elle n'est pas activée
	 */
	//----------------------------------------------------------------------------------------------
	private fun askForConnection() {
		Timber.v("In")

		if (!isNetworkAvailable(requireContext())) {
			Timber.d("Connexion non activée => Demande d'activation")
			val connectionDialogBoxBuilder = MaterialAlertDialogBuilder(requireContext())
				.setTitle(
					getResString(requireContext(),
						R.string.scandetailfragment_connection_title)
				)
				.setMessage(getResString(
					requireContext(),
					R.string.scandetailfragment_connection_message
				))
				.setCancelable(false)
				.setPositiveButton(
					getResString(requireContext(), R.string.scandetailfragment_connection_action_ok),
					null
				)
				.setNegativeButton(
					getResString(requireContext(), R.string.scandetailfragment_connection_action_cancel)
				) { _, _ -> Timber.v("In")
					requireActivity().finish()
				}
			val connectionDialogBox = connectionDialogBoxBuilder.show()

			val positiveButton = connectionDialogBox.getButton(DialogInterface.BUTTON_POSITIVE)
			positiveButton.setOnClickListener {
				connectionDialogBox.dismiss()
				if (!isNetworkAvailable(requireContext())) {
					Timber.d("La connexion internet n'est toujours pas activée :-(")
					askForConnection()
				}
			}
		}
	}

	//==============================================================================================
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		Timber.v("In")
		super.onViewCreated(view, savedInstanceState)

		setupUi()

		startLocationUpdates()
	}

	//==============================================================================================
	/**
	 * Mise en place de l'interface
	 */
	//----------------------------------------------------------------------------------------------
	private fun setupUi() {
		Timber.v("In")

		binding.tvStopname.text = getResString(
			requireContext(),
			R.string.scandetailfragment_stopplacename,
			viewModel.stopPlace?.gipaCode
				?: getResString(
					requireContext(),
					R.string.scandetailfragment_stopplacename_nogipa
				) ?: "viewModel.stopPlace?.gipaCode",
			viewModel.stopPlace?.idfmData?.ArTName
				?: getResString(
					requireContext(),
					R.string.scandetailfragment_stopplacename_noname
				) ?: "?",
		)

		binding.btnOk.setOnClickListener {
			Timber.v("In")
			if (viewModel.stopPlace?.fixBusData == null) {
				Timber.d("Création d'un nouveau fixBusData")
				viewModel.stopPlace = viewModel.stopPlace?.copy(
					fixBusData = ArretFixBusUi(
						userPositionLat = lastLocation.latitude,
						userPositionLng = lastLocation.longitude,
						userPositionAccuracy = lastLocation.accuracy,
						mapDisplayed = mapDisplayed,
						stopGIPA = viewModel.stopPlace?.gipaCode,
						stopName = viewModel.stopPlace?.idfmData?.ArTName,
						stopPositionLat = googleMap.cameraPosition.target.latitude,
						stopPositionLng = googleMap.cameraPosition.target.longitude,
						stopManualPositionning = manualPositionning,
						stopType = null,
						stopBIVType = null,
						stopFareZone = viewModel.stopPlace?.idfmData?.ArTFareZone,
						stopAccessibility =
						when (viewModel.stopPlace?.idfmData?.ArTAccessibility) {
							"true" -> true
							"false" -> false
							else -> null
						},
						stopAudibleSignals =
						when (viewModel.stopPlace?.idfmData?.ArTAudibleSignals) {
							"true" -> true
							"false" -> false
							else -> null
						},
						stopVisualSigns =
						when (viewModel.stopPlace?.idfmData?.ArTVisualSigns) {
							"true" -> true
							"false" -> false
							else -> null
						},
						stopUSBCharger = null,
						stopInterventionNeeded = null,
						stopComments = null
					)
				)
			} else {
				Timber.d("Mise à jour du fixBusData déjà existant")
				viewModel.stopPlace = viewModel.stopPlace?.copy(
					fixBusData = viewModel.stopPlace?.fixBusData?.copy (
						userPositionLat = lastLocation.latitude,
						userPositionLng = lastLocation.longitude,
						userPositionAccuracy =  lastLocation.accuracy,
						stopPositionLat = googleMap.cameraPosition.target.latitude,
						stopPositionLng = googleMap.cameraPosition.target.longitude,
						mapDisplayed = mapDisplayed
					)
				)
			}

			stopLocationUpdates()
			findNavController().navigate(R.id.nav_scanedit)
		}

		binding.btnCancel.setOnClickListener {
			Timber.v("In")
			stopLocationUpdates()
			findNavController().navigate(R.id.nav_scan)
		}
	}

	//==============================================================================================
	/**
	 * Destruction du fragment
	 */
	//----------------------------------------------------------------------------------------------
	override fun onDestroy() {
		Timber.v("In")

		stopLocationUpdates()
		super.onDestroy()
	}

	//==============================================================================================
	/**
	 * Démarrage de la surveillance de la modification de la position courante
	 */
	//----------------------------------------------------------------------------------------------
	@SuppressLint("MissingPermission")
	fun startLocationUpdates() {
		Timber.v("In")
		fusedLocationClient.requestLocationUpdates(
			locationRequest,
			locationUpdatesCallback,
			Looper.getMainLooper()
		)
	}

	//==============================================================================================
	/**
	 * Arrêt de la surveillance de la modification de la position courante
	 */
	//----------------------------------------------------------------------------------------------
	private fun stopLocationUpdates() {
		Timber.v("In")
		if (::fusedLocationClient.isInitialized) {
			fusedLocationClient.removeLocationUpdates(locationUpdatesCallback)
		}
	}

	//==============================================================================================
	/**
	 * Affichage de la carte dans la vue, une fois chargée, et après le contrôle des permissions
	 */
	//----------------------------------------------------------------------------------------------
	private fun onMapReady() {
		Timber.v("In")

		val mapFragment = childFragmentManager.findFragmentById(R.id.mv_map) as SupportMapFragment?
		mapFragment?.getMapAsync(onMapReadyCallback)
	}

	//==============================================================================================
	/**
	 * Affichae de la boîte de dialogue indiquant que les droits à la localisation n'ont pas été
	 * accordés et que le positionnement sur la carte doit être fait manuellement.
	 */
	//----------------------------------------------------------------------------------------------
	private fun showLocationPermissionDeniedDialogBox() {
		Timber.v("In")

		val title = getResString(context, R.string.scandetailfragment_permission_dialogbox_title)
		val message = getResString(context, R.string.scandetailfragment_permission_dialogbox_message)
		val action = getResString(context, R.string.scandetailfragment_permission_dialogbox_action)

		val dialogBox = MaterialAlertDialogBuilder(requireActivity())
			.setTitle(title)
			.setMessage(message)
			.setCancelable(false)
			.setPositiveButton(action) { _, _ -> }
			.create()
		dialogBox.setCanceledOnTouchOutside(false)
		dialogBox.show()
	}

	//==============================================================================================
	/**
	 * Affichage des informations complémentaires pour la demande d'autorisation de localisation
	 *
	 * @param onPositiveClickCallback Callback pour poursuivre le workflow d'autorisation
	 */
	//----------------------------------------------------------------------------------------------
	private fun showLocationPermissionRationaleDialogBox(onPositiveClickCallback: () -> Unit) {
		Timber.v("In")

		val title =
			getResString(context, R.string.scandetailfragment_permission_rationale_dialogbox_title)
		val message =
			getResString(context, R.string.scandetailfragment_permission_rationale_dialogbox_message)
		val action =
			getResString(context, R.string.scandetailfragment_permission_rationale_dialogbox_action)

		val dialogBox = MaterialAlertDialogBuilder(requireActivity())
			.setTitle(title)
			.setMessage(message)
			.setCancelable(false)
			.setPositiveButton(action) { _, _ -> onPositiveClickCallback() }
			.create()
		dialogBox.setCanceledOnTouchOutside(false)
		dialogBox.show()
	}
}