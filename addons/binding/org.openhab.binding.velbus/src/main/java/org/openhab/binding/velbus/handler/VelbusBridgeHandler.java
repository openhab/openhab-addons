/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.velbus.handler;

import static org.openhab.binding.velbus.VelbusBindingConstants.PORT;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TooManyListenersException;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.velbus.internal.VelbusPacketInputStream;
import org.openhab.binding.velbus.internal.VelbusPacketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.NRSerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

/**
 * {@link VelbusBridgeHandler} is the handler for a Velbus Serial-interface and connects it to
 * the framework.
 *
 * @author Cedric Boon - Initial contribution
 */
public class VelbusBridgeHandler extends BaseBridgeHandler implements SerialPortEventListener {

    private Logger logger = LoggerFactory.getLogger(VelbusBridgeHandler.class);

    private static final int BAUD = 9600;
    private NRSerialPort serialPort;
    private OutputStream outputStream;
    private VelbusPacketInputStream inputStream;
    private long lastPacketTimeMillis;

    private VelbusPacketListener defaultPacketListener;
    private Map<Byte, VelbusPacketListener> packetListeners = new HashMap<Byte, VelbusPacketListener>();

    public VelbusBridgeHandler(Bridge velbusBridge) {
        super(velbusBridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // There is nothing to handle in the bridge handler
    }

    @Override
    public void initialize() {
        logger.debug("Initializing velbus bridge handler.");

        String port = (String) getConfig().get(PORT);
        if (port != null) {
            serialPort = new NRSerialPort(port, BAUD);
            if (serialPort.connect()) {
                updateStatus(ThingStatus.ONLINE);
                logger.debug("Bridge online on serial port {}", port);

                outputStream = serialPort.getOutputStream();
                inputStream = new VelbusPacketInputStream(serialPort.getInputStream());

                try {
                    serialPort.addEventListener(this);
                    serialPort.notifyOnDataAvailable(true);
                } catch (TooManyListenersException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Failed to register event listener on serial port " + port);
                    logger.debug("Failed to register event listener on serial port {}", port);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Failed to connect to serial port " + port);
                logger.debug("Failed to connect to serial port {}", port);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Serial port name not configured");
            logger.debug("Serial port name not configured");
        }
    }

    public synchronized void sendPacket(byte[] packet) {
        long currentTimeMillis = System.currentTimeMillis();
        long timeSinceLastPacket = currentTimeMillis - lastPacketTimeMillis;

        if (timeSinceLastPacket < 60) {
            // When sending you need a delay of 60ms between each packet (to prevent flooding the VMB1USB).
            long timeToDelay = 60 - timeSinceLastPacket;

            scheduler.schedule(() -> {
                sendPacket(packet);
            }, timeToDelay, TimeUnit.MILLISECONDS);

            return;
        }

        try {
            outputStream.write(packet);
            outputStream.flush();
        } catch (IOException e) {
            logger.error("Serial port write error", e);
        }

        lastPacketTimeMillis = System.currentTimeMillis();
    }

    public void setDefaultPacketListener(VelbusPacketListener velbusPacketListener) {
        defaultPacketListener = velbusPacketListener;
    }

    public void registerPacketListener(byte address, VelbusPacketListener packetListener) {
        if (packetListener == null) {
            throw new IllegalArgumentException("It's not allowed to pass a null RelayStatusListener.");
        }

        packetListeners.put(Byte.valueOf(address), packetListener);
    }

    public void unregisterRelayStatusListener(byte address, VelbusPacketListener packetListener) {
        packetListeners.remove(Byte.valueOf(address));
    }

    @Override
    public void dispose() {
        if (serialPort != null) {
            serialPort.disconnect();
            serialPort = null;
        }
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        logger.debug("Serial port event triggered");

        if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                byte[] packet;
                while ((packet = inputStream.readPacket()) != null) {
                    byte address = packet[2];

                    VelbusPacketListener packetListener = packetListeners.get(address);
                    if (packetListener != null) {
                        packetListener.onPacketReceived(packet);
                    } else if (defaultPacketListener != null) {
                        defaultPacketListener.onPacketReceived(packet);
                    }
                }
            } catch (IOException e) {
                logger.error("Serial port read error", e);
            }
        }
    }
}
