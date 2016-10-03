// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

import nachos.security.*;
import nachos.threads.*;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.IOException;

/**
 * This class implements a file system that redirects all requests to the host
 * operating system's file system.
 */
public class StubFileSystem implements FileSystem {
	/**
	 * Allocate a new stub file system.
	 * 
	 * @param privilege encapsulates privileged access to the Nachos machine.
	 * @param directory the root directory of the stub file system.
	 */
	public StubFileSystem(Privilege privilege, File directory) {
		this.privilege = privilege;
		this.directory = directory;
	}

	public OpenFile open(String name, boolean truncate) {
		if (!checkName(name))
			return null;

		delay();

		try {
			return new StubOpenFile(name, truncate);
		}
		catch (IOException e) {
			return null;
		}
	}

	public boolean remove(String name) {
		if (!checkName(name))
			return false;

		delay();

		FileRemover fr = new FileRemover(new File(directory, name));
		privilege.doPrivileged(fr);
		return fr.successful;
	}

	private class FileRemover implements Runnable {
		public FileRemover(File f) {
			this.f = f;
		}

		public void run() {
			successful = f.delete();
		}

		public boolean successful = false;

		private File f;
	}

	private void delay() {
		long time = Machine.timer().getTime();
		int amount = 1000;
		ThreadedKernel.alarm.waitUntil(amount);
		Lib.assertTrue(Machine.timer().getTime() >= time + amount);
	}

	private class StubOpenFile extends OpenFileWithPosition {
		StubOpenFile(final String name, final boolean truncate)
				throws IOException {
			super(StubFileSystem.this, name);

			final File f = new File(directory, name);

			if (openCount == maxOpenFiles)
				throw new IOException();

			privilege.doPrivileged(new Runnable() {
				public void run() {
					getRandomAccessFile(f, truncate);
				}
			});

			if (file == null)
				throw new IOException();

			open = true;
			openCount++;
		}

		private void getRandomAccessFile(File f, boolean truncate) {
			try {
				if (!truncate && !f.exists())
					return;

				file = new RandomAccessFile(f, "rw");

				if (truncate)
					file.setLength(0);
			}
			catch (IOException e) {
			}
		}

		public int read(int pos, byte[] buf, int offset, int length) {
			if (!open)
				return -1;

			try {
				delay();

				file.seek(pos);
				return Math.max(0, file.read(buf, offset, length));
			}
			catch (IOException e) {
				return -1;
			}
		}

		public int write(int pos, byte[] buf, int offset, int length) {
			if (!open)
				return -1;

			try {
				delay();

				file.seek(pos);
				file.write(buf, offset, length);
				return length;
			}
			catch (IOException e) {
				return -1;
			}
		}

		public int length() {
			try {
				return (int) file.length();
			}
			catch (IOException e) {
				return -1;
			}
		}

		public void close() {
			if (open) {
				open = false;
				openCount--;
			}

			try {
				file.close();
			}
			catch (IOException e) {
			}
		}

		private RandomAccessFile file = null;

		private boolean open = false;
	}

	private int openCount = 0;

	private static final int maxOpenFiles = 16;

	private Privilege privilege;

	private File directory;

	private static boolean checkName(String name) {
		char[] chars = name.toCharArray();

		for (int i = 0; i < chars.length; i++) {
			if (chars[i] < 0 || chars[i] >= allowedFileNameCharacters.length)
				return false;
			if (!allowedFileNameCharacters[(int) chars[i]])
				return false;
		}
		return true;
	}

	private static boolean[] allowedFileNameCharacters = new boolean[0x80];

	private static void reject(char c) {
		allowedFileNameCharacters[c] = false;
	}

	private static void allow(char c) {
		allowedFileNameCharacters[c] = true;
	}

	private static void reject(char first, char last) {
		for (char c = first; c <= last; c++)
			allowedFileNameCharacters[c] = false;
	}

	private static void allow(char first, char last) {
		for (char c = first; c <= last; c++)
			allowedFileNameCharacters[c] = true;
	}

	static {
		reject((char) 0x00, (char) 0x7F);

		allow('A', 'Z');
		allow('a', 'z');
		allow('0', '9');

		allow('-');
		allow('_');
		allow('.');
		allow(',');
	}
}
