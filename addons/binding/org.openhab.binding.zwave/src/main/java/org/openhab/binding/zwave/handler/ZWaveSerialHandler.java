/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.handler;

import static org.openhab.binding.zwave.ZWaveBindingConstants.*;

import java.io.IOException;
import java.util.TooManyListenersException;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

/**
 * The {@link ZWaveSerialHandler} is responsible for the serial communications to the ZWave stick.
 * <p>
 * The {@link ZWaveSerialHandler} is a SmartHome bridge. It handles the serial communications, and provides a number of
 * channels that feed back serial communications statistics.
 *
 * @author Chris Jackson - Initial contribution
 */
public class ZWaveSerialHandler extends ZWaveControllerHandler {

    private Logger logger = LoggerFactory.getLogger(ZWaveSerialHandler.class);

    private String portId;

    private SerialPort serialPort;

    private int SOFCount = 0;
    private int CANCount = 0;
    private int NAKCount = 0;
    private int ACKCount = 0;
    private int OOFCount = 0;

    private static final int ZWAVE_RECEIVE_TIMEOUT = 1000;

    private ZWaveReceiveThread receiveThread;

    public ZWaveSerialHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing ZWave serial controller.");

        portId = (String) getConfig().get(CONFIGURATION_PORT);

        if (portId == null || portId.length() == 0) {
            logger.error("ZWave port is not set.");
            return;
        }

        super.initialize();
        logger.info("Connecting to serial port '{}'", portId);
        try {
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portId);
            CommPort commPort = portIdentifier.open("org.openhab.binding.zwave", 2000);
            serialPort = (SerialPort) commPort;
            serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            serialPort.enableReceiveThreshold(1);
            serialPort.enableReceiveTimeout(ZWAVE_RECEIVE_TIMEOUT);
            logger.debug("Starting receive thread");
            receiveThread = new ZWaveReceiveThread();
            receiveThread.start();

            // RXTX serial port library causes high CPU load
            // Start event listener, which will just sleep and slow down event loop
            serialPort.addEventListener(receiveThread);
            serialPort.notifyOnDataAvailable(true);

            logger.info("Serial port is initialized");

            initializeNetwork();
        } catch (NoSuchPortException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Serial Error: Port " + portId + " does not exist");
        } catch (PortInUseException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Serial Error: Port " + portId + " in use");
        } catch (UnsupportedCommOperationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Serial Error: Unsupported comm operation on Port " + portId);
        } catch (TooManyListenersException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Serial Error: Too many listeners on Port " + portId);
        }
    }

    /**
     * Closes the connection to the ZWave controller.
     */
    @Override
    public void dispose() {
        if (receiveThread != null) {
            receiveThread.interrupt();
            try {
                receiveThread.join();
            } catch (InterruptedException e) {
            }
            receiveThread = null;
        }
        if (serialPort != null) {
            serialPort.close();
            serialPort = null;
        }
        logger.info("Stopped ZWave serial handler");

        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // if(channelUID.getId().equals(CHANNEL_1)) {
        // TODO: handle command
        // }
    }

    /**
     * ZWave controller Receive Thread. Takes care of receiving all messages.
     * It uses a semaphore to synchronize communication with the sending thread.
     */
    private class ZWaveReceiveThread extends Thread implements SerialPortEventListener {

        private static final int SOF = 0x01;
        private static final int ACK = 0x06;
        private static final int NAK = 0x15;
        private static final int CAN = 0x18;

        private final Logger logger = LoggerFactory.getLogger(ZWaveReceiveThread.class);

        ZWaveReceiveThread() {
            super("ZWaveReceiveThread");
        }

        @Override
        public void serialEvent(SerialPortEvent arg0) {
            try {
                logger.trace("RXTX library CPU load workaround, sleep forever");
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
            }
        }

        /**
         * Sends 1 byte frame response.
         *
         * @param response
         *            the response code to send.
         */
        private void sendResponse(int response) {
            try {
                synchronized (serialPort.getOutputStream()) {
                    serialPort.getOutputStream().write(response);
                    serialPort.getOutputStream().flush();
                    logger.trace("Response SENT");
                }
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }

        /**
         * Run method. Runs the actual receiving process.
         */
        @Override
        public void run() {
            logger.debug("Starting ZWave thread: Receive");
            try {
                // Send a NAK to resynchronise communications
                sendResponse(NAK);

                while (!interrupted()) {
                    int nextByte;

                    try {
                        nextByte = serialPort.getInputStream().read();

                        if (nextByte == -1) {
                            continue;
                        }
                    } catch (IOException e) {
                        logger.error("Got I/O exception {} during receiving. exiting thread.", e.getLocalizedMessage());
                        break;
                    }

                    switch (nextByte) {
                        case SOF:
                            // Keep track of statistics
                            SOFCount++;
                            updateState(new ChannelUID(getThing().getUID(), CHANNEL_SERIAL_SOF),
                                    new DecimalType(SOFCount));

                            int messageLength;
                            try {
                                messageLength = serialPort.getInputStream().read();
                            } catch (IOException e) {
                                logger.error("Got I/O exception {} during receiving. exiting thread.",
                                        e.getLocalizedMessage());

                                break;
                            }

                            byte[] buffer = new byte[messageLength + 2];
                            buffer[0] = SOF;
                            buffer[1] = (byte) messageLength;
                            int total = 0;

                            while (total < messageLength) {
                                try {
                                    int read = serialPort.getInputStream().read(buffer, total + 2,
                                            messageLength - total);
                                    total += (read > 0 ? read : 0);
                                } catch (IOException e) {
                                    logger.error("Got I/O exception {} during receiving. exiting thread.",
                                            e.getLocalizedMessage());
                                    return;
                                }
                            }

                            logger.debug("Receive Message = {}", SerialMessage.bb2hex(buffer));
                            SerialMessage recvMessage = new SerialMessage(buffer);
                            if (recvMessage.isValid) {
                                logger.trace("Message is valid, sending ACK");
                                sendResponse(ACK);

                                incomingMessage(recvMessage);
                            } else {
                                logger.error("Message is invalid, discarding");
                                sendResponse(NAK);
                            }
                            break;
                        case ACK:
                            // Keep track of statistics
                            ACKCount++;
                            updateState(new ChannelUID(getThing().getUID(), CHANNEL_SERIAL_ACK),
                                    new DecimalType(ACKCount));
                            logger.trace("Received ACK");
                            break;
                        case NAK:
                            NAKCount++;
                            updateState(new ChannelUID(getThing().getUID(), CHANNEL_SERIAL_NAK),
                                    new DecimalType(NAKCount));
                            logger.error("Protocol error (NAK), discarding");

                            // TODO: Add NAK processing
                            break;
                        case CAN:
                            CANCount++;
                            updateState(new ChannelUID(getThing().getUID(), CHANNEL_SERIAL_CAN),
                                    new DecimalType(CANCount));
                            logger.error("Protocol error (CAN), resending");
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                break;
                            }

                            // TODO: Add CAN processing (Resend?)
                            break;
                        default:
                            OOFCount++;
                            updateState(new ChannelUID(getThing().getUID(), CHANNEL_SERIAL_OOF),
                                    new DecimalType(OOFCount));
                            logger.warn(String.format("Protocol error (OOF). Got 0x%02X. Sending NAK.", nextByte));
                            sendResponse(NAK);
                            break;
                    }
                }
            } catch (Exception e) {
                logger.error("Exception during ZWave thread: Receive {}", e);
            }
            logger.debug("Stopped ZWave thread: Receive");

            serialPort.removeEventListener();
        }
    }

    @Override
    public void sendPacket(SerialMessage serialMessage) {
        byte[] buffer = serialMessage.getMessageBuffer();
        if (serialPort == null) {
            logger.debug("NODE {}: Port closed sending REQUEST Message = {}", serialMessage.getMessageNode(),
                    SerialMessage.bb2hex(buffer));
            return;
        }

        logger.debug("NODE {}: Sending REQUEST Message = {}", serialMessage.getMessageNode(),
                SerialMessage.bb2hex(buffer));

        try {
            synchronized (serialPort.getOutputStream()) {
                serialPort.getOutputStream().write(buffer);
                serialPort.getOutputStream().flush();
                logger.trace("Message SENT");
            }
        } catch (IOException e) {
            logger.error("Got I/O exception {} during sending. exiting thread.", e.getLocalizedMessage());
        }
    }
}
