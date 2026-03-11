#include "ffmplayground/structs.h"

Point ffm_point_add(Point a, Point b) {
    Point r = { a.x + b.x, a.y + b.y };
    return r;
}

PointF ffm_pointf_add(PointF a, PointF b) {
    PointF r = { a.x + b.x, a.y + b.y };
    return r;
}

void ffm_point_set(Point* p, int32_t x, int32_t y) {
    if (p) {
        p->x = x;
        p->y = y;
    }
}

void ffm_point_get(const Point* p, int32_t* x, int32_t* y) {
    if (p && x && y) {
        *x = p->x;
        *y = p->y;
    }
}
