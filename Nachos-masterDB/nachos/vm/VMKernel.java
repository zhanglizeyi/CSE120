package nachos.vm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;
import nachos.vm.*;

/**
 * A kernel that can support multiple demand-paging user processes.
 */
public class VMKernel extends UserKernel {
	/**
	 * Allocate a new VM kernel.
	 */
	public VMKernel() {
		super();

	}

	/**
	 * Initialize this kernel.
	 */
	public void initialize(String[] args) {
		super.initialize(args);

//		swapSpace = new HashMap<MetaData, TranslationEntry>();
//		pagesCanBeSwapped = new ArrayList<TranslationEntry>();
//		freePages = new LinkedList<Integer>();
//		pinnedPages = new ArrayList<Integer>();
		
		swapPages = new LinkedList<Integer>();
		
		for(int i=0; i< swapPageCount; i++){
            swapPages.add(i);
        }
		
//		pinnedPages = new ArrayList<Integer>();
//		pinLock = new Lock();
		iptLock = new Lock();
		spLock = new Lock();
//		clockLock = new Lock();
		fullyPinned = new Condition(iptLock);
		
//		for (int i = 0; i < iPageTable.length; i++)
//		{
//			iPageTable[i] = null;
//		}
		
		swapFile = ThreadedKernel.fileSystem.open(swapName, true);
		

	}

	public static class MetaData {
		// virtual page number
		int vpn;
		
		// owning process
		VMProcess ownProcess;
		
		// pinned condition
		boolean pinned;
		
		public MetaData(int vpn, VMProcess ownProcess, boolean pinned)
		{
			this.vpn = vpn;
			this.ownProcess = ownProcess;
			this.pinned = pinned;
		}
		
		public TranslationEntry[] getPT() {
			return ownProcess.getPT();			
		}
		
		public TranslationEntry getEntry() {
			return getPT()[vpn];
		}
	}

	/**
	 * Test this kernel.
	 */
	public void selfTest() {
		super.selfTest();
	}

	/**
	 * Start running user programs.
	 */
	public void run() {
		super.run();
	}

	/**
	 * Terminate this kernel. Never returns.
	 */
	public void terminate() {
		//Close swapfile
		swapFile.close();
        ThreadedKernel.fileSystem.remove(swapFile.getName());
		
		super.terminate();
	}
	

	
//	public static void pinPage(Integer i){
//        pinLock.acquire();
//        pinnedPages.add(i);
//        pinLock.release();
//    }
//	
//	public static void unPinPage(Integer i){
//        pinLock.acquire();
//        pinnedPages.remove(i);
//        pinLock.release();
//    }
//	
//	public static boolean contains(TranslationEntry i){
//		pinLock.acquire();
//		boolean result = pinnedPages.contains(i);
//		pinLock.release();
//		return result;
//	}
	
	// assumes you hold iptLock already
	public static boolean swapOut(int ppn){
		// synchronizing tlb occurs b4 this function call
		
		MetaData data = iPageTable[ppn];
		data.pinned = true;
		iptLock.release();
		
		data.ownProcess.spnLock.acquire();
		Integer spn = data.ownProcess.vpnToSpn.get(data.vpn);
		if (spn == null) {
			// add a page to swapPages to increase its size i necessary
			spLock.acquire();
			if (swapPages.size() == 0)
				swapPages.add(++swapPageCount);
			spn = swapPages.removeFirst();
			spLock.release();
			data.ownProcess.vpnToSpn.put(data.vpn, spn);
		}
		data.ownProcess.spnLock.release();
		
		
        int size = Processor.pageSize;
        byte[] mem = Machine.processor().getMemory();
        
        int writtenBytes = 0;
        
        while (writtenBytes < size) {
            writtenBytes += swapFile.write(spn*size+writtenBytes, mem, ppn*size+writtenBytes, size-writtenBytes);
            if (writtenBytes == -1) {
            	Lib.debug(dbgVM, "Error occurred writing to swapFile");
            }
        }

//        System.arraycopy(pageContent,0,mem,data.getEntry().ppn,size);

		iptLock.acquire();
		
		data.getEntry().valid = false;
		

		
		iPageTable[ppn] = null;
		

		
		return true;
//        TranslationEntry swapPage = null;

//        if(!pagesCanBeSwapped.isEmpty()){
//            int i =0;
//            while(i< pagesCanBeSwapped.size())
//            {
//                spLock.acquire();
//                swapPage = pagesCanBeSwapped.get(i);
//                spLock.release();
//                
//                if(!contains(swapPage)){
//                    spLock.acquire();
//                    swapPage = pagesCanBeSwapped.remove(i);
//                    spLock.release();
//                    break;
//                }
//                i = (i+1) % (pagesCanBeSwapped.size());
//            }
//        }
//        int removed = -1;
//        
//        iptLock.acquire();
//        for(int i = 0; i < iPageTable.length; i++){
//        	
//        	//index is the ppn
//            if(i == swapPage.ppn){
//                removed = i;
//                break;
//            }
//        }
//
//        if(removed!=-1)
//        	iPageTable[removed] = null;
//        iptLock.release();
//
//        int size = Processor.pageSize;
//        
//        //if dirty, write to swap space
//        if(swapPage.dirty)  {
//        	byte[] pageContents = new byte[size];
//        	byte[] memory = Machine.processor().getMemory();
//
//        	System.arraycopy(memory, swapPage.ppn*size, pageContents, 0, size);
//        	int loc = freePages.removeFirst()*size;
//        	swapFile.write(loc, pageContents, 0, size);
//        }
//        
//        MetaData removedPage = iPageTable[removed];
//        swapSpace.put(removedPage,swapPage);
//        
//        
//        return swapPage.ppn;
    }
	
	// assumes you already hold the iptLock
	public static boolean swapIn(int vpn, VMProcess process){
//        TranslationEntry freeEntry = allocEntry(vpn, process, true, false);
        MetaData data = new MetaData(vpn, process, false);
        
//        int loc = diskLoc.get(data);
        iptLock.acquire();
		iPageTable[data.getEntry().ppn] = data;
		data.pinned = true;
		iptLock.release();
        int size = Processor.pageSize;
        
        byte[] pageContent = new byte[size];

        Integer spn = process.vpnToSpn.get(vpn);

        int writtenBytes = 0;
        
        while (writtenBytes < size) {
            writtenBytes += swapFile.read(spn*size+writtenBytes, pageContent, writtenBytes, size-writtenBytes);
            if (writtenBytes == -1) {
            	Lib.debug(dbgVM, "Error occurred reading from swapFile");
            }
        }
        byte[] mem = Machine.processor().getMemory();
        System.arraycopy(pageContent,0,mem,data.getEntry().ppn*size,size);
        
        iptLock.acquire();
        data.pinned = false;
        iptLock.release();
//        swapSpace.remove(data);
//        freePages.add(freeEntry.ppn);
        
//        diskLoc.remove(data);

        return true;

    }
	
	public static int allocPage(int vpn, VMProcess process, boolean readOnly){
		
		// kernel needs a static condition variable for all pages pinned
		int ppn = -1;
		

		
		physPageMutex.P();
		if (physicalPages.size() != 0)
			ppn = physicalPages.pollFirst();
		physPageMutex.V();
		
		iptLock.acquire(); // protects ipt and clockhand as well
		int originalHand = clockhand;
		
		while (ppn < 0)
		{
			
			
			clockhand = (clockhand + 1) % VMKernel.iPageTable.length;
//			if (clockhand == originalHand)
//			{
//				// condition variable wait
//				fullyPinned.sleep();
//			}
			
			physPageMutex.P();
			if (physicalPages.size() != 0) {
				ppn = physicalPages.pollFirst();
				physPageMutex.V();
				break;
			}
			physPageMutex.V();
//			ppn = VMKernel.allocPage(fault, this, false, faultEntry.readOnly);
			
			
			VMKernel.MetaData currMet = VMKernel.iPageTable[clockhand];
			
			if(!currMet.pinned)
			{
				// possible to be evicted if not pinned
				TranslationEntry currEntry = currMet.getEntry();
				int tlbIndex = -1;

				for (int i = 0; i < Machine.processor().getTLBSize(); i++) {
			        TranslationEntry entry = Machine.processor().readTLBEntry(i);
			        if (entry.vpn == currEntry.vpn && entry.valid == true) {
			            currMet.getPT()[entry.vpn] = entry;
			            tlbIndex = i;
			            break;
			        }
			    }
				currEntry = currMet.getEntry();
				if(currEntry.used)
					currEntry.used = false;
				else {
					currEntry.valid = false;
					
					// Invalidate TLB entry if vpn is in it
					if (tlbIndex != -1) {
						Machine.processor().writeTLBEntry(tlbIndex, currEntry);
					}
					
					ppn = clockhand;
					
					if (currEntry.dirty) {
						// evict and swap; clockhand is current ppn
						swapOut(clockhand);
					}
					else {
						// evict without swapping
					}
				}
			}

			
		} // end while (ppn < 0)
		
		iPageTable[ppn] = new MetaData(vpn, process, false);
		
		iptLock.release();

		
		return ppn;
		
//        int ppn = -1;
		
		
        
//        if(freePages.size() > 0) {
//            ppn = freePages.pollFirst();
//            return ppn;
//        }
//        else
//            ppn = swapOut();
//
//        TranslationEntry newPage = new TranslationEntry(vpn, ppn, true, readOnly, false, false);
//
//        if(canSwap)
//            pagesCanBeSwapped.add(newPage);
//
//        MetaData data = new MetaData(vpn, process, false);
//        
//        iptLock.acquire();
//        iPageTable[ppn] =  data;
//        iptLock.release();
//
//        return ppn;

    }
	
	//Does the same thing as allocPage but i needed a way to return the translation entry :( kinda redundant 
//	public static TranslationEntry allocEntry(int vpn, VMProcess process, boolean canSwap, boolean readOnly){
//        int ppn = -1;
//        
//        if(freePages.size() > 0) {
//            ppn = freePages.pollFirst();
//        }
//        else
//            ppn = swapOut();
//
//        TranslationEntry newPage = new TranslationEntry(vpn, ppn, true, readOnly, false, false);
//
//        if(canSwap)
//            pagesCanBeSwapped.add(newPage);
//
//        MetaData data = new MetaData(vpn, process, false);
//        
//        iptLock.acquire();
//        iPageTable[ppn] =  data;
//        iptLock.release();
//
//        return newPage;
//
//    }
	
	
	//Define Variables
	//*******************************************************************************************
	
//	protected static ArrayList<Integer> pinnedPages;
//	protected static ArrayList<TranslationEntry> pagesCanBeSwapped;
//	protected static LinkedList<Integer> freePages;
	protected static int swapPageCount = 5;
	protected static LinkedList<Integer> swapPages;
//	protected static HashMap<MetaData, TranslationEntry> swapSpace;
//	protected static HashMap<MetaData, Integer> diskLoc; 
	public static OpenFile swapFile;
	private static String swapName = ".teamGabNap";
	public static Lock pinLock;
	public static Lock spLock;
	public static Lock iptLock;
	public static MetaData[] iPageTable = new MetaData[Machine.processor().getNumPhysPages()];
	
//	private static Lock clockLock;
	private static int clockhand = 0;
	public static Condition fullyPinned;

	
	// inherited variables
//	/** Globally accessible reference to the synchronized console. */
//	public static SynchConsole console;
//
//	// dummy variables to make javac smarter
//	private static Coff dummy1 = null;
//	
//	//Adding new variables for CSE120 Proj 2 Part II 
//	public static LinkedList<Integer> physicalPages;
//	
//	public static Semaphore physPageMutex;
//	
//	public static Semaphore processIDMutex;
//	
//	public static int processID;
//	
//	public static int processCount;
//	public static Semaphore pCountMutex;
//	
//	//References the root process
//	public static UserProcess root = null;

	// dummy variables to make javac smarter
	private static VMProcess dummy1 = null;

	private static final char dbgVM = 'v';
}
