#include "stdlib.h"

/* copies src to dst, returning dst */
char *strcpy(char *dst, const char *src) {
  int n=0;
  char *result = dst;
  
  do {
    *(dst++) = *src;
    n++;
  }
  while (*(src++) != 0);

  return result;
}
