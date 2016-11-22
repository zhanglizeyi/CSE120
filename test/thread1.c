#include <stdio.h>
#include <assert.h>
#include <pthread.h>

static volatile int counter = 0; 

void *myThread(void *arg){
	
	printf("%s: begin\n",(char *) arg);
	for(int i=0; i<20000000; i++){
		counter = counter + 1;
	}
	printf("%s: end\n",(char *) arg);
	return NULL;
}

int main(int argc, char *argv[]){

	pthread_t p1, p2;
	int rc;

	printf("================main: begin ===(counter %d )\n", counter);
	rc = pthread_create(&p1, NULL, myThread, "A"); assert(rc == 0);
	rc = pthread_create(&p2, NULL, myThread, "B"); assert(rc == 0);
	//join waits for the threads to finish
	
	rc = pthread_join(p1, NULL); assert(rc == 0);
	rc = pthread_join(p2, NULL); assert(rc == 0);

	printf("================main: end ======(counter %d)\n", counter);
	return 0;
}