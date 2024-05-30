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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
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
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;

/**
 * The {@link PegelOnlineHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class PegelOnlineHandler extends BaseThingHandler {

    private static final String STATIONS_URI = "https://www.pegelonline.wsv.de/webservices/rest-api/v2/stations";
    private Optional<PegelOnlineConfiguration> configuration = Optional.empty();
    private Optional<ScheduledFuture<?>> schedule = Optional.empty();
    private HttpClient httpClient;
    private String stationUUID = UNKNOWN;
    private Optional<Measure> cache = Optional.empty();

    public PegelOnlineHandler(Thing thing, HttpClient hc) {
        super(thing);
        httpClient = hc;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            if (cache.isPresent()) {
                Measure m = cache.get();
                if (MEASURE_CHANNEL.equals(channelUID.getId())) {
                    updateChannelState(MEASURE_CHANNEL,
                            QuantityType.valueOf(m.value, MetricPrefix.CENTI(SIUnits.METRE)));
                } else if (TREND_CHANNEL.equals(channelUID.getId())) {
                    updateChannelState(TREND_CHANNEL, DecimalType.valueOf(Integer.toString(m.trend)));
                } else if (TIMESTAMP_CHANNEL.equals(channelUID.getId())) {
                    updateChannelState(TIMESTAMP_CHANNEL, DateTimeType.valueOf(m.timestamp));
                } else if (WARNING_CHANNEL.equals(channelUID.getId())) {
                    updateChannelState(WARNING_CHANNEL, DecimalType.valueOf(Integer.toString(getWarnLevel(m))));
                }
            }
        }
    }

    private void measure() {
        try {
            ContentResponse cr = httpClient.GET(STATIONS_URI + "/" + stationUUID + "/W/currentmeasurement.json");
            Measure m = GSON.fromJson(cr.getContentAsString(), Measure.class);
            if (m != null) {
                updateChannels(m);
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    public void updateChannels(Measure m) {
        cache = Optional.of(m);
        updateChannelState(TIMESTAMP_CHANNEL, DateTimeType.valueOf(m.timestamp));
        updateChannelState(MEASURE_CHANNEL, QuantityType.valueOf(m.value, MetricPrefix.CENTI(SIUnits.METRE)));
        updateChannelState(TREND_CHANNEL, DecimalType.valueOf(Integer.toString(m.trend)));
        updateChannelState(WARNING_CHANNEL, DecimalType.valueOf(Integer.toString(getWarnLevel(m))));
    }

    int getWarnLevel(Measure m) {
        if (m.value < configuration.get().warningLevel1) {
            return NO_WARNING;
        } else if (m.value < configuration.get().warningLevel2) {
            return WARN_LEVEL_1;
        } else if (m.value < configuration.get().warningLevel3) {
            return WARN_LEVEL_2;
        } else if (m.value < configuration.get().hq10) {
            return WARN_LEVEL_3;
        } else if (m.value > configuration.get().hq100) {
            return HQ10;
        } else if (m.value > configuration.get().hqhqExtereme) {
            return HQ100;
        } else {
            return HQ_EXTREME;
        }
    }

    @Override
    public void initialize() {
        PegelOnlineConfiguration config = getConfigAs(PegelOnlineConfiguration.class);
        configuration = Optional.of(config);
        stationUUID = configuration.get().uuid;
        schedule = Optional.of(scheduler.scheduleWithFixedDelay(this::measure, 0, configuration.get().refreshInterval,
                TimeUnit.MINUTES));
    }

    @Override
    public void dispose() {
        if (schedule.isPresent()) {
            schedule.get().cancel(true);
        }
        schedule = Optional.empty();
    }

    @Override
    public void updateConfiguration(Configuration configuration) {
        super.updateConfiguration(configuration);
    }

    private void updateChannelState(String channel, State st) {
        updateState(new ChannelUID(thing.getUID(), channel), st);
    }
}
