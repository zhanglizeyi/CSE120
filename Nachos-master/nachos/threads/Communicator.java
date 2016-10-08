package nachos.threads;

import nachos.machine.*;

import java.util.LinkedList;

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
	public Communicator() {
		
		// The lock for communication
		comLock = new Lock();
		
		// Speak condition
		speakCond = new Condition2(comLock);
				
		// Listen condition
		listenCond = new Condition2(comLock);
				
		// Return condition
		rtnCond = new Condition2(comLock);
		
		// Integer buffer
		buffer = null;
		
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
		
		// Aquire the lock
		comLock.acquire();
		
		while (buffer != null)
		{
			speakCond.sleep();
		}
		
		buffer = word;
		
		listenCond.wake();
		
		rtnCond.sleep();
		
		comLock.release();
		
		
		
//		KThread nextThread = listenQueue.nextThread();
//		if ( nextThread == null )
//		{
//			speakQueue.waitForAccess(KThread.currentThread());
//			
//			speakCond.sleep();
//		}
//		else
//		{
//			speakQueue.acquire(KThread.currentThread());
//			
//			Lib.assertTrue(message == null);
//			message = new Integer(word);
//			
//			listenCond.wake(); // should we be waking? Or call ready() directly on the next thread.
//		}
//		
//		comLock.release();
//		
		
		
	}

	/**
	 * Wait for a thread to speak through this communicator, and then return the
	 * <i>word</i> that thread passed to <tt>speak()</tt>.
	 * 
	 * @return the integer transferred.
	 */
	public int listen() {
		
		int wordToReturn;
		
		// Aquire the lock
		comLock.acquire();
		
		while (buffer == null)
		{
			listenCond.sleep();
		}
		
		wordToReturn = buffer.intValue();
		buffer = null;
		
		speakCond.wake();
		
		rtnCond.wake();
		
		comLock.release();
		
		return wordToReturn;
		
//		KThread nextThread = speakQueue.nextThread();
//		if ( nextThread == null )
//		{
//			listenQueue.waitForAccess(KThread.currentThread());
//			
//			listenCond.sleep();
//		}
//		else
//		{
//			listenQueue.acquire(KThread.currentThread());
//			
//			Lib.assertTrue(message != null);
//			
//			wordToReturn = message.intValue();
//			message = null;
//			
//			listenCond.wake(); // should we be waking? Or call ready() directly on the next thread.
//		}
//		
//		comLock.release();
	}
	
	private Lock comLock;
	private Condition2 speakCond;
	private Condition2 listenCond;
	private Condition2 rtnCond;
	private Integer buffer;
	
	
	
	//A class that contains the tester. You pass this to the KThread constructor
	//to give the thread code to execute. 
	protected static class MyTester implements Runnable {
	 // Thread local copies of global variables go here. You need to access these
	 // variables in run, but they are passed to the tester in the constructor.
	 private int id;
	 
	 private static Communicator comm = new Communicator();
	 
	 // Construct the object. Pass the ID of the thread plus any variables you
	 // want to share between threads. You may want to pass a KThread as a global
	 // variable to test join.
		MyTester(int id) {
		    this.id = id;
		}
		
		// This method contains the actual code run by the thread. The constructor
		// is run by the main thread! You will want to test methods, such as,
		// join, waitUntil, speak, and listen, in here. 
		public void run() {
		    // Use an if statement to make the different threads execute different
		    // code.
		    if (id > 0) {
		        for (int i = 0; i < 5; i++) {
		            System.out.println("CommTester " + id + " calling speak with " + i);
		            comm.speak(i);
		        }
		    } else {
		        for (int i = 0; i < 20; i++) {
		            System.out.println("CommTester " + id + " listening on iteration " + i);
		            int heard = comm.listen();
		            System.out.println("CommTester " + id + " heard word " + heard);
		        }
		    }
		    
		    if (id == 0)
		    	System.out.println("Done with Communicator test 1 /////////////////////////////////");
		    ThreadedKernel.alarm.waitUntil(2000);
		    
		    if (id == 0) {
		        for (int i = 0; i < 20; i++) {
		            System.out.println("CommTester " + id + " calling speak with " + i);
		            comm.speak(i);
		        }
		    } else {
		        for (int i = 0; i < 5; i++) {
		            System.out.println("CommTester " + id + " listening on iteration " + i);
		            int heard = comm.listen();
		            System.out.println("CommTester " + id + " heard word " + heard);
		        }
		    }
		    
		    if (id == 0)
		    	System.out.println("Done with Communicator test 2 /////////////////////////////////");
		    ThreadedKernel.alarm.waitUntil(2000);
		    
		    if (id == 0 || id == 4) {
		        for (int i = 0; i < 6; i++) {
		            System.out.println("CommTester " + id + " calling speak with " + i);
		            comm.speak(i);
		        }
		    } else {
		        for (int i = 0; i < 4; i++) {
		            System.out.println("CommTester " + id + " listening on iteration " + i);
		            int heard = comm.listen();
		            System.out.println("CommTester " + id + " heard word " + heard);
		        }
		    }
		    
		    if (id == 0)
		    	System.out.println("Done with Communicator test 3 /////////////////////////////////");
		    ThreadedKernel.alarm.waitUntil(2000);

		    
		    if (id != 0 && id != 4) {
		        for (int i = 0; i < 4; i++) {
		            System.out.println("CommTester " + id + " calling speak with " + i);
		            comm.speak(i);
		        }
		    } else {
		        for (int i = 0; i < 6; i++) {
		            System.out.println("CommTester " + id + " listening on iteration " + i);
		            int heard = comm.listen();
		            System.out.println("CommTester " + id + " heard word " + heard);
		        }
		    }
		    
		    if (id == 0)
		    	System.out.println("Done with Communicator test 4 /////////////////////////////////");
		    ThreadedKernel.alarm.waitUntil(2000);

		}
	}

	//This method is called by the kernel when Nachos starts. 
	public static void selfTest() {
		// Initialize your global variables. You may want to make a Communicator
		// object, for example, and then share it between two threads.
		
		// Initialize your threads.
		KThread thread1 = new KThread(new MyTester(1));
		KThread thread2 = new KThread(new MyTester(2));
		KThread thread3 = new KThread(new MyTester(3));
		KThread thread4 = new KThread(new MyTester(4));



		
		// Fork your new threads.
		thread1.fork();
		thread2.fork();
		thread3.fork();
		thread4.fork();
		
		// This is the main thread. We can also consider this to be thread 0. So
		// let's have it run the code in the tester class as well.
		new MyTester(0).run();
	}

	
}
