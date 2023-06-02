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
package org.openhab.binding.oceanic.internal.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.TooManyListenersException;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.openhab.binding.oceanic.internal.SerialOceanicBindingConfiguration;
import org.openhab.binding.oceanic.internal.Throttler;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortEvent;
import org.openhab.core.io.transport.serial.SerialPortEventListener;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SerialOceanicThingHandler} implements {@link OceanicThingHandler} for an Oceanic water softener that is
 * directly connected to a serial port of the openHAB host
 *
 * @author Karel Goderis - Initial contribution
 */
public class SerialOceanicThingHandler extends OceanicThingHandler implements SerialPortEventListener {

    private static final long REQUEST_TIMEOUT = 10000;
    private static final int BAUD = 19200;

    private final Logger logger = LoggerFactory.getLogger(SerialOceanicThingHandler.class);

    private final SerialPortManager serialPortManager;
    private SerialPort serialPort;
    private InputStream inputStream;
    private OutputStream outputStream;
    private SerialPortReader readerThread;

    public SerialOceanicThingHandler(Thing thing, SerialPortManager serialPortManager) {
        super(thing);
        this.serialPortManager = serialPortManager;
    }

    @Override
    public void initialize() {
        super.initialize();

        SerialOceanicBindingConfiguration config = getConfigAs(SerialOceanicBindingConfiguration.class);

        if (serialPort == null && config.port != null) {

            SerialPortIdentifier portIdentifier = serialPortManager.getIdentifier(config.port);

            if (portIdentifier == null) {
                String availablePorts = serialPortManager.getIdentifiers().map(id -> id.getName())
                        .collect(Collectors.joining(System.lineSeparator()));
                String description = String.format("Serial port '%s' could not be found. Available ports are:%n%s",
                        config.port, availablePorts);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, description);
                return;
            }

            try {
                logger.info("Connecting to the Oceanic water softener using {}.", config.port);
                serialPort = portIdentifier.open(this.getThing().getUID().getBindingId(), 2000);

                serialPort.setSerialPortParams(BAUD, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);
                serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
                serialPort.enableReceiveThreshold(1);
                serialPort.disableReceiveTimeout();

                inputStream = serialPort.getInputStream();
                outputStream = serialPort.getOutputStream();

                serialPort.addEventListener(this);
                serialPort.notifyOnDataAvailable(true);

                readerThread = new SerialPortReader(inputStream);
                readerThread.start();

                updateStatus(ThingStatus.ONLINE);

            } catch (PortInUseException portInUseException) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Port in use: " + config.port);
            } catch (UnsupportedCommOperationException | IOException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Communication error");
            } catch (TooManyListenersException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Too many listeners to serial port.");
            }
        }
    }

    @Override
    public void dispose() {
        if (readerThread != null) {
            try {
                readerThread.interrupt();
                readerThread.join();
            } catch (InterruptedException e) {
                logger.error("An exception occurred while interrupting the serial port reader thread : {}",
                        e.getMessage(), e);
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

        super.dispose();
    }

    @Override
    protected String requestResponse(String commandAsString) {
        synchronized (this) {
            SerialOceanicBindingConfiguration config = getConfigAs(SerialOceanicBindingConfiguration.class);

            Throttler.lock(config.port);

            lastLineReceived = "";
            String request = commandAsString + "\r";
            String response = null;

            try {
                if (logger.isTraceEnabled()) {
                    logger.trace("Requesting : {} ('{}')", request, request.getBytes());
                }
                outputStream.write(request.getBytes());
                outputStream.flush();
            } catch (IOException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Error writing '" + request + "' to serial port " + config.port + " : " + e.getMessage());
            }

            long timeStamp = System.currentTimeMillis();
            while (lastLineReceived.equals("")) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    logger.error("An exception occurred while putting the thread to sleep: {}", e.getMessage());
                }
                if (System.currentTimeMillis() - timeStamp > REQUEST_TIMEOUT) {
                    logger.warn("A timeout occurred while requesting data from the water softener");
                    readerThread.reset();
                    break;
                }
            }
            response = lastLineReceived;

            Throttler.unlock(config.port);

            return response;
        }
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        if (logger.isTraceEnabled()) {
            logger.trace("Received a serial port event : {}", serialPortEvent.getEventType());
        }
    }

    public class SerialPortReader extends Thread {

        private boolean interrupted = false;
        private InputStream inputStream;
        int index = 0;

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss,SSS");

        public SerialPortReader(InputStream in) {
            this.inputStream = in;
            this.setName("SerialPortReader-" + getThing().getUID());
        }

        public void reset() {
            logger.trace("Resetting the SerialPortReader");
            index = 0;
        }

        @Override
        public void interrupt() {
            logger.trace("Interrupting the SerialPortReader");
            interrupted = true;
            super.interrupt();
        }

        @Override
        public void run() {
            logger.trace("Starting the serial port reader");

            byte[] dataBuffer = new byte[bufferSize];
            byte[] tmpData = new byte[bufferSize];
            String line;

            final byte lineFeed = (byte) '\n';
            final byte carriageReturn = (byte) '\r';
            final byte nullChar = (byte) '\0';

            long sleep = 10;
            int len = -1;

            try {
                while (!interrupted) {
                    logger.trace("Reading the inputStream");

                    if ((len = inputStream.read(tmpData)) > -1) {
                        if (logger.isTraceEnabled()) {
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < len; i++) {
                                sb.append(String.format("%02X ", tmpData[i]));
                            }
                            logger.trace("Read {} bytes : {}", len, sb.toString());
                        }
                        for (int i = 0; i < len; i++) {
                            if (logger.isTraceEnabled()) {
                                logger.trace("Byte {} equals '{}' (hex '{}')", i, new String(new byte[] { tmpData[i] }),
                                        String.format("%02X", tmpData[i]));
                            }

                            if (tmpData[i] != lineFeed && tmpData[i] != carriageReturn && tmpData[i] != nullChar) {
                                dataBuffer[index++] = tmpData[i];
                                if (logger.isTraceEnabled()) {
                                    logger.trace("dataBuffer[{}] set to '{}'(hex '{}')", index - 1,
                                            new String(new byte[] { dataBuffer[index - 1] }),
                                            String.format("%02X", dataBuffer[index - 1]));
                                }
                            }

                            if (i > 0 && (tmpData[i] == lineFeed || tmpData[i] == carriageReturn
                                    || tmpData[i] == nullChar)) {
                                if (index > 0) {
                                    if (logger.isTraceEnabled()) {
                                        logger.trace("The resulting line is '{}'",
                                                new String(Arrays.copyOf(dataBuffer, index)));
                                    }
                                    line = StringUtils.chomp(new String(Arrays.copyOf(dataBuffer, index)));
                                    line = line.replace(",", ".");
                                    line = line.trim();
                                    index = 0;

                                    lastLineReceived = line;
                                    break;
                                }
                            }

                            if (index == bufferSize) {
                                index = 0;
                            }
                        }
                    }

                    try {
                        Thread.sleep(sleep);
                    } catch (InterruptedException e) {
                        // Move on silently
                    }
                }
            } catch (Exception e) {
                logger.error("An exception occurred while reading serial port  : {}", e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    }
}
