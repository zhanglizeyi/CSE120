#include "stdio.h"
#include "stdlib.h"

int main(int argc, char** argv)
{
  int i;

  printf("%d arguments\n", argc);
  
  for (i=0; i<argc; i++)
    printf("arg %d: %s\n", i, argv[i]);

  return 0;
}
