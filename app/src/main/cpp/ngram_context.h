#ifndef NGRAM_CONTEXT_H
#define NGRAM_CONTEXT_H

#include <string>
#include <vector>
#include <unordered_map>

namespace openbangla {

class NgramContext {
public:
    NgramContext();
    void setPreviousWords(const std::vector<std::string>& prevWords);
    void addBigram(const std::string& word1, const std::string& word2, float probability);
    float getNgramProbability(const std::string& word) const;

private:
    std::vector<std::string> mPreviousWords;
    // key: "w1|w2", value: prob
    std::unordered_map<std::string, float> mBigramProbabilities;
};

} // namespace openbangla

#endif // NGRAM_CONTEXT_H
