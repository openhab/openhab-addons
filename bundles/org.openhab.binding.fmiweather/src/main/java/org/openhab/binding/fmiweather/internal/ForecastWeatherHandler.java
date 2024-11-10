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
package org.openhab.binding.fmiweather.internal;

import static org.openhab.binding.fmiweather.internal.BindingConstants.*;
import static org.openhab.binding.fmiweather.internal.client.ForecastRequest.*;
import static org.openhab.core.library.unit.SIUnits.CELSIUS;
import static org.openhab.core.library.unit.Units.*;
import static org.openhab.core.types.TimeSeries.Policy.REPLACE;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.fmiweather.internal.client.Data;
import org.openhab.binding.fmiweather.internal.client.FMIRequest;
import org.openhab.binding.fmiweather.internal.client.FMIResponse;
import org.openhab.binding.fmiweather.internal.client.ForecastRequest;
import org.openhab.binding.fmiweather.internal.client.LatLon;
import org.openhab.binding.fmiweather.internal.client.Location;
import org.openhab.binding.fmiweather.internal.client.exception.FMIUnexpectedResponseException;
import org.openhab.binding.fmiweather.internal.config.ForecastConfiguration;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.TimeSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ForecastWeatherHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class ForecastWeatherHandler extends AbstractWeatherHandler {

    private final Logger logger = LoggerFactory.getLogger(ForecastWeatherHandler.class);

    private static final String GROUP_FORECAST_NOW = "forecastNow";
    private static final int QUERY_RESOLUTION_MINUTES = 20; // The channel group hours should be divisible by this
    // Hirlam horizon is 54h https://ilmatieteenlaitos.fi/avoin-data-avattavat-aineistot (in Finnish)
    private static final int FORECAST_HORIZON_HOURS = 50; // should be divisible by QUERY_RESOLUTION_MINUTES
    private static final Map<String, Map.Entry<String, @Nullable Unit<?>>> CHANNEL_TO_FORECAST_FIELD_NAME_AND_UNIT = new HashMap<>(
            9);

    private static void addMapping(String channelId, String requestField, @Nullable Unit<?> unit) {
        CHANNEL_TO_FORECAST_FIELD_NAME_AND_UNIT.put(channelId,
                new AbstractMap.SimpleImmutableEntry<>(requestField, unit));
    }

    static {
        addMapping(CHANNEL_TEMPERATURE, PARAM_TEMPERATURE, CELSIUS);
        addMapping(CHANNEL_HUMIDITY, PARAM_HUMIDITY, PERCENT);
        addMapping(CHANNEL_WIND_DIRECTION, PARAM_WIND_DIRECTION, DEGREE_ANGLE);
        addMapping(CHANNEL_WIND_SPEED, PARAM_WIND_SPEED, METRE_PER_SECOND);
        addMapping(CHANNEL_GUST, PARAM_WIND_GUST, METRE_PER_SECOND);
        addMapping(CHANNEL_PRESSURE, PARAM_PRESSURE, MILLIBAR);
        addMapping(CHANNEL_PRECIPITATION_INTENSITY, PARAM_PRECIPITATION_1H, MILLIMETRE_PER_HOUR);
        addMapping(CHANNEL_TOTAL_CLOUD_COVER, PARAM_TOTAL_CLOUD_COVER, PERCENT);
        addMapping(CHANNEL_FORECAST_WEATHER_ID, PARAM_WEATHER_SYMBOL, null);
    }

    private @NonNullByDefault({}) LatLon location;
    private String query = "";

    public ForecastWeatherHandler(final Thing thing, final HttpClient httpClient) {
        super(thing, httpClient);
        // Override poll interval to slower value
        pollIntervalSeconds = (int) TimeUnit.MINUTES.toSeconds(QUERY_RESOLUTION_MINUTES);
    }

    @Override
    public void initialize() {
        ForecastConfiguration config = getConfigAs(ForecastConfiguration.class);
        String location = config.location;
        if (location.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "location parameter not set");
            return;
        }
        String[] split = location.split(",");
        if (split.length != 2) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, String.format(
                    "location parameter should have latitude and longitude separated by comma (LATITUDE,LONGITUDE). Found %d values instead",
                    split.length));
            return;
        }
        this.location = new LatLon(new BigDecimal(split[0].trim()), new BigDecimal(split[1].trim()));
        query = config.query;

        super.initialize();
    }

    @Override
    public void dispose() {
        super.dispose();
        this.location = null;
    }

    @Override
    protected FMIRequest getRequest() {
        long now = Instant.now().getEpochSecond();
        return new ForecastRequest(location, query, floorToEvenMinutes(now, QUERY_RESOLUTION_MINUTES),
                ceilToEvenMinutes(now + TimeUnit.HOURS.toSeconds(FORECAST_HORIZON_HOURS), QUERY_RESOLUTION_MINUTES),
                QUERY_RESOLUTION_MINUTES);
    }

    @Override
    protected void updateChannels() {
        FMIResponse response = this.response;
        if (response == null) {
            return;
        }
        try {
            Location location = unwrap(response.getLocations().stream().findFirst(),
                    "No locations in response -- no data? Aborting");
            Map<String, String> properties = editProperties();
            properties.put(PROP_NAME, location.name);
            properties.put(PROP_LATITUDE, location.latitude.toPlainString());
            properties.put(PROP_LONGITUDE, location.longitude.toPlainString());
            updateProperties(properties);
            updateHourlyChannels(response, location);
            updateTimeSeriesChannels(response, location);
            updateStatus(ThingStatus.ONLINE);
        } catch (FMIUnexpectedResponseException e) {
            // Unexpected (possibly bug) issue with response
            logger.warn("Unexpected response encountered: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    String.format("Unexpected API response: %s", e.getMessage()));
        }
    }

    private void updateHourlyChannels(FMIResponse response, Location location) throws FMIUnexpectedResponseException {
        for (Channel channel : getThing().getChannels()) {
            ChannelUID channelUID = channel.getUID();
            if (CHANNEL_GROUP_FORECAST.equals(channelUID.getGroupId())) {
                // Skip time series group
                continue;
            }
            int hours = getHours(channelUID);
            int timeIndex = getTimeIndex(hours);
            if (channelUID.getIdWithoutGroup().equals(CHANNEL_TIME)) {
                // All parameters and locations should share the same timestamps. We use temperature to figure out
                // timestamp for the group of channels
                final String field = ForecastRequest.PARAM_TEMPERATURE;
                Data data = unwrap(response.getData(location, field),
                        "Field %s not present for location %s in response. Bug?", field, location);
                updateEpochSecondStateIfLinked(channelUID, data.timestampsEpochSecs[timeIndex]);
            } else {
                String field = getDataField(channelUID);
                Unit<?> unit = getUnit(channelUID);
                if (field == null) {
                    logger.error("Channel {} not handled. Bug?", channelUID.getId());
                    continue;
                }
                Data data = unwrap(response.getData(location, field),
                        "Field %s not present for location %s in response. Bug?", field, location);
                updateStateIfLinked(channelUID, data.values[timeIndex], unit);
            }
        }
    }

    private void updateTimeSeriesChannels(FMIResponse response, Location location)
            throws FMIUnexpectedResponseException {
        for (Channel channel : getThing().getChannelsOfGroup(CHANNEL_GROUP_FORECAST)) {
            ChannelUID channelUID = channel.getUID();
            if (CHANNEL_TIME.equals(channelUID.getIdWithoutGroup())) {
                // All parameters and locations should share the same timestamps. We use temperature to figure out
                // timestamp for the group of channels
                final String field = ForecastRequest.PARAM_TEMPERATURE;
                Data data = unwrap(response.getData(location, field),
                        "Field %s not present for location %s in response. Bug?", field, location);
                updateEpochSecondStateIfLinked(channelUID, data.timestampsEpochSecs[0]);
                continue;
            }
            String field = getDataField(channelUID);
            Unit<?> unit = getUnit(channelUID);
            if (field == null) {
                logger.error("Channel {} not handled. Bug?", channelUID.getId());
                continue;
            }
            Data data = unwrap(response.getData(location, field),
                    "Field %s not present for location %s in response. Bug?", field, location);
            if (data.values.length != data.timestampsEpochSecs.length) {
                logger.warn("Number of values ({}) doesn't match number of timestamps ({})", data.values.length,
                        data.timestampsEpochSecs.length);
                continue;
            }
            updateStateIfLinked(channelUID, data.values[0], unit);
            TimeSeries timeSeries = new TimeSeries(REPLACE);
            for (int i = 0; i < data.values.length; i++) {
                timeSeries.add(Instant.ofEpochSecond(data.timestampsEpochSecs[i]), getState(data.values[i], unit));
            }
            sendTimeSeries(channelUID, timeSeries);
        }
    }

    private static int getHours(ChannelUID uid) {
        String groupId = uid.getGroupId();
        if (groupId == null) {
            throw new IllegalStateException("All channels should be in group!");
        }
        if (GROUP_FORECAST_NOW.equals(groupId)) {
            return 0;
        } else {
            return Integer.valueOf(groupId.substring(groupId.length() - 2));
        }
    }

    private static int getTimeIndex(int hours) {
        return (int) (TimeUnit.HOURS.toMinutes(hours) / QUERY_RESOLUTION_MINUTES);
    }

    private static @Nullable String getDataField(ChannelUID channelUID) {
        Entry<String, @Nullable Unit<?>> entry = CHANNEL_TO_FORECAST_FIELD_NAME_AND_UNIT
                .get(channelUID.getIdWithoutGroup());
        if (entry == null) {
            return null;
        }
        return entry.getKey();
    }

    private static @Nullable Unit<?> getUnit(ChannelUID channelUID) {
        Entry<String, @Nullable Unit<?>> entry = CHANNEL_TO_FORECAST_FIELD_NAME_AND_UNIT
                .get(channelUID.getIdWithoutGroup());
        if (entry == null) {
            return null;
        }
        return entry.getValue();
    }
}
