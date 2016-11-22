#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/wait.h>

/*
	fork.c 
	When system call, fork will create a new process
	that is exact copy of the calling process. And, both 
	will return from the fork(). 

	父会分配给子类他的memory， address...

	wait -- will let parent delay its execution until the child
			finishes executing. 
			When the child is done, wait() returns to the parent
*/

int main(int argc, char * argv[])
{
	printf("===============hello world (pid: %d)===============\n", (int) getpid());

	
	//int rk = frok();
	for(int i=0;i<3;i++){
			int rc = fork();
	if(rc < 0)
	{
		fprintf(stderr, "fork failed\n" );
		exit(1);
	}
	else if(rc == 0)
	{
		printf("hello, I am child (pid:%d)\n", (int) getpid());
	}
	else
	{
		//int wc = wait(NULL);
		printf("hello, I am parent of %d (PID:%d)\n", rc, (int) getpid());
		
	}
	}

	return 0;
}
