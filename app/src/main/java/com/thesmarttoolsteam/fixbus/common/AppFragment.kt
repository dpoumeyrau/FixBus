package com.thesmarttoolsteam.fixbus.common

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.thesmarttoolsteam.fixbus.FixBusApp
import com.thesmarttoolsteam.fixbus.R
import timber.log.Timber


//==================================================================================================
/**
 * Fragment par défaut de l'application.
 * Le fragment intègre les traitements par défaut des fragments (Firebase Analytics, ...)
 */
//--------------------------------------------------------------------------------------------------
open class AppFragment : Fragment() {

	//==============================================================================================
	/**
	 * Création de la vue
	 *
	 * @param view Vue du fragment
	 * @param savedInstanceState Données sauvegardées
	 */
	//----------------------------------------------------------------------------------------------
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		Timber.v("In")
		super.onViewCreated(view, savedInstanceState)

		Timber.d("Traçage Firebase Analytics")
		FixBusApp.appFirebaseServices.trackScreenView(
			"${this::class.simpleName}.onCreateView()",
			this::class.simpleName,
			this::class.qualifiedName
		)

		requireActivity().findViewById<BottomNavigationView>(R.id.nav_menu)?.visibility =
			View.VISIBLE
	}

	//==============================================================================================
	/**
	 * Masquage de la flèche de navigation et transmission du fragment courant à l'activité
	 */
	//----------------------------------------------------------------------------------------------
	override fun onResume() {
		Timber.v("In")
		super.onResume()

		Timber.d("Masquage de la flèche de navigation")
		(activity as AppCompatActivity?)!!.supportActionBar?.setDisplayHomeAsUpEnabled(false)

		val enumValues = FragmentEnum.values()
		val enumValue = enumValues.firstOrNull {
			it.name == this::class.simpleName
		}

		if (enumValue != null) {
			Timber.i("Fragment : $enumValue")
			sendFragmentEnumToActivity(enumValue)
		} else {
			Timber.w("Fragment non trouvé : ${this::class.simpleName}")
		}
	}
}