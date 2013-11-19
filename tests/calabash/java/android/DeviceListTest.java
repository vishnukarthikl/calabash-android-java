package calabash.java.android;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DeviceListTest {

    @Test
    public void shouldGetDeviceListFromAbdOutput() {
        String deviceListOutput = "List of devices attached " +
                "emulator-5554\tdevice123456789\tofflineemulator-5556\tno device";

        DeviceList deviceList = new DeviceList(deviceListOutput);

        assertEquals(3, deviceList.size());
        assertEquals("emulator-5554", deviceList.get(0).getSerial());
        assertEquals("device", deviceList.get(0).getState());
        assertEquals("123456789", deviceList.get(1).getSerial());
        assertEquals("offline", deviceList.get(1).getState());
        assertEquals("emulator-5556", deviceList.get(2).getSerial());
        assertEquals("no device", deviceList.get(2).getState());
    }

    @Test
    public void shouldReturnEmptyDevice() {
        String deviceListOutput = "List of devices attached";

        DeviceList deviceList = new DeviceList(deviceListOutput);

        assertEquals(0, deviceList.size());

    }
}
