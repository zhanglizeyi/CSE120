#include "stdlib.h"

/* lexicographically compares a and b up to n chars */
int strncmp(const char* a, const char* b, int n)
{
  assert(n > 0);
  
  do {
    if (*a < *b)
      return -1;
    if (*a > *b)
      return 1;
    n--;
    a++;
    b++;
  }
  while (n > 0);

  return 0;
}
