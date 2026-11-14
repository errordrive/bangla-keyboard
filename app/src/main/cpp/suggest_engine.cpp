#include "suggest_engine.h"
#include <algorithm>

namespace openbangla {

SuggestEngine::SuggestEngine() {}

void SuggestEngine::loadDictionary(const uint8_t* data, size_t size) {
    mTrie.loadBinaryDictionary(data, size);
}

void SuggestEngine::addWord(const std::string& word, int frequency) {
    mTrie.insertWord(word, frequency);
}

void SuggestEngine::setPreviousWords(const std::vector<std::string>& prevWords) {
    mNgram.setPreviousWords(prevWords);
}

void SuggestEngine::setKeyGeometry(const std::vector<KeyRect>& keys) {
    mProximity.setKeyGeometry(keys);
}

std::vector<std::string> SuggestEngine::getSuggestions(const std::string& input,
                                                      const std::vector<float>& touchXs,
                                                      const std::vector<float>& touchYs,
                                                      int maxSuggestions) {
    std::vector<std::string> results;
    if (input.empty()) return results;

    // 1. Get exact prefix suggestions
    auto prefixCands = mTrie.getPrefixSuggestions(input, maxSuggestions * 2);

    // 2. Fuzzy / Edit Distance suggestions across known dictionary words
    auto allWords = mTrie.getAllWords();
    std::vector<SuggestionCandidate> scoredCands;

    for (const auto& cand : prefixCands) {
        scoredCands.push_back(cand);
    }

    for (const auto& word : allWords) {
        // Skip words already in prefixCands
        bool exists = false;
        for (const auto& c : prefixCands) {
            if (c.word == word) {
                exists = true;
                break;
            }
        }
        if (exists) continue;

        int dist = DamerauLevenshtein::computeDistance(input, word);
        if (dist <= 2) { // Allow up to 2 edits
            float similarity = DamerauLevenshtein::computeSimilarity(input, word);
            int freq = mTrie.getWordFrequency(word);

            // Compute spatial proximity score if touch coordinates are provided
            float proximityScore = 1.0f;
            size_t minLen = std::min(input.length(), std::min(touchXs.size(), touchYs.size()));
            for (size_t i = 0; i < minLen; ++i) {
                proximityScore *= mProximity.getProximityPenalty(input[i], touchXs[i], touchYs[i]);
            }

            // N-gram context probability boost
            float ngramBoost = mNgram.getNgramProbability(word);

            float combinedScore = (freq * 0.4f) + (similarity * 100.0f * 0.4f) +
                                  (proximityScore * 10.0f) + (ngramBoost * 30.0f);

            SuggestionCandidate sc;
            sc.word = word;
            sc.frequency = freq;
            sc.editDistance = static_cast<float>(dist);
            sc.combinedScore = combinedScore;
            scoredCands.push_back(sc);
        }
    }

    // Sort by combined score descending
    std::sort(scoredCands.begin(), scoredCands.end(),
              [](const SuggestionCandidate& a, const SuggestionCandidate& b) {
                  return a.combinedScore > b.combinedScore;
              });

    for (size_t i = 0; i < scoredCands.size() && results.size() < static_cast<size_t>(maxSuggestions); ++i) {
        results.push_back(scoredCands[i].word);
    }

    return results;
}

} // namespace openbangla
