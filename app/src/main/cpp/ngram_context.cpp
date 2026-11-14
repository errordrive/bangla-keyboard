#include "ngram_context.h"

namespace openbangla {

NgramContext::NgramContext() {}

void NgramContext::setPreviousWords(const std::vector<std::string>& prevWords) {
    mPreviousWords = prevWords;
}

void NgramContext::addBigram(const std::string& word1, const std::string& word2, float probability) {
    mBigramProbabilities[word1 + "|" + word2] = probability;
}

float NgramContext::getNgramProbability(const std::string& word) const {
    if (mPreviousWords.empty()) {
        return 0.0f;
    }

    const std::string& lastWord = mPreviousWords.back();
    std::string key = lastWord + "|" + word;
    auto it = mBigramProbabilities.find(key);
    if (it != mBigramProbabilities.end()) {
        return it->second;
    }

    return 0.0f;
}

} // namespace openbangla
