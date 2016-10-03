#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"

int main(int argc, char** argv)
{
  if (argc!=2) {
    printf("Usage: rm <file>\n");
    return 1;
  }

  if (unlink(argv[1]) != 0) {
      printf("Unable to remove %s\n", argv[1]);
      return 1;
  }

  return 0;
}
