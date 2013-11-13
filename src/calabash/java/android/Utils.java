/**
 *
 */
package calabash.java.android;

import org.jruby.RubyArray;
import org.jruby.RubyHash;
import org.jruby.RubyObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

final class Utils {

    public static Object[] toJavaArray(RubyArray array) {
		ArrayList<Object> result = new ArrayList<Object>();
		for (int i = 0; i < array.size(); i++) {
			Object rubyObject = array.get(i);
			Object javaObject = toJavaObject(rubyObject);
			result.add(javaObject);
		}

		return result.toArray();
	}

	public static Object toJavaObject(Object rubyObject) {
		if (rubyObject == null)
			return rubyObject;

		if (rubyObject instanceof RubyArray)
			return toJavaArray((RubyArray) rubyObject);
		if (rubyObject instanceof RubyHash)
			return toJavaHash((RubyHash) rubyObject);
		if (rubyObject instanceof RubyObject)
			return ((RubyObject) rubyObject).toJava(Object.class);

		return rubyObject.toString();
	}

	public static Map<?, ?> toJavaHash(RubyHash rubyHash) {
		HashMap<Object, Object> map = new HashMap<Object, Object>();
		Set<?> keySet = rubyHash.keySet();
		for (Object rubyKey : keySet) {
			Object rubyValue = rubyHash.get(rubyKey);
			Object javaKey = toJavaObject(rubyKey);
			Object javaValue = toJavaObject(rubyValue);
			map.put(javaKey, javaValue);
		}
		return map;
	}


	public static void runCommand(String[] command, String onExceptionMessage)
			throws CalabashException {
		int exitCode;
		try {
			Process process = Runtime.getRuntime().exec(command);
			exitCode = process.waitFor();
            if (exitCode == 0)
				return;
			else
				throw new CalabashException(onExceptionMessage);
		} catch (Exception e) {
			throw new CalabashException(onExceptionMessage);
		}
	}

	public static String toString(InputStream in) throws CalabashException {
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		StringBuilder sb = new StringBuilder();

		String read;
		try {
			while ((read = br.readLine()) != null) {
				sb.append(read);
			}
		} catch (IOException e) {
			throw new CalabashException("Error reading from stream.", e);
		}

		return sb.toString();
	}

}
