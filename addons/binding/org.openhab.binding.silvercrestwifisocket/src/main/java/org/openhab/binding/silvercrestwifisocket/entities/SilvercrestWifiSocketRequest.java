package org.openhab.binding.silvercrestwifisocket.entities;

import org.openhab.binding.silvercrestwifisocket.enums.SilvercrestWifiSocketRequestType;

/**
 * This POJO represents one Wifi Socket request.
 *
 * @author Jaime Vaz - Initial contribution
 *
 */
public class SilvercrestWifiSocketRequest {

    private String macAddress;
    private SilvercrestWifiSocketRequestType type;

    /**
     * Default constructor.
     *
     * @param macAddress the mac address
     * @param type the {@link SilvercrestWifiSocketRequestType}
     */
    public SilvercrestWifiSocketRequest(final String macAddress, final SilvercrestWifiSocketRequestType type) {
        this.macAddress = macAddress;
        this.type = type;
    }

    public String getMacAddress() {
        return this.macAddress;
    }

    public void setMacAddress(final String macAddress) {
        this.macAddress = macAddress;
    }

    public SilvercrestWifiSocketRequestType getType() {
        return this.type;
    }

    public void setType(final SilvercrestWifiSocketRequestType type) {
        this.type = type;
    }
}
