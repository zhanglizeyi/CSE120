#include "stdlib.h"

void *memset(void *s, int c, unsigned int n) {
  int i;

  for (i=0; i<n; i++)
    ((char*)s)[i] = (char) c;

  return s;
}
