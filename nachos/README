                      Nachos for Java README

Welcome to Nachos for Java. We believe that working in Java rather than
C++ will greatly simplify the development process by preventing bugs
arising from memory management errors, and improving debugging support.

* Additional software

Nachos requires the Java Devlopment Kit, version 1.5 or later. A
recent version of the JDK is already installed on the instructional
machines.  If you are working at home, you will need to download the
JDK.  It is available from:

  http://www.oracle.com/technetwork/java/javase/downloads/index.html

Please DO NOT DOWNLOAD the JDK into your class account!  Use the
preinstalled version instead.

The build process for Nachos relies on GNU make. If you are running on
one of the instructional machines, it is the default make in your
path.  If you are running on Windows, you will need to download and
install a port.  The most popular is the Cygwin toolkit, available at:

  https://www.cygwin.com/

The Cygwin package includes ports of most common GNU utilities to
Windows.

For projects 2 and 3, you will need a MIPS cross compiler, which is a
specially compiled GCC which will run on one architecture (e.g.  x86)
and produce files for the MIPS processor.  These compilers are already
installed on the instructional machines, and are available in the
directory specified by the $ARCHDIR environment variable.

If you are working at home, you will need to get a cross-compiler for
yourself.  The cross-compiler for Linux is available from the project
web page, along with instructions for installing it.

* Compiling Nachos

You should now have a directory called nachos, containing a Makefile,
this README, and a number of subdirectories. 

The "nachos" command invokes java to run Nachos.  It is a script that
we have configured to already be in your path, and it is also
available in the "bin" directory.

To compile Nachos, go to the subdirectory for the project you wish to
compile (I will assume 'proj0/' for Project 0 in these examples), and
run make:

    cd proj0
    make

This will compile those portions of Nachos which are relevant to the
project, and place the compiled .class files in the proj0/nachos
directory. 

You can now test Nachos from the proj0/ directory with:

   nachos

You should see output resembling the following:

  nachos 5.0j initializing... config interrupt timer elevators user-check grader
  *** thread 0 looped 0 times
  *** thread 1 looped 0 times
  *** thread 0 looped 1 times
  *** thread 1 looped 1 times
  *** thread 0 looped 2 times
  *** thread 1 looped 2 times
  *** thread 0 looped 3 times
  *** thread 1 looped 3 times
  *** thread 0 looped 4 times
  *** thread 1 looped 4 times
  Machine halting!

  Ticks: total 24750, kernel 24750, user 0
  Disk I/O: reads 0, writes 0
  Console I/O: reads 0, writes 0
  Paging: page faults 0, TLB misses 0
  Network I/O: received 0, sent 0

This is the correct output for the "bare bones" Nachos, without any of
the features you will add during the projects.

If you are working on a project which runs user programs (projects 2-3), 
you will also need to compile the MIPS test programs with:

  make test

* Command Line Arguments

For a summary of the command line arguments, run:

    nachos -h

The commands are:

        -d <debug flags>
                Enable some debug flags, e.g. -d ti

        -h
                Print this help message.

        -s <seed>
                Specify the seed for the random number generator

        -x <program>
		Specify a program that UserKernel.run() should execute,
		instead of the value of the configuration variable
		Kernel.shellProgram

        -z
                print the copyright message

        -- <grader class>
		Specify an autograder class to use, instead of
		nachos.ag.AutoGrader

        -# <grader arguments>
                Specify the argument string to pass to the autograder.

        -[] <config file>
                Specifiy a config file to use, instead of nachos.conf


Nachos offers the following debug flags:

    c: COFF loader info 
    i: HW interrupt controller info 
    p: processor info 
    m: disassembly 
    M: more disassembly 
    t: thread info 
    a: process info (formerly "address space", hence a) 

To use multiple debug flags, clump them all together. For example, to
monitor coff info and process info, run:

    nachos -d ac

* nachos.conf

When Nachos starts, it reads in nachos.conf from the current
directory.  It contains a bunch of keys and values, in the simple
format "key = value" with one key/value pair per line. To change the
default scheduler, default shell program, to change the amount of
memory the simulator provides, or to reduce network reliability, modify
this file.

Machine.stubFileSystem:
    Specifies whether the machine should provide a stub file system. A
    stub file system just provides direct access to the test directory.
    Since we're not doing the file system project, this should always
    be true.

Machine.processor:
    Specifies whether the machine should provide a MIPS processor. In
    the first project, we only run kernel code, so this is false. In
    the other projects it should be true.

Machine.console:
    Specifies whether the machine should provide a console. Again, the
    first project doesn't need it, but the rest of them do.

Machine.disk:
    Specifies whether the machine should provide a simulated disk. No
    file system project, so this should always be false.

ElevatorBank.allowElevatorGUI:
    Normally true. When we grade, this will be false, to prevent
    malicious students from running a GUI during grading.

NachosSecurityManager.fullySecure:
    Normally false. When we grade, this will be true, to enable
    additional security checks.

Kernel.kernel:
    Specifies what kernel class to dynmically load.  For proj1, this is
    nachos.threads.ThreadedKernel. For proj2, this should be
    nachos.userprog.UserKernel. For proj3, nachos.vm.VMKernel. For
    proj4, nachos.network.NetKernel.

Processor.usingTLB:
    Specifies whether the MIPS processor provides a page table
    interface or a TLB interface. In page table mode (proj2), the
    processor accesses an arbitrarily large kernel data structure to do
    address translation. In TLB mode (proj3 and proj4), the processor
    maintains a small TLB (4 entries).

Processor.numPhysPages:
    The number of pages of physical memory.  Each page is 1K. This is
    normally 64, but we can lower it in proj3 to see whether projects
    thrash or crash.

Documentation:

The JDK provides a command to create a set of HTML pages showing all
classes and methods in program. We will make these pages available on
the webpage, but you can create your own for your home machine by doing
the following (from the nachos/ directory):

	mkdir ../doc
	make doc

Troubleshooting:

If you receive an error about "class not found exception", it may be
because you have not set the CLASSPATH environment variable. Add the
following to your .cshrc:

	setenv CLASSPATH .

or to your .bashrc:
   
	export CLASSPATH=.

* Credits

Nachos was originally written by Wayne A. Christopher, Steven J.
Procter, and Thomas E. Anderson. It incorporates the SPIM simulator
written by John Ousterhout. Nachos was rewritten in Java by Daniel
Hettena.

This version of Nachos was borrowed from CS162 at Berkeley and
modified for use at UCSD.

* Copyright

Copyright (c) 1992-2001 The Regents of the University of California.
All rights reserved.

Permission to use, copy, modify, and distribute this software and its
documentation for any purpose, without fee, and without written
agreement is hereby granted, provided that the above copyright notice
and the following two paragraphs appear in all copies of this
software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.  THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

