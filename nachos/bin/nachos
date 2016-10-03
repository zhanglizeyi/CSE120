#!/bin/sh

# Shell-script front-end to run Nachos.
# Simply sets terminal to a minimum of one byte to complete a read and
# disables character echo. Restores original terminal state on exit.

onexit () {
  stty $OLDSTTYSTATE
}

OLDSTTYSTATE=`stty -g`
trap onexit 0
stty -icanon min 1 -echo
java nachos.machine.Machine $*

