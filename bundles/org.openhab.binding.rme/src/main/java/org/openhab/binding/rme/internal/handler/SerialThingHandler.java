/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.rme.internal.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.TooManyListenersException;
import java.util.stream.Collectors;

import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortEvent;
import org.openhab.core.io.transport.serial.SerialPortEventListener;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SerialThingHandler} is responsible for handling commands, which
 * are sent to one of the channels. Thing Handler classes that use serial
 * communications can extend/implement this class, but must make sure they
 * supplement the configuration parameters into the {@link SerialConfiguration}
 * Configuration of the underlying Thing, if not already specified in the
 * thing.xml definition
 *
 * @author Karel Goderis - Initial contribution
 */
public abstract class SerialThingHandler extends BaseThingHandler implements SerialPortEventListener {

    // List of all Configuration parameters
    public static final String PORT = "port";
    public static final String BAUD_RATE = "baud";
    public static final String BUFFER_SIZE = "buffer";

    private final Logger logger = LoggerFactory.getLogger(SerialThingHandler.class);

    private SerialPort serialPort;
    private SerialPortIdentifier portId;
    private final SerialPortManager serialPortManager;
    private InputStream inputStream;
    private OutputStream outputStream;
    protected int baud;
    protected String port;
    protected int bufferSize;
    protected long sleep = 100;
    protected long interval = 0;
    private Thread readerThread = null;

    public SerialThingHandler(Thing thing, SerialPortManager serialPortManager) {
        super(thing);
        this.serialPortManager = serialPortManager;
    }

    /**
     * Called when data is received on the serial port
     *
     * @param line the received data as a String
     *
     **/
    public abstract void onDataReceived(String line);

    /**
     * Write data to the serial port
     *
     * @param msg the received data as a String
     *
     **/
    public void writeString(String msg) {
        String port = (String) this.getConfig().get(PORT);

        try {
            // write string to serial port
            outputStream.write(msg.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error writing '" + msg + "' to serial port " + port + " : " + e.getMessage());
        }
    }

    @Override
    public void serialEvent(SerialPortEvent arg0) {
        try {
            /*
             * The short select() timeout in the native code of the nrjavaserial lib does cause a high CPU load, despite
             * the fix published (see https://github.com/NeuronRobotics/nrjavaserial/issues/22). A workaround for this
             * problem is to (1) put the Thread initiated by the nrjavaserial library to sleep forever, so that the
             * number of calls to the select() function gets minimized, and (2) implement a Threaded streamreader
             * directly in java
             */
            logger.trace("RXTX library CPU load workaround, sleep forever");
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing serial thing handler.");
        if (serialPort != null) {
            serialPort.removeEventListener();
        }
        if (readerThread != null) {
            try {
                readerThread.interrupt();
                readerThread.join();
            } catch (InterruptedException e) {
            }
        }
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                logger.debug("Error while closing the input stream: {}", e.getMessage());
            }
        }
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                logger.debug("Error while closing the output stream: {}", e.getMessage());
            }
        }
        if (serialPort != null) {
            serialPort.close();
        }
        readerThread = null;
        inputStream = null;
        outputStream = null;
        serialPort = null;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing serial thing handler.");

        if (baud == 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Baud rate is not configured");
            return;
        } else if (port == null || port.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Serial port is not configured");
            return;
        }

        portId = serialPortManager.getIdentifier(port);

        if (portId == null) {
            String availablePorts = serialPortManager.getIdentifiers().map(id -> id.getName())
                    .collect(Collectors.joining(System.lineSeparator()));
            String description = String.format("Serial port '%s' could not be found. Available ports are:%n%s", port,
                    availablePorts);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, description);
            return;

        }

        // initialize serial port
        try {
            serialPort = portId.open("openHAB", 2000);
        } catch (PortInUseException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Could not open serial port " + port + ": " + e.getMessage());
            return;
        }

        try {
            inputStream = serialPort.getInputStream();
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Could not open serial port " + port + ": " + e.getMessage());
            return;
        }

        try {
            serialPort.addEventListener(this);
        } catch (TooManyListenersException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Could not open serial port " + port + ": " + e.getMessage());
            return;
        }

        // activate the DATA_AVAILABLE notifier
        serialPort.notifyOnDataAvailable(true);

        try {
            // set port parameters
            serialPort.setSerialPortParams(baud, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
        } catch (UnsupportedCommOperationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Could not configure serial port " + port + ": " + e.getMessage());
            return;
        }

        try {
            // get the output stream
            outputStream = serialPort.getOutputStream();
            updateStatus(ThingStatus.ONLINE);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Could not communicate with the serial port " + port + ": " + e.getMessage());
            return;
        }

        readerThread = new SerialPortReader(inputStream);
        readerThread.start();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // by default, we write anything we received as a string to the serial port
        writeString(command.toString());
    }

    public class SerialPortReader extends Thread {

        private static final byte LINE_FEED = (byte) '\n';
        private static final byte CARRIAGE_RETURN = (byte) '\r';

        private boolean interrupted = false;
        private InputStream inputStream;
        private boolean hasInterval = interval == 0 ? false : true;

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss,SSS");

        public SerialPortReader(InputStream in) {
            this.inputStream = in;
            this.setName("SerialPortReader-" + getThing().getUID());
        }

        @Override
        public void interrupt() {
            interrupted = true;
            super.interrupt();
        }

        @Override
        public void run() {
            byte[] dataBuffer = new byte[bufferSize];
            byte[] tmpData = new byte[bufferSize];
            int index = 0;
            int len = -1;
            boolean foundStart = false;

            logger.debug("Serial port listener for serial port '{}' has started", port);

            try {
                while (!interrupted) {
                    long startOfRead = System.currentTimeMillis();

                    if ((len = inputStream.read(tmpData)) > 0) {
                        foundStart = false;
                        for (int i = 0; i < len; i++) {
                            if (hasInterval && i > 0) {
                                if (tmpData[i] != LINE_FEED && tmpData[i] != CARRIAGE_RETURN) {
                                    if (tmpData[i - 1] == LINE_FEED || tmpData[i - 1] == CARRIAGE_RETURN) {
                                        index = 0;
                                        foundStart = true;
                                    }
                                }
                            }

                            if (tmpData[i] != LINE_FEED && tmpData[i] != CARRIAGE_RETURN) {
                                dataBuffer[index++] = tmpData[i];
                            }

                            if (tmpData[i] == LINE_FEED || tmpData[i] == CARRIAGE_RETURN) {
                                if (index > 1) {
                                    if (hasInterval) {
                                        if (foundStart) {
                                            onDataReceived(new String(Arrays.copyOf(dataBuffer, index)));
                                            break;
                                        } else {
                                            index = 0;
                                            foundStart = true;
                                        }
                                    } else {
                                        onDataReceived(new String(Arrays.copyOf(dataBuffer, index)));
                                        index = 0;
                                    }
                                }
                            }

                            if (index == bufferSize) {
                                if (!hasInterval) {
                                    onDataReceived(new String(Arrays.copyOf(dataBuffer, index)));
                                }
                                index = 0;
                            }
                        }

                        if (hasInterval) {
                            try {
                                Thread.sleep(Math.max(interval - (System.currentTimeMillis() - startOfRead), 0));
                            } catch (InterruptedException e) {
                            }
                        }
                    }

                    try {
                        Thread.sleep(sleep);
                    } catch (InterruptedException e) {
                    }

                }
            } catch (InterruptedIOException e) {
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                logger.error("An exception occurred while reading serial port '{}' : {}", port, e.getMessage(), e);
            }

            logger.debug("Serial port listener for serial port '{}' has stopped", port);
        }
    }
}
