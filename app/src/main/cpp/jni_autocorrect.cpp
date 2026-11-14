#include <jni.h>
#include <string>
#include "phonetic_fixer.h"

static openbangla::PhoneticFixer* gFixer = nullptr;

extern "C" {

JNIEXPORT void JNICALL
Java_com_openbangla_keyboard_engine_NativeAutoCorrectFixer_initFixer(JNIEnv* env, jobject thiz) {
    if (!gFixer) {
        gFixer = new openbangla::PhoneticFixer();
    }
}

JNIEXPORT jstring JNICALL
Java_com_openbangla_keyboard_engine_NativeAutoCorrectFixer_getCorrectionNative(
    JNIEnv* env, jobject thiz, jstring jinput) {
    if (!gFixer || !jinput) return nullptr;

    const char* nativeInput = env->GetStringUTFChars(jinput, nullptr);
    std::string correction = gFixer->getCorrection(nativeInput);
    env->ReleaseStringUTFChars(jinput, nativeInput);

    if (correction.empty()) {
        return nullptr;
    }
    return env->NewStringUTF(correction.c_str());
}

JNIEXPORT void JNICALL
Java_com_openbangla_keyboard_engine_NativeAutoCorrectFixer_addRuleNative(
    JNIEnv* env, jobject thiz, jstring jkey, jstring jval) {
    if (!gFixer || !jkey || !jval) return;

    const char* key = env->GetStringUTFChars(jkey, nullptr);
    const char* val = env->GetStringUTFChars(jval, nullptr);

    gFixer->addRule(key, val);

    env->ReleaseStringUTFChars(jkey, key);
    env->ReleaseStringUTFChars(jval, val);
}

JNIEXPORT void JNICALL
Java_com_openbangla_keyboard_engine_NativeAutoCorrectFixer_releaseFixer(JNIEnv* env, jobject thiz) {
    if (gFixer) {
        delete gFixer;
        gFixer = nullptr;
    }
}

} // extern "C"
