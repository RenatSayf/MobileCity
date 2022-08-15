@file:Suppress("ObjectLiteralToLambda")

package com.alfasreda.mobilecity.ui.main

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

class SpeechViewModel(private val app: Application) : AndroidViewModel(app), TextToSpeech.OnInitListener {

    sealed class State {
        object InitSuccess: State()
        object InitNotSuccess: State()
        object LangMissingData: State()
        object LangNotSupported: State()
    }

    private var _state = MutableStateFlow<State>(State.InitNotSuccess)
    val state = _state.asStateFlow()

    private val speech: TextToSpeech = TextToSpeech(app, this)
    override fun onInit(status: Int) {
        when(status) {
            TextToSpeech.SUCCESS -> {
                val result = speech.setLanguage(Locale.getDefault())
                if (result == TextToSpeech.LANG_MISSING_DATA) {
                    _state.value = State.LangMissingData
                }
                else if (result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    _state.value = State.LangNotSupported
                }
                else {
                    _state.value = State.InitSuccess
                }
            }
            else -> {
                _state.value = State.InitNotSuccess
            }
        }
    }
}