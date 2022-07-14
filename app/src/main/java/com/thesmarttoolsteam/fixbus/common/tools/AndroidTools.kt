package com.thesmarttoolsteam.fixbus.common.tools

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresPermission
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.thesmarttoolsteam.fixbus.common.VIBRATION_DEFAULT_DURATION
import timber.log.Timber

//==================================================================================================
/**
 * Vérification que le téléphone dispose bien des GooglePlayServices
 */
//--------------------------------------------------------------------------------------------------
fun areGooglePlayServicesEnabled(context: Context): Boolean {
    Timber.v("In")
    val status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
    return if (status != ConnectionResult.SUCCESS) {
        Timber.w("Google Play Services non accessibles")
        false
    } else {
        Timber.w("Google Play Services accessibles")
        true
    }
}

//==================================================================================================
/**
 * Déclencher une vibration du téléphone
 *
 * @param context Contexte
 * @param duration Durée en millisecondes (par défaut VIBRATION_DEFAULT_DURATION)
 */
//--------------------------------------------------------------------------------------------------
@RequiresPermission(android.Manifest.permission.VIBRATE)
fun vibrate(context: Context, duration: Long = VIBRATION_DEFAULT_DURATION) {
    Timber.v("In")

    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(
            Context.VIBRATOR_MANAGER_SERVICE
        ) as VibratorManager
                ).defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(
            VibrationEffect.createOneShot(
                duration,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
        )
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(duration)
    }
}

//==================================================================================================
/**
 * Vérifie sir la connexion internet est disponible
 */
//--------------------------------------------------------------------------------------------------
@SuppressLint("MissingPermission")
fun isNetworkAvailable(context: Context): Boolean {

    Timber.v("In")

    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
            as ConnectivityManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

        if (capabilities != null) {
            if (
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            ) {
                Timber.d("Connexion internet OK")
                return true
            }
        }
    } else
        @Suppress("DEPRECATION") {
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
            Timber.d("Connexion internet OK")
            return true
        }
    }
    Timber.d("Connexion internet KO")
    return false
}

//==================================================================================================
/**
 * Vérifie si la localisation est activée
 */
//--------------------------------------------------------------------------------------------------
fun isLocationAvailable(context: Context): Boolean {
    Timber.v("In")

    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val isLocationAvailable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    Timber.d("Location activé : $isLocationAvailable")

    return isLocationAvailable
}