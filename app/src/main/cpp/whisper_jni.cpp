#include <jni.h>
#include <string>
#include <vector>
#include <android/log.h>
#include "whisper.h"

#define LOG_TAG "WhisperJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Global whisper context per instance (simple single-instance for now)
static struct whisper_context* g_ctx = nullptr;
static std::vector<float> g_audio_buffer;

extern "C" JNIEXPORT jboolean JNICALL
Java_com_past9_phoneaos_voice_WhisperStt_nativeInit(
    JNIEnv* env, jobject /* this */, jstring jModelPath
) {
    const char* modelPath = env->GetStringUTFChars(jModelPath, nullptr);
    LOGI("Initializing whisper with model: %s", modelPath);

    // Default params
    struct whisper_context_params cparams = whisper_context_default_params();
    cparams.use_gpu = false;  // CPU only on Android

    g_ctx = whisper_init_from_file_with_params(modelPath, cparams);
    env->ReleaseStringUTFChars(jModelPath, modelPath);

    if (!g_ctx) {
        LOGE("Failed to initialize whisper context");
        return JNI_FALSE;
    }

    LOGI("Whisper initialized successfully");
    return JNI_TRUE;
}

extern "C" JNIEXPORT void JNICALL
Java_com_past9_phoneaos_voice_WhisperStt_nativeSetLanguage(
    JNIEnv* /* env */, jobject /* this */, jstring jLang
) {
    if (!g_ctx) return;
    const char* lang = nullptr;
    // whisper.cpp expects language code like "fa" for Farsi
    // The model auto-detects if not specified, but we can hint
    // Note: whisper.cpp uses ISO 639-1 codes
    // We'll set it in full_transcribe params instead
    (void)jLang; // unused for now, language set at transcribe time
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_past9_phoneaos_voice_WhisperStt_nativeTranscribe(
    JNIEnv* env, jobject /* this */, jfloatArray jAudioData, jint sampleRate
) {
    if (!g_ctx) {
        return env->NewStringUTF("");
    }

    jsize len = env->GetArrayLength(jAudioData);
    if (len == 0) {
        return env->NewStringUTF("");
    }

    // Copy audio data
    jfloat* audioData = env->GetFloatArrayElements(jAudioData, nullptr);
    std::vector<float> samples(audioData, audioData + len);
    env->ReleaseFloatArrayElements(jAudioData, audioData, JNI_ABORT);

    // whisper.cpp expects 16kHz mono
    // Resample if needed (for now assume 16kHz from AudioRecord)
    if (sampleRate != 16000) {
        LOGE("Sample rate %d not supported, need 16000", sampleRate);
        return env->NewStringUTF("");
    }

    LOGI("Transcribing %d samples (%.2f seconds)", len, len / 16000.0f);

    // Full transcription parameters
    struct whisper_full_params wparams = whisper_full_default_params(WHISPER_SAMPLING_GREEDY);
    wparams.print_progress = false;
    wparams.print_special = false;
    wparams.print_realtime = false;
    wparams.print_timestamps = false;
    wparams.translate = false;
    wparams.language = "fa";  // Farsi
    wparams.n_threads = 4;
    wparams.offset_ms = 0;
    wparams.duration_ms = 0;

    // Run full transcription
    int ret = whisper_full(g_ctx, wparams, samples.data(), samples.size());
    if (ret != 0) {
        LOGE("whisper_full failed with code %d", ret);
        return env->NewStringUTF("");
    }

    // Collect results
    std::string result;
    int n_segments = whisper_full_n_segments(g_ctx);
    for (int i = 0; i < n_segments; ++i) {
        const char* text = whisper_full_get_segment_text(g_ctx, i);
        if (text && *text) {
            if (!result.empty()) result += " ";
            result += text;
        }
    }

    LOGI("Transcription: %s", result.c_str());
    return env->NewStringUTF(result.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_com_past9_phoneaos_voice_WhisperStt_nativeFree(
    JNIEnv* /* env */, jobject /* this */
) {
    if (g_ctx) {
        whisper_free(g_ctx);
        g_ctx = nullptr;
        LOGI("Whisper context freed");
    }
}