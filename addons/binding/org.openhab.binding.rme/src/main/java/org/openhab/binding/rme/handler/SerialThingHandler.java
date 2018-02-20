/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rme.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import org.apache.commons.io.IOUtils;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

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

    private Logger logger = LoggerFactory.getLogger(SerialThingHandler.class);

    private SerialPort serialPort;
    private CommPortIdentifier portId;
    private InputStream inputStream;
    private OutputStream outputStream;
    protected int baud;
    protected String port;
    protected int bufferSize;
    protected long sleep = 100;
    protected long interval = 0;
    Thread readerThread = null;

    public SerialThingHandler(Thing thing) {
        super(thing);
    }

    /**
     * Called when data is received on the serial port
     *
     * @param line
     *            - the received data as a String
     *
     **/
    public abstract void onDataReceived(String line);

    /**
     * Write data to the serial port
     *
     * @param msg
     *            - the received data as a String
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
        IOUtils.closeQuietly(inputStream);
        IOUtils.closeQuietly(outputStream);
        if (serialPort != null) {
            serialPort.close();
        }

        if (readerThread != null) {
            try {
                readerThread.interrupt();
                readerThread.join();
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing serial thing handler.");

        if (serialPort == null && port != null && baud != 0) {

            // parse ports and if the default port is found, initialized the
            // reader
            @SuppressWarnings("rawtypes")
            Enumeration portList = CommPortIdentifier.getPortIdentifiers();
            while (portList.hasMoreElements()) {
                CommPortIdentifier id = (CommPortIdentifier) portList.nextElement();
                if (id.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                    if (id.getName().equals(port)) {
                        logger.debug("Serial port '{}' has been found.", port);
                        portId = id;
                    }
                }
            }

            if (portId != null) {
                // initialize serial port
                try {
                    serialPort = portId.open("openHAB", 2000);
                } catch (PortInUseException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Could not open serial port " + serialPort + ": " + e.getMessage());
                    return;
                }

                try {
                    inputStream = serialPort.getInputStream();
                } catch (IOException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Could not open serial port " + serialPort + ": " + e.getMessage());
                    return;
                }

                try {
                    serialPort.addEventListener(this);
                } catch (TooManyListenersException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Could not open serial port " + serialPort + ": " + e.getMessage());
                    return;
                }

                // activate the DATA_AVAILABLE notifier
                serialPort.notifyOnDataAvailable(true);

                try {
                    // set port parameters
                    serialPort.setSerialPortParams(baud, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                            SerialPort.PARITY_NONE);
                } catch (UnsupportedCommOperationException e) {

                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Could not configure serial port " + serialPort + ": " + e.getMessage());
                    return;
                }

                try {
                    // get the output stream
                    outputStream = serialPort.getOutputStream();
                    updateStatus(ThingStatus.ONLINE);
                } catch (IOException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Could not communicate with the serial port " + serialPort + ": " + e.getMessage());
                    return;
                }

                readerThread = new SerialPortReader(inputStream);
                readerThread.start();

            } else {
                StringBuilder sb = new StringBuilder();
                portList = CommPortIdentifier.getPortIdentifiers();
                while (portList.hasMoreElements()) {
                    CommPortIdentifier id = (CommPortIdentifier) portList.nextElement();
                    if (id.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                        sb.append(id.getName() + "\n");
                    }
                }
                logger.error("Serial port '{}' could not be found. Available ports are:\n {}", port, sb);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // by default, we write anything we received as a string to the serial
        // port
        writeString(command.toString());
    }

    public class SerialPortReader extends Thread {

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
            try {
                inputStream.close();
            } catch (IOException e) {
            } // quietly close
        }

        @Override
        public void run() {

            byte[] dataBuffer = new byte[bufferSize];
            byte[] tmpData = new byte[bufferSize];
            int index = 0;
            int len = -1;
            boolean foundStart = false;

            final byte LINE_FEED = (byte) '\n';
            final byte CARRIAGE_RETURN = (byte) '\r';

            logger.debug("Serial port listener for serial port '{}' has started", port);

            try {
                while (interrupted != true) {
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