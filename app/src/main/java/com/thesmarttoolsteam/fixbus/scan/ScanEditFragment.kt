package com.thesmarttoolsteam.fixbus.scan

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.thesmarttoolsteam.fixbus.FixBusActivity
import com.thesmarttoolsteam.fixbus.FixBusActivityViewModel
import com.thesmarttoolsteam.fixbus.R
import com.thesmarttoolsteam.fixbus.common.AppFragment
import com.thesmarttoolsteam.fixbus.common.AppPermissionManager
import com.thesmarttoolsteam.fixbus.common.getResString
import com.thesmarttoolsteam.fixbus.databinding.FragmentScandetailBinding
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

	}
}