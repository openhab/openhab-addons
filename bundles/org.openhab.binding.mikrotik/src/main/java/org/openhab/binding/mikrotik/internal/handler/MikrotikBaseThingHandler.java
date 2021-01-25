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
package org.openhab.binding.mikrotik.internal.handler;

import static org.eclipse.smarthome.core.thing.ThingStatus.OFFLINE;
import static org.eclipse.smarthome.core.thing.ThingStatus.ONLINE;
import static org.eclipse.smarthome.core.types.RefreshType.REFRESH;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ThingStatusInfoBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.joda.time.DateTime;
import org.openhab.binding.mikrotik.internal.config.InterfaceThingConfig;
import org.openhab.binding.mikrotik.internal.model.RouterosDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MikrotikBaseThingHandler} is a base class for all other RouterOS things of map-value nature.
 * It is responsible for handling commands, which are sent to one of the channels and emit channel updates
 * whenever required.
 *
 * @author Oleg Vivtash - Initial contribution
 *
 *
 * @param <C> config - the config class used by this base thing handler
 *
 */
@NonNullByDefault
public abstract class MikrotikBaseThingHandler<C> extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(MikrotikBaseThingHandler.class);
    private @Nullable InterfaceThingConfig config;
    private @Nullable ScheduledFuture<?> refreshJob;
    protected DateTime lastModelsRefresh = DateTime.now();
    protected Map<String, State> currentState = new HashMap<>();

    // public static boolean supportsThingType(ThingTypeUID thingTypeUID) <- in subclasses

    public MikrotikBaseThingHandler(Thing thing) {
        super(thing);
    }

    protected @Nullable MikrotikRouterosBridgeHandler getVerifiedBridgeHandler() {
        @Nullable
        Bridge bridgeRef = getBridge();
        if (bridgeRef != null && bridgeRef.getHandler() != null
                && (bridgeRef.getHandler() instanceof MikrotikRouterosBridgeHandler)) {
            return (MikrotikRouterosBridgeHandler) bridgeRef.getHandler();
        }
        return null;
    }

    protected final @Nullable RouterosDevice getRouteros() {
        @Nullable
        MikrotikRouterosBridgeHandler bridgeHandler = getVerifiedBridgeHandler();
        return bridgeHandler == null ? null : bridgeHandler.getRouteros();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handling command = {} for channel = {}", command, channelUID);
        if (getThing().getStatus() == ONLINE) {
            RouterosDevice routeros = getRouteros();
            if (routeros != null) {
                if (command == REFRESH) {
                    throttledRefreshModels();
                    refreshChannel(channelUID);
                } else {
                    try {
                        executeCommand(channelUID, command);
                    } catch (Exception e) {
                        logger.warn("Unexpected error handling command = {} for channel = {} : {}", command, channelUID,
                                e.getMessage());
                    }
                }
            }
        }
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing!");
        cancelRefreshJob();
        if (getVerifiedBridgeHandler() == null) {
            updateStatus(OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "This thing requires a RouterOS bridge");
            return;
        }

        updateStatus(ONLINE);

        // derive the config class from the generic type
        logger.trace("Getting config for {}", getThing().getUID());
        Class<?> klass = (Class<?>) (((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0]);
        C config = (C) getConfigAs(klass);
        logger.trace("Running initializer for {} ({}) with config: {}", getThing().getUID(), getThing().getStatus(),
                config);
        initialize(config);
        logger.debug("Finished initializing!");
    }

    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        logger.trace("Updating {} status to {} / {}", getThing().getUID(), status, statusDetail);
        if (status == ONLINE || (status == OFFLINE && statusDetail == ThingStatusDetail.COMMUNICATION_ERROR)) {
            scheduleRefreshJob();
        } else if (status == OFFLINE
                && (statusDetail == ThingStatusDetail.CONFIGURATION_ERROR || statusDetail == ThingStatusDetail.GONE)) {
            cancelRefreshJob();
        }
        // update the status only if it's changed
        ThingStatusInfo statusInfo = ThingStatusInfoBuilder.create(status, statusDetail).withDescription(description)
                .build();
        if (!statusInfo.equals(getThing().getStatusInfo())) {
            super.updateStatus(status, statusDetail, description);
        }
    }

    private void scheduleRefreshJob() {
        synchronized (this) {
            if (refreshJob == null) {
                int refreshPeriod = getVerifiedBridgeHandler().getBridgeConfig().refresh;
                logger.debug("Scheduling refresh job every {}s", refreshPeriod);
                refreshJob = scheduler.scheduleWithFixedDelay(this::scheduledRun, refreshPeriod, refreshPeriod,
                        TimeUnit.SECONDS);
            }
        }
    }

    private void cancelRefreshJob() {
        synchronized (this) {
            if (refreshJob != null) {
                logger.debug("Cancelling refresh job");
                refreshJob.cancel(true);
                refreshJob = null;
            }
        }
    }

    private void scheduledRun() {
        logger.trace("scheduledRun() called");
        try {
            if (getVerifiedBridgeHandler() == null) {
                updateStatus(OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Failed reaching out to RouterOS bridge");
                return;
            }
            if (getBridge() != null && getBridge().getStatus() == OFFLINE) {
                updateStatus(OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "The RouterOS bridge is currently offline");
                return;
            }

            if (getThing().getStatus() != ONLINE)
                updateStatus(ONLINE);
            logger.debug("Refreshing all {} channels", getThing().getUID());
            for (Channel channel : getThing().getChannels()) {
                refreshChannel(channel.getUID());
            }
        } catch (Exception e) {
            logger.warn("Unhandled exception while refreshing the {} Mikrotik thing", getThing().getUID(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    protected final void refresh() throws ChannelUpdateException {
        if (getThing().getStatus() == ONLINE) {
            if (getRouteros() != null) {
                throttledRefreshModels();
                for (Channel channel : getThing().getChannels()) {
                    ChannelUID channelUID = channel.getUID();
                    try {
                        refreshChannel(channelUID);
                    } catch (RuntimeException e) {
                        throw new ChannelUpdateException(getThing().getUID(), channelUID, e);
                    }
                }
            }
        }
    }

    protected void throttledRefreshModels() {
        MikrotikRouterosBridgeHandler bridgeHandler = (MikrotikRouterosBridgeHandler) getBridge().getHandler();
        if (DateTime.now().isAfter(lastModelsRefresh.plusSeconds(bridgeHandler.getBridgeConfig().refresh))) {
            lastModelsRefresh = DateTime.now();
            refreshModels();
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Mikrotik Thing");
        cancelRefreshJob();
    }

    protected abstract void initialize(@NonNull C config);

    protected abstract void refreshModels();

    protected abstract void refreshChannel(ChannelUID channelUID);

    protected abstract void executeCommand(ChannelUID channelUID, Command command);
}
