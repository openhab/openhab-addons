package org.openhab.binding.silvercrest.wifisocketoutlet.enums;

/**
 * This enum represents the available Wifi Socket Outlet response types.
 *
 * @author Jaime Vaz - Initial contribution
 *
 */
public enum WifiSocketOutletResponseType {
    /** Status changed to ON. */
    ON,
    /** Status changed to OFF. */
    OFF,
    /** ACKnowledgement. */
    ACK,
    /** Discovery request. */
    DISCOVERY;
}
