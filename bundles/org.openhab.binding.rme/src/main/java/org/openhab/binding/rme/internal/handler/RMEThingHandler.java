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
package org.openhab.binding.rme.internal.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.TooManyListenersException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.openhab.binding.rme.internal.RMEBindingConstants.DataField;
import org.openhab.binding.rme.internal.RMEThingConfiguration;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortEvent;
import org.openhab.core.io.transport.serial.SerialPortEventListener;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RMEThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Karel Goderis - Initial contribution
 */
public class RMEThingHandler extends BaseThingHandler implements SerialPortEventListener {

    private final Logger logger = LoggerFactory.getLogger(RMEThingHandler.class);

    private static final StringType AUTOMATIC = new StringType("Automatic");
    private static final StringType CITY = new StringType("City");
    private static final StringType MANUAL = new StringType("Manual");
    private static final StringType RAIN = new StringType("Rain");

    private SerialPort serialPort;
    private final SerialPortManager serialPortManager;
    private InputStream inputStream;
    private OutputStream outputStream;
    protected long sleep = 250;
    protected long interval = 5000;
    private Thread readerThread = null;

    public RMEThingHandler(Thing thing, SerialPortManager serialPortManager) {
        super(thing);
        this.serialPortManager = serialPortManager;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing RME handler.");

        RMEThingConfiguration config = getConfigAs(RMEThingConfiguration.class);

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
                logger.info("Connecting to the RME Rain Manager using {}.", config.port);
                serialPort = portIdentifier.open(this.getThing().getUID().getBindingId(), 2000);

                serialPort.setSerialPortParams(config.baudrate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
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
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.warn("The RME Rain Manager is a read-only device and can not handle commands");
    }

    public void onDataReceived(String receivedLine) {
        String line = StringUtils.chomp(receivedLine);

        // little hack to overcome Locale limits of the RME Rain Manager
        // note to the attentive reader : should we add support for system
        // locale's in the Type classes? ;-)
        line = line.replace(",", ".");
        line = line.trim();

        Pattern responsePattern = Pattern.compile("(.*);(0|1);(0|1);(0|1);(0|1);(0|1);(0|1);(0|1);(0|1);(0|1)");

        try {
            logger.trace("Processing '{}'", line);

            Matcher matcher = responsePattern.matcher(line);
            if (matcher.matches()) {
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    switch (DataField.get(i)) {
                        case LEVEL: {
                            DecimalType decimalType = new DecimalType(matcher.group(i));
                            updateState(new ChannelUID(getThing().getUID(), DataField.get(i).channelID()), decimalType);
                            break;
                        }
                        case MODE: {
                            StringType stringType = null;
                            if (matcher.group(i).equals("0")) {
                                stringType = MANUAL;
                            } else if (matcher.group(i).equals("1")) {
                                stringType = AUTOMATIC;
                            }
                            if (stringType != null) {
                                updateState(new ChannelUID(getThing().getUID(), DataField.get(i).channelID()),
                                        stringType);
                            }
                            break;
                        }
                        case SOURCE: {
                            StringType stringType = null;
                            if (matcher.group(i).equals("0")) {
                                stringType = RAIN;
                            } else if (matcher.group(i).equals("1")) {
                                stringType = CITY;
                            }
                            if (stringType != null) {
                                updateState(new ChannelUID(getThing().getUID(), DataField.get(i).channelID()),
                                        stringType);
                            }
                            break;
                        }
                        default:
                            if (matcher.group(i).equals("0")) {
                                updateState(new ChannelUID(getThing().getUID(), DataField.get(i).channelID()),
                                        OnOffType.OFF);
                            } else if (matcher.group(i).equals("1")) {
                                updateState(new ChannelUID(getThing().getUID(), DataField.get(i).channelID()),
                                        OnOffType.ON);
                            }
                            break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("An exception occurred while receiving data : '{}'", e.getMessage(), e);
        }
    }

    /**
     * Write data to the serial port
     *
     * @param msg the received data as a String
     *
     **/
    public void writeString(String msg) {
        RMEThingConfiguration config = getConfigAs(RMEThingConfiguration.class);

        try {
            // write string to serial port
            outputStream.write(msg.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error writing '" + msg + "' to serial port " + config.port + " : " + e.getMessage());
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

    public class SerialPortReader extends Thread {

        RMEThingConfiguration config = getConfigAs(RMEThingConfiguration.class);

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
            byte[] dataBuffer = new byte[config.buffer];
            byte[] tmpData = new byte[config.buffer];
            int index = 0;
            int len = -1;
            boolean foundStart = false;

            logger.debug("Serial port listener for serial port '{}' has started", config.port);

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

                            if (index == config.buffer) {
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
                logger.error("An exception occurred while reading serial port '{}' : {}", config.port, e.getMessage(),
                        e);
            }

            logger.debug("Serial port listener for serial port '{}' has stopped", config.port);
        }
    }
}
