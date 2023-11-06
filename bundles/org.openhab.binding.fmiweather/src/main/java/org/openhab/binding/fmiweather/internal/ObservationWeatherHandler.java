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
package org.openhab.binding.fmiweather.internal;

import static org.openhab.binding.fmiweather.internal.BindingConstants.*;
import static org.openhab.binding.fmiweather.internal.client.ObservationRequest.*;
import static org.openhab.core.library.unit.SIUnits.*;
import static org.openhab.core.library.unit.Units.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.measure.Unit;
import javax.measure.quantity.Length;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.fmiweather.internal.client.Data;
import org.openhab.binding.fmiweather.internal.client.FMIResponse;
import org.openhab.binding.fmiweather.internal.client.FMISID;
import org.openhab.binding.fmiweather.internal.client.Location;
import org.openhab.binding.fmiweather.internal.client.ObservationRequest;
import org.openhab.binding.fmiweather.internal.client.Request;
import org.openhab.binding.fmiweather.internal.client.exception.FMIUnexpectedResponseException;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.UnDefType;
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
    private static BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static BigDecimal NA_CLOUD_MAX = BigDecimal.valueOf(8); // API value when having full clouds (overcast)
    private static BigDecimal NA_CLOUD_COVERAGE = BigDecimal.valueOf(9); // API value when cloud coverage could not be
                                                                         // determined.

    public static final Unit<Length> MILLIMETRE = MetricPrefix.MILLI(METRE);
    public static final Unit<Length> CENTIMETRE = MetricPrefix.CENTI(METRE);

    private static final Map<String, Map.Entry<String, @Nullable Unit<?>>> CHANNEL_TO_OBSERVATION_FIELD_NAME_AND_UNIT = new HashMap<>(
            11);
    private static final Map<String, @Nullable Function<BigDecimal, @Nullable BigDecimal>> OBSERVATION_FIELD_NAME_TO_CONVERSION_FUNC = new HashMap<>(
            11);

    private static void addMapping(String channelId, String requestField, @Nullable Unit<?> result_unit,
            @Nullable Function<BigDecimal, @Nullable BigDecimal> conversion) {
        CHANNEL_TO_OBSERVATION_FIELD_NAME_AND_UNIT.put(channelId,
                new AbstractMap.SimpleImmutableEntry<>(requestField, result_unit));
        OBSERVATION_FIELD_NAME_TO_CONVERSION_FUNC.put(requestField, conversion);
    }

    static {
        addMapping(CHANNEL_TEMPERATURE, PARAM_TEMPERATURE, CELSIUS, null);
        addMapping(CHANNEL_HUMIDITY, PARAM_HUMIDITY, PERCENT, null);
        addMapping(CHANNEL_WIND_DIRECTION, PARAM_WIND_DIRECTION, DEGREE_ANGLE, null);
        addMapping(CHANNEL_WIND_SPEED, PARAM_WIND_SPEED, METRE_PER_SECOND, null);
        addMapping(CHANNEL_GUST, PARAM_WIND_GUST, METRE_PER_SECOND, null);
        addMapping(CHANNEL_PRESSURE, PARAM_PRESSURE, MILLIBAR, null);
        addMapping(CHANNEL_PRECIPITATION_AMOUNT, PARAM_PRECIPITATION_AMOUNT, MILLIMETRE, null);
        addMapping(CHANNEL_SNOW_DEPTH, PARAM_SNOW_DEPTH, CENTIMETRE, null);
        addMapping(CHANNEL_VISIBILITY, PARAM_VISIBILITY, METRE, null);
        // Converting 0...8 scale to percentage 0...100%. Value of 9 is converted to null/UNDEF
        addMapping(CHANNEL_CLOUDS, PARAM_CLOUDS, PERCENT, clouds -> clouds.compareTo(NA_CLOUD_COVERAGE) == 0 ? null
                : clouds.divide(NA_CLOUD_MAX).multiply(HUNDRED));
        addMapping(CHANNEL_OBSERVATION_PRESENT_WEATHER, PARAM_PRESENT_WEATHER, null, null);
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
    protected Request getRequest() {
        long now = Instant.now().getEpochSecond();
        return new ObservationRequest(new FMISID(fmisid),
                floorToEvenMinutes(now - OBSERVATION_LOOK_BACK_SECONDS, STEP_MINUTES),
                ceilToEvenMinutes(now, STEP_MINUTES), STEP_MINUTES);
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
                    Unit<?> unit = getUnit(channelUID);
                    if (field == null) {
                        logger.error("Channel {} not handled. Bug?", channelUID.getId());
                        continue;
                    }
                    Data data = unwrap(response.getData(location, field),
                            "Field %s not present for location % in response. Bug?", field, location);
                    BigDecimal rawValue = data.values[lastValidIndex];
                    BigDecimal processedValue = preprocess(field, rawValue);
                    updateStateIfLinked(channelUID, processedValue, unit);
                }
            }
            updateStatus(ThingStatus.ONLINE);
        } catch (FMIUnexpectedResponseException e) {
            // Unexpected (possibly bug) issue with response
            logger.warn("Unexpected response encountered: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    String.format("Unexpected API response: %s", e.getMessage()));
        }
    }

    @SuppressWarnings({ "null", "unused" })
    private static @Nullable String getDataField(ChannelUID channelUID) {
        Entry<String, @Nullable Unit<?>> entry = CHANNEL_TO_OBSERVATION_FIELD_NAME_AND_UNIT
                .get(channelUID.getIdWithoutGroup());
        if (entry == null) {
            return null;
        }
        return entry.getKey();
    }

    @SuppressWarnings({ "null", "unused" })
    private static @Nullable Unit<?> getUnit(ChannelUID channelUID) {
        Entry<String, @Nullable Unit<?>> entry = CHANNEL_TO_OBSERVATION_FIELD_NAME_AND_UNIT
                .get(channelUID.getIdWithoutGroup());
        if (entry == null) {
            return null;
        }
        return entry.getValue();
    }

    private static @Nullable BigDecimal preprocess(String fieldName, @Nullable BigDecimal rawValue) {
        if (rawValue == null) {
            return null;
        }
        Function<BigDecimal, @Nullable BigDecimal> func = OBSERVATION_FIELD_NAME_TO_CONVERSION_FUNC.get(fieldName);
        if (func == null) {
            // No conversion required
            return rawValue;
        }
        return func.apply(rawValue);
    }
}
