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
package org.openhab.binding.rfxcom.internal.handler;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.rfxcom.internal.DeviceMessageListener;
import org.openhab.binding.rfxcom.internal.config.RFXComBridgeConfiguration;
import org.openhab.binding.rfxcom.internal.connector.RFXComConnectorInterface;
import org.openhab.binding.rfxcom.internal.connector.RFXComEventListener;
import org.openhab.binding.rfxcom.internal.connector.RFXComJD2XXConnector;
import org.openhab.binding.rfxcom.internal.connector.RFXComSerialConnector;
import org.openhab.binding.rfxcom.internal.connector.RFXComTcpConnector;
import org.openhab.binding.rfxcom.internal.discovery.RFXComDeviceDiscoveryService;
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
import org.openhab.binding.rfxcom.internal.messages.RFXComMessageFactoryImpl;
import org.openhab.binding.rfxcom.internal.messages.RFXComTransmitterMessage;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private SerialPortManager serialPortManager;

    private RFXComMessageFactory messageFactory;

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
                    byte[] data = msg.decodeMessage();
                    if (logger.isDebugEnabled()) {
                        logger.debug("Transmitting bytes '{}' for message '{}'", HexUtils.bytesToHex(data), msg);
                    }
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

    public RFXComBridgeHandler(@NonNull Bridge br, SerialPortManager serialPortManager) {
        super(br);
        this.serialPortManager = serialPortManager;
        this.messageFactory = RFXComMessageFactoryImpl.INSTANCE;
    }

    public RFXComBridgeHandler(@NonNull Bridge br, SerialPortManager serialPortManager,
            RFXComMessageFactory messageFactory) {
        super(br);
        this.serialPortManager = serialPortManager;
        this.messageFactory = messageFactory;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(RFXComDeviceDiscoveryService.class);
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

        if (configuration.serialPort != null && configuration.serialPort.startsWith("rfc2217")) {
            logger.debug("Please use the Transceiver over TCP/IP bridge type for a serial over IP connection.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.config-error-invalid-thing-type");
            return;
        }

        if (connectorTask == null || connectorTask.isCancelled()) {
            connectorTask = scheduler.scheduleWithFixedDelay(() -> {
                logger.trace("Checking RFXCOM transceiver connection, thing status = {}", thing.getStatus());
                if (thing.getStatus() != ThingStatus.ONLINE) {
                    connect();
                }
            }, 0, 60, TimeUnit.SECONDS);
        }
    }

    private synchronized void connect() {
        logger.debug("Connecting to RFXCOM transceiver");

        try {
            String readerThreadName = "OH-binding-" + getThing().getUID().getAsString();
            if (configuration.serialPort != null) {
                if (connector == null) {
                    connector = new RFXComSerialConnector(serialPortManager, readerThreadName);
                }
            } else if (configuration.bridgeId != null) {
                if (connector == null) {
                    connector = new RFXComJD2XXConnector(readerThreadName);
                }
            } else if (configuration.host != null) {
                if (connector == null) {
                    connector = new RFXComTcpConnector(readerThreadName);
                }
            }

            if (connector != null) {
                connector.disconnect();
                connector.connect(configuration);

                logger.debug("Reset controller");
                connector.sendMessage(RFXComInterfaceMessage.CMD_RESET);

                // controller does not response immediately after reset,
                // so wait a while
                Thread.sleep(300);

                connector.addEventListener(eventListener);

                logger.debug("Get status of controller");
                connector.sendMessage(RFXComInterfaceMessage.CMD_GET_STATUS);
            }
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

    public void sendMessage(RFXComMessage msg) {
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
                RFXComMessage message = messageFactory.createMessage(packet);
                logger.debug("Message received: {}", message);

                if (message instanceof RFXComInterfaceMessage) {
                    RFXComInterfaceMessage msg = (RFXComInterfaceMessage) message;
                    if (msg.subType == SubType.RESPONSE) {
                        if (msg.command == Commands.GET_STATUS) {
                            logger.debug("RFXCOM transceiver/receiver type: {}, hw version: {}.{}, fw version: {}",
                                    msg.transceiverType, msg.hardwareVersion1, msg.hardwareVersion2,
                                    msg.firmwareVersion);
                            if (msg.firmwareVersion < 1000) {
                                /**
                                 * Versions before 1000 had some different behaviour, so lets encourage upgrading.
                                 * 1001 was released in Feb 2016!
                                 */
                                logger.warn(
                                        "RFXCOM device using outdated firmware (version {}), consider flashing with more a more recent version",
                                        msg.firmwareVersion);
                            }
                            thing.setProperty(Thing.PROPERTY_HARDWARE_VERSION,
                                    msg.hardwareVersion1 + "." + msg.hardwareVersion2);
                            thing.setProperty(Thing.PROPERTY_FIRMWARE_VERSION, Integer.toString(msg.firmwareVersion));

                            if (configuration.ignoreConfig) {
                                logger.debug("Ignoring transceiver configuration");
                            } else {
                                byte[] setMode = null;

                                if (configuration.setMode != null && !configuration.setMode.isEmpty()) {
                                    try {
                                        setMode = HexUtils.hexToBytes(configuration.setMode);
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
                                    if (logger.isDebugEnabled()) {
                                        logger.debug("Setting RFXCOM mode using: {}", HexUtils.bytesToHex(setMode));
                                    }
                                    connector.sendMessage(setMode);
                                }
                            }

                            // No need to wait for a response to any set mode. We start
                            // regardless of whether it fails and the RFXCOM's buffer
                            // is big enough to queue up the command.
                            logger.debug("Start receiver");
                            connector.sendMessage(RFXComInterfaceMessage.CMD_START_RECEIVER);
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
                logger.debug("Message not supported, data: {}", HexUtils.bytesToHex(packet));
            } catch (RFXComException e) {
                logger.error("Error occurred during packet receiving, data: {}", HexUtils.bytesToHex(packet), e);
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
        return !deviceStatusListeners.contains(deviceStatusListener) && deviceStatusListeners.add(deviceStatusListener);
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
