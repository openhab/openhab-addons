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

import static org.openhab.binding.caddx.internal.CaddxBindingConstants.SEND_COMMAND;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TooManyListenersException;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
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
    static final byte[] DISCOVERY_PARTITIONS_SNAPSHOT_REQUEST = { 0x27 };

    private final SerialPortManager portManager;
    private @Nullable CaddxDiscoveryService discoveryService = null;
    private CaddxProtocol protocol = CaddxProtocol.Binary;
    private String serialPortName = "";
    private int baudRate;
    private @Nullable CaddxCommunicator communicator = null;

    // Things served by the bridge
    private Map<BigDecimal, Thing> thingZonesMap = new ConcurrentHashMap<>();
    private Map<BigDecimal, Thing> thingPartitionsMap = new ConcurrentHashMap<>();
    private Map<BigDecimal, Thing> thingKeypadsMap = new ConcurrentHashMap<>();
    private @Nullable Thing thingPanel = null;

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
        updateStatus(ThingStatus.OFFLINE);

        // create & start panel interface
        logger.debug("Starting interface at port {} with baudrate {} and protocol {}", serialPortName, baudRate,
                protocol);

        try {
            communicator = new CaddxCommunicator(portManager, protocol, serialPortName, baudRate);
        } catch (IOException | TooManyListenersException | UnsupportedCommOperationException | PortInUseException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Communication cannot be initialized. " + e.toString());

            return;
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
                    return thingPartitionsMap.get(BigDecimal.valueOf(partition));
                }
            case ZONE:
                if (zone != null) {
                    return thingZonesMap.get(BigDecimal.valueOf(zone));
                }
            case KEYPAD:
                if (keypad != null) {
                    return thingKeypadsMap.get(BigDecimal.valueOf(keypad));
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

                    sendCommand(cmd, data);

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
    public boolean sendCommand(String command, String data) {
        logger.trace("sendCommand(): Attempting to send Command: command - {} - data: {}", command, data);

        CaddxMessage msg = null;

        switch (command) {
            case CaddxBindingConstants.ZONE_BYPASSED:
                msg = new CaddxMessage(CaddxMessageType.ZONE_BYPASS_TOGGLE, data);
                break;
            case CaddxBindingConstants.ZONE_STATUS_REQUEST:
                msg = new CaddxMessage(CaddxMessageType.ZONE_STATUS_REQUEST, data);
                break;
            case CaddxBindingConstants.ZONE_NAME_REQUEST:
                msg = new CaddxMessage(CaddxMessageType.ZONE_NAME_REQUEST, data);
                break;
            case CaddxBindingConstants.PARTITION_STATUS_REQUEST:
                msg = new CaddxMessage(CaddxMessageType.PARTITION_STATUS_REQUEST, data);
                break;
            case CaddxBindingConstants.PARTITION_PRIMARY_COMMAND_WITH_PIN:
                msg = new CaddxMessage(CaddxMessageType.PRIMARY_KEYPAD_FUNCTION_WITH_PIN, data);
                break;
            case CaddxBindingConstants.PARTITION_SECONDARY_COMMAND:
                msg = new CaddxMessage(CaddxMessageType.SECONDARY_KEYPAD_FUNCTION, data);
                break;
            case CaddxBindingConstants.PANEL_SYSTEM_STATUS_REQUEST:
                msg = new CaddxMessage(CaddxMessageType.SYSTEM_STATUS_REQUEST, data);
                break;
            case CaddxBindingConstants.PANEL_INTERFACE_CONFIGURATION_REQUEST:
                msg = new CaddxMessage(CaddxMessageType.INTERFACE_CONFIGURATION_REQUEST, data);
                break;
            case CaddxBindingConstants.PANEL_LOG_EVENT_REQUEST:
                msg = new CaddxMessage(CaddxMessageType.LOG_EVENT_REQUEST, data);
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
    public void caddxMessage(CaddxCommunicator communicator, CaddxMessage caddxMessage) {
        CaddxSource source = caddxMessage.getSource();

        if (source != CaddxSource.NONE) {
            CaddxThingType caddxThingType = null;
            @Nullable
            Integer partition = null;
            @Nullable
            Integer zone = null;
            @Nullable
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
                        int zoneOffset = Integer.parseInt(caddxMessage.getPropertyById("zone_offset"));
                        for (int i = 1; i <= 16; i++) {
                            if (caddxMessage.getPropertyById("zone_" + i + "_trouble").equals("false")) {
                                thing = findThing(CaddxThingType.ZONE, null, zoneOffset + i, null);
                                if (thing != null) {
                                    continue;
                                }

                                event = new CaddxEvent(caddxMessage, null, zoneOffset + i, null);
                                discoveryService.addThing(getThing(), CaddxThingType.ZONE, event);
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

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(CaddxDiscoveryService.class);
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof ThingHandlerPartition) {
            BigDecimal id = (BigDecimal) childThing.getConfiguration()
                    .get(CaddxPartitionConfiguration.PARTITION_NUMBER);
            thingPartitionsMap.put(id, childThing);
        } else if (childHandler instanceof ThingHandlerZone) {
            BigDecimal id = (BigDecimal) childThing.getConfiguration().get(CaddxZoneConfiguration.ZONE_NUMBER);
            thingZonesMap.put(id, childThing);
        } else if (childHandler instanceof ThingHandlerKeypad) {
            BigDecimal id = (BigDecimal) childThing.getConfiguration().get(CaddxKeypadConfiguration.KEYPAD_ADDRESS);
            thingKeypadsMap.put(id, childThing);
        } else if (childHandler instanceof ThingHandlerPanel) {
            thingPanel = childThing;
        }

        super.childHandlerInitialized(childHandler, childThing);
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof ThingHandlerPartition) {
            BigDecimal id = (BigDecimal) childThing.getConfiguration()
                    .get(CaddxPartitionConfiguration.PARTITION_NUMBER);
            thingPartitionsMap.remove(id);
        } else if (childHandler instanceof ThingHandlerZone) {
            BigDecimal id = (BigDecimal) childThing.getConfiguration().get(CaddxZoneConfiguration.ZONE_NUMBER);
            thingZonesMap.remove(id);
        } else if (childHandler instanceof ThingHandlerKeypad) {
            BigDecimal id = (BigDecimal) childThing.getConfiguration().get(CaddxKeypadConfiguration.KEYPAD_ADDRESS);
            thingKeypadsMap.remove(id);
        } else if (childHandler instanceof ThingHandlerPanel) {
            thingPanel = null;
        }

        super.childHandlerDisposed(childHandler, childThing);
    }
}
