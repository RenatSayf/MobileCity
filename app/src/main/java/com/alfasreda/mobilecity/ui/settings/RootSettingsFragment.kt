package com.alfasreda.mobilecity.ui.settings

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.alfasreda.mobilecity.R
import com.alfasreda.mobilecity.databinding.FragmentRootSettingsBinding
import com.alfasreda.mobilecity.databinding.ToolBarBinding
import com.alfasreda.mobilecity.utils.setUpToolBar

class RootSettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentRootSettingsBinding.inflate(inflater, container, false)
        setUpToolBar(
            binding = binding.includeAppBar,
            navIconResource = R.drawable.ic_arrow_back_white,
            iconContentDescription = "Назад в главное меню",
            title = "Настройки",
            titleContentDescription = "Это экран настроек приложения"
        )

        binding.includeAppBar.btnBackNavigation.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        return binding.root
    }

}