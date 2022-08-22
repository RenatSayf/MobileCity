@file:Suppress("ObjectLiteralToLambda", "MoveVariableDeclarationIntoWhen")

package com.alfasreda.mobilecity.ui.main

import android.app.Application
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*


class SpeechViewModel(private val app: Application) : AndroidViewModel(app), TextToSpeech.OnInitListener {

    companion object {
        val UTTERANCE_Id = "${this::class.java.simpleName}.utteranceId"
    }

    sealed class State {
        object NotActive: State()
        object InitSuccess: State()
        object InitError: State()
        object LangMissingData: State()
        object LangNotSupported: State()
    }

    private var _state = MutableStateFlow<State>(State.NotActive)
    val state = _state.asStateFlow()

    private val speech: TextToSpeech = TextToSpeech(app, this)
    override fun onInit(status: Int) {
        when(status) {
            TextToSpeech.SUCCESS -> {
                val result = speech.setLanguage(Locale.getDefault())
                when (result) {
                    TextToSpeech.LANG_MISSING_DATA -> {
                        _state.value = State.LangMissingData
                    }
                    TextToSpeech.LANG_NOT_SUPPORTED -> {
                        _state.value = State.LangNotSupported
                    }
                    else -> {
                        _state.value = State.InitSuccess
                    }
                }
            }
            else -> {
                _state.value = State.InitError
            }
        }
    }

    fun speak(text: String, listener: UtteranceProgressListener? = null) {
        if (_state.value == State.InitSuccess) {
            speech.speak(text, TextToSpeech.QUEUE_ADD, Bundle(), UTTERANCE_Id)
            if (listener != null) {
                speech.setOnUtteranceProgressListener(listener)
            }
        }
    }
}