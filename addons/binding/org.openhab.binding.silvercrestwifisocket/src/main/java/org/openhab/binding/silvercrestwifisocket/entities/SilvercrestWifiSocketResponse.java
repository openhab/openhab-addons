package org.openhab.binding.silvercrestwifisocket.entities;

import org.openhab.binding.silvercrestwifisocket.enums.SilvercrestWifiSocketResponseType;

/**
 * This POJO represents one Wifi Socket Response.
 *
 * @author Jaime Vaz - Initial contribution
 *
 */
public class SilvercrestWifiSocketResponse {

    private String macAddress;
    private String hostAddress;
    private SilvercrestWifiSocketResponseType type;

    /**
     * Default constructor.
     *
     * @param macAddress the mac address
     * @param hostAddress the host address
     * @param type the {@link SilvercrestWifiSocketResponseType}
     */
    public SilvercrestWifiSocketResponse(final String macAddress, final String hostAddress,
            final SilvercrestWifiSocketResponseType type) {
        super();
        this.macAddress = macAddress;
        this.hostAddress = hostAddress;
        this.type = type;
    }

    /**
     * Constructor.
     *
     * @param macAddress the mac address
     * @param type the {@link SilvercrestWifiSocketResponseType}
     */
    public SilvercrestWifiSocketResponse(final String macAddress, final SilvercrestWifiSocketResponseType type) {
        this(macAddress, null, type);
    }

    public String getMacAddress() {
        return this.macAddress;
    }

    public void setMacAddress(final String macAddress) {
        this.macAddress = macAddress;
    }

    public SilvercrestWifiSocketResponseType getType() {
        return this.type;
    }

    public void setType(final SilvercrestWifiSocketResponseType type) {
        this.type = type;
    }

    public String getHostAddress() {
        return this.hostAddress;
    }

    public void setHostAddress(final String hostAddress) {
        this.hostAddress = hostAddress;
    }
}
