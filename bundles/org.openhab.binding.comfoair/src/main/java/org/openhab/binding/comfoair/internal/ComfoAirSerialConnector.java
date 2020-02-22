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
package org.openhab.binding.comfoair.internal;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;

import org.apache.commons.io.IOUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.io.transport.serial.PortInUseException;
import org.eclipse.smarthome.io.transport.serial.SerialPort;
import org.eclipse.smarthome.io.transport.serial.SerialPortEvent;
import org.eclipse.smarthome.io.transport.serial.SerialPortEventListener;
import org.eclipse.smarthome.io.transport.serial.SerialPortIdentifier;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.eclipse.smarthome.io.transport.serial.UnsupportedCommOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connector class for serial communication with ComfoAir device
 *
 * @author Hans Böhm - Initial contribution
 *
 */
@NonNullByDefault
public class ComfoAirSerialConnector implements SerialPortEventListener {

    private final Logger logger = LoggerFactory.getLogger(ComfoAirSerialConnector.class);

    private static byte[] START = { (byte) 0x07, (byte) 0xf0 };
    private static byte[] END = { (byte) 0x07, (byte) 0x0f };
    private static byte[] ACK = { (byte) 0x07, (byte) 0xf3 };

    private boolean isSuspended = true;

    private boolean connected = false;
    private final String serialPortName;
    private final int baudRate;
    private final SerialPortManager serialPortManager;
    private @Nullable SerialPort serialPort;
    private @Nullable InputStream inputStream;
    private @Nullable OutputStream outputStream;

    public ComfoAirSerialConnector(final SerialPortManager serialPortManager, final String serialPortName,
            final int baudRate) {
        this.serialPortManager = serialPortManager;
        this.serialPortName = serialPortName;
        this.baudRate = baudRate;
    }

    /**
     * Open serial port.
     */
    public void open() {
        logger.debug("open(): Opening ComfoAir connection");

        try {
            SerialPortIdentifier portIdentifier = serialPortManager.getIdentifier(serialPortName);
            if (portIdentifier != null) {
                SerialPort serialPort = portIdentifier.open(this.getClass().getName(), 3000);
                serialPort.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);
                serialPort.enableReceiveThreshold(1);
                serialPort.enableReceiveTimeout(1000);
                serialPort.addEventListener(this);
                serialPort.notifyOnDataAvailable(true);
                this.serialPort = serialPort;

                inputStream = new DataInputStream(new BufferedInputStream(serialPort.getInputStream()));
                outputStream = serialPort.getOutputStream();

                ComfoAirCommand command = ComfoAirCommandType.getChangeCommand(ComfoAirCommandType.ACTIVATE.key,
                        OnOffType.ON);

                if (command != null) {
                    sendCommand(command, new int[0]);
                } else {
                    logger.debug("Failure while creating COMMAND: {}", command);
                }
                setConnected(true);
            } else {
                logger.debug("open(): No such Port: {}", serialPortName);
                setConnected(false);
            }
        } catch (PortInUseException e) {
            logger.debug("open(): Port in Use Exception: {}", e.getMessage(), e);
            setConnected(false);
        } catch (UnsupportedCommOperationException e) {
            logger.debug("open(): Unsupported Comm Operation Exception: {}", e.getMessage(), e);
            setConnected(false);
        } catch (IOException e) {
            logger.debug("open(): IO Exception: {}", e.getMessage(), e);
            setConnected(false);
        } catch (TooManyListenersException e) {
            logger.debug("open(): Too Many Listeners Exception: {}", e.getMessage(), e);
        }
    }

    /**
     * Close serial port.
     */
    public void close() {
        logger.debug("close(): Close ComfoAir connection");
        SerialPort serialPort = this.serialPort;

        if (serialPort != null) {
            ComfoAirCommand command = ComfoAirCommandType.getChangeCommand(ComfoAirCommandType.ACTIVATE.key,
                    OnOffType.OFF);

            if (command != null) {
                sendCommand(command, new int[0]);
            } else {
                logger.debug("Failure while creating COMMAND: {}", command);
            }

            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
            serialPort.close();
        }
    }

    /**
     * Prepare a command for sending using the serial port.
     *
     * @param command
     * @param preRequestData
     * @return reply byte values
     */
    public synchronized int[] sendCommand(ComfoAirCommand command, int[] preRequestData) {
        Integer requestCmd = command.getRequestCmd();
        int retry = 0;

        if (requestCmd != null) {
            // Switch support for app or ccease control
            if (requestCmd == 0x9b) {
                isSuspended = !isSuspended;
            } else if (requestCmd == 0x9c) {
                return new int[] { isSuspended ? 0x00 : 0x03 };
            } else if (isSuspended) {
                logger.trace("Ignore cmd. Service is currently suspended");
                return new int[0];
            }

            do {
                // If preRequestData param was send (preRequestData is sending for write command)
                int[] requestData;

                if (preRequestData.length <= 0) {
                    requestData = command.getRequestData();
                } else {
                    requestData = buildRequestData(command, preRequestData);

                    if (requestData.length <= 0) {
                        logger.debug("Unable to build data for write command: {}",
                                String.format("%02x", command.getReplyCmd()));
                        return new int[0];
                    }
                }

                // Fake read request for ccease properties
                if (requestData.length <= 0 && requestCmd == 0x37) {
                    requestData = new int[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
                }

                byte[] requestBlock = calculateRequest(requestCmd, requestData);
                logger.trace("send DATA: {}", dumpData(requestBlock));

                if (!send(requestBlock)) {
                    return new int[0];
                }

                byte[] responseBlock = new byte[0];

                try {
                    InputStream inputStream = this.inputStream;
                    // 31 is max. response length
                    byte[] readBuffer = new byte[31];

                    do {
                        while (inputStream != null && inputStream.available() > 0) {

                            int bytes = inputStream.read(readBuffer);

                            // merge bytes
                            byte[] mergedBytes = new byte[responseBlock.length + bytes];
                            System.arraycopy(responseBlock, 0, mergedBytes, 0, responseBlock.length);
                            System.arraycopy(readBuffer, 0, mergedBytes, responseBlock.length, bytes);

                            responseBlock = mergedBytes;
                        }
                        try {
                            // add wait states around reading the stream, so that
                            // interrupted transmissions are merged
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            // ignore interruption
                        }

                    } while (inputStream != null && inputStream.available() > 0);

                    // check for ACK
                    if (responseBlock.length >= 2 && responseBlock[0] == (byte) 0x07
                            && responseBlock[1] == (byte) 0xf3) {
                        if (command.getReplyCmd() == null) {
                            // confirm additional data with an ACK
                            if (responseBlock.length > 2) {
                                send(ACK);
                            }
                            return new int[0];
                        }

                        // check for start and end sequence and if the response cmd
                        // matches
                        // 11 is the minimum response length with one data byte
                        if (responseBlock.length >= 11 && responseBlock[2] == (byte) 0x07
                                && responseBlock[3] == (byte) 0xf0
                                && responseBlock[responseBlock.length - 2] == (byte) 0x07
                                && responseBlock[responseBlock.length - 1] == (byte) 0x0f
                                && (responseBlock[5] & 0xff) == command.getReplyCmd()) {

                            logger.trace("receive RAW DATA: {}", dumpData(responseBlock));

                            byte[] cleanedBlock = cleanupBlock(responseBlock);

                            int dataSize = cleanedBlock[2];

                            // the cleanedBlock size should equal dataSize + 2 cmd
                            // bytes and + 1 checksum byte
                            if (dataSize + 3 == cleanedBlock.length - 1) {

                                byte checksum = cleanedBlock[dataSize + 3];
                                int[] replyData = new int[dataSize];
                                for (int i = 0; i < dataSize; i++) {
                                    replyData[i] = cleanedBlock[i + 3] & 0xff;
                                }

                                byte[] _block = new byte[3 + replyData.length];
                                System.arraycopy(cleanedBlock, 0, _block, 0, _block.length);

                                // validate calculated checksum against submitted
                                // checksum
                                if (calculateChecksum(_block) == checksum) {

                                    logger.trace("receive CMD: {} DATA: {}",
                                            String.format("%02x", command.getReplyCmd()), dumpData(replyData));

                                    send(ACK);

                                    return replyData;
                                }

                                logger.debug("Unable to handle data. Checksum verification failed");
                            } else {
                                logger.debug("Unable to handle data. Data size not valid");
                            }

                            logger.trace("skip CMD: {} DATA: {}", String.format("%02x", command.getReplyCmd()),
                                    dumpData(cleanedBlock));
                        }
                    }

                } catch (IOException e) {
                    logger.debug("IO error: {}", e.getMessage());
                }

                try {

                    Thread.sleep(1000);
                    logger.debug("Retry cmd. Last call was not successful. Request: {} Response: {}",
                            dumpData(requestBlock), (responseBlock.length > 0 ? dumpData(responseBlock) : "null"));

                } catch (InterruptedException e) {
                    // ignore interruption
                }

            } while (retry++ < 5);

            if (retry == 5) {
                logger.debug("Unable to send command. {} retries failed.", retry);
            }
        }
        return new int[0];
    }

    /**
     * Generate the byte sequence for sending to ComfoAir (incl. START & END
     * sequence and checksum).
     *
     * @param command
     * @param requestData
     * @return response byte value block with cmd, data and checksum
     */
    private byte[] calculateRequest(int command, int[] requestData) {

        // generate the command block (cmd and request data)
        int length = requestData.length;

        byte[] block = new byte[4 + length];

        block[0] = 0x00;
        block[1] = (byte) command;
        block[2] = (byte) length;
        if (requestData.length > 0) {
            for (int i = 0; i < requestData.length; i++) {
                block[i + 3] = (byte) requestData[i];
            }
        }

        // calculate checksum for command block
        byte checksum = calculateChecksum(block);
        block[block.length - 1] = checksum;

        // escape the command block with checksum included
        block = escapeBlock(block);

        byte[] request = new byte[4 + block.length];

        request[0] = START[0];
        request[1] = START[1];
        System.arraycopy(block, 0, request, 2, block.length);
        request[request.length - 2] = END[0];
        request[request.length - 1] = END[1];

        return request;
    }

    /**
     * Calculates a checksum for a command block (cmd, data and checksum).
     *
     * @param block
     * @return checksum byte value
     */
    private byte calculateChecksum(byte[] block) {

        int datasum = 0;
        for (int i = 0; i < block.length; i++) {
            datasum += block[i];
        }
        datasum += 173;

        String hexString = Integer.toHexString(datasum);
        if (hexString.length() > 2) {
            hexString = hexString.substring(hexString.length() - 2);
        }

        return (byte) Integer.parseInt(hexString, 16);
    }

    /**
     * Cleanup a commandblock from quoted 0x07 characters.
     *
     * @param processBuffer
     * @return the 0x07 cleaned byte values
     */
    private byte[] cleanupBlock(byte[] processBuffer) {

        int pos = 0;
        byte[] cleanedBuffer = new byte[50];

        for (int i = 4; i < processBuffer.length - 2; i++) {

            if ((byte) 0x07 == processBuffer[i] && (byte) 0x07 == processBuffer[i + 1]) {
                i++;
            }

            cleanedBuffer[pos] = processBuffer[i];
            pos++;
        }

        byte[] _block = new byte[pos];
        System.arraycopy(cleanedBuffer, 0, _block, 0, _block.length);

        return _block;
    }

    /**
     * Escape special 0x07 character.
     *
     * @param cleanedBuffer
     * @return escaped byte value array
     */
    private byte[] escapeBlock(byte[] cleanedBuffer) {

        int pos = 0;

        byte[] processBuffer = new byte[50];

        for (int i = 0; i < cleanedBuffer.length; i++) {
            if ((byte) 0x07 == cleanedBuffer[i]) {
                processBuffer[pos] = (byte) 0x07;
                pos++;
            }

            processBuffer[pos] = cleanedBuffer[i];
            pos++;
        }

        byte[] _block = new byte[pos];
        System.arraycopy(processBuffer, 0, _block, 0, _block.length);

        return _block;
    }

    /**
     * Send the byte values.
     *
     * @param request
     * @return successful flag
     */
    private boolean send(byte[] request) {
        logger.trace("send DATA: {}", dumpData(request));

        try {
            if (outputStream != null) {
                outputStream.write(request);
            }
            return true;

        } catch (IOException e) {
            logger.debug("Error writing to serial port {}: {}", serialPortName, e.getLocalizedMessage());
            return false;
        }
    }

    /**
     * Is used to debug byte values.
     *
     * @param replyData
     * @return
     */
    public static String dumpData(int[] replyData) {

        StringBuffer sb = new StringBuffer();
        for (int ch : replyData) {
            sb.append(String.format(" %02x", ch));
        }
        return sb.toString();
    }

    private String dumpData(byte[] data) {

        StringBuffer sb = new StringBuffer();
        for (byte ch : data) {
            sb.append(String.format(" %02x", ch));
        }
        return sb.toString();
    }

    /**
     * Build request data based on reply data
     *
     * @param command
     * @param preRequestData
     * @return new build int values array
     */
    private int[] buildRequestData(ComfoAirCommand command, int[] preRequestData) {
        int[] newRequestData;

        Integer requestCmd = command.getRequestCmd();
        Integer dataPosition = command.getDataPosition();
        Integer requestValue = command.getRequestValue();
        logger.debug("Building request data: requestCmd = {}, dataPosition = {}, requestValue = {}", requestCmd,
                dataPosition, requestValue);

        if (requestCmd != null && dataPosition != null && requestValue != null) {
            if (requestCmd == 0xcb) {
                newRequestData = new int[8];

                if (preRequestData.length > 0 && newRequestData.length <= preRequestData.length) {

                    for (int i = 0; i < newRequestData.length; i++) {

                        if (dataPosition == i) {
                            newRequestData[i] = requestValue;
                        } else {
                            newRequestData[i] = preRequestData[i];
                        }
                    }

                } else {
                    return new int[0];
                }

            } else if (requestCmd == 0xcf) {
                newRequestData = new int[9];

                if (preRequestData.length > 0 && newRequestData.length <= preRequestData.length) {

                    for (int i = 0; i < newRequestData.length; i++) {
                        int j = i > 5 ? i + 4 : i;

                        if (dataPosition == i) {
                            newRequestData[i] = requestValue;
                        } else {
                            newRequestData[i] = preRequestData[j];
                        }
                    }

                } else {
                    return new int[0];
                }

            } else if (requestCmd == 0xd7) {
                newRequestData = new int[8];

                if (preRequestData.length > 0 && newRequestData.length <= preRequestData.length) {

                    for (int i = 0; i < newRequestData.length; i++) {
                        int j = i > 5 ? i + 3 : i;

                        if (dataPosition == i) {

                            if (dataPosition == 4) {
                                requestValue = checkByteAndCalculateValue(command, requestValue, preRequestData[j]);

                                newRequestData[i] = preRequestData[j] + requestValue;

                            } else {
                                newRequestData[i] = requestValue;
                            }

                        } else {
                            newRequestData[i] = preRequestData[j];
                        }
                    }

                } else {
                    return new int[0];
                }

            } else if (requestCmd == 0xed) {
                newRequestData = new int[5];

                if (preRequestData.length > 0 && newRequestData.length <= preRequestData.length) {

                    for (int i = 0; i < newRequestData.length; i++) {
                        int j = i > 3 ? i + 2 : i;

                        if (dataPosition == i) {
                            newRequestData[i] = requestValue;
                        } else {
                            newRequestData[i] = preRequestData[j];
                        }
                    }

                } else {
                    return new int[0];
                }

            } else if (requestCmd == 0x9f) {
                newRequestData = new int[19];

                if (preRequestData.length > 0 && newRequestData.length <= preRequestData.length) {

                    for (int i = 0; i < newRequestData.length; i++) {

                        if (dataPosition == i) {

                            if (dataPosition == 0 || dataPosition == 1 || dataPosition == 2) {
                                requestValue = checkByteAndCalculateValue(command, requestValue, preRequestData[i]);

                                newRequestData[i] = preRequestData[i] + requestValue;

                            } else {
                                newRequestData[i] = requestValue;
                            }

                        } else {
                            newRequestData[i] = preRequestData[i];
                        }
                    }

                } else {
                    return new int[0];
                }

            } else {
                return new int[0];
            }

            return newRequestData;
        } else {
            return new int[0];
        }
    }

    /**
     * Check if preValue contains possible byte and calculate new value
     *
     * @param command
     * @param requestValue
     * @param preValue
     * @return new int value
     */
    private int checkByteAndCalculateValue(ComfoAirCommand command, int requestValue, int preValue) {
        String key = command.getKeys().get(0);
        ComfoAirCommandType commandType = ComfoAirCommandType.getCommandTypeByKey(key);
        int possibleValue = commandType.getPossibleValues()[0];

        boolean isActive = (preValue & possibleValue) == possibleValue;
        int newValue;

        if (isActive) {
            newValue = requestValue == 1 ? 0 : -possibleValue;
        } else {
            newValue = requestValue == 1 ? possibleValue : 0;
        }
        return newValue;
    }

    /**
     * @return true if connected or false if not
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Set the connection state
     *
     * @param connected true if connected or false if not
     */
    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        try {
            logger.trace("RXTX library CPU load workaround, sleep forever");
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
        }
    }
}
