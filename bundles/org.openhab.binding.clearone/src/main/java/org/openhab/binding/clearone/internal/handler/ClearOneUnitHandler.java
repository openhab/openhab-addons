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
package org.openhab.binding.clearone.internal.handler;

import static org.openhab.binding.clearone.internal.ClearOneBindingConstants.*;

import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.clearone.internal.ClearOneDynamicConfigOptionProvider;
import org.openhab.binding.clearone.internal.ClearOneDynamicStateDescriptionProvider;
import org.openhab.binding.clearone.internal.ClearOneUnitDiscoveryService;
import org.openhab.binding.clearone.internal.Message;
import org.openhab.binding.clearone.internal.config.UnitConfiguration;
import org.openhab.binding.clearone.internal.config.ZoneConfiguration;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.StateOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a class for handling a Unit type Thing.
 *
 * @author Garry Mitchell - Initial Contribution
 */
public class ClearOneUnitHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(ClearOneUnitHandler.class);

    /** Bridge Handler for the Thing. */
    public ClearOneStackHandler bridgeHandler = null;

    private boolean thingHandlerInitialized = false;

    /** Thing count. */
    private int thingCount = 0;

    /** The Discovery Service. */
    private ClearOneUnitDiscoveryService discoveryService = null;

    /** Unit Number. */
    private String deviceId;
    private String typeId;
    private String unitName;
    private String unitUid;

    /** List of Sources */
    ArrayList<StateOption> sources = new ArrayList<StateOption>();
    private ClearOneDynamicStateDescriptionProvider stateDescriptionProvider;
    private ClearOneDynamicConfigOptionProvider configOptionProvider;

    /**
     * Constructor.
     *
     * @param bridge
     * @param stateDescriptionProvider
     * @param configOptionProvider
     */
    public ClearOneUnitHandler(Bridge bridge, ClearOneDynamicStateDescriptionProvider stateDescriptionProvider,
            ClearOneDynamicConfigOptionProvider configOptionProvider) {
        super(bridge);
        this.setStateDescriptionProvider(stateDescriptionProvider);
        this.setConfigOptionProvider(configOptionProvider);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Thing handler - Thing ID: {}.", this.getThing().getUID());

        getBridgeHandler();

        getConfiguration();

        initializeSources();

        updateStatus(ThingStatus.OFFLINE);
    }

    @Override
    public void dispose() {
        logger.debug("Thing {} disposed.", getThing().getUID());

        this.setThingHandlerInitialized(false);

        super.dispose();
    }

    /**
     * Register the Discovery Service.
     *
     * @param discoveryService
     */
    public void registerDiscoveryService(ClearOneUnitDiscoveryService discoveryService) {
        if (discoveryService == null) {
            throw new IllegalArgumentException("registerDiscoveryService(): Illegal Argument. Not allowed to be Null!");
        } else {
            this.discoveryService = discoveryService;
            logger.trace("registerDiscoveryService(): Discovery Service Registered!");
        }
    }

    /**
     * Unregister the Discovery Service.
     */
    public void unregisterDiscoveryService() {
        discoveryService = null;
        logger.trace("unregisterDiscoveryService(): Discovery Service Unregistered!");
    }

    /**
     * Method to Initialize Thing Handler.
     */
    public void initializeThingHandler() {
        if (getBridgeHandler() != null) {
            if (getThing().getStatus().equals(ThingStatus.ONLINE)) {
                Thing thing = getThing();
                List<Channel> channels = thing.getChannels();
                logger.debug("initializeThingHandler(): Initialize Thing Handler - {}", thing.getUID());

                for (Channel channel : channels) {
                    updateChannel(channel.getUID(), 0);
                }

                this.setThingHandlerInitialized(true);
                refreshUnitChannels();

                logger.debug("initializeThingHandler(): Thing Handler Initialized - {}", thing.getUID());
            } else {
                logger.debug("initializeThingHandler(): Thing '{}' Unable To Initialize Thing Handler!: Status - {}",
                        thing.getUID(), thing.getStatus());
            }
        }
    }

    /**
     * Refresh all Unit specific channels direct from the unit
     */
    public void refreshUnitChannels() {
        sendCommand(XAP_CMD_MACRO, "");
        sendCommand(XAP_CMD_PRESET, "");

        int maxInputs = 12;
        char maxProcessing = 'H';
        if (typeId.equals(XAP_UNIT_TYPE_XAP400)) {
            maxInputs = 8;
            maxProcessing = 'D';
        }
        for (int input = 1; input <= maxInputs; input++) {
            sendCommand(XAP_CMD_LABEL, String.format("%d I", input));
        }
        for (char expansion = 'O'; expansion <= 'Z'; expansion++) {
            sendCommand(XAP_CMD_LABEL, String.format("%s E 0", expansion));
        }
        for (char processing = 'A'; processing <= maxProcessing; processing++) {
            sendCommand(XAP_CMD_LABEL, String.format("%s P", processing));
        }
    }

    /**
     * Get the Bridge Handler.
     *
     * @return brdigeHandler
     */
    public synchronized ClearOneStackHandler getBridgeHandler() {
        if (this.bridgeHandler == null) {
            Bridge bridge = getBridge();

            if (bridge == null) {
                logger.debug("getBridgeHandler(): Unable to get bridge!");
                return null;
            }

            logger.debug("getBridgeHandler(): Bridge for '{}' - '{}'", getThing().getUID(), bridge.getUID());

            ThingHandler handler = bridge.getHandler();

            if (handler instanceof ClearOneStackHandler) {
                this.bridgeHandler = (ClearOneStackHandler) handler;
            } else {
                logger.debug("getBridgeHandler(): Unable to get bridge handler!");
            }
        }

        return this.bridgeHandler;
    }

    public void updateChannel(ChannelUID channelUID, int state) {
        logger.debug("updateChannel(): Unit Channel UID: {} {}", channelUID, state);

        if (channelUID != null) {
            switch (channelUID.getId()) {
                case MACRO:
                case PRESET:
                    updateState(channelUID, new DecimalType(state));
                    break;
                default:
                    logger.debug("updateChannel(): Unit Channel not updated - {}.", channelUID);
                    break;
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand(): Command Received - {} {}.", channelUID, command);

        if (command instanceof RefreshType) {
            if (!getThing().getStatus().equals(ThingStatus.ONLINE)) {
                if (getBridgeHandler() != null && bridgeHandler.isConnected()) {
                    updateStatus(ThingStatus.ONLINE);
                    this.initializeThingHandler();
                }
            } else {
                if (bridgeHandler != null && bridgeHandler.isConnected()) {
                    switch (channelUID.getId()) {
                        case MACRO:
                            sendCommand(XAP_CMD_MACRO, "");
                            break;
                        case PRESET:
                            sendCommand(XAP_CMD_PRESET, "");
                            break;
                    }
                }
            }
            return;
        }

        if (bridgeHandler != null && bridgeHandler.isConnected()) {
            String ampCommand = "";
            String param = "";

            switch (channelUID.getId()) {
                case MACRO:
                    ampCommand = XAP_CMD_MACRO;
                    param = String.format("%d", ((DecimalType) command).intValue());
                    break;
                case PRESET:
                    ampCommand = XAP_CMD_PRESET;
                    param = String.format("%d", ((DecimalType) command).intValue());
            }

            if (param != "") {
                sendCommand(ampCommand, param);
                updateChannel(channelUID, ((DecimalType) command).intValue());
            }
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus().equals(ThingStatus.ONLINE)) {
            updateStatus(bridgeStatusInfo.getStatus());
            this.initializeThingHandler();
        } else {
            this.setThingHandlerInitialized(false);
        }

        logger.debug("bridgeStatusChanged(): Bridge Status: '{}' - Thing '{}' Status: '{}'!", bridgeStatusInfo,
                getThing().getUID(), getThing().getStatus());
    }

    /**
     * Get the thing configuration.
     */
    private void getConfiguration() {
        UnitConfiguration unitConfiguration = getConfigAs(UnitConfiguration.class);
        setDeviceId(unitConfiguration.deviceId);
        setTypeId(unitConfiguration.typeId);
    }

    /**
     * Get Device ID.
     *
     * @return deviceId
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Set Device ID.
     *
     * @param typeId
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Get Type ID.
     *
     * @return typeId
     */
    public String getTypeId() {
        return typeId;
    }

    /**
     * Set Type ID.
     *
     * @param typeId
     */
    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    /**
     * Get Unit Name.
     *
     * @return unitName
     */
    public String getUnitName() {
        return unitName;
    }

    /**
     * Set Unit Name.
     *
     * @param unitName
     */
    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    /**
     * Get Unit UID.
     *
     * @return unitUid
     */
    public String getUnitUid() {
        return unitUid;
    }

    /**
     * Set Unit UID.
     *
     * @param unitUid
     */
    public void setUnitUid(String unitUid) {
        this.unitUid = unitUid;
    }

    /**
     * Get Channel by ChannelUID.
     *
     * @param channelUID
     */
    public Channel getChannel(ChannelUID channelUID) {
        Channel channel = null;

        List<Channel> channels = getThing().getChannels();

        for (Channel ch : channels) {
            if (channelUID == ch.getUID()) {
                channel = ch;
                break;
            }
        }

        return channel;
    }

    /**
     * Get Thing Handler refresh status.
     *
     * @return thingRefresh
     */
    public boolean isThingHandlerInitialized() {
        return thingHandlerInitialized;
    }

    /**
     * Set Thing Handler refresh status.
     *
     * @param deviceInitialized
     */
    public void setThingHandlerInitialized(boolean refreshed) {
        this.thingHandlerInitialized = refreshed;
    }

    public void eventReceived(Message message, Thing thing) {
        if (thing != null) {
            if (getThing().equals(thing)) {
                ChannelUID channelUID = null;
                String zone = "";
                String group = "";
                String name = "";

                String[] data = message.data.split("\\s+");

                switch (message.commandId) {
                    case XAP_CMD_UID:
                        return;
                    case XAP_CMD_MACRO:
                        channelUID = new ChannelUID(getThing().getUID(), MACRO);
                        updateChannel(channelUID, Integer.valueOf(data[0]));
                        return;
                    case XAP_CMD_PRESET:
                        channelUID = new ChannelUID(getThing().getUID(), PRESET);
                        updateChannel(channelUID, Integer.valueOf(data[0]));
                        return;
                    case XAP_CMD_MUTE:
                    case XAP_CMD_GAIN:
                        zone = data[0];
                        group = data[1];
                        // Only supports Output Zones
                        if (group.contentEquals("O")) {
                            break;
                        } else {
                            return;
                        }
                    case XAP_CMD_MTRX:
                        zone = data[2];
                        group = data[3];
                        if (group.contentEquals("O")) {
                            break;
                        } else {
                            return;
                        }
                    case XAP_CMD_LABEL:
                        zone = data[0];
                        group = data[1];
                        if (group.contentEquals("O")) {
                            if (data.length >= 3) {
                                name = message.data
                                        .substring(message.data.indexOf(" ", message.data.indexOf(" ") + 1) + 1);
                            }
                            break;
                        } else if (group.contentEquals("I") || group.contentEquals("E") || group.contentEquals("P")) {
                            if (group.contentEquals("I") || group.contentEquals("P")) {
                                if (data.length >= 3) {
                                    name = message.data
                                            .substring(message.data.indexOf(" ", message.data.indexOf(" ") + 1) + 1);
                                }
                                if (!name.contentEquals("")) {
                                    updateSourceName(zone, group, name);
                                }
                            } else if (group.contentEquals("E")) {
                                if (data[2].contentEquals("0")) {
                                    // Expansion bus output - this is a source
                                    if (data.length >= 4) {
                                        name = message.data.substring(message.data.indexOf(" ",
                                                message.data.indexOf(" ", message.data.indexOf(" ") + 1) + 1) + 1);
                                    }
                                    if (!name.contentEquals("")) {
                                        updateSourceName(zone, group, name);
                                    }
                                }
                            }
                            return;
                        } else {
                            return;
                        }
                    default:
                        return;
                }

                Thing zoneThing = findThing(zone);

                logger.debug("eventReceived(): Thing Search - '{}'", zoneThing);

                if (zoneThing != null) {
                    ClearOneZoneHandler thingHandler = (ClearOneZoneHandler) zoneThing.getHandler();

                    if (thingHandler != null) {
                        if (thingHandler.isThingHandlerInitialized()) {
                            thingHandler.eventReceived(message, zoneThing);
                        } else {
                            logger.debug("eventReceived(): Thing '{}' Not Refreshed!", zoneThing.getUID());
                        }
                    }
                } else {
                    logger.debug("eventReceived(): Thing Not Found! Send to Discovery Service!");

                    if (discoveryService != null) {
                        discoveryService.addZone(getThing(), String.valueOf(zone), name);
                    }
                }
            }
        }
    }

    /**
     * Returns Connected.
     */
    public boolean isConnected() {
        if (bridgeHandler != null) {
            return bridgeHandler.isConnected();
        } else {
            return false;
        }
    }

    public boolean sendCommand(String command, String params) {
        if (!isConnected()) {
            return false;
        }

        String data = String.format("#%s%s %s %s\r", getTypeId(), getDeviceId(), command, params);
        // String data = String.format("#** %s %s\r", command, params);
        return bridgeHandler.sendCommand(data);
    }

    /**
     * Check if things have changed.
     */
    public void checkThings() {
        // logger.debug("Checking Things!");

        // allThingsInitialized = true;

        List<Thing> things = getThing().getThings();

        if (things.size() != thingCount) {
            // thingsHaveChanged = true;
            thingCount = things.size();
        }

        for (Thing thing : things) {

            ClearOneZoneHandler handler = (ClearOneZoneHandler) thing.getHandler();

            if (handler != null) {
                // logger.debug("***Checking '{}' - Status: {}, Initialized: {}", thing.getUID(), thing.getStatus(),
                // handler.isThingHandlerInitialized());

                if (!handler.isThingHandlerInitialized() || !thing.getStatus().equals(ThingStatus.ONLINE)) {
                    if (getThing().getStatus().equals(ThingStatus.ONLINE)) {
                        handler.bridgeStatusChanged(getThing().getStatusInfo());
                    }
                }
            } else {
                logger.error("checkThings(): Thing handler not found!");
            }
        }
    }

    /**
     * Find a Thing.
     *
     * @param unitType
     * @param unitId
     * @return thing
     */
    public Thing findThing(String zone) {
        List<Thing> things = getThing().getThings();

        Thing thing = null;

        for (Thing t : things) {
            try {
                Configuration config = t.getConfiguration();
                ClearOneZoneHandler handler = (ClearOneZoneHandler) t.getHandler();

                if (handler != null) {
                    int thingZone = Integer.valueOf((String) config.get(ZoneConfiguration.ZONE_NUMBER));
                    if (thingZone == Integer.valueOf(zone)) {
                        thing = t;
                        logger.debug("findThing(): Thing Found - {}, {}", t, handler);
                        return thing;
                    }
                }
            } catch (Exception e) {
                logger.debug("findThing(): Error Seaching Thing - {} ", e.getMessage(), e);
            }
        }

        return thing;
    }

    private void initializeSources() {
        sources.clear();
        sources.add(new StateOption(XAP_SOURCE_NONE, "None"));
        sources.add(new StateOption("1 I", "Input 1"));
        sources.add(new StateOption("2 I", "Input 2"));
        sources.add(new StateOption("3 I", "Input 3"));
        sources.add(new StateOption("4 I", "Input 4"));
        sources.add(new StateOption("5 I", "Input 5"));
        sources.add(new StateOption("6 I", "Input 6"));
        sources.add(new StateOption("7 I", "Input 7"));
        sources.add(new StateOption("8 I", "Input 8"));
        if (typeId.equals(XAP_UNIT_TYPE_XAP800)) {
            sources.add(new StateOption("9 I", "Input 9"));
            sources.add(new StateOption("10 I", "Input 10"));
            sources.add(new StateOption("11 I", "Input 11"));
            sources.add(new StateOption("12 I", "Input 12"));
        }
        sources.add(new StateOption("O E", "Expansion O"));
        sources.add(new StateOption("P E", "Expansion P"));
        sources.add(new StateOption("Q E", "Expansion Q"));
        sources.add(new StateOption("R E", "Expansion R"));
        sources.add(new StateOption("S E", "Expansion S"));
        sources.add(new StateOption("T E", "Expansion T"));
        sources.add(new StateOption("U E", "Expansion U"));
        sources.add(new StateOption("V E", "Expansion V"));
        sources.add(new StateOption("W E", "Expansion W"));
        sources.add(new StateOption("X E", "Expansion X"));
        sources.add(new StateOption("Y E", "Expansion Y"));
        sources.add(new StateOption("Z E", "Expansion Z"));
        sources.add(new StateOption("A P", "Processing A"));
        sources.add(new StateOption("B P", "Processing B"));
        sources.add(new StateOption("C P", "Processing C"));
        sources.add(new StateOption("D P", "Processing D"));
        if (typeId.equals(XAP_UNIT_TYPE_XAP800)) {
            sources.add(new StateOption("E P", "Processing E"));
            sources.add(new StateOption("F P", "Processing F"));
            sources.add(new StateOption("G P", "Processing G"));
            sources.add(new StateOption("H P", "Processing H"));
        }
    }

    private void updateSourceName(String input, String group, String name) {
        int index = getIndexOfSource(input + " " + group);
        if (index >= 0) {
            sources.set(index, new StateOption(input + " " + group, name));
            updateZoneSourceNames();
        }
    }

    public int getIndexOfSource(String input) {
        for (StateOption source : sources) {
            String value = source.getValue();
            if (value.equals(input))
                return sources.indexOf(source);
        }
        return -1;
    }

    public ClearOneDynamicStateDescriptionProvider getStateDescriptionProvider() {
        return stateDescriptionProvider;
    }

    public void setStateDescriptionProvider(ClearOneDynamicStateDescriptionProvider stateDescriptionProvider) {
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    public void setBridgeHandler(ClearOneStackHandler bridgeHandler) {
        this.bridgeHandler = bridgeHandler;
    }

    public ClearOneDynamicConfigOptionProvider getConfigOptionProvider() {
        return configOptionProvider;
    }

    public void setConfigOptionProvider(ClearOneDynamicConfigOptionProvider configOptionProvider) {
        this.configOptionProvider = configOptionProvider;
    }

    private void updateZoneSourceNames() {
        List<Thing> things = getThing().getThings();
        for (Thing thing : things) {

            ClearOneZoneHandler handler = (ClearOneZoneHandler) thing.getHandler();

            if (handler != null) {
                if (handler.isThingHandlerInitialized() && thing.getStatus().equals(ThingStatus.ONLINE)) {
                    if (getThing().getStatus().equals(ThingStatus.ONLINE)) {
                        handler.updateUnitSourceNames();
                    }
                }
            } else {
                logger.error("updateZoneSourceNames(): Thing handler not found!");
            }
        }
    }
}
