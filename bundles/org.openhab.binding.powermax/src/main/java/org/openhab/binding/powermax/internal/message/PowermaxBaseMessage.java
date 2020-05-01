/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.powermax.internal.message;

import org.eclipse.smarthome.core.util.HexUtils;
import org.openhab.binding.powermax.internal.state.PowermaxState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base class for handling a message with the Visonic alarm system
 *
 * @author Laurent Garnier - Initial contribution
 */
public class PowermaxBaseMessage {

    private final Logger logger = LoggerFactory.getLogger(PowermaxBaseMessage.class);

    private byte[] rawData;
    private int code;
    private PowermaxSendType sendType;
    private PowermaxReceiveType receiveType;

    /**
     * Constructor.
     *
     * @param message the message as a buffer of bytes
     */
    public PowermaxBaseMessage(byte[] message) {
        this.sendType = null;
        decodeMessage(message);
    }

    /**
     * Constructor.
     *
     * @param sendType the type of a message to be sent
     */
    public PowermaxBaseMessage(PowermaxSendType sendType) {
        this(sendType, null);
    }

    /**
     * Constructor.
     *
     * @param sendType the type of a message to be sent
     * @param param the dynamic part of a message to be sent; null if no dynamic part
     */
    public PowermaxBaseMessage(PowermaxSendType sendType, byte[] param) {
        this.sendType = sendType;
        byte[] message = new byte[sendType.getMessage().length + 3];
        int index = 0;
        message[index++] = 0x0D;
        for (int i = 0; i < sendType.getMessage().length; i++) {
            if ((param != null) && (sendType.getParamPosition() != null) && (i >= sendType.getParamPosition())
                    && (i < (sendType.getParamPosition() + param.length))) {
                message[index++] = param[i - sendType.getParamPosition()];
            } else {
                message[index++] = sendType.getMessage()[i];
            }
        }
        message[index++] = 0x00;
        message[index++] = 0x0A;
        decodeMessage(message);
    }

    /**
     * Extract information from the buffer of bytes and set class attributes
     *
     * @param data the message as a buffer of bytes
     */
    private void decodeMessage(byte[] data) {
        rawData = data;
        code = rawData[1] & 0x000000FF;
        try {
            receiveType = PowermaxReceiveType.fromCode((byte) code);
        } catch (IllegalArgumentException e) {
            receiveType = null;
        }
    }

    /**
     * Work to be done when receiving a message from the Visonic alarm system
     *
     * @return a new state containing all changes driven by the message
     */
    public PowermaxState handleMessage(PowermaxCommManager commManager) {
        // Send an ACK if needed
        if (isAckRequired() && commManager != null) {
            commManager.sendAck(this, (byte) 0x02);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("{}message handled by class {}: {}", (receiveType == null) ? "Unsupported " : "",
                    this.getClass().getSimpleName(), this);
        }

        return null;
    }

    /**
     * @return the raw data of the message (buffer of bytes)
     */
    public byte[] getRawData() {
        return rawData;
    }

    /**
     * @return the identifying code of the message (second byte in the buffer)
     */
    public int getCode() {
        return code;
    }

    /**
     * @return the type of the message to be sent
     */
    public PowermaxSendType getSendType() {
        return sendType;
    }

    public void setSendType(PowermaxSendType sendType) {
        this.sendType = sendType;
    }

    /**
     * @return the type of the received message
     */
    public PowermaxReceiveType getReceiveType() {
        return receiveType;
    }

    /**
     * @return true if the received message requires the sending of an ACK
     */
    public boolean isAckRequired() {
        return receiveType == null || receiveType.isAckRequired();
    }

    @Override
    public String toString() {
        String str = "\n - Raw data = " + HexUtils.bytesToHex(rawData);
        str += "\n - type = " + String.format("%02X", code);
        if (sendType != null) {
            str += " ( " + sendType.toString() + " )";
        } else if (receiveType != null) {
            str += " ( " + receiveType.toString() + " )";
        }

        return str;
    }

    /**
     * Instantiate a class for handling a received message The class depends on the message.
     *
     * @param message the received message as a buffer of bytes
     *
     * @return a new class instance
     */
    public static PowermaxBaseMessage getMessageHandler(byte[] message) {
        PowermaxBaseMessage msgHandler;
        try {
            PowermaxReceiveType msgType = PowermaxReceiveType.fromCode(message[1]);
            switch (msgType) {
                case ACK:
                    msgHandler = new PowermaxAckMessage(message);
                    break;
                case TIMEOUT:
                    msgHandler = new PowermaxTimeoutMessage(message);
                    break;
                case DENIED:
                    msgHandler = new PowermaxDeniedMessage(message);
                    break;
                case DOWNLOAD_RETRY:
                    msgHandler = new PowermaxDownloadRetryMessage(message);
                    break;
                case SETTINGS:
                case SETTINGS_ITEM:
                    msgHandler = new PowermaxSettingsMessage(message);
                    break;
                case INFO:
                    msgHandler = new PowermaxInfoMessage(message);
                    break;
                case EVENT_LOG:
                    msgHandler = new PowermaxEventLogMessage(message);
                    break;
                case ZONESNAME:
                    msgHandler = new PowermaxZonesNameMessage(message);
                    break;
                case STATUS:
                    msgHandler = new PowermaxStatusMessage(message);
                    break;
                case ZONESTYPE:
                    msgHandler = new PowermaxZonesTypeMessage(message);
                    break;
                case PANEL:
                    msgHandler = new PowermaxPanelMessage(message);
                    break;
                case POWERLINK:
                    msgHandler = new PowermaxPowerlinkMessage(message);
                    break;
                case POWERMASTER:
                    msgHandler = new PowermaxPowerMasterMessage(message);
                    break;
                default:
                    msgHandler = new PowermaxBaseMessage(message);
                    break;
            }
        } catch (IllegalArgumentException e) {
            msgHandler = new PowermaxBaseMessage(message);
        }
        return msgHandler;
    }
}
