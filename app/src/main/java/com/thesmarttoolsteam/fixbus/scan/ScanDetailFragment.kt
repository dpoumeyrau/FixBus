package com.thesmarttoolsteam.fixbus.scan

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.thesmarttoolsteam.fixbus.FixBusActivity
import com.thesmarttoolsteam.fixbus.FixBusActivityViewModel
import com.thesmarttoolsteam.fixbus.R
import com.thesmarttoolsteam.fixbus.common.AppFragment
import com.thesmarttoolsteam.fixbus.common.AppPermissionManager
import com.thesmarttoolsteam.fixbus.common.getResString
import com.thesmarttoolsteam.fixbus.databinding.FragmentScandetailBinding
import timber.log.Timber


class ScanDetailFragment : AppFragment() {

	private val viewModel: FixBusActivityViewModel by activityViewModels()
	private lateinit var binding: FragmentScandetailBinding
	private lateinit var fusedLocationClient: FusedLocationProviderClient
	private lateinit var locationRequest: LocationRequest
	private lateinit var googleMap: GoogleMap
	private var moveMapOnLocationUpdate = true

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
			LocationRequest.PRIORITY_HIGH_ACCURACY,
			CancellationTokenSource().token
		)
			.addOnSuccessListener { location: Location? ->
				Timber.v("Location : $location")

				if (location != null) {
					Timber.d("Sauvegarde de la position de l'utilisateur")
					viewModel.stopPlace?.newUserLocationLat = location.latitude
					viewModel.stopPlace?.newUserLocationLng = location.longitude
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

				if (viewModel.stopPlace?.GPSLatLng != null) {
					Timber.d("Ajout du marqueur (position courante de l'utilisateur)")
					googleMap.addMarker(
						MarkerOptions()
							.position(viewModel.stopPlace?.GPSLatLng!!)
							.title(viewModel.stopPlace?.ArTName ?: "Indéterminé")
					)
				}

				startLocationUpdates()
			}

		// Affichage du pin sur la position de l'utilisateur
		googleMap.isMyLocationEnabled = true

		binding.ivPin.visibility = View.VISIBLE
		binding.flMaptype.visibility = View.VISIBLE

		// Mise en place des listeners
		setGoogleMapListeners()
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
			}
			Timber.d("${googleMap.cameraPosition.target.latitude} / ${googleMap.cameraPosition.target.longitude}")
		}

		// Clic sur le bouton "Position courante"
		googleMap.setOnMyLocationButtonClickListener {
			Timber.d("In")
			moveMapOnLocationUpdate = true
			false        // On continue le traitement par défaut
		}

		// Gestion du bouton de choix du type de carte
		binding.ivMaptype.setOnClickListener { _ ->
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
					viewModel.stopPlace?.newUserLocationLat = location.latitude
					viewModel.stopPlace?.newUserLocationLng = location.longitude
					viewModel.stopPlace?.NewUserLocationAccuracy = location.accuracy
				}

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
					onMapReady()
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

		locationRequest = LocationRequest.create().apply {
			interval = 500L
			fastestInterval = 250L
			priority = LocationRequest.PRIORITY_HIGH_ACCURACY
		}

		return binding.root
	}

	//==============================================================================================
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		Timber.v("In")
		super.onViewCreated(view, savedInstanceState)

		fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
		setupUi()
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
			viewModel.stopPlace?.ArTPrivateCode
				?: getResString(
					requireContext(),
					R.string.scandetailfragment_stopplacename_nogipa
				) ?: "?",
			viewModel.stopPlace?.ArTName
				?: getResString(
					requireContext(),
					R.string.scandetailfragment_stopplacename_noname
				) ?: "?",
		)

		binding.btnOk.setOnClickListener {
			Timber.v("In")
			viewModel.stopPlace?.newStopPlaceLat = googleMap.cameraPosition.target.latitude
			viewModel.stopPlace?.newStopPlaceLng = googleMap.cameraPosition.target.longitude
			findNavController().navigate(R.id.nav_scanedit)
		}

		binding.btnCancel.setOnClickListener {
			Timber.v("In")
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
	fun stopLocationUpdates() {
		Timber.v("In")
		fusedLocationClient.removeLocationUpdates(locationUpdatesCallback)
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