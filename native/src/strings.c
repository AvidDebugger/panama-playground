#include "ffmplayground/strings.h"

size_t ffm_strlen(const char* s) {
    if (!s) return 0;
    size_t n = 0;
    while (s[n]) n++;
    return n;
}

char* ffm_strcpy(char* dst, const char* src) {
    if (!dst || !src) return dst;
    size_t i = 0;
    while ((dst[i] = src[i]) != '\0') i++;
    return dst;
}

char* ffm_strcat(char* dst, const char* src) {
    if (!dst || !src) return dst;
    size_t i = 0;
    while (dst[i]) i++;
    size_t j = 0;
    while ((dst[i++] = src[j++]) != '\0');
    return dst;
}
