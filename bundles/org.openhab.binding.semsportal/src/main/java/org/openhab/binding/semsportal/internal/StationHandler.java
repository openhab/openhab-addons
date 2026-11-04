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
    private Instant lastUpdate = Instant.EPOCH;

    public StationHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            scheduler.execute(() -> {
                if (claimRefresh()) {
                    // updates all the channels
                    updateStation();
                } else if (currentStatus != null) {
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

    /**
     * Claims the right to fetch the status when the one we have is too old. Claiming before fetching keeps the
     * REFRESH commands sent for every linked channel from each fetching it again; when the fetch is still running they
     * find the status in place once it is done.
     */
    private boolean claimRefresh() {
        Instant now = Instant.now();
        synchronized (this) {
            if (lastUpdate.isAfter(now.minus(MAX_STATUS_AGE_MINUTES, ChronoUnit.MINUTES))) {
                return false;
            }
            lastUpdate = now;
            return true;
        }
    }

    private void setLastUpdate(Instant lastUpdate) {
        synchronized (this) {
            this.lastUpdate = lastUpdate;
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        startTasks();
    }

    private void startTasks() {
        synchronized (this) {
            if (refreshStatusTask == null) {
                try {
                    refreshStatusTask = scheduler.scheduleWithFixedDelay(() -> updateStation(), 0, getUpdateInterval(),
                            TimeUnit.MINUTES);
                } catch (Exception e) {
                    updateStatus(ThingStatusDetail.COMMUNICATION_ERROR,
                            "Unable to update station info. Check Bridge status for details.");
                }
            }
        }
    }

    private void stopTasks() {
        synchronized (this) {
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
        stopTasks();
    }

    private long getUpdateInterval() {
        PortalHandler portal = getPortal();
        if (portal == null) {
            return SEMSPortalBindingConstants.DEFAULT_UPDATE_INTERVAL_MINUTES;
        }
        return portal.getUpdateInterval();
    }

    private boolean updateStation() {
        PortalHandler portal = getPortal();
        if (portal != null) {
            try {
                StationStatus status = portal.getStationStatus(getStationUUID());
                currentStatus = status;
                setLastUpdate(Instant.now());
                if (status != null && status.isOperational()) {
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, "Station not operational");
                }
                updateAllChannels();
                return true;
            } catch (CommunicationException commEx) {
                setLastUpdate(Instant.EPOCH);
                updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, commEx.getMessage());
            } catch (ConfigurationException confEx) {
                setLastUpdate(Instant.EPOCH);
                updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, confEx.getMessage());
            }
        } else {
            logger.debug("Unable to find portal for thing {}", getThing().getUID());
            setLastUpdate(Instant.EPOCH);
            updateStatus(ThingStatusDetail.BRIDGE_OFFLINE,
                    "Unable to update station info. Check Bridge status for details.");
        }
        return false;
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
