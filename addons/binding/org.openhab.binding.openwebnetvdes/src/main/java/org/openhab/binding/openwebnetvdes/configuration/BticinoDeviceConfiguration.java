package org.openhab.binding.openwebnetvdes.configuration;

import org.openhab.binding.openwebnetvdes.devices.VdesDeviceType;

public class BticinoDeviceConfiguration {
		
	private VdesDeviceType deviceType = null;
	private int whereAddress = -1;
	private String serialNumber = null;
	private String name = null;
	private int roomId = -1;
	
	private BticinoDeviceConfiguration() {
	}
	
	/*public static BticinoDeviceConfiguration create(Message message) {	
		BticinoDeviceConfiguration configuration = new BticinoDeviceConfiguration();
		configuration.setValues((C_Message) message);
		
		return configuration;
	}*/
	
	/*public static BticinoDeviceConfiguration create(DeviceInformation di) {
		DeviceConfiguration configuration = new DeviceConfiguration();
		configuration.setValues(di.getRFAddress(), di.getDeviceType(), di.getSerialNumber(), di.getName());
		return configuration;
	}*/
	

	/*public void setValues(C_Message message) {
		setValues(message.getRFAddress(), message.getDeviceType(), message.getSerialNumber());
	}*/
	
	/*private void setValues(String rfAddress, VdesDeviceType deviceType, String serialNumber, String name) {
		setValues(rfAddress, deviceType, serialNumber);
		this.name = name;
	}*/
	
	/*private void setValues(String rfAddress, VdesDeviceType deviceType, String serialNumber) {
		this.rfAddress = rfAddress;
		this.deviceType = deviceType;
		this.serialNumber = serialNumber;
	}*/
	
	/*public String getRFAddress() {
		return rfAddress;
	}*/

	/*public VdesDeviceType getDeviceType() {
		return deviceType;
	}

	public String getSerialNumber() {
		return serialNumber;
	}
	
	public String getName() {
		return name;
	}

	public int getRoomId() {
		return roomId;
	}
	
	public void setRoomId(int roomId) {
		this.roomId = roomId;
	}*/

	public int getWhereAddress() {
		return whereAddress;
	}

	public void setWhereAddress(int whereAddress) {
		this.whereAddress = whereAddress;
	}		
	
}

