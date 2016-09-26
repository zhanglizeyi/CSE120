
package nachos.threads;

import java.util.Comparator;

public class KnappThread {

	private /*static*/ KThread threadToWake;
	private /*static*/ long wakeTime;
	
	public KnappThread(KThread t, long time)
	{
		this.threadToWake = t;
		this.wakeTime = time;
	}
	
	public KThread getThreadToWake()
	{
		return this.threadToWake;
	}
	
	public long getWakeTime()
	{
		return wakeTime;
	}
	
	public static class Comparer<T> implements Comparator<T>
	{
		public int compare(T t1, T t2)
		{
			KnappThread thread1 = (KnappThread) t1;
			KnappThread thread2 = (KnappThread) t2;
			
			if (thread1.getWakeTime() < thread2.getWakeTime())
				return -1;
			else if (thread1.getWakeTime() > thread2.getWakeTime())
				return 1;
			else
				return 0;
		}
		
		public boolean equals(Object v1)
		{
			// We should never use this method
			return false;
		}
	}
	
}
