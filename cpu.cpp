#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>
#include <assert.h>
#include "common.h"

using namespace std;

int main( int argc, char *argv[]){

	if (arg !=2){
		cout << "usage: cpu " << endl;
		exit(1);
	}

	char *str = argv[1];
	while(1) {
		Spin(1);
		cout << str << endl;
	}

	return 0;
}