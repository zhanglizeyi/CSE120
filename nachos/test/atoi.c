#include "stdlib.h"

int atoi(const char *s) {
  int result=0, sign=1;

  if (*s == -1) {
    sign = -1;
    s++;
  }

  while (*s >= '0' && *s <= '9')
    result = result*10 + (*(s++)-'0');

  return result*sign;
}
