package com.thesmarttoolsteam.fixbus.scan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseReference
import com.thesmarttoolsteam.fixbus.FixBusActivityViewModel
import com.thesmarttoolsteam.fixbus.FixBusApp
import com.thesmarttoolsteam.fixbus.R
import com.thesmarttoolsteam.fixbus.common.APPFIREBASESERVICES_DEFAULT_FIREBASE_REALTIMEDATABASE_SERVICE_UNKNOWN_ERROR
import com.thesmarttoolsteam.fixbus.common.AppFragment
import com.thesmarttoolsteam.fixbus.common.database.model.ArretFixBusUi
import com.thesmarttoolsteam.fixbus.common.tools.getResString
import com.thesmarttoolsteam.fixbus.databinding.FragmentScaneditBinding
import timber.log.Timber


class ScanEditFragment : AppFragment() {

	private val viewModel: FixBusActivityViewModel by activityViewModels()
	private lateinit var binding: FragmentScaneditBinding

	//==============================================================================================
	/**
	 * Initialisation du fragment : Création de la vue
	 *
	 * @param inflater Gestionnaire de construction de la vue
	 * @param container Container de la vue
	 * @param savedInstanceState Données sauvegardées le cas échéant
	 * @return Vue créée
	 */
	//----------------------------------------------------------------------------------------------
	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		Timber.v("In")

		binding = FragmentScaneditBinding.inflate(inflater)


		return binding.root
	}

	//==============================================================================================
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		Timber.v("In")
		super.onViewCreated(view, savedInstanceState)

		setupUi()
	}

	//==============================================================================================
	/**
	 * Mise en place de l'interface
	 */
	//----------------------------------------------------------------------------------------------
	private fun setupUi() {
		Timber.v("In")

		val fixBusData: ArretFixBusUi = viewModel.stopPlace!!.fixBusData!!

		binding.tvGipa.text = getResString(
			context,
			R.string.scaneditfragment_layout_tv_gipa_text,
			viewModel.stopPlace?.gipaCode ?: "?"
		) ?: "scaneditfragment_layout_tv_gipa_text"
		binding.tvGps.text = getResString(
								context,
								R.string.scaneditfragment_layout_tv_gps_text,
								viewModel.stopPlace?.fixBusData?.stopPositionLat ?: 0f,
								viewModel.stopPlace?.fixBusData?.stopPositionLng ?: 0f,
		) ?: "scaneditfragment_layout_tv_gps_text"
		binding.etStopname.setText(viewModel.stopPlace?.fixBusData?.stopName)

		when (fixBusData.stopType) {
			"Potelet" -> binding.cStoptypePole.isChecked = true
			"Abribus" -> binding.cStoptypeBushelter.isChecked = true
			"Provisoire" -> binding.cStoptypeTemporary.isChecked = true
		}
		when (fixBusData.stopBIVType) {
			"Aucune" -> binding.cBivtypeNone.isChecked = true
			"DARC" -> binding.cBivtypeDarc.isChecked = true
			"3G" -> binding.cBivtype3g.isChecked = true
			"HNS" -> binding.cBivtypeHns.isChecked = true
		}
		when (fixBusData.stopFareZone) {
			"1" -> binding.cFarezone1.isChecked = true
			"2" -> binding.cFarezone2.isChecked = true
			"3" -> binding.cFarezone3.isChecked = true
			"4" -> binding.cFarezone4.isChecked = true
			"5" -> binding.cFarezone5.isChecked = true
			"?" -> binding.cFarezoneUnknown.isChecked = true
		}
		when (fixBusData.stopAccessibility) {
			true -> binding.cAccessibilityYes.isChecked = true
			false -> binding.cAccessibilityNo.isChecked = true
			else -> {}
		}
		when (fixBusData.stopAudibleSignals) {
			true -> binding.cAudiblesignalsYes.isChecked = true
			false -> binding.cAudiblesignalsNo.isChecked = true
			else -> {}
		}
		when (fixBusData.stopVisualSigns) {
			true -> binding.cVisualsignsYes.isChecked = true
			false -> binding.cVisualsignsNo.isChecked = true
			else -> {}
		}
		when (fixBusData.stopUSBCharger) {
			true -> binding.cUsbchargerYes.isChecked = true
			false -> binding.cUsbchargerNo.isChecked = true
			else -> {}
		}
		when (fixBusData.stopInterventionNeeded) {
			true -> binding.cInterventionneededYes.isChecked = true
			false -> binding.cInterventionneededNo.isChecked = true
			else -> {}
		}
		binding.etComments.setText(fixBusData.stopComments)

		checkStopPlaceProperties()
		setListeners()
	}

	//==============================================================================================
	/**
	 * Mise en place des listeners pour intercepter toute modification sur une donnée éditable
	 */
	//----------------------------------------------------------------------------------------------
	private fun setListeners() {
		Timber.v("In")

		binding.cgStoptype.setOnCheckedStateChangeListener { _, chipIds ->
			Timber.v("In")
			if (chipIds.size > 0) {
				val choiceView = binding.root.findViewById<Chip>(chipIds[0])
				if (choiceView != null) {
					Timber.d("Choix du type d'arrêt : ${choiceView.text}")
					viewModel.stopPlace!!.fixBusData!!.stopType = choiceView.text.toString()
				}
				checkStopPlaceProperties()
			} else {
				Timber.d("Pas de changement de sélection")
			}
		}

		binding.cgBivtype.setOnCheckedStateChangeListener { _, chipIds ->
			Timber.v("In")
			if (chipIds.size > 0) {
				val choiceView = binding.root.findViewById<Chip>(chipIds[0])
				if (choiceView != null) {
					Timber.d("Choix du type de BIV : ${choiceView.text}")
					viewModel.stopPlace!!.fixBusData!!.stopBIVType = choiceView.text.toString()
				}
				checkStopPlaceProperties()
			} else {
				Timber.d("Pas de changement de sélection")
			}
		}

		binding.cgFarezone.setOnCheckedStateChangeListener { _, chipIds ->
			Timber.v("In")
			if (chipIds.size > 0) {
				val choiceView = binding.root.findViewById<Chip>(chipIds[0])
				if (choiceView != null) {
					Timber.d("Choix de la zone : ${choiceView.text}")
					viewModel.stopPlace!!.fixBusData!!.stopFareZone = choiceView.text.toString()
				}
				checkStopPlaceProperties()
			} else {
				Timber.d("Pas de changement de sélection")
			}
		}

		binding.cgAccessibility.setOnCheckedStateChangeListener { _, chipIds ->
			Timber.v("In")
			if (chipIds.size > 0) {
				val choiceView = binding.root.findViewById<Chip>(chipIds[0])
				if (choiceView != null) {
					Timber.d("Accessibility : ${choiceView.text == "Oui"}")
					viewModel.stopPlace!!.fixBusData!!.stopAccessibility =
						(choiceView.text.toString() == "Oui")
				}
				checkStopPlaceProperties()
			} else {
				Timber.d("Pas de changement de sélection")
			}
		}

		binding.cgAudiblesignals.setOnCheckedStateChangeListener { _, chipIds ->
			Timber.v("In")
			if (chipIds.size > 0) {
				val choiceView = binding.root.findViewById<Chip>(chipIds[0])
				if (choiceView != null) {
					Timber.d("Audible Signals : ${choiceView.text == "Oui"}")
					viewModel.stopPlace!!.fixBusData!!.stopAudibleSignals =
						(choiceView.text.toString() == "Oui")
				}
				checkStopPlaceProperties()
			} else {
				Timber.d("Pas de changement de sélection")
			}
		}

		binding.cgVisualsigns.setOnCheckedStateChangeListener { _, chipIds ->
			Timber.v("In")
			if (chipIds.size > 0) {
				val choiceView = binding.root.findViewById<Chip>(chipIds[0])
				if (choiceView != null) {
					Timber.d("Visual Signs : ${choiceView.text == "Oui"}")
					viewModel.stopPlace!!.fixBusData!!.stopVisualSigns =
						(choiceView.text.toString() == "Oui")
				}
				checkStopPlaceProperties()
			} else {
				Timber.d("Pas de changement de sélection")
			}
		}

		binding.cgUsbcharger.setOnCheckedStateChangeListener { _, chipIds ->
			Timber.v("In")
			if (chipIds.size > 0) {
				val choiceView = binding.root.findViewById<Chip>(chipIds[0])
				if (choiceView != null) {
					Timber.d("Visual Signs : ${choiceView.text == "Oui"}")
					viewModel.stopPlace!!.fixBusData!!.stopUSBCharger =
						(choiceView.text.toString() == "Oui")
				}
				checkStopPlaceProperties()
			} else {
				Timber.d("Pas de changement de sélection")
			}
		}

		binding.cgInterventionneeded.setOnCheckedStateChangeListener { _, chipIds ->
			Timber.v("In")
			if (chipIds.size > 0) {
				val choiceView = binding.root.findViewById<Chip>(chipIds[0])
				if (choiceView != null) {
					Timber.d("Visual Signs : ${choiceView.text == "Oui"}")
					viewModel.stopPlace!!.fixBusData!!.stopInterventionNeeded =
						(choiceView.text.toString() == "Oui")
				}
				checkStopPlaceProperties()
			} else {
				Timber.d("Pas de changement de sélection")
			}
		}

		binding.etStopname.addTextChangedListener {
			Timber.v("In")
			viewModel.stopPlace!!.fixBusData!!.stopName = it?.toString()
			checkStopPlaceProperties()
		}

		binding.etComments.addTextChangedListener {
			Timber.v("In")
			viewModel.stopPlace!!.fixBusData!!.stopComments = it?.toString()
		}

		binding.btnCancel.setOnClickListener {
			Timber.v("In")
			Timber.d("Retour au fragment précédent")
			findNavController().navigate(R.id.nav_scandetail)
		}

		binding.btnOk.setOnClickListener {
			Timber.v("In")
			Timber.d("Sauvegarde des données")
			saveStopPlaceProperties()
		}
	}

	//==============================================================================================
	/**
	 * Validation des données
	 */
	//----------------------------------------------------------------------------------------------
	private fun checkStopPlaceProperties(): Boolean {
		Timber.v("In")

		var returnValue = true

		if (binding.etStopname.text.toString().isBlank()) {
			returnValue = false
		}

		if (binding.cgStoptype.checkedChipId == View.NO_ID) {
			Timber.d("Type d'arrêt non précisé")
			setStopPlacePropertyStatus(binding.tvStoptype, binding.cgStoptype, false)
			returnValue = false
		} else {
			setStopPlacePropertyStatus(binding.tvStoptype, binding.cgStoptype, true)
		}

		if (binding.cgBivtype.checkedChipId == View.NO_ID) {
			Timber.d("Type de BIV non précisé")
			setStopPlacePropertyStatus(binding.tvBivtype, binding.cgBivtype, false)
			returnValue = false
		} else {
			setStopPlacePropertyStatus(binding.tvBivtype, binding.cgBivtype, true)
		}

		if (binding.cgFarezone.checkedChipId == View.NO_ID) {
			Timber.d("Zone tarifaire non précisée")
			setStopPlacePropertyStatus(binding.tvFarezone, binding.cgFarezone, false)
			returnValue = false
		} else {
			setStopPlacePropertyStatus(binding.tvFarezone, binding.cgFarezone, true)
		}

		if (binding.cgAccessibility.checkedChipId == View.NO_ID) {
			Timber.d("Accessibilité non précisée")
			setStopPlacePropertyStatus(binding.tvAccessibility, binding.cgAccessibility, false)
			returnValue = false
		} else {
			setStopPlacePropertyStatus(binding.tvAccessibility, binding.cgAccessibility, true)
		}

		if (binding.cgAudiblesignals.checkedChipId == View.NO_ID) {
			Timber.d("Présence d'information sonore non précisée")
			setStopPlacePropertyStatus(binding.tvAudiblesignals, binding.cgAudiblesignals, false)
			returnValue = false
		} else {
			setStopPlacePropertyStatus(binding.tvAudiblesignals, binding.cgAudiblesignals, true)
		}

		if (binding.cgVisualsigns.checkedChipId == View.NO_ID) {
			Timber.d("Présence d'information visuelle dynamique non précisée")
			setStopPlacePropertyStatus(binding.tvVisualsigns, binding.cgVisualsigns, false)
			returnValue = false
		} else {
			setStopPlacePropertyStatus(binding.tvVisualsigns, binding.cgVisualsigns, true)
		}

		if (binding.cgUsbcharger.checkedChipId == View.NO_ID) {
			Timber.d("Présence de chargeur USB non précisée")
			setStopPlacePropertyStatus(binding.tvUsbcharger, binding.cgUsbcharger, false)
			returnValue = false
		} else {
			setStopPlacePropertyStatus(binding.tvUsbcharger, binding.cgUsbcharger, true)
		}

		if (binding.cgInterventionneeded.checkedChipId == View.NO_ID) {
			Timber.d("Nécessité d'intervention non précisée")
			setStopPlacePropertyStatus(binding.tvInterventionneeded, binding.cgInterventionneeded, false)
			returnValue = false
		} else {
			setStopPlacePropertyStatus(binding.tvInterventionneeded, binding.cgInterventionneeded, true)
		}

		binding.btnOk.isEnabled = returnValue

		return returnValue
	}

	//==============================================================================================
	/**
	 * Gestion de la couleur des champs en fonction de la validité de la propriété (false = invalid)
	 */
	//----------------------------------------------------------------------------------------------
	private fun setStopPlacePropertyStatus(
		textView: TextView,
		chipGroup: ChipGroup,
		isValid: Boolean) {
		Timber.v("In")

		val defaultChipStrokeColor = binding.cTemplate.chipStrokeColor
		val defaultTextColor = binding.tvTemplate.currentTextColor

		if (!isValid) {
			textView.setTextColor(resources.getColor(R.color.formDataMissing, null))
			chipGroup.forEach {
				(it as Chip).setChipStrokeColorResource(R.color.formDataMissing)
			}
		} else {
			textView.setTextColor(defaultTextColor)
			chipGroup.forEach {
				(it as Chip).chipStrokeColor = defaultChipStrokeColor
			}
		}
	}

	//==============================================================================================
	/**
	 * Sauvegarde des données
	 * La valeur du noeud est conservée : Les enregistrements s'écrasent tant qu'on n'a pas rescanné
	 * un code barre
	 */
	//----------------------------------------------------------------------------------------------
	private fun saveStopPlaceProperties() {
		Timber.v("In")


		val realtimeDatabase = FixBusApp.appFirebaseServices.getFirebaseRealtimeDatabase()
		val reference = realtimeDatabase?.reference

		val completionListener = DatabaseReference.CompletionListener { error, _ ->
			binding.pbSaving.visibility = View.INVISIBLE
			enableChildViews(true)
			if (error == null) {
				Timber.d("Sauvegarde OK")
				showDatabaseSuccessSnackbar()
				findNavController().navigate(R.id.nav_scan)
			} else {
				Timber.e("Result : ${error.message}")
				showDatabaseFailureDialogBox(error.message)
			}
		}

		try {
			reference
				?.child(
					getResString(
						requireContext(),
						R.string.gradle_configFirebaseCollectedDataChildName
					) ?: "gradle_configFirebaseCollectedDataChildName"
				)
				?.child(
					getResString(
						requireContext(),
						R.string.gradle_configFirebaseRealtimeBranch
					) ?: "gradle_configFirebaseRealtimeBranch"
				)
				?.child(viewModel.firebaseRealtimeDatabaseChildUUID!!)
				?.setValue(viewModel.stopPlace, completionListener)
		} catch (error: Exception) {
			Timber.e(error, "Exception : ${error.message}")
			showDatabaseFailureDialogBox(
				error.message
					?: APPFIREBASESERVICES_DEFAULT_FIREBASE_REALTIMEDATABASE_SERVICE_UNKNOWN_ERROR
			)
			binding.pbSaving.visibility = View.INVISIBLE
			enableChildViews(true)
		}

		binding.pbSaving.visibility = View.VISIBLE
		enableChildViews(false)

	}

	//==============================================================================================
	/**
	 * Affichage d'un snackbar sur résultat OK de la sauvegarde
	 */
	//----------------------------------------------------------------------------------------------
	private fun showDatabaseSuccessSnackbar() {
		Timber.v("In")

		Snackbar.make(
			binding.root,
			getResString(
				requireContext(),
				R.string.scaneditfragment_databasesuccesssnackbar_message
			) ?: "scaneditfragment_databasesuccesssnackbar_message",
			Snackbar.LENGTH_LONG
		)
			.setAnchorView(binding.vwBottom)
			.show()
	}

	//==============================================================================================
	/**
	 * Affichage d'un snackbar sur résultat KO de la sauvegarde
	 */
	//----------------------------------------------------------------------------------------------
	private fun showDatabaseFailureDialogBox(error: String) {
		Timber.v("In")

		MaterialAlertDialogBuilder(requireContext())
			.setTitle(
				getResString(requireContext(),
				R.string.scaneditfragment_databaseerrordialogbox_title)
			)
			.setMessage(getResString(
					requireContext(),
					R.string.scaneditfragment_databaseerrordialogbox_message,
					error
			))
			.setCancelable(false)
			.setPositiveButton(getResString(requireContext(), R.string.scaneditfragment_databaseerrordialogbox_ok)) { dialog, _ ->
				Timber.v("In")
				dialog.dismiss()
			}
			.show()
	}

	//==============================================================================================
	/**
	 *  Changer le mode de tous les éléments du formulaire (hors bouton "Précédent")
	 */
	//==============================================================================================
	private fun enableChildViews(enable: Boolean) {
		Timber.v("In")

		binding.cgStoptype.children.iterator().forEach {
			it.isClickable = enable
		}
		binding.cgStoptype.children.iterator().forEach {
			it.isClickable = enable
		}
		binding.cgInterventionneeded.children.iterator().forEach {
			it.isClickable = enable
		}
		binding.cgFarezone.children.iterator().forEach {
			it.isClickable = enable
		}
		binding.cgAudiblesignals.children.iterator().forEach {
			it.isClickable = enable
		}
		binding.cgAccessibility.children.iterator().forEach {
			it.isClickable = enable
		}
		binding.cgBivtype.children.iterator().forEach {
			it.isClickable = enable
		}
		binding.cgUsbcharger.children.iterator().forEach {
			it.isClickable = enable
		}
		binding.cgVisualsigns.children.iterator().forEach {
			it.isClickable = enable
		}
		binding.etStopname.isEnabled = enable
		binding.etComments.isEnabled = enable
		binding.btnOk.isEnabled = enable
	}
}
