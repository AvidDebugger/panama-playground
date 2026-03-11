#ifndef FFMPLAY_STRUCTS_H
#define FFMPLAY_STRUCTS_H

#include <stdint.h>

typedef struct {
    int32_t x;
    int32_t y;
} Point;

typedef struct {
    double x;
    double y;
} PointF;

Point ffm_point_add(Point a, Point b);
PointF ffm_pointf_add(PointF a, PointF b);

void ffm_point_set(Point* p, int32_t x, int32_t y);
void ffm_point_get(const Point* p, int32_t* x, int32_t* y);

#endif /* FFMPLAY_STRUCTS_H */
