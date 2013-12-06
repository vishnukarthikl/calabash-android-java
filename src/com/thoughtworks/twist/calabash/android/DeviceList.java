package com.thoughtworks.twist.calabash.android;

import java.util.ArrayList;
import java.util.List;

public class DeviceList {
    List<Device> devices = new ArrayList<Device>();
    public static final String[] STATES = new String[]{"no device", "device", "offline"};
    public static final String HEADER = "List of devices attached ";

    public DeviceList(String outputFromAdbDeviceList) {
        parseForDevices(outputFromAdbDeviceList);
    }

    private void parseForDevices(String outputFromAdbDeviceList) {
        int startIndex = outputFromAdbDeviceList.indexOf(HEADER) + HEADER.length();
        String devices = outputFromAdbDeviceList.substring(startIndex);

        String[] devicesNameSerialList = devices.split("\t");

        String nextSerial = devicesNameSerialList[0];
        for (int i = 1; i < devicesNameSerialList.length; i++) {
            String serial = nextSerial;
            String state = getState(devicesNameSerialList[i]);
            Device device = new Device(serial, state);
            this.add(device);
            nextSerial = getNextSerial(devicesNameSerialList[i], state);
        }
    }

    private String getNextSerial(String split, String state) {
        return split.substring(state.length(), split.length());

    }

    private String getState(String split) {
        for (String state : STATES) {
            if (split.contains(state))
                return state;
        }
        return null;
    }

    private void add(Device device) {
        devices.add(device);
    }

    public int size() {
        return devices.size();
    }

    public Device get(int index) {
        return devices.get(index);
    }

    public boolean contains(Device device) {
        return devices.contains(device);
    }
}
