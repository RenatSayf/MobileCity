@file:Suppress("ObjectLiteralToLambda")

package com.alfasreda.mobilecity.ui.settings

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceFragmentCompat
import com.alfasreda.mobilecity.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().setTheme(R.style.AppSettingsStyle)

        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.screen_bg_color))
    }

}