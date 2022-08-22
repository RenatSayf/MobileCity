package com.alfasreda.mobilecity.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.alfasreda.mobilecity.R
import com.alfasreda.mobilecity.databinding.FragmentMenuBinding
import com.alfasreda.mobilecity.ui.dialogs.AboutDialog
import com.alfasreda.mobilecity.ui.main.SpeechViewModel
import com.alfasreda.mobilecity.utils.setUpToolBar
import com.alfasreda.mobilecity.utils.showSnackBar

@Suppress("ObjectLiteralToLambda")
class MenuFragment : Fragment() {

    private lateinit var binding: FragmentMenuBinding

    private val speechVM: SpeechViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {

            setUpToolBar(
                binding = includeAppBar,
                navIconResource = R.drawable.ic_arrow_back_white,
                iconContentDescription = "Назад на главный экран",
                title = "Меню",
                titleContentDescription = "Это главное меню приложения"
            )

            includeAppBar.btnBackNavigation.setOnClickListener {
                findNavController().popBackStack()
            }

            btnBack.setOnClickListener {
                findNavController().popBackStack()
            }

            btnSettings.setOnClickListener {
                showSnackBar("Раздел находится в разработке")
            }

            btnFilter.setOnClickListener {
                showSnackBar("Раздел находится в разработке")
            }

            btnAbout.setOnClickListener {
                AboutDialog().show(requireActivity().supportFragmentManager, AboutDialog.TAG)
            }

            btnExit.setOnClickListener {
                requireActivity().finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        with(binding) {

            includeAppBar.root.forEach { view ->
                view.setOnLongClickListener {
                    val description = it.contentDescription
                    if (!description.isNullOrEmpty()) {
                        speechVM.speak(description.toString())
                    }
                    true
                }
            }
            contentLayout.forEach { view ->
                view.setOnLongClickListener {
                    val description = it.contentDescription
                    if (!description.isNullOrEmpty()) {
                        speechVM.speak(description.toString())
                    }
                    true
                }
            }
        }
    }

}