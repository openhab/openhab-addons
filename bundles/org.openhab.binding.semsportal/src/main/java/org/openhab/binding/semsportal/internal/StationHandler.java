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
package org.openhab.binding.semsportal.internal;

import static org.openhab.binding.semsportal.internal.SEMSPortalBindingConstants.*;

import java.time.LocalDateTime;
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
 */
@NonNullByDefault
public class StationHandler extends BaseThingHandler {
    private Logger logger = LoggerFactory.getLogger(StationHandler.class);
    private static final long MAX_STATUS_AGE_MINUTES = 1;

    private @Nullable StationStatus currentStatus;
    private LocalDateTime lastUpdate = LocalDateTime.MIN;

    public StationHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (isPortalOK()) {
            if (command instanceof RefreshType) {
                scheduler.execute(() -> {
                    ensureRecentStatus();
                    updateChannelState(channelUID);
                });
            }
        }
    }

    private boolean isPortalOK() {
        PortalHandler portal = getPortal();
        return portal != null && portal.isLoggedIn();
    }

    private void updateChannelState(ChannelUID channelUID) {
        if (!isPortalOK()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                    "Unable to update station info. Check Bridge status for details.");
            return;
        }
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

    private void ensureRecentStatus() {
        if (lastUpdate.isBefore(LocalDateTime.now().minusMinutes(MAX_STATUS_AGE_MINUTES))) {
            updateStation();
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(() -> {
            try {
                scheduler.scheduleWithFixedDelay(() -> ensureRecentStatus(), 0, getUpdateInterval(), TimeUnit.MINUTES);
            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                        "Unable to update station info. Check Bridge status for details.");
            }
        });
    }

    private long getUpdateInterval() {
        PortalHandler portal = getPortal();
        if (portal == null) {
            return SEMSPortalBindingConstants.DEFAULT_UPDATE_INTERVAL_MINUTES;
        }
        return portal.getUpdateInterval();
    }

    private void updateStation() {
        if (!isPortalOK()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                    "Unable to update station info. Check Bridge status for details.");
            return;
        }
        PortalHandler portal = getPortal();
        if (portal != null) {
            try {
                currentStatus = portal.getStationStatus(getStationUUID());
                StationStatus localCurrentStatus = currentStatus;
                if (localCurrentStatus != null && localCurrentStatus.isOperational()) {
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.NONE, "Station not operational");
                }
                updateAllChannels();
            } catch (CommunicationException commEx) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, commEx.getMessage());
            } catch (ConfigurationException confEx) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, confEx.getMessage());
            }
        } else {
            logger.debug("Unable to find portal for thing {}", getThing().getUID());
        }
    }

    private String getStationUUID() {
        String uuid = getThing().getProperties().get(STATION_UUID);
        if (uuid == null) {
            Object uuidObj = getThing().getConfiguration().get(STATION_UUID);
            if (uuidObj instanceof String) {
                uuid = (String) uuidObj;
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
