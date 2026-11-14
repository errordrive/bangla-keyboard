#include "proximity_info.h"

namespace openbangla {

ProximityInfo::ProximityInfo() {}

void ProximityInfo::setKeyGeometry(const std::vector<KeyRect>& keys) {
    mKeys = keys;
}

float ProximityInfo::getProximityPenalty(int expectedCode, float touchX, float touchY) const {
    for (const auto& key : mKeys) {
        if (key.code == expectedCode) {
            float centerX = key.x + (key.width / 2.0f);
            float centerY = key.y + (key.height / 2.0f);
            float dx = (touchX - centerX) / (key.width * 0.5f);
            float dy = (touchY - centerY) / (key.height * 0.5f);
            float squaredDist = dx * dx + dy * dy;

            // Gaussian proximity weight
            return std::exp(-0.5f * squaredDist);
        }
    }
    return 0.5f; // default fallback penalty
}

} // namespace openbangla
