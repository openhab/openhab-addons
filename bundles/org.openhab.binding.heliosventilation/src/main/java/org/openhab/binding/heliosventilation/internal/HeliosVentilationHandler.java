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
import java.util.HashMap;
import java.util.TooManyListenersException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
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

    private static final HashMap<Byte, HeliosVentilationVariable> variables = new HashMap<Byte, HeliosVentilationVariable>();
    private static final int POLL_OFFLINE_THRESHOLD = 3;

    private final Logger logger = LoggerFactory.getLogger(HeliosVentilationHandler.class);

    private @NonNullByDefault({}) HeliosVentilationConfiguration config;

    private @NonNullByDefault({}) SerialPortManager serialPortManager;

    private @Nullable SerialPortIdentifier portId;
    private @Nullable SerialPort serialPort;
    private @Nullable InputStream inputStream;
    private @Nullable OutputStream outputStream;

    // Polling variables
    private @Nullable ScheduledFuture<?> pollingTask;
    private int pollCounter;

    public HeliosVentilationHandler(Thing thing, final SerialPortManager serialPortManager) {
        super(thing);
        this.serialPortManager = serialPortManager;

        // Read-only
        variables.put((byte) 0x32,
                new HeliosVentilationVariable(thing, HeliosVentilationBindingConstants.CHANNEL_OUTSIDE_TEMP,
                        (byte) 0x32, false, HeliosVentilationVariable.type.TEMPERATURE));
        variables.put((byte) 0x33,
                new HeliosVentilationVariable(thing, HeliosVentilationBindingConstants.CHANNEL_OUTGOING_TEMP,
                        (byte) 0x33, false, HeliosVentilationVariable.type.TEMPERATURE));
        variables.put((byte) 0x34,
                new HeliosVentilationVariable(thing, HeliosVentilationBindingConstants.CHANNEL_EXTRACT_TEMP,
                        (byte) 0x34, false, HeliosVentilationVariable.type.TEMPERATURE));
        variables.put((byte) 0x35,
                new HeliosVentilationVariable(thing, HeliosVentilationBindingConstants.CHANNEL_SUPPLY_TEMP, (byte) 0x35,
                        false, HeliosVentilationVariable.type.TEMPERATURE));

        // writable
        variables.put((byte) 0xAF,
                new HeliosVentilationVariable(thing, HeliosVentilationBindingConstants.CHANNEL_BYPASS_TEMP, (byte) 0xAF,
                        true, HeliosVentilationVariable.type.TEMPERATURE));
        variables.put((byte) 0xAE,
                new HeliosVentilationVariable(thing, HeliosVentilationBindingConstants.CHANNEL_RH_LIMIT, (byte) 0xAE,
                        true, HeliosVentilationVariable.type.BYTE_PERCENT));

        variables.put((byte) 0xB2,
                new HeliosVentilationVariable(thing, HeliosVentilationBindingConstants.CHANNEL_HYSTERESIS, (byte) 0xB2,
                        true, HeliosVentilationVariable.type.HYSTERESIS));
        variables.put((byte) 0xA9,
                new HeliosVentilationVariable(thing, HeliosVentilationBindingConstants.CHANNEL_MIN_FANSPEED,
                        (byte) 0xA9, true, HeliosVentilationVariable.type.FANSPEED));
        variables.put((byte) 0xA5,
                new HeliosVentilationVariable(thing, HeliosVentilationBindingConstants.CHANNEL_MAX_FANSPEED,
                        (byte) 0xA5, true, HeliosVentilationVariable.type.FANSPEED));

        variables.put((byte) 0x29,
                new HeliosVentilationVariable(thing, HeliosVentilationBindingConstants.CHANNEL_FANSPEED, (byte) 0x29,
                        true, HeliosVentilationVariable.type.FANSPEED));

        /*
         * not yet supported
         * variables.put((byte) 0xB1,
         * new HeliosVentilationVariable(thing, "DC_fan_outgoing", true, HeliosVentilationVariable.type.Percent)); // in
         *
         * variables.put((byte) 0xB0,
         * new HeliosVentilationVariable(thing, "DC_fan_supply", true, HeliosVentilationVariable.type.Percent)); // in %
         *
         * variables.put((byte) 0xAA, new HeliosVentilationVariable(thing, "adjust_interval", true,
         * HeliosVentilationVariable.type.Number); // bit 0-4: adjust interval; bit 4: RH level setting: 1 = auto 0
         * = manual; bit 5: 1 = boost, 0 = fireplace; bit 6: radiator type: 1 = water 0 = electric; bit 7: cascade on
         *
         * variables.put((byte) 0xA8,
         * new HeliosVentilationVariable(thing, "supply_stop_temp", (byte) 0xA8, true,
         * HeliosVentilationVariable.type.Temperature));
         *
         * variables.put((byte) 0xA7,
         * new HeliosVentilationVariable(thing, "preheat_temp", (byte) 0xA7, true,
         * HeliosVentilationVariable.type.Temperature));
         *
         * variables.put((byte) 0xA6,
         * new HeliosVentilationVariable(thing, "maintenance_interval", true, HeliosVentilationVariable.type.Number));
         * // in months
         *
         * variables.put((byte) 0xA4,
         * new HeliosVentilationVariable(thing, "set_temp", true, HeliosVentilationVariable.type.Temperature));
         */

        /*
         * variables.put((byte) 0xA3, new HeliosVentilationVariable(thing, "state", (byte) 0xA3, true,
         * HeliosVentilationVariable.type.Number));
         * This byte has a bit level interpretation and contains state and reset_maintenance
         * variables.put((byte) 0xA3, "reset_maintenance");
         *
         * "power_state", 0xA3, bit 0
         * "co2_state", 0xA3, bit 1
         * "rh_state", 0xA3, bit 2
         * "bypass_disabled", 0xA3, bit 3
         */
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (!isConnected()) {
            connect(); // let's try to reconnect if the connection failed or was never established before
        }

        if (command instanceof RefreshType) {
            logger.debug("Refreshing HeliosVentilation data for {}", channelUID);
            variables.values().forEach((v) -> {
                ChannelUID tmp = v.channelUID();
                if (tmp.equals(channelUID)) {
                    poll(v);
                }
            });
        } else if (command instanceof DecimalType) {
            variables.values().forEach((v) -> {
                ChannelUID tmp = v.channelUID();
                if (tmp.equals(channelUID)) {
                    if (v.isWritable()) {
                        byte txFrame[] = { 0x01, BUSMEMBER_ME, BUSMEMBER_CONTROLBOARDS, v.address(), 0x00, 0x00 };
                        txFrame[4] = v.getTransmitDataFor((DecimalType) command);
                        txFrame[5] = (byte) checksum(txFrame);
                        logger.debug("*********************************...");

                        tx(txFrame);
                        txFrame[2] = BUSMEMBER_SLAVEBOARDS;
                        txFrame[5] = (byte) checksum(txFrame);

                        tx(txFrame);
                        txFrame[2] = BUSMEMBER_MAINBOARD;
                        txFrame[5] = (byte) checksum(txFrame);
                        tx(txFrame);
                    }
                }
            });
        }

    }

    @Override
    public void initialize() {
        config = getConfigAs(HeliosVentilationConfiguration.class);

        logger.debug("   Serial Port: {},", config.serialPort);
        logger.debug("   Baud:        {},", 9600);
        logger.debug("   PollPeriod:  {},", config.pollPeriod);

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

    private void connect() {
        logger.debug("HeliosVentilation: connecting...");
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
    public void startPolling() {
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
    public void stopPolling() {
        if (pollingTask != null && !pollingTask.isCancelled()) {
            pollingTask.cancel(true);
            pollingTask = null;
        }
    }

    /**
     * Method for polling the RS485 Helios RemoteContol bus
     */
    public synchronized void polling() {
        logger.trace("HeliosVentilation Polling data for '{}'", getThing().getUID());
        pollCounter++;
        if (pollCounter > POLL_OFFLINE_THRESHOLD) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.GONE, "No data received!");
            disconnect();
        }

        if (!isConnected()) {
            connect(); // let's try to reconnect if the connection failed or was never established before
        }

        variables.values().forEach((v) -> {
            ChannelUID channelUID = v.channelUID();
            if (isLinked(channelUID)) {
                poll(v);
            }
        });
    }

    private void disconnect() {
        updateStatus(ThingStatus.OFFLINE);
        if (serialPort != null) {
            serialPort.close();
        }
        IOUtils.closeQuietly(inputStream);
        IOUtils.closeQuietly(outputStream);
        outputStream = null;
        inputStream = null;
        serialPort = null;
    }

    private void poll(HeliosVentilationVariable v) {
        byte txFrame[] = { 0x01, BUSMEMBER_ME, BUSMEMBER_MAINBOARD, 0x00, v.address(), 0x00 };
        txFrame[5] = (byte) checksum(txFrame);

        tx(txFrame);
    }

    /*
     * transmit a frame
     */
    private void tx(byte[] txFrame) {
        try {
            if (outputStream != null) {
                logger.trace("HeliosVentilation: Write to serial port: {}",
                        String.format("%02x %02x %02x %02x", txFrame[1], txFrame[2], txFrame[3], txFrame[4]));

                outputStream.write(txFrame);
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
                    do {
                        int cnt = 0;
                        int c;

                        do {
                            c = inputStream.read();
                            if (cnt > 0 || c == 0x01) {
                                frame[cnt] = (byte) c;
                                cnt++;
                            }

                            if (cnt < 6 && inputStream.available() < 1) {
                                // frame not yet complete but no input available, let's wait a little to merge
                                // interrupted transmissions
                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException e) {
                                    // ignore interruption
                                }
                            }
                        } while (inputStream.available() > 0 && (c != -1) && cnt < 6);

                        // handle frame
                        // check checksum
                        int sum = checksum(frame);
                        if (sum == (frame[5] & 0xff)) {
                            logger.trace("HeliosVentilation: Read from serial port: {}",
                                    String.format("%02x %02x %02x %02x", frame[1], frame[2], frame[3], frame[4]));
                            interpretFrame(frame);

                        } else {
                            logger.trace(
                                    "HeliosVentilation: Read frame with not matching checksum from serial port: {}",
                                    String.format("%02x %02x %02x %02x %02x %02x (expected %02x)", frame[0], frame[1],
                                            frame[2], frame[3], frame[4], frame[5], sum));

                        }

                    } while (inputStream.available() > 0);

                } catch (IOException e1) {
                    logger.debug("Error reading from serial port: {}", e1.getMessage(), e1);
                }
                break;
            default:
                break;
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
     * interpret a frame, which is already validated to be in correct format
     *
     * @param frame 6 bytes long data with 0x01, sender, receiver, address, value, checksum
     */
    private void interpretFrame(byte[] frame) {
        if ((frame[2] & BUSMEMBER_REC_MASK) == (BUSMEMBER_ME & BUSMEMBER_REC_MASK)) {
            // something to read for us
            byte var = frame[3];
            byte val = frame[4];
            if (variables.containsKey(var)) {
                HeliosVentilationVariable variable = variables.get(var);
                String t = variable.asString(val);
                logger.trace("Received {} = {}", variable, t);
                updateChannelFor(variable, val);
                // plausible data received, so the thing is online
                updateStatus(ThingStatus.ONLINE);
                pollCounter = 0;

            } else {
                logger.trace("Received unkown data @{} = {}", String.format("%02X ", var), String.format("%02X ", val));
            }
        }
    }

    /**
     * update the channel for the given variable
     *
     * @param variable
     * @param val
     */
    private void updateChannelFor(HeliosVentilationVariable variable, byte val) {
        String channelId;
        channelId = variable.getName();

        if (thing.getChannel(channelId) == null) {
            ThingBuilder thingBuilder = editThing();
            ChannelTypeUID channelTypeUID = new ChannelTypeUID(HeliosVentilationBindingConstants.BINDING_ID, "Number");
            ChannelUID channelUID = new ChannelUID(thing.getUID(), channelId);

            Channel channel = ChannelBuilder.create(channelUID, "Number").withType(channelTypeUID).withLabel(channelId)
                    .build();

            thingBuilder.withChannel(channel).withLabel(thing.getLabel());

            updateThing(thingBuilder.build());
        }

        Channel channel = getThing().getChannel(channelId);
        updateState(channelId, variable.asState(val));
    }

}
