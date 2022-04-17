package com.thesmarttoolsteam.fixbus.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import timber.log.Timber

//==================================================================================================
/**
 * Gestion du viseur du scanner de QRCode
 * @param context
 */
//--------------------------------------------------------------------------------------------------
class GIPAQrCodeBoxView (context: Context) : View(context) {

	private val paint = Paint()
	private var mRect = RectF()

	//----------------------------------------------------------------------------------------------
	/**
	 * Affichage du viseur sur la surface d'affichage
	 *
	 * @param canvas Surface d'affichage (vue)
	 */
	//----------------------------------------------------------------------------------------------
	override fun onDraw(canvas: Canvas?) {
		Timber.v("In")
		super.onDraw(canvas)

		val cornerRadius = 10f

		paint.style = Paint.Style.STROKE
		paint.color = Color.RED
		paint.strokeWidth = 5f

		canvas?.drawRoundRect(mRect, cornerRadius, cornerRadius, paint)
	}

	//----------------------------------------------------------------------------------------------
	/**
	 * Mise à jour du rectangle à afficher dans la vue
	 *
	 * @param rect
	 */
	//----------------------------------------------------------------------------------------------
	fun setRect(rect: RectF) {
		Timber.v("In")

		mRect = rect
		invalidate()        // Pour demander le rafraichissement de toute la vue ==> onDraw
		requestLayout()     // Reconstruction de la vue
	}
}