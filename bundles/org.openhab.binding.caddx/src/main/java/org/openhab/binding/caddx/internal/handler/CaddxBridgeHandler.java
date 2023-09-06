/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import static org.openhab.binding.caddx.internal.CaddxBindingConstants.SEND_COMMAND;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TooManyListenersException;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.caddx.internal.CaddxBindingConstants;
import org.openhab.binding.caddx.internal.CaddxCommunicator;
import org.openhab.binding.caddx.internal.CaddxEvent;
import org.openhab.binding.caddx.internal.CaddxMessage;
import org.openhab.binding.caddx.internal.CaddxMessageContext;
import org.openhab.binding.caddx.internal.CaddxMessageType;
import org.openhab.binding.caddx.internal.CaddxPanelListener;
import org.openhab.binding.caddx.internal.CaddxProtocol;
import org.openhab.binding.caddx.internal.CaddxSource;
import org.openhab.binding.caddx.internal.action.CaddxBridgeActions;
import org.openhab.binding.caddx.internal.config.CaddxBridgeConfiguration;
import org.openhab.binding.caddx.internal.config.CaddxKeypadConfiguration;
import org.openhab.binding.caddx.internal.config.CaddxPartitionConfiguration;
import org.openhab.binding.caddx.internal.config.CaddxZoneConfiguration;
import org.openhab.binding.caddx.internal.discovery.CaddxDiscoveryService;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
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

    static final byte[] DISCOVERY_ZONES_SNAPSHOT_REQUEST_00 = { 0x25, 0x00 }; // 1 - 16
    static final byte[] DISCOVERY_ZONES_SNAPSHOT_REQUEST_10 = { 0x25, 0x01 }; // 17 - 32
    static final byte[] DISCOVERY_ZONES_SNAPSHOT_REQUEST_20 = { 0x25, 0x02 }; // 33 - 48
    static final byte[] DISCOVERY_ZONES_SNAPSHOT_REQUEST_30 = { 0x25, 0x03 }; // 49 - 64
    static final byte[] DISCOVERY_ZONES_SNAPSHOT_REQUEST_40 = { 0x25, 0x04 }; // 65 - 80
    static final byte[] DISCOVERY_ZONES_SNAPSHOT_REQUEST_50 = { 0x25, 0x05 }; // 81 - 96
    static final byte[] DISCOVERY_ZONES_SNAPSHOT_REQUEST_60 = { 0x25, 0x06 }; // 97 - 112
    static final byte[] DISCOVERY_ZONES_SNAPSHOT_REQUEST_70 = { 0x25, 0x07 }; // 113 - 64
    static final byte[] DISCOVERY_ZONES_SNAPSHOT_REQUEST_80 = { 0x25, 0x08 }; // 129 - 144
    static final byte[] DISCOVERY_ZONES_SNAPSHOT_REQUEST_90 = { 0x25, 0x09 }; // 145 - 160
    static final byte[] DISCOVERY_ZONES_SNAPSHOT_REQUEST_A0 = { 0x25, 0x0A }; // 161 - 176
    static final byte[] DISCOVERY_ZONES_SNAPSHOT_REQUEST_B0 = { 0x25, 0x0B }; // 177 - 192
    static final byte[] DISCOVERY_PARTITIONS_SNAPSHOT_REQUEST = { 0x27 };

    private final SerialPortManager portManager;
    private @Nullable CaddxDiscoveryService discoveryService = null;
    private CaddxProtocol protocol = CaddxProtocol.Binary;
    private String serialPortName = "";
    private int baudRate;
    private int maxZoneNumber;
    private boolean isIgnoreZoneStatusTransitions;
    private @Nullable CaddxCommunicator communicator = null;

    // Things served by the bridge
    private Map<Integer, Thing> thingZonesMap = new ConcurrentHashMap<>();
    private Map<Integer, Thing> thingPartitionsMap = new ConcurrentHashMap<>();
    private Map<Integer, Thing> thingKeypadsMap = new ConcurrentHashMap<>();
    private @Nullable Thing thingPanel = null;

    public @Nullable CaddxDiscoveryService getDiscoveryService() {
        return discoveryService;
    }

    public void setDiscoveryService(CaddxDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    public CaddxBridgeHandler(SerialPortManager portManager, Bridge bridge) {
        super(bridge);

        this.portManager = portManager;
    }

    @Override
    public void initialize() {
        CaddxBridgeConfiguration configuration = getConfigAs(CaddxBridgeConfiguration.class);

        String portName = configuration.getSerialPort();
        if (portName == null) {
            logger.debug("Serial port is not defined in the configuration");
            return;
        }
        serialPortName = portName;
        protocol = configuration.getProtocol();
        baudRate = configuration.getBaudrate();
        maxZoneNumber = configuration.getMaxZoneNumber();
        isIgnoreZoneStatusTransitions = configuration.isIgnoreZoneStatusTransitions();
        updateStatus(ThingStatus.OFFLINE);

        // create & start panel interface
        logger.debug("Starting interface at port {} with baudrate {} and protocol {}", serialPortName, baudRate,
                protocol);

        try {
            communicator = new CaddxCommunicator(getThing().getUID().getAsString(), portManager, protocol,
                    serialPortName, baudRate);
        } catch (IOException | TooManyListenersException | UnsupportedCommOperationException | PortInUseException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Communication cannot be initialized. " + e.toString());

            return;
        }

        CaddxCommunicator comm = communicator;
        if (comm != null) {
            comm.addListener(this);

            // Send discovery commands for the zones
            comm.transmit(new CaddxMessage(CaddxMessageContext.DISCOVERY, DISCOVERY_ZONES_SNAPSHOT_REQUEST_00, false));
            comm.transmit(new CaddxMessage(CaddxMessageContext.DISCOVERY, DISCOVERY_ZONES_SNAPSHOT_REQUEST_10, false));
            comm.transmit(new CaddxMessage(CaddxMessageContext.DISCOVERY, DISCOVERY_ZONES_SNAPSHOT_REQUEST_20, false));
            comm.transmit(new CaddxMessage(CaddxMessageContext.DISCOVERY, DISCOVERY_ZONES_SNAPSHOT_REQUEST_30, false));
            comm.transmit(new CaddxMessage(CaddxMessageContext.DISCOVERY, DISCOVERY_ZONES_SNAPSHOT_REQUEST_40, false));
            comm.transmit(new CaddxMessage(CaddxMessageContext.DISCOVERY, DISCOVERY_ZONES_SNAPSHOT_REQUEST_50, false));
            comm.transmit(new CaddxMessage(CaddxMessageContext.DISCOVERY, DISCOVERY_ZONES_SNAPSHOT_REQUEST_60, false));
            comm.transmit(new CaddxMessage(CaddxMessageContext.DISCOVERY, DISCOVERY_ZONES_SNAPSHOT_REQUEST_70, false));
            comm.transmit(new CaddxMessage(CaddxMessageContext.DISCOVERY, DISCOVERY_ZONES_SNAPSHOT_REQUEST_80, false));
            comm.transmit(new CaddxMessage(CaddxMessageContext.DISCOVERY, DISCOVERY_ZONES_SNAPSHOT_REQUEST_90, false));
            comm.transmit(new CaddxMessage(CaddxMessageContext.DISCOVERY, DISCOVERY_ZONES_SNAPSHOT_REQUEST_A0, false));
            comm.transmit(new CaddxMessage(CaddxMessageContext.DISCOVERY, DISCOVERY_ZONES_SNAPSHOT_REQUEST_B0, false));

            // Send discovery commands for the partitions
            comm.transmit(
                    new CaddxMessage(CaddxMessageContext.DISCOVERY, DISCOVERY_PARTITIONS_SNAPSHOT_REQUEST, false));

            // Send status commands to the zones and partitions
            thingZonesMap.forEach((k, v) -> sendCommand(CaddxMessageContext.COMMAND,
                    CaddxBindingConstants.ZONE_STATUS_REQUEST, String.valueOf(k - 1)));
            thingPartitionsMap.forEach((k, v) -> sendCommand(CaddxMessageContext.COMMAND,
                    CaddxBindingConstants.PARTITION_STATUS_REQUEST, String.valueOf(k - 1)));
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
        switch (caddxThingType) {
            case PARTITION:
                if (partition != null) {
                    return thingPartitionsMap.get(Integer.valueOf(partition));
                }
            case ZONE:
                if (zone != null) {
                    return thingZonesMap.get(Integer.valueOf(zone));
                }
            case KEYPAD:
                if (keypad != null) {
                    return thingKeypadsMap.get(Integer.valueOf(keypad));
                }
            case PANEL:
                return thingPanel;
        }
        return null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("handleCommand(), channelUID: {}, command: {}", channelUID, command);

        switch (channelUID.getId()) {
            case SEND_COMMAND:
                if (!command.toString().isEmpty()) {
                    String[] tokens = command.toString().split("\\|");

                    String cmd = tokens[0];
                    String data = "";
                    if (tokens.length > 1) {
                        data = tokens[1];
                    }

                    sendCommand(CaddxMessageContext.COMMAND, cmd, data);

                    updateState(channelUID, new StringType(""));
                }
                break;
            default:
                logger.debug("Unknown command {}", command);
                break;
        }
    }

    /**
     * Sends a command to the panel
     *
     * @param command The command to be send
     * @param data The associated command data
     */
    public boolean sendCommand(CaddxMessageContext context, String command, String data) {
        logger.trace("sendCommand(): Attempting to send Command: command - {} - data: {}", command, data);

        CaddxMessage msg = null;

        switch (command) {
            case CaddxBindingConstants.ZONE_BYPASSED:
                msg = new CaddxMessage(context, CaddxMessageType.ZONE_BYPASS_TOGGLE, data);
                break;
            case CaddxBindingConstants.ZONE_STATUS_REQUEST:
                msg = new CaddxMessage(context, CaddxMessageType.ZONE_STATUS_REQUEST, data);
                break;
            case CaddxBindingConstants.ZONE_NAME_REQUEST:
                msg = new CaddxMessage(context, CaddxMessageType.ZONE_NAME_REQUEST, data);
                break;
            case CaddxBindingConstants.PARTITION_STATUS_REQUEST:
                msg = new CaddxMessage(context, CaddxMessageType.PARTITION_STATUS_REQUEST, data);
                break;
            case CaddxBindingConstants.PARTITION_PRIMARY_COMMAND_WITH_PIN:
                msg = new CaddxMessage(context, CaddxMessageType.PRIMARY_KEYPAD_FUNCTION_WITH_PIN, data);
                break;
            case CaddxBindingConstants.PARTITION_SECONDARY_COMMAND:
                msg = new CaddxMessage(context, CaddxMessageType.SECONDARY_KEYPAD_FUNCTION, data);
                break;
            case CaddxBindingConstants.PANEL_SYSTEM_STATUS_REQUEST:
                msg = new CaddxMessage(context, CaddxMessageType.SYSTEM_STATUS_REQUEST, data);
                break;
            case CaddxBindingConstants.PANEL_INTERFACE_CONFIGURATION_REQUEST:
                msg = new CaddxMessage(context, CaddxMessageType.INTERFACE_CONFIGURATION_REQUEST, data);
                break;
            case CaddxBindingConstants.PANEL_LOG_EVENT_REQUEST:
                msg = new CaddxMessage(context, CaddxMessageType.LOG_EVENT_REQUEST, data);
                break;
            case CaddxBindingConstants.KEYPAD_TERMINAL_MODE_REQUEST:
                msg = new CaddxMessage(context, CaddxMessageType.KEYPAD_TERMINAL_MODE_REQUEST, data);
                break;
            case CaddxBindingConstants.KEYPAD_SEND_KEYPAD_TEXT_MESSAGE:
                msg = new CaddxMessage(context, CaddxMessageType.SEND_KEYPAD_TEXT_MESSAGE, data);
                break;
            default:
                logger.debug("Unknown command {}", command);
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
    public void registerDiscoveryService(CaddxDiscoveryService discoveryService) {
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
    public void caddxMessage(CaddxMessage caddxMessage) {
        CaddxSource source = caddxMessage.getSource();

        if (source != CaddxSource.NONE) {
            CaddxThingType caddxThingType = null;
            Integer partition = null;
            Integer zone = null;
            Integer keypad = null;

            switch (source) {
                case PANEL:
                    caddxThingType = CaddxThingType.PANEL;
                    break;
                case PARTITION:
                    caddxThingType = CaddxThingType.PARTITION;
                    partition = Integer.parseInt(caddxMessage.getPropertyById("partition_number")) + 1;
                    break;
                case ZONE:
                    caddxThingType = CaddxThingType.ZONE;
                    zone = Integer.parseInt(caddxMessage.getPropertyById("zone_number")) + 1;
                    break;
                case KEYPAD:
                    caddxThingType = CaddxThingType.KEYPAD;
                    keypad = Integer.parseInt(caddxMessage.getPropertyById("keypad_address"));
                    break;
                default:
                    logger.debug("Source has illegal value");
                    return;
            }

            CaddxEvent event = new CaddxEvent(caddxMessage, partition, zone, keypad);

            // Ignore Zone Status messages according to the configuration
            if (isIgnoreZoneStatusTransitions
                    && caddxMessage.getCaddxMessageType() == CaddxMessageType.ZONE_STATUS_MESSAGE
                    && caddxMessage.getContext() == CaddxMessageContext.NONE) {
                logger.debug("Zone {} Transition ignored.", zone);
                return;
            }

            // Find the thing
            Thing thing = findThing(caddxThingType, partition, zone, keypad);
            CaddxDiscoveryService discoveryService = getDiscoveryService();
            if (thing != null) {
                CaddxBaseThingHandler thingHandler = (CaddxBaseThingHandler) thing.getHandler();
                if (thingHandler != null) {
                    thingHandler.caddxEventReceived(event, thing);
                }
            } else {
                if (discoveryService != null) {
                    discoveryService.addThing(getThing(), caddxThingType, event);
                }
            }

            // Handle specific messages that add multiple discovered things
            if (discoveryService != null) {
                switch (caddxMessage.getCaddxMessageType()) {
                    case PARTITIONS_SNAPSHOT_MESSAGE:
                        for (int i = 1; i <= 8; i++) {
                            if (caddxMessage.getPropertyById("partition_" + i + "_valid").equals("true")) {
                                thing = findThing(CaddxThingType.PARTITION, i, null, null);
                                if (thing != null) {
                                    continue;
                                }

                                event = new CaddxEvent(caddxMessage, i, null, null);
                                discoveryService.addThing(getThing(), CaddxThingType.PARTITION, event);
                            }
                        }
                        break;
                    case ZONES_SNAPSHOT_MESSAGE:
                        int zoneOffset = Integer.parseInt(caddxMessage.getPropertyById("zone_offset")) * 16;
                        for (int i = 1; i <= 16; i++) {
                            if (zoneOffset + i <= maxZoneNumber) {
                                thing = findThing(CaddxThingType.ZONE, null, zoneOffset + i, null);
                                if (thing != null) {
                                    continue;
                                }

                                event = new CaddxEvent(caddxMessage, null, zoneOffset + i, null);
                                discoveryService.addThing(getThing(), CaddxThingType.ZONE, event);
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

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof ThingHandlerPartition) {
            int id = ((BigDecimal) childThing.getConfiguration().get(CaddxPartitionConfiguration.PARTITION_NUMBER))
                    .intValue();
            thingPartitionsMap.put(id, childThing);
        } else if (childHandler instanceof ThingHandlerZone) {
            int id = ((BigDecimal) childThing.getConfiguration().get(CaddxZoneConfiguration.ZONE_NUMBER)).intValue();
            thingZonesMap.put(id, childThing);
        } else if (childHandler instanceof ThingHandlerKeypad) {
            int id = ((BigDecimal) childThing.getConfiguration().get(CaddxKeypadConfiguration.KEYPAD_ADDRESS))
                    .intValue();
            thingKeypadsMap.put(id, childThing);
        } else if (childHandler instanceof ThingHandlerPanel) {
            thingPanel = childThing;
        }

        super.childHandlerInitialized(childHandler, childThing);
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof ThingHandlerPartition) {
            int id = ((BigDecimal) childThing.getConfiguration().get(CaddxPartitionConfiguration.PARTITION_NUMBER))
                    .intValue();
            thingPartitionsMap.remove(id);
        } else if (childHandler instanceof ThingHandlerZone) {
            int id = ((BigDecimal) childThing.getConfiguration().get(CaddxZoneConfiguration.ZONE_NUMBER)).intValue();
            thingZonesMap.remove(id);
        } else if (childHandler instanceof ThingHandlerKeypad) {
            int id = ((BigDecimal) childThing.getConfiguration().get(CaddxKeypadConfiguration.KEYPAD_ADDRESS))
                    .intValue();
            thingKeypadsMap.remove(id);
        } else if (childHandler instanceof ThingHandlerPanel) {
            thingPanel = null;
        }

        super.childHandlerDisposed(childHandler, childThing);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        Set<Class<? extends ThingHandlerService>> set = new HashSet<Class<? extends ThingHandlerService>>(2);
        set.add(CaddxDiscoveryService.class);
        set.add(CaddxBridgeActions.class);
        return set;
    }

    public void restart() {
        // Stop the currently running communicator
        CaddxCommunicator comm = communicator;
        if (comm != null) {
            comm.stop();
            comm = null;
        }

        // Initialize again
        initialize();
    }
}
