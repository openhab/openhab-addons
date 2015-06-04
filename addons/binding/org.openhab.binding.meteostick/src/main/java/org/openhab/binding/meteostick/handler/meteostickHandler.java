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
import java.util.Collections;
import java.util.Set;
import java.util.TooManyListenersException;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link meteostickHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 * 
 * @author Chris Jackson - Initial contribution
 */
public class meteostickHandler extends BaseThingHandler {
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_FINEOFFSET);

    private Logger logger = LoggerFactory.getLogger(meteostickHandler.class);

    private static int RECEIVE_TIMEOUT = 3000;

    private SerialPort serialPort;
    private ZWaveReceiveThread receiveThread;
    
    private String meteostickMode = "m4";
    private final String meteostickFormat = "o1";

    public meteostickHandler(Thing thing) {
        super(thing);
    }
    

    @Override
    public void initialize() {
        logger.debug("Initializing MeteoStick handler.");
        super.initialize();

        Configuration config = getThing().getConfiguration();

        String port = (String) config.get("port");
        connectPort(port);

    }

    @Override
    public void dispose() {
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    /**
     * Connects to the comm port and starts send and receive threads.
     * 
     * @param serialPortName the port name to open
     * @throws SerialInterfaceException when a connection error occurs.
     */
    public void connectPort(final String serialPortName) {
        logger.info("Connecting to serial port {}", serialPortName);
        try {
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(serialPortName);
            CommPort commPort = portIdentifier.open("org.openhab.binding.zwave", 2000);
            this.serialPort = (SerialPort) commPort;
            this.serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            this.serialPort.enableReceiveThreshold(1);
            this.serialPort.enableReceiveTimeout(RECEIVE_TIMEOUT);
            this.receiveThread = new ZWaveReceiveThread();
            this.receiveThread.start();

            // RXTX serial port library causes high CPU load
            // Start event listener, which will just sleep and slow down event loop
            serialPort.addEventListener(this.receiveThread);
            serialPort.notifyOnDataAvailable(true);

            logger.info("Serial port is initialized");
        } catch (NoSuchPortException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "Serial Error: Port " + serialPortName + " does not exist");
        } catch (PortInUseException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "Serial Error: Port " + serialPortName + " in use");
        } catch (UnsupportedCommOperationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "Serial Error: Unsupported comm operation on Port " + serialPortName);
        } catch (TooManyListenersException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "Serial Error: Too many listeners on Port " + serialPortName);
        }
    }

    /**
     * Disconnects from the serial interface and stops
     * send and receive threads.
     */
    public void disconnect() {
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

    public void sendToMeteostick(String string) {
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
     * Receive Thread. Takes care of receiving all messages.
     * 
     * @author Chris Jackson
     */
    private class ZWaveReceiveThread extends Thread implements SerialPortEventListener {

        private final Logger logger = LoggerFactory.getLogger(ZWaveReceiveThread.class);

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
            logger.debug("Starting MeteoStick Recieve Thread");
            try {
                byte[] rxPacket = new byte[100];
                int rxCnt = 0;
                while (!interrupted()) {
                    int rxByte;

                    try {
                        rxByte = serialPort.getInputStream().read();

                        if (rxByte == -1) {
                            continue;
                        }
                    } catch (IOException e) {
                        logger.error("Got I/O exception {} during receiving. exiting thread.", e.getLocalizedMessage());
                        break;
                    }

                    // Check for end of line
                    if(rxByte == 13) {
                        updateStatus(ThingStatus.ONLINE);

                        String inputString = new String(rxPacket, 0, rxCnt);
                        String p[] = inputString.split("\\s+");
                        switch(p[0]) {
                            case "?":
                                // Device has been reset
                                sendToMeteostick(meteostickFormat);
                                sendToMeteostick(meteostickMode);
                                break;
                            case "B":       // Barometer
                                
                                break;
                        }
                        rxCnt = 0;
                    }
                    else if(rxByte != 10) {
                        rxPacket[rxCnt++] = (byte) rxByte;
                    }
                }
            } catch (Exception e) {
                logger.error("Exception during Z-Wave thread: Receive", e);
            }
            logger.debug("Stopped Z-Wave thread: Receive");

            serialPort.removeEventListener();
        }
    }
}
