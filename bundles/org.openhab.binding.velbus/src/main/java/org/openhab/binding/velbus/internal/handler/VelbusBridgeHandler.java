/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.velbus.internal.handler;

import static org.openhab.binding.velbus.internal.VelbusBindingConstants.PORT;

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
import org.eclipse.smarthome.io.transport.serial.PortInUseException;
import org.eclipse.smarthome.io.transport.serial.SerialPort;
import org.eclipse.smarthome.io.transport.serial.SerialPortEvent;
import org.eclipse.smarthome.io.transport.serial.SerialPortEventListener;
import org.eclipse.smarthome.io.transport.serial.SerialPortIdentifier;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.eclipse.smarthome.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.binding.velbus.internal.VelbusPacketInputStream;
import org.openhab.binding.velbus.internal.VelbusPacketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link VelbusBridgeHandler} is the handler for a Velbus Serial-interface and connects it to
 * the framework.
 *
 * @author Cedric Boon - Initial contribution
 */
public class VelbusBridgeHandler extends BaseBridgeHandler implements SerialPortEventListener {

    private Logger logger = LoggerFactory.getLogger(VelbusBridgeHandler.class);

    private static final int BAUD = 9600;
    private SerialPort serialPort;
    private final SerialPortManager serialPortManager;
    private OutputStream outputStream;
    private VelbusPacketInputStream inputStream;
    private long lastPacketTimeMillis;

    private VelbusPacketListener defaultPacketListener;
    private final Map<Byte, VelbusPacketListener> packetListeners = new HashMap<>();

    public VelbusBridgeHandler(Bridge velbusBridge, SerialPortManager serialPortManager) {
        super(velbusBridge);
        this.serialPortManager = serialPortManager;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // There is nothing to handle in the bridge handler
    }

    @Override
    public void initialize() {
        logger.debug("Initializing velbus bridge handler.");

        String port = (String) getConfig().get(PORT);
        if (port == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Serial port name not configured");
            return;
        }

        SerialPortIdentifier serialPortIdentifier = serialPortManager.getIdentifier(port);
        if (serialPortIdentifier == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Serial port not found: " + port);
            return;
        }

        try {
            serialPort = serialPortIdentifier.open(VelbusBridgeHandler.class.getCanonicalName(), 2000);
            serialPort.setSerialPortParams(BAUD, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

            inputStream = new VelbusPacketInputStream(serialPort.getInputStream());
            outputStream = serialPort.getOutputStream();

            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);

            updateStatus(ThingStatus.ONLINE);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Error: " + e.getMessage());
        } catch (PortInUseException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Port already used: " + port);
        } catch (TooManyListenersException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Failed to register event listener on serial port: " + port);
        } catch (UnsupportedCommOperationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Unsupported operation on port '" + port + "': " + e.getMessage());
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
            serialPort.removeEventListener();
            serialPort.close();
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
