package com.alfasreda.mobilecity.ui.menu

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.alfasreda.mobilecity.R
import com.alfasreda.mobilecity.databinding.FragmentMenuBinding

@Suppress("ObjectLiteralToLambda")
class MenuFragment : Fragment() {

    private lateinit var binding: FragmentMenuBinding

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

            toolBar.setNavigationOnClickListener {
                findNavController().popBackStack()
            }

            btnBack.setOnClickListener {
                findNavController().popBackStack()
            }

            btnExit.setOnLongClickListener(object : View.OnLongClickListener {
                override fun onLongClick(v: View?): Boolean {
                    requireActivity().finish()
                    return true
                }
            })
        }
    }

}