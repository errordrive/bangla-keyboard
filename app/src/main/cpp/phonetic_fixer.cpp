#include "phonetic_fixer.h"
#include <algorithm>

namespace openbangla {

PhoneticFixer::PhoneticFixer() {
    // Populate base high-frequency phonetic auto-corrections for Bengali
    mRules = {
        {"ami", "আমি"},
        {"amar", "আমার"},
        {"amader", "আমাদের"},
        {"amake", "আমাকে"},
        {"apni", "আপনি"},
        {"apnar", "আপনার"},
        {"apnader", "আপনাদের"},
        {"apnake", "আপনাকে"},
        {"tumi", "তুমি"},
        {"tomar", "তোমার"},
        {"tomader", "তোমাদের"},
        {"tomake", "তোমাকে"},
        {"kemon", "কেমন"},
        {"achho", "আছো"},
        {"achen", "আছেন"},
        {"bhalo", "ভালো"},
        {"bhalobashi", "ভালোবাসি"},
        {"bhalobasha", "ভালোবাসা"},
        {"dhaka", "ঢাকা"},
        {"bangla", "বাংলা"},
        {"bangladesh", "বাংলাদেশ"},
        {"shonar", "সোনার"},
        {"desh", "দেশ"},
        {"khub", "খুব"},
        {"onek", "অনেক"},
        {"shundor", "সুন্দর"},
        {"shokal", "সকাল"},
        {"shondha", "সন্ধ্যা"},
        {"rat", "রাত"},
        {"dhonnobad", "ধন্যবাদ"},
        {"shagotom", "স্বাগতম"},
        {"kobe", "কবে"},
        {"kothay", "কোথায়"},
        {"keno", "কেন"},
        {"ki", "কি"},
        {"kintu", "কিন্তু"},
        {"ebong", "এবং"},
        {"othoba", "অথবা"},
        {"shathe", "সাথে"},
        {"porashona", "পড়াশোনা"},
        {"kaj", "কাজ"},
        {"khobor", "খবর"}
    };
    sortRules();
}

void PhoneticFixer::sortRules() {
    std::sort(mRules.begin(), mRules.end(), [](const PhoneticRule& a, const PhoneticRule& b) {
        return a.key < b.key;
    });
}

void PhoneticFixer::addRule(const std::string& key, const std::string& replacement) {
    mRules.push_back({key, replacement});
    sortRules();
}

int PhoneticFixer::binarySearch(const std::string& target) const {
    int left = 0;
    int right = static_cast<int>(mRules.size()) - 1;

    while (left <= right) {
        int mid = left + (right - left) / 2;
        if (mRules[mid].key == target) {
            return mid;
        }
        if (mRules[mid].key < target) {
            left = mid + 1;
        } else {
            right = mid - 1;
        }
    }
    return -1;
}

std::string PhoneticFixer::getCorrection(const std::string& input) const {
    if (input.empty()) return "";
    int idx = binarySearch(input);
    if (idx >= 0 && idx < static_cast<int>(mRules.size())) {
        return mRules[idx].replacement;
    }
    return "";
}

} // namespace openbangla
