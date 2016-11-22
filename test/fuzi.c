#include <sys/wait.h>
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>

int main(int argc, char *argv[]){
	int pid;

	pid = fork();

	if(pid < 0){
		printf("error \n");
		exit(1);
	}
	else if(pid ==0)
	{
		execlp("/bin/ls","ls",NULL);
	}
	else
	{
		int wc = wait(NULL);
		printf("child complete\n");
		exit(0);
	}

	return 0;
}