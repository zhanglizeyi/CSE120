#include "stdlib.h"

/* lexicographically compares a and b */
int strcmp(const char* a, const char* b) {
  do {
    if (*a < *b)
      return -1;
    if (*a > *b)
      return 1;
  }
  while (*(a++) != 0 && *(b++) != 0);

  return 0;
}
