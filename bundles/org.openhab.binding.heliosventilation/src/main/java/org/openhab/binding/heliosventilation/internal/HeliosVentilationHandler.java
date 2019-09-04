/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.heliosventilation.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.TooManyListenersException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
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
 * The {@link HeliosVentilationHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Raphael Mack - Initial contribution
 */
@NonNullByDefault
public class HeliosVentilationHandler extends BaseThingHandler implements SerialPortEventListener {
    private static final int BUSMEMBER_MAINBOARD = 0x11;
    private static final int BUSMEMBER_SLAVEBOARDS = 0x10;
    private static final byte BUSMEMBER_CONTROLBOARDS = (byte) 0x20;
    private static final int BUSMEMBER_REC_MASK = 0xF0; // interpreting frames delivered to BUSMEMBER_ME &
                                                        // BUSMEMBER_REC_MASK
    private static final int BUSMEMBER_ME = 0x2F; // used as sender when communicating with the helios system
    private static final int POLL_OFFLINE_THRESHOLD = 3;

    private static final HashMap<Byte, HeliosVentilationDataPoint> Datapoints;
    private static final Logger Logger;
    static {
        /* logger is used by readChannelProperties() so we need to initialize logger first. */
        Logger = LoggerFactory.getLogger(HeliosVentilationHandler.class);
        Datapoints = readChannelProperties();
    }

    private @NonNullByDefault({}) HeliosVentilationConfiguration config;

    private @NonNullByDefault({}) SerialPortManager serialPortManager;

    private @Nullable SerialPortIdentifier portId;
    private @Nullable SerialPort serialPort;
    private @Nullable InputStream inputStream;
    private @Nullable OutputStream outputStream;

    private @Nullable ScheduledFuture<?> pollingTask;
    private int pollCounter;

    /**
     * store received data for read-modify-write operations on bitlevel
     */
    private HashMap<Byte, Byte> memory = new HashMap<Byte, Byte>();

    public HeliosVentilationHandler(Thing thing, final SerialPortManager serialPortManager) {
        super(thing);
        this.serialPortManager = serialPortManager;
    }

    /**
     * parse datapoints from properties
     *
     * @return
     */
    private static HashMap<Byte, HeliosVentilationDataPoint> readChannelProperties() {
        HashMap<Byte, HeliosVentilationDataPoint> result = new HashMap<Byte, HeliosVentilationDataPoint>();

        URL resource = Thread.currentThread().getContextClassLoader()
                .getResource(HeliosVentilationBindingConstants.DATAPOINT_FILE);
        Properties properties = new Properties();
        try {
            properties.load(resource.openStream());

            Enumeration<Object> keys = properties.keys();
            while (keys.hasMoreElements()) {
                String channel = (String) keys.nextElement();
                Logger.error("reading channel {} = {}", channel, properties.getProperty(channel));
                HeliosVentilationDataPoint dp;
                try {
                    dp = new HeliosVentilationDataPoint(channel, properties.getProperty(channel));
                    if (result.containsKey(dp.address())) {
                        result.get(dp.address()).append(dp);
                    } else {
                        result.put(dp.address(), dp);
                    }
                } catch (HeliosPropertiesFormatException e) {
                    Logger.error("could not read resource file {}, binding will probably fail: {}",
                            HeliosVentilationBindingConstants.DATAPOINT_FILE, e.getMessage());
                }
            }
        } catch (Exception e) {
            Logger.error("could not read resource file {}, binding will probably fail: {}",
                    HeliosVentilationBindingConstants.DATAPOINT_FILE, e.getMessage());
        }

        return result;
    }

    @Override
    public void initialize() {
        config = getConfigAs(HeliosVentilationConfiguration.class);

        Logger.debug("   Serial Port: {},", config.serialPort);
        Logger.debug("   Baud:        {},", 9600);
        Logger.debug("   PollPeriod:  {},", config.pollPeriod);

        if (config.serialPort.length() < 1) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Port must be set!");
            return;
        } else {
            updateStatus(ThingStatus.UNKNOWN);
            if (this.config.pollPeriod > 0) {
                startPolling();
            }
        }

        scheduler.execute(() -> {
            connect();
        });
    }

    @SuppressWarnings("null")
    private synchronized void connect() {
        Logger.debug("HeliosVentilation: connecting...");
        // parse ports and if the port is found, initialize the reader
        portId = serialPortManager.getIdentifier(config.serialPort);
        if (portId == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Port " + config.serialPort + " is not known!");
            serialPort = null;
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        } else if (!isConnected()) {
            // initialize serial port
            try {
                serialPort = portId.open(getThing().getUID().toString(), 2000);
                serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);
                serialPort.addEventListener(this);

                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly(outputStream);
                inputStream = serialPort.getInputStream();
                outputStream = serialPort.getOutputStream();

                // activate the DATA_AVAILABLE notifier
                serialPort.notifyOnDataAvailable(true);
                updateStatus(ThingStatus.UNKNOWN);
            } catch (final IOException ex) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "I/O error!");
            } catch (PortInUseException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "Port is in use!");
            } catch (TooManyListenersException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        "Cannot attach listener to port!");
            } catch (UnsupportedCommOperationException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        "Serial port does not support the RS485 parameters of the Helios remote protocol.");
            }
        }
    }

    @Override
    public void dispose() {
        stopPolling();
        disconnect();
        super.dispose();
    }

    /**
     * Start the polling task.
     */
    @SuppressWarnings("null")
    public synchronized void startPolling() {
        if (pollingTask != null && pollingTask.isCancelled()) {
            pollingTask.cancel(true);
        }
        if (config.pollPeriod > 0) {
            pollingTask = scheduler.scheduleWithFixedDelay(this::polling, 0, config.pollPeriod, TimeUnit.SECONDS);
        } else {
            pollingTask = null;
        }
    }

    /**
     * Stop the polling task.
     */
    @SuppressWarnings("null")
    public synchronized void stopPolling() {
        if (pollingTask != null && !pollingTask.isCancelled()) {
            pollingTask.cancel(true);
            pollingTask = null;
        }
    }

    /**
     * Method for polling the RS485 Helios RemoteContol bus
     */
    public synchronized void polling() {
        Logger.trace("HeliosVentilation Polling data for '{}'", getThing().getUID());
        pollCounter++;
        if (pollCounter > POLL_OFFLINE_THRESHOLD) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.GONE, "No data received!");
            disconnect();
        }

        if (!isConnected()) {
            connect(); // let's try to reconnect if the connection failed or was never established before
        }

        Datapoints.values().forEach((v) -> {
            if (isLinked(v.getName())) {
                poll(v);
            }
        });
    }

    private void disconnect() {
        if (thing.getStatus() != ThingStatus.REMOVING) {
            updateStatus(ThingStatus.OFFLINE);
        }
        synchronized (this) {
            if (serialPort != null) {
                serialPort.close();
            }
        }
        IOUtils.closeQuietly(inputStream);
        IOUtils.closeQuietly(outputStream);
        outputStream = null;
        inputStream = null;
        serialPort = null;
    }

    private void poll(HeliosVentilationDataPoint v) {
        byte txFrame[] = { 0x01, BUSMEMBER_ME, BUSMEMBER_MAINBOARD, 0x00, v.address(), 0x00 };
        txFrame[5] = (byte) checksum(txFrame);

        tx(txFrame);
    }

    /*
     * transmit a frame
     */
    private void tx(byte[] txFrame) {
        try {
            OutputStream out = outputStream;
            if (out != null) {
                Logger.trace("HeliosVentilation: Write to serial port: {}",
                        String.format("%02x %02x %02x %02x", txFrame[1], txFrame[2], txFrame[3], txFrame[4]));

                out.write(txFrame);
                // after each frame we have to wait.
                // 30 ms is taken from what we roughly see the original remote control is doing
                Thread.sleep(30);
            }
        } catch (IOException e) {
            // in case we cannot write the connection is somehow broken, let's officially disconnect
            disconnect();
            connect();
        } catch (InterruptedException e) {
            // ignore if we got interrupted
        }
    }

    /**
     * Check connection status
     *
     * @return true if currently connected
     */
    private boolean isConnected() {
        return serialPort != null && inputStream != null && outputStream != null;
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        switch (event.getEventType()) {
            case SerialPortEvent.DATA_AVAILABLE:
                // we get here if data has been received
                try {
                    try {
                        // wait a little to increase the likelihood that the complete frame is already buffered.
                        // this improves the robustness for RS485/USB converters which sometimes duplicate bytes
                        // otherwise
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        // ignore interruption
                    }
                    byte frame[] = { 0, 0, 0, 0, 0, 0 };
                    InputStream in = inputStream;
                    if (in != null) {
                        do {
                            int cnt = 0;
                            int c;
                            do {
                                c = in.read();
                                if (cnt > 0 || c == 0x01) {
                                    frame[cnt] = (byte) c;
                                    cnt++;
                                }

                                if (cnt < 6 && in.available() < 1) {
                                    // frame not yet complete but no input available, let's wait a little to merge
                                    // interrupted transmissions
                                    try {
                                        Thread.sleep(10);
                                    } catch (InterruptedException e) {
                                        // ignore interruption
                                    }
                                }
                            } while (in.available() > 0 && (c != -1) && cnt < 6);
                            int sum = checksum(frame);
                            if (sum == (frame[5] & 0xff)) {
                                Logger.trace("HeliosVentilation: Read from serial port: {}",
                                        String.format("%02x %02x %02x %02x", frame[1], frame[2], frame[3], frame[4]));
                                interpretFrame(frame);

                            } else {
                                Logger.trace(
                                        "HeliosVentilation: Read frame with not matching checksum from serial port: {}",
                                        String.format("%02x %02x %02x %02x %02x %02x (expected %02x)", frame[0],
                                                frame[1], frame[2], frame[3], frame[4], frame[5], sum));

                            }

                        } while (in.available() > 0);
                    }

                } catch (IOException e1) {
                    Logger.debug("Error reading from serial port: {}", e1.getMessage(), e1);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (!isConnected()) {
            connect(); // let's try to reconnect if the connection failed or was never established before
        }

        if (command instanceof RefreshType) {
            Logger.debug("Refreshing HeliosVentilation data for {}", channelUID);
            Datapoints.values().forEach((v) -> {
                if (channelUID.getThingUID().equals(thing.getUID()) && v.getName().equals(channelUID.getId())) {
                    poll(v);
                }
            });
        } else if (command instanceof DecimalType || command instanceof QuantityType || command instanceof OnOffType) {
            Datapoints.values().forEach((outer) -> {
                HeliosVentilationDataPoint v = outer;
                do {
                    if (channelUID.getThingUID().equals(thing.getUID()) && v.getName().equals(channelUID.getId())) {
                        if (v.isWritable()) {
                            byte txFrame[] = { 0x01, BUSMEMBER_ME, BUSMEMBER_CONTROLBOARDS, v.address(), 0x00, 0x00 };
                            txFrame[4] = v.getTransmitDataFor((State) command);
                            if (v.requiresReadModifyWrite()) {
                                txFrame[4] |= memory.get(v.address()) & ~v.bitMask();
                                memory.put(v.address(), txFrame[4]);
                            }
                            txFrame[5] = (byte) checksum(txFrame);
                            tx(txFrame);

                            txFrame[2] = BUSMEMBER_SLAVEBOARDS;
                            txFrame[5] = (byte) checksum(txFrame);
                            tx(txFrame);

                            txFrame[2] = BUSMEMBER_MAINBOARD;
                            txFrame[5] = (byte) checksum(txFrame);
                            tx(txFrame);
                        }
                    }
                    v = v.link();
                } while (v != null);
            });
        }

    }

    /**
     * calculate checksum of a frame
     *
     * @param frame filled with 5 bytes
     * @return checksum of the first 5 bytes of frame
     */
    private int checksum(byte[] frame) {
        int sum = 0;
        for (int a = 0; a < 5; a++) {
            sum += frame[a] & 0xff;
        }
        sum %= 256;
        return sum;
    }

    /**
     * interpret a frame, which is already validated to be in correct format with valid checksum
     *
     * @param frame 6 bytes long data with 0x01, sender, receiver, address, value, checksum
     */
    private void interpretFrame(byte[] frame) {
        if ((frame[2] & BUSMEMBER_REC_MASK) == (BUSMEMBER_ME & BUSMEMBER_REC_MASK)) {
            // something to read for us
            byte var = frame[3];
            byte val = frame[4];
            if (Datapoints.containsKey(var)) {
                HeliosVentilationDataPoint datapoint = Datapoints.get(var);
                if (datapoint.requiresReadModifyWrite()) {
                    memory.put(var, val);
                }
                do {
                    String t = datapoint.asString(val);
                    Logger.trace("Received {} = {}", datapoint, t);
                    if (thing.getStatus() != ThingStatus.REMOVING) {
                        // plausible data received, so the thing is online
                        updateStatus(ThingStatus.ONLINE);
                        pollCounter = 0;

                        updateState(datapoint.getName(), datapoint.asState(val));
                    }
                    datapoint = datapoint.link();
                } while (datapoint != null);

            } else {
                Logger.trace("Received unkown data @{} = {}", String.format("%02X ", var), String.format("%02X ", val));
            }
        }
    }
}
