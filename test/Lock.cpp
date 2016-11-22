#include <iostream>       // std::cout
#include <thread>         // std::thread
#include <mutex>          // std::mutex, std::unique_lock
#include <cstdlib>

using namespace std;
std::mutex mtx;

class Lock{

public: 
	static int x; 

	static void AddToX();
	static void SubFromX();
};

int x = 0;

Lock::AddToX(){
		cout << "==========begain Add==========" << x << endl;
		mtx.lock();
		for(int i=0; i< 100; i++){
			int n = x;
			x = n + 1;
		}
		mtx.unlock();
		cout << "==========end Add==========" << x << endl;
}

Lock::SubFromX(){
		cout << "==========begain Add==========" << x << endl;
		mtx.lock();
		for(int i=0; i< 100; i++){
			int n = x;
			x = n-1;
		}
	mtx.unlock();
	cout << "==========end Add==========" << x << endl;
}

int main ()
{
	AddToX();
	SubFromX();

    return 0;
}