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
import org.openhab.binding.asuswrt.internal.helpers.AsuswrtUtils;
import org.openhab.binding.asuswrt.internal.structures.AsuswrtClientInfo;
import org.openhab.core.config.core.Configuration;
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
public class AsuswrtClient extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(AsuswrtClient.class);
    private final AsuswrtRouter router;
    private Map<String, Object> oldStates = new HashMap<>();
    protected final String uid;

    /**
     * Constructor
     *
     * @param thing Thing object representing client
     * @param router Router (Bridge) Thing
     */
    public AsuswrtClient(Thing thing, AsuswrtRouter router) {
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
        router.queryDeviceData(false);
        refreshData();
        updateStatus(ThingStatus.ONLINE);
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
            refreshData();
        }
    }

    /**
     * update clientpropedrties and channels
     * 
     * @param clientInfo
     */
    public void clientPropertiesChanged(AsuswrtClientInfo clientInfo) {
        logger.trace("({}) clientPropertiesChanged ", uid);
        Map<String, String> properties = editProperties();
        properties.put(Thing.PROPERTY_MAC_ADDRESS, clientInfo.getMac());
        properties.put(Thing.PROPERTY_VENDOR, clientInfo.getVendor());
        properties.put(PROPERTY_CLIENT_NAME, clientInfo.getName());
        updateProperties(properties);

        updateState(getChannelID(CHANNEL_GROUP_CLIENT, CHANNEL_CLIENT_ONLINE), getOnOffType(clientInfo.isOnline()));
        updateState(getChannelID(CHANNEL_GROUP_CLIENT, CHANNEL_CLIENT_INETSTATE),
                getOnOffType(clientInfo.getInternetState()));
        updateState(getChannelID(CHANNEL_GROUP_CLIENT, CHANNEL_IP_ADDRESS), getStringType(clientInfo.getIP()));
        updateState(getChannelID(CHANNEL_GROUP_CLIENT, CHANNEL_IP_METHOD), getStringType(clientInfo.getIpMethod()));
    }

    /**
     * fire events when new clientInformations changed
     * 
     * @param clientInfo
     */
    private void fireEvents(AsuswrtClientInfo clientInfo) {
        if (checkForStateChange(CHANNEL_CLIENT_ONLINE, clientInfo.isOnline())) {
            if (clientInfo.isOnline()) {
                triggerChannel(getChannelID(CHANNEL_GROUP_CLIENT, EVENT_CLIENT_ONLINE), EVENT_STATE_CONNECTED);
            } else {
                triggerChannel(getChannelID(CHANNEL_GROUP_CLIENT, EVENT_CLIENT_ONLINE), EVENT_STATE_GONE);
            }
        }
    }

    /**
     * refresh data
     */
    private void refreshData() {
        String mac = getMac();
        AsuswrtClientInfo clientInfo = router.deviceInfo.getClients().getClientByMAC(mac);
        fireEvents(clientInfo);
        clientPropertiesChanged(clientInfo);
    }

    /***********************************
     *
     * FUNCTIONS
     *
     ************************************/

    /**
     * Get MAC-Address of Client from properties or settings
     * 
     * @return
     */
    public String getMac() {
        String mac = "";
        Map<String, String> properties = getThing().getProperties();
        Configuration config = getThing().getConfiguration();

        /* get mac from properties */
        if (properties.containsKey(Thing.PROPERTY_MAC_ADDRESS)) {
            mac = config.get(Thing.PROPERTY_MAC_ADDRESS).toString();
        }

        /* get mac from config */
        if (mac.isBlank() && config.containsKey(Thing.PROPERTY_MAC_ADDRESS)) {
            mac = config.get(Thing.PROPERTY_MAC_ADDRESS).toString();
        }

        if (mac.isBlank()) {
            logger.debug("({}) cant find macAddress in properties and config", uid);
        }
        return AsuswrtUtils.formatMac(mac, ':');
    }

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
        channel = channel.replace(CHANNEL_GROUP_CLIENT + "#", "");
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
