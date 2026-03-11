#ifndef FFMPLAY_PERF_H
#define FFMPLAY_PERF_H

#include <stddef.h>
#include <stdint.h>

/* Sum elements in array (for benchmarking memory access patterns) */
int64_t ffm_sum_i32(const int32_t* arr, size_t n);

/* Copy n int32 elements from src to dst */
void ffm_copy_i32(int32_t* dst, const int32_t* src, size_t n);

/* Fill array with value */
void ffm_fill_i32(int32_t* arr, size_t n, int32_t value);

#endif /* FFMPLAY_PERF_H */
