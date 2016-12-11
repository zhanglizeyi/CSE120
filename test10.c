/*
 * write10.c
 *
 * Test the write system call under a variety of good and bad
 * conditions, verifying output where possible.  Requires basic
 * functionality for open, creat, close, and read.
 *
 * Motto: Always check the return value of system calls.
 *
 * Geoff Voelker
 * 11/9/15
 */

#include "stdio.h"
#include "stdlib.h"

int bigbuf1[1024];
int bigbuf2[1024];
int bigbufnum = 1024;

int
do_creat (char *fname) {
    int fd;

    //printf ("creating %s...!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n", fname); ////////////////////////////////////////
    fd = creat (fname);
    if (fd >= 0) {
	printf ("...passed (fd = %d)\n", fd);
    } else {
	printf ("...failed (%d)\n", fd);
	exit (-1);
    }
    return fd;
}

int
do_open (char *fname) {
    int fd;

    printf ("opening %s...\n", fname);
    fd = open (fname);
    if (fd >= 0) {
	printf ("...passed (fd = %d)\n", fd);
    } else {
	printf ("...failed (%d)\n", fd);
	exit (-1);
    }
    return fd;
}

void
do_close (int fd) {
    int r;

    printf ("closing %d...\n", fd);
    r = close (fd);
    if (r < 0) {
	printf ("...failed (r = %d)\n", r);
    }
}

/*
 * Write "len" bytes of "buffer" into the file "fname".  "stride"
 * controls how many bytes are written in each system call.
 */
void
do_write (char *fname, char *buffer, int len, int stride)
{
    int fd, r, remain;
    char *ptr;

    fd = do_creat (fname);

    ptr = buffer, remain = len;
    printf ("writing %d bytes to file, %d bytes at a time...\n", len, stride);
    while (remain > 0) {
	int n = ((remain < stride) ? remain : stride);
        printf("each time write %d bytes",n);/////////////////
	r = write (fd, ptr, n);
    //printf("this is at the 769th pos %d",ptr+769);
	if (r < 0) {
	    printf ("...failed (r = %d)\n", r);
	} else if (r != n) {
	    printf ("...failed (expected to write %d bytes, but wrote %d)\n", n, r);
	} else {
	    printf ("...passed (wrote %d bytes)\n", r);
	}
	
	ptr += stride;
	remain -= stride;
    }
    //char buffer[769];
    //read (fd, buffer, 769);
    //printf("this is at the 769th pos %d", buffer+769);

    do_close (fd);
}

/*
 * Validate that the bytes of the file "fname" are the same as the
 * bytes in "truth".  Only compare "len" number of bytes.  "buffer" is
 * the temporary buffer used to read the contents of the file.  It is
 * allocated by the caller and needs to be at least "len" number of
 * bytes in size.
 */
void
do_validate (char *fname, char *buffer, char *truth, int len)
{
    int fd, r;

    fd = do_open (fname);

    printf ("reading %s into buffer..............................................\n", fname);
    r = read (fd, buffer, len);
    if (r < 0) {
	printf ("...failed (r = %d)\n", r);
	do_close (fd);
	return;
    } else if (r != len) {
	printf ("...failed (expected to read %d bytes, but read %d)\n", len, r);
	do_close (fd);
	return;
    } else {
	printf ("...success\n");
    }

    r = 0;
    printf ("validating %s...\n", fname);
    while (r < len) {
	if (buffer[r] != truth[r]) {
	    printf ("...failed (offset %d: expected %d, read %d)\n",
		    r, truth[r], buffer[r]); /////////////////////////////////
	    break;
	}
	r++;
    }
    if (r == len) {
	printf ("...passed\n");
    }

    do_close (fd);
}

int
main ()
{   
    char buffer[128], *file, *ptr;
    int buflen = 128;
    int fd, r, len, i;

    // /* write a small amount of data in a few different ways */
    file = "write.out";
    char *str = "roses are red\nviolets are blue\nI love Nachos\nand so do you\n";
    len = strlen (str);

    /* write all bytes at once */
    do_write (file, str, len, len);
    do_validate (file, buffer, str, len);

    /* write 8 bytes at a time */
    do_write (file, str, len, 8);
    do_validate (file, buffer, str, len);

    /* write 1 byte at a time */
    do_write (file, str, len, 1);
    do_validate (file, buffer, str, len);

    /* ok, now write lots of binary data.  if you want to manually
     * confirm what was written, running "od -i ../test/binary.out"
     * will print the file and interpret the data as integers. */
    file = "binary.out";
    len = sizeof (bigbuf1);  /* len in units of bytes, bigbufnum in ints */
    for (i = 0; i < bigbufnum; i++) {
	bigbuf1[i] = i;
    }

    /* write all at once */
    do_write (file, (char *) bigbuf1, len, len);
    do_validate (file, (char *) bigbuf2, (char *) bigbuf1, len);

    /* write 128 bytes at a time */
    do_write (file, (char *) bigbuf1, len, 128);
    do_validate (file, (char *) bigbuf2, (char *) bigbuf1, len);

    /* test corner cases for each of the three parameters to the write
     * system call. */

    /* test fd */
    fd = -10, len = 10;  /* value of len should not matter... */
    printf ("writing to an invalid fd (%d)...\n", fd);
    r = write (fd, buffer, len);
    if (r < 0) {
	printf ("...passed (r = %d)\n", r);
    } else {
	printf ("...failed (r = %d, should be -1)\n", r);
    }

    fd = 256, len = 10;  /* value of len should not matter... */
    printf ("writing to an invalid fd (%d)...\n", fd);
    r = write (fd, buffer, len);
    if (r < 0) {
	printf ("...passed (r = %d)\n", r);
    } else {
	printf ("...failed (r = %d, should be -1)\n", r);
    }

    fd = 8, len = 10;  /* value of len should not matter... */
    printf ("writing to an unopened fd (%d)...\n", fd);
    r = write (fd, buffer, len);
    if (r < 0) {
	printf ("...passed (r = %d)\n", r);
    } else {
	printf ("...failed (r = %d, should be -1)\n", r);
    }

    file = "bad.out";
    fd = do_creat (file);

    /* test buffer */
    printf ("writing count = 0 bytes...\n");
    r = write (fd, buffer, 0);
    if (r == 0) {
	printf ("...passed\n");
    } else {
	printf ("...failed (r = %d)\n", r);
    }

    printf ("writing with an invalid buffer (should not crash, only return an error)...\n");
    r = write (fd, (char *) 0xBADFFF, 10);
    printf("==================== bad buffer input %x \n", 0xBADFFF);
    if (r < 0) {
	printf ("...passed (r = %d)\n", r);
    } else {
	printf ("...failed (r = %d)\n", r);
    }
    printf("================END==================\n");

    /* test count */
    printf ("writing with an invalid count (should not crash, only return an error)...\n");
    r = write (fd, (char *) str, -1);
    if (r < 0) {
	printf ("...passed (r = %d)\n", r);
    } else {
	printf ("...failed (r = %d)\n", r);
    }

    printf ("writing with a buffer that extends beyond the end of the\n");
    printf ("address space.  write should return an error.\n");
    r = write (fd, (char *) 0, (80 * 1024));
    if (r > 0) {
	printf ("...failed (r = %d)\n", r);
    } else {
	printf ("...passed (r = %d)\n", r);
    }
    return 0;
}
