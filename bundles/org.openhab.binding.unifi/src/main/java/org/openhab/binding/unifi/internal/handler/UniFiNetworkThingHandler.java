/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifi.internal.UniFiNetworkThingConfig;
import org.openhab.binding.unifi.internal.api.UniFiController;
import org.openhab.binding.unifi.internal.api.UniFiException;
import org.openhab.binding.unifi.internal.api.cache.UniFiControllerCache;
import org.openhab.binding.unifi.internal.api.dto.UniFiClient;
import org.openhab.binding.unifi.internal.api.dto.UniFiNetwork;
import org.openhab.binding.unifi.internal.api.dto.UniFiSite;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link UniFiNetworkThingHandler} is responsible for handling commands and status updates for a wireless network.
 *
 * @author Thomas Lauterbach - Initial contribution
 */
@NonNullByDefault
public class UniFiNetworkThingHandler extends UniFiBaseThingHandler<UniFiNetwork, UniFiNetworkThingConfig> {

    private UniFiNetworkThingConfig config = new UniFiNetworkThingConfig();

    public UniFiNetworkThingHandler(final Thing thing) {
        super(thing);
    }

    @Override
    protected boolean initialize(final UniFiNetworkThingConfig config) {
        this.config = config;

        if (!config.isValid()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/error.thing.network.offline.configuration_error");
            return false;
        }
        return true;
    }

    @Override
    protected @Nullable UniFiNetwork getEntity(final UniFiControllerCache cache) {
        return cache.getNetwork(config.getNetworkId());
    }

    @Override
    protected State getChannelState(final UniFiNetwork network, final String channelId) {
        final State state;

        switch (channelId) {
            case CHANNEL_ENABLE:
                state = OnOffType.from(network.isEnabled());
                break;
            case CHANNEL_SITE:
                final UniFiSite site = network.getSite();
                if (site != null && site.getDescription() != null && !site.getDescription().isBlank()) {
                    state = StringType.valueOf(site.getDescription());
                } else {
                    state = UnDefType.UNDEF;
                }
                break;
            case CHANNEL_PURPOSE:
                state = StringType.valueOf(network.getPurpose());
                break;
            case CHANNEL_TOTAL_CLIENTS:
                state = countClients(network);
                break;
            default:
                // Unsupported channel; nothing to update
                state = UnDefType.NULL;
        }
        return state;
    }

    private static State countClients(final UniFiNetwork network) {
        final UniFiSite site = network.getSite();

        if (site == null) {
            return UnDefType.UNDEF;
        } else {
            return new DecimalType(site.getCache().countClients(site, c -> c instanceof UniFiClient client
                    && (network.getName() != null && network.equals(client.getNetwork()))));
        }
    }

    @Override
    protected boolean handleCommand(final UniFiController controller, final UniFiNetwork network,
            final ChannelUID channelUID, Command command) throws UniFiException {
        final String channelID = channelUID.getId();

        if (CHANNEL_ENABLE.equals(channelID) && command instanceof OnOffType && network.getSite() != null) {
            controller.enableNetwork(network, OnOffType.ON == command);
            return true;
        }
        return false;
    }
}
