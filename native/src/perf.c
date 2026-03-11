#include "ffmplayground/perf.h"

int64_t ffm_sum_i32(const int32_t* arr, size_t n) {
    int64_t sum = 0;
    for (size_t i = 0; i < n; i++) {
        sum += arr[i];
    }
    return sum;
}

void ffm_copy_i32(int32_t* dst, const int32_t* src, size_t n) {
    for (size_t i = 0; i < n; i++) {
        dst[i] = src[i];
    }
}

void ffm_fill_i32(int32_t* arr, size_t n, int32_t value) {
    for (size_t i = 0; i < n; i++) {
        arr[i] = value;
    }
}
