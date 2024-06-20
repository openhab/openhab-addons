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
package org.openhab.binding.pegelonline.internal.handler;

import static org.openhab.binding.pegelonline.internal.PegelOnlineBindingConstants.*;

import java.util.Optional;
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
    private Optional<PegelOnlineConfiguration> configuration = Optional.empty();
    private Optional<ScheduledFuture<?>> schedule = Optional.empty();
    private Optional<Measure> cache = Optional.empty();
    private TreeMap<Integer, Integer> warnMap = new TreeMap<>();
    private String stationUUID = UNKNOWN;
    private HttpClient httpClient;

    public PegelOnlineHandler(Thing thing, HttpClient hc) {
        super(thing);
        httpClient = hc;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            if (cache.isPresent()) {
                Measure m = cache.get();
                if (LEVEL_CHANNEL.equals(channelUID.getId())) {
                    updateChannelState(LEVEL_CHANNEL, QuantityType.valueOf(m.value, MetricPrefix.CENTI(SIUnits.METRE)));
                } else if (TREND_CHANNEL.equals(channelUID.getId())) {
                    updateChannelState(TREND_CHANNEL, DecimalType.valueOf(Integer.toString(m.trend)));
                } else if (TIMESTAMP_CHANNEL.equals(channelUID.getId())) {
                    updateChannelState(TIMESTAMP_CHANNEL, DateTimeType.valueOf(m.timestamp));
                } else if (WARNING_CHANNEL.equals(channelUID.getId())) {
                    updateChannelState(WARNING_CHANNEL,
                            DecimalType.valueOf(Integer.toString(warnMap.floorEntry((int) m.value).getValue())));
                }
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
        configuration = Optional.of(config);
        String description = "@text/pegelonline.handler.status.wait-feedback";
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, description);
        schedule = Optional.of(scheduler.scheduleWithFixedDelay(this::performMeasurement, 0,
                configuration.get().refreshInterval, TimeUnit.MINUTES));
    }

    @Override
    public void dispose() {
        warnMap.clear();
        if (schedule.isPresent()) {
            schedule.get().cancel(true);
        }
        schedule = Optional.empty();
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
        cache = Optional.of(measureDto);
        updateChannelState(TIMESTAMP_CHANNEL, DateTimeType.valueOf(measureDto.timestamp));
        updateChannelState(LEVEL_CHANNEL, QuantityType.valueOf(measureDto.value, MetricPrefix.CENTI(SIUnits.METRE)));
        updateChannelState(TREND_CHANNEL, DecimalType.valueOf(Integer.toString(measureDto.trend)));
        updateChannelState(WARNING_CHANNEL,
                DecimalType.valueOf(Integer.toString(warnMap.floorEntry((int) measureDto.value).getValue())));
    }

    private void updateChannelState(String channel, State st) {
        updateState(new ChannelUID(thing.getUID(), channel), st);
    }
}
