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
package org.openhab.binding.caddx.internal.handler;

import static org.openhab.binding.caddx.internal.CaddxBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.TooManyListenersException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.io.transport.serial.PortInUseException;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.eclipse.smarthome.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.binding.caddx.internal.CaddxBindingConstants;
import org.openhab.binding.caddx.internal.CaddxCommunicator;
import org.openhab.binding.caddx.internal.CaddxEvent;
import org.openhab.binding.caddx.internal.CaddxMessage;
import org.openhab.binding.caddx.internal.CaddxMessageType;
import org.openhab.binding.caddx.internal.CaddxPanelListener;
import org.openhab.binding.caddx.internal.CaddxProtocol;
import org.openhab.binding.caddx.internal.CaddxSource;
import org.openhab.binding.caddx.internal.config.CaddxBridgeConfiguration;
import org.openhab.binding.caddx.internal.config.CaddxKeypadConfiguration;
import org.openhab.binding.caddx.internal.config.CaddxPartitionConfiguration;
import org.openhab.binding.caddx.internal.config.CaddxZoneConfiguration;
import org.openhab.binding.caddx.internal.discovery.CaddxDiscoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The bridge handler for the Caddx RS232 Serial interface.
 *
 * @author Georgios Moutsos - Initial contribution
 */
@NonNullByDefault
public class CaddxBridgeHandler extends BaseBridgeHandler implements CaddxPanelListener {
    private final Logger logger = LoggerFactory.getLogger(CaddxBridgeHandler.class);

    static final byte[] DISCOVERY_PARTITION_STATUS_REQUEST_0 = { 0x26, 0x00 };
    static final byte[] DISCOVERY_ZONES_SNAPSHOT_REQUEST_00 = { 0x25, 0x00 };
    static final byte[] DISCOVERY_ZONES_SNAPSHOT_REQUEST_10 = { 0x25, 0x10 };
    static final byte[] DISCOVERY_ZONES_SNAPSHOT_REQUEST_20 = { 0x25, 0x20 };
    static final byte[] DISCOVERY_ZONES_SNAPSHOT_REQUEST_30 = { 0x25, 0x30 };
    static final byte[] DISCOVERY_PARTITIONS_SNAPSHOT_REQUEST = { 0x27 };

    private final SerialPortManager portManager;
    private @Nullable CaddxDiscoveryService discoveryService = null;
    private CaddxProtocol protocol = CaddxProtocol.Binary;
    private String serialPortName = "";
    private int baudRate;
    private @Nullable CaddxCommunicator communicator = null;

    public @Nullable CaddxDiscoveryService getDiscoveryService() {
        return discoveryService;
    }

    public void setDiscoveryService(CaddxDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    /**
     * Constructor.
     *
     * @param bridge
     */
    public CaddxBridgeHandler(SerialPortManager portManager, Bridge bridge) {
        super(bridge);

        this.portManager = portManager;
    }

    private void init() {
        CaddxBridgeConfiguration configuration = getConfigAs(CaddxBridgeConfiguration.class);

        protocol = configuration.getProtocol();
        serialPortName = configuration.getSerialPort();
        baudRate = configuration.getBaudrate().intValue();
        updateStatus(ThingStatus.OFFLINE);

        // create & start panel interface
        logger.info("starting interface at port {} with baudrate {}", serialPortName, baudRate);

        try {
            communicator = new CaddxCommunicator(portManager, protocol, serialPortName, baudRate);
        } catch (IOException | TooManyListenersException | UnsupportedCommOperationException | PortInUseException e) {
            logger.warn("Cannot initialize Communication.", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Communication cannot be initialized");

            throw new IllegalArgumentException();
        }

        CaddxCommunicator comm = communicator;
        if (comm != null) {
            comm.addListener(this);

            // Send discovery commands for the things
            comm.transmit(new CaddxMessage(DISCOVERY_ZONES_SNAPSHOT_REQUEST_00, false));
            comm.transmit(new CaddxMessage(DISCOVERY_ZONES_SNAPSHOT_REQUEST_10, false));
            comm.transmit(new CaddxMessage(DISCOVERY_ZONES_SNAPSHOT_REQUEST_20, false));
            comm.transmit(new CaddxMessage(DISCOVERY_PARTITION_STATUS_REQUEST_0, false));
            comm.transmit(new CaddxMessage(DISCOVERY_PARTITIONS_SNAPSHOT_REQUEST, false));
        }
        // list all channels
        if (logger.isTraceEnabled()) {
            logger.trace("list all {} channels:", getThing().getChannels().size());
            for (Channel c : getThing().getChannels()) {
                logger.trace("Channel Type {} UID {}", c.getChannelTypeUID(), c.getUID());
            }
        }
    }

    @Override
    public void initialize() {
        logger.trace("Initializing the Bridge handler.");

        init();
    }

    @Override
    public void dispose() {
        CaddxCommunicator comm = communicator;
        if (comm != null) {
            comm.stop();
            comm = null;
        }

        if (discoveryService != null) {
            unregisterDiscoveryService();
        }

        super.dispose();
    }

    public @Nullable Thing findThing(CaddxThingType caddxThingType, @Nullable Integer partition, @Nullable Integer zone,
            @Nullable Integer keypad) {
        List<Thing> things = getThing().getThings();

        Thing thing = null;

        for (Thing t : things) {
            try {
                Configuration config = t.getConfiguration();
                CaddxBaseThingHandler handler = (CaddxBaseThingHandler) t.getHandler();

                if (handler != null) {
                    CaddxThingType handlerCaddxThingType = handler.getCaddxThingType();

                    if (handlerCaddxThingType.equals(caddxThingType)) {
                        switch (handlerCaddxThingType) {
                            case PANEL:
                                thing = t;
                                return thing;
                            case KEYPAD:
                                BigDecimal keypadAddress = (BigDecimal) config
                                        .get(CaddxKeypadConfiguration.KEYPAD_ADDRESS);
                                if (keypad == keypadAddress.intValue()) {
                                    thing = t;
                                    return thing;
                                }
                                break;
                            case PARTITION:
                                BigDecimal partitionNumber = (BigDecimal) config
                                        .get(CaddxPartitionConfiguration.PARTITION_NUMBER);
                                if (partition == partitionNumber.intValue()) {
                                    thing = t;
                                    return thing;
                                }
                                break;
                            case ZONE:
                                BigDecimal zoneNumber = (BigDecimal) config.get(CaddxZoneConfiguration.ZONE_NUMBER);
                                if (zone == zoneNumber.intValue()) {
                                    thing = t;
                                    return thing;
                                }
                                break;
                            default:
                                break;
                        }
                    }

                }
            } catch (Exception e) {
                logger.warn("findThing(): Error Searching Thing - {} ", e.getMessage(), e);
            }
        }

        return thing;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("handleCommand(), channelUID: {}, command: {}", channelUID, command);

        switch (channelUID.getId()) {
            case BRIDGE_RESET:
                if (command == OnOffType.ON) {
                    CaddxCommunicator n = communicator;
                    if (n != null) {
                        n.stop();
                        n = null;
                        updateStatus(ThingStatus.OFFLINE);
                    }
                } else if (command == OnOffType.OFF) {
                    init();
                    updateStatus(ThingStatus.ONLINE);
                }
                break;
            case SEND_COMMAND:
                if (!command.toString().isEmpty()) {
                    String[] tokens = command.toString().split(",");

                    String cmd = tokens[0];
                    String data = "";
                    if (tokens.length > 1) {
                        data = tokens[1];
                    }

                    sendCommand(cmd, data);

                    updateState(channelUID, new StringType(""));
                }
                break;
            default:
                break;
        }
    }

    /**
     * Sends a command to the panel
     *
     * @param command The command to be send
     * @param data The associated command data
     */
    public boolean sendCommand(String command, String data) {
        logger.trace("sendCommand(): Attempting to send Command: command - {} - data: {}", command, data);

        CaddxMessage msg = null;

        if (CaddxBindingConstants.ZONE_BYPASS_TOGGLE.equals(command)) {
            msg = new CaddxMessage(CaddxMessageType.Zone_Bypass_Toggle, data);
        } else if (CaddxBindingConstants.ZONE_STATUS_REQUEST.equals(command)) {
            msg = new CaddxMessage(CaddxMessageType.Zone_Status_Request, data);
        } else if (CaddxBindingConstants.ZONE_NAME_REQUEST.equals(command)) {
            msg = new CaddxMessage(CaddxMessageType.Zone_Name_Request, data);
        } else if (CaddxBindingConstants.PARTITION_STATUS_REQUEST.equals(command)) {
            msg = new CaddxMessage(CaddxMessageType.Partition_Status_Request, data);
        } else if (CaddxBindingConstants.PARTITION_PRIMARY_COMMAND.equals(command)) {
            msg = new CaddxMessage(CaddxMessageType.Primary_Keypad_Function_without_PIN, data);
        } else if (CaddxBindingConstants.PARTITION_SECONDARY_COMMAND.equals(command)) {
            msg = new CaddxMessage(CaddxMessageType.Secondary_Keypad_Function, data);
        } else if (CaddxBindingConstants.PANEL_SYSTEM_STATUS_REQUEST.equals(command)) {
            msg = new CaddxMessage(CaddxMessageType.System_Status_Request, data);
        } else if (CaddxBindingConstants.PANEL_INTERFACE_CONFIGURATION_REQUEST.equals(command)) {
            msg = new CaddxMessage(CaddxMessageType.Interface_Configuration_Request, data);
        } else if (CaddxBindingConstants.PANEL_LOG_EVENT_REQUEST.equals(command)) {
            msg = new CaddxMessage(CaddxMessageType.Log_Event_Request, data);
        } else {
            return false;
        }

        CaddxCommunicator comm = communicator;
        if (comm != null) {
            comm.transmit(msg);
        }

        return true;
    }

    /**
     * Register the Discovery Service.
     *
     * @param discoveryService
     */
    public void registerDiscoveryService(@Nullable CaddxDiscoveryService discoveryService) {
        if (discoveryService == null) {
            throw new IllegalArgumentException("registerDiscoveryService(): Illegal Argument. Not allowed to be Null!");
        }

        this.discoveryService = discoveryService;

        logger.trace("registerDiscoveryService(): Discovery Service Registered!");
    }

    /**
     * Unregister the Discovery Service.
     */
    public void unregisterDiscoveryService() {
        logger.trace("unregisterDiscoveryService(): Discovery Service Unregistered!");
        discoveryService = null;
    }

    @Override
    public void caddxMessage(CaddxCommunicator communicator, CaddxMessage caddxMessage) {
        CaddxSource source = caddxMessage.getSource();

        if (source != CaddxSource.None) {
            CaddxThingType caddxThingType = null;
            @Nullable
            Integer partition = null;
            @Nullable
            Integer zone = null;
            @Nullable
            Integer keypad = null;

            switch (source) {
                case Panel:
                    caddxThingType = CaddxThingType.PANEL;
                    break;
                case Partition:
                    caddxThingType = CaddxThingType.PARTITION;
                    partition = Integer.parseInt(caddxMessage.getPropertyById("partition_number")) + 1;
                    break;
                case Zone:
                    caddxThingType = CaddxThingType.ZONE;
                    zone = Integer.parseInt(caddxMessage.getPropertyById("zone_number")) + 1;
                    break;
                case Keypad:
                    caddxThingType = CaddxThingType.KEYPAD;
                    keypad = Integer.parseInt(caddxMessage.getPropertyById("keypad_address"));
                    break;
                default:
                    logger.debug("Source has illegal value");
                    return;
            }

            CaddxEvent event = new CaddxEvent(caddxMessage, partition, zone, keypad);

            // Find the thing
            Thing thing = findThing(caddxThingType, partition, zone, keypad);
            CaddxDiscoveryService discoverService = getDiscoveryService();
            if (thing != null) {
                CaddxBaseThingHandler thingHandler = (CaddxBaseThingHandler) thing.getHandler();
                if (thingHandler != null) {
                    thingHandler.caddxEventReceived(event, thing);
                }
            } else {
                if (discoverService != null) {
                    discoverService.addThing(getThing(), caddxThingType, event);
                }
            }

            // Handle specific messages that add multiple discovered things
            if (discoverService != null) {
                switch (caddxMessage.getCaddxMessageType()) {
                    case Partitions_Snapshot_Message:
                        for (int i = 1; i <= 8; i++) {
                            if (caddxMessage.getPropertyById("partition_" + Integer.toString(i) + "_valid") == "true") {
                                thing = findThing(CaddxThingType.PARTITION, i, null, null);
                                if (thing != null) {
                                    continue;
                                }

                                event = new CaddxEvent(caddxMessage, i, null, null);
                                discoverService.addThing(getThing(), CaddxThingType.PARTITION, event);
                            }
                        }
                        break;
                    case Zones_Snapshot_Message:
                        int zoneOffset = Integer.parseInt(caddxMessage.getPropertyById("zone_offset"));
                        for (int i = 1; i <= 16; i++) {
                            if (caddxMessage.getPropertyById("zone_" + Integer.toString(i) + "_trouble")
                                    .equals("false")) {
                                thing = findThing(CaddxThingType.ZONE, null, zoneOffset + i, null);
                                if (thing != null) {
                                    continue;
                                }

                                event = new CaddxEvent(caddxMessage, null, zoneOffset + i, null);
                                discoverService.addThing(getThing(), CaddxThingType.ZONE, event);
                            } else {
                                logger.debug("troubled zone: {}", zoneOffset + i);
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        updateStatus(ThingStatus.ONLINE);
    }
}
