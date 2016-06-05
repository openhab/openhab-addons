package org.openhab.binding.silvercrest.wifisocketoutlet.enums;

/**
 * This enum represents the available Wifi Socket Outlet request types.
 *
 * @author Jaime Vaz - Initial contribution
 *
 */
public enum WifiSocketOutletRequestType {
    /** Request ON. */
    ON("010000FFFF04040404"),
    /** Request OFF. */
    OFF("01000000FF04040404"),
    /** Request Status. */
    GPIO_STATUS("020000000004040404"),
    /** Discover socket. The command has one placeholder for the mac address. */
    DISCOVERY("23%s0202");

    private String command;

    private WifiSocketOutletRequestType(final String command) {
        this.command = command;
    }

    /**
     * Gets the hexadecimal command/format for include in request messages.
     *
     * @return the hexadecimal command/format
     */
    public String getCommand() {
        return this.command;
    }
}
