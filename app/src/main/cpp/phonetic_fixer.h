#ifndef PHONETIC_FIXER_H
#define PHONETIC_FIXER_H

#include <string>
#include <vector>

namespace openbangla {

struct PhoneticRule {
    std::string key;
    std::string replacement;
};

class PhoneticFixer {
public:
    PhoneticFixer();
    std::string getCorrection(const std::string& input) const;
    void addRule(const std::string& key, const std::string& replacement);
    void sortRules();

private:
    std::vector<PhoneticRule> mRules;
    int binarySearch(const std::string& target) const;
};

} // namespace openbangla

#endif // PHONETIC_FIXER_H
