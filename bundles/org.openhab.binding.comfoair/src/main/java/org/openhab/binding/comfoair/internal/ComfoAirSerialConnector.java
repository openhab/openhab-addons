/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.core.library.types.OnOffType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connector class for serial communication with ComfoAir device
 *
 * @author Hans Böhm - Initial contribution
 *
 */
@NonNullByDefault
public class ComfoAirSerialConnector {

    private final Logger logger = LoggerFactory.getLogger(ComfoAirSerialConnector.class);

    private static final byte CTRL = (byte) 0x07;
    private static final byte[] START = { CTRL, (byte) 0xf0 };
    private static final byte[] END = { CTRL, (byte) 0x0f };
    private static final byte[] ACK = { CTRL, (byte) 0xf3 };

    private static final int MAX_RETRIES = 5;

    private boolean isSuspended = true;

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
     *
     * @throws ComfoAirSerialException
     */
    public void open() throws ComfoAirSerialException {
        logger.debug("open(): Opening ComfoAir connection");

        try {
            SerialPortIdentifier portIdentifier = serialPortManager.getIdentifier(serialPortName);
            if (portIdentifier != null) {
                SerialPort serialPort = portIdentifier.open(this.getClass().getName(), 3000);
                serialPort.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);
                serialPort.notifyOnDataAvailable(true);

                try {
                    serialPort.enableReceiveThreshold(1);
                } catch (UnsupportedCommOperationException e) {
                    logger.debug("Enable receive threshold is unsupported");
                }
                try {
                    serialPort.enableReceiveTimeout(1000);
                } catch (UnsupportedCommOperationException e) {
                    logger.debug("Enable receive timeout is unsupported");
                }

                this.serialPort = serialPort;

                inputStream = new DataInputStream(new BufferedInputStream(serialPort.getInputStream()));
                outputStream = serialPort.getOutputStream();

                ComfoAirCommand command = ComfoAirCommandType.getChangeCommand(ComfoAirCommandType.ACTIVATE.getKey(),
                        OnOffType.ON);

                if (command != null) {
                    sendCommand(command, ComfoAirCommandType.Constants.EMPTY_INT_ARRAY);
                } else {
                    logger.debug("Failure while creating COMMAND: {}", command);
                }
            } else {
                throw new ComfoAirSerialException("No such Port: " + serialPortName);
            }
        } catch (PortInUseException | UnsupportedCommOperationException | IOException e) {
            throw new ComfoAirSerialException(e);
        }
    }

    /**
     * Close serial port.
     */
    public void close() {
        logger.debug("close(): Close ComfoAir connection");
        SerialPort serialPort = this.serialPort;

        if (serialPort != null) {
            ComfoAirCommand command = ComfoAirCommandType.getChangeCommand(ComfoAirCommandType.ACTIVATE.getKey(),
                    OnOffType.OFF);

            if (command != null) {
                sendCommand(command, ComfoAirCommandType.Constants.EMPTY_INT_ARRAY);
            } else {
                logger.debug("Failure while creating COMMAND: {}", command);
            }

            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.debug("Error while closing input stream: {}", e.getMessage());
                }
            }

            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    logger.debug("Error while closing output stream: {}", e.getMessage());
                }
            }

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
        Integer requestValue = command.getRequestValue();
        int retry = 0;

        if (requestCmd != null) {
            // Switch support for app or ccease control
            if (requestCmd == ComfoAirCommandType.Constants.REQUEST_SET_RS232 && requestValue != null) {
                if (requestValue == 1) {
                    isSuspended = false;
                } else if (requestValue == 0) {
                    isSuspended = true;
                }
            } else if (isSuspended) {
                logger.trace("Ignore cmd. Service is currently suspended");
                return ComfoAirCommandType.Constants.EMPTY_INT_ARRAY;
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
                        return ComfoAirCommandType.Constants.EMPTY_INT_ARRAY;
                    }
                }

                byte[] requestBlock = calculateRequest(requestCmd, requestData);
                if (logger.isTraceEnabled()) {
                    logger.trace("send DATA: {}", dumpData(requestBlock));
                }

                if (!send(requestBlock)) {
                    return ComfoAirCommandType.Constants.EMPTY_INT_ARRAY;
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
                            Thread.currentThread().interrupt();
                            logger.warn("Transmission was interrupted: {}", e.getMessage());
                            throw new RuntimeException(e);
                        }
                    } while (inputStream != null && inputStream.available() > 0);

                    // check for ACK
                    if (responseBlock.length >= 2 && responseBlock[0] == ACK[0] && responseBlock[1] == ACK[1]) {
                        if (command.getReplyCmd() == null) {
                            // confirm additional data with an ACK
                            if (responseBlock.length > 2) {
                                send(ACK);
                            }
                            return ComfoAirCommandType.Constants.EMPTY_INT_ARRAY;
                        }

                        boolean isValidData = false;

                        // check for start and end sequence and if the response cmd
                        // matches
                        // 11 is the minimum response length with one data byte
                        if (responseBlock.length >= 11 && responseBlock[2] == START[0] && responseBlock[3] == START[1]
                                && responseBlock[responseBlock.length - 2] == END[0]
                                && responseBlock[responseBlock.length - 1] == END[1]) {
                            if ((responseBlock[5] & 0xff) == command.getReplyCmd()) {
                                isValidData = true;
                            } else {
                                int startIndex = -1;
                                int endIndex = -1;

                                for (int i = 4; i < (responseBlock.length - 11) && endIndex < 0; i++) {
                                    if (responseBlock[i] == START[0] && responseBlock[i + 1] == START[1]
                                            && ((responseBlock[i + 3] & 0xff) == command.getReplyCmd())) {
                                        startIndex = i;
                                        for (int j = startIndex; j < responseBlock.length; j++) {
                                            if (responseBlock[j] == END[0] && responseBlock[j + 1] == END[1]) {
                                                endIndex = j + 1;
                                                break;
                                            }
                                        }
                                    }
                                }

                                if (startIndex > -1 && endIndex > -1) {
                                    byte[] subResponse = new byte[endIndex - startIndex + 3];
                                    System.arraycopy(responseBlock, 0, subResponse, 0, 2);
                                    System.arraycopy(responseBlock, startIndex, subResponse, 2, subResponse.length - 2);
                                    responseBlock = subResponse;
                                    isValidData = true;
                                }
                            }
                        }

                        if (isValidData) {
                            if (logger.isTraceEnabled()) {
                                logger.trace("receive RAW DATA: {}", dumpData(responseBlock));
                            }

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

                                byte[] block = Arrays.copyOf(cleanedBlock, 3 + dataSize);

                                // validate calculated checksum against submitted
                                // checksum
                                if (calculateChecksum(block) == checksum) {
                                    if (logger.isTraceEnabled()) {
                                        logger.trace("receive CMD: {} DATA: {}",
                                                String.format("%02x", command.getReplyCmd()), dumpData(replyData));
                                    }
                                    send(ACK);

                                    return replyData;
                                }

                                logger.debug("Unable to handle data. Checksum verification failed");
                            } else {
                                logger.debug("Unable to handle data. Data size not valid");
                            }

                            if (logger.isTraceEnabled()) {
                                logger.trace("skip CMD: {} DATA: {}", String.format("%02x", command.getReplyCmd()),
                                        dumpData(cleanedBlock));
                            }
                        }
                    }
                } catch (InterruptedIOException e) {
                    Thread.currentThread().interrupt();
                    logger.warn("Transmission was interrupted: {}", e.getMessage());
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    logger.debug("IO error: {}", e.getMessage());
                }

                try {
                    Thread.sleep(1000);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Retry cmd. Last call was not successful. Request: {} Response: {}",
                                dumpData(requestBlock), (responseBlock.length > 0 ? dumpData(responseBlock) : "null"));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warn("Transmission was interrupted: {}", e.getMessage());
                    throw new RuntimeException(e);
                }
            } while (retry++ < MAX_RETRIES);

            if (retry >= MAX_RETRIES) {
                logger.debug("Unable to send command. {} retries failed.", retry);
            }
        }
        return ComfoAirCommandType.Constants.EMPTY_INT_ARRAY;
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

        return (byte) (datasum & 0xFF);
    }

    /**
     * Cleanup a commandblock from quoted 0x07 characters.
     *
     * @param processBuffer
     * @return the 0x07 cleaned byte values
     */
    private byte[] cleanupBlock(byte[] processBuffer) {
        int pos = 0;
        byte[] cleanedBuffer = new byte[processBuffer.length];

        for (int i = 4; i < processBuffer.length - 2; i++) {
            if (CTRL == processBuffer[i] && CTRL == processBuffer[i + 1]) {
                i++;
            }
            cleanedBuffer[pos] = processBuffer[i];
            pos++;
            // Trim unrequested data in response
            if (END[0] == processBuffer[i + 1] && END[1] == processBuffer[i + 2]) {
                break;
            }
        }
        return Arrays.copyOf(cleanedBuffer, pos);
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
            if (CTRL == cleanedBuffer[i]) {
                processBuffer[pos] = CTRL;
                pos++;
            }
            processBuffer[pos] = cleanedBuffer[i];
            pos++;
        }
        return Arrays.copyOf(processBuffer, pos);
    }

    /**
     * Send the byte values.
     *
     * @param request
     * @return successful flag
     */
    private boolean send(byte[] request) {
        if (logger.isTraceEnabled()) {
            logger.trace("send DATA: {}", dumpData(request));
        }

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
        StringBuilder sb = new StringBuilder();
        for (int ch : replyData) {
            sb.append(String.format(" %02x", ch));
        }
        return sb.toString();
    }

    private String dumpData(byte[] data) {
        StringBuilder sb = new StringBuilder();
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

        if (requestCmd != null && dataPosition != null && requestValue != null) {
            switch (requestCmd) {
                case ComfoAirCommandType.Constants.REQUEST_SET_DELAYS:
                    newRequestData = new int[8];

                    if (preRequestData.length > 0 && newRequestData.length <= preRequestData.length) {
                        System.arraycopy(preRequestData, 0, newRequestData, 0, newRequestData.length);
                        newRequestData[dataPosition] = requestValue;
                    } else {
                        return ComfoAirCommandType.Constants.EMPTY_INT_ARRAY;
                    }
                    break;
                case ComfoAirCommandType.Constants.REQUEST_SET_FAN_LEVEL:
                    newRequestData = new int[9];

                    if (preRequestData.length > 0 && newRequestData.length <= preRequestData.length) {
                        System.arraycopy(preRequestData, 0, newRequestData, 0, 6);
                        if (preRequestData.length > 10) {
                            System.arraycopy(preRequestData, 10, newRequestData, 6, newRequestData.length - 6);
                        }
                        newRequestData[dataPosition] = requestValue;
                    } else {
                        return ComfoAirCommandType.Constants.EMPTY_INT_ARRAY;
                    }
                    break;
                case ComfoAirCommandType.Constants.REQUEST_SET_STATES:
                    newRequestData = new int[8];

                    if (preRequestData.length > 0 && newRequestData.length <= preRequestData.length) {
                        if (dataPosition == 4) {
                            requestValue = preRequestData[dataPosition]
                                    + checkByteAndCalculateValue(command, requestValue, preRequestData[dataPosition]);
                        }
                        System.arraycopy(preRequestData, 0, newRequestData, 0, 6);
                        System.arraycopy(preRequestData, 9, newRequestData, 6, newRequestData.length - 6);
                        newRequestData[dataPosition] = requestValue;
                    } else {
                        return ComfoAirCommandType.Constants.EMPTY_INT_ARRAY;
                    }
                    break;
                case ComfoAirCommandType.Constants.REQUEST_SET_GHX:
                    newRequestData = new int[5];

                    if (preRequestData.length > 0 && newRequestData.length <= preRequestData.length) {
                        System.arraycopy(preRequestData, 0, newRequestData, 0, 4);
                        System.arraycopy(preRequestData, 6, newRequestData, 4, newRequestData.length - 4);
                        newRequestData[dataPosition] = requestValue;
                    } else {
                        return ComfoAirCommandType.Constants.EMPTY_INT_ARRAY;
                    }
                    break;
                case ComfoAirCommandType.Constants.REQUEST_SET_ANALOGS:
                    newRequestData = new int[19];

                    if (preRequestData.length > 0 && newRequestData.length <= preRequestData.length) {
                        switch (dataPosition) {
                            case 0:
                            case 1:
                            case 2:
                                requestValue = preRequestData[dataPosition] + checkByteAndCalculateValue(command,
                                        requestValue, preRequestData[dataPosition]);
                        }
                        System.arraycopy(preRequestData, 0, newRequestData, 0, newRequestData.length);
                        newRequestData[dataPosition] = requestValue;
                    } else {
                        return ComfoAirCommandType.Constants.EMPTY_INT_ARRAY;
                    }
                    break;
                default:
                    return ComfoAirCommandType.Constants.EMPTY_INT_ARRAY;
            }
            return newRequestData;
        } else {
            return ComfoAirCommandType.Constants.EMPTY_INT_ARRAY;
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
        if (commandType != null) {
            int[] possibleValues = commandType.getPossibleValues();
            if (possibleValues != null) {
                int possibleValue = possibleValues[0];
                boolean isActive = (preValue & possibleValue) == possibleValue;
                int newValue;

                if (isActive) {
                    newValue = requestValue == 1 ? 0 : -possibleValue;
                } else {
                    newValue = requestValue == 1 ? possibleValue : 0;
                }
                return newValue;
            }
        }
        return 0;
    }

    public boolean getIsSuspended() {
        return isSuspended;
    }
}
