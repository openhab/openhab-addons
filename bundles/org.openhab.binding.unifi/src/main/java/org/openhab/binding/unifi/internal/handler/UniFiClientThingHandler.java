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
package org.openhab.binding.unifi.internal.handler;

import static org.openhab.binding.unifi.internal.UniFiBindingConstants.*;
import static org.openhab.core.thing.ThingStatus.*;
import static org.openhab.core.thing.ThingStatusDetail.CONFIGURATION_ERROR;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifi.internal.UniFiBindingConstants;
import org.openhab.binding.unifi.internal.UniFiClientThingConfig;
import org.openhab.binding.unifi.internal.api.UniFiException;
import org.openhab.binding.unifi.internal.api.model.UniFiClient;
import org.openhab.binding.unifi.internal.api.model.UniFiController;
import org.openhab.binding.unifi.internal.api.model.UniFiDevice;
import org.openhab.binding.unifi.internal.api.model.UniFiSite;
import org.openhab.binding.unifi.internal.api.model.UniFiWiredClient;
import org.openhab.binding.unifi.internal.api.model.UniFiWirelessClient;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UniFiClientThingHandler} is responsible for handling commands and status
 * updates for UniFi Wireless Devices.
 *
 * @author Matthew Bowman - Initial contribution
 * @author Patrik Wimnell - Blocking / Unblocking client support
 */
@NonNullByDefault
public class UniFiClientThingHandler extends UniFiBaseThingHandler<UniFiClient, UniFiClientThingConfig> {

    public static boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return UniFiBindingConstants.THING_TYPE_WIRELESS_CLIENT.equals(thingTypeUID);
    }

    private final Logger logger = LoggerFactory.getLogger(UniFiClientThingHandler.class);

    private UniFiClientThingConfig config = new UniFiClientThingConfig();

    public UniFiClientThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected synchronized void initialize(UniFiClientThingConfig config) {
        // mgb: called when the config changes
        logger.debug("Initializing the UniFi Client Handler with config = {}", config);
        if (!config.isValid()) {
            updateStatus(OFFLINE, CONFIGURATION_ERROR,
                    "You must define a MAC address, IP address, hostname or alias for this thing.");
            return;
        }
        this.config = config;
        updateStatus(ONLINE);
    }

    private static boolean belongsToSite(UniFiClient client, String siteName) {
        boolean result = true; // mgb: assume true = proof by contradiction
        if (!siteName.isEmpty()) {
            UniFiSite site = client.getSite();
            // mgb: if the 'site' can't be found or the name doesn't match...
            if (site == null || !site.matchesName(siteName)) {
                // mgb: ... then the client doesn't belong to this thing's configured 'site' and we 'filter' it
                result = false;
            }
        }
        return result;
    }

    @Override
    protected synchronized @Nullable UniFiClient getEntity(UniFiController controller) {
        UniFiClient client = controller.getClient(config.getClientID());
        // mgb: short circuit
        if (client == null || !belongsToSite(client, config.getSite())) {
            return null;
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
            case CHANNEL_BLOCKED:
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
            case CHANNEL_RECONNECT:
                state = OnOffType.OFF;
                break;
        }
        return state;
    }

    private synchronized boolean isClientHome(UniFiClient client) {
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

    @Override
    protected void refreshChannel(UniFiClient client, ChannelUID channelUID) {
        boolean clientHome = isClientHome(client);
        UniFiDevice device = client.getDevice();
        UniFiSite site = (device == null ? null : device.getSite());
        String channelID = channelUID.getIdWithoutGroup();
        State state = getDefaultState(channelID, clientHome);
        switch (channelID) {
            // mgb: common wired + wireless client channels

            // :online
            case CHANNEL_ONLINE:
                state = OnOffType.from(clientHome);
                break;

            // :site
            case CHANNEL_SITE:
                if (clientHome && site != null && site.getDescription() != null && !site.getDescription().isBlank()) {
                    state = StringType.valueOf(site.getDescription());
                }
                break;

            // :macAddress
            case CHANNEL_MAC_ADDRESS:
                if (clientHome && client.getMac() != null && !client.getMac().isBlank()) {
                    state = StringType.valueOf(client.getMac());
                }
                break;

            // :ipAddress
            case CHANNEL_IP_ADDRESS:
                if (clientHome && client.getIp() != null && !client.getIp().isBlank()) {
                    state = StringType.valueOf(client.getIp());
                }
                break;

            // :uptime
            case CHANNEL_UPTIME:
                if (clientHome && client.getUptime() != null) {
                    state = new DecimalType(client.getUptime());
                }
                break;

            // :lastSeen
            case CHANNEL_LAST_SEEN:
                // mgb: we don't check clientOnline as lastSeen is also included in the Insights data
                if (client.getLastSeen() != null) {
                    state = new DateTimeType(
                            ZonedDateTime.ofInstant(client.getLastSeen().toInstant(), ZoneId.systemDefault()));
                }
                break;

            // :blocked
            case CHANNEL_BLOCKED:
                state = OnOffType.from(client.isBlocked());
                break;

            default:
                // mgb: additional wired client channels
                if (client.isWired() && (client instanceof UniFiWiredClient)) {
                    state = getWiredChannelState((UniFiWiredClient) client, clientHome, channelID);
                }

                // mgb: additional wireless client channels
                else if (client.isWireless() && (client instanceof UniFiWirelessClient)) {
                    state = getWirelessChannelState((UniFiWirelessClient) client, clientHome, channelID);
                }
                break;
        }
        // mgb: only non null states get updates
        if (state != UnDefType.NULL) {
            updateState(channelID, state);
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
                if (clientHome && device != null && device.getName() != null && !device.getName().isBlank()) {
                    state = StringType.valueOf(device.getName());
                }
                break;

            // :essid
            case CHANNEL_ESSID:
                if (clientHome && client.getEssid() != null && !client.getEssid().isBlank()) {
                    state = StringType.valueOf(client.getEssid());
                }
                break;

            // :rssi
            case CHANNEL_RSSI:
                if (clientHome && client.getRssi() != null) {
                    state = new DecimalType(client.getRssi());
                }
                break;

            // :reconnect
            case CHANNEL_RECONNECT:
                // nop - read-only channel
                break;
        }
        return state;
    }

    @Override
    protected void handleCommand(UniFiClient client, ChannelUID channelUID, Command command) throws UniFiException {
        String channelID = channelUID.getIdWithoutGroup();
        switch (channelID) {
            case CHANNEL_BLOCKED:
                handleBlockedCommand(client, channelUID, command);
                break;
            case CHANNEL_RECONNECT:
                handleReconnectCommand(client, channelUID, command);
                break;
            default:
                logger.warn("Ignoring unsupported command = {} for channel = {}", command, channelUID);
        }
    }

    private void handleBlockedCommand(UniFiClient client, ChannelUID channelUID, Command command)
            throws UniFiException {
        if (command instanceof OnOffType) {
            client.block(command == OnOffType.ON);
        } else {
            logger.warn("Ignoring unsupported command = {} for channel = {} - valid commands types are: OnOffType",
                    command, channelUID);
        }
    }

    private void handleReconnectCommand(UniFiClient client, ChannelUID channelUID, Command command)
            throws UniFiException {
        if (command instanceof OnOffType) {
            if (command == OnOffType.ON) {
                client.reconnect();
                updateState(channelUID, OnOffType.OFF);
            }
        } else {
            logger.warn("Ignoring unsupported command = {} for channel = {} - valid commands types are: OnOffType",
                    command, channelUID);
        }
    }
}
