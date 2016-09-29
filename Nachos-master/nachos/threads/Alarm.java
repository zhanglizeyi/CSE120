package nachos.threads;

import nachos.machine.*;

import java.util.PriorityQueue;

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
		
		pq = new PriorityQueue<KnappThread>(3, new KnappThread.Comparer<KnappThread>());
	}

	/**
	 * The timer interrupt handler. This is called by the machine's timer
	 * periodically (approximately every 500 clock ticks). Causes the current
	 * thread to yield, forcing a context switch if there is another thread that
	 * should be run.
	 */
	public void timerInterrupt() {
		
		KnappThread currKnapp = pq.peek();
		
		while ( currKnapp != null )
		{
			if (Machine.timer().getTime() >= currKnapp.getWakeTime())
			{
				Lib.assertTrue(currKnapp.getThreadToWake() != null);
				Machine.interrupt().disable();
				
//				System.out.println("Current time: " + Machine.timer().getTime() + "\n"
//						+ "Wait time: " + currKnapp.getWakeTime());
				
				pq.poll();
				
				currKnapp.getThreadToWake().ready();
				
				currKnapp = pq.peek();
			}
			else
			{
				break;
			}
		}
		
		KThread.currentThread().yield();
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
		// dk instead probably put thread in waiting queue
//		long wakeTime = Machine.timer().getTime() + x;
//		while (wakeTime > Machine.timer().getTime())
//			KThread.yield();
		
		wakeTime(x);
	}
	
	private void wakeTime(long x)
	{
		KnappThread newKnapp = new KnappThread(KThread.currentThread(),
				Machine.timer().getTime() + x);
		pq.add(newKnapp);
		
		boolean status = Machine.interrupt().disable();
		KThread.currentThread().sleep();
		Machine.interrupt().restore(status);
	}
	
	private static PriorityQueue<KnappThread> pq;
	
}
