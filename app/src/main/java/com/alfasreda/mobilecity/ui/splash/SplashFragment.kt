package com.alfasreda.mobilecity.ui.splash

import android.os.Bundle
import android.speech.tts.UtteranceProgressListener
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.alfasreda.mobilecity.R
import com.alfasreda.mobilecity.ui.main.MainFragment
import com.alfasreda.mobilecity.ui.main.SpeechViewModel
import com.alfasreda.mobilecity.utils.Speech
import com.alfasreda.mobilecity.utils.appPref

const val KEY_FIRST_RUN = "KEY_FIRST_RUN"

class SplashFragment : Fragment() {

    private val speechVM: SpeechViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenStarted {

            speechVM.state.collect { state ->
                when(state) {

                    SpeechViewModel.State.NotActive -> {}
                    SpeechViewModel.State.InitError -> {
                        val bundle = Bundle().apply {
                            putInt(MainFragment.ARG_SPEECH, Speech.INIT_ERROR)
                        }
                        navigateToMainScreen(bundle)
                    }
                    SpeechViewModel.State.InitSuccess -> {
                        val isFirstRun = appPref.getBoolean(KEY_FIRST_RUN, true)
                        if (isFirstRun) {
                            speechVM.speak(getString(R.string.introductory_phrase), listener = object : UtteranceProgressListener() {
                                override fun onStart(utteranceId: String?) {
                                }
                                override fun onDone(utteranceId: String?) {
                                    navigateToMainScreen(null)
                                }
                                override fun onError(utteranceId: String?) {
                                    navigateToMainScreen(null)
                                }
                            })
                        }
                        else {
                            navigateToMainScreen(null)
                        }
                    }
                    SpeechViewModel.State.LangMissingData -> {
                        val bundle = Bundle().apply {
                            putInt(MainFragment.ARG_SPEECH, Speech.LANG_MISSING_DATA)
                        }
                        navigateToMainScreen(bundle)
                    }
                    SpeechViewModel.State.LangNotSupported -> {
                        val bundle = Bundle().apply {
                            putInt(MainFragment.ARG_SPEECH, Speech.LANG_NOT_SUPPORTED)
                        }
                        navigateToMainScreen(bundle)
                    }

                }
            }
        }
    }

    private fun navigateToMainScreen(bundle: Bundle?) {
        if (bundle == null) {
            findNavController().navigate(R.id.action_splashFragment_to_mainFragment)
        }
        else findNavController().navigate(R.id.action_splashFragment_to_mainFragment, bundle)
    }

}