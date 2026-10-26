package com.past9.phoneaos.voice

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

/**
 * On-device Farsi speech-to-text using whisper.cpp (GGML).
 *
 * Runs entirely offline: no API key, no network, no data leaves the phone.
 *
 * Model files (ggml-*.bin) are downloaded once from GitHub releases to the app's
 * files directory, then memory-mapped by whisper.cpp. Typical sizes:
 *   tiny   ~39 MB   (fast, good for short commands)
 *   base   ~148 MB  (balanced, recommended for Farsi)
 *   small  ~488 MB  (better accuracy, slower)
 *
 * Usage:
 *   val stt = WhisperStt(context, modelSize = "base") { text ->
 *       // text is the final transcription
 *   } onEnd = { error ->
 *       // error != null means init failed (model missing, etc.)
 *   }
 *   stt.start()
 *   // ... user speaks ...
 *   stt.finish()
 */
class WhisperStt(
    private val context: Context,
    /** Model size: "tiny", "base", "small". Larger = better accuracy, more RAM/disk. */
    private val modelSize: String = "base",
    private val onText: (String) -> Unit,
    private val onEnd: (error: String?) -> Unit,
) {
    companion object {
        private const val TAG = "WhisperStt"
        private const val RATE = 16000
        private val MODEL_URLS = mapOf(
            "tiny"  to "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-tiny.bin",
            "base"  to "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-base.bin",
            "small" to "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-small.bin",
        )
        private val MODEL_SIZES = mapOf(
            "tiny"  to 39_000_000L,
            "base"  to 148_000_000L,
            "small" to 488_000_000L,
        )

        // Load native library
        init {
            try {
                System.loadLibrary("whisper-jni")
                Log.i(TAG, "Native library loaded")
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "Failed to load whisper-jni: ${e.message}")
            }
        }
    }

    private val running = AtomicBoolean(false)
    private val recording = AtomicBoolean(false)
    private var modelPath: String? = null
    private var rec: AudioRecord? = null
    private var audioThread: Thread? = null
    
    // Download state (for setup screen)
    private val _isDownloading = AtomicBoolean(false)
    private var _downloadProgress: Float = 0f
    private var _downloadError: String? = null
    
    fun isDownloading() = _isDownloading.get()
    fun downloadProgress() = _downloadProgress
    fun downloadError() = _downloadError
    fun modelPath() = modelPath

    /** Called from UI to ensure model is downloaded before start(). */
    suspend fun prepare(): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = getModelFile()
            if (file.exists()) {
                modelPath = file.absolutePath
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "prepare failed", e)
            false
        }
    }

    private fun getModelFile() = java.io.File(context.filesDir, "whisper_$modelSize.bin")

    /** Start listening. Must call prepare() first or ensure model exists. */
    fun start() {
        if (running.getAndSet(true)) return
        if (modelPath == null) {
            modelPath = getModelFile().absolutePath
        }
        if (!java.io.File(modelPath!!).exists()) {
            onEnd("Model not downloaded: $modelSize")
            running.set(false)
            return
        }

        // Initialize native context
        val ok = nativeInit(modelPath!!)
        if (!ok) {
            onEnd("Failed to initialize whisper (bad model?)")
            running.set(false)
            return
        }

        // Set language to Farsi
        nativeSetLanguage("fa")

        // Start microphone
        startMic()
    }

    private fun startMic() {
        recording.set(true)
        audioThread = thread(name = "whisper-record", isDaemon = true) {
            try {
                val min = AudioRecord.getMinBufferSize(RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
                @Suppress("MissingPermission")
                val r = AudioRecord(
                    MediaRecorder.AudioSource.VOICE_RECOGNITION,
                    RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    maxOf(min, RATE / 10 * 2)  // 200ms buffer
                )
                if (r.state != AudioRecord.STATE_INITIALIZED) {
                    stop("Microphone unavailable")
                    return@thread
                }
                rec = r
                r.startRecording()

                val bufferSize = RATE / 5  // 200ms chunks (3200 samples)
                val shortBuf = ShortArray(bufferSize)
                val floatBuf = FloatArray(bufferSize)

                while (running.get() && recording.get()) {
                    val n = r.read(shortBuf, 0, bufferSize)
                    if (n <= 0) continue

                    // Convert int16 to float32 [-1, 1]
                    for (i in 0 until n) {
                        floatBuf[i] = shortBuf[i] / 32768.0f
                    }

                    // Accumulate for batch transcription on finish
                    for (i in 0 until n) {
                        audioBuffer.add(floatBuf[i])
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Recording error", e)
                stop("Recording error: ${e.message}")
            }
        }
    }

    private val audioBuffer = java.util.ArrayList<Float>()

    /** Stop listening and transcribe the accumulated audio. */
    fun finish() {
        if (!running.get()) return
        recording.set(false)
        audioThread?.join(2000)
        rec?.stop()
        rec?.release()
        rec = null

        if (audioBuffer.isEmpty()) {
            onText("")
            onEnd(null)
            nativeFree()
            running.set(false)
            return
        }

        // Convert to float array and transcribe
        val floatArray = FloatArray(audioBuffer.size) { audioBuffer[it] }
        val text = nativeTranscribe(floatArray, RATE)
        audioBuffer.clear()

        onText(text)
        onEnd(null)
        nativeFree()
        running.set(false)
    }

    /** Cancel without transcribing. */
    fun cancel() {
        recording.set(false)
        running.set(false)
        audioThread?.interrupt()
        rec?.stop()
        rec?.release()
        rec = null
        audioBuffer.clear()
        nativeFree()
        onEnd("Cancelled")
    }

    private fun stop(error: String) {
        recording.set(false)
        running.set(false)
        rec?.stop()
        rec?.release()
        rec = null
        nativeFree()
        onEnd(error)
    }

    // Native methods
    external fun nativeInit(modelPath: String): Boolean
    external fun nativeSetLanguage(lang: String)
    external fun nativeTranscribe(audioData: FloatArray, sampleRate: Int): String
    external fun nativeFree()

    /** Download model with progress callback. Returns file path on success. */
    suspend fun downloadModel(progress: (Float) -> Unit): String = withContext(Dispatchers.IO) {
        val url = MODEL_URLS[modelSize] ?: throw IllegalArgumentException("Unknown model: $modelSize")
        val expectedSize = MODEL_SIZES[modelSize] ?: 0L
        val file = getModelFile()
        val tmpFile = java.io.File(file.absolutePath + ".tmp")

        val client = okhttp3.OkHttpClient()
        val request = okhttp3.Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw java.io.IOException("HTTP ${response.code}")
        val body = response.body ?: throw java.io.IOException("No body")
        val inputStream = body.byteStream()
        val outputStream = tmpFile.outputStream()
        var downloaded = 0L
        val buf = ByteArray(8192)
        while (true) {
            val read = inputStream.read(buf)
            if (read == -1) break
            outputStream.write(buf, 0, read)
            downloaded += read.toLong()
            if (expectedSize > 0) progress(downloaded.toFloat() / expectedSize)
        }
        outputStream.close()
        inputStream.close()
        tmpFile.renameTo(file)
        modelPath = file.absolutePath
        file.absolutePath
    }

    /** Download model with internal state updates for UI. */
    suspend fun downloadModel(): String = withContext(Dispatchers.IO) {
        _isDownloading.set(true)
        _downloadProgress = 0f
        _downloadError = null
        try {
            val path = downloadModel { p -> _downloadProgress = p }
            _isDownloading.set(false)
            _downloadProgress = 1f
            path
        } catch (e: Exception) {
            _isDownloading.set(false)
            _downloadError = e.message ?: "Download failed"
            throw e
        }
    }

    fun cancelDownload() {
        _isDownloading.set(false)
        _downloadError = "Cancelled"
    }
}
