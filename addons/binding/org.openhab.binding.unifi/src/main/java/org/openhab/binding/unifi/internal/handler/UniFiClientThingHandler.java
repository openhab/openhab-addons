/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.unifi.internal.handler;

import static org.eclipse.smarthome.core.thing.ThingStatus.OFFLINE;
import static org.eclipse.smarthome.core.thing.ThingStatus.ONLINE;
import static org.eclipse.smarthome.core.thing.ThingStatusDetail.*;
import static org.eclipse.smarthome.core.types.RefreshType.REFRESH;
import static org.openhab.binding.unifi.internal.UniFiBindingConstants.*;

import java.util.Calendar;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.unifi.internal.UniFiBindingConstants;
import org.openhab.binding.unifi.internal.UniFiClientThingConfig;
import org.openhab.binding.unifi.internal.api.model.UniFiClient;
import org.openhab.binding.unifi.internal.api.model.UniFiDevice;
import org.openhab.binding.unifi.internal.api.model.UniFiSite;
import org.openhab.binding.unifi.internal.api.model.UniFiWiredClient;
import org.openhab.binding.unifi.internal.api.model.UniFiWirelessClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UniFiClientThingHandler} is responsible for handling commands and status
 * updates for UniFi Wireless Devices.
 *
 * @author Matthew Bowman - Initial contribution
 */
@NonNullByDefault
public class UniFiClientThingHandler extends BaseThingHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .of(UniFiBindingConstants.THING_TYPE_WIRELESS_CLIENT).collect(Collectors.toSet());

    private final Logger logger = LoggerFactory.getLogger(UniFiClientThingHandler.class);

    private volatile @Nullable UniFiClientThingConfig config; /* mgb: volatile because accessed from multiple threads */

    public UniFiClientThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        // mgb: called when the config changes

        config = getConfig().as(UniFiClientThingConfig.class).tidy();
        logger.debug("Initializing the UniFi Client Handler with config = {}", config);

        if (!config.isValid()) {
            updateStatus(OFFLINE, CONFIGURATION_ERROR,
                    "You must define a MAC address, IP address, hostname or alias for this thing.");
            return;
        }

        Bridge bridge = getBridge();

        if (bridge == null || bridge.getHandler() == null
                || !(bridge.getHandler() instanceof UniFiControllerThingHandler)) {
            updateStatus(OFFLINE, CONFIGURATION_ERROR, "You must choose a UniFi Controller for this thing.");
            return;
        }

        UniFiControllerThingHandler controllerHandler = (UniFiControllerThingHandler) bridge.getHandler();
        if (config.getConsiderHome() <= controllerHandler.getRefreshInterval()) {
            updateStatus(OFFLINE, CONFIGURATION_ERROR,
                    "Consider home parameter must be larger than the controller's refresh interval ("
                            + controllerHandler.getRefreshInterval() + "s).");
            return;
        }

        if (bridge.getStatus() == OFFLINE) {
            updateStatus(OFFLINE, BRIDGE_OFFLINE, "The UniFi Controller is currently offline.");
            return;
        }

        // mgb: only refreshes if we we're ONLINE
        refresh();

        updateStatus(ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // mgb: only handle the command if we're ONLINE
        if (getThing().getStatus() == ONLINE) {
            logger.debug("Handling command = {} for channel = {}", command, channelUID);
            if (command == REFRESH) {
                refreshChannel(getClient(), channelUID);
            } else {
                logger.debug("Ignoring unsupported command = {} for channel = {} - the UniFi binding is read-only!",
                        command, channelUID);
            }
        }
    }

    public void refresh() {
        // mgb: only refresh if we're ONLINE
        if (getThing().getStatus() == ONLINE) {
            UniFiClient client = getClient();
            for (Channel channel : getThing().getChannels()) {
                ChannelUID channelUID = channel.getUID();
                refreshChannel(client, channelUID);
            }
        }
    }

    private @Nullable UniFiClient getClient() {
        UniFiClient client = null;

        Bridge bridge = getBridge();
        if (bridge != null) {
            UniFiControllerThingHandler handler = (UniFiControllerThingHandler) bridge.getHandler();
            if (handler != null) {
                client = handler.getClient(config.getClientID(), config.getSite());
            }
        }

        if (client == null) {
            logger.debug("Could not find a matching client: cid = {}, site = {}", config.getClientID(),
                    config.getSite());
        }

        return client;
    }

    private State getDefaultState(String channelID, boolean clientHome) {
        State state = UnDefType.NULL;
        switch (channelID) {
            case CHANNEL_ONLINE:
            case CHANNEL_SITE:
            case CHANNEL_AP:
            case CHANNEL_ESSID:
            case CHANNEL_RSSI:
            case CHANNEL_MAC_ADDRESS:
            case CHANNEL_IP_ADDRESS:
                state = (clientHome ? UnDefType.NULL : UnDefType.UNDEF); // skip the update if the client is home
                break;
            case CHANNEL_UPTIME:
                // mgb: uptime should default to 0 seconds
                state = (clientHome ? UnDefType.NULL : new DecimalType(0)); // skip the update if the client is home
                break;
            case CHANNEL_LAST_SEEN:
                // mgb: lastSeen should keep the last state no matter what
                state = UnDefType.NULL;
                break;
        }
        return state;
    }

    private boolean isClientHome(@Nullable UniFiClient client) {
        boolean online = false;
        if (client != null) {
            Calendar lastSeen = client.getLastSeen();
            if (lastSeen == null) {
                logger.warn("Could not determine if client is online: cid = {}, lastSeen = null", config.getClientID());
            } else {
                Calendar considerHome = (Calendar) lastSeen.clone();
                considerHome.add(Calendar.SECOND, config.getConsiderHome());
                Calendar now = Calendar.getInstance();
                online = (now.compareTo(considerHome) < 0);
            }
        }
        return online;
    }

    private void refreshChannel(@Nullable UniFiClient client, ChannelUID channelUID) {
        // mgb: only refresh if we're ONLINE
        if (getThing().getStatus() == ONLINE) {
            logger.debug("Refreshing channel = {}", channelUID);

            boolean clientHome = isClientHome(client);
            UniFiDevice device = (client == null ? null : client.getDevice());
            UniFiSite site = (device == null ? null : device.getSite());

            String channelID = channelUID.getIdWithoutGroup();
            State state = getDefaultState(channelID, clientHome);

            switch (channelID) {
                // mgb: common wired + wireless client channels

                // :online
                case CHANNEL_ONLINE:
                    state = (clientHome ? OnOffType.ON : OnOffType.OFF);
                    break;

                // :site
                case CHANNEL_SITE:
                    if (clientHome && site != null && StringUtils.isNotBlank(site.getDescription())) {
                        state = StringType.valueOf(site.getDescription());
                    }
                    break;

                // :macAddress
                case CHANNEL_MAC_ADDRESS:
                    if (clientHome && client != null && StringUtils.isNotBlank(client.getMac())) {
                        state = StringType.valueOf(client.getMac());
                    }
                    break;

                // :ipAddress
                case CHANNEL_IP_ADDRESS:
                    if (clientHome && client != null && StringUtils.isNotBlank(client.getIp())) {
                        state = StringType.valueOf(client.getIp());
                    }
                    break;

                // :uptime
                case CHANNEL_UPTIME:
                    if (clientHome && client != null && client.getUptime() != null) {
                        state = new DecimalType(client.getUptime());
                    }
                    break;

                // :lastSeen
                case CHANNEL_LAST_SEEN:
                    // mgb: we don't check clientOnline as lastSeen is also included in the Insights data
                    if (client != null && client.getLastSeen() != null) {
                        state = new DateTimeType(client.getLastSeen());
                    }
                    break;

                default:
                    if (client != null) {
                        // mgb: additional wired client channels
                        if (client.isWired() && (client instanceof UniFiWiredClient)) {
                            state = getWiredChannelState((UniFiWiredClient) client, clientHome, channelID);
                        }

                        // mgb: additional wireless client channels
                        else if (client.isWireless() && (client instanceof UniFiWirelessClient)) {
                            state = getWirelessChannelState((UniFiWirelessClient) client, clientHome, channelID);
                        }
                    }
                    break;
            }

            // mgb: only non null states get updates
            if (state != UnDefType.NULL) {
                updateState(channelID, state);
            }
        }
    }

    private State getWiredChannelState(UniFiWiredClient client, boolean clientHome, String channelID) {
        State state = UnDefType.NULL;
        return state;
    }

    private State getWirelessChannelState(UniFiWirelessClient client, boolean clientHome, String channelID) {
        State state = UnDefType.NULL;
        switch (channelID) {
            // :ap
            case CHANNEL_AP:
                UniFiDevice device = client.getDevice();
                if (clientHome && device != null && StringUtils.isNotBlank(device.getName())) {
                    state = StringType.valueOf(device.getName());
                }
                break;

            // :essid
            case CHANNEL_ESSID:
                if (clientHome && StringUtils.isNotBlank(client.getEssid())) {
                    state = StringType.valueOf(client.getEssid());
                }
                break;

            // :rssi
            case CHANNEL_RSSI:
                if (clientHome && client.getRssi() != null) {
                    state = new DecimalType(client.getRssi());
                }
                break;

        }
        return state;
    }

}
