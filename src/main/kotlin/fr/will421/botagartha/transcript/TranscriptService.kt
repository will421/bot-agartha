@file:OptIn(KordVoice::class)

package fr.will421.botagartha.transcript

import club.minnced.opus.util.OpusLibrary
import dev.kord.common.annotation.KordVoice
import dev.kord.core.behavior.channel.BaseVoiceChannelBehavior
import dev.kord.voice.AudioFrame
import dev.kord.voice.AudioProvider
import dev.kord.voice.VoiceConnection
import fr.will421.botagartha.utils.bufferTimeout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.slf4j.LoggerFactory
import tomp2p.opuswrapper.Opus
import java.nio.IntBuffer
import java.nio.ShortBuffer
import java.time.Duration
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.SourceDataLine

class TranscriptService : KoinComponent {

    private val logger = LoggerFactory.getLogger(javaClass)


    var connection: VoiceConnection? = null

    companion object {
        init {
            OpusLibrary.loadFromJar()
        }
    }

    suspend fun startEcho(voiceChannel: BaseVoiceChannelBehavior) {
        // lets close the old connection if there is one
        connection?.shutdown()
        connection = null


        val echoAudioProvider = EchoAudioProvider()
        connection = voiceChannel.connect {
            receiveVoice = true
            audioProvider(echoAudioProvider)
        }

        echoAudioProvider.setFramesFlow(connection!!.streams.incomingAudioFrames.map { it.second })
    }


    suspend fun startSendToSpeaker(voiceChannel: BaseVoiceChannelBehavior) {
        // lets close the old connection if there is one
        connection?.shutdown()
        connection = null


        connection = voiceChannel.connect {
            receiveVoice = true
        }

        val error = IntBuffer.allocate(4)
        val opusDecoder = Opus.INSTANCE.opus_decoder_create(16000, 1, error)

        val format = AudioFormat(16000F, 16, 1, true, true)
        val playOnSpeaker = PlayOnSpeaker(format)

        connection!!.streams.incomingAudioFrames
            .map { it.second.data }
            .collect { packet ->
                val shortBuffer = ShortBuffer.allocate(960)
                val decoded = Opus.INSTANCE.opus_decode(opusDecoder, packet, packet.size, shortBuffer, 960, 0)
                shortBuffer.position(shortBuffer.position() + decoded)
                shortBuffer.flip()
                playOnSpeaker.play(shortBuffer)
            }
    }

    suspend fun startTranscript(voiceChannel: BaseVoiceChannelBehavior) {
        // lets close the old connection if there is one
        connection?.shutdown()
        connection = null

        val error = IntBuffer.allocate(4)
        val opusDecoder = Opus.INSTANCE.opus_decoder_create(16000, 1, error)
        val recognizer = Recognizer(16000F)

        connection = voiceChannel.connect {
            receiveVoice = true
        }

        connection!!.streams.incomingAudioFrames
            .map { it.second.data }
            .bufferTimeout(500, Duration.ofMillis(500))
            .collect { chunk ->
                chunk.forEach { packet ->
                    val shortBuffer = ShortBuffer.allocate(960)
                    val decoded = Opus.INSTANCE.opus_decode(opusDecoder, packet, packet.size, shortBuffer, 960, 0)
                    shortBuffer.position(shortBuffer.position() + decoded)
                    shortBuffer.flip()
                    recognizer.feed(shortBuffer)
                }
                logger.info(recognizer.result())
                recognizer.reset()
            }

    }

    suspend fun stop() {
        // lets close the old connection if there is one
        connection?.shutdown()
        connection = null
    }

}

class PlayOnSpeaker(format: AudioFormat) {

    private val speaker: SourceDataLine

    init {
        speaker = AudioSystem.getSourceDataLine(format)
        speaker.open(format)
        speaker.start()
    }

    fun play(shortBuffer: ShortBuffer) {
        val shortAudioBuffer = ShortArray(shortBuffer.remaining())
        shortBuffer[shortAudioBuffer]
        val audio: ByteArray = ShortToByte_Twiddle_Method(shortAudioBuffer)
        speaker.write(audio, 0, audio.size)
    }

    private fun ShortToByte_Twiddle_Method(input: ShortArray): ByteArray {
        val len = input.size
        val buffer = ByteArray(len * 2)
        for (i in 0 until len) {
            buffer[i * 2 + 1] = input[i].toByte()
            buffer[i * 2] = (input[i].toInt().toBigInteger() shr 8).toByte()
        }
        return buffer
    }
}

class EchoAudioProvider : AudioProvider {

    var incomingAudioFrames: Flow<AudioFrame>? = null

    override suspend fun provide(): AudioFrame? {
        val audioFrame = incomingAudioFrames?.first()
        return audioFrame
    }

    fun setFramesFlow(frames: Flow<AudioFrame>) {
        incomingAudioFrames = frames
    }
}