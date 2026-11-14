#ifndef DAMERAU_LEVENSHTEIN_H
#define DAMERAU_LEVENSHTEIN_H

#include <string>
#include <vector>
#include <algorithm>
#include <unordered_map>

namespace openbangla {

class DamerauLevenshtein {
public:
    static int computeDistance(const std::string& source, const std::string& target);
    static float computeSimilarity(const std::string& source, const std::string& target);
};

} // namespace openbangla

#endif // DAMERAU_LEVENSHTEIN_H
