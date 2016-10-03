#include "stdlib.h"

/* returns the length of the character string str, not including the null-terminator */
unsigned strlen(const char *str) {
  int result=0;

  while (*(str++) != 0)
    result++;

  return result;
}
