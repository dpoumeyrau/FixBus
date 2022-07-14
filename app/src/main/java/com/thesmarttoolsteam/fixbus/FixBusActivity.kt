package com.thesmarttoolsteam.fixbus

import android.app.Activity
import android.content.DialogInterface
import android.os.Bundle
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationBarView
import com.thesmarttoolsteam.fixbus.common.FragmentEnum
import com.thesmarttoolsteam.fixbus.common.USER_PERSONALNUMBER_PATTERN
import com.thesmarttoolsteam.fixbus.common.database.model.ArretTransporteurUi
import com.thesmarttoolsteam.fixbus.common.services.AppPreferences
import com.thesmarttoolsteam.fixbus.common.tools.getResString
import com.thesmarttoolsteam.fixbus.databinding.ActivityFixbusBinding
import com.thesmarttoolsteam.fixbus.databinding.DialogUserpersonalnumberBinding
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

			Timber.d("Vérification de la version du fichier de données IDFM")
			ArretTransporteurUi.getLastIDFMDataFilename (this) { idfmStoragePath ->
				Timber.d("*** Dernière version du fichier : $idfmStoragePath")
				if (idfmStoragePath != null
					&& (AppPreferences.idfmStoreFilePath.trim() != idfmStoragePath)) {
					Timber.d("Mise à jour nécessaire !")
					ArretTransporteurUi.loadIDFMDataFile(idfmStoragePath)
				} else {
					Timber.d("Pas de mise à jour à appliquer !")
				}
			}

			AppPreferences.launchesCount++
		}

		if (viewModel.userId == null) {
			Timber.d("Matricule non renseigné : Affichage de la boite de dialogue")
			askForUserPersonalNumber()
		} else {
			Timber.d("Matricule déjà renseigné")
			showActivity()
		}
	}

	//==============================================================================================
	/**
	 * Affichage de l'activité, une fois que le matricule est correctement renseigné
	 */
	//----------------------------------------------------------------------------------------------
	private fun showActivity() {
		Timber.v("In")

		binding = ActivityFixbusBinding.inflate(layoutInflater)
		setContentView(binding.root)
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
	 * Gérer le bouton "Retour arrière
	 */
	//----------------------------------------------------------------------------------------------
	override fun onBackPressed() {
		Timber.v("In")

		Timber.d("Current Fragment : $currentFragment")
		when (currentFragment) {
			FragmentEnum.ScanFragment -> {
				Timber.d("Demande de confirmation de sortie de l'application")
				finish()
			}
			else -> {
				super.onBackPressed()
			}
		}
	}




	//==============================================================================================
	/**
	 * Récupérer le matricule de l'utilisateur
	 */
	//----------------------------------------------------------------------------------------------
	private fun askForUserPersonalNumber() {
		val personalNumberBinding =  DialogUserpersonalnumberBinding.inflate(layoutInflater)

		val personalNumberDialogBoxBuilder = MaterialAlertDialogBuilder(this)
			.setTitle(
				getResString(this,
					R.string.fixbuxactivity_personalnumber_title)
			)
			.setMessage(getResString(
				this,
				R.string.fixbuxactivity_personalnumber_message
			))
			.setCancelable(false)
			.setPositiveButton(
				getResString(this, R.string.fixbuxactivity_personalnumber_action_ok),
				null
			)
			.setNegativeButton(
				getResString(this, R.string.fixbuxactivity_personalnumber_action_cancel)
			) { _, _ -> Timber.v("In")
				this.finish()
			}

		personalNumberBinding.etPersonalnumber.setText(AppPreferences.userPersonalNumber)
		personalNumberBinding.etPersonalnumber.requestFocus()

		personalNumberBinding.etPersonalnumber.addTextChangedListener {
			personalNumberBinding.tilPersonalnumber.error = null
		}

		personalNumberDialogBoxBuilder.setView(personalNumberBinding.root)
		val dialog = personalNumberDialogBoxBuilder.show()

		val positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
		positiveButton.setOnClickListener {
			Timber.v("In")

			if (isValidPersonalNumber(personalNumberBinding.etPersonalnumber.text.toString())) {
				Timber.d("Matricule correct : ${personalNumberBinding.etPersonalnumber.text}")
				AppPreferences.userPersonalNumber = personalNumberBinding.etPersonalnumber.text.toString()
				viewModel.userId = personalNumberBinding.etPersonalnumber.text.toString()

				// Fermeture du clavier
				val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
				inputMethodManager.hideSoftInputFromWindow(personalNumberBinding.root.windowToken, 0)

				// Affichage de l'activité et fermeture de la boite de dialogue
				showActivity()
				dialog.dismiss()
			} else {
				Timber.d("Matricule incorrect [${personalNumberBinding.etPersonalnumber.text}]")
				personalNumberBinding.tilPersonalnumber.error = getResString(
					this,
					R.string.fixbuxactivity_personalnumber_error
				)
			}
		}
	}

	//==============================================================================================
	/**
	 * Contrôle de la conformité du matricule de l'utilisateur : X999999 ou XX999999
	 */
	//----------------------------------------------------------------------------------------------
	private fun isValidPersonalNumber(personalNumber: String?): Boolean {
		Timber.v("In")

		val regEx = Regex(USER_PERSONALNUMBER_PATTERN)
		val isValid = regEx.matchEntire(personalNumber as CharSequence) != null
		Timber.d("Conformité : [$personalNumber] = $isValid")
		return isValid
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