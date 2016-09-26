/*-------------------------------------------------------------
 * stdlib.h
 *
 * Header file for standard library functions.
 *-------------------------------------------------------------*/

#ifndef STDLIB_H
#define STDLIB_H

#include "syscall.h"

#define null	0L
#define true	1
#define false	0

#define min(a,b)  (((a) < (b)) ? (a) : (b))
#define max(a,b)  (((a) > (b)) ? (a) : (b))

#define divRoundDown(n,s)  ((n) / (s))
#define divRoundUp(n,s)    (((n) / (s)) + ((((n) % (s)) > 0) ? 1 : 0))

#define assert(_EX)	((_EX) ? (void) 0 : __assert(__FILE__, __LINE__))
void __assert(char* file, int line);

#define assertNotReached()	assert(false)

void *memcpy(void *s1, const void *s2, unsigned int n);
void *memset(void *s, int c, unsigned int n);

unsigned int strlen(const char *str);
char *strcpy(char *dst, const char *src);
int strcmp(const char *a, const char *b);
int strncmp(const char *a, const char *b, int n);

int atoi(const char *s);

#endif // STDLIB_H
