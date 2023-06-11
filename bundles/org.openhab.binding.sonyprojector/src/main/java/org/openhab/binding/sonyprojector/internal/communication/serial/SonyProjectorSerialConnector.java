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
package org.openhab.binding.sonyprojector.internal.communication.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.TooManyListenersException;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sonyprojector.internal.SonyProjectorModel;
import org.openhab.binding.sonyprojector.internal.communication.SonyProjectorConnector;
import org.openhab.binding.sonyprojector.internal.communication.SonyProjectorItem;
import org.openhab.core.i18n.CommunicationException;
import org.openhab.core.i18n.ConnectionException;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortEvent;
import org.openhab.core.io.transport.serial.SerialPortEventListener;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for communicating with Sony Projectors through a serial connection
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class SonyProjectorSerialConnector extends SonyProjectorConnector implements SerialPortEventListener {

    private final Logger logger = LoggerFactory.getLogger(SonyProjectorSerialConnector.class);

    private static final int BAUD_RATE = 38400;
    private static final long READ_TIMEOUT_MS = TimeUnit.MILLISECONDS.toMillis(3500);

    protected static final byte START_CODE = (byte) 0xA9;
    protected static final byte END_CODE = (byte) 0x9A;
    private static final byte GET = (byte) 0x01;
    private static final byte SET = (byte) 0x00;
    protected static final byte TYPE_ACK = (byte) 0x03;
    private static final byte TYPE_ITEM = (byte) 0x02;

    private String serialPortName;
    private SerialPortManager serialPortManager;

    private @Nullable SerialPort serialPort;

    /**
     * Constructor
     *
     * @param serialPortManager the serial port manager
     * @param serialPortName the serial port name to be used
     * @param model the projector model in use
     */
    public SonyProjectorSerialConnector(SerialPortManager serialPortManager, String serialPortName,
            SonyProjectorModel model) {
        this(serialPortManager, serialPortName, model, false);
    }

    /**
     * Constructor
     *
     * @param serialPortManager the serial port manager
     * @param serialPortName the serial port name to be used
     * @param model the projector model in use
     * @param simu whether the communication is simulated or real
     */
    public SonyProjectorSerialConnector(SerialPortManager serialPortManager, String serialPortName,
            SonyProjectorModel model, boolean simu) {
        super(model, simu);

        this.serialPortManager = serialPortManager;
        this.serialPortName = serialPortName;
    }

    @Override
    public synchronized void open() throws ConnectionException {
        if (!connected) {
            logger.debug("Opening serial connection on port {}", serialPortName);
            try {
                SerialPortIdentifier portIdentifier = serialPortManager.getIdentifier(serialPortName);
                if (portIdentifier == null) {
                    throw new ConnectionException("@text/exception.invalid-serial-port", serialPortName);
                }

                SerialPort commPort = portIdentifier.open(this.getClass().getName(), 2000);

                commPort.setSerialPortParams(BAUD_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                        SerialPort.PARITY_EVEN);
                commPort.enableReceiveThreshold(8);
                commPort.enableReceiveTimeout(100);
                commPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

                InputStream dataIn = commPort.getInputStream();
                OutputStream dataOut = commPort.getOutputStream();

                if (dataOut != null) {
                    dataOut.flush();
                }
                if (dataIn != null && dataIn.markSupported()) {
                    try {
                        dataIn.reset();
                    } catch (IOException e) {
                    }
                }

                // RXTX serial port library causes high CPU load
                // Start event listener, which will just sleep and slow down event
                // loop
                try {
                    commPort.addEventListener(this);
                    commPort.notifyOnDataAvailable(true);
                } catch (TooManyListenersException e) {
                    logger.debug("Too Many Listeners Exception: {}", e.getMessage(), e);
                }

                this.serialPort = commPort;
                this.dataIn = dataIn;
                this.dataOut = dataOut;

                connected = true;

                logger.debug("Serial connection opened");
            } catch (PortInUseException | UnsupportedCommOperationException | IOException e) {
                throw new ConnectionException("@text/exception.opening-serial-connection-failed", e);
            }
        }
    }

    @Override
    public synchronized void close() {
        if (connected) {
            logger.debug("Closing serial connection");
            SerialPort serialPort = this.serialPort;
            if (serialPort != null) {
                serialPort.removeEventListener();
            }
            super.close();
            if (serialPort != null) {
                serialPort.close();
                this.serialPort = null;
            }
            connected = false;
        }
    }

    @Override
    protected byte[] buildMessage(SonyProjectorItem item, boolean getCommand, byte[] data) {
        byte[] message = new byte[8];
        message[0] = START_CODE;
        message[1] = item.getCode()[0];
        message[2] = item.getCode()[1];
        message[3] = getCommand ? GET : SET;
        message[4] = data[0];
        message[5] = data[1];
        message[6] = computeCheckSum(message);
        message[7] = END_CODE;
        return message;
    }

    @Override
    protected synchronized byte[] readResponse() throws CommunicationException {
        logger.debug("readResponse (timeout = {} ms)...", READ_TIMEOUT_MS);
        byte[] message = new byte[8];
        boolean startCodeReached = false;
        boolean endCodeReached = false;
        boolean timeout = false;
        byte[] dataBuffer = new byte[8];
        int index = 0;
        long startTimeRead = System.currentTimeMillis();
        while (!endCodeReached && !timeout) {
            logger.trace("readResponse readInput...");
            int len = readInput(dataBuffer);
            logger.trace("readResponse readInput {} => {}", len, HexUtils.bytesToHex(dataBuffer));
            if (len > 0) {
                for (int i = 0; i < len; i++) {
                    if (dataBuffer[i] == START_CODE) {
                        startCodeReached = true;
                    }
                    if (startCodeReached) {
                        if (index < 8) {
                            message[index++] = dataBuffer[i];
                        }
                        if (dataBuffer[i] == END_CODE) {
                            endCodeReached = true;
                        }
                    }
                }
            }
            timeout = (System.currentTimeMillis() - startTimeRead) > READ_TIMEOUT_MS;
        }
        if (!endCodeReached && timeout) {
            logger.debug("readResponse timeout: only {} bytes read after {} ms", index, READ_TIMEOUT_MS);
            throw new CommunicationException("readResponse failed: timeout");
        }
        logger.debug("readResponse: {}", HexUtils.bytesToHex(message));

        return message;
    }

    @Override
    protected void validateResponse(byte[] responseMessage, SonyProjectorItem item) throws CommunicationException {
        if (responseMessage.length != 8) {
            logger.debug("Unexpected response data length: {}", responseMessage.length);
            throw new CommunicationException("Unexpected response data length");
        }

        // Check START CODE
        if (responseMessage[0] != START_CODE) {
            logger.debug("Unexpected message START CODE in response: {} rather than {}",
                    Integer.toHexString(responseMessage[0] & 0x000000FF), Integer.toHexString(START_CODE & 0x000000FF));
            throw new CommunicationException("Unexpected message START CODE in response");
        }

        // Check END CODE
        if (responseMessage[7] != END_CODE) {
            logger.debug("Unexpected  message END CODE in response: {} rather than {}",
                    Integer.toHexString(responseMessage[7] & 0x000000FF), Integer.toHexString(END_CODE & 0x000000FF));
            throw new CommunicationException("Unexpected message END CODE in response");
        }

        byte checksum = computeCheckSum(responseMessage);
        if (responseMessage[6] != checksum) {
            logger.debug("Invalid check sum in response: {} rather than {}",
                    Integer.toHexString(responseMessage[6] & 0x000000FF), Integer.toHexString(checksum & 0x000000FF));
            throw new CommunicationException("Invalid check sum in response");
        }

        if (responseMessage[3] == TYPE_ITEM) {
            // Item number should be the same as used for sending
            byte[] itemResponseMsg = Arrays.copyOfRange(responseMessage, 1, 3);
            if (!Arrays.equals(itemResponseMsg, item.getCode())) {
                logger.debug("Unexpected item number in response: {} rather than {}",
                        HexUtils.bytesToHex(itemResponseMsg), HexUtils.bytesToHex(item.getCode()));
                throw new CommunicationException("Unexpected item number in response");
            }
        } else if (responseMessage[3] == TYPE_ACK) {
            // ACK
            byte[] errorCode = Arrays.copyOfRange(responseMessage, 1, 3);
            if (!Arrays.equals(errorCode, SonyProjectorSerialError.COMPLETE.getDataCode())) {
                String msg = "KO";
                try {
                    SonyProjectorSerialError error = SonyProjectorSerialError.getFromDataCode(errorCode);
                    msg = error.getMessage();
                } catch (CommunicationException e) {
                }
                logger.debug("{} received in response", msg);
                throw new CommunicationException(msg + " received in response");
            }
        } else {
            logger.debug("Unexpected TYPE in response: {}", Integer.toHexString(responseMessage[3] & 0x000000FF));
            throw new CommunicationException("Unexpected TYPE in response");
        }
    }

    private byte computeCheckSum(byte[] message) {
        byte result = 0;
        for (int i = 1; i <= 5; i++) {
            result |= (message[i] & 0x000000FF);
        }
        return result;
    }

    @Override
    protected byte[] getResponseData(byte[] responseMessage) {
        return Arrays.copyOfRange(responseMessage, 4, 6);
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        try {
            logger.debug("RXTX library CPU load workaround, sleep forever");
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
