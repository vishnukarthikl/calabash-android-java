package com.thoughtworks.calabash.android;

import com.thoughtworks.calabash.android.AndroidBridge;
import com.thoughtworks.calabash.android.AndroidConfiguration;
import com.thoughtworks.calabash.android.CalabashException;
import com.thoughtworks.calabash.android.Environment;
import com.thoughtworks.calabash.android.EnvironmentInitializer;

/**
 * Sometimes "set_text" doesn't work properly in Calabash, so we need to
 * simulate the behavior of the physical keyboard. We can do this through ADB:
 * 
 * http://krazyrobot.com/2014/02/calabash-android-enter-text-from-keyboard-using
 * -adb/
 * 
 * @author Mike Chabot
 * 
 */
public class Keyboard {

	private static AndroidBridge bridge;

	public Keyboard(AndroidConfiguration androidConfiguration) {
		try {
			Environment environment = EnvironmentInitializer.initialize(androidConfiguration);
			bridge = new AndroidBridge(environment);
		} catch (CalabashException e) {
			System.out.println("Unable to initialize environment");
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Send text input to the device
	 * 
	 * @param text
	 */
	public void writeTextEvent(String text) {
		try {
			bridge.sendTextEvent(text);
			pause();
		} catch (CalabashException e) {
			System.out.println("Unable to send text event");
		}
	}

	/**
	 * Send text input to the device
	 * 
	 * @param text
	 */
	public void writeTextEvent(int integer) {
		try {
			bridge.sendTextEvent(String.valueOf(integer));
			pause();
		} catch (CalabashException e) {
			System.out.println("Unable to send text event");
		}
	}

	/**
	 * Send key strokes to the device
	 * 
	 * @param event
	 */
	public void writeKeyEvent(String event) {
		try {
			bridge.sendKeyEvent(event);
			pause();
		} catch (CalabashException e) {
			System.out.println("Unable to send text event");
		}
	}

	/**
	 * Pause the thread
	 */
	private void pause() {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			System.out.println("Error pausing keyboard");
		}
	}

	public void pressBackspace() {
		writeKeyEvent("KEYCODE_DEL");
	}
}