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
package org.openhab.binding.semsportal.internal;

import static org.openhab.binding.semsportal.internal.SEMSPortalBindingConstants.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.semsportal.internal.dto.StationStatus;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link StationHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Iwan Bron - Initial contribution
 * @author Julio Vilmar Gesser - Refresh task lifecycle and recovery
 */
@NonNullByDefault
public class StationHandler extends BaseThingHandler {
    private Logger logger = LoggerFactory.getLogger(StationHandler.class);
    private static final long MAX_STATUS_AGE_MINUTES = 1;

    private @Nullable ScheduledFuture<?> refreshStatusTask;

    private volatile @Nullable StationStatus currentStatus;
    private volatile boolean disposed;

    // all guarded by "this"
    private Instant lastUpdate = Instant.EPOCH;
    private boolean refreshing;
    // Changes when the handler is initialized or disposed: the result of a refresh that started in a previous
    // generation is dropped
    private int generation;

    public StationHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            scheduler.execute(() -> {
                // when the status is being fetched, or is recent, the channel gets it from there
                if (!disposed && !refresh(false) && currentStatus != null) {
                    updateChannelState(channelUID);
                }
            });
        }
    }

    private void updateChannelState(ChannelUID channelUID) {
        switch (channelUID.getId()) {
            case CHANNEL_CURRENT_OUTPUT:
                updateState(channelUID.getId(), StateHelper.getCurrentOutput(currentStatus));
                break;
            case CHANNEL_TODAY_TOTAL:
                updateState(channelUID.getId(), StateHelper.getDayTotal(currentStatus));
                break;
            case CHANNEL_MONTH_TOTAL:
                updateState(channelUID.getId(), StateHelper.getMonthTotal(currentStatus));
                break;
            case CHANNEL_OVERALL_TOTAL:
                updateState(channelUID.getId(), StateHelper.getOverallTotal(currentStatus));
                break;
            case CHANNEL_TODAY_INCOME:
                updateState(channelUID.getId(), StateHelper.getDayIncome(currentStatus));
                break;
            case CHANNEL_TOTAL_INCOME:
                updateState(channelUID.getId(), StateHelper.getTotalIncome(currentStatus));
                break;
            case CHANNEL_LASTUPDATE:
                updateState(channelUID.getId(), StateHelper.getLastUpdate(currentStatus));
                break;
            default:
                logger.debug("No mapping found for channel {}", channelUID.getId());
        }
    }

    @Override
    public void initialize() {
        synchronized (this) {
            disposed = false;
            generation++;
            refreshing = false;
            currentStatus = null;
            lastUpdate = Instant.EPOCH;
        }
        updateStatus(ThingStatus.UNKNOWN);
        startTasks();
    }

    private void startTasks() {
        synchronized (this) {
            if (refreshStatusTask == null && !disposed) {
                try {
                    refreshStatusTask = scheduler.scheduleWithFixedDelay(() -> refresh(true), 0, getUpdateInterval(),
                            TimeUnit.MINUTES);
                } catch (Exception e) {
                    updateStatus(ThingStatusDetail.COMMUNICATION_ERROR,
                            "Unable to update station info. Check Bridge status for details.");
                }
            }
        }
    }

    /**
     * Stops the periodic task. A fetch that is running is cancelled, and its result is dropped.
     */
    private void stopTasks() {
        synchronized (this) {
            generation++;
            refreshing = false;
            ScheduledFuture<?> task = refreshStatusTask;
            if (task != null) {
                task.cancel(true);
                refreshStatusTask = null;
            }
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            startTasks();
        } else if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE
                && bridgeStatusInfo.getStatusDetail() != ThingStatusDetail.COMMUNICATION_ERROR) {
            stopTasks();
        }
    }

    @Override
    public void dispose() {
        synchronized (this) {
            disposed = true;
            stopTasks();
        }
    }

    private long getUpdateInterval() {
        PortalHandler portal = getPortal();
        if (portal == null) {
            return SEMSPortalBindingConstants.DEFAULT_UPDATE_INTERVAL_MINUTES;
        }
        return portal.getUpdateInterval();
    }

    /**
     * Fetches the status of the station and publishes it, unless the status is being fetched already, or it is recent
     * and the fetch is not forced. That keeps the REFRESH commands sent for every linked channel when openHAB starts,
     * and the periodic task, from each fetching it again.
     *
     * @param force fetch also when the status is recent
     * @return whether the status was fetched
     */
    private boolean refresh(boolean force) {
        int started;
        synchronized (this) {
            if (disposed || refreshing || (!force
                    && lastUpdate.isAfter(Instant.now().minus(MAX_STATUS_AGE_MINUTES, ChronoUnit.MINUTES)))) {
                return false;
            }
            refreshing = true;
            started = generation;
        }
        Result result = fetch();
        boolean current;
        // The next refresh can start before the result is published
        synchronized (this) {
            current = started == generation;
            if (current) {
                refreshing = false;
                lastUpdate = result.error == null ? Instant.now() : Instant.EPOCH;
            }
        }
        if (current) {
            publish(result);
        }
        return true;
    }

    private Result fetch() {
        PortalHandler portal = getPortal();
        if (portal == null) {
            logger.debug("Unable to find portal for thing {}", getThing().getUID());
            return Result.failure(ThingStatusDetail.BRIDGE_OFFLINE,
                    "Unable to update station info. Check Bridge status for details.");
        }
        try {
            return Result.success(portal.getStationStatus(getStationUUID()));
        } catch (CommunicationException commEx) {
            return Result.failure(ThingStatusDetail.COMMUNICATION_ERROR, commEx.getMessage());
        } catch (ConfigurationException confEx) {
            return Result.failure(ThingStatusDetail.CONFIGURATION_ERROR, confEx.getMessage());
        } catch (RuntimeException e) {
            logger.debug("Unexpected error when updating the station", e);
            return Result.failure(ThingStatusDetail.COMMUNICATION_ERROR, "Unexpected error when updating the station");
        }
    }

    private void publish(Result result) {
        ThingStatusDetail error = result.error;
        if (error != null) {
            updateStatus(error, result.message);
            return;
        }
        StationStatus status = result.status;
        currentStatus = status;
        if (status != null && status.isOperational()) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, "Station not operational");
        }
        updateAllChannels();
    }

    /**
     * What the portal gave: the status, or why there is none.
     */
    private record Result(@Nullable StationStatus status, @Nullable ThingStatusDetail error, @Nullable String message) {
        static Result success(@Nullable StationStatus status) {
            return new Result(status, null, null);
        }

        static Result failure(ThingStatusDetail error, @Nullable String message) {
            return new Result(null, error, message);
        }
    }

    private String getStationUUID() {
        String uuid = getThing().getProperties().get(STATION_UUID);
        if (uuid == null) {
            Object uuidObj = getConfig().get(STATION_UUID);
            if (uuidObj instanceof String stringValue) {
                uuid = stringValue;
            }
        }
        return uuid == null ? "" : uuid;
    }

    private void updateAllChannels() {
        for (String channelName : ALL_CHANNELS) {
            Channel channel = thing.getChannel(channelName);
            if (channel != null) {
                updateChannelState(channel.getUID());
            }
        }
    }

    private @Nullable PortalHandler getPortal() {
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() != null && bridge.getHandler() instanceof PortalHandler) {
            return (PortalHandler) bridge.getHandler();
        }
        return null;
    }
}
