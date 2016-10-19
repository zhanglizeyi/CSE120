package nachos.threads;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>, and multiple
 * threads can be waiting to <i>listen</i>. But there should never be a time
 * when both a speaker and a listener are waiting, because the two threads can
 * be paired off at this point.
 */
public class Communicator {
	/**
	 * Allocate a new communicator.
	 */

	/*genernal purpose
		1. thread call speak(), then only thread block inside of speak(), then a different 
			call listen()
		2. thread call listen(), then only thread block inside of speak(), then a different 
			call speak()
		3. speak() can not return speak() itself, until call the listen() before call speak()
		4. mutiple threads call speak(), then theards will also block inside of the speak()
		
		speaker() -> 1. lock.acquire()
					 2. check buffer is full  -> write.sleep()
					 3. (write into buffer)
					 4. write.wake()
					 5. lock release()
		
		writter() -> 1. lock.acquire()
					 2. check buffer == null -> speaker.sleep()
					 3. (read from buffer)
					 4. speaker.wake()
					 5. lock.release()
	*/



	private int message;
	private Lock lock;

	private Condition2 speakCond;
	private Condition2 listenCond;
	private Condition2 returnCond;
	

	public blic Communicator() {
	
		message = null;

		lock = new Lock();

		speakCond = new Condition2(lock);

		listenCond = new Condition2(lock);

		returnCond = new Condition2(lock);
	}

	/**
	 * Wait for a thread to listen through this communicator, and then transfer
	 * <i>word</i> to the listener.
	 * 
	 * <p>
	 * Does not return until this thread is paired up with a listening thread.
	 * Exactly one listener should receive <i>word</i>.
	 * 
	 * @param word the integer to transfer.
	 */
	public void speak(int word) {

		lock.acquire();

		while( message != null)
			speakCond.sleep();

		message = word;
		listenCond.wake();
		returnCond.sleep();
		lock.release();
	}

	/**
	 * Wait for a thread to speak through this communicator, and then return the
	 * <i>word</i> that thread passed to <tt>speak()</tt>.
	 * 
	 * @return the integer transferred.
	 */
	public int listen() {

		lock.acquire(); 

		while( message == null )
			listenCond.sleep();

		//clear buffer 
		int retWord = message;
		message = null;

		speakCond.wake();

		lock.release();

		return retWord;
	}
}
