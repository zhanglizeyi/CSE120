#include "stdio.h"
#include "stdlib.h"

void readline(char *s, int maxlength) {
  int i = 0;

  while (1) {
    char c = getch();
    /* if end of line, finish up */
    if (c == '\n') {
      putchar('\n');
      s[i] = 0;
      return;
    }
    /* else if backspace... */
    else if (c == '\b') {
      /* if nothing to delete, beep */
      if (i == 0) {
	beep();
      }
      /* else delete it */
      else {
	printf("\b \b");
	i--;
      }
    }
    /* else if bad character or no room for more, beep */
    else if (c < 0x20 || i+1 == maxlength) {
      beep();
    }
    /* else add the character */
    else {
      s[i++] = c;
      putchar(c);
    }
  }
}
