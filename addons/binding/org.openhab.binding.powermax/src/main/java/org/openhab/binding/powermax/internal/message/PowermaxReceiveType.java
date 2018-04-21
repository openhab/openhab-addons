/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.powermax.internal.message;

/**
 * Used to map received messages from the Visonic alarm panel to a ENUM value
 *
 * @author Laurent Garnier
 * @since 1.9.0
 */
public enum PowermaxReceiveType {

    ACK((byte) 0x02, 0, false, PowermaxAckMessage.class),
    TIMEOUT((byte) 0x06, 0, false, PowermaxTimeoutMessage.class),
    DENIED((byte) 0x08, 0, true, PowermaxDeniedMessage.class),
    STOP((byte) 0x0B, 0, true, PowermaxBaseMessage.class),
    DOWNLOAD_RETRY((byte) 0x25, 14, true, PowermaxDownloadRetryMessage.class),
    SETTINGS((byte) 0x33, 14, true, PowermaxSettingsMessage.class),
    INFO((byte) 0x3C, 14, true, PowermaxInfoMessage.class),
    SETTINGS_ITEM((byte) 0x3F, 0, true, PowermaxSettingsMessage.class),
    EVENT_LOG((byte) 0xA0, 15, true, PowermaxEventLogMessage.class),
    ZONESNAME((byte) 0xA3, 15, true, PowermaxZonesNameMessage.class),
    STATUS((byte) 0xA5, 15, true, PowermaxStatusMessage.class),
    ZONESTYPE((byte) 0xA6, 15, true, PowermaxZonesTypeMessage.class),
    PANEL((byte) 0xA7, 15, true, PowermaxPanelMessage.class),
    POWERLINK((byte) 0xAB, 15, false, PowermaxPowerlinkMessage.class),
    POWERMASTER((byte) 0xB0, 0, true, PowermaxPowerMasterMessage.class),
    F1((byte) 0xF1, 9, false, PowermaxBaseMessage.class);

    private byte code;
    private int length;
    private boolean ackRequired;
    private Class<?> handlerClass;

    private PowermaxReceiveType(byte code, int length, boolean ackRequired, Class<?> handlerClass) {
        this.code = code;
        this.length = length;
        this.ackRequired = ackRequired;
        this.handlerClass = handlerClass;
    }

    /**
     * @return the code identifying the message (second byte in the message)
     */
    public byte getCode() {
        return code;
    }

    /**
     * @return the message expected length
     */
    public int getLength() {
        return length;
    }

    /**
     * @return true if the received message requires the sending of an ACK, false if not
     */
    public boolean isAckRequired() {
        return ackRequired;
    }

    /**
     * @return the class that should handle the message
     */
    public Class<?> getHandlerClass() {
        return handlerClass;
    }

    /**
     * Set the class that should handle the message
     *
     * @param handlerClass
     *            the class that should handle the message
     */
    public void setHandlerClass(Class<?> handlerClass) {
        this.handlerClass = handlerClass;
    }

    /**
     * Get the ENUM value from its identifying code
     *
     * @param code
     *            the identifying code
     *
     * @return the corresponding ENUM value
     *
     * @throws IllegalArgumentException if no ENUM value corresponds to this code
     */
    public static PowermaxReceiveType fromCode(byte code) {
        for (PowermaxReceiveType messageType : PowermaxReceiveType.values()) {
            if (messageType.getCode() == code) {
                return messageType;
            }
        }

        throw new IllegalArgumentException("Invalid code: " + code);
    }
}
