#include "stdlib.h"

void *memcpy(void *s1, const void *s2, unsigned n) {
  int i;

  for (i=0; i<n; i++)
    ((char*)s1)[i] = ((char*)s2)[i];

  return s1;
}
