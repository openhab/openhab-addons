/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.oceanic.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.eclipse.smarthome.core.types.TypeParser;
import org.openhab.binding.oceanic.OceanicBindingConstants.OceanicChannelSelector;
import org.openhab.binding.oceanic.internal.SerialPortThrottler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.RXTXCommDriver;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

/**
 * The {@link OceanicHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Karel Goderis - Initial contribution
 */
public class OceanicThingHandler extends BaseThingHandler {

    // List of Configuration constants
    public static final String INTERVAL = "interval";
    public static final String PORT = "port";
    public static final String BAUD_RATE = "baud";
    public static final String BUFFER_SIZE = "buffer";

    private Logger logger = LoggerFactory.getLogger(OceanicThingHandler.class);

    private SerialPort serialPort;
    private CommPortIdentifier portId;
    private InputStream inputStream;
    private OutputStream outputStream;
    private int baud;
    private String port;
    private int bufferSize;

    private ScheduledFuture<?> pollingJob;
    private SerialPortReader readerThread = null;
    private static String lastLineReceived = "";
    private long REQUEST_TIMEOUT = 10000;

    public OceanicThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Oceanic handler.");

        if (getConfig().get(BAUD_RATE) == null) {
            baud = 19200;
        } else {
            baud = (int) getConfig().get(BAUD_RATE);
        }

        if (getConfig().get(BUFFER_SIZE) == null) {
            bufferSize = 1024;
        } else {
            bufferSize = (int) getConfig().get(BUFFER_SIZE);
        }

        port = (String) getConfig().get(PORT);

        if (serialPort == null && port != null && baud != 0) {

            if (portId == null) {
                try {
                    RXTXCommDriver rxtxCommDriver = new RXTXCommDriver();
                    rxtxCommDriver.initialize();
                    CommPortIdentifier.addPortName(port, CommPortIdentifier.PORT_RAW, rxtxCommDriver);
                    portId = CommPortIdentifier.getPortIdentifier(port);
                } catch (NoSuchPortException e) {
                    logger.error("An exception occurred while setting up serial port '{}' : '{}'", port, e.getMessage(),
                            e);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Could not setup serial port " + serialPort + ": " + e.getMessage());
                    return;
                }
            }

            if (portId != null) {
                try {
                    serialPort = portId.open(this.getThing().getUID().getBindingId(), 2000);
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

                serialPort.notifyOnDataAvailable(true);

                try {
                    serialPort.setSerialPortParams(baud, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                            SerialPort.PARITY_NONE);
                    serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
                } catch (UnsupportedCommOperationException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Could not configure serial port " + serialPort + ": " + e.getMessage());
                    return;
                }

                try {
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
                @SuppressWarnings("rawtypes")
                Enumeration portList = CommPortIdentifier.getPortIdentifiers();
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

        if (pollingJob == null || pollingJob.isCancelled()) {
            pollingJob = scheduler.scheduleWithFixedDelay(pollingRunnable, 1,
                    ((BigDecimal) getConfig().get(INTERVAL)).intValue(), TimeUnit.SECONDS);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Oceanic handler.");
        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
            pollingJob = null;
        }

        if (readerThread != null) {
            try {
                readerThread.interrupt();
                readerThread.join();
            } catch (InterruptedException e) {
                logger.error("An exception occurred while interrupting the serial port reader thread : {}",
                        e.getMessage(), e);
            }
        }

        IOUtils.closeQuietly(inputStream);
        IOUtils.closeQuietly(outputStream);
        if (serialPort != null) {
            serialPort.close();
        }
    }

    private Runnable pollingRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                if (getThing().getStatus() == ThingStatus.ONLINE) {
                    for (Channel aChannel : getThing().getChannels()) {
                        for (OceanicChannelSelector selector : OceanicChannelSelector.values()) {
                            ChannelUID theChannelUID = new ChannelUID(getThing().getUID(), selector.toString());
                            if (aChannel.getUID().equals(theChannelUID)
                                    && selector.getTypeValue() == OceanicChannelSelector.ValueSelectorType.GET) {
                                String response = requestResponse(selector.name());
                                if (response != "") {
                                    if (selector.isProperty()) {
                                        logger.debug("Updating the property '{}' with value '{}'", selector.toString(),
                                                selector.convertValue(response));
                                        Map<String, String> properties = editProperties();
                                        properties.put(selector.toString(), selector.convertValue(response));
                                        updateProperties(properties);
                                    } else {
                                        State value = createStateForType(selector, response);
                                        updateState(theChannelUID, value);
                                    }
                                } else {
                                    logger.warn("Received an empty answer for '{}'", selector.name());
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("An exception occurred while polling the Oceanic Water Softener: '{}'", e.getMessage());
            }
        }
    };

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (getThing().getStatus() == ThingStatus.ONLINE) {
            if (!(command instanceof RefreshType)) {
                String commandAsString = command.toString();
                String channelID = channelUID.getId();

                for (Channel aChannel : getThing().getChannels()) {
                    if (aChannel.getUID().equals(channelUID)) {
                        try {
                            OceanicChannelSelector selector = OceanicChannelSelector.getValueSelector(channelID,
                                    OceanicChannelSelector.ValueSelectorType.SET);

                            switch (selector) {
                                case setSV1:
                                    commandAsString = selector.name() + commandAsString;
                                    break;
                                default:
                                    commandAsString = selector.name();
                                    break;
                            }
                            String response = requestResponse(commandAsString);
                            if (response.equals("ERR")) {
                                logger.error("An error occurred while setting '{}' to {}", selector.toString(),
                                        commandAsString);
                            }
                        } catch (IllegalArgumentException e) {
                            logger.warn(
                                    "An error occurred while trying to set the read-only variable associated with channel '{}' to '{}'",
                                    channelID, command.toString());
                        }
                        break;
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private State createStateForType(OceanicChannelSelector selector, String value) {

        Class<? extends Type> typeClass = selector.getTypeClass();
        List<Class<? extends State>> stateTypeList = new ArrayList<Class<? extends State>>();

        stateTypeList.add((Class<? extends State>) typeClass);

        State state = TypeParser.parseState(stateTypeList, selector.convertValue(value));

        return state;
    }

    private String requestResponse(String commandAsString) {
        synchronized (this) {
            SerialPortThrottler.lock(port);

            lastLineReceived = "";
            String response = null;
            writeString(commandAsString + "\r");
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

            SerialPortThrottler.unlock(port);

            return response;
        }
    }

    public void writeString(String msg) {
        String port = (String) this.getConfig().get(PORT);

        try {
            if (logger.isTraceEnabled()) {
                logger.trace("writeString : {} ('{}')", msg, msg.getBytes());
            }
            outputStream.write(msg.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error writing '" + msg + "' to serial port " + port + " : " + e.getMessage());
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
            if (logger.isTraceEnabled()) {
                logger.trace("Resetting the SerialPortReader");
            }
            index = 0;
        }

        @Override
        public void interrupt() {
            if (logger.isTraceEnabled()) {
                logger.trace("Interrupting the SerialPortReader");
            }
            interrupted = true;
            super.interrupt();
            try {
                inputStream.close();
            } catch (IOException e) {
                // quietly clos
            }
        }

        @Override
        public void run() {

            if (logger.isTraceEnabled()) {
                logger.trace("Starting the serial port reader");
            }

            byte[] dataBuffer = new byte[bufferSize];
            byte[] tmpData = new byte[bufferSize];
            String line;

            final byte lineFeed = (byte) '\n';
            final byte carriageReturn = (byte) '\r';
            final byte nullChar = (byte) '\0';

            long sleep = 10;
            int len = -1;

            try {
                while (interrupted != true) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Reading the inputStream");
                    }

                    if ((len = inputStream.read(tmpData)) > -1) {
                        if (logger.isTraceEnabled()) {
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < len; i++) {
                                sb.append(String.format("%02X", tmpData[i]));
                                sb.append(" ");
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
            }
        }
    }
}
