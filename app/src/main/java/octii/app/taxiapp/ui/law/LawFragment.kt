/******************************************************************************
 * Copyright (c) 2021. Created by iooojik.                                    *
 * Telegram: @iooojik                                                         *
 * Email: sbobrov760@gmail.com                                                *
 * All rights reserved. Last modified 23.08.2021, 22:41                       *
 ******************************************************************************/

package octii.app.taxiapp.ui.law

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import octii.app.taxiapp.databinding.FragmentLawBinding

class LawFragment : Fragment() {
	
	lateinit var binding: FragmentLawBinding
	
	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		binding = FragmentLawBinding.inflate(layoutInflater)
		setInformation()
		return binding.root
	}
	
	private fun setInformation(){
		binding.webView.loadUrl("https://iooojik.ru/taxi/privacy")
	}
}