#include <jni.h>
#include <string>
#include <vector>
#include "suggest_engine.h"

static openbangla::SuggestEngine* gSuggestEngine = nullptr;

extern "C" {

JNIEXPORT void JNICALL
Java_com_openbangla_keyboard_engine_NativeLanguageEngine_initNativeEngine(JNIEnv* env, jobject thiz) {
    if (!gSuggestEngine) {
        gSuggestEngine = new openbangla::SuggestEngine();
    }
}

JNIEXPORT void JNICALL
Java_com_openbangla_keyboard_engine_NativeLanguageEngine_loadDictionary(
    JNIEnv* env, jobject thiz, jbyteArray dictData) {
    if (!gSuggestEngine || !dictData) return;

    jsize len = env->GetArrayLength(dictData);
    jbyte* bytes = env->GetByteArrayElements(dictData, nullptr);

    gSuggestEngine->loadDictionary(reinterpret_cast<const uint8_t*>(bytes), static_cast<size_t>(len));

    env->ReleaseByteArrayElements(dictData, bytes, JNI_ABORT);
}

JNIEXPORT void JNICALL
Java_com_openbangla_keyboard_engine_NativeLanguageEngine_addWord(
    JNIEnv* env, jobject thiz, jstring jword, jint freq) {
    if (!gSuggestEngine || !jword) return;

    const char* nativeWord = env->GetStringUTFChars(jword, nullptr);
    gSuggestEngine->addWord(nativeWord, freq);
    env->ReleaseStringUTFChars(jword, nativeWord);
}

JNIEXPORT void JNICALL
Java_com_openbangla_keyboard_engine_NativeLanguageEngine_setPreviousWords(
    JNIEnv* env, jobject thiz, jobjectArray jwords) {
    if (!gSuggestEngine || !jwords) return;

    int count = env->GetArrayLength(jwords);
    std::vector<std::string> prevWords;
    for (int i = 0; i < count; ++i) {
        jstring str = (jstring)env->GetObjectArrayElement(jwords, i);
        if (str) {
            const char* utf = env->GetStringUTFChars(str, nullptr);
            prevWords.push_back(utf);
            env->ReleaseStringUTFChars(str, utf);
            env->DeleteLocalRef(str);
        }
    }

    gSuggestEngine->setPreviousWords(prevWords);
}

JNIEXPORT jobjectArray JNICALL
Java_com_openbangla_keyboard_engine_NativeLanguageEngine_getSuggestionsNative(
    JNIEnv* env, jobject thiz, jstring jinput, jfloatArray jtouchXs, jfloatArray jtouchYs, jint maxCount) {
    if (!gSuggestEngine || !jinput) {
        return env->NewObjectArray(0, env->FindClass("java/lang/String"), nullptr);
    }

    const char* inputChars = env->GetStringUTFChars(jinput, nullptr);
    std::string input(inputChars);
    env->ReleaseStringUTFChars(jinput, inputChars);

    std::vector<float> xs;
    std::vector<float> ys;

    if (jtouchXs && jtouchYs) {
        int lenX = env->GetArrayLength(jtouchXs);
        int lenY = env->GetArrayLength(jtouchYs);
        jfloat* elemX = env->GetFloatArrayElements(jtouchXs, nullptr);
        jfloat* elemY = env->GetFloatArrayElements(jtouchYs, nullptr);
        for (int i = 0; i < lenX; ++i) xs.push_back(elemX[i]);
        for (int i = 0; i < lenY; ++i) ys.push_back(elemY[i]);
        env->ReleaseFloatArrayElements(jtouchXs, elemX, JNI_ABORT);
        env->ReleaseFloatArrayElements(jtouchYs, elemY, JNI_ABORT);
    }

    auto suggestions = gSuggestEngine->getSuggestions(input, xs, ys, maxCount);

    jclass stringClass = env->FindClass("java/lang/String");
    jobjectArray result = env->NewObjectArray(static_cast<jsize>(suggestions.size()), stringClass, nullptr);

    for (size_t i = 0; i < suggestions.size(); ++i) {
        jstring jstr = env->NewStringUTF(suggestions[i].c_str());
        env->SetObjectArrayElement(result, static_cast<jsize>(i), jstr);
        env->DeleteLocalRef(jstr);
    }

    return result;
}

JNIEXPORT void JNICALL
Java_com_openbangla_keyboard_engine_NativeLanguageEngine_releaseNativeEngine(JNIEnv* env, jobject thiz) {
    if (gSuggestEngine) {
        delete gSuggestEngine;
        gSuggestEngine = nullptr;
    }
}

} // extern "C"
