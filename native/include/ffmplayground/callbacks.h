#ifndef FFMPLAY_CALLBACKS_H
#define FFMPLAY_CALLBACKS_H

#include <stddef.h>
#include <stdint.h>

/* Comparator for qsort: returns negative if a < b, 0 if equal, positive if a > b */
typedef int (*IntComparator)(const void* a, const void* b);

void ffm_qsort_int(int32_t* base, size_t nmemb, IntComparator compar);

/* Apply a callback to each element, doubling it in place */
typedef int32_t (*IntUnaryOp)(int32_t x);
void ffm_map_int(int32_t* base, size_t nmemb, IntUnaryOp op);

#endif /* FFMPLAY_CALLBACKS_H */
