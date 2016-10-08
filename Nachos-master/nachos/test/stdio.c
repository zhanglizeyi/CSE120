#include "stdio.h"
#include "stdlib.h"

int fgetc(int fd) {
    unsigned char c;

    while (read(fd, &c, 1) != 1);

    return c;
}

void fputc(char c, int fd) {
    write(fd, &c, 1);
}

void fputs(const char *s, int fd) {
    write(fd, (char*) s, strlen(s));
}
