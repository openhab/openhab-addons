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
package org.openhab.binding.asuswrt.internal.things;

import static org.openhab.binding.asuswrt.internal.constants.AsuswrtBindingConstants.*;
import static org.openhab.binding.asuswrt.internal.helpers.AsuswrtUtils.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.asuswrt.internal.structures.AsuswrtIpInfo;
import org.openhab.binding.asuswrt.internal.structures.AsuswrtTraffic;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CLASS HANDLING ROUTER CLIENTS
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class AsuswrtInterface extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(AsuswrtInterface.class);
    private final AsuswrtRouter router;
    private String ifName = "";
    private Map<String, Object> oldStates = new HashMap<>();
    protected final String uid;

    /**
     * Constructor
     *
     * @param thing Thing object representing client
     * @param router Router (Bridge) Thing
     */
    public AsuswrtInterface(Thing thing, AsuswrtRouter router) {
        super(thing);
        this.router = router;
        this.uid = getThing().getUID().getAsString();
    }

    /***********************************
     *
     * INIT AND SETTINGS
     *
     ************************************/

    /**
     * INITIALIZE DEVICE
     */
    @Override
    public void initialize() {
        logger.trace("({}) Initializing thing ", uid);
        Configuration config = getThing().getConfiguration();
        if (config.containsKey(PROPERTY_INTERFACE_NAME)) {
            this.ifName = config.get(PROPERTY_INTERFACE_NAME).toString();
            updateProperty(NETWORK_REPRASENTATION_PROPERTY, ifName);
            updateChannels();
            updateStatus(ThingStatus.ONLINE);
        } else {
            logger.debug("({}) configurtation error", uid);
        }
    }

    /***********************************
     *
     * COMMAND AND EVENTS
     *
     ************************************/
    /**
     * handle Commands
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateChannels();
        }
    }

    public void updateChannels() {
        try {
            AsuswrtIpInfo interfaceInfo = router.getInterfaces().getByName(ifName);
            fireEvents(interfaceInfo);
            updateInterfaceChannels(interfaceInfo);
            updateTrafficChannels(interfaceInfo.geTraffic());
        } catch (Exception e) {
            logger.debug("({}) unable to refresh data - property interfaceName not found ", uid);
        }
    }

    /**
     * Update Network Channels
     * 
     * @param deviceInfo
     */
    private void updateInterfaceChannels(AsuswrtIpInfo interfaceInfo) {
        updateState(getChannelID(CHANNEL_GROUP_NETWORK, CHANNEL_NETWORK_MAC), getStringType(interfaceInfo.getMAC()));
        updateState(getChannelID(CHANNEL_GROUP_NETWORK, CHANNEL_NETWORK_IP),
                getStringType(interfaceInfo.getIpAddress()));
        updateState(getChannelID(CHANNEL_GROUP_NETWORK, CHANNEL_NETWORK_MASK),
                getStringType(interfaceInfo.getSubnet()));
        updateState(getChannelID(CHANNEL_GROUP_NETWORK, CHANNEL_NETWORK_GATEWAY),
                getStringType(interfaceInfo.getGateway()));
        updateState(getChannelID(CHANNEL_GROUP_NETWORK, CHANNEL_NETWORK_METHOD),
                getStringType(interfaceInfo.getIpProto()));
        updateState(getChannelID(CHANNEL_GROUP_NETWORK, CHANNEL_NETWORK_DNS),
                getStringType(interfaceInfo.getDNSNServer()));
        updateState(getChannelID(CHANNEL_GROUP_NETWORK, CHANNEL_NETWORK_STATE),
                getOnOffType(interfaceInfo.isConnected()));
    }

    /**
     * Update Traffic Channels
     * 
     * @param traffic
     */
    private void updateTrafficChannels(AsuswrtTraffic traffic) {
        updateState(getChannelID(CHANNEL_GROUP_TRAFFIC, CHANNEL_TRAFFIC_CURRENT_RX),
                getQuantityType(traffic.getCurrentRX(), Units.MEGABIT_PER_SECOND));
        updateState(getChannelID(CHANNEL_GROUP_TRAFFIC, CHANNEL_TRAFFIC_CURRENT_TX),
                getQuantityType(traffic.getCurrentTX(), Units.MEGABIT_PER_SECOND));
        updateState(getChannelID(CHANNEL_GROUP_TRAFFIC, CHANNEL_TRAFFIC_TODAY_RX),
                getQuantityType(traffic.getTodayRX(), Units.MEGABYTE));
        updateState(getChannelID(CHANNEL_GROUP_TRAFFIC, CHANNEL_TRAFFIC_TODAY_TX),
                getQuantityType(traffic.getTodayTX(), Units.MEGABYTE));
        updateState(getChannelID(CHANNEL_GROUP_TRAFFIC, CHANNEL_TRAFFIC_TOTAL_RX),
                getQuantityType(traffic.getTotalRX(), Units.MEGABYTE));
        updateState(getChannelID(CHANNEL_GROUP_TRAFFIC, CHANNEL_TRAFFIC_TOTAL_TX),
                getQuantityType(traffic.getTotalTX(), Units.MEGABYTE));
    }

    /**
     * fire events when new informations changed
     * 
     * @param clientInfo
     */
    public void fireEvents(AsuswrtIpInfo interfaceInfo) {
        Boolean isConnected = interfaceInfo.isConnected();
        if (checkForStateChange(CHANNEL_NETWORK_STATE, isConnected)) {
            if (isConnected) {
                triggerChannel(getChannelID(CHANNEL_GROUP_NETWORK, EVENT_CONNECTION), EVENT_STATE_CONNECTED);
            } else {
                triggerChannel(getChannelID(CHANNEL_GROUP_NETWORK, EVENT_CONNECTION), EVENT_STATE_DISCONNECTED);
            }
        }
    }

    /***********************************
     *
     * FUNCTIONS
     *
     ************************************/

    /**
     * Get ChannelID including group
     * 
     * @param group String channel-group
     * @param channel String channel-name
     * @return String channelID
     */
    protected String getChannelID(String group, String channel) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (CHANNEL_GROUP_THING_SET.contains(thingTypeUID) && group.length() > 0) {
            return group + "#" + channel;
        }
        return channel;
    }

    /**
     * Get Channel from ChannelID
     * 
     * @param channelID String channelID
     * @return String channel-name
     */
    protected String getChannelFromID(ChannelUID channelID) {
        String channel = channelID.getIdWithoutGroup();
        channel = channel.replace(CHANNEL_GROUP_NETWORK + "#", "");
        return channel;
    }

    /**
     * Check if state changed since last channel update
     * 
     * @param stateName name of state (channel)
     * @param comparator comparation value
     * @return true if changed, false if not or no old value existds
     */
    private Boolean checkForStateChange(String stateName, Object comparator) {
        if (oldStates.get(stateName) == null) {
            oldStates.put(stateName, comparator);
        } else if (!comparator.equals(oldStates.get(stateName))) {
            oldStates.put(stateName, comparator);
            return true;
        }
        return false;
    }
}
