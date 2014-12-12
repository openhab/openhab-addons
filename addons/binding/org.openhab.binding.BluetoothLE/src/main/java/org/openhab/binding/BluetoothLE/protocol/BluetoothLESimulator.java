package org.openhab.binding.BluetoothLE.protocol;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

public class BluetoothLESimulator extends BluetoothLEConnector {
	
	//  0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35
	// 03 01 9C 75 2C B4 10 F5 1A 02 01 04 09 09 43 39 35 37 30 31 35 46 07 16 09 18 2F 08 00 FE 04 16 0F 18 4F CC
	// 03 01 9C 75 2C B4 10 F5 1A 02 01 04 09 09 43 39 35 37 30 31 35 46 07 16 09 18 DB 08 00 FE 04 16 0F 18 53 D8
	// 03 01 36 BB 5E 19 49 FE 1A 02 01 04 09 09 36 33 42 42 39 34 45 46 07 16 09 18 DC 09 00 FE 04 16 0F 18 4A D5

	private Byte[] freeTecDevice1 = getBytes("03019C752CB410F51A02010409094339353730313546071609182F0800FE04160F184FCC");
	private Byte[] freeTecDevice2 = getBytes("030136BB5E1949FE1A0201040909363342423934454607160918DC0900FE04160F184AD5");

	private List<Byte[]> devices = new ArrayList<Byte[]>();
	private int currentDevice = 0;

	public BluetoothLESimulator() {
		devices.add(freeTecDevice1);
		devices.add(freeTecDevice2);
	}
	
	@Override
	public void connect() throws Exception {
		
	}

	@Override
	public void disconnect() throws Exception {
		
	}

	@Override
	public Object getData() throws Exception {	
		Thread.sleep(5000); // do not run havoc :-)

		simulateFreeTecDevices(freeTecDevice1);
		simulateFreeTecDevices(freeTecDevice2);
		
		currentDevice++;
		if (currentDevice >= devices.size()) currentDevice = 0;
		byte[] bytes = ArrayUtils.toPrimitive(devices.get(currentDevice));

		return getData(bytes, bytes.length);
	}
	
	private void simulateFreeTecDevices(Byte[] bytes) {
		// change temperature
		bytes[26]++;
		
		// change battery level
		bytes[34]++;
		if (bytes[34] == 101) bytes[34] = 0;
	}
	
	private Byte[] getBytes(String hexString) {
		byte[] bytes = javax.xml.bind.DatatypeConverter.parseHexBinary(hexString);
		Byte[] ret = new Byte[bytes.length];
		for(int i = 0; i < bytes.length; i++) {
			ret[i] = bytes[i];
		}
		return ret;
	}
}
