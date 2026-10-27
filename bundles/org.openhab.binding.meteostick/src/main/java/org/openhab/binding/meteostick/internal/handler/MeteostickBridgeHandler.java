/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.meteostick.internal.handler;

import static org.openhab.binding.meteostick.internal.MeteostickBindingConstants.*;
import static org.openhab.core.library.unit.MetricPrefix.HECTO;
import static org.openhab.core.library.unit.SIUnits.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Set;
import java.util.TooManyListenersException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.meteostick.internal.MeteostickBindingConstants;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortEvent;
import org.openhab.core.io.transport.serial.SerialPortEventListener;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MeteostickBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Chris Jackson - Initial contribution
 * @author Jens Erat - Harden packet parsing and validate received values
 */
public class MeteostickBridgeHandler extends BaseBridgeHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_BRIDGE);

    private final Logger logger = LoggerFactory.getLogger(MeteostickBridgeHandler.class);

    private static final int RECEIVE_TIMEOUT = 3000;

    // Plausibility bounds, chosen to reject physically impossible values while accepting any real weather reading.
    private static final BigDecimal MIN_TEMPERATURE = new BigDecimal("-100");
    private static final BigDecimal MAX_TEMPERATURE = new BigDecimal("100");
    private static final BigDecimal MIN_PRESSURE = new BigDecimal("0");
    private static final BigDecimal MAX_PRESSURE = new BigDecimal("1500");

    private SerialPort serialPort;
    private final SerialPortManager serialPortManager;
    private ReceiveThread receiveThread;

    private ScheduledFuture<?> offlineTimerJob;

    private String meteostickMode = "m1";
    private final String meteostickFormat = "o1";

    private Date lastData;

    private ConcurrentMap<Integer, MeteostickEventListener> eventListeners = new ConcurrentHashMap<>();

    public MeteostickBridgeHandler(Bridge thing, SerialPortManager serialPortManager) {
        super(thing);
        this.serialPortManager = serialPortManager;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing MeteoStick Bridge handler.");

        updateStatus(ThingStatus.UNKNOWN);

        Configuration config = getThing().getConfiguration();

        final String port = (String) config.get("port");

        final BigDecimal mode = (BigDecimal) config.get("mode");
        if (mode != null) {
            meteostickMode = "m" + mode.toString();
        }

        Runnable pollingRunnable = () -> {
            if (connectPort(port)) {
                offlineTimerJob.cancel(true);
            }
        };

        // Scheduling a job on each hour to update the last hour rainfall
        offlineTimerJob = scheduler.scheduleWithFixedDelay(pollingRunnable, 0, 60, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        disconnect();
        if (offlineTimerJob != null) {
            offlineTimerJob.cancel(true);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    private void resetMeteoStick() {
        sendToMeteostick("r");
    }

    protected void subscribeEvents(int channel, MeteostickEventListener handler) {
        logger.debug("MeteoStick bridge: subscribeEvents to channel {} with {}", channel, handler);

        if (eventListeners.containsKey(channel)) {
            logger.debug("MeteoStick bridge: subscribeEvents to channel {} already registered", channel);
            return;
        }
        eventListeners.put(channel, handler);

        resetMeteoStick();
    }

    protected void unsubscribeEvents(int channel, MeteostickEventListener handler) {
        logger.debug("MeteoStick bridge: unsubscribeEvents to channel {} with {}", channel, handler);

        eventListeners.remove(channel, handler);

        resetMeteoStick();
    }

    /**
     * Connects to the comm port and starts send and receive threads.
     *
     * @param serialPortName the port name to open
     * @throws SerialInterfaceException when a connection error occurs.
     */
    private boolean connectPort(final String serialPortName) {
        logger.debug("MeteoStick Connecting to serial port {}", serialPortName);

        SerialPortIdentifier portIdentifier = serialPortManager.getIdentifier(serialPortName);
        if (portIdentifier == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Serial Error: Port " + serialPortName + " does not exist");
            return false;
        }

        boolean success = false;
        try {
            serialPort = portIdentifier.open("org.openhab.binding.meteostick", 2000);
            serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            serialPort.enableReceiveThreshold(1);
            serialPort.enableReceiveTimeout(RECEIVE_TIMEOUT);

            receiveThread = new ReceiveThread();
            receiveThread.start();

            // RXTX serial port library causes high CPU load
            // Start event listener, which will just sleep and slow down event loop
            serialPort.addEventListener(this.receiveThread);
            serialPort.notifyOnDataAvailable(true);

            logger.debug("Serial port is initialized");

            success = true;
        } catch (PortInUseException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Serial Error: Port " + serialPortName + " in use");
        } catch (UnsupportedCommOperationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Serial Error: Unsupported comm operation on port " + serialPortName);
        } catch (TooManyListenersException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Serial Error: Too many listeners on port " + serialPortName);
        }

        return success;
    }

    /**
     * Disconnects from the serial interface and stops send and receive threads.
     */
    private void disconnect() {
        if (receiveThread != null) {
            receiveThread.interrupt();
            try {
                receiveThread.join();
            } catch (InterruptedException e) {
            }
            receiveThread = null;
        }

        if (this.serialPort != null) {
            this.serialPort.close();
            this.serialPort = null;
        }
        logger.debug("Disconnected from serial port");
    }

    private void sendToMeteostick(String string) {
        try {
            synchronized (serialPort.getOutputStream()) {
                serialPort.getOutputStream().write(string.getBytes());
                serialPort.getOutputStream().write(13);
                serialPort.getOutputStream().flush();
            }
        } catch (IOException e) {
            logger.error("Got I/O exception {} during sending. exiting thread.", e.getLocalizedMessage());
        }
    }

    /**
     * Processes one received line.
     * <p>
     * Package-visible so that packet handling can be tested without a serial port.
     *
     * @param message the received line
     */
    void processMessage(String message) {
        String[] fields = message.split("\\s+");
        if (fields.length == 0) {
            return;
        }

        switch (fields[0]) {
            case "B": // Barometer
                processBarometer(fields);
                break;
            case "#":
                break;
            case "?":
                // Device has been reset - reconfigure
                int channels = 0;
                for (int channel : eventListeners.keySet()) {
                    channels += (int) Math.pow(2, channel - 1);
                }

                sendToMeteostick(meteostickFormat);
                sendToMeteostick(meteostickMode);
                sendToMeteostick("t" + channels);
                break;
            default:
                processSensorData(fields);
                break;
        }
    }

    private void processBarometer(String[] fields) {
        // Computed ("o1") output has exactly two values, temperature and pressure. Raw ("o0") output has six
        // values, so requiring the computed field count keeps raw packets from being published as computed values.
        if (fields.length != 3) {
            logger.debug("MeteoStick bridge: ignoring barometer packet with {} fields", fields.length);
            return;
        }

        BigDecimal temperature;
        BigDecimal pressure;
        try {
            temperature = new BigDecimal(fields[1]);
            pressure = new BigDecimal(fields[2]);
        } catch (NumberFormatException e) {
            logger.debug("MeteoStick bridge: ignoring barometer packet with non-numeric values");
            return;
        }

        if (!isWithin(temperature, MIN_TEMPERATURE, MAX_TEMPERATURE)
                || !isWithin(pressure, MIN_PRESSURE, MAX_PRESSURE)) {
            logger.debug("MeteoStick bridge: ignoring barometer packet with implausible values");
            return;
        }

        updateState(new ChannelUID(getThing().getUID(), CHANNEL_INDOOR_TEMPERATURE),
                new QuantityType<>(temperature.setScale(1), CELSIUS));
        updateState(new ChannelUID(getThing().getUID(), CHANNEL_PRESSURE),
                new QuantityType<>(pressure.setScale(1, RoundingMode.HALF_UP), HECTO(PASCAL)));
    }

    private static boolean isWithin(BigDecimal value, BigDecimal min, BigDecimal max) {
        return value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
    }

    private void processSensorData(String[] fields) {
        if (fields.length < 3) {
            logger.debug("MeteoStick bridge: ignoring sensor packet with {} fields", fields.length);
            return;
        }

        int channel;
        try {
            channel = Integer.parseInt(fields[1]);
        } catch (NumberFormatException e) {
            logger.debug("MeteoStick bridge: ignoring sensor packet with non-numeric channel");
            return;
        }

        MeteostickEventListener listener = eventListeners.get(channel);
        if (listener != null) {
            listener.onDataReceived(fields);
        } else {
            logger.debug("MeteoStick bridge: data from channel {} with no handler", channel);
        }
    }

    private class ReceiveThread extends Thread implements SerialPortEventListener {
        private final Logger logger = LoggerFactory.getLogger(ReceiveThread.class);

        public ReceiveThread() {
            super(String.format("OH-binding-%s-%s", MeteostickBindingConstants.BINDING_ID, "Receiver"));
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
         * Run method. Runs the actual receiving process.
         */
        @Override
        public void run() {
            logger.debug("Starting MeteoStick Receive Thread");
            LineBuffer lineBuffer = new LineBuffer();
            while (!interrupted()) {
                int rxByte;
                try {
                    rxByte = serialPort.getInputStream().read();
                } catch (IOException e) {
                    if (interrupted()) {
                        break;
                    }
                    logger.debug("MeteoStick: I/O error while reading serial port: {}", e.getMessage());
                    continue;
                }

                if (rxByte == -1) {
                    continue;
                }

                lastData = new Date();
                startTimeoutCheck();

                String line = lineBuffer.accept(rxByte);
                if (line != null) {
                    logger.debug("MeteoStick received: {}", line);
                    try {
                        processMessage(line);
                    } catch (RuntimeException e) {
                        logger.debug("MeteoStick: ignoring malformed line '{}': {}", line, e.toString());
                    }
                    updateStatus(ThingStatus.ONLINE);
                } else if (lineBuffer.consumeDroppedLine()) {
                    logger.debug("MeteoStick: discarding over-long line");
                }
            }

            logger.debug("Stopping MeteoStick Receive Thread");
            serialPort.removeEventListener();
        }
    }

    private synchronized void startTimeoutCheck() {
        Runnable pollingRunnable = () -> {
            String detail;
            if (lastData == null) {
                detail = "No data received";
            } else {
                detail = "No data received since " + lastData.toString();
            }
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, detail);
        };

        if (offlineTimerJob != null) {
            offlineTimerJob.cancel(true);
        }

        // Scheduling a job on each hour to update the last hour rainfall
        offlineTimerJob = scheduler.schedule(pollingRunnable, 90, TimeUnit.SECONDS);
    }
}
