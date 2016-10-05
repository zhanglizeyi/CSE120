// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

import java.io.EOFException;

/**
 * A COFF (common object file format) loader.
 */
public class Coff {
	/**
	 * Allocate a new Coff object.
	 */
	protected Coff() {
		file = null;
		entryPoint = 0;
		sections = null;
	}

	/**
	 * Load the COFF executable in the specified file.
	 * 
	 * <p>
	 * Notes:
	 * <ol>
	 * <li>If the constructor returns successfully, the file becomes the
	 * property of this loader, and should not be accessed any further.
	 * <li>The autograder expects this loader class to be used. Do not load
	 * sections through any other mechanism.
	 * <li>This loader will verify that the file is backed by a file system, by
	 * asserting that read() operations take non-zero simulated time to
	 * complete. Do not supply a file backed by a simulated cache (the primary
	 * purpose of this restriction is to prevent sections from being loaded
	 * instantaneously while handling page faults).
	 * </ol>
	 * 
	 * @param file the file containing the executable.
	 * @exception EOFException if the executable is corrupt.
	 */
	public Coff(OpenFile file) throws EOFException {
		this.file = file;

		Coff coff = Machine.autoGrader().createLoader(file);

		if (coff != null) {
			this.entryPoint = coff.entryPoint;
			this.sections = coff.sections;
		}
		else {
			byte[] headers = new byte[headerLength + aoutHeaderLength];

			if (file.length() < headers.length) {
				Lib.debug(dbgCoff, "\tfile is not executable");
				throw new EOFException();
			}

			Lib.strictReadFile(file, 0, headers, 0, headers.length);

			int magic = Lib.bytesToUnsignedShort(headers, 0);
			int numSections = Lib.bytesToUnsignedShort(headers, 2);
			int optionalHeaderLength = Lib.bytesToUnsignedShort(headers, 16);
			int flags = Lib.bytesToUnsignedShort(headers, 18);
			entryPoint = Lib.bytesToInt(headers, headerLength + 16);

			if (magic != 0x0162) {
				Lib.debug(dbgCoff, "\tincorrect magic number");
				throw new EOFException();
			}
			if (numSections < 2 || numSections > 10) {
				Lib.debug(dbgCoff, "\tbad section count");
				throw new EOFException();
			}
			if ((flags & 0x0003) != 0x0003) {
				Lib.debug(dbgCoff, "\tbad header flags");
				throw new EOFException();
			}

			int offset = headerLength + optionalHeaderLength;

			sections = new CoffSection[numSections];
			for (int s = 0; s < numSections; s++) {
				int sectionEntryOffset = offset + s * CoffSection.headerLength;
				try {
					sections[s] = new CoffSection(file, this,
							sectionEntryOffset);
				}
				catch (EOFException e) {
					Lib.debug(dbgCoff, "\terror loading section " + s);
					throw e;
				}
			}
		}
	}

	/**
	 * Return the number of sections in the executable.
	 * 
	 * @return the number of sections in the executable.
	 */
	public int getNumSections() {
		return sections.length;
	}

	/**
	 * Return an object that can be used to access the specified section. Valid
	 * section numbers include <tt>0</tt> through <tt>getNumSections() -
	 * 1</tt>.
	 * 
	 * @param sectionNumber the section to select.
	 * @return an object that can be used to access the specified section.
	 */
	public CoffSection getSection(int sectionNumber) {
		Lib.assertTrue(sectionNumber >= 0 && sectionNumber < sections.length);

		return sections[sectionNumber];
	}

	/**
	 * Return the program entry point. This is the value that to which the PC
	 * register should be initialized to before running the program.
	 * 
	 * @return the program entry point.
	 */
	public int getEntryPoint() {
		Lib.assertTrue(file != null);

		return entryPoint;
	}

	/**
	 * Close the executable file and release any resources allocated by this
	 * loader.
	 */
	public void close() {
		file.close();

		sections = null;
	}

	private OpenFile file;

	/** The virtual address of the first instruction of the program. */
	protected int entryPoint;

	/** The sections in this COFF executable. */
	protected CoffSection sections[];

	private static final int headerLength = 20;

	private static final int aoutHeaderLength = 28;

	private static final char dbgCoff = 'c';
}
