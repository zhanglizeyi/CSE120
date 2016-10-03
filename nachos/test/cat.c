#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"

#define BUFSIZE 1024

char buf[BUFSIZE];

int main(int argc, char** argv)
{
  int fd, amount;

  if (argc!=2) {
    printf("Usage: cat <file>\n");
    return 1;
  }

  fd = open(argv[1]);
  if (fd==-1) {
    printf("Unable to open %s\n", argv[1]);
    return 1;
  }

  while ((amount = read(fd, buf, BUFSIZE))>0) {
    write(1, buf, amount);
  }

  close(fd);

  return 0;
}
