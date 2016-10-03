#include "stdio.h"
#include "stdlib.h"

static char digittoascii(unsigned n, int uppercase) {
    assert(n<36);
  
    if (n<=9)
	return '0'+n;
    else if (uppercase)
	return 'A'+n-10;
    else
	return 'a'+n-10;
}

static int charprint(char **s, char c) {
    *((*s)++) = c;

    return 1;
}

static int mcharprint(char **s, char* chars, int length) {
    memcpy(*s, chars, length);
    *s += length;
  
    return length;
}

static int integerprint(char **s, int n, unsigned base, int min, int zpad, int upper) {
    char buf[32];
    int i=32, digit, len=0;

    assert(base>=2 && base < 36);

    if (min>32)
	min=32;

    if (n==0) {
	for (i=1; i<min; i++)
	    len += charprint(s, zpad ? '0' : ' ');
      
	len += charprint(s, '0');
	return len;
    }

    if (n<0) {
	len += charprint(s, '-');
	n *= -1;
    }

    while (n!=0) {
	digit = n%base;
	n /= base;
    
	if (digit<0)
	    digit *= -1;

	buf[--i] = digittoascii(digit, upper);
    }

    while (i>32-min)
	buf[--i] = zpad ? '0' : ' ';

    len += mcharprint(s, &buf[i], 32-i);
    return len;
}

static int stringprint(char **s, char *string) {
    return mcharprint(s, string, strlen(string));
}

static int _vsprintf(char *s, char *format, va_list ap) {
    int min,zpad,len=0,regular=0;
    char *temp;

    /* process format string */
    while (*format != 0) {
	/* if switch, process */
	if (*format == '%') {
	    if (regular > 0) {
		len += mcharprint(&s, format-regular, regular);
		regular = 0;
	    }
	    format++;
	    /* bug: '-' here will potentially screw things up */
	    assert(*format != '-');

	    min=zpad=0;

	    if (*format == '0')
		zpad=1;

	    min = atoi(format);

	    temp = format;
	    while (*temp >= '0' && *temp <= '9')
		temp++;

	    switch (*(temp++)) {

	    case 'c':
		len += charprint(&s, va_arg(ap, int));
		break;
	
	    case 'd':
		len += integerprint(&s, va_arg(ap, int), 10, min, zpad, 0);
		break;

	    case 'x':
		len += integerprint(&s, va_arg(ap, int), 16, min, zpad, 0);
		break;

	    case 'X':
		len += integerprint(&s, va_arg(ap, int), 16, min, zpad, 1);
		break;

	    case 's':
		len += stringprint(&s, (char*) va_arg(ap, int));
		break;
      
	    default:
		len += charprint(&s, '%');
		temp = format;
	    }

	    format = temp;
	}
	else {
	    regular++;
	    format++;
	}
    }

    if (regular > 0) {
	len += mcharprint(&s, format-regular, regular);
	regular = 0;
    }
  
    *s = 0;

    return len;
}

void vsprintf(char *s, char *format, va_list ap) {
    _vsprintf(s, format, ap);
}

static char vfprintfbuf[256];

void vfprintf(int fd, char *format, va_list ap) {
    int len = _vsprintf(vfprintfbuf, format, ap);
    assert(len < sizeof(vfprintfbuf));
    write(fd, vfprintfbuf, len);
}

void vprintf(char *format, va_list ap) {
    vfprintf(stdout, format, ap);
}

void sprintf(char *s, char *format, ...) {
    va_list ap;
    va_start(ap,format);

    vsprintf(s, format, ap);

    va_end(ap);
}

void fprintf(int fd, char *format, ...) {
    va_list ap;
    va_start(ap,format);

    vfprintf(fd, format, ap);

    va_end(ap);
}

void printf(char *format, ...) {
    va_list ap;
    va_start(ap,format);

    vprintf(format, ap);

    va_end(ap);
}
