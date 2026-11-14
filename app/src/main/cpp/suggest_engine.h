#ifndef SUGGEST_ENGINE_H
#define SUGGEST_ENGINE_H

#include "patricia_trie.h"
#include "damerau_levenshtein.h"
#include "proximity_info.h"
#include "ngram_context.h"
#include <vector>
#include <string>

namespace openbangla {

class SuggestEngine {
public:
    SuggestEngine();

    void loadDictionary(const uint8_t* data, size_t size);
    void addWord(const std::string& word, int frequency);
    void setPreviousWords(const std::vector<std::string>& prevWords);
    void setKeyGeometry(const std::vector<KeyRect>& keys);

    std::vector<std::string> getSuggestions(const std::string& input,
                                            const std::vector<float>& touchXs,
                                            const std::vector<float>& touchYs,
                                            int maxSuggestions);

private:
    PatriciaTrie mTrie;
    ProximityInfo mProximity;
    NgramContext mNgram;
};

} // namespace openbangla

#endif // SUGGEST_ENGINE_H
