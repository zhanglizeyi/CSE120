#include "stdio.h"
#include "stdlib.h"

#define BUFFERSIZE	64

#define MAXARGSIZE	16
#define MAXARGS		16

/**
 * tokenizeCommand
 *
 * Splits the specified command line into tokens, creating a token array with a maximum
 * of maxTokens entries, using storage to hold the tokens. The storage array should be as
 * long as the command line.
 *
 * Whitespace (spaces, tabs, newlines) separate tokens, unless
 * enclosed in double quotes. Any character can be quoted by preceeding
 * it with a backslash. Quotes must be terminated.
 *
 * Returns the number of tokens, or -1 on error.
 */
static int tokenizeCommand(char* command, int maxTokens, char *tokens[], char* storage) {
    const int quotingCharacter = 0x00000001;
    const int quotingString = 0x00000002;
    const int startedArg = 0x00000004;

    int state = 0;    
    int numTokens = 0;

    char c;

    assert(maxTokens > 0);

    while ((c = *(command++)) != '\0') {
	if (state & quotingCharacter) {
	    switch (c) {
	    case 't':
		c = '\t';
		break;
	    case 'n':
		c = '\n';
		break;
	    }
	    *(storage++) = c;
	    state &= ~quotingCharacter;
	}
	else if (state & quotingString) {
	    switch (c) {
	    case '\\':
		state |= quotingCharacter;
		break;
	    case '"':
		state &= ~quotingString;
		break;
	    default:
		*(storage++) = c;
		break;
	    }
	}
	else {
	    switch (c) {
	    case ' ':
	    case '\t':
	    case '\n':
		if (state & startedArg) {
		    *(storage++) = '\0';
		    state &= ~startedArg;
		}
		break;
	    default:
		if (!(state & startedArg)) {
		    if (numTokens == maxTokens) {
			return -1;
		    }
		    tokens[numTokens++] = storage;
		    state |= startedArg;
		}

		switch (c) {
		case '\\':
		    state |= quotingCharacter;
		    break;
		case '"':
		    state |= quotingString;
		    break;
		default:
		    *(storage++) = c;
		    break;
		}
	    }
	}
    }

    if (state & quotingCharacter) {
	printf("Unmatched \\.\n");
	return -1;
    }

    if (state & quotingString) {
	printf("Unmatched \".\n");
	return -1;
    }

    if (state & startedArg) {
	*(storage++) = '\0';
    }

    return numTokens;
}

void runline(char* line) {
    int pid, background, status;
   
    char args[BUFFERSIZE], prog[BUFFERSIZE];
    char *argv[MAXARGS];

    int argc = tokenizeCommand(line, MAXARGS, argv, args);
    if (argc <= 0)
	return;

    if (argc > 0 && strcmp(argv[argc-1], "&") == 0) {
	argc--;
	background = 1;
    }
    else {
	background = 0;
    }

    if (argc > 0) {
	if (strcmp(argv[0], "exit")==0) {
	    if (argc == 1) {
		exit(0);
	    }
	    else if (argc == 2) {
		exit(atoi(argv[1]));
	    }
	    else {
		printf("exit: Expression Syntax.\n");
		return;
	    }
	}
	else if (strcmp(argv[0], "halt")==0) {
	    if (argc == 1) {
		halt();
		printf("Not the root process!\n");
	    }
	    else {
		printf("halt: Expression Syntax.\n");
	    }
	    return;
	}
	else if (strcmp(argv[0], "join")==0) {
	    if (argc == 2) {
		pid = atoi(argv[1]);
	    }
	    else {
		printf("join: Expression Syntax.\n");
		return;
	    }
	}
	else {
	    strcpy(prog, argv[0]);
	    strcat(prog, ".coff");

	    pid = exec(prog, argc, argv);
	    if (pid == -1) {
		printf("%s: exec failed.\n", argv[0]);
		return;
	    }
	}

	if (!background) {
	    switch (join(pid, &status)) {
	    case -1:
		printf("join: Invalid process ID.\n");
		break;
	    case 0:
		printf("\n[%d] Unhandled exception\n", pid);
		break;
	    case 1:
		printf("\n[%d] Done (%d)\n", pid, status);
		break;
	    }
	}
	else {
	    printf("\n[%d]\n", pid);
	}
    }
}

int main(int argc, char *argv[]) {
    char prompt[] = "nachos% ";

    char buffer[BUFFERSIZE];

    while (1) {
	printf("%s", prompt);

	readline(buffer, BUFFERSIZE);

	runline(buffer);
    }
}
