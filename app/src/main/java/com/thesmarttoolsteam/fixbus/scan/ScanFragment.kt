package com.thesmarttoolsteam.fixbus.scan

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.thesmarttoolsteam.fixbus.FixBusActivityViewModel
import com.thesmarttoolsteam.fixbus.FixBusApp
import com.thesmarttoolsteam.fixbus.R
import com.thesmarttoolsteam.fixbus.common.AppFragment
import com.thesmarttoolsteam.fixbus.common.AppPermissionManager
import com.thesmarttoolsteam.fixbus.common.GIPAQrCodeAnalyzer
import com.thesmarttoolsteam.fixbus.common.tools.getResString
import com.thesmarttoolsteam.fixbus.databinding.FragmentScanBinding
import com.thesmarttoolsteam.fixbus.scan.tools.ArretsTransporteurRepository
import com.thesmarttoolsteam.fixbus.scan.tools.ArretsTransporteurRepositoryRoomImpl
import com.thesmarttoolsteam.fixbus.scan.ui.StopPlaceUi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanFragment : AppFragment() {

	private lateinit var binding: FragmentScanBinding
	private lateinit var cameraExecutor: ExecutorService
	private lateinit var gipaQrCodeAnalyzer: GIPAQrCodeAnalyzer
	private val viewModel: FixBusActivityViewModel by activityViewModels()
	private val arretsTransporteurRepository: ArretsTransporteurRepository by lazy {
		ArretsTransporteurRepositoryRoomImpl(FixBusApp.appDatabaseService.arretsTransporteurDao)
	}

	//==============================================================================================
	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		Timber.v("In")
		binding = FragmentScanBinding.inflate(inflater, container, false)

		return binding.root
	}

	//==============================================================================================
	/**
	 * Personnalisation de la vue
	 *
	 * @param view Vue créée
	 * @param savedInstanceState Données sauvegardées
	 */
	//----------------------------------------------------------------------------------------------
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		Timber.v("In")
		super.onViewCreated(view, savedInstanceState)

		cameraExecutor = Executors.newSingleThreadExecutor()
		setupUi()
	}

	//==============================================================================================
	/**
	 * Destruction du fragment : Fin du thread de la caméra
	 */
	//----------------------------------------------------------------------------------------------
	override fun onDestroy() {
		Timber.v("In")

		if (::binding.isInitialized) {
			binding.ivTarget.visibility = View.INVISIBLE
		}

		if (::cameraExecutor.isInitialized) {
			cameraExecutor.shutdown()
		}
		super.onDestroy()
	}


	//==============================================================================================
	/**
	 * Initialisation de l'interface et lancement de la caméra après contrôle des permissions
	 */
	//----------------------------------------------------------------------------------------------
	private fun setupUi() {
		Timber.v("In")

		manageCameraPermission()
	}

	//==============================================================================================
	/**
	 * Gestion des permissions
	 */
	//----------------------------------------------------------------------------------------------
	private fun manageCameraPermission() {
		Timber.v("In")

		AppPermissionManager(
			this,
			Manifest.permission.CAMERA,
			object: AppPermissionManager.AppPermissionManagerInterface {

				//==================================================================================
				/**
				 * Affichage des explications complémentaires
				 *
				 * @param continueWithPermissionCallback vers le gestionnaire de permission si l'utilisateur veut aller
				 * plus loin
				 */
				//----------------------------------------------------------------------------------
				override fun onPermissionRationaleRequested(continueWithPermissionCallback: ()->Unit) {
					Timber.v("In")
					showCameraPermissionRationaleDialogBox {
						continueWithPermissionCallback()
					}
				}

				//==================================================================================
				/**
				 * La permission a été acceptée, on déclenche le traitement
				 */
				//----------------------------------------------------------------------------------
				override fun onPermissionGrantedCallback() {
					Timber.v("In")
					startCamera()
				}

				//==================================================================================
				/**
				 * Traitement du refus de permission
				 */
				//----------------------------------------------------------------------------------
				override fun onPermissionDeniedCallback() {
					Timber.v("In")
					showCameraPermissionDeniedDialogBox()
				}
			}
		)

	}

	//==============================================================================================
	/**
	 * Affichage de la boîte de dialogue indiquant que la permission nécessaire n'a pas été donnée
	 */
	//----------------------------------------------------------------------------------------------
	private fun showCameraPermissionDeniedDialogBox() {
		Timber.v("In")

		val title = getResString(context, R.string.scanfragment_permission_dialogbox_title)
		val message = getResString(context, R.string.scanfragment_permission_dialogbox_message)
		val action = getResString(context, R.string.scanfragment_permission_dialogbox_action)

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
	 * Affichage de la boîte de dialogue d'explications complémentaires ("Rationale")
	 * pour préciser pourquoi les droits sont nécessaires
	 *
	 * @param onPositiveClickCallback Méthode à appeler sur le bouton "OK" (demande d'autorisation)
	 */
	//----------------------------------------------------------------------------------------------
	private fun showCameraPermissionRationaleDialogBox(onPositiveClickCallback: () -> Unit) {
		Timber.v("In")

		val title =
			getResString(context, R.string.scanfragment_permission_rationale_dialogbox_title)
		val message =
			getResString(context, R.string.scanfragment_permission_rationale_dialogbox_message)
		val action =
			getResString(context, R.string.scanfragment_permission_rationale_dialogbox_action)

		val dialogBox = MaterialAlertDialogBuilder(requireActivity())
			.setTitle(title)
			.setMessage(message)
			.setCancelable(false)
			.setPositiveButton(action) { _, _ -> onPositiveClickCallback() }
			.create()
		dialogBox.setCanceledOnTouchOutside(false)
		dialogBox.show()
	}

	//==============================================================================================
	/**
	 * Activation de la caméra
	 */
	//----------------------------------------------------------------------------------------------
	private fun startCamera() {
		Timber.v("In")

		val cameraProviderFuture = ProcessCameraProvider.getInstance(requireActivity())
		cameraExecutor = Executors.newSingleThreadExecutor()

		binding.ivTarget.visibility = View.VISIBLE

		cameraProviderFuture.addListener({
			val cameraProvider = cameraProviderFuture.get()

			Timber.d("Construction de l'affichage de la caméra")
			val preview = Preview.Builder()
				.build()
				.also {
					it.setSurfaceProvider(binding.vwPreview.surfaceProvider)
				}

			Timber.d("Construction de l'algorithme de décodage des QRCodes")
			val imageAnalyzer = ImageAnalysis.Builder()
				.setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
				.build()

			gipaQrCodeAnalyzer = GIPAQrCodeAnalyzer(
				requireActivity(),
				binding.vwPreview.width.toFloat(),
				binding.vwPreview.height.toFloat()
			) { gipaCode -> onQrcodeRead(gipaCode) }

			imageAnalyzer.setAnalyzer(cameraExecutor, gipaQrCodeAnalyzer)

			Timber.d("Sélection de la caméra de dos")
			val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

			try {
				Timber.d("Désactivation des cas d'usages associés à la caméra")
				cameraProvider.unbindAll()

				Timber.d("Affectation du cas d'usage (QRCode) à la caméra")
				cameraProvider.bindToLifecycle(
					this,
					cameraSelector,
					preview,
					imageAnalyzer
				)

			} catch (e: Exception) {
				Timber.e(e, "Exception sur activation de la caméra : ${e.message}")
			}
		}, ContextCompat.getMainExecutor(requireActivity()))
	}

	//==============================================================================================
	/**
	 * Désactivation de l'analyse de la caméra
	 */
	//----------------------------------------------------------------------------------------------
	private fun stopCamera() {
		Timber.v("In")

		binding.ivTarget.visibility = View.INVISIBLE
		cameraExecutor.shutdown()
	}

	//==============================================================================================
	/**
	 * Code GIPA trouvé : Recherche des propriétés de l'arrêt dans le repository
	 *
	 * @param gipaCode Code GIPA identifié
	 */
	//----------------------------------------------------------------------------------------------
	private fun onQrcodeRead(gipaCode: String) {
		Timber.v("In")

		stopCamera()
		binding.pbSearching.visibility = View.VISIBLE

		CoroutineScope(Dispatchers.IO).launch {
//			val arretTransporteur = arretsTransporteurRepository.getArretsTransporteurByGipa("wwww")
			val arretTransporteur = arretsTransporteurRepository.getArretsTransporteurByGipa(gipaCode)
			viewModel.firebaseRealtimeDatabaseChildUUID = UUID.randomUUID().toString()
			viewModel.stopPlace = StopPlaceUi(
				creationDate = SimpleDateFormat("yyyyMMddhhmm", Locale.FRANCE).format(Date()),
				userId = viewModel.userId ?: "?",
				gipaCode = gipaCode,
				idfmData = arretTransporteur
			)
			requireActivity().runOnUiThread {
				Timber.v("In")
				binding.pbSearching.visibility = View.INVISIBLE
				findNavController().navigate(R.id.nav_scandetail)
			}
		}
	}
}