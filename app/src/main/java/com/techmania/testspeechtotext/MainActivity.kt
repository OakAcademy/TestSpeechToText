package com.techmania.testspeechtotext

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.techmania.testspeechtotext.databinding.ActivityMainBinding
import java.util.*


class MainActivity : AppCompatActivity() {

    lateinit var speechRecognizer : SpeechRecognizer
    lateinit var mainBinding: ActivityMainBinding
    lateinit var speechIntent : Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        val view = mainBinding.root
        setContentView(view)

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        mainBinding.buttonStart.setOnClickListener {

            if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO),1)
            }else{
                convertSpeech()
            }

        }

        //when the user clicks buttonStop, the mic. will close manually.
        mainBinding.buttonStop.setOnClickListener {
            speechRecognizer.stopListening()
            speechRecognizer.cancel()
            speechRecognizer.destroy()
            mainBinding.textView.text = "Please tap to button to speak"
            mainBinding.textViewResult.text = " "
        }

    }

    fun recognitionListenerFunctions(){
        speechRecognizer.setRecognitionListener(object : RecognitionListener {

            override fun onReadyForSpeech(params: Bundle?) {
                mainBinding.textView.text = "Listening..."
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                mainBinding.textView.text = "Please tap to button to speak"
            }

            override fun onError(error: Int) {

                when (error) {

                    SpeechRecognizer.ERROR_NO_MATCH -> { // if there is no speak, this error code is throw and we can handle it
                        val handler = Handler(Looper.getMainLooper())
                        mainBinding.textView.text = "Restarting the microphone, please wait..."

                        //after 2 seconds, the microphone is ready to speak again
                        handler.postDelayed(object : Runnable{
                            override fun run() {
                                convertSpeech()
                            }
                        },2000)

                    }
                    //if there is another error, the microphone will close
                    else -> {
                        speechRecognizer.stopListening()
                        speechRecognizer.cancel()
                        speechRecognizer.destroy()
                        mainBinding.textView.text = "Please tap to button to speak"
                    }
                }
            }

            override fun onResults(results: Bundle?) {

                val data: ArrayList<String> = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) as ArrayList<String>

                val myText = mainBinding.textViewResult.text.toString()
                mainBinding.textViewResult.text = myText.plus(" ").plus(data[0])

                //after the result, the microphone will not close, it will start again
                convertSpeech()
            }

            override fun onPartialResults(partialResults: Bundle?) {}

            override fun onEvent(eventType: Int, params: Bundle?) {}

        })
    }

    fun convertSpeech(){

        speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.getDefault())
        recognitionListenerFunctions()
        speechRecognizer.startListening(speechIntent)
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1 && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            convertSpeech()
        }
    }

}