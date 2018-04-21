/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.powermax.internal.message;

import java.lang.reflect.InvocationTargetException;

import javax.xml.bind.DatatypeConverter;

import org.openhab.binding.powermax.internal.state.PowermaxState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base class for handling a message with the Visonic alarm system
 *
 * @author Laurent Garnier
 * @since 1.9.0
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
     * @param message
     *            the message as a buffer of bytes
     */
    public PowermaxBaseMessage(byte[] message) {
        this.sendType = null;
        decodeMessage(message);
    }

    /**
     * Constructor.
     *
     * @param sendType
     *            the type of a message to be sent
     */
    public PowermaxBaseMessage(PowermaxSendType sendType) {
        this(sendType, null);
    }

    /**
     * Constructor.
     *
     * @param sendType
     *            the type of a message to be sent
     * @param param
     *            the dynamic part of a message to be sent; null if no dynamic part
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
     * @param data
     *            the message as a buffer of bytes
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
    public PowermaxState handleMessage() {
        // Send an ACK if needed
        if (isAckRequired()) {
            PowermaxCommDriver.getTheCommDriver().sendAck(this, (byte) 0x02);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("{}message handled by class {}: {}", (receiveType == null) ? "Unsupported " : "",
                    this.getClass().getSimpleName(), this.toString());
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
        String str = "\n - Raw data = " + DatatypeConverter.printHexBinary(rawData);
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
     * @param message
     *            the received message as a buffer of bytes
     *
     * @return a new class instance
     */
    public static PowermaxBaseMessage getMessageObject(byte[] message) {
        Class<?> cl;
        try {
            cl = PowermaxReceiveType.fromCode(message[1]).getHandlerClass();
        } catch (IllegalArgumentException e) {
            cl = PowermaxBaseMessage.class;
        }
        try {
            return (PowermaxBaseMessage) cl.getConstructor(byte[].class).newInstance(message);
        } catch (InstantiationException e) {
            return new PowermaxBaseMessage(message);
        } catch (IllegalAccessException e) {
            return new PowermaxBaseMessage(message);
        } catch (NoSuchMethodException e) {
            return new PowermaxBaseMessage(message);
        } catch (InvocationTargetException e) {
            return new PowermaxBaseMessage(message);
        }
    }

}
