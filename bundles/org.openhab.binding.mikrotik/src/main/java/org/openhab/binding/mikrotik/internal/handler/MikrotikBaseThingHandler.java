/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import static org.openhab.core.thing.ThingStatus.OFFLINE;
import static org.openhab.core.thing.ThingStatus.ONLINE;
import static org.openhab.core.thing.ThingStatusDetail.CONFIGURATION_ERROR;
import static org.openhab.core.types.RefreshType.REFRESH;

import java.lang.reflect.ParameterizedType;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mikrotik.internal.config.ConfigValidation;
import org.openhab.binding.mikrotik.internal.config.RouterosThingConfig;
import org.openhab.binding.mikrotik.internal.model.RouterosDevice;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ThingStatusInfoBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
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
public abstract class MikrotikBaseThingHandler<C extends ConfigValidation> extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(MikrotikBaseThingHandler.class);
    protected @Nullable C config;
    private @Nullable ScheduledFuture<?> refreshJob;
    protected ExpiringCache<Boolean> refreshCache = new ExpiringCache<>(Duration.ofDays(1), () -> false);
    protected Map<String, State> currentState = new HashMap<>();

    // public static boolean supportsThingType(ThingTypeUID thingTypeUID) <- in subclasses

    public MikrotikBaseThingHandler(Thing thing) {
        super(thing);
    }

    protected @Nullable MikrotikRouterosBridgeHandler getVerifiedBridgeHandler() {
        Bridge bridgeRef = getBridge();
        if (bridgeRef != null && bridgeRef.getHandler() != null
                && (bridgeRef.getHandler() instanceof MikrotikRouterosBridgeHandler)) {
            return (MikrotikRouterosBridgeHandler) bridgeRef.getHandler();
        }
        return null;
    }

    protected final @Nullable RouterosDevice getRouterOs() {
        MikrotikRouterosBridgeHandler bridgeHandler = getVerifiedBridgeHandler();
        return bridgeHandler == null ? null : bridgeHandler.getRouteros();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handling command = {} for channel = {}", command, channelUID);
        if (getThing().getStatus() == ONLINE) {
            RouterosDevice routeros = getRouterOs();
            if (routeros != null) {
                if (command == REFRESH) {
                    refreshCache.getValue();
                    refreshChannel(channelUID);
                } else {
                    try {
                        executeCommand(channelUID, command);
                    } catch (RuntimeException e) {
                        logger.warn("Unexpected error handling command = {} for channel = {} : {}", command, channelUID,
                                e.getMessage());
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initialize() {
        cancelRefreshJob();
        if (getVerifiedBridgeHandler() == null) {
            updateStatus(OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "This thing requires a RouterOS bridge");
            return;
        }

        var superKlass = (ParameterizedType) getClass().getGenericSuperclass();
        if (superKlass == null) {
            updateStatus(OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "getGenericSuperclass failed for thing handler");
            return;
        }
        Class<?> klass = (Class<?>) (superKlass.getActualTypeArguments()[0]);

        C localConfig = (C) getConfigAs(klass);
        this.config = localConfig;

        if (!localConfig.isValid()) {
            updateStatus(OFFLINE, CONFIGURATION_ERROR, String.format("%s is invalid", klass.getSimpleName()));
            return;
        }

        updateStatus(ONLINE);
    }

    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
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

    @SuppressWarnings("null")
    private void scheduleRefreshJob() {
        synchronized (this) {
            if (refreshJob == null) {
                var bridgeHandler = getVerifiedBridgeHandler();
                if (bridgeHandler == null) {
                    updateStatus(OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Cannot obtain bridge handler");
                    return;
                }
                RouterosThingConfig bridgeConfig = bridgeHandler.getBridgeConfig();
                int refreshPeriod = bridgeConfig.refresh;
                logger.debug("Scheduling refresh job every {}s", refreshPeriod);

                this.refreshCache = new ExpiringCache<>(Duration.ofSeconds(refreshPeriod), this::verifiedRefreshModels);
                refreshJob = scheduler.scheduleWithFixedDelay(this::scheduledRun, refreshPeriod, refreshPeriod,
                        TimeUnit.SECONDS);
            }
        }
    }

    private void cancelRefreshJob() {
        synchronized (this) {
            var job = this.refreshJob;
            if (job != null) {
                logger.debug("Cancelling refresh job");
                job.cancel(true);
                this.refreshJob = null;
                // Not setting to null as getValue() can potentially be called after
                this.refreshCache = new ExpiringCache<>(Duration.ofDays(1), () -> false);
            }
        }
    }

    private void scheduledRun() {
        MikrotikRouterosBridgeHandler bridgeHandler = getVerifiedBridgeHandler();
        if (bridgeHandler == null) {
            updateStatus(OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Failed reaching out to RouterOS bridge");
            return;
        }
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getStatus() == OFFLINE) {
            updateStatus(OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "The RouterOS bridge is currently offline");
            return;
        }

        if (getThing().getStatus() != ONLINE) {
            updateStatus(ONLINE);
        }
        logger.debug("Refreshing all {} channels", getThing().getUID());
        for (Channel channel : getThing().getChannels()) {
            try {
                refreshChannel(channel.getUID());
            } catch (RuntimeException e) {
                logger.warn("Unhandled exception while refreshing the {} Mikrotik thing", getThing().getUID(), e);
                updateStatus(OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    }

    protected final void refresh() throws ChannelUpdateException {
        if (getThing().getStatus() == ONLINE) {
            if (getRouterOs() != null) {
                refreshCache.getValue();
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

    protected boolean verifiedRefreshModels() {
        if (getRouterOs() != null && config != null) {
            refreshModels();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void dispose() {
        cancelRefreshJob();
    }

    protected abstract void refreshModels();

    protected abstract void refreshChannel(ChannelUID channelUID);

    protected abstract void executeCommand(ChannelUID channelUID, Command command);
}
