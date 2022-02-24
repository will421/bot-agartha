package fr.will421.botagartha.transcript

import org.vosk.Model
import org.vosk.Recognizer
import java.nio.ShortBuffer

class VoskTranscript {

    companion object {
        fun initModel() {
            MODEL = Model("model")
        }

        var MODEL: Model? = null
    }


}

class Recognizer(sampleRate: Float) {

    fun feed(shortBuffer: ShortBuffer) {
        delegate.acceptWaveForm(shortBuffer.array(), shortBuffer.remaining())
    }

    fun result(): String? {
        return delegate.finalResult
    }

    fun reset() {
        delegate.reset()
    }

    val delegate = Recognizer(VoskTranscript.MODEL, sampleRate)
}