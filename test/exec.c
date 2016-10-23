#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <sys/wait.h>

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
		printf("hello, I am child (pid:%d)\n", (int) getpid());
		char *args[3];
		args[0] = strdup("wc"); //program: "wc" word count
		args[1] = strdup("exec.c"); //argument: file to count
		args[2] = NULL;	//marks end of array
		execvp(args[0], args);
		printf("this shouldn't print out");
	}
	else
	{
		int wc = wait(NULL);
		printf("hello, I am parent of %d (wc:%d) (PID:%d)\n", rc, wc, (int) getpid());
		
	}

	return 0;
}