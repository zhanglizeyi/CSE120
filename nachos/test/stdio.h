/*-------------------------------------------------------------
 * stdio.h
 *
 * Header file for standard I/O routines.
 *-------------------------------------------------------------*/

#ifndef STDIO_H
#define STDIO_H

#include "syscall.h"
#include "stdarg.h"

typedef int		FILE;
#define stdin		fdStandardInput
#define stdout		fdStandardOutput

int  fgetc(FILE stream);
void readline(char *s, int maxlength);
int  tryreadline(char *s, char c, int maxlength);

#define getc(stream)	fgetc(stream)
#define getchar()	getc(stdin)
#define getch()		getchar()

void fputc(char c, FILE stream);
void fputs(const char *s, FILE stream);

#define puts(s)		fputs(s,stdout)
#define putc(c,stream)	fputc(c,stream)
#define putchar(c)	putc(c,stdout)
#define beep()		putchar(0x07)

void vsprintf(char *s, char *format, va_list ap);
void vfprintf(FILE f, char *format, va_list ap);
void vprintf(char *format, va_list ap);
void sprintf(char *s, char *format, ...);
void fprintf(FILE f, char *format, ...);
void printf(char *format, ...);

#endif // STDIO_H
