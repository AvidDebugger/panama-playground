#ifndef FFMPLAY_STRINGS_H
#define FFMPLAY_STRINGS_H

#include <stddef.h>

/* Returns length of null-terminated string (like strlen) */
size_t ffm_strlen(const char* s);

/* Copies src to dst, returns dst (like strcpy) */
char* ffm_strcpy(char* dst, const char* src);

/* Concatenates src to dst, returns dst (like strcat) */
char* ffm_strcat(char* dst, const char* src);

#endif /* FFMPLAY_STRINGS_H */
