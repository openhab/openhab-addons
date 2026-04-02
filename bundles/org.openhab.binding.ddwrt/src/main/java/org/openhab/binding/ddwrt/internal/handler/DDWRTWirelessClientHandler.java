/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.ddwrt.internal.handler;

import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.CHANNEL_AP;
import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.CHANNEL_AP_MAC;
import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.CHANNEL_HOSTNAME;
import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.CHANNEL_IP_ADDRESS;
import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.CHANNEL_LAST_SEEN;
import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.CHANNEL_MAC_ADDRESS;
import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.CHANNEL_ONLINE;
import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.CHANNEL_RX_RATE;
import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.CHANNEL_SNR;
import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.CHANNEL_SSID;
import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.CHANNEL_TX_RATE;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ddwrt.internal.DDWRTWirelessClientConfiguration;
import org.openhab.binding.ddwrt.internal.api.DDWRTNetwork;
import org.openhab.binding.ddwrt.internal.api.DDWRTNetworkCache;
import org.openhab.binding.ddwrt.internal.api.DDWRTWirelessClient;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for a DD-WRT Wireless Client thing.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public class DDWRTWirelessClientHandler
        extends DDWRTBaseHandler<DDWRTWirelessClient, DDWRTWirelessClientConfiguration> {

    private final Logger logger = Objects.requireNonNull(LoggerFactory.getLogger(DDWRTWirelessClientHandler.class));

    private DDWRTWirelessClientConfiguration config = new DDWRTWirelessClientConfiguration();

    public DDWRTWirelessClientHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected boolean initialize(DDWRTWirelessClientConfiguration config) {
        this.config = config;
        if (config.hostname.isEmpty()) {
            logger.warn("Hostname is required for wireless client thing");
            return false;
        }
        return true;
    }

    @Override
    protected @Nullable DDWRTWirelessClient getEntity(DDWRTNetworkCache cache) {
        String hostname = config.hostname;
        logger.debug("Looking up wireless client for hostname: {}, config MAC: {}", hostname, config.mac);

        // Try MAC first if available
        if (!config.mac.isEmpty()) {
            DDWRTWirelessClient client = cache.getWirelessClient(config.mac);
            if (client != null) {
                logger.debug("Found client by MAC: {}", config.mac);
                return client;
            }
            logger.debug("Client not found by MAC: {}", config.mac);
        }

        // Try hostname lookup (primary identifier, stable across MAC randomization)
        DDWRTWirelessClient client = cache.getWirelessClientByHostname(hostname);
        if (client != null) {
            logger.debug("Found client by hostname: {}", hostname);
        } else {
            logger.debug("Client not found by hostname: {}", hostname);
        }
        return client;
    }

    @Override
    protected State getChannelState(DDWRTWirelessClient client, String channelId) {
        return switch (channelId) {
            case CHANNEL_ONLINE -> OnOffType.from(client.isOnline());
            case CHANNEL_MAC_ADDRESS -> StringType.valueOf(client.getMac());
            case CHANNEL_HOSTNAME ->
                client.getHostname().isEmpty() ? UnDefType.UNDEF : StringType.valueOf(client.getHostname());
            case CHANNEL_IP_ADDRESS ->
                client.getIpAddress().isEmpty() ? UnDefType.UNDEF : StringType.valueOf(client.getIpAddress());
            case CHANNEL_AP ->
                client.getRadioName().isEmpty() ? UnDefType.UNDEF : StringType.valueOf(client.getRadioName());
            case CHANNEL_AP_MAC ->
                client.getApMac().isEmpty() ? UnDefType.UNDEF : StringType.valueOf(client.getApMac());
            case CHANNEL_SSID -> client.getSsid().isEmpty() ? UnDefType.UNDEF : StringType.valueOf(client.getSsid());
            case CHANNEL_SNR -> new DecimalType(client.getSnr());
            case CHANNEL_RX_RATE ->
                client.getRxRate().isEmpty() ? UnDefType.UNDEF : new DecimalType(parseRate(client.getRxRate()));
            case CHANNEL_TX_RATE ->
                client.getTxRate().isEmpty() ? UnDefType.UNDEF : new DecimalType(parseRate(client.getTxRate()));
            case CHANNEL_LAST_SEEN -> {
                Instant lastSeen = client.getLastSeen();
                yield lastSeen != null
                        ? new DateTimeType(
                                Objects.requireNonNull(ZonedDateTime.ofInstant(lastSeen, ZoneId.systemDefault())))
                        : UnDefType.UNDEF;
            }
            default -> UnDefType.NULL;
        };
    }

    @Override
    protected boolean handleCommand(DDWRTNetwork network, DDWRTWirelessClient client, ChannelUID channelUID,
            Command command) {
        // Wireless clients are read-only
        return false;
    }

    @Override
    protected State getDefaultState(String channelId) {
        if (CHANNEL_ONLINE.equals(channelId)) {
            return OnOffType.OFF;
        }
        return UnDefType.UNDEF;
    }

    @Override
    protected List<String> getCacheKeys() {
        List<String> keys = new ArrayList<>();
        // Register by configured hostname — primary key for MAC-randomizing clients
        if (!config.hostname.isEmpty()) {
            keys.add(config.hostname.toLowerCase(Locale.ROOT));
        }
        // Also register by configured MAC if available
        if (!config.mac.isEmpty()) {
            keys.add(config.mac.toLowerCase(Locale.ROOT));
        }
        return keys;
    }

    /**
     * Parse a rate string like "130.0" to a double value.
     */
    private double parseRate(String rate) {
        try {
            return Double.parseDouble(rate.replaceAll("[^0-9.]", ""));
        } catch (NumberFormatException e) {
            logger.trace("Could not parse rate: {}", rate);
            return 0;
        }
    }
}
