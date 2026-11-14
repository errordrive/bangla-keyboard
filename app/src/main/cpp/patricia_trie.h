#ifndef PATRICIA_TRIE_H
#define PATRICIA_TRIE_H

#include <string>
#include <vector>
#include <memory>
#include <unordered_map>
#include <cstdint>

namespace openbangla {

struct TrieNode {
    char character;
    bool isTerminal;
    int frequency; // 0 - 255
    std::unordered_map<char, std::shared_ptr<TrieNode>> children;

    TrieNode(char c = '\0', bool term = false, int freq = 0)
        : character(c), isTerminal(term), frequency(freq) {}
};

struct SuggestionCandidate {
    std::string word;
    int frequency;
    float editDistance;
    float combinedScore;
};

class PatriciaTrie {
public:
    PatriciaTrie();
    bool loadBinaryDictionary(const uint8_t* data, size_t size);
    void insertWord(const std::string& word, int frequency);
    bool containsWord(const std::string& word) const;
    int getWordFrequency(const std::string& word) const;
    std::vector<SuggestionCandidate> getPrefixSuggestions(const std::string& prefix, int maxResults) const;
    std::vector<std::string> getAllWords() const;

private:
    std::shared_ptr<TrieNode> mRoot;
    void collectWords(const std::shared_ptr<TrieNode>& node, const std::string& current,
                      std::vector<SuggestionCandidate>& results) const;
    void collectAll(const std::shared_ptr<TrieNode>& node, const std::string& current,
                    std::vector<std::string>& results) const;
};

} // namespace openbangla

#endif // PATRICIA_TRIE_H
