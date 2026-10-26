package com.albin.todoapp.presentation.screens.assistant.components

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import java.util.Locale

@Composable
fun rememberVoiceRecognizer(
    onResult: (String) -> Unit
): () -> Unit {
    val context = LocalContext.current

    // Fallback system speech activity launcher (Google Voice Typing / OS Recognizer)
    val speechIntentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.trim()
            if (!spoken.isNullOrBlank()) {
                onResult(spoken)
            }
        }
    }

    val launchIntentFallback: () -> Unit = {
        try {
            val deviceLocale = Locale.getDefault()
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, deviceLocale.toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, deviceLocale.toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Di tu tarea o recordatorio...")
            }
            speechIntentLauncher.launch(intent)
        } catch (_: Exception) {
            Toast.makeText(
                context,
                "No hay servicio de reconocimiento de voz disponible en este dispositivo.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    val speechRecognizer = remember {
        try {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                // Use standard createSpeechRecognizer which handles system service binding gracefully
                // rather than createOnDeviceSpeechRecognizer which throws error 13 if the Spanish offline pack isn't downloaded.
                SpeechRecognizer.createSpeechRecognizer(context)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    DisposableEffect(speechRecognizer) {
        onDispose {
            try {
                speechRecognizer?.destroy()
            } catch (_: Exception) {}
        }
    }

    val startListeningAction: () -> Unit = {
        startInAppListening(
            context = context,
            recognizer = speechRecognizer,
            onResult = onResult,
            onFallback = launchIntentFallback
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startListeningAction()
        } else {
            Toast.makeText(
                context,
                "Se requiere permiso de micrófono para dictar tareas.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    return {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            startListeningAction()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}

private fun startInAppListening(
    context: Context,
    recognizer: SpeechRecognizer?,
    onResult: (String) -> Unit,
    onFallback: () -> Unit
) {
    if (recognizer == null) {
        onFallback()
        return
    }

    val deviceLocale = Locale.getDefault()

    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, deviceLocale.toLanguageTag())
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, deviceLocale.toLanguageTag())
        putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, deviceLocale.toLanguageTag())
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
    }

    recognizer.setRecognitionListener(object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Toast.makeText(context, "Escuchando... Di tu tarea", Toast.LENGTH_SHORT).show()
        }

        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}

        override fun onError(error: Int) {
            try {
                recognizer.cancel()
            } catch (_: Exception) {}

            // Immediate fallback to system Google Voice Typing dialog when in-app service fails:
            // 13: ERROR_LANGUAGE_UNAVAILABLE (offline language pack not downloaded)
            // 12: ERROR_LANGUAGE_NOT_SUPPORTED
            // 11: ERROR_SERVER_DISCONNECTED
            // 8: ERROR_RECOGNIZER_BUSY
            // 5: ERROR_CLIENT
            // 4: ERROR_SERVER
            // 2: ERROR_NETWORK
            // 1: ERROR_NETWORK_TIMEOUT
            if (error == 13 || error == 12 || error == 11 ||
                error == SpeechRecognizer.ERROR_CLIENT ||
                error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY ||
                error == SpeechRecognizer.ERROR_SERVER ||
                error == SpeechRecognizer.ERROR_NETWORK ||
                error == SpeechRecognizer.ERROR_NETWORK_TIMEOUT
            ) {
                onFallback()
                return
            }

            val errorMsg = when (error) {
                SpeechRecognizer.ERROR_NO_MATCH -> "No se detectó voz. Intenta de nuevo."
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Tiempo de espera agotado."
                SpeechRecognizer.ERROR_AUDIO -> "Error de audio al grabar."
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permiso de micrófono denegado."
                else -> "No se pudo reconocer la voz (código: $error)."
            }
            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val spokenText = matches?.firstOrNull()?.trim()
            if (!spokenText.isNullOrBlank()) {
                onResult(spokenText)
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    })

    try {
        recognizer.startListening(intent)
    } catch (_: Exception) {
        onFallback()
    }
}
