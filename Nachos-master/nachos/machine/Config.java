// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

import java.util.HashMap;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StreamTokenizer;

/**
 * Provides routines to access the Nachos configuration.
 */
public final class Config {
	/**
	 * Load configuration information from the specified file. Must be called
	 * before the Nachos security manager is installed.
	 * 
	 * @param fileName the name of the file containing the configuration to use.
	 */
	public static void load(String fileName) {
		System.out.print(" config");

		Lib.assertTrue(!loaded);
		loaded = true;

		configFile = fileName;

		try {
			config = new HashMap<String, String>();

			File file = new File(configFile);
			Reader reader = new FileReader(file);
			StreamTokenizer s = new StreamTokenizer(reader);

			s.resetSyntax();
			s.whitespaceChars(0x00, 0x20);
			s.wordChars(0x21, 0xFF);
			s.eolIsSignificant(true);
			s.commentChar('#');
			s.quoteChar('"');

			int line = 1;

			s.nextToken();

			while (true) {
				if (s.ttype == StreamTokenizer.TT_EOF)
					break;

				if (s.ttype == StreamTokenizer.TT_EOL) {
					line++;
					s.nextToken();
					continue;
				}

				if (s.ttype != StreamTokenizer.TT_WORD)
					loadError(line);

				String key = s.sval;

				if (s.nextToken() != StreamTokenizer.TT_WORD
						|| !s.sval.equals("="))
					loadError(line);

				if (s.nextToken() != StreamTokenizer.TT_WORD && s.ttype != '"')
					loadError(line);

				String value = s.sval;

				// ignore everything after first string
				while (s.nextToken() != StreamTokenizer.TT_EOL
						&& s.ttype != StreamTokenizer.TT_EOF)
					;

				if (config.get(key) != null)
					loadError(line);

				config.put(key, value);
				line++;
			}
		}
		catch (Throwable e) {
			System.err.println("Error loading " + configFile);
			System.exit(1);
		}
	}

	private static void loadError(int line) {
		System.err.println("Error in " + configFile + " line " + line);
		System.exit(1);
	}

	private static void configError(String message) {
		System.err.println("");
		System.err.println("Error in " + configFile + ": " + message);
		System.exit(1);
	}

	/**
	 * Get the value of a key in <tt>nachos.conf</tt>.
	 * 
	 * @param key the key to look up.
	 * @return the value of the specified key, or <tt>null</tt> if it is not
	 * present.
	 */
	public static String getString(String key) {
		return (String) config.get(key);
	}

	/**
	 * Get the value of a key in <tt>nachos.conf</tt>, returning the specified
	 * default if the key does not exist.
	 * 
	 * @param key the key to look up.
	 * @param defaultValue the value to return if the key does not exist.
	 * @return the value of the specified key, or <tt>defaultValue</tt> if it is
	 * not present.
	 */
	public static String getString(String key, String defaultValue) {
		String result = getString(key);

		if (result == null)
			return defaultValue;

		return result;
	}

	private static Integer requestInteger(String key) {
		try {
			String value = getString(key);
			if (value == null)
				return null;

			return new Integer(value);
		}
		catch (NumberFormatException e) {
			configError(key + " should be an integer");

			Lib.assertNotReached();
			return null;
		}
	}

	/**
	 * Get the value of an integer key in <tt>nachos.conf</tt>.
	 * 
	 * @param key the key to look up.
	 * @return the value of the specified key.
	 */
	public static int getInteger(String key) {
		Integer result = requestInteger(key);

		if (result == null)
			configError("missing int " + key);

		return result.intValue();
	}

	/**
	 * Get the value of an integer key in <tt>nachos.conf</tt>, returning the
	 * specified default if the key does not exist.
	 * 
	 * @param key the key to look up.
	 * @param defaultValue the value to return if the key does not exist.
	 * @return the value of the specified key, or <tt>defaultValue</tt> if the
	 * key does not exist.
	 */
	public static int getInteger(String key, int defaultValue) {
		Integer result = requestInteger(key);

		if (result == null)
			return defaultValue;

		return result.intValue();
	}

	private static Double requestDouble(String key) {
		try {
			String value = getString(key);
			if (value == null)
				return null;

			return new Double(value);
		}
		catch (NumberFormatException e) {
			configError(key + " should be a double");

			Lib.assertNotReached();
			return null;
		}
	}

	/**
	 * Get the value of a double key in <tt>nachos.conf</tt>.
	 * 
	 * @param key the key to look up.
	 * @return the value of the specified key.
	 */
	public static double getDouble(String key) {
		Double result = requestDouble(key);

		if (result == null)
			configError("missing double " + key);

		return result.doubleValue();
	}

	/**
	 * Get the value of a double key in <tt>nachos.conf</tt>, returning the
	 * specified default if the key does not exist.
	 * 
	 * @param key the key to look up.
	 * @param defaultValue the value to return if the key does not exist.
	 * @return the value of the specified key, or <tt>defaultValue</tt> if the
	 * key does not exist.
	 */
	public static double getDouble(String key, double defaultValue) {
		Double result = requestDouble(key);

		if (result == null)
			return defaultValue;

		return result.doubleValue();
	}

	private static Boolean requestBoolean(String key) {
		String value = getString(key);

		if (value == null)
			return null;

		if (value.equals("1") || value.toLowerCase().equals("true")) {
			return Boolean.TRUE;
		}
		else if (value.equals("0") || value.toLowerCase().equals("false")) {
			return Boolean.FALSE;
		}
		else {
			configError(key + " should be a boolean");

			Lib.assertNotReached();
			return null;
		}
	}

	/**
	 * Get the value of a boolean key in <tt>nachos.conf</tt>.
	 * 
	 * @param key the key to look up.
	 * @return the value of the specified key.
	 */
	public static boolean getBoolean(String key) {
		Boolean result = requestBoolean(key);

		if (result == null)
			configError("missing boolean " + key);

		return result.booleanValue();
	}

	/**
	 * Get the value of a boolean key in <tt>nachos.conf</tt>, returning the
	 * specified default if the key does not exist.
	 * 
	 * @param key the key to look up.
	 * @param defaultValue the value to return if the key does not exist.
	 * @return the value of the specified key, or <tt>defaultValue</tt> if the
	 * key does not exist.
	 */
	public static boolean getBoolean(String key, boolean defaultValue) {
		Boolean result = requestBoolean(key);

		if (result == null)
			return defaultValue;

		return result.booleanValue();
	}

	private static boolean loaded = false;

	private static String configFile;

	private static HashMap<String, String> config;
}
