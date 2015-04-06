package org.openhab.binding.openwebnetvdes.devices;

import java.util.Set;

public abstract class BticinoDevice {
	protected static Set<DeviceFeatureType> features;
	protected int whereAddress; //the address according to the OWN Spec
	protected boolean riser; //Is the device a riser
	private int roomId = -1;
	
	private boolean updated;
	//private boolean batteryLow;

	private boolean initialized;
	private boolean answer;
	private boolean error;
	private boolean valid;	
	private boolean gatewayKnown;
	private boolean panelLocked;
	private boolean linkStatusError;

	public abstract VdesDeviceType getType();
	
	public int getWhereAddress() {
		return whereAddress;
	}

	public void setWhereAddress(int whereAddress) {
		this.whereAddress = whereAddress;
	}

	public boolean isRiser() {
		return riser;
	}

	public void setRiser(boolean riser) {
		this.riser = riser;
	}

	public Set<DeviceFeatureType> getFeatures() {
		return features;
	}

	public final int getRoomId() {
		return roomId;
	}

	public final void setRoomId(int roomId) {
		this.roomId = roomId;
	}

	private void setLinkStatusError(boolean linkStatusError) {
		this.linkStatusError = linkStatusError;
	}

	private void setPanelLocked(boolean panelLocked) {
		this.panelLocked = panelLocked;
	}

	private void setGatewayKnown(boolean gatewayKnown) {
		this.gatewayKnown = gatewayKnown;
	}

	private void setValid(boolean valid) {
		this.valid = valid;
	}

	private void setError(boolean error) {
		this.error = error;

	}

	private void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	private void setAnswer(boolean answer) {
		this.answer = answer;
	}

	public boolean isUpdated() {
		return updated;
	}

	public void setUpdated(boolean updated) {
		this.updated = updated;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public boolean isAnswer() {
		return answer;
	}

	public boolean isError() {
		return error;
	}

	public boolean isValid() {
		return valid;
	}

	public boolean isGatewayKnown() {
		return gatewayKnown;
	}

	public boolean isPanelLocked() {
		return panelLocked;
	}

	public boolean isLinkStatusError() {
		return linkStatusError;
	}

}
