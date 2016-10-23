package nachos.threads; 

import nachos.machine.*;


/*
	HashMapThread help us with store threads into this structure with 
	currentThread key,
	timer value
	
*/
public class HashMapThread{

	private KTread thread;
	private long timer;

	private HashMapThread(KTread k, long t){
		this.thread = k;
		this.timer = t;
	}

	//setter and getter implelemtation 
	private KTread getThread(){
		return this.thread;
	}

	private long getTimer(){
		return this.timer;
	}

	private KTread setThread(KTread k){
		this.thread = k;
	}

	private long setTimer(long t){
		this.timer = t;
	}

	private boolean isEmpty(){
		return size = 0;
	}

}