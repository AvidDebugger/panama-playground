#ifndef FFMPLAY_SCALARS_H
#define FFMPLAY_SCALARS_H

#include <stdint.h>

int32_t ffm_add_i32(int32_t a, int32_t b);
int64_t ffm_add_i64(int64_t a, int64_t b);
double ffm_add_f64(double a, double b);

/* No-op for measuring pure downcall overhead */
void ffm_noop(void);

#endif /* FFMPLAY_SCALARS_H */
