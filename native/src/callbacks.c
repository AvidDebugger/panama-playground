#include "ffmplayground/callbacks.h"
#include <stdlib.h>

void ffm_qsort_int(int32_t* base, size_t nmemb, IntComparator compar) {
    qsort(base, nmemb, sizeof(int32_t), (int (*)(const void*, const void*))compar);
}

void ffm_map_int(int32_t* base, size_t nmemb, IntUnaryOp op) {
    for (size_t i = 0; i < nmemb; i++) {
        base[i] = op(base[i]);
    }
}
