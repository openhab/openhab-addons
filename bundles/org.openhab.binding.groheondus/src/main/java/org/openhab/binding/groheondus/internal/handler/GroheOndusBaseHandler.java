/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.groheondus.internal.handler;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.groheondus.internal.GroheOndusApplianceConfiguration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.floriansw.ondus.api.OndusService;
import io.github.floriansw.ondus.api.model.BaseAppliance;
import io.github.floriansw.ondus.api.model.Location;
import io.github.floriansw.ondus.api.model.Room;

/**
 * @author Florian Schmidt - Initial contribution
 */
@NonNullByDefault
public abstract class GroheOndusBaseHandler<T extends BaseAppliance, M> extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(GroheOndusBaseHandler.class);

    protected @Nullable GroheOndusApplianceConfiguration config;

    private @Nullable ScheduledFuture<?> poller;

    private final int applianceType;

    // Used to space scheduled updates apart by 1 second to avoid rate limiting from service
    private int thingCounter = 0;

    public GroheOndusBaseHandler(Thing thing, int applianceType, int thingCounter) {
        super(thing);
        this.applianceType = applianceType;
        this.thingCounter = thingCounter;
    }

    protected void schedulePolling() {
        OndusService ondusService = getOndusService();
        if (ondusService == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "@text/error.noservice");
            return;
        }

        @Nullable
        T appliance = getAppliance(ondusService);
        if (appliance == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/error.empty.response");
            return;
        }
        int pollingInterval = getPollingInterval(appliance);
        ScheduledFuture<?> poller = this.poller;
        if (poller != null) {
            // Cancel any previous polling
            poller.cancel(true);
        }
        this.poller = scheduler.scheduleWithFixedDelay(this::updateChannels, thingCounter, pollingInterval,
                TimeUnit.SECONDS);
        logger.debug("Scheduled polling every {}s for appliance {}", pollingInterval, thing.getUID());
    }

    @Override
    public void dispose() {
        logger.debug("Disposing scheduled updater for thing {}", thing.getUID());
        ScheduledFuture<?> poller = this.poller;
        if (poller != null) {
            poller.cancel(true);
        }
        super.dispose();
    }

    @Override
    public void initialize() {
        config = getConfigAs(GroheOndusApplianceConfiguration.class);
        schedulePolling();
    }

    public void updateChannels() {
        logger.debug("Updating channels for appliance {}", thing.getUID());
        OndusService ondusService = getOndusService();
        if (ondusService == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "@text/error.noservice");
            // Update channels to UNDEF

            return;
        }

        @Nullable
        T appliance = getAppliance(ondusService);
        if (appliance == null) {
            logger.debug("Updating channels failed since appliance is null, thing {}", thing.getUID());
            return;
        }

        M measurement = getLastDataPoint(appliance);
        if (measurement != null) {
            getThing().getChannels().forEach(channel -> updateChannel(channel.getUID(), appliance, measurement));
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/error.failedtoloaddata");
        }
    }

    protected abstract M getLastDataPoint(T appliance);

    protected abstract void updateChannel(ChannelUID channelUID, T appliance, M measurement);

    public @Nullable OndusService getOndusService() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            return null;
        }
        BridgeHandler handler = bridge.getHandler();
        if (!(handler instanceof GroheOndusAccountHandler)) {
            return null;
        }
        try {
            return ((GroheOndusAccountHandler) handler).getService();
        } catch (IllegalStateException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return null;
        }
    }

    protected Room getRoom() {
        return new Room(config.roomId, getLocation());
    }

    protected Location getLocation() {
        return new Location(config.locationId);
    }

    protected @Nullable T getAppliance(OndusService ondusService) {
        try {
            BaseAppliance appliance = ondusService.getAppliance(getRoom(), config.applianceId).orElse(null);
            if (appliance != null) {
                if (appliance.getType() != getType()) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/error.wrongtype");
                    return null;
                }
                return (T) appliance;
            } else {
                logger.debug("getAppliance for thing {} returned null", thing.getUID());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/error.failedtoloaddata");
                getThing().getChannels().forEach(channel -> updateState(channel.getUID(), UnDefType.UNDEF));
            }

        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            getThing().getChannels().forEach(channel -> updateState(channel.getUID(), UnDefType.UNDEF));
            logger.debug("Could not load appliance", e);
        }
        return null;
    }

    protected abstract int getPollingInterval(T appliance);

    private int getType() {
        return this.applianceType;
    }
}
