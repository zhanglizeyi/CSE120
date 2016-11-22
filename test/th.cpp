#include <iostream>
#include <cstdlib>
#include <pthread.h>
#include <thread>

using namespace std;
std::thread th;

#define NUM_THREADS 2 

void Thread1(){

}

int Thread2(){

}

void task1( int a ){
	a = a * 2;
}

void task2( int b ){
	b = b * 4;
}

int main(){
	static int rc = 1;

	std::thread t1(task1);
	std::thread t2(task2);

	return 0;
}