// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

import nachos.security.*;

/**
 * The <tt>Processor</tt> class simulates a MIPS processor that supports a
 * subset of the R3000 instruction set. Specifically, the processor lacks all
 * coprocessor support, and can only execute in user mode. Address translation
 * information is accessed via the API. The API also allows a kernel to set an
 * exception handler to be called on any user mode exception.
 * 
 * <p>
 * The <tt>Processor</tt> API is re-entrant, so a single simulated processor can
 * be shared by multiple user threads.
 * 
 * <p>
 * An instance of a <tt>Processor</tt> also includes pages of physical memory
 * accessible to user programs, the size of which is fixed by the constructor.
 */
public final class Processor {
	/**
	 * Allocate a new MIPS processor, with the specified amount of memory.
	 * 
	 * @param privilege encapsulates privileged access to the Nachos machine.
	 * @param numPhysPages the number of pages of physical memory to attach.
	 */
	public Processor(Privilege privilege, int numPhysPages) {
		System.out.print(" processor");

		this.privilege = privilege;
		privilege.processor = new ProcessorPrivilege();

		Class<?> clsKernel = Lib.loadClass(Config.getString("Kernel.kernel"));
		Class<?> clsVMKernel = Lib.tryLoadClass("nachos.vm.VMKernel");

		usingTLB = (clsVMKernel != null && clsVMKernel
				.isAssignableFrom(clsKernel));

		this.numPhysPages = numPhysPages;

		for (int i = 0; i < numUserRegisters; i++)
			registers[i] = 0;

		mainMemory = new byte[pageSize * numPhysPages];

		if (usingTLB) {
			translations = new TranslationEntry[tlbSize];
			for (int i = 0; i < tlbSize; i++)
				translations[i] = new TranslationEntry();
		}
		else {
			translations = null;
		}
	}

	/**
	 * Set the exception handler, called whenever a user exception occurs.
	 * 
	 * <p>
	 * When the exception handler is called, interrupts will be enabled, and the
	 * CPU cause register will specify the cause of the exception (see the
	 * <tt>exception<i>*</i></tt> constants).
	 * 
	 * @param exceptionHandler the kernel exception handler.
	 */
	public void setExceptionHandler(Runnable exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
	}

	/**
	 * Get the exception handler, set by the last call to
	 * <tt>setExceptionHandler()</tt>.
	 * 
	 * @return the exception handler.
	 */
	public Runnable getExceptionHandler() {
		return exceptionHandler;
	}

	/**
	 * Start executing instructions at the current PC. Never returns.
	 */
	public void run() {
		Lib.debug(dbgProcessor, "starting program in current thread");

		registers[regNextPC] = registers[regPC] + 4;

		Machine.autoGrader().runProcessor(privilege);

		Instruction inst = new Instruction();

		while (true) {
			try {
				inst.run();
			}
			catch (MipsException e) {
				e.handle();
			}

			privilege.interrupt.tick(false);
		}
	}

	/**
	 * Read and return the contents of the specified CPU register.
	 * 
	 * @param number the register to read.
	 * @return the value of the register.
	 */
	public int readRegister(int number) {
		Lib.assertTrue(number >= 0 && number < numUserRegisters);

		return registers[number];
	}

	/**
	 * Write the specified value into the specified CPU register.
	 * 
	 * @param number the register to write.
	 * @param value the value to write.
	 */
	public void writeRegister(int number, int value) {
		Lib.assertTrue(number >= 0 && number < numUserRegisters);

		if (number != 0)
			registers[number] = value;
	}

	/**
	 * Test whether this processor uses a software-managed TLB, or single-level
	 * paging.
	 * 
	 * <p>
	 * If <tt>false</tt>, this processor directly supports single-level paging;
	 * use <tt>setPageTable()</tt>.
	 * 
	 * <p>
	 * If <tt>true</tt>, this processor has a software-managed TLB; use
	 * <tt>getTLBSize()</tt>, <tt>readTLBEntry()</tt>, and
	 * <tt>writeTLBEntry()</tt>.
	 * 
	 * <p>
	 * Using a method associated with the wrong address translation mechanism
	 * will result in an assertion failure.
	 * 
	 * @return <tt>true</tt> if this processor has a software-managed TLB.
	 */
	public boolean hasTLB() {
		return usingTLB;
	}

	/**
	 * Get the current page table, set by the last call to setPageTable().
	 * 
	 * @return the current page table.
	 */
	public TranslationEntry[] getPageTable() {
		Lib.assertTrue(!usingTLB);

		return translations;
	}

	/**
	 * Set the page table pointer. All further address translations will use the
	 * specified page table. The size of the current address space will be
	 * determined from the length of the page table array.
	 * 
	 * @param pageTable the page table to use.
	 */
	public void setPageTable(TranslationEntry[] pageTable) {
		Lib.assertTrue(!usingTLB);

		this.translations = pageTable;
	}

	/**
	 * Return the number of entries in this processor's TLB.
	 * 
	 * @return the number of entries in this processor's TLB.
	 */
	public int getTLBSize() {
		Lib.assertTrue(usingTLB);

		return tlbSize;
	}

	/**
	 * Returns the specified TLB entry.
	 * 
	 * @param number the index into the TLB.
	 * @return the contents of the specified TLB entry.
	 */
	public TranslationEntry readTLBEntry(int number) {
		Lib.assertTrue(usingTLB);
		Lib.assertTrue(number >= 0 && number < tlbSize);

		return new TranslationEntry(translations[number]);
	}

	/**
	 * Fill the specified TLB entry.
	 * 
	 * <p>
	 * The TLB is fully associative, so the location of an entry within the TLB
	 * does not affect anything.
	 * 
	 * @param number the index into the TLB.
	 * @param entry the new contents of the TLB entry.
	 */
	public void writeTLBEntry(int number, TranslationEntry entry) {
		Lib.assertTrue(usingTLB);
		Lib.assertTrue(number >= 0 && number < tlbSize);

		translations[number] = new TranslationEntry(entry);
	}

	/**
	 * Return the number of pages of physical memory attached to this simulated
	 * processor.
	 * 
	 * @return the number of pages of physical memory.
	 */
	public int getNumPhysPages() {
		return numPhysPages;
	}

	/**
	 * Return a reference to the physical memory array. The size of this array
	 * is <tt>pageSize * getNumPhysPages()</tt>.
	 * 
	 * @return the main memory array.
	 */
	public byte[] getMemory() {
		return mainMemory;
	}

	/**
	 * Concatenate a page number and an offset into an address.
	 * 
	 * @param page the page number. Must be between <tt>0</tt> and
	 * <tt>(2<sup>32</sup> / pageSize) - 1</tt>.
	 * @param offset the offset within the page. Must be between <tt>0</tt> and
	 * <tt>pageSize - 1</tt>.
	 * @return a 32-bit address consisting of the specified page and offset.
	 */
	public static int makeAddress(int page, int offset) {
		Lib.assertTrue(page >= 0 && page < maxPages);
		Lib.assertTrue(offset >= 0 && offset < pageSize);

		return (page * pageSize) | offset;
	}

	/**
	 * Extract the page number component from a 32-bit address.
	 * 
	 * @param address the 32-bit address.
	 * @return the page number component of the address.
	 */
	public static int pageFromAddress(int address) {
		return (int) (((long) address & 0xFFFFFFFFL) / pageSize);
	}

	/**
	 * Extract the offset component from an address.
	 * 
	 * @param address the 32-bit address.
	 * @return the offset component of the address.
	 */
	public static int offsetFromAddress(int address) {
		return (int) (((long) address & 0xFFFFFFFFL) % pageSize);
	}

	private void finishLoad() {
		delayedLoad(0, 0, 0);
	}

	/**
	 * Translate a virtual address into a physical address, using either a page
	 * table or a TLB. Check for alignment, make sure the virtual page is valid,
	 * make sure a read-only page is not being written, make sure the resulting
	 * physical page is valid, and then return the resulting physical address.
	 * 
	 * @param vaddr the virtual address to translate.
	 * @param size the size of the memory reference (must be 1, 2, or 4).
	 * @param writing <tt>true</tt> if the memory reference is a write.
	 * @return the physical address.
	 * @exception MipsException if a translation error occurred.
	 */
	private int translate(int vaddr, int size, boolean writing)
			throws MipsException {
		if (Lib.test(dbgProcessor))
			System.out.println("\ttranslate vaddr=0x" + Lib.toHexString(vaddr)
					+ (writing ? ", write" : ", read..."));

		// check alignment
		if ((vaddr & (size - 1)) != 0) {
			Lib.debug(dbgProcessor, "\t\talignment error");
			throw new MipsException(exceptionAddressError, vaddr);
		}

		// calculate virtual page number and offset from the virtual address
		int vpn = pageFromAddress(vaddr);
		int offset = offsetFromAddress(vaddr);

		TranslationEntry entry = null;

		// if not using a TLB, then the vpn is an index into the table
		if (!usingTLB) {
			if (translations == null || vpn >= translations.length
					|| translations[vpn] == null || !translations[vpn].valid) {
				privilege.stats.numPageFaults++;
				Lib.debug(dbgProcessor, "\t\tpage fault");
				throw new MipsException(exceptionPageFault, vaddr);
			}

			entry = translations[vpn];
		}
		// else, look through all TLB entries for matching vpn
		else {
			for (int i = 0; i < tlbSize; i++) {
				if (translations[i].valid && translations[i].vpn == vpn) {
					entry = translations[i];
					break;
				}
			}
			if (entry == null) {
				privilege.stats.numTLBMisses++;
				Lib.debug(dbgProcessor, "\t\tTLB miss");
				throw new MipsException(exceptionTLBMiss, vaddr);
			}
		}

		// check if trying to write a read-only page
		if (entry.readOnly && writing) {
			Lib.debug(dbgProcessor, "\t\tread-only exception");
			throw new MipsException(exceptionReadOnly, vaddr);
		}

		// check if physical page number is out of range
		int ppn = entry.ppn;
		if (ppn < 0 || ppn >= numPhysPages) {
			Lib.debug(dbgProcessor, "\t\tbad ppn");
			throw new MipsException(exceptionBusError, vaddr);
		}

		// set used and dirty bits as appropriate
		entry.used = true;
		if (writing)
			entry.dirty = true;

		int paddr = (ppn * pageSize) + offset;

		if (Lib.test(dbgProcessor))
			System.out.println("\t\tpaddr=0x" + Lib.toHexString(paddr));
		return paddr;
	}

	/**
	 * Read </i>size</i> (1, 2, or 4) bytes of virtual memory at <i>vaddr</i>,
	 * and return the result.
	 * 
	 * @param vaddr the virtual address to read from.
	 * @param size the number of bytes to read (1, 2, or 4).
	 * @return the value read.
	 * @exception MipsException if a translation error occurred.
	 */
	private int readMem(int vaddr, int size) throws MipsException {
		if (Lib.test(dbgProcessor))
			System.out.println("\treadMem vaddr=0x" + Lib.toHexString(vaddr)
					+ ", size=" + size);

		Lib.assertTrue(size == 1 || size == 2 || size == 4);

		int value = Lib.bytesToInt(mainMemory, translate(vaddr, size, false),
				size);

		if (Lib.test(dbgProcessor))
			System.out.println("\t\tvalue read=0x"
					+ Lib.toHexString(value, size * 2));

		return value;
	}

	/**
	 * Write <i>value</i> to </i>size</i> (1, 2, or 4) bytes of virtual memory
	 * starting at <i>vaddr</i>.
	 * 
	 * @param vaddr the virtual address to write to.
	 * @param size the number of bytes to write (1, 2, or 4).
	 * @param value the value to store.
	 * @exception MipsException if a translation error occurred.
	 */
	private void writeMem(int vaddr, int size, int value) throws MipsException {
		if (Lib.test(dbgProcessor))
			System.out.println("\twriteMem vaddr=0x" + Lib.toHexString(vaddr)
					+ ", size=" + size + ", value=0x"
					+ Lib.toHexString(value, size * 2));

		Lib.assertTrue(size == 1 || size == 2 || size == 4);

		Lib.bytesFromInt(mainMemory, translate(vaddr, size, true), size, value);
	}

	/**
	 * Complete the in progress delayed load and scheduled a new one.
	 * 
	 * @param nextLoadTarget the target register of the new load.
	 * @param nextLoadValue the value to be loaded into the new target.
	 * @param nextLoadMask the mask specifying which bits in the new target are
	 * to be overwritten. If a bit in <tt>nextLoadMask</tt> is 0, then the
	 * corresponding bit of register <tt>nextLoadTarget</tt> will not be
	 * written.
	 */
	private void delayedLoad(int nextLoadTarget, int nextLoadValue,
			int nextLoadMask) {
		// complete previous delayed load, if not modifying r0
		if (loadTarget != 0) {
			int savedBits = registers[loadTarget] & ~loadMask;
			int newBits = loadValue & loadMask;
			registers[loadTarget] = savedBits | newBits;
		}

		// schedule next load
		loadTarget = nextLoadTarget;
		loadValue = nextLoadValue;
		loadMask = nextLoadMask;
	}

	/**
	 * Advance the PC to the next instruction.
	 * 
	 * <p>
	 * Transfer the contents of the nextPC register into the PC register, and
	 * then add 4 to the value in the nextPC register. Same as
	 * <tt>advancePC(readRegister(regNextPC)+4)</tt>.
	 * 
	 * <p>
	 * Use after handling a syscall exception so that the processor will move on
	 * to the next instruction.
	 */
	public void advancePC() {
		advancePC(registers[regNextPC] + 4);
	}

	/**
	 * Transfer the contents of the nextPC register into the PC register, and
	 * then write the nextPC register.
	 * 
	 * @param nextPC the new value of the nextPC register.
	 */
	private void advancePC(int nextPC) {
		registers[regPC] = registers[regNextPC];
		registers[regNextPC] = nextPC;
	}

	/** Caused by a syscall instruction. */
	public static final int exceptionSyscall = 0;

	/** Caused by an access to an invalid virtual page. */
	public static final int exceptionPageFault = 1;

	/** Caused by an access to a virtual page not mapped by any TLB entry. */
	public static final int exceptionTLBMiss = 2;

	/** Caused by a write access to a read-only virtual page. */
	public static final int exceptionReadOnly = 3;

	/** Caused by an access to an invalid physical page. */
	public static final int exceptionBusError = 4;

	/** Caused by an access to a misaligned virtual address. */
	public static final int exceptionAddressError = 5;

	/** Caused by an overflow by a signed operation. */
	public static final int exceptionOverflow = 6;

	/** Caused by an attempt to execute an illegal instruction. */
	public static final int exceptionIllegalInstruction = 7;

	/** The names of the CPU exceptions. */
	public static final String exceptionNames[] = { "syscall      ",
			"page fault   ", "TLB miss     ", "read-only    ", "bus error    ",
			"address error", "overflow     ", "illegal inst " };

	/** Index of return value register 0. */
	public static final int regV0 = 2;

	/** Index of return value register 1. */
	public static final int regV1 = 3;

	/** Index of argument register 0. */
	public static final int regA0 = 4;

	/** Index of argument register 1. */
	public static final int regA1 = 5;

	/** Index of argument register 2. */
	public static final int regA2 = 6;

	/** Index of argument register 3. */
	public static final int regA3 = 7;

	/** Index of the stack pointer register. */
	public static final int regSP = 29;

	/** Index of the return address register. */
	public static final int regRA = 31;

	/** Index of the low register, used for multiplication and division. */
	public static final int regLo = 32;

	/** Index of the high register, used for multiplication and division. */
	public static final int regHi = 33;

	/** Index of the program counter register. */
	public static final int regPC = 34;

	/** Index of the next program counter register. */
	public static final int regNextPC = 35;

	/** Index of the exception cause register. */
	public static final int regCause = 36;

	/** Index of the exception bad virtual address register. */
	public static final int regBadVAddr = 37;

	/** The total number of software-accessible CPU registers. */
	public static final int numUserRegisters = 38;

	/** Provides privilege to this processor. */
	private Privilege privilege;

	/** MIPS registers accessible to the kernel. */
	private int registers[] = new int[numUserRegisters];

	/** The registered target of the delayed load currently in progress. */
	private int loadTarget = 0;

	/** The bits to be modified by the delayed load currently in progress. */
	private int loadMask;

	/** The value to be loaded by the delayed load currently in progress. */
	private int loadValue;

	/** <tt>true</tt> if using a software-managed TLB. */
	private boolean usingTLB;

	/** Number of TLB entries. */
	private int tlbSize = 4;

	/**
	 * Either an associative or direct-mapped set of translation entries,
	 * depending on whether there is a TLB.
	 */
	private TranslationEntry[] translations;

	/** Size of a page, in bytes. */
	public static final int pageSize = 0x400;

	/** Number of pages in a 32-bit address space. */
	public static final int maxPages = (int) (0x100000000L / pageSize);

	/** Number of physical pages in memory. */
	private int numPhysPages;

	/** Main memory for user programs. */
	private byte[] mainMemory;

	/** The kernel exception handler, called on every user exception. */
	private Runnable exceptionHandler = null;

	private static final char dbgProcessor = 'p';

	private static final char dbgDisassemble = 'm';

	private static final char dbgFullDisassemble = 'M';

	private class ProcessorPrivilege implements Privilege.ProcessorPrivilege {
		public void flushPipe() {
			finishLoad();
		}
	}

	private class MipsException extends Exception {
		public MipsException(int cause) {
			Lib.assertTrue(cause >= 0 && cause < exceptionNames.length);

			this.cause = cause;
		}

		public MipsException(int cause, int badVAddr) {
			this(cause);

			hasBadVAddr = true;
			this.badVAddr = badVAddr;
		}

		public void handle() {
			writeRegister(regCause, cause);

			if (hasBadVAddr)
				writeRegister(regBadVAddr, badVAddr);

			if (Lib.test(dbgDisassemble) || Lib.test(dbgFullDisassemble))
				System.out.println("exception: " + exceptionNames[cause]);

			finishLoad();

			Lib.assertTrue(exceptionHandler != null);

			// autograder might not want kernel to know about this exception
			if (!Machine.autoGrader().exceptionHandler(privilege))
				return;

			exceptionHandler.run();
		}

		private boolean hasBadVAddr = false;

		private int cause, badVAddr;
	}

	private class Instruction {
		public void run() throws MipsException {
			// hopefully this looks familiar to 152 students?
			fetch();
			decode();
			execute();
			writeBack();
		}

		private boolean test(int flag) {
			return Lib.test(flag, flags);
		}

		private void fetch() throws MipsException {
			if ((Lib.test(dbgDisassemble) && !Lib.test(dbgProcessor))
					|| Lib.test(dbgFullDisassemble))
				System.out.print("PC=0x" + Lib.toHexString(registers[regPC])
						+ "\t");

			value = readMem(registers[regPC], 4);
		}

		private void decode() {
			op = Lib.extract(value, 26, 6);
			rs = Lib.extract(value, 21, 5);
			rt = Lib.extract(value, 16, 5);
			rd = Lib.extract(value, 11, 5);
			sh = Lib.extract(value, 6, 5);
			func = Lib.extract(value, 0, 6);
			target = Lib.extract(value, 0, 26);
			imm = Lib.extend(value, 0, 16);

			Mips info;
			switch (op) {
			case 0:
				info = Mips.specialtable[func];
				break;
			case 1:
				info = Mips.regimmtable[rt];
				break;
			default:
				info = Mips.optable[op];
				break;
			}

			operation = info.operation;
			name = info.name;
			format = info.format;
			flags = info.flags;

			mask = 0xFFFFFFFF;
			branch = true;

			// get memory access size
			if (test(Mips.SIZEB))
				size = 1;
			else if (test(Mips.SIZEH))
				size = 2;
			else if (test(Mips.SIZEW))
				size = 4;
			else
				size = 0;

			// get nextPC
			nextPC = registers[regNextPC] + 4;

			// get dstReg
			if (test(Mips.DSTRA))
				dstReg = regRA;
			else if (format == Mips.IFMT)
				dstReg = rt;
			else if (format == Mips.RFMT)
				dstReg = rd;
			else
				dstReg = -1;

			// get jtarget
			if (format == Mips.RFMT)
				jtarget = registers[rs];
			else if (format == Mips.IFMT)
				jtarget = registers[regNextPC] + (imm << 2);
			else if (format == Mips.JFMT)
				jtarget = (registers[regNextPC] & 0xF0000000) | (target << 2);
			else
				jtarget = -1;

			// get imm
			if (test(Mips.UNSIGNED)) {
				imm &= 0xFFFF;
			}

			// get addr
			addr = registers[rs] + imm;

			// get src1
			if (test(Mips.SRC1SH))
				src1 = sh;
			else
				src1 = registers[rs];

			// get src2
			if (test(Mips.SRC2IMM))
				src2 = imm;
			else
				src2 = registers[rt];

			if (test(Mips.UNSIGNED)) {
				src1 &= 0xFFFFFFFFL;
				src2 &= 0xFFFFFFFFL;
			}

			if (Lib.test(dbgDisassemble) || Lib.test(dbgFullDisassemble))
				print();
		}

		private void print() {
			if (Lib.test(dbgDisassemble) && Lib.test(dbgProcessor)
					&& !Lib.test(dbgFullDisassemble))
				System.out.print("PC=0x" + Lib.toHexString(registers[regPC])
						+ "\t");

			if (operation == Mips.INVALID) {
				System.out.print("invalid: op=" + Lib.toHexString(op, 2)
						+ " rs=" + Lib.toHexString(rs, 2) + " rt="
						+ Lib.toHexString(rt, 2) + " rd="
						+ Lib.toHexString(rd, 2) + " sh="
						+ Lib.toHexString(sh, 2) + " func="
						+ Lib.toHexString(func, 2) + "\n");
				return;
			}

			int spaceIndex = name.indexOf(' ');
			Lib.assertTrue(spaceIndex != -1
					&& spaceIndex == name.lastIndexOf(' '));

			String instname = name.substring(0, spaceIndex);
			char[] args = name.substring(spaceIndex + 1).toCharArray();

			System.out.print(instname + "\t");

			int minCharsPrinted = 0, maxCharsPrinted = 0;

			for (int i = 0; i < args.length; i++) {
				switch (args[i]) {
				case Mips.RS:
					System.out.print("$" + rs);
					minCharsPrinted += 2;
					maxCharsPrinted += 3;

					if (Lib.test(dbgFullDisassemble)) {
						System.out
								.print("#0x" + Lib.toHexString(registers[rs]));
						minCharsPrinted += 11;
						maxCharsPrinted += 11;
					}
					break;
				case Mips.RT:
					System.out.print("$" + rt);
					minCharsPrinted += 2;
					maxCharsPrinted += 3;

					if (Lib.test(dbgFullDisassemble)
							&& (i != 0 || !test(Mips.DST))
							&& !test(Mips.DELAYEDLOAD)) {
						System.out
								.print("#0x" + Lib.toHexString(registers[rt]));
						minCharsPrinted += 11;
						maxCharsPrinted += 11;
					}
					break;
				case Mips.RETURNADDRESS:
					if (rd == 31)
						continue;
				case Mips.RD:
					System.out.print("$" + rd);
					minCharsPrinted += 2;
					maxCharsPrinted += 3;
					break;
				case Mips.IMM:
					System.out.print(imm);
					minCharsPrinted += 1;
					maxCharsPrinted += 6;
					break;
				case Mips.SHIFTAMOUNT:
					System.out.print(sh);
					minCharsPrinted += 1;
					maxCharsPrinted += 2;
					break;
				case Mips.ADDR:
					System.out.print(imm + "($" + rs);
					minCharsPrinted += 4;
					maxCharsPrinted += 5;

					if (Lib.test(dbgFullDisassemble)) {
						System.out
								.print("#0x" + Lib.toHexString(registers[rs]));
						minCharsPrinted += 11;
						maxCharsPrinted += 11;
					}

					System.out.print(")");
					break;
				case Mips.TARGET:
					System.out.print("0x" + Lib.toHexString(jtarget));
					minCharsPrinted += 10;
					maxCharsPrinted += 10;
					break;
				default:
					Lib.assertTrue(false);
				}
				if (i + 1 < args.length) {
					System.out.print(", ");
					minCharsPrinted += 2;
					maxCharsPrinted += 2;
				}
				else {
					// most separation possible is tsi, 5+1+1=7,
					// thankfully less than 8 (makes this possible)
					Lib.assertTrue(maxCharsPrinted - minCharsPrinted < 8);
					// longest string is stj, which is 40-42 chars w/ -d M;
					// go for 48
					while ((minCharsPrinted % 8) != 0) {
						System.out.print(" ");
						minCharsPrinted++;
						maxCharsPrinted++;
					}
					while (minCharsPrinted < 48) {
						System.out.print("\t");
						minCharsPrinted += 8;
					}
				}
			}

			if (Lib.test(dbgDisassemble) && Lib.test(dbgProcessor)
					&& !Lib.test(dbgFullDisassemble))
				System.out.print("\n");
		}

		private void execute() throws MipsException {
			int value;
			int preserved;

			switch (operation) {
			case Mips.ADD:
				dst = src1 + src2;
				break;
			case Mips.SUB:
				dst = src1 - src2;
				break;
			case Mips.MULT:
				dst = src1 * src2;
				registers[regLo] = (int) Lib.extract(dst, 0, 32);
				registers[regHi] = (int) Lib.extract(dst, 32, 32);
				break;
			case Mips.DIV:
				try {
					registers[regLo] = (int) (src1 / src2);
					registers[regHi] = (int) (src1 % src2);
					if (registers[regLo] * src2 + registers[regHi] != src1)
						throw new ArithmeticException();
				}
				catch (ArithmeticException e) {
					throw new MipsException(exceptionOverflow);
				}
				break;

			case Mips.SLL:
				dst = src2 << (src1 & 0x1F);
				break;
			case Mips.SRA:
				dst = src2 >> (src1 & 0x1F);
				break;
			case Mips.SRL:
				dst = src2 >>> (src1 & 0x1F);
				break;

			case Mips.SLT:
				dst = (src1 < src2) ? 1 : 0;
				break;

			case Mips.AND:
				dst = src1 & src2;
				break;
			case Mips.OR:
				dst = src1 | src2;
				break;
			case Mips.NOR:
				dst = ~(src1 | src2);
				break;
			case Mips.XOR:
				dst = src1 ^ src2;
				break;
			case Mips.LUI:
				dst = imm << 16;
				break;

			case Mips.BEQ:
				branch = (src1 == src2);
				break;
			case Mips.BNE:
				branch = (src1 != src2);
				break;
			case Mips.BGEZ:
				branch = (src1 >= 0);
				break;
			case Mips.BGTZ:
				branch = (src1 > 0);
				break;
			case Mips.BLEZ:
				branch = (src1 <= 0);
				break;
			case Mips.BLTZ:
				branch = (src1 < 0);
				break;

			case Mips.JUMP:
				break;

			case Mips.MFLO:
				dst = registers[regLo];
				break;
			case Mips.MFHI:
				dst = registers[regHi];
				break;
			case Mips.MTLO:
				registers[regLo] = (int) src1;
				break;
			case Mips.MTHI:
				registers[regHi] = (int) src1;
				break;

			case Mips.SYSCALL:
				throw new MipsException(exceptionSyscall);

			case Mips.LOAD:
				value = readMem(addr, size);

				if (!test(Mips.UNSIGNED))
					dst = Lib.extend(value, 0, size * 8);
				else
					dst = value;

				break;

			case Mips.LWL:
				value = readMem(addr & ~0x3, 4);

				// LWL shifts the input left so the addressed byte is highest
				preserved = (3 - (addr & 0x3)) * 8; // number of bits to
													// preserve
				mask = -1 << preserved; // preserved bits are 0 in mask
				dst = value << preserved; // shift input to correct place
				addr &= ~0x3;

				break;

			case Mips.LWR:
				value = readMem(addr & ~0x3, 4);

				// LWR shifts the input right so the addressed byte is lowest
				preserved = (addr & 0x3) * 8; // number of bits to preserve
				mask = -1 >>> preserved; // preserved bits are 0 in mask
				dst = value >>> preserved; // shift input to correct place
				addr &= ~0x3;

				break;

			case Mips.STORE:
				writeMem(addr, size, (int) src2);
				break;

			case Mips.SWL:
				value = readMem(addr & ~0x3, 4);

				// SWL shifts highest order byte into the addressed position
				preserved = (3 - (addr & 0x3)) * 8;
				mask = -1 >>> preserved;
				dst = src2 >>> preserved;

				// merge values
				dst = (dst & mask) | (value & ~mask);

				writeMem(addr & ~0x3, 4, (int) dst);
				break;

			case Mips.SWR:
				value = readMem(addr & ~0x3, 4);

				// SWR shifts the lowest order byte into the addressed position
				preserved = (addr & 0x3) * 8;
				mask = -1 << preserved;
				dst = src2 << preserved;

				// merge values
				dst = (dst & mask) | (value & ~mask);

				writeMem(addr & ~0x3, 4, (int) dst);
				break;

			case Mips.UNIMPL:
				System.err.println("Warning: encountered unimplemented inst");

			case Mips.INVALID:
				throw new MipsException(exceptionIllegalInstruction);

			default:
				Lib.assertNotReached();
			}
		}

		private void writeBack() throws MipsException {
			// if instruction is signed, but carry bit !+ sign bit, throw
			if (test(Mips.OVERFLOW) && Lib.test(dst, 31) != Lib.test(dst, 32))
				throw new MipsException(exceptionOverflow);

			if (test(Mips.DELAYEDLOAD))
				delayedLoad(dstReg, (int) dst, mask);
			else
				finishLoad();

			if (test(Mips.LINK))
				dst = nextPC;

			if (test(Mips.DST) && dstReg != 0)
				registers[dstReg] = (int) dst;

			if ((test(Mips.DST) || test(Mips.DELAYEDLOAD)) && dstReg != 0) {
				if (Lib.test(dbgFullDisassemble)) {
					System.out.print("#0x" + Lib.toHexString((int) dst));
					if (test(Mips.DELAYEDLOAD))
						System.out.print(" (delayed load)");
				}
			}

			if (test(Mips.BRANCH) && branch) {
				nextPC = jtarget;
			}

			advancePC(nextPC);

			if ((Lib.test(dbgDisassemble) && !Lib.test(dbgProcessor))
					|| Lib.test(dbgFullDisassemble))
				System.out.print("\n");
		}

		// state used to execute a single instruction
		int value, op, rs, rt, rd, sh, func, target, imm;

		int operation, format, flags;

		String name;

		int size;

		int addr, nextPC, jtarget, dstReg;

		long src1, src2, dst;

		int mask;

		boolean branch;
	}

	private static class Mips {
		Mips() {
		}

		Mips(int operation, String name) {
			this.operation = operation;
			this.name = name;
		}

		Mips(int operation, String name, int format, int flags) {
			this(operation, name);
			this.format = format;
			this.flags = flags;
		}

		int operation = INVALID;

		String name = "invalid ";

		int format;

		int flags;

		// operation types
		static final int INVALID = 0, UNIMPL = 1, ADD = 2, SUB = 3, MULT = 4,
				DIV = 5, SLL = 6, SRA = 7, SRL = 8, SLT = 9, AND = 10, OR = 11,
				NOR = 12, XOR = 13, LUI = 14, MFLO = 21, MFHI = 22, MTLO = 23,
				MTHI = 24, JUMP = 25, BEQ = 26, BNE = 27, BLEZ = 28, BGTZ = 29,
				BLTZ = 30, BGEZ = 31, SYSCALL = 32, LOAD = 33, LWL = 36,
				LWR = 37, STORE = 38, SWL = 39, SWR = 40, MAX = 40;

		static final int IFMT = 1, JFMT = 2, RFMT = 3;

		static final int DST = 0x00000001, DSTRA = 0x00000002,
				OVERFLOW = 0x00000004, SRC1SH = 0x00000008,
				SRC2IMM = 0x00000010, UNSIGNED = 0x00000020, LINK = 0x00000040,
				DELAYEDLOAD = 0x00000080, SIZEB = 0x00000100,
				SIZEH = 0x00000200, SIZEW = 0x00000400, BRANCH = 0x00000800;

		static final char RS = 's', RT = 't', RD = 'd', IMM = 'i',
				SHIFTAMOUNT = 'h', ADDR = 'a', // imm(rs)
				TARGET = 'j', RETURNADDRESS = 'r'; // rd, or none if rd=31;
													// can't be last

		static final Mips[] optable = {
				new Mips(), // special
				new Mips(), // reg-imm
				new Mips(JUMP, "j j", JFMT, BRANCH),
				new Mips(JUMP, "jal j", JFMT, BRANCH | LINK | DST | DSTRA),
				new Mips(BEQ, "beq stj", IFMT, BRANCH),
				new Mips(BNE, "bne stj", IFMT, BRANCH),
				new Mips(BLEZ, "blez sj", IFMT, BRANCH),
				new Mips(BGTZ, "bgtz sj", IFMT, BRANCH),
				new Mips(ADD, "addi tsi", IFMT, DST | SRC2IMM | OVERFLOW),
				new Mips(ADD, "addiu tsi", IFMT, DST | SRC2IMM),
				new Mips(SLT, "slti tsi", IFMT, DST | SRC2IMM),
				new Mips(SLT, "sltiu tsi", IFMT, DST | SRC2IMM | UNSIGNED),
				new Mips(AND, "andi tsi", IFMT, DST | SRC2IMM | UNSIGNED),
				new Mips(OR, "ori tsi", IFMT, DST | SRC2IMM | UNSIGNED),
				new Mips(XOR, "xori tsi", IFMT, DST | SRC2IMM | UNSIGNED),
				new Mips(LUI, "lui ti", IFMT, DST | SRC2IMM | UNSIGNED),
				new Mips(), new Mips(), new Mips(), new Mips(),
				new Mips(BEQ, "beql stj", IFMT, BRANCH),
				new Mips(BNE, "bnel stj", IFMT, BRANCH),
				new Mips(BLEZ, "blezl sj", IFMT, BRANCH),
				new Mips(BGTZ, "bgtzl sj", IFMT, BRANCH), new Mips(),
				new Mips(), new Mips(), new Mips(), new Mips(), new Mips(),
				new Mips(), new Mips(),
				new Mips(LOAD, "lb ta", IFMT, DELAYEDLOAD | SIZEB),
				new Mips(LOAD, "lh ta", IFMT, DELAYEDLOAD | SIZEH),
				new Mips(LWL, "lwl ta", IFMT, DELAYEDLOAD),
				new Mips(LOAD, "lw ta", IFMT, DELAYEDLOAD | SIZEW),
				new Mips(LOAD, "lbu ta", IFMT, DELAYEDLOAD | SIZEB | UNSIGNED),
				new Mips(LOAD, "lhu ta", IFMT, DELAYEDLOAD | SIZEH | UNSIGNED),
				new Mips(LWR, "lwr ta", IFMT, DELAYEDLOAD), new Mips(),
				new Mips(STORE, "sb ta", IFMT, SIZEB),
				new Mips(STORE, "sh ta", IFMT, SIZEH),
				new Mips(SWL, "swl ta", IFMT, 0),
				new Mips(STORE, "sw ta", IFMT, SIZEW), new Mips(), new Mips(),
				new Mips(SWR, "swr ta", IFMT, 0), new Mips(),
				new Mips(UNIMPL, "ll "), new Mips(), new Mips(), new Mips(),
				new Mips(), new Mips(), new Mips(), new Mips(),
				new Mips(UNIMPL, "sc "), new Mips(), new Mips(), new Mips(),
				new Mips(), new Mips(), new Mips(), new Mips(), };

		static final Mips[] specialtable = {
				new Mips(SLL, "sll dth", RFMT, DST | SRC1SH), new Mips(),
				new Mips(SRL, "srl dth", RFMT, DST | SRC1SH),
				new Mips(SRA, "sra dth", RFMT, DST | SRC1SH),
				new Mips(SLL, "sllv dts", RFMT, DST), new Mips(),
				new Mips(SRL, "srlv dts", RFMT, DST),
				new Mips(SRA, "srav dts", RFMT, DST),
				new Mips(JUMP, "jr s", RFMT, BRANCH),
				new Mips(JUMP, "jalr rs", RFMT, BRANCH | LINK | DST),
				new Mips(), new Mips(), new Mips(SYSCALL, "syscall "),
				new Mips(UNIMPL, "break "), new Mips(),
				new Mips(UNIMPL, "sync "), new Mips(MFHI, "mfhi d", RFMT, DST),
				new Mips(MTHI, "mthi s", RFMT, 0),
				new Mips(MFLO, "mflo d", RFMT, DST),
				new Mips(MTLO, "mtlo s", RFMT, 0), new Mips(), new Mips(),
				new Mips(), new Mips(), new Mips(MULT, "mult st", RFMT, 0),
				new Mips(MULT, "multu st", RFMT, UNSIGNED),
				new Mips(DIV, "div st", RFMT, 0),
				new Mips(DIV, "divu st", RFMT, UNSIGNED), new Mips(),
				new Mips(), new Mips(), new Mips(),
				new Mips(ADD, "add dst", RFMT, DST | OVERFLOW),
				new Mips(ADD, "addu dst", RFMT, DST),
				new Mips(SUB, "sub dst", RFMT, DST | OVERFLOW),
				new Mips(SUB, "subu dst", RFMT, DST),
				new Mips(AND, "and dst", RFMT, DST),
				new Mips(OR, "or dst", RFMT, DST),
				new Mips(XOR, "xor dst", RFMT, DST),
				new Mips(NOR, "nor dst", RFMT, DST), new Mips(), new Mips(),
				new Mips(SLT, "slt dst", RFMT, DST),
				new Mips(SLT, "sltu dst", RFMT, DST | UNSIGNED), new Mips(),
				new Mips(), new Mips(), new Mips(), new Mips(), new Mips(),
				new Mips(), new Mips(), new Mips(), new Mips(), new Mips(),
				new Mips(), new Mips(), new Mips(), new Mips(), new Mips(),
				new Mips(), new Mips(), new Mips(), new Mips(), };

		static final Mips[] regimmtable = {
				new Mips(BLTZ, "bltz sj", IFMT, BRANCH),
				new Mips(BGEZ, "bgez sj", IFMT, BRANCH),
				new Mips(BLTZ, "bltzl sj", IFMT, BRANCH),
				new Mips(BGEZ, "bgezl sj", IFMT, BRANCH),
				new Mips(),
				new Mips(),
				new Mips(),
				new Mips(),
				new Mips(),
				new Mips(),
				new Mips(),
				new Mips(),
				new Mips(),
				new Mips(),
				new Mips(),
				new Mips(),
				new Mips(BLTZ, "bltzal sj", IFMT, BRANCH | LINK | DST | DSTRA),
				new Mips(BGEZ, "bgezal sj", IFMT, BRANCH | LINK | DST | DSTRA),
				new Mips(BLTZ, "bltzlal sj", IFMT, BRANCH | LINK | DST | DSTRA),
				new Mips(BGEZ, "bgezlal sj", IFMT, BRANCH | LINK | DST | DSTRA),
				new Mips(), new Mips(), new Mips(), new Mips(), new Mips(),
				new Mips(), new Mips(), new Mips(), new Mips(), new Mips(),
				new Mips(), new Mips() };
	}
}
