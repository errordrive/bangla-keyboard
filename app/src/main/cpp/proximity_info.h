#ifndef PROXIMITY_INFO_H
#define PROXIMITY_INFO_H

#include <vector>
#include <cmath>

namespace openbangla {

struct KeyRect {
    int code;
    float x;
    float y;
    float width;
    float height;
};

class ProximityInfo {
public:
    ProximityInfo();
    void setKeyGeometry(const std::vector<KeyRect>& keys);
    float getProximityPenalty(int expectedCode, float touchX, float touchY) const;

private:
    std::vector<KeyRect> mKeys;
};

} // namespace openbangla

#endif // PROXIMITY_INFO_H
