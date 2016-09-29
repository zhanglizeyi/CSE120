#include "stdio.h"
#include "stdlib.h"

int main(int argc, char** argv)
{
  int i;

  printf("%d arguments\n", argc);
  //printf("argc address: %d\n", &argc);
  //printf("argv address: %d\n", &argv);
  printf("local var i address: %d\n", &i);

  char * fmt = "arg %d: %s\n";
  /*
  printf("address of fmt: %d\n", &fmt);
  printf("address stored in fmt: %d\n", fmt);
  if (argc > 0) {
    printf("address stored in argv[0]: %d\n", argv[0]);
    printf("string in argv[0]: %s\n", argv[0]);
    printf("what it should be with guessed address 13316: %s\n", 13316);
  }
  if (argc > 1) {
    printf("address stored in argv[1]: %d\n", argv[1]);
    printf("string in argv[1]: %s\n", argv[1]);
  }
  if (argc > 2) {
    printf("address stored in argv[2]: %d\n", argv[2]);
    printf("string in argv[2]: %s\n", argv[2]);
  }
  if (argc > 3) {
    printf("address stored in argv[3]: %d\n", argv[3]);
    printf("string in argv[3]: %s\n", argv[3]);
  }
  */
  
  for (i=0; i<argc; i++)
    printf(fmt, i, argv[i]);

  return 0;
}
