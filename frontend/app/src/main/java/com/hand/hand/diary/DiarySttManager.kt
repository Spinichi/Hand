//package com.hand.hand.diary
//
//import android.content.Context
//import android.content.Intent
//import android.os.Bundle
//import android.speech.RecognitionListener
//import android.speech.RecognizerIntent
//import android.speech.SpeechRecognizer
//import android.util.Log
//
//class DiarySttManager(private val context: Context) {
//
//    private val speechRecognizer: SpeechRecognizer =
//        SpeechRecognizer.createSpeechRecognizer(context)
//
//    private val recognizerIntent: Intent =
//        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
//            putExtra(
//                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
//            )
//            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
//            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
//        }
//
//    // 👇 전체 녹음 과정에서 텍스트 누적
//    private var accumulatedText = ""
//
//    private var onFinalResultCallback: ((String) -> Unit)? = null
//
//    init {
//        speechRecognizer.setRecognitionListener(object : RecognitionListener {
//
//            override fun onReadyForSpeech(params: Bundle?) {
//                Log.d("DiaryStt", "onReadyForSpeech → 초기화")
//                accumulatedText = ""
//            }
//
//            override fun onBeginningOfSpeech() {
//                Log.d("DiaryStt", "onBeginningOfSpeech")
//            }
//
//            override fun onPartialResults(partialResults: Bundle?) {
//                val list = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
//                val text = list?.firstOrNull() ?: ""
//                if (text.isNotBlank()) {
//                    accumulatedText = text   // 👈 계속 덮어쓰기 (실시간 가장 최신값 유지)
//                }
//                Log.d("DiaryStt", "partial: $text")
//            }
//
//            override fun onResults(results: Bundle?) {
//                val list = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
//                val finalText = list?.firstOrNull() ?: accumulatedText
//
//                Log.d("DiaryStt", "final: $finalText")
//
//                onFinalResultCallback?.invoke(finalText)
//            }
//
//            override fun onError(error: Int) {
//                Log.e("DiaryStt", "에러 발생: $error → accumulated='$accumulatedText'")
//                onFinalResultCallback?.invoke(accumulatedText)
//            }
//
//            override fun onRmsChanged(rmsdB: Float) {}
//            override fun onBufferReceived(buffer: ByteArray?) {}
//            override fun onEndOfSpeech() {}
//            override fun onEvent(eventType: Int, params: Bundle?) {}
//        })
//    }
//
//    fun startListening(onFinal: (String) -> Unit) {
//        onFinalResultCallback = onFinal
//        accumulatedText = ""
//        speechRecognizer.startListening(recognizerIntent)
//    }
//
//    fun stopListening() {
//        speechRecognizer.stopListening()
//    }
//
//    fun destroy() {
//        speechRecognizer.destroy()
//    }
//}
