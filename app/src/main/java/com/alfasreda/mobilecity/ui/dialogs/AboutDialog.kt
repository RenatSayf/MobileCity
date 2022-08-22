package com.alfasreda.mobilecity.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alfasreda.mobilecity.BuildConfig
import com.alfasreda.mobilecity.R
import com.alfasreda.mobilecity.databinding.DialogAboutUsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class AboutDialog : BottomSheetDialogFragment() {

    companion object {
        val TAG = "${this::class.java.simpleName}.TAG"
    }

    private lateinit var binding: DialogAboutUsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogAboutUsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        setStyle(STYLE_NO_TITLE, R.style.BottomSheetDialogTheme)
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val version = "Версия ${BuildConfig.VERSION_NAME}"
        binding.tvVersion.text = version

        binding.btnClose.setOnClickListener {
            dismiss()
        }
    }
}