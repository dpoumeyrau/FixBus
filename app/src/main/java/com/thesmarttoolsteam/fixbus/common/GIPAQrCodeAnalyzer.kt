package com.thesmarttoolsteam.fixbus.common

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.thesmarttoolsteam.fixbus.R
import com.thesmarttoolsteam.fixbus.common.tools.vibrate
import timber.log.Timber

//==================================================================================================
/**
 * Classe d'analyse de l'image (pour identifier un QRCode)
 *
 * @property context Contexte
 * @property previewViewWidth Largeur du composant de prévisualisation dans le layout
 * @property previewViewHeight Hauteur du composant de prévisualisation dans le layout
 * @property onQrcodeReadCallback Méthode callback à appeler lorsq'un QRCode est trouvé
 */
//--------------------------------------------------------------------------------------------------
class GIPAQrCodeAnalyzer(
	private val context: Context,
	private val previewViewWidth: Float,
	private val previewViewHeight: Float,
	private val onQrcodeReadCallback: (Int)->Unit) : ImageAnalysis.Analyzer {

	// Mise à l'échelle de la box dans le composant de visualisation
	private var scaleX = 1f
	private var scaleY = 1f

	//==============================================================================================
	/**
	 * Analyse du contenu de l'image pour identification du QRCode
	 *
	 * @param image Image de la caméra
	 */
	//----------------------------------------------------------------------------------------------
	@SuppressLint("MissingPermission")
	@ExperimentalGetImage
	override fun analyze(image: ImageProxy) {
		Timber.v("In")

		val img = image.image
		if (img != null) {
			// Mise à l'échelle
			scaleX = previewViewWidth / img.height.toFloat()
			scaleY = previewViewHeight / img.width.toFloat()

			// Redressement de l'image
			val inputImage = InputImage.fromMediaImage(img, image.imageInfo.rotationDegrees)

			// Recherche du QRCode
			val scanner = BarcodeScanning.getClient(BarcodeScannerOptions.Builder().build())
			scanner.process(inputImage)
				.addOnSuccessListener { barcodes ->
					if (barcodes.isNotEmpty()) {
						Timber.v("QRCodes détectés : ${barcodes.size}")
						// On parcourt les qrcodes et on prend le premier qui correspond à la
						// structure attendue : https://tagiv.ratp.fr/bus/<gipa>
						for (barcode in barcodes) {
							// Traitement
							Timber.d("QRCode : ${barcode.rawValue}")

							val gipaCode = checkQRCodeURL(barcode.rawValue ?: "")
							if (gipaCode != null) {
								Timber.v("QRCode confirme trouvé")
								vibrate(context)
								onQrcodeReadCallback(gipaCode)
							}
						}
					}

				}
				.addOnFailureListener {
					Timber.w(it, "Exception détectée : ${it.message}")
				}
				.addOnCompleteListener {
					Timber.d("Fermeture de l'iamge")
					image.close()
				}
//		    image.close()
		} else {
			Timber.w("Image nulle")
		}
	}

	//==============================================================================================
	/**
	 * Contrôle de validité du QRCode
	 *
	 * @param url Contenu du QRCode
	 * @return Code GIPA si trouvé ou null sinon
	 */
	//----------------------------------------------------------------------------------------------
	private fun checkQRCodeURL(url: String): Int? {
		Timber.v("In")

		Timber.d("Contrôle de l'url : $url")
		val qrcodeUrlTemplate = getResString(context, R.string.gradle_qrcodeUrlTemplate)
			?: "gradle_qrcodeUrlTemplate"
		val uri = Uri.parse(url)
		var gipaCode: Int? = null

		if ((!url.startsWith(qrcodeUrlTemplate)) || (uri.pathSegments.size != 2)) {
			Timber.w("Mauvaise structure d'URL")
			return null
		}

		try {
			gipaCode = Integer.parseInt(uri.lastPathSegment!!)
			Timber.d("Code GIPA : $gipaCode")
		} catch (e: Exception) {
			Timber.e(e, "Exception : ${e.message}")
		}

		return gipaCode
	}
}
