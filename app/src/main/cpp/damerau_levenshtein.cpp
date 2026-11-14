#include "damerau_levenshtein.h"

namespace openbangla {

int DamerauLevenshtein::computeDistance(const std::string& source, const std::string& target) {
    int lenS = static_cast<int>(source.length());
    int lenT = static_cast<int>(target.length());

    if (lenS == 0) return lenT;
    if (lenT == 0) return lenS;

    std::vector<std::vector<int>> d(lenS + 2, std::vector<int>(lenT + 2, 0));
    int maxDist = lenS + lenT;
    d[0][0] = maxDist;

    for (int i = 0; i <= lenS; ++i) {
        d[i + 1][0] = maxDist;
        d[i + 1][1] = i;
    }
    for (int j = 0; j <= lenT; ++j) {
        d[0][j + 1] = maxDist;
        d[1][j + 1] = j;
    }

    std::unordered_map<char, int> lastRow;

    for (int i = 1; i <= lenS; ++i) {
        int lastMatchCol = 0;
        char cs = source[i - 1];

        for (int j = 1; j <= lenT; ++j) {
            char ct = target[j - 1];
            int i1 = lastRow.count(ct) ? lastRow[ct] : 0;
            int j1 = lastMatchCol;

            int cost = (cs == ct) ? 0 : 1;
            if (cost == 0) {
                lastMatchCol = j;
            }

            d[i + 1][j + 1] = std::min({
                d[i][j + 1] + 1,             // deletion
                d[i + 1][j] + 1,             // insertion
                d[i][j] + cost               // substitution
            });

            // Transposition check
            if (i1 > 0 && j1 > 0) {
                int transCost = d[i1][j1] + (i - i1 - 1) + 1 + (j - j1 - 1);
                d[i + 1][j + 1] = std::min(d[i + 1][j + 1], transCost);
            }
        }
        lastRow[cs] = i;
    }

    return d[lenS + 1][lenT + 1];
}

float DamerauLevenshtein::computeSimilarity(const std::string& source, const std::string& target) {
    int dist = computeDistance(source, target);
    int maxLen = std::max(static_cast<int>(source.length()), static_cast<int>(target.length()));
    if (maxLen == 0) return 1.0f;
    return 1.0f - (static_cast<float>(dist) / static_cast<float>(maxLen));
}

} // namespace openbangla
