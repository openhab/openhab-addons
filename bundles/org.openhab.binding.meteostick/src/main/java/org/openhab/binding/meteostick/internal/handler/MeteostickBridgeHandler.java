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
 */
public class MeteostickBridgeHandler extends BaseBridgeHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_BRIDGE);

    private final Logger logger = LoggerFactory.getLogger(MeteostickBridgeHandler.class);

    private static final int RECEIVE_TIMEOUT = 3000;

    private volatile SerialPort serialPort;
    private final SerialPortManager serialPortManager;
    private ReceiveThread receiveThread;

    private ScheduledFuture<?> reconnectJob;
    private ScheduledFuture<?> offlineTimerJob;
    private final Object portLock = new Object();

    // Package-visible so tests can shorten the reconnect poll instead of waiting 60 s.
    long reconnectIntervalSeconds = 60;

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

        Runnable reconnectRunnable = () -> connectPort(port);

        // Try to (re)connect periodically; connectPort is idempotent, so the poll keeps running while connected.
        reconnectJob = scheduler.scheduleWithFixedDelay(reconnectRunnable, 0, reconnectIntervalSeconds,
                TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        // Cancel the polls before tearing down the port so a late reconnect cannot re-open it mid-dispose.
        if (reconnectJob != null) {
            reconnectJob.cancel(true);
        }
        if (offlineTimerJob != null) {
            offlineTimerJob.cancel(true);
        }
        disconnect();
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
     * @return {@code true} if the port is (already) connected, {@code false} otherwise
     */
    private boolean connectPort(final String serialPortName) {
        synchronized (portLock) {
            if (serialPort != null) {
                return true; // already connected
            }

            logger.debug("MeteoStick Connecting to serial port {}", serialPortName);

            SerialPortIdentifier portIdentifier = serialPortManager.getIdentifier(serialPortName);
            if (portIdentifier == null) {
                updateStatus(ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        "Serial Error: Port " + serialPortName + " does not exist");
                return false;
            }

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
                return true;
            } catch (PortInUseException e) {
                updateStatus(ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        "Serial Error: Port " + serialPortName + " in use");
            } catch (UnsupportedCommOperationException e) {
                updateStatus(ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        "Serial Error: Unsupported comm operation on port " + serialPortName);
            } catch (TooManyListenersException e) {
                updateStatus(ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        "Serial Error: Too many listeners on port " + serialPortName);
            }

            return false;
        }
    }

    /**
     * Disconnects from the serial interface and stops send and receive threads.
     */
    private void disconnect() {
        ReceiveThread thread;
        synchronized (portLock) {
            thread = receiveThread;
        }
        if (thread != null) {
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        closePort();
        logger.debug("Disconnected from serial port");
    }

    /**
     * Closes the serial port and forgets the receive thread. Safe to call from the receive thread itself (it must not
     * interrupt/join itself) and idempotent, so it can also back up {@link #disconnect()}.
     */
    private void closePort() {
        synchronized (portLock) {
            if (serialPort != null) {
                serialPort.removeEventListener();
                serialPort.close();
                serialPort = null;
            }
            receiveThread = null;
        }
    }

    private void sendToMeteostick(String string) {
        SerialPort port = serialPort;
        if (port == null) {
            return;
        }
        try {
            synchronized (port.getOutputStream()) {
                port.getOutputStream().write(string.getBytes());
                port.getOutputStream().write(13);
                port.getOutputStream().flush();
            }
        } catch (IOException e) {
            logger.error("Got I/O exception {} during sending. exiting thread.", e.getLocalizedMessage());
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
            byte[] rxPacket = new byte[100];
            int rxCnt = 0;
            int consecutiveFastEof = 0;
            boolean connectionLost = false;
            while (!interrupted()) {
                int rxByte;
                long start = System.nanoTime();
                try {
                    rxByte = serialPort.getInputStream().read();

                    if (rxByte == -1) {
                        // read() returns -1 both on the idle receive timeout and on EOF. A dead port returns -1
                        // immediately, well before RECEIVE_TIMEOUT, so consecutive fast -1s indicate EOF.
                        if ((System.nanoTime() - start) / 1_000_000 < RECEIVE_TIMEOUT) {
                            if (++consecutiveFastEof >= 2) {
                                logger.debug("MeteoStick receive thread: EOF, reconnecting");
                                connectionLost = true;
                                break;
                            }
                        } else {
                            consecutiveFastEof = 0;
                        }
                        continue;
                    }
                    consecutiveFastEof = 0;

                    lastData = new Date();
                    startTimeoutCheck();

                    // Check for end of line
                    if (rxByte == 13 && rxCnt > 0) {
                        String inputString = new String(rxPacket, 0, rxCnt);
                        logger.debug("MeteoStick received: {}", inputString);
                        String[] p = inputString.split("\\s+");

                        switch (p[0]) {
                            case "B": // Barometer
                                BigDecimal temperature = new BigDecimal(p[1]);
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_INDOOR_TEMPERATURE),
                                        new QuantityType<>(temperature.setScale(1), CELSIUS));

                                BigDecimal pressure = new BigDecimal(p[2]);
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_PRESSURE),
                                        new QuantityType<>(pressure.setScale(1, RoundingMode.HALF_UP), HECTO(PASCAL)));
                                break;
                            case "#":
                                break;
                            case "?":
                                // Create the channel command
                                int channels = 0;
                                for (int channel : eventListeners.keySet()) {
                                    channels += Math.pow(2, channel - 1);
                                }

                                // Device has been reset - reconfigure
                                sendToMeteostick(meteostickFormat);
                                sendToMeteostick(meteostickMode);
                                sendToMeteostick("t" + channels);
                                break;
                            default:
                                if (p.length < 3) {
                                    logger.debug("MeteoStick bridge: short data ({})", p.length);
                                    break;
                                }

                                try {
                                    MeteostickEventListener listener = eventListeners.get(Integer.parseInt(p[1]));
                                    if (listener != null) {
                                        listener.onDataReceived(p);
                                    } else {
                                        logger.debug("MeteoStick bridge: data from channel {} with no handler",
                                                Integer.parseInt(p[1]));
                                    }
                                } catch (NumberFormatException e) {
                                }
                                break;
                        }

                        updateStatus(ThingStatus.ONLINE);

                        rxCnt = 0;
                    } else if (rxByte != 10) {
                        // Ignore line feed
                        rxPacket[rxCnt] = (byte) rxByte;

                        if (rxCnt < rxPacket.length) {
                            rxCnt++;
                        }
                    }
                } catch (IOException e) {
                    if (interrupted()) {
                        break;
                    }
                    logger.warn("MeteoStick: I/O error while reading serial port, reconnecting", e);
                    connectionLost = true;
                    break;
                } catch (Exception e) {
                    rxCnt = 0;
                    logger.error("Exception during MeteoStick receive thread", e);
                }
            }

            logger.debug("Stopping MeteoStick Receive Thread");
            closePort();
            if (connectionLost) {
                updateStatus(ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "Serial connection lost, reconnecting");
            }
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
            updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, detail);
        };

        if (offlineTimerJob != null) {
            offlineTimerJob.cancel(true);
        }

        // Scheduling a job on each hour to update the last hour rainfall
        offlineTimerJob = scheduler.schedule(pollingRunnable, 90, TimeUnit.SECONDS);
    }
}
