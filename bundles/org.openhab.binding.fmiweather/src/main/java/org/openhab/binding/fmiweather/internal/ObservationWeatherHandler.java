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
package org.openhab.binding.fmiweather.internal;

import static org.eclipse.smarthome.core.library.unit.SIUnits.*;
import static org.eclipse.smarthome.core.library.unit.SmartHomeUnits.*;
import static org.openhab.binding.fmiweather.internal.BindingConstants.*;
import static org.openhab.binding.fmiweather.internal.client.ObservationRequest.*;

import java.time.Instant;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;
import javax.measure.quantity.Length;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.unit.MetricPrefix;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.fmiweather.internal.client.Data;
import org.openhab.binding.fmiweather.internal.client.FMIResponse;
import org.openhab.binding.fmiweather.internal.client.FMISID;
import org.openhab.binding.fmiweather.internal.client.Location;
import org.openhab.binding.fmiweather.internal.client.ObservationRequest;
import org.openhab.binding.fmiweather.internal.client.exception.FMIResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ObservationWeatherHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class ObservationWeatherHandler extends AbstractWeatherHandler {

    private final Logger logger = LoggerFactory.getLogger(ObservationWeatherHandler.class);
    private static final long OBSERVATION_LOOK_BACK_SECONDS = TimeUnit.MINUTES.toSeconds(30);
    private static final int STEP_MINUTES = 10;
    private static final int POLL_INTERVAL_SECONDS = 600;

    public static final Unit<Length> MILLIMETRE = MetricPrefix.MILLI(METRE);
    public static final Unit<Length> CENTIMETRE = MetricPrefix.CENTI(METRE);

    private static final Map<String, Map.Entry<String, @Nullable Unit<?>>> CHANNEL_TO_OBSERVATION_FIELD_NAME_AND_UNIT = new HashMap<>(
            11);

    private static void addMapping(String channelId, String requestField, @Nullable Unit<?> unit) {
        CHANNEL_TO_OBSERVATION_FIELD_NAME_AND_UNIT.put(channelId,
                new AbstractMap.SimpleImmutableEntry<>(requestField, unit));
    }

    static {
        addMapping(CHANNEL_TEMPERATURE, PARAM_TEMPERATURE, CELSIUS);
        addMapping(CHANNEL_HUMIDITY, PARAM_HUMIDITY, PERCENT);
        addMapping(CHANNEL_WIND_DIRECTION, PARAM_WIND_DIRECTION, DEGREE_ANGLE);
        addMapping(CHANNEL_WIND_SPEED, PARAM_WIND_SPEED, METRE_PER_SECOND);
        addMapping(CHANNEL_GUST, PARAM_WIND_GUST, METRE_PER_SECOND);
        addMapping(CHANNEL_PRESSURE, PARAM_PRESSURE, MILLIBAR);
        addMapping(CHANNEL_PRECIPITATION_AMOUNT, PARAM_PRECIPITATION_AMOUNT, MILLIMETRE);
        addMapping(CHANNEL_SNOW_DEPTH, PARAM_SNOW_DEPTH, CENTIMETRE);
        addMapping(CHANNEL_VISIBILITY, PARAM_VISIBILITY, METRE);
        addMapping(CHANNEL_CLOUDS, PARAM_CLOUDS, null);
        addMapping(CHANNEL_OBSERVATION_PRESENT_WEATHER, PARAM_PRESENT_WEATHER, null);
    }

    private @NonNullByDefault({}) String fmisid;

    public ObservationWeatherHandler(Thing thing) {
        super(thing);
        pollIntervalSeconds = POLL_INTERVAL_SECONDS;
    }

    @Override
    public void initialize() {
        fmisid = Objects.toString(getConfig().get(BindingConstants.FMISID), null);
        if (fmisid == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    String.format("%s parameter not set", FMISID));
        } else {
            super.initialize();
        }
    }

    @Override
    protected void update() {
        for (int retry = 0; retry < RETRIES; retry++) {
            try {
                long now = Instant.now().getEpochSecond();
                response = client.query(new ObservationRequest(new FMISID(fmisid),
                        floorToEvenMinutes(now - OBSERVATION_LOOK_BACK_SECONDS, STEP_MINUTES),
                        ceilToEvenMinutes(now, STEP_MINUTES), STEP_MINUTES), TIMEOUT_MILLIS);
            } catch (FMIResponseException e) {
                response = null;
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        String.format("%s: %s", e.getClass().getName(), e.getMessage()));
                try {
                    Thread.sleep(RETRY_DELAY_MILLIS);
                } catch (InterruptedException e1) {
                    break;
                }
                continue;
            }
        }
        updateChannels();
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
            // All parameters and locations should share the same timestamps. We use temperature to figure out most
            // recent timestamp which has non-NaN value
            int lastValidIndex = unwrap(
                    response.getData(location, ObservationRequest.PARAM_TEMPERATURE).map(data -> lastValidIndex(data)),
                    "lastValidIndex not available. Bug?");
            for (Channel channel : getThing().getChannels()) {
                ChannelUID channelUID = channel.getUID();
                if (lastValidIndex < 0) {
                    updateState(channelUID, UnDefType.UNDEF);
                } else if (channelUID.getIdWithoutGroup().equals(CHANNEL_TIME)) {
                    String field = ObservationRequest.PARAM_TEMPERATURE;
                    Data data = unwrap(response.getData(location, field),
                            "Field %s not present for location %s in response. Bug?", field, location);
                    updateEpochSecondStateIfLinked(channelUID, data.timestampsEpochSecs[lastValidIndex]);
                } else {
                    String field = getDataField(channelUID);
                    @Nullable
                    Unit<?> unit = getUnit(channelUID);
                    if (field == null) {
                        logger.error("Channel {} not handled. Bug?", channelUID.getId());
                        continue;
                    }
                    Data data = unwrap(response.getData(location, field),
                            "Field %s not present for location % in response. Bug?", field, location);
                    updateStateIfLinked(channelUID, data.values[lastValidIndex], unit);
                }
            }
        } catch (IllegalStateException e) {
            // IllegalStateException: Unexpected (possibly bug) issue with response
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    String.format("%s: %s", e.getClass().getName(), e.getMessage()));
        } finally {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @SuppressWarnings({ "unused", "null" })
    private static @Nullable String getDataField(ChannelUID channelUID) {
        Entry<String, @Nullable Unit<?>> entry = CHANNEL_TO_OBSERVATION_FIELD_NAME_AND_UNIT
                .get(channelUID.getIdWithoutGroup());
        if (entry == null) {
            return null;
        }
        return entry.getKey();
    }

    @SuppressWarnings({ "unused", "null" })
    private static @Nullable Unit<?> getUnit(ChannelUID channelUID) {
        Entry<String, @Nullable Unit<?>> entry = CHANNEL_TO_OBSERVATION_FIELD_NAME_AND_UNIT
                .get(channelUID.getIdWithoutGroup());
        if (entry == null) {
            return null;
        }
        return entry.getValue();
    }

}
