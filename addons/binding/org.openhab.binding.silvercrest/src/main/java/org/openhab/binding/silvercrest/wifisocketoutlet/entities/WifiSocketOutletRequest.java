package org.openhab.binding.silvercrest.wifisocketoutlet.entities;

import org.openhab.binding.silvercrest.wifisocketoutlet.enums.WifiSocketOutletRequestType;

/**
 * This POJO represents one Wifi Socket Outlet request.
 *
 * @author Jaime Vaz - Initial contribution
 *
 */
public class WifiSocketOutletRequest {

    private String macAddress;
    private WifiSocketOutletRequestType type;

    /**
     * Default constructor.
     *
     * @param macAddress the mac address
     * @param type the {@link WifiSocketOutletRequestType}
     */
    public WifiSocketOutletRequest(final String macAddress, final WifiSocketOutletRequestType type) {
        this.macAddress = macAddress;
        this.type = type;
    }

    public String getMacAddress() {
        return this.macAddress;
    }

    public void setMacAddress(final String macAddress) {
        this.macAddress = macAddress;
    }

    public WifiSocketOutletRequestType getType() {
        return this.type;
    }

    public void setType(final WifiSocketOutletRequestType type) {
        this.type = type;
    }
}
