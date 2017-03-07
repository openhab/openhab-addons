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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.DatatypeConverter;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.rfxcom.RFXComBindingConstants;
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
import org.openhab.binding.rfxcom.internal.messages.RFXComInterfaceMessage;
import org.openhab.binding.rfxcom.internal.messages.RFXComInterfaceMessage.Commands;
import org.openhab.binding.rfxcom.internal.messages.RFXComInterfaceMessage.SubType;
import org.openhab.binding.rfxcom.internal.messages.RFXComInterfaceMessage.TransceiverType;
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

    RFXComConnectorInterface connector = null;
    private MessageListener eventListener = new MessageListener();

    private List<DeviceMessageListener> deviceStatusListeners = new CopyOnWriteArrayList<>();

    private static final int timeout = 5000;
    private static byte seqNbr = 0;
    private static RFXComTransmitterMessage responseMessage = null;
    private Object notifierObject = new Object();
    private RFXComBridgeConfiguration configuration = null;
    private ScheduledFuture<?> connectorTask;

    public RFXComBridgeHandler(Bridge br) {
        super(br);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Bridge commands not supported.");
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed.");

        for (DeviceMessageListener deviceStatusListener : deviceStatusListeners) {
            unregisterDeviceStatusListener(deviceStatusListener);
        }

        if (connector != null) {
            connector.removeEventListener(eventListener);
            connector.disconnect();
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
            connectorTask = scheduler.scheduleAtFixedRate(new Runnable() {

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

    private static synchronized byte getSeqNumber() {
        return seqNbr;
    }

    private static synchronized byte getNextSeqNumber() {
        if (++seqNbr == 0) {
            seqNbr = 1;
        }

        return seqNbr;
    }

    private static synchronized RFXComTransmitterMessage getResponseMessage() {
        return responseMessage;
    }

    private static synchronized void setResponseMessage(RFXComTransmitterMessage respMessage) {
        responseMessage = respMessage;
    }

    private void connect() {
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
                // wait response
                Thread.sleep(200);

                if (configuration.ignoreConfig) {
                    logger.debug("Ignoring transceiver configuration");
                } else {

                    byte[] setMode = new byte[0];

                    try {
                        setMode = createConfMessage(getThing().getUID().toString(), configuration);

                    } catch (IllegalArgumentException e) {

                        if (configuration.setMode != null && !configuration.setMode.isEmpty()) {
                            try {
                                setMode = DatatypeConverter.parseHexBinary(configuration.setMode);

                            } catch (IllegalArgumentException ee) {
                                logger.warn("setMode hexBinary value length should be 14 bytes (28 characters)");
                            }
                        }
                    } finally {
                        if (setMode.length == 14) {
                            logger.debug("Setting RFXCOM mode: {}", DatatypeConverter.printHexBinary(setMode));

                            connector.sendMessage(setMode);
                            // wait response
                            Thread.sleep(200);
                        } else if (setMode.length > 0) {
                            logger.warn("Illegal RFXCOM transceiver mode configuration");
                        }
                    }

                }

                logger.debug("Start receiver");
                connector.sendMessage(RFXComMessageFactory.CMD_START_RECEIVER);
                updateStatus(ThingStatus.ONLINE);
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

    private byte[] createConfMessage(String bridgeType, RFXComBridgeConfiguration conf) {
        if (conf != null && bridgeType != null) {
            RFXComInterfaceMessage msg = new RFXComInterfaceMessage();
            msg.command = Commands.SET_MODE;

            switch (bridgeType) {
                case RFXComBindingConstants.BRIDGE_TYPE_RFXTRX315:
                    if (conf.transceiverType != null) {
                        switch (conf.transceiverType) {
                            case RFXComBindingConstants.TRANSCEIVER_310MHz:
                                msg.transceiverType = TransceiverType._310MHZ;
                                break;
                            case RFXComBindingConstants.TRANSCEIVER_315MHz:
                                msg.transceiverType = TransceiverType._315MHZ;
                                break;
                            default:
                                throw new IllegalArgumentException("Illegal transceiver type");
                        }
                    }
                    break;

                case RFXComBindingConstants.BRIDGE_TYPE_RFXREC433:
                    msg.transceiverType = TransceiverType._433_92MHZ_RECEIVER_ONLY;
                    break;

                case RFXComBindingConstants.BRIDGE_TYPE_RFXTRX433:
                    msg.transceiverType = TransceiverType._433_92MHZ_TRANSCEIVER;
                    break;

                case RFXComBindingConstants.BRIDGE_TYPE_MANUAL_BRIDGE:
                case RFXComBindingConstants.BRIDGE_TYPE_TCP_BRIDGE:
                    if (conf.transceiverType != null) {
                        switch (conf.transceiverType) {
                            case RFXComBindingConstants.TRANSCEIVER_433_92MHz:
                                msg.transceiverType = TransceiverType._433_92MHZ_TRANSCEIVER;
                                break;
                            case RFXComBindingConstants.TRANSCEIVER_433_92MHz_R:
                                msg.transceiverType = TransceiverType._433_92MHZ_RECEIVER_ONLY;
                                break;
                            case RFXComBindingConstants.TRANSCEIVER_310MHz:
                                msg.transceiverType = TransceiverType._310MHZ;
                                break;
                            case RFXComBindingConstants.TRANSCEIVER_315MHz:
                                msg.transceiverType = TransceiverType._315MHZ;
                                break;
                            default:
                                throw new IllegalArgumentException("Illegal transceiver type");
                        }
                    }
                    break;

                default:
                    throw new IllegalArgumentException("Illegal transceiver type");
            }

            msg.enableUndecodedPackets = configuration.enableUndecoded;
            msg.enableImagintronixOpusPackets = configuration.enableImagintronixOpus;
            msg.enableByronSXPackets = configuration.enableByronSX;
            msg.enableRSLPackets = configuration.enableRSL;
            msg.enableLighting4Packets = configuration.enableLighting4;
            msg.enableFineOffsetPackets = configuration.enableFineOffsetViking;
            msg.enableRubicsonPackets = configuration.enableRubicson;
            msg.enableAEPackets = configuration.enableAEBlyss;
            msg.enableBlindsT1T2T3T4Packets = configuration.enableBlindsT1T2T3T4;
            msg.enableBlindsT0Packets = configuration.enableBlindsT0;
            msg.enableProGuardPackets = configuration.enableProGuard;
            msg.enableLaCrossePackets = configuration.enableLaCrosse;
            msg.enableHidekiUPMPackets = configuration.enableHidekiUPM;
            msg.enableADPackets = configuration.enableADLightwaveRF;
            msg.enableMertikPackets = configuration.enableMertik;
            msg.enableVisonicPackets = configuration.enableVisonic;
            msg.enableATIPackets = configuration.enableATI;
            msg.enableOregonPackets = configuration.enableOregonScientific;
            msg.enableMeiantechPackets = configuration.enableMeiantech;
            msg.enableHomeEasyPackets = configuration.enableHomeEasyEU;
            msg.enableACPackets = configuration.enableAC;
            msg.enableARCPackets = configuration.enableARC;
            msg.enableX10Packets = configuration.enableX10;

            return msg.decodeMessage();
        }

        throw new IllegalArgumentException("");
    }

    public synchronized void sendMessage(RFXComMessage msg) throws RFXComException {

        ((RFXComBaseMessage) msg).seqNbr = getNextSeqNumber();
        byte[] data = msg.decodeMessage();

        logger.debug("Transmitting message '{}'", msg);
        logger.trace("Transmitting data: {}", DatatypeConverter.printHexBinary(data));

        setResponseMessage(null);

        try {
            connector.sendMessage(data);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            throw new RFXComException("Send failed, reason: " + e.getMessage(), e);
        }

        try {

            RFXComTransmitterMessage resp = null;
            synchronized (notifierObject) {
                notifierObject.wait(timeout);
                resp = getResponseMessage();
            }

            if (resp != null) {
                switch (resp.response) {
                    case ACK:
                    case ACK_DELAYED:
                        logger.debug("Command successfully transmitted, '{}' received", resp.response);
                        break;

                    case NAK:
                    case NAK_INVALID_AC_ADDRESS:
                }
            } else {
                logger.warn("No response received from transceiver");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }

        } catch (InterruptedException ie) {
            logger.error("No acknowledge received from RFXCOM controller, timeout {}ms ", timeout);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
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
                        logger.debug("RFXCOM transceiver/receiver type: {}, hw version: {}.{}, fw version: {}",
                                msg.transceiverType, msg.hardwareVersion1, msg.hardwareVersion2, msg.firmwareVersion);
                    }
                } else if (message instanceof RFXComTransmitterMessage) {
                    RFXComTransmitterMessage resp = (RFXComTransmitterMessage) message;

                    byte seqNbr = getSeqNumber();
                    if (resp.seqNbr == seqNbr) {
                        logger.debug("Transmitter response received: {}", message.toString());
                        setResponseMessage(resp);
                        synchronized (notifierObject) {
                            notifierObject.notify();
                        }
                    } else {
                        logger.warn("Sequence number '{}' does not match, expecting number '{}'", resp.seqNbr, seqNbr);
                    }

                } else {

                    for (DeviceMessageListener deviceStatusListener : deviceStatusListeners) {
                        try {
                            deviceStatusListener.onDeviceMessageReceived(getThing().getUID(), message);
                        } catch (Exception e) {
                            logger.error("An exception occurred while calling the DeviceStatusListener", e);
                        }
                    }
                }
            } catch (RFXComMessageNotImplementedException e) {
                logger.debug("Message not supported, data: {}", DatatypeConverter.printHexBinary(packet));
            } catch (RFXComException e) {
                logger.error("Error occured during packet receiving, data: {}",
                        DatatypeConverter.printHexBinary(packet), e);
            }

            updateStatus(ThingStatus.ONLINE);
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

}
