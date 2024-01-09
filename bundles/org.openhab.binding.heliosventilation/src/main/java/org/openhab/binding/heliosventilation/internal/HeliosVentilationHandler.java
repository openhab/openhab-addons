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
package org.openhab.binding.heliosventilation.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TooManyListenersException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortEvent;
import org.openhab.core.io.transport.serial.SerialPortEventListener;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
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

    /** Logger Instance */
    private final Logger logger = LoggerFactory.getLogger(HeliosVentilationHandler.class);

    /**
     * store received data for read-modify-write operations on bitlevel
     */
    private final Map<Byte, Byte> memory = new HashMap<>();

    private final SerialPortManager serialPortManager;

    /**
     * init to default to avoid NPE in case handleCommand() is called before initialize()
     */
    private HeliosVentilationConfiguration config = new HeliosVentilationConfiguration();

    private @Nullable SerialPort serialPort;
    private @Nullable InputStream inputStream;
    private @Nullable OutputStream outputStream;

    private @Nullable ScheduledFuture<?> pollingTask;
    private int pollCounter;

    public HeliosVentilationHandler(Thing thing, final SerialPortManager serialPortManager) {
        super(thing);
        this.serialPortManager = serialPortManager;
    }

    @Override
    public void initialize() {
        config = getConfigAs(HeliosVentilationConfiguration.class);

        logger.debug("Serial Port: {}, 9600 baud, PollPeriod: {}", config.serialPort, config.pollPeriod);

        if (config.serialPort.length() < 1) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Port must be set!");
            return;
        } else {
            SerialPortIdentifier portId = serialPortManager.getIdentifier(config.serialPort);
            if (portId == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                        "Port " + config.serialPort + " is not known!");
                serialPort = null;
            } else {
                updateStatus(ThingStatus.UNKNOWN);
                if (this.config.pollPeriod > 0) {
                    startPolling();
                }
            }
        }

        scheduler.execute(this::connect);
    }

    private synchronized void connect() {
        logger.debug("HeliosVentilation: connecting...");
        // parse ports and if the port is found, initialize the reader
        SerialPortIdentifier portId = serialPortManager.getIdentifier(config.serialPort);
        if (portId == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Port " + config.serialPort + " is not known!");
            serialPort = null;

            disconnect();
        } else if (!isConnected()) {
            // initialize serial port
            try {
                SerialPort serial = portId.open(getThing().getUID().toString(), 2000);
                serial.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                serial.addEventListener(this);

                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    // ignore the exception on close
                    inputStream = null;
                }
                try {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (IOException e) {
                    // ignore the exception on close
                    outputStream = null;
                }

                inputStream = serial.getInputStream();
                outputStream = serial.getOutputStream();

                // activate the DATA_AVAILABLE notifier
                serial.notifyOnDataAvailable(true);
                serialPort = serial;
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
    public synchronized void startPolling() {
        final ScheduledFuture<?> task = pollingTask;
        if (task != null && task.isCancelled()) {
            task.cancel(true);
        }
        if (config.pollPeriod > 0) {
            pollingTask = scheduler.scheduleWithFixedDelay(this::polling, 10, config.pollPeriod, TimeUnit.SECONDS);
        } else {
            pollingTask = null;
        }
    }

    /**
     * Stop the polling task.
     */
    public synchronized void stopPolling() {
        final ScheduledFuture<?> task = pollingTask;
        if (task != null && !task.isCancelled()) {
            task.cancel(true);
            pollingTask = null;
        }
    }

    /**
     * Method for polling the RS485 Helios RemoteContol bus
     */
    public synchronized void polling() {
        if (logger.isTraceEnabled()) {
            logger.trace("HeliosVentilation Polling data for '{}'", getThing().getUID());
        }
        pollCounter++;
        if (pollCounter > POLL_OFFLINE_THRESHOLD) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.GONE, "No data received!");
            logger.info("No data received for '{}' disconnecting now...", getThing().getUID());
            disconnect();
        }

        if (!isConnected()) {
            connect(); // let's try to reconnect if the connection failed or was never established before
        }

        HeliosVentilationBindingConstants.DATAPOINTS.values().forEach((v) -> {
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
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                // ignore the exception on close
                inputStream = null;
            }
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                // ignore the exception on close
                outputStream = null;
            }

            SerialPort serial = serialPort;
            if (serial != null) {
                serial.close();
            }
            serialPort = null;
        }
    }

    private void poll(HeliosVentilationDataPoint v) {
        byte[] txFrame = { 0x01, BUSMEMBER_ME, BUSMEMBER_MAINBOARD, 0x00, v.address(), 0x00 };
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
                if (logger.isTraceEnabled()) {
                    logger.trace("HeliosVentilation: Write to serial port: {}",
                            String.format("%02x %02x %02x %02x", txFrame[1], txFrame[2], txFrame[3], txFrame[4]));
                }

                out.write(txFrame);
                out.flush();
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
    public synchronized void serialEvent(SerialPortEvent event) {
        switch (event.getEventType()) {
            case SerialPortEvent.DATA_AVAILABLE:
                // we get here if data has been received

                try {
                    // Wait roughly a frame length to ensure that the complete frame is already buffered. This improves
                    // the robustness for RS485/USB converters which sometimes duplicate bytes otherwise.
                    Thread.sleep(8);
                } catch (InterruptedException e) {
                    // ignore interruption
                }

                byte[] frame = { 0, 0, 0, 0, 0, 0 };
                InputStream in = inputStream;
                if (in != null) {
                    try {
                        do {
                            int cnt = 0;
                            // read data from serial device
                            while (cnt < 6 && in.available() > 0) {
                                final int bytes = in.read(frame, cnt, 1);
                                if (cnt > 0 || frame[0] == 0x01) {
                                    // only proceed if the first byte was 0x01
                                    cnt += bytes;
                                }
                            }
                            int sum = checksum(frame);
                            if (sum == (frame[5] & 0xff)) {
                                if (logger.isTraceEnabled()) {
                                    logger.trace("HeliosVentilation: Read from serial port: {}", String
                                            .format("%02x %02x %02x %02x", frame[1], frame[2], frame[3], frame[4]));
                                }
                                interpretFrame(frame);

                            } else {
                                if (logger.isTraceEnabled()) {
                                    logger.trace(
                                            "HeliosVentilation: Read frame with not matching checksum from serial port: {}",
                                            String.format("%02x %02x %02x %02x %02x %02x (expected %02x)", frame[0],
                                                    frame[1], frame[2], frame[3], frame[4], frame[5], sum));
                                }

                            }

                        } while (in.available() > 0);

                    } catch (IOException e1) {
                        logger.debug("Error reading from serial port: {}", e1.getMessage(), e1);
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            scheduler.execute(this::polling);
        } else if (command instanceof DecimalType || command instanceof QuantityType || command instanceof OnOffType) {
            scheduler.execute(() -> update(channelUID, command));
        }
    }

    /**
     * Update the variable corresponding to given channel/command
     *
     * @param channelUID UID of the channel to update
     * @param command data element to write
     *
     */
    public void update(ChannelUID channelUID, Command command) {
        HeliosVentilationBindingConstants.DATAPOINTS.values().forEach((outer) -> {
            HeliosVentilationDataPoint v = outer;
            do {
                if (channelUID.getThingUID().equals(thing.getUID()) && v.getName().equals(channelUID.getId())) {
                    if (v.isWritable()) {
                        byte[] txFrame = { 0x01, BUSMEMBER_ME, BUSMEMBER_CONTROLBOARDS, v.address(), 0x00, 0x00 };
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
                v = v.next();
            } while (v != null);
        });
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
            if (HeliosVentilationBindingConstants.DATAPOINTS.containsKey(var)) {
                HeliosVentilationDataPoint datapoint = HeliosVentilationBindingConstants.DATAPOINTS.get(var);
                if (datapoint.requiresReadModifyWrite()) {
                    memory.put(var, val);
                }
                do {
                    if (logger.isTraceEnabled()) {
                        String t = datapoint.asString(val);
                        logger.trace("Received {} = {}", datapoint, t);
                    }
                    updateStatus(ThingStatus.ONLINE);
                    pollCounter = 0;

                    updateState(datapoint.getName(), datapoint.asState(val));
                    datapoint = datapoint.next();
                } while (datapoint != null);

            } else {
                if (logger.isTraceEnabled()) {
                    logger.trace("Received unkown data @{} = {}", String.format("%02X ", var),
                            String.format("%02X ", val));
                }
            }
        }
    }
}
