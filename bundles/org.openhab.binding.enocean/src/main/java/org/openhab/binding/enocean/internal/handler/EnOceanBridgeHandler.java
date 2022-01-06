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
package org.openhab.binding.enocean.internal.handler;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.openhab.binding.enocean.internal.EnOceanConfigStatusMessage;
import org.openhab.binding.enocean.internal.config.EnOceanBaseConfig;
import org.openhab.binding.enocean.internal.config.EnOceanBridgeConfig;
import org.openhab.binding.enocean.internal.config.EnOceanBridgeConfig.ESPVersion;
import org.openhab.binding.enocean.internal.messages.BasePacket;
import org.openhab.binding.enocean.internal.messages.ESP3PacketFactory;
import org.openhab.binding.enocean.internal.messages.Response;
import org.openhab.binding.enocean.internal.messages.Response.ResponseType;
import org.openhab.binding.enocean.internal.messages.Responses.BaseResponse;
import org.openhab.binding.enocean.internal.messages.Responses.RDBaseIdResponse;
import org.openhab.binding.enocean.internal.messages.Responses.RDLearnedClientsResponse;
import org.openhab.binding.enocean.internal.messages.Responses.RDLearnedClientsResponse.LearnedClient;
import org.openhab.binding.enocean.internal.messages.Responses.RDRepeaterResponse;
import org.openhab.binding.enocean.internal.messages.Responses.RDVersionResponse;
import org.openhab.binding.enocean.internal.transceiver.EnOceanESP2Transceiver;
import org.openhab.binding.enocean.internal.transceiver.EnOceanESP3Transceiver;
import org.openhab.binding.enocean.internal.transceiver.EnOceanTransceiver;
import org.openhab.binding.enocean.internal.transceiver.PacketListener;
import org.openhab.binding.enocean.internal.transceiver.ResponseListener;
import org.openhab.binding.enocean.internal.transceiver.ResponseListenerIgnoringTimeouts;
import org.openhab.binding.enocean.internal.transceiver.TeachInListener;
import org.openhab.binding.enocean.internal.transceiver.TransceiverErrorListener;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.core.status.ConfigStatusMessage;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ConfigStatusBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EnOceanBridgeHandler} is responsible for sending ESP3Packages build by {@link EnOceanActuatorHandler} and
 * transferring received ESP3Packages to {@link EnOceanSensorHandler}.
 *
 * @author Daniel Weber - Initial contribution
 */
public class EnOceanBridgeHandler extends ConfigStatusBridgeHandler implements TransceiverErrorListener {

    private Logger logger = LoggerFactory.getLogger(EnOceanBridgeHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_BRIDGE);

    private EnOceanTransceiver transceiver; // holds connection to serial/tcp port and sends/receives messages
    private ScheduledFuture<?> connectorTask; // is used for reconnection if something goes wrong

    private byte[] baseId = null;
    private Thing[] sendingThings = new Thing[128];

    private SerialPortManager serialPortManager;

    private boolean smackAvailable = false;
    private boolean sendTeachOuts = true;
    private Set<String> smackClients = Set.of();

    public EnOceanBridgeHandler(Bridge bridge, SerialPortManager serialPortManager) {
        super(bridge);
        this.serialPortManager = serialPortManager;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (transceiver == null) {
            updateStatus(ThingStatus.OFFLINE);
            return;
        }

        switch (channelUID.getId()) {
            case CHANNEL_REPEATERMODE:
                if (command instanceof RefreshType) {
                    sendMessage(ESP3PacketFactory.CO_RD_REPEATER,
                            new ResponseListenerIgnoringTimeouts<RDRepeaterResponse>() {
                                @Override
                                public void responseReceived(RDRepeaterResponse response) {
                                    if (response.isValid() && response.isOK()) {
                                        updateState(channelUID, response.getRepeaterLevel());
                                    } else {
                                        updateState(channelUID, new StringType(REPEATERMODE_OFF));
                                    }
                                }
                            });
                } else if (command instanceof StringType) {
                    sendMessage(ESP3PacketFactory.CO_WR_REPEATER((StringType) command),
                            new ResponseListenerIgnoringTimeouts<BaseResponse>() {
                                @Override
                                public void responseReceived(BaseResponse response) {
                                    if (response.isOK()) {
                                        updateState(channelUID, (StringType) command);
                                    }
                                }
                            });
                }
                break;

            case CHANNEL_SETBASEID:
                if (command instanceof StringType) {
                    try {
                        byte[] id = HexUtils.hexToBytes(((StringType) command).toFullString());

                        sendMessage(ESP3PacketFactory.CO_WR_IDBASE(id),
                                new ResponseListenerIgnoringTimeouts<BaseResponse>() {
                                    @Override
                                    public void responseReceived(BaseResponse response) {
                                        if (response.isOK()) {
                                            updateState(channelUID, new StringType("New Id successfully set"));
                                        } else if (response.getResponseType() == ResponseType.RET_FLASH_HW_ERROR) {
                                            updateState(channelUID,
                                                    new StringType("The write/erase/verify process failed"));
                                        } else if (response.getResponseType() == ResponseType.RET_BASEID_OUT_OF_RANGE) {
                                            updateState(channelUID, new StringType("Base id out of range"));
                                        } else if (response.getResponseType() == ResponseType.RET_BASEID_MAX_REACHED) {
                                            updateState(channelUID, new StringType("No more change possible"));
                                        }
                                    }
                                });
                    } catch (IllegalArgumentException e) {
                        updateState(channelUID, new StringType("BaseId could not be parsed"));
                    }
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "trying to connect to gateway...");
        if (this.serialPortManager == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "SerialPortManager could not be found");
        } else {
            if (connectorTask == null || connectorTask.isDone()) {
                connectorTask = scheduler.scheduleWithFixedDelay(new Runnable() {
                    @Override
                    public void run() {
                        if (thing.getStatus() != ThingStatus.ONLINE) {
                            initTransceiver();
                        }
                    }
                }, 0, 60, TimeUnit.SECONDS);
            }
        }
    }

    private synchronized void initTransceiver() {
        try {
            EnOceanBridgeConfig c = getThing().getConfiguration().as(EnOceanBridgeConfig.class);
            if (transceiver != null) {
                transceiver.ShutDown();
            }

            switch (c.getESPVersion()) {
                case ESP2:
                    transceiver = new EnOceanESP2Transceiver(c.path, this, scheduler, serialPortManager);
                    smackAvailable = false;
                    sendTeachOuts = false;
                    break;
                case ESP3:
                    transceiver = new EnOceanESP3Transceiver(c.path, this, scheduler, serialPortManager);
                    sendTeachOuts = c.sendTeachOuts;
                    break;
                default:
                    break;
            }

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "opening serial port...");
            transceiver.Initialize();

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "starting rx thread...");
            transceiver.StartReceiving(scheduler);
            logger.info("EnOceanSerialTransceiver RX thread up and running");

            if (c.rs485) {
                if (c.rs485BaseId != null && !c.rs485BaseId.isEmpty()) {
                    baseId = HexUtils.hexToBytes(c.rs485BaseId);
                    if (baseId.length != 4) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                "RS485 BaseId has the wrong format. It is expected to be an 8 digit hex code, for example 01000000");
                    }
                } else {
                    baseId = new byte[4];
                }

                updateProperty(PROPERTY_BASE_ID, HexUtils.bytesToHex(baseId));
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                        "trying to get bridge base id...");

                logger.debug("request base id");
                transceiver.sendBasePacket(ESP3PacketFactory.CO_RD_IDBASE,
                        new ResponseListenerIgnoringTimeouts<RDBaseIdResponse>() {
                            @Override
                            public void responseReceived(RDBaseIdResponse response) {
                                logger.debug("received response for base id");
                                if (response.isValid() && response.isOK()) {
                                    baseId = response.getBaseId().clone();
                                    updateProperty(PROPERTY_BASE_ID, HexUtils.bytesToHex(response.getBaseId()));
                                    updateProperty(PROPERTY_REMAINING_WRITE_CYCLES_Base_ID,
                                            Integer.toString(response.getRemainingWriteCycles()));
                                    transceiver.setFilteredDeviceId(baseId);

                                    updateStatus(ThingStatus.ONLINE);
                                } else {
                                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                            "Could not get BaseId");
                                }
                            }
                        });

                if (c.getESPVersion() == ESPVersion.ESP3) {
                    logger.debug("set postmaster mailboxes");
                    transceiver.sendBasePacket(ESP3PacketFactory.SA_WR_POSTMASTER((byte) (c.enableSmack ? 20 : 0)),
                            new ResponseListenerIgnoringTimeouts<BaseResponse>() {

                                @Override
                                public void responseReceived(BaseResponse response) {

                                    logger.debug("received response for postmaster mailboxes");
                                    if (response.isOK()) {
                                        updateProperty("Postmaster mailboxes:",
                                                Integer.toString(c.enableSmack ? 20 : 0));
                                        smackAvailable = c.enableSmack;
                                        refreshProperties();
                                    } else {
                                        updateProperty("Postmaster mailboxes:", "Not supported");
                                        smackAvailable = false;
                                    }
                                }
                            });
                }
            }

            logger.debug("request version info");
            transceiver.sendBasePacket(ESP3PacketFactory.CO_RD_VERSION,
                    new ResponseListenerIgnoringTimeouts<RDVersionResponse>() {
                        @Override
                        public void responseReceived(RDVersionResponse response) {
                            if (response.isValid() && response.isOK()) {
                                updateProperty(PROPERTY_APP_VERSION, response.getAPPVersion());
                                updateProperty(PROPERTY_API_VERSION, response.getAPIVersion());
                                updateProperty(PROPERTY_CHIP_ID, response.getChipID());
                                updateProperty(PROPERTY_DESCRIPTION, response.getDescription());
                            }
                        }
                    });
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Port could not be found");
        } catch (PortInUseException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Port already in use");
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Port could not be initialized");
            return;
        }
    }

    @Override
    public synchronized void dispose() {
        if (transceiver != null) {
            transceiver.ShutDown();
            transceiver = null;
        }

        if (connectorTask != null && !connectorTask.isDone()) {
            connectorTask.cancel(true);
            connectorTask = null;
        }

        super.dispose();
    }

    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        Collection<ConfigStatusMessage> configStatusMessages = new LinkedList<>();

        // The serial port must be provided
        String path = getThing().getConfiguration().as(EnOceanBridgeConfig.class).path;
        if (path == null || path.isEmpty()) {
            configStatusMessages.add(ConfigStatusMessage.Builder.error(PATH)
                    .withMessageKeySuffix(EnOceanConfigStatusMessage.PORT_MISSING.getMessageKey()).withArguments(PATH)
                    .build());
        }

        return configStatusMessages;
    }

    public byte[] getBaseId() {
        return baseId.clone();
    }

    public boolean isSmackClient(Thing sender) {
        return smackClients.contains(sender.getConfiguration().as(EnOceanBaseConfig.class).enoceanId);
    }

    public Integer getNextSenderId(Thing sender) {
        return getNextSenderId(sender.getConfiguration().as(EnOceanBaseConfig.class).enoceanId);
    }

    public Integer getNextSenderId(String enoceanId) {
        EnOceanBridgeConfig config = getConfigAs(EnOceanBridgeConfig.class);

        if (config.nextSenderId != null && sendingThings[config.nextSenderId] == null) {
            Configuration c = this.editConfiguration();
            c.put(PARAMETER_NEXT_SENDERID, null);
            updateConfiguration(c);

            return config.nextSenderId;
        }

        for (int i = 1; i < sendingThings.length; i++) {
            if (sendingThings[i] == null || sendingThings[i].getConfiguration().as(EnOceanBaseConfig.class).enoceanId
                    .equalsIgnoreCase(enoceanId)) {
                return i;
            }
        }

        return null;
    }

    public boolean existsSender(int id, Thing sender) {
        return sendingThings[id] != null && !sendingThings[id].getConfiguration().as(EnOceanBaseConfig.class).enoceanId
                .equalsIgnoreCase(sender.getConfiguration().as(EnOceanBaseConfig.class).enoceanId);
    }

    public void addSender(int id, Thing thing) {
        sendingThings[id] = thing;
    }

    public void removeSender(int id) {
        sendingThings[id] = null;
    }

    public <T extends Response> void sendMessage(BasePacket message, ResponseListener<T> responseListener) {
        try {
            transceiver.sendBasePacket(message, responseListener);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    public void addPacketListener(PacketListener listener) {
        addPacketListener(listener, listener.getEnOceanIdToListenTo());
    }

    public void addPacketListener(PacketListener listener, long senderIdToListenTo) {
        if (transceiver != null) {
            transceiver.addPacketListener(listener, senderIdToListenTo);
        }
    }

    public void removePacketListener(PacketListener listener) {
        removePacketListener(listener, listener.getEnOceanIdToListenTo());
    }

    public void removePacketListener(PacketListener listener, long senderIdToListenTo) {
        if (transceiver != null) {
            transceiver.removePacketListener(listener, senderIdToListenTo);
        }
    }

    public void startDiscovery(TeachInListener teachInListener) {
        transceiver.startDiscovery(teachInListener);

        if (smackAvailable) {
            // activate smack teach in
            logger.debug("activate smack teach in");
            try {
                transceiver.sendBasePacket(ESP3PacketFactory.SA_WR_LEARNMODE(true),
                        new ResponseListenerIgnoringTimeouts<BaseResponse>() {

                            @Override
                            public void responseReceived(BaseResponse response) {

                                if (response.isOK()) {
                                    logger.debug("Smack teach in activated");
                                }
                            }
                        });
            } catch (IOException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Smack packet could not be send: " + e.getMessage());
            }
        }
    }

    public void stopDiscovery() {
        transceiver.stopDiscovery();

        try {
            transceiver.sendBasePacket(ESP3PacketFactory.SA_WR_LEARNMODE(false), null);
            refreshProperties();
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Smack packet could not be send: " + e.getMessage());
        }
    }

    private void refreshProperties() {
        if (getThing().getStatus() == ThingStatus.ONLINE && smackAvailable) {

            logger.debug("request learned smack clients");
            try {
                transceiver.sendBasePacket(ESP3PacketFactory.SA_RD_LEARNEDCLIENTS,
                        new ResponseListenerIgnoringTimeouts<RDLearnedClientsResponse>() {

                            @Override
                            public void responseReceived(RDLearnedClientsResponse response) {

                                logger.debug("received response for learned smack clients");
                                if (response.isValid() && response.isOK()) {
                                    LearnedClient[] clients = response.getLearnedClients();
                                    updateProperty("Learned smart ack clients", Integer.toString(clients.length));
                                    updateProperty("Smart ack clients",
                                            Arrays.stream(clients)
                                                    .map(x -> String.format("%s (MB Idx: %d)",
                                                            HexUtils.bytesToHex(x.clientId), x.mailboxIndex))
                                                    .collect(Collectors.joining(", ")));
                                    smackClients = Arrays.stream(clients).map(x -> HexUtils.bytesToHex(x.clientId))
                                            .collect(Collectors.toSet());
                                }
                            }
                        });
            } catch (IOException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Smack packet could not be send: " + e.getMessage());

            }
        }
    }

    @Override
    public void ErrorOccured(Throwable exception) {
        transceiver.ShutDown();
        transceiver = null;
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, exception.getMessage());
    }

    public boolean sendTeachOuts() {
        return sendTeachOuts;
    }
}
