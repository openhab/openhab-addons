/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.TooManyListenersException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_BRIDGE);

    private final Logger logger = LoggerFactory.getLogger(MeteostickBridgeHandler.class);

    private static final int RECEIVE_TIMEOUT = 3000;

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

    private class ReceiveThread extends Thread implements SerialPortEventListener {
        private final Logger logger = LoggerFactory.getLogger(ReceiveThread.class);

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
            int rxByte;
            while (!interrupted()) {
                try {
                    rxByte = serialPort.getInputStream().read();

                    if (rxByte == -1) {
                        continue;
                    }

                    lastData = new Date();
                    startTimeoutCheck();

                    // Check for end of line
                    if (rxByte == 13 && rxCnt > 0) {
                        String inputString = new String(rxPacket, 0, rxCnt);
                        logger.debug("MeteoStick received: {}", inputString);
                        String p[] = inputString.split("\\s+");

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
                } catch (Exception e) {
                    rxCnt = 0;
                    logger.error("Exception during MeteoStick receive thread", e);
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
