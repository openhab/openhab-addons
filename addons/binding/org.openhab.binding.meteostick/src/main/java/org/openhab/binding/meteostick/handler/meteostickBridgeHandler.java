/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.meteostick.handler;

import static org.openhab.binding.meteostick.meteostickBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Set;
import java.util.TooManyListenersException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

/**
 * The {@link meteostickBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Chris Jackson - Initial contribution
 */
public class meteostickBridgeHandler extends BaseThingHandler {
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_BRIDGE);

    private Logger logger = LoggerFactory.getLogger(meteostickBridgeHandler.class);

    private static int RECEIVE_TIMEOUT = 3000;

    private SerialPort serialPort;
    private ReceiveThread receiveThread;

    private String meteostickMode = "m1";
    private String meteostickChannels = "t0";
    private final String meteostickFormat = "o1";

    private ConcurrentMap<Integer, meteostickEventListener> eventListeners = new ConcurrentHashMap<Integer, meteostickEventListener>();

    public meteostickBridgeHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void setCallback(ThingHandlerCallback thingHandlerCallback) {
        logger.debug("Callback is set to: {}.", thingHandlerCallback, new RuntimeException("log stacktrace"));
        super.setCallback(thingHandlerCallback);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing MeteoStick Bridge handler.");
        super.initialize();

        updateStatus(ThingStatus.OFFLINE);

        Configuration config = getThing().getConfiguration();

        String port = (String) config.get("port");
        connectPort(port);
    }

    @Override
    public void dispose() {
        disconnect();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    private void createChannelCommand() {
        int channels = 0;
        for (int channel = 1; channel < 8; channel++) {
            if (eventListeners.get(channel) != null) {
                channels += Math.pow(2, channel - 1);
            }
        }

        meteostickChannels = "t" + channels;
    }

    private void resetMeteoStick() {
        sendToMeteostick("r");
    }

    protected void subscribeEvents(int channel, meteostickEventListener handler) {
        logger.debug("MeteoStick bridge: subscribeEvents to channel {} with {}", channel, handler);

        if (eventListeners.containsKey(channel)) {
            logger.debug("MeteoStick bridge: subscribeEvents to channel {} already registered", channel);
            return;
        }
        eventListeners.put(channel, handler);

        createChannelCommand();
        resetMeteoStick();
    }

    protected void unsubscribeEvents(int channel, meteostickEventListener handler) {
        logger.debug("MeteoStick bridge: unsubscribeEvents to channel {} with {}", channel, handler);

        eventListeners.remove(channel, handler);

        createChannelCommand();
        resetMeteoStick();
    }

    /**
     * Connects to the comm port and starts send and receive threads.
     *
     * @param serialPortName the port name to open
     * @throws SerialInterfaceException when a connection error occurs.
     */
    private void connectPort(final String serialPortName) {
        logger.info("MeteoStick Connecting to serial port {}", serialPortName);

        try {
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(serialPortName);
            CommPort commPort = portIdentifier.open("org.openhab.binding.meteostick", 2000);
            serialPort = (SerialPort) commPort;
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

            logger.info("Serial port is initialized");
        } catch (NoSuchPortException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Serial Error: Port " + serialPortName + " does not exist");
        } catch (PortInUseException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Serial Error: Port " + serialPortName + " in use");
        } catch (UnsupportedCommOperationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Serial Error: Unsupported comm operation on Port " + serialPortName);
        } catch (TooManyListenersException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Serial Error: Too many listeners on Port " + serialPortName);
        }
    }

    /**
     * Disconnects from the serial interface and stops
     * send and receive threads.
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
        logger.info("Disconnected from serial port");
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

                    // Check for end of line
                    if (rxByte == 13 && rxCnt > 0) {
                        String inputString = new String(rxPacket, 0, rxCnt);
                        logger.debug("MeteoStick received: {}", inputString);
                        String p[] = inputString.split("\\s+");

                        switch (p[0]) {
                            case "B": // Barometer
                                BigDecimal temperature = new BigDecimal(p[1]);
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_INDOOR_TEMPERATURE),
                                        new DecimalType(temperature.setScale(1)));

                                BigDecimal pressure = new BigDecimal(p[2]);
                                // pressure.round(new MathContext(1, RoundingMode.HALF_UP));
                                // pressure.setScale(1, RoundingMode.HALF_UP);

                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_PRESSURE),
                                        new DecimalType(pressure.setScale(1, RoundingMode.HALF_UP)));
                                break;
                            case "#":
                                break;
                            case "?":
                                // Device has been reset - reconfigure
                                sendToMeteostick(meteostickFormat);
                                sendToMeteostick(meteostickMode);
                                sendToMeteostick(meteostickChannels);
                                break;
                            default:
                                if (p.length < 3) {
                                    logger.debug("MeteoStick bridge: short data ({})", p.length);
                                    break;
                                }

                                try {
                                    meteostickEventListener listener = eventListeners.get(Integer.parseInt(p[1]));
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

            logger.debug("Stopping MeteoStick Recieve Thread");
            serialPort.removeEventListener();
        }
    }
}
