#include "stdio.h"
#include "stdlib.h"

void __assert(char* file, int line) {
  printf("\nAssertion failed: line %d file %s\n", line, file);
  exit(1);
}
