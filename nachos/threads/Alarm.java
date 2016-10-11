package nachos.threads;

import nachos.machine.*;

//import java lib 
import java.util.*;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Iterator;
import java.util.HashMap;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
	/**
	 * Allocate a new Alarm. Set the machine's timer interrupt handler to this
	 * alarm's callback.
	 * 
	 * <p>
	 * <b>Note</b>: Nachos will not function correctly with more than one alarm.
	 */
	public Alarm() {
		Machine.timer().setInterruptHandler(new Runnable() {
			public void run() {
				timerInterrupt();
			}
		});
	}

	/*
		create a Hashmap to store threads
	*/
	Map<KThread, Long> mThread = new HashMap<KThread, Long>();
	/**
	 * The timer interrupt handler. This is called by the machine's timer
	 * periodically (approximately every 500 clock ticks). Causes the current
	 * thread to yield, forcing a context switch if there is another thread that
	 * should be run.
	 */
	public void timerInterrupt() {
		KThread.currentThread().yield();

		Iterator<Entry<KThread, Long>> it = mThread.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry<KThread, Long> entry = (Map.Entry<KThread, Long>) it.next();
			System.out.println(entry.getKey() + " = " + entry.getValue());
			if(entry.getValue() <= Machine.timer().getTime())
			{
				entry.getKey().ready();
				it.remove();
			}
		}
	}

	/**
	 * Put the current thread to sleep for at least <i>x</i> ticks, waking it up
	 * in the timer interrupt handler. The thread must be woken up (placed in
	 * the scheduler ready set) during the first timer interrupt where
	 * 
	 * <p>
	 * <blockquote> (current time) >= (WaitUntil called time)+(x) </blockquote>
	 * 
	 * @param x the minimum number of clock ticks to wait.
	 * 
	 * @see nachos.machine.Timer#getTime()
	 */
	public void waitUntil(long x) {
		// for now, cheat just to get something working (busy waiting is bad)
		//long wakeTime = Machine.timer().getTime() + x;
		//while (wakeTime > Machine.timer().getTime())
			//KThread.yield();

		/*Modification proj_1*/
		if( x > 0 )
		{
			long wakeTime = Machine.timer().getTime() + x;
			mThread.put(KThread.currentThread(), wakeTime);

			boolean checkStatus = Machine.interrupt().disable();
			KThread.currentThread().sleep();
			Machine.interrupt().restore(checkStatus);
		}
	}

	//test part not sure 
	
}





