package com.thesmarttoolsteam.fixbus

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarItemView
import com.google.android.material.navigation.NavigationBarView
import com.thesmarttoolsteam.fixbus.common.FragmentEnum
import com.thesmarttoolsteam.fixbus.common.tools.getResString
import com.thesmarttoolsteam.fixbus.common.services.AppPreferences
import com.thesmarttoolsteam.fixbus.databinding.ActivityFixbusBinding
import timber.log.Timber

//==================================================================================================
/**
 * Activité pricipale de l'application FixBus
 */
//--------------------------------------------------------------------------------------------------
class FixBusActivity : AppCompatActivity(),  NavigationBarView.OnItemSelectedListener {

	private val viewModel: FixBusActivityViewModel by viewModels() // Création simplifiée
	private lateinit var binding: ActivityFixbusBinding
	private var currentFragment: FragmentEnum = FragmentEnum.ScanFragment
	private lateinit var navController: NavController

	//==============================================================================================
	override fun onCreate(savedInstanceState: Bundle?) {
		Timber.v("In")
		super.onCreate(savedInstanceState)

		(application as? FixBusApp)?.initRequiredServices {
			Timber.d("Fin de l'initialisation des services obligatoires")
			Timber.d("Lancement des services complémentaires")
			(application as? FixBusApp)?.initAdditionalServices()
			AppPreferences.launchesCount++
		}

		binding = ActivityFixbusBinding.inflate(layoutInflater)
		setContentView(binding.root)

		viewModel.userId = "DP747541" // Pour supprimer le warning "not used"
		setActivityListeners()
		setNavigation()
	}

	//==============================================================================================
	/**
	 * Mise en place des listeners globaux à l'activité
	 */
	//----------------------------------------------------------------------------------------------
	private fun setActivityListeners() {
		Timber.v("In")

		setOnFragmentResultListener()
	}

	//==============================================================================================
	/**
	 * Création de la navigation
	 */
	//----------------------------------------------------------------------------------------------
	private fun setNavigation() {
		Timber.v("In")

		Timber.d("Construction du controleur de navigation")
		navController  = (
				this@FixBusActivity
					.supportFragmentManager
					.findFragmentById(R.id.nav_hostfragment) as NavHostFragment
				).navController
		this.binding.navMenu.setupWithNavController(navController)
		binding.navMenu.setOnItemSelectedListener(this)
	}

	//==============================================================================================
	/**
	 * Gestion manuelle de la navigation via la bottombar
	 * Cela ne fonctionne pas correctement sans cette méthode
	 */
	//----------------------------------------------------------------------------------------------
	override fun onNavigationItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.nav_scan -> {
				Timber.d("Retour au fragment de scan")
				navController.navigate(R.id.nav_scan)
			}
			R.id.nav_search -> {
				Timber.d("Retour au fragment de recherche")
				navController.navigate(R.id.nav_search)
			}
			else -> {
				Timber.w("Appel d'un item de navigation non paramétré : ${item.title}")
				return false
			}
		}
		return true
	}

	//==============================================================================================
	/**
	 * Listener sur les données envoyées par les fragments
	 */
	//----------------------------------------------------------------------------------------------
	private fun setOnFragmentResultListener() {
		Timber.v("In")

		Timber.d("Mise en place du listener d'écoute du fragment courant")
		getResString(this, R.string.fragment_result_key)?.let { key ->
			supportFragmentManager
				.setFragmentResultListener(key, this) { _, bundle ->
					onFragmentResultListener(bundle)
				}
		}
	}

	//==============================================================================================
	/**
	 * Listener sur les données envoyées par les fragments via l'argument [bundle]
	 */
	//----------------------------------------------------------------------------------------------
	private fun onFragmentResultListener(bundle: Bundle) {
		Timber.v("In")

		when {
			bundle.containsKey(getResString(this, R.string.currentFragmentEnum_key)) -> {
				Timber.d("Paramètre transmis : Fragment courant")
				onChangeFragmentListener(bundle)
			}

			else -> {
				Timber.w("Paramètre non géré")
			}
		}
	}

	//==============================================================================================
	/**
	 * Listener sur le changement de fragment passé via
	 * l'argument [Bundle] (clé currentFragmentEnum_key)
	 */
	//----------------------------------------------------------------------------------------------
	private fun onChangeFragmentListener(bundle: Bundle) {
		Timber.v("In")
		Timber.d("Changement du fragment depuis $currentFragment")

		currentFragment = bundle.get(
			getResString(this, R.string.currentFragmentEnum_key)
		) as FragmentEnum

		Timber.d("vers $currentFragment")
	}
}