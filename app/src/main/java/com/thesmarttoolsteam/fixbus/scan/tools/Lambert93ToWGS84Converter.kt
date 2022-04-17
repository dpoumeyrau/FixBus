package com.thesmarttoolsteam.fixbus.scan.tools

import com.google.android.gms.maps.model.LatLng
import kotlin.math.*

// https://gis.stackexchange.com/questions/70089/lambert93-to-wgs84-ign-algorithms-longitude-problem

object Lambert93ToWGS84Converter {

	private const val M_PI_2 = Math.PI / 2.0
	private const val DEFAULT_EPS = 1e-10
	private const val E_WGS84 = 0.08181919106
	private const val E2 = E_WGS84 / 2.0
	private const val LON_MERID_IERS = 3.0 * Math.PI / 180.0
	private const val N = 0.7256077650
	private const val C = 11754255.426
	private const val XS = 700000.000
	private const val YS = 12655612.050

	private fun latitudeFromLatitudeISO(latISo: Double): Double {
		var phi0 = 2 * atan(exp(latISo)) - M_PI_2
		var phiI = (2
				* atan(
			((1 + E_WGS84 * sin(phi0)) /
					(1 - E_WGS84 * sin(phi0))).pow(E2) * exp(latISo)
		)
				- M_PI_2)
		var delta = abs(phiI - phi0)
		while (delta > DEFAULT_EPS) {
			phi0 = phiI
			phiI = 2 * atan(
				((1 + E_WGS84 * sin(phi0)) /
						(1 - E_WGS84 * sin(phi0))).pow(E2)
						* exp(latISo)
			) - M_PI_2
			delta = abs(phiI - phi0)
		}
		return phiI
	}

	fun toLatLng(x: Double?, y: Double?): LatLng? {

		if ((x == null) || (y == null)) return null

		val dX = x - XS
		val dY = y - YS
		val r = sqrt(dX * dX + dY * dY)
		val gamma = atan(dX / -dY)
		val latIso = -1 / N * ln(abs(r / C))

		return LatLng(
			Math.toDegrees(latitudeFromLatitudeISO(latIso)),
			Math.toDegrees(LON_MERID_IERS + gamma / N)
		)
	}
}