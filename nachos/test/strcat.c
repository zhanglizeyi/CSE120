#include "stdlib.h"

/* concatenates s2 to the end of s1 and returns s1 */
char *strcat(char *s1, const char *s2) {
    char* result = s1;

    while (*s1 != 0)
	s1++;
    
    do {
	*(s1++) = *(s2);
    }
    while (*(s2++) != 0);

    return result;
}
