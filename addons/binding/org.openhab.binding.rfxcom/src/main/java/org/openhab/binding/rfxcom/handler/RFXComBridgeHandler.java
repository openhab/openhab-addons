/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.handler;

import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.DatatypeConverter;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.rfxcom.internal.DeviceMessageListener;
import org.openhab.binding.rfxcom.internal.config.RFXComBridgeConfiguration;
import org.openhab.binding.rfxcom.internal.connector.RFXComConnectorInterface;
import org.openhab.binding.rfxcom.internal.connector.RFXComEventListener;
import org.openhab.binding.rfxcom.internal.connector.RFXComJD2XXConnector;
import org.openhab.binding.rfxcom.internal.connector.RFXComSerialConnector;
import org.openhab.binding.rfxcom.internal.connector.RFXComTcpConnector;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComMessageNotImplementedException;
import org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage;
import org.openhab.binding.rfxcom.internal.messages.RFXComDeviceMessage;
import org.openhab.binding.rfxcom.internal.messages.RFXComInterfaceControlMessage;
import org.openhab.binding.rfxcom.internal.messages.RFXComInterfaceMessage;
import org.openhab.binding.rfxcom.internal.messages.RFXComInterfaceMessage.Commands;
import org.openhab.binding.rfxcom.internal.messages.RFXComInterfaceMessage.SubType;
import org.openhab.binding.rfxcom.internal.messages.RFXComMessage;
import org.openhab.binding.rfxcom.internal.messages.RFXComMessageFactory;
import org.openhab.binding.rfxcom.internal.messages.RFXComTransmitterMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.NoSuchPortException;

/**
 * {@link RFXComBridgeHandler} is the handler for a RFXCOM transceivers. All
 * {@link RFXComHandler}s use the {@link RFXComBridgeHandler} to execute the
 * actual commands.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class RFXComBridgeHandler extends BaseBridgeHandler {
    private Logger logger = LoggerFactory.getLogger(RFXComBridgeHandler.class);

    private RFXComConnectorInterface connector = null;
    private MessageListener eventListener = new MessageListener();

    private List<DeviceMessageListener> deviceStatusListeners = new CopyOnWriteArrayList<>();

    private RFXComBridgeConfiguration configuration = null;
    private ScheduledFuture<?> connectorTask;

    private class TransmitQueue {
        private Queue<RFXComBaseMessage> queue = new LinkedBlockingQueue<>();

        public synchronized void enqueue(RFXComBaseMessage msg) throws IOException {
            boolean wasEmpty = queue.isEmpty();
            if (queue.offer(msg)) {
                if (wasEmpty) {
                    send();
                }
            } else {
                logger.error("Transmit queue overflow. Lost message: {}", msg);
            }
        }

        public synchronized void sendNext() throws IOException {
            queue.poll();
            send();
        }

        public synchronized void send() throws IOException {
            while (!queue.isEmpty()) {
                RFXComBaseMessage msg = queue.peek();

                try {
                    logger.debug("Transmitting message '{}'", msg);
                    byte[] data = msg.decodeMessage();
                    connector.sendMessage(data);
                    break;
                } catch (RFXComException rfxe) {
                    logger.error("Error during send of {}", msg, rfxe);
                    queue.poll();
                }
            }
        }
    }

    private TransmitQueue transmitQueue = new TransmitQueue();

    public RFXComBridgeHandler(@NonNull Bridge br) {
        super(br);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Bridge commands not supported.");
    }

    @Override
    public synchronized void dispose() {
        logger.debug("Handler disposed.");

        for (DeviceMessageListener deviceStatusListener : deviceStatusListeners) {
            unregisterDeviceStatusListener(deviceStatusListener);
        }

        if (connector != null) {
            connector.removeEventListener(eventListener);
            connector.disconnect();
            connector = null;
        }

        if (connectorTask != null && !connectorTask.isCancelled()) {
            connectorTask.cancel(true);
            connectorTask = null;
        }

        super.dispose();
    }

    @Override
    public void initialize() {
        logger.debug("Initializing RFXCOM bridge handler");
        updateStatus(ThingStatus.OFFLINE);

        configuration = getConfigAs(RFXComBridgeConfiguration.class);

        if (connectorTask == null || connectorTask.isCancelled()) {
            connectorTask = scheduler.scheduleWithFixedDelay(new Runnable() {

                @Override
                public void run() {
                    logger.debug("Checking RFXCOM transceiver connection, thing status = {}", thing.getStatus());
                    if (thing.getStatus() != ThingStatus.ONLINE) {
                        connect();
                    }
                }
            }, 0, 60, TimeUnit.SECONDS);
        }
    }

    private synchronized void connect() {
        logger.debug("Connecting to RFXCOM transceiver");

        try {
            if (configuration.serialPort != null) {
                if (connector == null) {
                    connector = new RFXComSerialConnector();
                }
            } else if (configuration.bridgeId != null) {
                if (connector == null) {
                    connector = new RFXComJD2XXConnector();
                }
            } else if (configuration.host != null) {
                if (connector == null) {
                    connector = new RFXComTcpConnector();
                }
            }

            if (connector != null) {
                connector.disconnect();
                connector.connect(configuration);

                logger.debug("Reset controller");
                connector.sendMessage(RFXComMessageFactory.CMD_RESET);

                // controller does not response immediately after reset,
                // so wait a while
                Thread.sleep(300);

                connector.addEventListener(eventListener);

                logger.debug("Get status of controller");
                connector.sendMessage(RFXComMessageFactory.CMD_GET_STATUS);
            }
        } catch (NoSuchPortException e) {
            logger.error("Connection to RFXCOM transceiver failed", e);
        } catch (IOException e) {
            logger.error("Connection to RFXCOM transceiver failed", e);
            if ("device not opened (3)".equalsIgnoreCase(e.getMessage())) {
                if (connector instanceof RFXComJD2XXConnector) {
                    logger.info("Automatically Discovered RFXCOM bridges use FTDI chip driver (D2XX)."
                            + " Reason for this error normally is related to operating system native FTDI drivers,"
                            + " which prevent D2XX driver to open device."
                            + " To solve this problem, uninstall OS FTDI native drivers or add manually universal bridge 'RFXCOM USB Transceiver',"
                            + " which use normal serial port driver rather than D2XX.");
                }
            }
        } catch (Exception e) {
            logger.error("Connection to RFXCOM transceiver failed", e);
        } catch (UnsatisfiedLinkError e) {
            logger.error("Error occurred when trying to load native library for OS '{}' version '{}', processor '{}'",
                    System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"), e);
        }
    }

    public void sendMessage(RFXComMessage msg) throws RFXComException {
        try {
            RFXComBaseMessage baseMsg = (RFXComBaseMessage) msg;
            transmitQueue.enqueue(baseMsg);
        } catch (IOException e) {
            logger.error("I/O Error", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private class MessageListener implements RFXComEventListener {

        @Override
        public void packetReceived(byte[] packet) {
            try {
                RFXComMessage message = RFXComMessageFactory.createMessage(packet);
                logger.debug("Message received: {}", message);

                if (message instanceof RFXComInterfaceMessage) {
                    RFXComInterfaceMessage msg = (RFXComInterfaceMessage) message;
                    if (msg.subType == SubType.RESPONSE) {
                        if (msg.command == Commands.GET_STATUS) {
                            logger.info("RFXCOM transceiver/receiver type: {}, hw version: {}.{}, fw version: {}",
                                msg.transceiverType, msg.hardwareVersion1, msg.hardwareVersion2, msg.firmwareVersion);
                            thing.setProperty(Thing.PROPERTY_HARDWARE_VERSION, msg.hardwareVersion1 + "." + msg.hardwareVersion2);
                            thing.setProperty(Thing.PROPERTY_FIRMWARE_VERSION, Integer.toString(msg.firmwareVersion));

                            if (configuration.ignoreConfig) {
                                logger.debug("Ignoring transceiver configuration");
                            } else {
                                byte[] setMode = null;

                                if (configuration.setMode != null && !configuration.setMode.isEmpty()) {
                                    try {
                                        setMode = DatatypeConverter.parseHexBinary(configuration.setMode);
                                        if (setMode.length != 14) {
                                            logger.warn("Invalid RFXCOM transceiver mode configuration");
                                            setMode = null;
                                        }
                                    } catch (IllegalArgumentException ee) {
                                        logger.warn("Failed to parse setMode data", ee);
                                    }
                                } else {
                                    RFXComInterfaceControlMessage modeMsg = new RFXComInterfaceControlMessage(
                                            msg.transceiverType, configuration);
                                    setMode = modeMsg.decodeMessage();
                                }

                                if (setMode != null) {
                                    logger.debug("Setting RFXCOM mode using: {}",
                                            DatatypeConverter.printHexBinary(setMode));
                                    connector.sendMessage(setMode);
                                }
                            }

                            // No need to wait for a response to any set mode. We start
                            // regardless of whether it fails and the RFXCOM's buffer
                            // is big enough to queue up the command.
                            logger.debug("Start receiver");
                            connector.sendMessage(RFXComMessageFactory.CMD_START_RECEIVER);
                        }
                    } else if (msg.subType == SubType.START_RECEIVER) {
                        updateStatus(ThingStatus.ONLINE);
                        logger.debug("Start TX of any queued messages");
                        transmitQueue.send();
                    } else {
                        logger.debug("Interface response received: {}", msg);
                        transmitQueue.sendNext();
                    }
                } else if (message instanceof RFXComTransmitterMessage) {
                    RFXComTransmitterMessage resp = (RFXComTransmitterMessage) message;

                    logger.debug("Transmitter response received: {}", resp);

                    transmitQueue.sendNext();
                } else if (message instanceof RFXComDeviceMessage) {
                    for (DeviceMessageListener deviceStatusListener : deviceStatusListeners) {
                        try {
                            deviceStatusListener.onDeviceMessageReceived(getThing().getUID(),
                                    (RFXComDeviceMessage) message);
                        } catch (Exception e) {
                            // catch all exceptions give all handlers a fair chance of handling the messages
                            logger.error("An exception occurred while calling the DeviceStatusListener", e);
                        }
                    }
                } else {
                    logger.warn("The received message cannot be processed, please create an "
                            + "issue at the relevant tracker. Received message: {}", message);
                }
            } catch (RFXComMessageNotImplementedException e) {
                logger.debug("Message not supported, data: {}", DatatypeConverter.printHexBinary(packet));
            } catch (RFXComException e) {
                logger.error("Error occurred during packet receiving, data: {}",
                        DatatypeConverter.printHexBinary(packet), e);
            } catch (IOException e) {
                errorOccurred("I/O error");
            }
        }

        @Override
        public void errorOccurred(String error) {
            logger.error("Error occurred: {}", error);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    public boolean registerDeviceStatusListener(DeviceMessageListener deviceStatusListener) {
        if (deviceStatusListener == null) {
            throw new IllegalArgumentException("It's not allowed to pass a null deviceStatusListener.");
        }
        return deviceStatusListeners.contains(deviceStatusListener) ? false
                : deviceStatusListeners.add(deviceStatusListener);
    }

    public boolean unregisterDeviceStatusListener(DeviceMessageListener deviceStatusListener) {
        if (deviceStatusListener == null) {
            throw new IllegalArgumentException("It's not allowed to pass a null deviceStatusListener.");
        }
        return deviceStatusListeners.remove(deviceStatusListener);
    }

    public RFXComBridgeConfiguration getConfiguration() {
        return configuration;
    }
}
