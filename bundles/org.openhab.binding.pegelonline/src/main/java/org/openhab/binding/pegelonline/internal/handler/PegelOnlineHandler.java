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
package org.openhab.binding.pegelonline.internal.handler;

import static org.openhab.binding.pegelonline.internal.PegelOnlineBindingConstants.*;

import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.pegelonline.internal.config.PegelOnlineConfiguration;
import org.openhab.binding.pegelonline.internal.dto.Measure;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PegelOnlineHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class PegelOnlineHandler extends BaseThingHandler {
    private static final String STATIONS_URI = "https://www.pegelonline.wsv.de/webservices/rest-api/v2/stations";
    private final Logger logger = LoggerFactory.getLogger(PegelOnlineHandler.class);
    private final HttpClient httpClient;
    private @Nullable ScheduledFuture<?> schedule;
    private @Nullable Measure cache;
    private TreeMap<Integer, Integer> warnMap = new TreeMap<>();
    private String stationUUID = UNKNOWN;

    public PegelOnlineHandler(Thing thing, HttpClient hc) {
        super(thing);
        httpClient = hc;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Measure measure = cache;
        if (command instanceof RefreshType && measure != null) {
            String channelId = channelUID.getId();
            if (LEVEL_CHANNEL.equals(channelId)) {
                updateChannelState(LEVEL_CHANNEL,
                        QuantityType.valueOf(measure.value, MetricPrefix.CENTI(SIUnits.METRE)));
            } else if (TREND_CHANNEL.equals(channelId)) {
                updateChannelState(TREND_CHANNEL, DecimalType.valueOf(Integer.toString(measure.trend)));
            } else if (TIMESTAMP_CHANNEL.equals(channelId)) {
                updateChannelState(TIMESTAMP_CHANNEL, DateTimeType.valueOf(measure.timestamp));
            } else if (WARNING_CHANNEL.equals(channelId)) {
                updateWarningChannel(measure.value);
            }
        }
    }

    @Override
    public void initialize() {
        PegelOnlineConfiguration config = getConfigAs(PegelOnlineConfiguration.class);
        stationUUID = config.uuid;
        if (!config.uuidCheck()) {
            String description = "@text/pegelonline.handler.status.uuid [\"" + stationUUID + "\"]";
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, description);
            return;
        }
        if (!config.warningCheck()) {
            String description = "@text/pegelonline.handler.status.warning";
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, description);
            return;
        }
        warnMap = config.getWarnings();
        String description = "@text/pegelonline.handler.status.wait-feedback";
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, description);
        schedule = scheduler.scheduleWithFixedDelay(this::performMeasurement, 0, config.refreshInterval,
                TimeUnit.MINUTES);
    }

    @Override
    public void dispose() {
        warnMap.clear();
        ScheduledFuture<?> schedule = this.schedule;
        if (schedule != null) {
            schedule.cancel(true);
        }
        this.schedule = null;
    }

    @Override
    public void updateConfiguration(Configuration configuration) {
        super.updateConfiguration(configuration);
    }

    void performMeasurement() {
        try {
            ContentResponse cr = httpClient.GET(STATIONS_URI + "/" + stationUUID + "/W/currentmeasurement.json");
            int responseStatus = cr.getStatus();
            if (responseStatus == 200) {
                String content = cr.getContentAsString();
                Measure measureDto = GSON.fromJson(content, Measure.class);
                if (isValid(measureDto) && measureDto != null) {
                    updateStatus(ThingStatus.ONLINE);
                    updateChannels(measureDto);
                } else {
                    String description = "@text/pegelonline.handler.status.json-error [\"" + content + "\"]";
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, description);
                }
            } else if (responseStatus == 404) {
                // 404 respoonse shows station isn't found
                String description = "@text/pegelonline.handler.status.uuid-not-found [\"" + stationUUID + "\"]";
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, description);
            } else {
                String description = "@text/pegelonline.handler.status.http-status [\"" + responseStatus + "\"]";
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, description);
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            String description = "@text/pegelonline.handler.status.http-exception [\"" + e.getMessage() + "\"]";
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, description);
        }
    }

    private boolean isValid(@Nullable Measure measureDto) {
        if (measureDto != null) {
            if (measureDto.timestamp != null) {
                try {
                    DateTimeType.valueOf(measureDto.timestamp);
                    return true;
                } catch (Exception e) {
                    logger.trace("Error converting {} into DateTime: {}", measureDto.timestamp, e.getMessage());
                }
            }
        }
        return false;
    }

    private void updateChannels(Measure measureDto) {
        cache = measureDto;
        updateChannelState(TIMESTAMP_CHANNEL, DateTimeType.valueOf(measureDto.timestamp));
        updateChannelState(LEVEL_CHANNEL, QuantityType.valueOf(measureDto.value, MetricPrefix.CENTI(SIUnits.METRE)));
        updateChannelState(TREND_CHANNEL, DecimalType.valueOf(Integer.toString(measureDto.trend)));
        updateWarningChannel(measureDto.value);
    }

    private void updateWarningChannel(double level) {
        Entry<Integer, Integer> warnLevelEntry = warnMap.floorEntry((int) level);
        if (warnLevelEntry != null) {
            updateChannelState(WARNING_CHANNEL, new DecimalType(warnLevelEntry.getValue()));
        }
    }

    private void updateChannelState(String channel, State st) {
        updateState(new ChannelUID(thing.getUID(), channel), st);
    }
}
