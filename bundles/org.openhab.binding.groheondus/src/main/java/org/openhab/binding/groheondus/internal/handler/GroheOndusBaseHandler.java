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
package org.openhab.binding.groheondus.internal.handler;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.grohe.ondus.api.OndusService;
import org.grohe.ondus.api.model.BaseAppliance;
import org.grohe.ondus.api.model.Location;
import org.grohe.ondus.api.model.Room;
import org.openhab.binding.groheondus.internal.GroheOndusApplianceConfiguration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Florian Schmidt - Initial contribution
 */
@NonNullByDefault
public abstract class GroheOndusBaseHandler<T extends BaseAppliance, M> extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(GroheOndusBaseHandler.class);

    protected @Nullable GroheOndusApplianceConfiguration config;

    private final int applianceType;

    public GroheOndusBaseHandler(Thing thing, int applianceType) {
        super(thing);
        this.applianceType = applianceType;
    }

    @Override
    public void initialize() {
        config = getConfigAs(GroheOndusApplianceConfiguration.class);

        OndusService ondusService = getOndusService();
        if (ondusService == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                    "No initialized OndusService available from bridge.");
            return;
        }

        @Nullable
        T appliance = getAppliance(ondusService);
        if (appliance == null) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.COMMUNICATION_ERROR, "Could not load appliance");
            return;
        }
        int pollingInterval = getPollingInterval(appliance);
        scheduler.scheduleWithFixedDelay(this::updateChannels, 0, pollingInterval, TimeUnit.SECONDS);

        updateStatus(ThingStatus.UNKNOWN);
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        super.channelLinked(channelUID);

        OndusService ondusService = getOndusService();
        if (ondusService == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                    "No initialized OndusService available from bridge.");
            return;
        }

        @Nullable
        T appliance = getAppliance(ondusService);
        if (appliance == null) {
            return;
        }
        updateChannel(channelUID, appliance, getLastDataPoint(appliance));
    }

    public void updateChannels() {
        OndusService ondusService = getOndusService();
        if (ondusService == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                    "No initialized OndusService available from bridge.");
            return;
        }

        @Nullable
        T appliance = getAppliance(ondusService);
        if (appliance == null) {
            return;
        }

        M measurement = getLastDataPoint(appliance);
        getThing().getChannels().forEach(channel -> updateChannel(channel.getUID(), appliance, measurement));

        updateStatus(ThingStatus.ONLINE);
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
            if (appliance.getType() != getType()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Thing is not a GROHE SENSE Guard device.");
                return null;
            }
            return (T) appliance;
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            logger.debug("Could not load appliance", e);
        }
        return null;
    }

    protected abstract int getPollingInterval(T appliance);

    private int getType() {
        return this.applianceType;
    }
}
