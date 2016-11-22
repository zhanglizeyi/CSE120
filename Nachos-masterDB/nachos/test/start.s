/* Start.s 
 *	Assembly language assist for user programs running on top of Nachos.
 *
 *	Since we don't want to pull in the entire C library, we define
 *	what we need for a user program here, namely Start and the system
 *	calls.
 */

#define START_S
#include "syscall.h"

        .text   
        .align  2

/* -------------------------------------------------------------
 * __start
 *	Initialize running a C program, by calling "main". 
 * -------------------------------------------------------------
 */

	.globl	__start
	.ent	__start
__start:
	jal	main
	addu	$4,$2,$0
	jal	exit	 /* if we return from main, exit(return value) */
	.end	__start

	.globl	__main
	.ent	__main
__main:	
	jr	$31
	.end	__main

/* -------------------------------------------------------------
 * System call stubs:
 *	Assembly language assist to make system calls to the Nachos kernel.
 *	There is one stub per system call, that places the code for the
 *	system call into register r2, and leaves the arguments to the
 *	system call alone (in other words, arg1 is in r4, arg2 is 
 *	in r5, arg3 is in r6, arg4 is in r7)
 *
 * 	The return value is in r2. This follows the standard C calling
 * 	convention on the MIPS.
 * -------------------------------------------------------------
 */

#define SYSCALLSTUB(name, number) \
	.globl	name		; \
	.ent	name		; \
name:				; \
	addiu	$2,$0,number	; \
	syscall			; \
	j	$31		; \
	.end	name

	SYSCALLSTUB(halt, syscallHalt)
	SYSCALLSTUB(exit, syscallExit)
	SYSCALLSTUB(exec, syscallExec)
	SYSCALLSTUB(join, syscallJoin)
	SYSCALLSTUB(creat, syscallCreate)
	SYSCALLSTUB(open, syscallOpen)
	SYSCALLSTUB(read, syscallRead)
	SYSCALLSTUB(write, syscallWrite)
	SYSCALLSTUB(close, syscallClose)
	SYSCALLSTUB(unlink, syscallUnlink)
	SYSCALLSTUB(mmap, syscallMmap)
	SYSCALLSTUB(connect, syscallConnect)
	SYSCALLSTUB(accept, syscallAccept)
