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

import static org.openhab.binding.unifi.internal.UniFiBindingConstants.CHANNEL_GUEST_CLIENTS;
import static org.openhab.binding.unifi.internal.UniFiBindingConstants.CHANNEL_TOTAL_CLIENTS;
import static org.openhab.binding.unifi.internal.UniFiBindingConstants.CHANNEL_WIRED_CLIENTS;
import static org.openhab.binding.unifi.internal.UniFiBindingConstants.CHANNEL_WIRELESS_CLIENTS;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifi.internal.UniFiSiteThingConfig;
import org.openhab.binding.unifi.internal.api.UniFiController;
import org.openhab.binding.unifi.internal.api.UniFiException;
import org.openhab.binding.unifi.internal.api.cache.UniFiControllerCache;
import org.openhab.binding.unifi.internal.api.dto.UniFiSite;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link UniFiSiteThingHandler} is responsible for handling commands and status
 * updates for {@link UniFiSite} instances.
 *
 * @author Matthew Bowman - Initial contribution
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class UniFiSiteThingHandler extends UniFiBaseThingHandler<UniFiSite, UniFiSiteThingConfig> {

    private UniFiSiteThingConfig config = new UniFiSiteThingConfig();

    public UniFiSiteThingHandler(final Thing thing) {
        super(thing);
    }

    @Override
    protected boolean initialize(final UniFiSiteThingConfig config) {
        this.config = config;
        if (!config.isValid()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/error.thing.site.offline.configuration_error");
            return false;
        }
        return true;
    }

    @Override
    protected @Nullable UniFiSite getEntity(final UniFiControllerCache cache) {
        return cache.getSite(config.getSiteID());
    }

    @Override
    protected State getChannelState(final UniFiSite site, final String channelId) {
        final UniFiControllerCache cache = site.getCache();
        final long count;

        switch (channelId) {
            case CHANNEL_TOTAL_CLIENTS:
                count = cache.countClients(site, c -> true);
                break;
            case CHANNEL_WIRELESS_CLIENTS:
                count = cache.countClients(site, c -> c.isWireless());
                break;
            case CHANNEL_WIRED_CLIENTS:
                count = cache.countClients(site, c -> c.isWired());
                break;
            case CHANNEL_GUEST_CLIENTS:
                count = cache.countClients(site, c -> c.isGuest());
                break;
            default:
                // Unsupported channel; nothing to update
                return UnDefType.NULL;
        }
        return new DecimalType(count);
    }

    @Override
    protected boolean handleCommand(final UniFiController controller, final UniFiSite entity,
            final ChannelUID channelUID, final Command command) throws UniFiException {
        return false;
    }
}
