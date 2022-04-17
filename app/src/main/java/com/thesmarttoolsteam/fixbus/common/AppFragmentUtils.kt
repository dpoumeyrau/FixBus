package com.thesmarttoolsteam.fixbus.common

import android.os.Bundle
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.thesmarttoolsteam.fixbus.R
import timber.log.Timber
import java.io.Serializable

//==================================================================================================
/**
 * Type des fragments (pour détermination du fragment courant)
 */
//--------------------------------------------------------------------------------------------------
enum class FragmentEnum {
	None,
	ScanFragment,
	ScanDetailFragment,
	SearchFragment,
}

//==================================================================================================
/**
 * Type des bottomsheets
 */
//--------------------------------------------------------------------------------------------------
enum class ModalBottomSheetEnum {}

//==================================================================================================
/**
 * Transmission du fragment affiché à l'activité (pour mise à jour de la navigation)
 *
 * @param fragmentEnum
 *///-----------------------------------------------------------------------------------------------
fun Fragment.sendFragmentEnumToActivity(fragmentEnum: FragmentEnum) {

	Timber.v("In")
	Timber.i("Fragment : $fragmentEnum")

	sendFragmentDataToActivity(R.string.currentFragmentEnum_key, fragmentEnum)
}

//==================================================================================================
/**
 * Transmission du bottomsheet modal à ouvrir depuis l'activité, à l'activité
 *
 * @param modalBottomSheetId BottomSheet à ouvrir
 */
//--------------------------------------------------------------------------------------------------
fun Fragment.sendModalBottomSheetEnumToActivity(modalBottomSheetId: ModalBottomSheetEnum) {
	Timber.v("In")
	Timber.i("bottomSheetId : $modalBottomSheetId")

	sendFragmentDataToActivity(R.string.modalBottomSheetEnum_key, modalBottomSheetId)
}

//==================================================================================================
/**
 * Envoi d'une donnée d'un fragment vers l'activité
 *
 * @param keyId Clé de la valeur transmise
 * @param data Valeur transmise
 */
//--------------------------------------------------------------------------------------------------
fun <T>Fragment.sendFragmentDataToActivity(@StringRes keyId: Int, data: T) {
	Timber.v("In")
	Timber.i("Key : $keyId / Data : $data")

	getResString(context, R.string.fragment_result_key)?. let { key ->
		requireActivity()
			.supportFragmentManager
			.setFragmentResult(
				key,
				Bundle().apply{
					when (data) {

						is Int -> {
							putInt(
								getResString(context, keyId),
								data
							)
						}

						is String -> {
							putString(
								getResString(context, keyId),
								data
							)
						}

						else -> {
							putSerializable(
								getResString(context, keyId),
								data as Serializable
							)
						}
					}
				}
			)
	}
}