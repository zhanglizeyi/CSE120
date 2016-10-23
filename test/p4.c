#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <fcntl.h>
#include <sys/wait.h>

/*
	exec.c 
	It loads code from that executable and overwrites its current code 
	segment with it, the heap and stack and other parts of the memory 
	space of the program are re-initialized. 
*/

int main(int argc, char *argv[]){

	printf("===============hello world (pid: %d)===============\n", (int) getpid());

	int rc = fork();
	//int rk = frok();
	if(rc < 0)
	{
		fprintf(stderr, "fork failed\n" );
		exit(1);
	}
	else if(rc == 0)
	{
		close(STDOUT_FILENO);
		open("./p4.output", O_CREAT|O_WRONLY|O_TRUNC, S_IRWXU);

		// printf("hello, I am child (pid:%d)\n", (int) getpid());
		char *args[3];
		args[0] = strdup("wc"); //program: "wc" word count
		args[1] = strdup("exec.c"); //argument: file to count
		args[2] = NULL;	//marks end of array
		execvp(args[0], args);
		//printf("this shouldn't print out");
	}
	else
	{
		int wc = wait(NULL);
		//printf("hello, I am parent of %d (wc:%d) (PID:%d)\n", rc, wc, (int) getpid());
		
	}

	return 0;
}