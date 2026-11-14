#include "patricia_trie.h"
#include <algorithm>
#include <cstring>

namespace openbangla {

PatriciaTrie::PatriciaTrie() {
    mRoot = std::make_shared<TrieNode>();
}

void PatriciaTrie::insertWord(const std::string& word, int frequency) {
    if (word.empty()) return;

    auto current = mRoot;
    for (char c : word) {
        if (current->children.find(c) == current->children.end()) {
            current->children[c] = std::make_shared<TrieNode>(c, false, 0);
        }
        current = current->children[c];
    }
    current->isTerminal = true;
    current->frequency = std::max(current->frequency, frequency);
}

bool PatriciaTrie::containsWord(const std::string& word) const {
    auto current = mRoot;
    for (char c : word) {
        auto it = current->children.find(c);
        if (it == current->children.end()) {
            return false;
        }
        current = it->second;
    }
    return current->isTerminal;
}

int PatriciaTrie::getWordFrequency(const std::string& word) const {
    auto current = mRoot;
    for (char c : word) {
        auto it = current->children.find(c);
        if (it == current->children.end()) {
            return 0;
        }
        current = it->second;
    }
    return current->isTerminal ? current->frequency : 0;
}

std::vector<SuggestionCandidate> PatriciaTrie::getPrefixSuggestions(const std::string& prefix, int maxResults) const {
    std::vector<SuggestionCandidate> results;
    auto current = mRoot;

    for (char c : prefix) {
        auto it = current->children.find(c);
        if (it == current->children.end()) {
            return results;
        }
        current = it->second;
    }

    collectWords(current, prefix, results);

    std::sort(results.begin(), results.end(), [](const SuggestionCandidate& a, const SuggestionCandidate& b) {
        return a.frequency > b.frequency;
    });

    if (results.size() > static_cast<size_t>(maxResults)) {
        results.resize(maxResults);
    }

    return results;
}

void PatriciaTrie::collectWords(const std::shared_ptr<TrieNode>& node, const std::string& current,
                               std::vector<SuggestionCandidate>& results) const {
    if (!node) return;

    if (node->isTerminal) {
        SuggestionCandidate cand;
        cand.word = current;
        cand.frequency = node->frequency;
        cand.editDistance = 0.0f;
        cand.combinedScore = static_cast<float>(node->frequency);
        results.push_back(cand);
    }

    for (const auto& pair : node->children) {
        collectWords(pair.second, current + pair.first, results);
    }
}

std::vector<std::string> PatriciaTrie::getAllWords() const {
    std::vector<std::string> results;
    collectAll(mRoot, "", results);
    return results;
}

void PatriciaTrie::collectAll(const std::shared_ptr<TrieNode>& node, const std::string& current,
                             std::vector<std::string>& results) const {
    if (!node) return;
    if (node->isTerminal) {
        results.push_back(current);
    }
    for (const auto& pair : node->children) {
        collectAll(pair.second, current + pair.first, results);
    }
}

bool PatriciaTrie::loadBinaryDictionary(const uint8_t* data, size_t size) {
    if (!data || size < 4) return false;

    // Simple robust binary format: [count: uint32] [len: uint16, word: chars, freq: uint8]...
    // Or plain text newline entries if passed text/dictionary bytes
    size_t offset = 0;
    while (offset < size) {
        // Read until newline or end
        size_t start = offset;
        while (offset < size && data[offset] != '\n' && data[offset] != '\r') {
            offset++;
        }

        if (offset > start) {
            std::string line(reinterpret_cast<const char*>(data + start), offset - start);
            size_t tab = line.find('\t');
            if (tab != std::string::npos) {
                std::string word = line.substr(0, tab);
                int freq = 128;
                try {
                    freq = std::stoi(line.substr(tab + 1));
                } catch (...) {}
                insertWord(word, freq);
            } else {
                insertWord(line, 100);
            }
        }

        while (offset < size && (data[offset] == '\n' || data[offset] == '\r')) {
            offset++;
        }
    }

    return true;
}

} // namespace openbangla
