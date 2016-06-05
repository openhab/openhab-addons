package org.openhab.binding.silvercrest.wifisocketoutlet.entities;

import org.openhab.binding.silvercrest.wifisocketoutlet.enums.WifiSocketOutletResponseType;

/**
 * This POJO represents one Wifi Socket Outlet Response.
 *
 * @author Jaime Vaz - Initial contribution
 *
 */
public class WifiSocketOutletResponse {

    private String macAddress;
    private String hostAddress;
    private WifiSocketOutletResponseType type;

    /**
     * Default constructor.
     *
     * @param macAddress the mac address
     * @param hostAddress the host address
     * @param type the {@link WifiSocketOutletResponseType}
     */
    public WifiSocketOutletResponse(final String macAddress, final String hostAddress,
            final WifiSocketOutletResponseType type) {
        super();
        this.macAddress = macAddress;
        this.hostAddress = hostAddress;
        this.type = type;
    }

    /**
     * Constructor.
     *
     * @param macAddress the mac address
     * @param type the {@link WifiSocketOutletResponseType}
     */
    public WifiSocketOutletResponse(final String macAddress, final WifiSocketOutletResponseType type) {
        this(macAddress, null, type);
    }

    public String getMacAddress() {
        return this.macAddress;
    }

    public void setMacAddress(final String macAddress) {
        this.macAddress = macAddress;
    }

    public WifiSocketOutletResponseType getType() {
        return this.type;
    }

    public void setType(final WifiSocketOutletResponseType type) {
        this.type = type;
    }

    public String getHostAddress() {
        return this.hostAddress;
    }

    public void setHostAddress(final String hostAddress) {
        this.hostAddress = hostAddress;
    }
}
