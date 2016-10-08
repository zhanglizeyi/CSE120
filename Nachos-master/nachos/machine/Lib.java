// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.PrivilegedAction;
import java.util.Random;

/**
 * Thrown when an assertion fails.
 */
class AssertionFailureError extends Error {
	AssertionFailureError() {
		super();
	}

	AssertionFailureError(String message) {
		super(message);
	}
}

/**
 * Provides miscellaneous library routines.
 */
public final class Lib {
	/**
	 * Prevent instantiation.
	 */
	private Lib() {
	}

	private static Random random = null;

	/**
	 * Seed the random number generater. May only be called once.
	 * 
	 * @param randomSeed the seed for the random number generator.
	 */
	public static void seedRandom(long randomSeed) {
		assertTrue(random == null);
		random = new Random(randomSeed);
	}

	/**
	 * Return a random integer between 0 and <i>range - 1</i>. Must not be
	 * called before <tt>seedRandom()</tt> seeds the random number generator.
	 * 
	 * @param range a positive value specifying the number of possible return
	 * values.
	 * @return a random integer in the specified range.
	 */
	public static int random(int range) {
		assertTrue(range > 0);
		return random.nextInt(range);
	}

	/**
	 * Return a random double between 0.0 (inclusive) and 1.0 (exclusive).
	 * 
	 * @return a random double between 0.0 and 1.0.
	 */
	public static double random() {
		return random.nextDouble();
	}

	/**
	 * Asserts that <i>expression</i> is <tt>true</tt>. If not, then Nachos
	 * exits with an error message.
	 * 
	 * @param expression the expression to assert.
	 */
	public static void assertTrue(boolean expression) {
		if (!expression)
			throw new AssertionFailureError();
	}

	/**
	 * Asserts that <i>expression</i> is <tt>true</tt>. If not, then Nachos
	 * exits with the specified error message.
	 * 
	 * @param expression the expression to assert.
	 * @param message the error message.
	 */
	public static void assertTrue(boolean expression, String message) {
		if (!expression)
			throw new AssertionFailureError(message);
	}

	/**
	 * Asserts that this call is never made. Same as <tt>assertTrue(false)</tt>.
	 */
	public static void assertNotReached() {
		assertTrue(false);
	}

	/**
	 * Asserts that this call is never made, with the specified error messsage.
	 * Same as <tt>assertTrue(false, message)</tt>.
	 * 
	 * @param message the error message.
	 */
	public static void assertNotReached(String message) {
		assertTrue(false, message);
	}

	/**
	 * Print <i>message</i> if <i>flag</i> was enabled on the command line. To
	 * specify which flags to enable, use the -d command line option. For
	 * example, to enable flags a, c, and e, do the following:
	 * 
	 * <p>
	 * 
	 * <pre>
	 * nachos -d ace
	 * </pre>
	 * 
	 * <p>
	 * Nachos uses several debugging flags already, but you are encouraged to
	 * add your own.
	 * 
	 * @param flag the debug flag that must be set to print this message.
	 * @param message the debug message.
	 */
	public static void debug(char flag, String message) {
		if (test(flag))
			System.out.println(message);
	}

	/**
	 * Tests if <i>flag</i> was enabled on the command line.
	 * 
	 * @param flag the debug flag to test.
	 * 
	 * @return <tt>true</tt> if this flag was enabled on the command line.
	 */
	public static boolean test(char flag) {
		if (debugFlags == null)
			return false;
		else if (debugFlags[(int) '+'])
			return true;
		else if (flag >= 0 && flag < 0x80 && debugFlags[(int) flag])
			return true;
		else
			return false;
	}

	/**
	 * Enable all the debug flags in <i>flagsString</i>.
	 * 
	 * @param flagsString the flags to enable.
	 */
	public static void enableDebugFlags(String flagsString) {
		if (debugFlags == null)
			debugFlags = new boolean[0x80];

		char[] newFlags = flagsString.toCharArray();
		for (int i = 0; i < newFlags.length; i++) {
			char c = newFlags[i];
			if (c >= 0 && c < 0x80)
				debugFlags[(int) c] = true;
		}
	}

	/** Debug flags specified on the command line. */
	private static boolean debugFlags[];

	/**
	 * Read a file, verifying that the requested number of bytes is read, and
	 * verifying that the read operation took a non-zero amount of time.
	 * 
	 * @param file the file to read.
	 * @param position the file offset at which to start reading.
	 * @param buf the buffer in which to store the data.
	 * @param offset the buffer offset at which storing begins.
	 * @param length the number of bytes to read.
	 */
	public static void strictReadFile(OpenFile file, int position, byte[] buf,
			int offset, int length) {
		long startTime = Machine.timer().getTime();
		assertTrue(file.read(position, buf, offset, length) == length);
		long finishTime = Machine.timer().getTime();
		assertTrue(finishTime > startTime);
	}

	/**
	 * Load an entire file into memory.
	 * 
	 * @param file the file to load.
	 * @return an array containing the contents of the entire file, or
	 * <tt>null</tt> if an error occurred.
	 */
	public static byte[] loadFile(OpenFile file) {
		int startOffset = file.tell();

		int length = file.length();
		if (length < 0)
			return null;

		byte[] data = new byte[length];

		file.seek(0);
		int amount = file.read(data, 0, length);
		file.seek(startOffset);

		if (amount == length)
			return data;
		else
			return null;
	}

	/**
	 * Take a read-only snapshot of a file.
	 * 
	 * @param file the file to take a snapshot of.
	 * @return a read-only snapshot of the file.
	 */
	public static OpenFile cloneFile(OpenFile file) {
		OpenFile clone = new ArrayFile(loadFile(file));

		clone.seek(file.tell());

		return clone;
	}

	/**
	 * Convert a short into its little-endian byte string representation.
	 * 
	 * @param array the array in which to store the byte string.
	 * @param offset the offset in the array where the string will start.
	 * @param value the value to convert.
	 */
	public static void bytesFromShort(byte[] array, int offset, short value) {
		array[offset + 0] = (byte) ((value >> 0) & 0xFF);
		array[offset + 1] = (byte) ((value >> 8) & 0xFF);
	}

	/**
	 * Convert an int into its little-endian byte string representation.
	 * 
	 * @param array the array in which to store the byte string.
	 * @param offset the offset in the array where the string will start.
	 * @param value the value to convert.
	 */
	public static void bytesFromInt(byte[] array, int offset, int value) {
		array[offset + 0] = (byte) ((value >> 0) & 0xFF);
		array[offset + 1] = (byte) ((value >> 8) & 0xFF);
		array[offset + 2] = (byte) ((value >> 16) & 0xFF);
		array[offset + 3] = (byte) ((value >> 24) & 0xFF);
	}

	/**
	 * Convert an int into its little-endian byte string representation, and
	 * return an array containing it.
	 * 
	 * @param value the value to convert.
	 * @return an array containing the byte string.
	 */
	public static byte[] bytesFromInt(int value) {
		byte[] array = new byte[4];
		bytesFromInt(array, 0, value);
		return array;
	}

	/**
	 * Convert an int into a little-endian byte string representation of the
	 * specified length.
	 * 
	 * @param array the array in which to store the byte string.
	 * @param offset the offset in the array where the string will start.
	 * @param length the number of bytes to store (must be 1, 2, or 4).
	 * @param value the value to convert.
	 */
	public static void bytesFromInt(byte[] array, int offset, int length,
			int value) {
		assertTrue(length == 1 || length == 2 || length == 4);

		switch (length) {
		case 1:
			array[offset] = (byte) value;
			break;
		case 2:
			bytesFromShort(array, offset, (short) value);
			break;
		case 4:
			bytesFromInt(array, offset, value);
			break;
		}
	}

	/**
	 * Convert to a short from its little-endian byte string representation.
	 * 
	 * @param array the array containing the byte string.
	 * @param offset the offset of the byte string in the array.
	 * @return the corresponding short value.
	 */
	public static short bytesToShort(byte[] array, int offset) {
		return (short) ((((short) array[offset + 0] & 0xFF) << 0) | (((short) array[offset + 1] & 0xFF) << 8));
	}

	/**
	 * Convert to an unsigned short from its little-endian byte string
	 * representation.
	 * 
	 * @param array the array containing the byte string.
	 * @param offset the offset of the byte string in the array.
	 * @return the corresponding short value.
	 */
	public static int bytesToUnsignedShort(byte[] array, int offset) {
		return (((int) bytesToShort(array, offset)) & 0xFFFF);
	}

	/**
	 * Convert to an int from its little-endian byte string representation.
	 * 
	 * @param array the array containing the byte string.
	 * @param offset the offset of the byte string in the array.
	 * @return the corresponding int value.
	 */
	public static int bytesToInt(byte[] array, int offset) {
		return (int) ((((int) array[offset + 0] & 0xFF) << 0)
				| (((int) array[offset + 1] & 0xFF) << 8)
				| (((int) array[offset + 2] & 0xFF) << 16) | (((int) array[offset + 3] & 0xFF) << 24));
	}

	/**
	 * Convert to an int from a little-endian byte string representation of the
	 * specified length.
	 * 
	 * @param array the array containing the byte string.
	 * @param offset the offset of the byte string in the array.
	 * @param length the length of the byte string.
	 * @return the corresponding value.
	 */
	public static int bytesToInt(byte[] array, int offset, int length) {
		assertTrue(length == 1 || length == 2 || length == 4);

		switch (length) {
		case 1:
			return array[offset];
		case 2:
			return bytesToShort(array, offset);
		case 4:
			return bytesToInt(array, offset);
		default:
			return -1;
		}
	}

	/**
	 * Convert to a string from a possibly null-terminated array of bytes.
	 * 
	 * @param array the array containing the byte string.
	 * @param offset the offset of the byte string in the array.
	 * @param length the maximum length of the byte string.
	 * @return a string containing the specified bytes, up to and not including
	 * the null-terminator (if present).
	 */
	public static String bytesToString(byte[] array, int offset, int length) {
		int i;
		for (i = 0; i < length; i++) {
			if (array[offset + i] == 0)
				break;
		}

		return new String(array, offset, i);
	}

	/**
	 * Mask out and shift a bit substring.
	 * 
	 * @param bits the bit string.
	 * @param lowest the first bit of the substring within the string.
	 * @param size the number of bits in the substring.
	 * @return the substring.
	 */
	public static int extract(int bits, int lowest, int size) {
		if (size == 32)
			return (bits >> lowest);
		else
			return ((bits >> lowest) & ((1 << size) - 1));
	}

	/**
	 * Mask out and shift a bit substring.
	 * 
	 * @param bits the bit string.
	 * @param lowest the first bit of the substring within the string.
	 * @param size the number of bits in the substring.
	 * @return the substring.
	 */
	public static long extract(long bits, int lowest, int size) {
		if (size == 64)
			return (bits >> lowest);
		else
			return ((bits >> lowest) & ((1L << size) - 1));
	}

	/**
	 * Mask out and shift a bit substring; then sign extend the substring.
	 * 
	 * @param bits the bit string.
	 * @param lowest the first bit of the substring within the string.
	 * @param size the number of bits in the substring.
	 * @return the substring, sign-extended.
	 */
	public static int extend(int bits, int lowest, int size) {
		int extra = 32 - (lowest + size);
		return ((extract(bits, lowest, size) << extra) >> extra);
	}

	/**
	 * Test if a bit is set in a bit string.
	 * 
	 * @param flag the flag to test.
	 * @param bits the bit string.
	 * @return <tt>true</tt> if <tt>(bits & flag)</tt> is non-zero.
	 */
	public static boolean test(long flag, long bits) {
		return ((bits & flag) != 0);
	}

	/**
	 * Creates a padded upper-case string representation of the integer argument
	 * in base 16.
	 * 
	 * @param i an integer.
	 * @return a padded upper-case string representation in base 16.
	 */
	public static String toHexString(int i) {
		return toHexString(i, 8);
	}

	/**
	 * Creates a padded upper-case string representation of the integer argument
	 * in base 16, padding to at most the specified number of digits.
	 * 
	 * @param i an integer.
	 * @param pad the minimum number of hex digits to pad to.
	 * @return a padded upper-case string representation in base 16.
	 */
	public static String toHexString(int i, int pad) {
		String result = Integer.toHexString(i).toUpperCase();
		while (result.length() < pad)
			result = "0" + result;
		return result;
	}

	/**
	 * Divide two non-negative integers, round the quotient up to the nearest
	 * integer, and return it.
	 * 
	 * @param a the numerator.
	 * @param b the denominator.
	 * @return <tt>ceiling(a / b)</tt>.
	 */
	public static int divRoundUp(int a, int b) {
		assertTrue(a >= 0 && b > 0);

		return ((a + (b - 1)) / b);
	}

	/**
	 * Load and return the named class, or return <tt>null</tt> if the class
	 * could not be loaded.
	 * 
	 * @param className the name of the class to load.
	 * @return the loaded class, or <tt>null</tt> if an error occurred.
	 */
	public static Class tryLoadClass(String className) {
		try {
			return ClassLoader.getSystemClassLoader().loadClass(className);
		}
		catch (Throwable e) {
			return null;
		}
	}

	/**
	 * Load and return the named class, terminating Nachos on any error.
	 * 
	 * @param className the name of the class to load.
	 * @return the loaded class.
	 */
	public static Class loadClass(String className) {
		try {
			return ClassLoader.getSystemClassLoader().loadClass(className);
		}
		catch (Throwable e) {
			Machine.terminate(e);
			return null;
		}
	}

	/**
	 * Create and return a new instance of the named class, using the
	 * constructor that takes no arguments.
	 * 
	 * @param className the name of the class to instantiate.
	 * @return a new instance of the class.
	 */
	public static Object constructObject(String className) {
		try {
			// kamil - workaround for Java 1.4
			// Thanks to Ka-Hing Cheung for the suggestion.
			// Fixed for Java 1.5 by geels
			Class[] param_types = new Class[0];
			Object[] params = new Object[0];
			return loadClass(className).getConstructor(param_types)
					.newInstance(params);
		}
		catch (Throwable e) {
			Machine.terminate(e);
			return null;
		}
	}

	/**
	 * Verify that the specified class extends or implements the specified
	 * superclass.
	 * 
	 * @param cls the descendant class.
	 * @param superCls the ancestor class.
	 */
	public static void checkDerivation(Class<?> cls, Class<?> superCls) {
		Lib.assertTrue(superCls.isAssignableFrom(cls));
	}

	/**
	 * Verifies that the specified class is public and not abstract, and that a
	 * constructor with the specified signature exists and is public.
	 * 
	 * @param cls the class containing the constructor.
	 * @param parameterTypes the list of parameters.
	 */
	public static void checkConstructor(Class cls, Class[] parameterTypes) {
		try {
			Lib.assertTrue(Modifier.isPublic(cls.getModifiers())
					&& !Modifier.isAbstract(cls.getModifiers()));
			Constructor constructor = cls.getConstructor(parameterTypes);
			Lib.assertTrue(Modifier.isPublic(constructor.getModifiers()));
		}
		catch (Exception e) {
			Lib.assertNotReached();
		}
	}

	/**
	 * Verifies that the specified class is public, and that a non-static method
	 * with the specified name and signature exists, is public, and returns the
	 * specified type.
	 * 
	 * @param cls the class containing the non-static method.
	 * @param methodName the name of the non-static method.
	 * @param parameterTypes the list of parameters.
	 * @param returnType the required return type.
	 */
	public static void checkMethod(Class cls, String methodName,
			Class[] parameterTypes, Class returnType) {
		try {
			Lib.assertTrue(Modifier.isPublic(cls.getModifiers()));
			Method method = cls.getMethod(methodName, parameterTypes);
			Lib.assertTrue(Modifier.isPublic(method.getModifiers())
					&& !Modifier.isStatic(method.getModifiers()));
			Lib.assertTrue(method.getReturnType() == returnType);
		}
		catch (Exception e) {
			Lib.assertNotReached();
		}
	}

	/**
	 * Verifies that the specified class is public, and that a static method
	 * with the specified name and signature exists, is public, and returns the
	 * specified type.
	 * 
	 * @param cls the class containing the static method.
	 * @param methodName the name of the static method.
	 * @param parameterTypes the list of parameters.
	 * @param returnType the required return type.
	 */
	public static void checkStaticMethod(Class cls, String methodName,
			Class[] parameterTypes, Class returnType) {
		try {
			Lib.assertTrue(Modifier.isPublic(cls.getModifiers()));
			Method method = cls.getMethod(methodName, parameterTypes);
			Lib.assertTrue(Modifier.isPublic(method.getModifiers())
					&& Modifier.isStatic(method.getModifiers()));
			Lib.assertTrue(method.getReturnType() == returnType);
		}
		catch (Exception e) {
			Lib.assertNotReached();
		}
	}

	/**
	 * Verifies that the specified class is public, and that a non-static field
	 * with the specified name and type exists, is public, and is not final.
	 * 
	 * @param cls the class containing the field.
	 * @param fieldName the name of the field.
	 * @param fieldType the required type.
	 */
	public static void checkField(Class cls, String fieldName, Class fieldType) {
		try {
			Lib.assertTrue(Modifier.isPublic(cls.getModifiers()));
			Field field = cls.getField(fieldName);
			Lib.assertTrue(field.getType() == fieldType);
			Lib.assertTrue(Modifier.isPublic(field.getModifiers())
					&& !Modifier.isStatic(field.getModifiers())
					&& !Modifier.isFinal(field.getModifiers()));
		}
		catch (Exception e) {
			Lib.assertNotReached();
		}
	}

	/**
	 * Verifies that the specified class is public, and that a static field with
	 * the specified name and type exists and is public.
	 * 
	 * @param cls the class containing the static field.
	 * @param fieldName the name of the static field.
	 * @param fieldType the required type.
	 */
	public static void checkStaticField(Class cls, String fieldName,
			Class fieldType) {
		try {
			Lib.assertTrue(Modifier.isPublic(cls.getModifiers()));
			Field field = cls.getField(fieldName);
			Lib.assertTrue(field.getType() == fieldType);
			Lib.assertTrue(Modifier.isPublic(field.getModifiers())
					&& Modifier.isStatic(field.getModifiers()));
		}
		catch (Exception e) {
			Lib.assertNotReached();
		}
	}
}
