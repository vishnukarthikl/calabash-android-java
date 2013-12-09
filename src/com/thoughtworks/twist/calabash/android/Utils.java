/**
 *
 */
package com.thoughtworks.twist.calabash.android;

import org.jruby.RubyArray;
import org.jruby.RubyHash;
import org.jruby.RubyObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

final class Utils {

    public static String getStringFromHash(RubyHash target, String key) {
        try {
            Object value = target.get(key);
            if (value != null)
                return value.toString();
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static Integer getIntFromHash(RubyHash target, String key) {
        String value = getStringFromHash(target, key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }

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


    public static void inspectElement(TreeNode node, int nestingLevel, InspectCallback callback) {
        callback.onEachElement(node.getData(), nestingLevel);
        for (TreeNode childNode : node.getChildren()) {
            inspectElement(childNode, nestingLevel + 1, callback);
        }
    }

    public static String runCommand(String[] command, String onExceptionMessage) throws CalabashException {
        int exitCode;
        try {
            Process process = executeCommand(command);
            exitCode = process.waitFor();
            String error = toString(process.getErrorStream());
            String output = toString(process.getInputStream());
            CalabashLogger.info(output);

            if (exitCode == 0) {
                return output;
            } else {
                CalabashLogger.error("Executing command failed");
                CalabashLogger.info(output);
                CalabashLogger.error(error);
                throw new CalabashException(onExceptionMessage);
            }
        } catch (Exception e) {
            throw new CalabashException(onExceptionMessage);
        }
    }

    public static String runCommand(String[] command) throws CalabashException {
        Process process;
        try {
            process = executeCommand(command);
            process.waitFor();
            String error = toString(process.getErrorStream());
            String output = toString(process.getInputStream());
            CalabashLogger.info(output);
            CalabashLogger.error(error);
            return output;
        } catch (Exception e) {
            throw new CalabashException(String.format("Failed to execute command %s", getCommandString(command)), e);
        }
    }

    public static Process runCommandInBackGround(String[] command, String onExceptionMessage) throws CalabashException {
        try {
            return executeCommand(command);
        } catch (Exception e) {
            throw new CalabashException(onExceptionMessage);
        }

    }

    private static Process executeCommand(String[] command) throws Exception {
        String cmd = getCommandString(command);
        CalabashLogger.info("Executing command");
        CalabashLogger.info(cmd);
        return Runtime.getRuntime().exec(command);
    }

    private static String getCommandString(String[] command) {
        return Arrays.toString(command).replaceAll("\\[|,|]", "");
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

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
}
