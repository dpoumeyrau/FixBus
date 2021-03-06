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
	 * @param view Vue cr????e
	 * @param savedInstanceState Donn??es sauvegard??es
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
	 * Destruction du fragment : Fin du thread de la cam??ra
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
	 * Initialisation de l'interface et lancement de la cam??ra apr??s contr??le des permissions
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
				 * Affichage des explications compl??mentaires
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
				 * La permission a ??t?? accept??e, on d??clenche le traitement
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
	 * Affichage de la bo??te de dialogue indiquant que la permission n??cessaire n'a pas ??t?? donn??e
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
	 * Affichage de la bo??te de dialogue d'explications compl??mentaires ("Rationale")
	 * pour pr??ciser pourquoi les droits sont n??cessaires
	 *
	 * @param onPositiveClickCallback M??thode ?? appeler sur le bouton "OK" (demande d'autorisation)
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
	 * Activation de la cam??ra
	 */
	//----------------------------------------------------------------------------------------------
	private fun startCamera() {
		Timber.v("In")

		val cameraProviderFuture = ProcessCameraProvider.getInstance(requireActivity())
		cameraExecutor = Executors.newSingleThreadExecutor()

		binding.ivTarget.visibility = View.VISIBLE

		cameraProviderFuture.addListener({
			val cameraProvider = cameraProviderFuture.get()

			Timber.d("Construction de l'affichage de la cam??ra")
			val preview = Preview.Builder()
				.build()
				.also {
					it.setSurfaceProvider(binding.vwPreview.surfaceProvider)
				}

			Timber.d("Construction de l'algorithme de d??codage des QRCodes")
			val imageAnalyzer = ImageAnalysis.Builder()
				.setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
				.build()

			gipaQrCodeAnalyzer = GIPAQrCodeAnalyzer(
				requireActivity(),
				binding.vwPreview.width.toFloat(),
				binding.vwPreview.height.toFloat()
			) { gipaCode -> onQrcodeRead(gipaCode) }

			imageAnalyzer.setAnalyzer(cameraExecutor, gipaQrCodeAnalyzer)

			Timber.d("S??lection de la cam??ra de dos")
			val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

			try {
				Timber.d("D??sactivation des cas d'usages associ??s ?? la cam??ra")
				cameraProvider.unbindAll()

				Timber.d("Affectation du cas d'usage (QRCode) ?? la cam??ra")
				cameraProvider.bindToLifecycle(
					this,
					cameraSelector,
					preview,
					imageAnalyzer
				)

			} catch (e: Exception) {
				Timber.e(e, "Exception sur activation de la cam??ra : ${e.message}")
			}
		}, ContextCompat.getMainExecutor(requireActivity()))
	}

	//==============================================================================================
	/**
	 * D??sactivation de l'analyse de la cam??ra
	 */
	//----------------------------------------------------------------------------------------------
	private fun stopCamera() {
		Timber.v("In")

		binding.ivTarget.visibility = View.INVISIBLE
		cameraExecutor.shutdown()
	}

	//==============================================================================================
	/**
	 * Code GIPA trouv?? : Recherche des propri??t??s de l'arr??t dans le repository
	 *
	 * @param gipaCode Code GIPA identifi??
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