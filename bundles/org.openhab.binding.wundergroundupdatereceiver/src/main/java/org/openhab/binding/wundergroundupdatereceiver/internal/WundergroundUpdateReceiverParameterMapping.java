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
package org.openhab.binding.wundergroundupdatereceiver.internal;

import static org.openhab.binding.wundergroundupdatereceiver.internal.WundergroundUpdateReceiverBindingConstants.*;
import static org.openhab.core.library.unit.ImperialUnits.*;
import static org.openhab.core.library.unit.Units.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * @author Daniel Demus - Initial contribution
 */
@NonNullByDefault
public class WundergroundUpdateReceiverParameterMapping {

    public final String parameterName;
    public final ChannelTypeUID channelTypeId;
    public final String channelGroup;
    public final @Nullable Unit<? extends Quantity<?>> unit;
    public final boolean isIndexable;

    public final @Nullable Pattern pattern;

    private WundergroundUpdateReceiverParameterMapping(String parameterName, ChannelTypeUID channelTypeId,
            String channelGroup, @Nullable Unit<? extends Quantity<?>> unit, boolean isIndexable,
            @Nullable Pattern pattern) {
        this.parameterName = parameterName;
        this.channelTypeId = channelTypeId;
        this.channelGroup = channelGroup;
        this.unit = unit;
        this.isIndexable = isIndexable;
        this.pattern = pattern;
    }

    private static final List<String> UNMAPPED_PARAMETERS = List.of(STATION_ID_PARAMETER, PASSWORD, ACTION,
            REALTIME_MARKER);

    private static final WundergroundUpdateReceiverParameterMapping[] KNOWN_MAPPINGS = {
            new WundergroundUpdateReceiverParameterMapping(LAST_RECEIVED, LAST_RECEIVED_DATETIME_CHANNELTYPEUID,
                    METADATA_GROUP, null, false, null),
            new WundergroundUpdateReceiverParameterMapping(LAST_QUERY_STATE, LAST_QUERY_STATE_CHANNELTYPEUID,
                    METADATA_GROUP, null, false, null),
            new WundergroundUpdateReceiverParameterMapping(LAST_QUERY_TRIGGER, LAST_QUERY_TRIGGER_CHANNELTYPEUID,
                    METADATA_GROUP, null, false, null),
            new WundergroundUpdateReceiverParameterMapping(DATEUTC_DATETIME, DATEUTC_DATETIME_CHANNELTYPEUID,
                    METADATA_GROUP, null, false, null),
            new WundergroundUpdateReceiverParameterMapping(DATEUTC, DATEUTC_CHANNELTYPEUID, METADATA_GROUP, null, false,
                    null),
            new WundergroundUpdateReceiverParameterMapping(SOFTWARE_TYPE, SOFTWARETYPE_CHANNELTYPEUID, METADATA_GROUP,
                    null, false, null),
            new WundergroundUpdateReceiverParameterMapping(LOW_BATTERY, LOW_BATTERY_CHANNELTYPEUID, METADATA_GROUP,
                    null, false, null),
            new WundergroundUpdateReceiverParameterMapping(REALTIME_FREQUENCY, REALTIME_FREQUENCY_CHANNELTYPEUID,
                    METADATA_GROUP, null, false, null),
            new WundergroundUpdateReceiverParameterMapping(WIND_DIRECTION, WIND_DIRECTION_CHANNELTYPEUID, WIND_GROUP,
                    DEGREE_ANGLE, false, null),
            new WundergroundUpdateReceiverParameterMapping(WIND_SPEED, WIND_SPEED_CHANNELTYPEUID, WIND_GROUP,
                    MILES_PER_HOUR, false, null),
            new WundergroundUpdateReceiverParameterMapping(GUST_SPEED, GUST_SPEED_CHANNELTYPEUID, WIND_GROUP,
                    MILES_PER_HOUR, false, null),
            new WundergroundUpdateReceiverParameterMapping(GUST_DIRECTION, GUST_DIRECTION_CHANNELTYPEUID, WIND_GROUP,
                    DEGREE_ANGLE, false, null),
            new WundergroundUpdateReceiverParameterMapping(WIND_SPEED_AVG_2MIN, WIND_SPEED_AVG_2MIN_CHANNELTYPEUID,
                    WIND_GROUP, MILES_PER_HOUR, false, null),
            new WundergroundUpdateReceiverParameterMapping(WIND_DIRECTION_AVG_2MIN,
                    WIND_DIRECTION_AVG_2MIN_CHANNELTYPEUID, WIND_GROUP, DEGREE_ANGLE, false, null),
            new WundergroundUpdateReceiverParameterMapping(GUST_SPEED_AVG_10MIN, GUST_SPEED_10MIN_CHANNELTYPEUID,
                    WIND_GROUP, MILES_PER_HOUR, false, null),
            new WundergroundUpdateReceiverParameterMapping(GUST_DIRECTION_AVG_10MIN,
                    GUST_DIRECTION_10MIN_CHANNELTYPEUID, WIND_GROUP, DEGREE_ANGLE, false, null),
            new WundergroundUpdateReceiverParameterMapping(TEMPERATURE, TEMPERATURE_CHANNELTYPEUID, TEMPERATURE_GROUP,
                    FAHRENHEIT, true, Pattern.compile(TEMPERATURE_INDEXED)),
            new WundergroundUpdateReceiverParameterMapping(INDOOR_TEMPERATURE, INDOOR_TEMPERATURE_CHANNELTYPEUID,
                    TEMPERATURE_GROUP, FAHRENHEIT, false, null),
            new WundergroundUpdateReceiverParameterMapping(SOIL_TEMPERATURE, SOIL_TEMPERATURE_CHANNELTYPEUID,
                    TEMPERATURE_GROUP, FAHRENHEIT, true, Pattern.compile(SOIL_TEMPERATURE_INDEXED)),
            new WundergroundUpdateReceiverParameterMapping(WIND_CHILL, WIND_CHILL_CHANNELTYPEUID, TEMPERATURE_GROUP,
                    FAHRENHEIT, false, null),
            new WundergroundUpdateReceiverParameterMapping(HUMIDITY, HUMIDITY_CHANNELTYPEUID, HUMIDITY_GROUP, PERCENT,
                    false, null),
            new WundergroundUpdateReceiverParameterMapping(INDOOR_HUMIDITY, INDOOR_HUMIDITY_CHANNELTYPEUID,
                    HUMIDITY_GROUP, PERCENT, false, null),
            new WundergroundUpdateReceiverParameterMapping(DEWPOINT, DEW_POINT_CHANNELTYPEUID, HUMIDITY_GROUP,
                    FAHRENHEIT, false, null),
            new WundergroundUpdateReceiverParameterMapping(SOIL_MOISTURE, SOIL_MOISTURE_CHANNELTYPEUID, HUMIDITY_GROUP,
                    PERCENT, true, Pattern.compile(SOIL_MOISTURE_INDEXED)),
            new WundergroundUpdateReceiverParameterMapping(LEAF_WETNESS, LEAFWETNESS_CHANNELTYPEUID, HUMIDITY_GROUP,
                    PERCENT, true, Pattern.compile(LEAF_WETNESS_INDEXED)),
            new WundergroundUpdateReceiverParameterMapping(RAIN_IN, RAIN_CHANNELTYPEUID, RAIN_GROUP, INCH, false, null),
            new WundergroundUpdateReceiverParameterMapping(DAILY_RAIN_IN, RAIN_DAILY_CHANNELTYPEUID, RAIN_GROUP, INCH,
                    false, null),
            new WundergroundUpdateReceiverParameterMapping(WEEKLY_RAIN_IN, RAIN_WEEKLY_CHANNELTYPEUID, RAIN_GROUP, INCH,
                    false, null),
            new WundergroundUpdateReceiverParameterMapping(MONTHLY_RAIN_IN, RAIN_MONTHLY_CHANNELTYPEUID, RAIN_GROUP,
                    INCH, false, null),
            new WundergroundUpdateReceiverParameterMapping(YEARLY_RAIN_IN, RAIN_YEARLY_CHANNELTYPEUID, RAIN_GROUP, INCH,
                    false, null),
            new WundergroundUpdateReceiverParameterMapping(SOLAR_RADIATION, SOLARRADIATION_CHANNELTYPEUID,
                    SUNLIGHT_GROUP, IRRADIANCE, false, null),
            new WundergroundUpdateReceiverParameterMapping(UV, UV_CHANNELTYPEUID, SUNLIGHT_GROUP, null, false, null),
            new WundergroundUpdateReceiverParameterMapping(VISIBILITY, VISIBILITY_CHANNELTYPEUID, SUNLIGHT_GROUP,
                    NAUTICAL_MILE, false, null),
            new WundergroundUpdateReceiverParameterMapping(WEATHER, METAR_CHANNELTYPEUID, SUNLIGHT_GROUP, null, false,
                    null),
            new WundergroundUpdateReceiverParameterMapping(CLOUDS, CLOUDS_CHANNELTYPEUID, SUNLIGHT_GROUP, null, false,
                    null),
            new WundergroundUpdateReceiverParameterMapping(BAROM_IN, BAROMETRIC_PRESSURE_CHANNELTYPEUID, PRESSURE_GROUP,
                    INCH_OF_MERCURY, false, null),
            new WundergroundUpdateReceiverParameterMapping(AQ_NO, NITRIC_OXIDE_CHANNELTYPEUID, POLLUTION_GROUP,
                    PARTS_PER_BILLION, false, null),
            new WundergroundUpdateReceiverParameterMapping(AQ_NO2, NITROGEN_DIOXIDE_NOX_NO_CHANNELTYPEUID,
                    POLLUTION_GROUP, PARTS_PER_BILLION, false, null),
            new WundergroundUpdateReceiverParameterMapping(AQ_NO2T, NITROGEN_DIOXIDE_MEASURED_CHANNELTYPEUID,
                    POLLUTION_GROUP, PARTS_PER_BILLION, false, null),
            new WundergroundUpdateReceiverParameterMapping(AQ_NO2Y, NITROGEN_DIOXIDE_NOY_NO_CHANNELTYPEUID,
                    POLLUTION_GROUP, PARTS_PER_BILLION, false, null),
            new WundergroundUpdateReceiverParameterMapping(AQ_NOX, NITROGEN_OXIDES_CHANNELTYPEUID, POLLUTION_GROUP,
                    PARTS_PER_BILLION, false, null),
            new WundergroundUpdateReceiverParameterMapping(AQ_NOY, TOTAL_REACTIVE_NITROGEN_CHANNELTYPEUID,
                    POLLUTION_GROUP, PARTS_PER_BILLION, false, null),
            new WundergroundUpdateReceiverParameterMapping(AQ_NO3, NO3_ION_CHANNELTYPEUID, POLLUTION_GROUP,
                    MICROGRAM_PER_CUBICMETRE, false, null),
            new WundergroundUpdateReceiverParameterMapping(AQ_SO2, SULFUR_DIOXIDE_CHANNELTYPEUID, POLLUTION_GROUP,
                    PARTS_PER_BILLION, false, null),
            new WundergroundUpdateReceiverParameterMapping(AQ_SO2T, SULFUR_DIOXIDE_TRACE_LEVELS_CHANNELTYPEUID,
                    POLLUTION_GROUP, PARTS_PER_BILLION, false, null),
            new WundergroundUpdateReceiverParameterMapping(AQ_SO4, SO4_ION_CHANNELTYPEUID, POLLUTION_GROUP,
                    MICROGRAM_PER_CUBICMETRE, false, null),
            new WundergroundUpdateReceiverParameterMapping(AQ_CO, CARBON_MONOXIDE_CHANNELTYPEUID, POLLUTION_GROUP,
                    PARTS_PER_MILLION, false, null),
            new WundergroundUpdateReceiverParameterMapping(AQ_COT, CARBON_MONOXIDE_TRACE_LEVELS_CHANNELTYPEUID,
                    POLLUTION_GROUP, MICROGRAM_PER_CUBICMETRE, false, null),
            new WundergroundUpdateReceiverParameterMapping(AQ_EC, ELEMENTAL_CARBON_CHANNELTYPEUID, POLLUTION_GROUP,
                    MICROGRAM_PER_CUBICMETRE, false, null),
            new WundergroundUpdateReceiverParameterMapping(AQ_OC, ORGANIC_CARBON_CHANNELTYPEUID, POLLUTION_GROUP,
                    MICROGRAM_PER_CUBICMETRE, false, null),
            new WundergroundUpdateReceiverParameterMapping(AQ_BC, BLACK_CARBON_CHANNELTYPEUID, POLLUTION_GROUP,
                    MICROGRAM_PER_CUBICMETRE, false, null),
            new WundergroundUpdateReceiverParameterMapping(AQ_UV_AETH, AETHALOMETER_CHANNELTYPEUID, POLLUTION_GROUP,
                    MICROGRAM_PER_CUBICMETRE, false, null),
            new WundergroundUpdateReceiverParameterMapping(AQ_PM2_5, PM2_5_MASS_CHANNELTYPEUID, POLLUTION_GROUP,
                    MICROGRAM_PER_CUBICMETRE, false, null),
            new WundergroundUpdateReceiverParameterMapping(AQ_PM10, PM10_MASS_CHANNELTYPEUID, POLLUTION_GROUP,
                    MICROGRAM_PER_CUBICMETRE, false, null),
            new WundergroundUpdateReceiverParameterMapping(AQ_OZONE, OZONE_CHANNELTYPEUID, POLLUTION_GROUP,
                    PARTS_PER_BILLION, false, null) };

    public static @Nullable WundergroundUpdateReceiverParameterMapping getOrCreateMapping(String parameterName,
            String value, WundergroundUpdateReceiverUnknownChannelTypeProvider channelTypeProvider) {
        if (isExcluded(parameterName)) {
            return null;
        }
        Optional<WundergroundUpdateReceiverParameterMapping> knownMapping = lookupMapping(parameterName);
        return knownMapping.orElseGet(() -> new WundergroundUpdateReceiverParameterMapping(parameterName,
                channelTypeProvider.getOrCreateChannelType(parameterName, value).getUID(), "Uncategorized", null, false,
                null));
    }

    public static boolean isExcluded(String parameter) {
        return UNMAPPED_PARAMETERS.contains(parameter);
    }

    public static @Nullable Unit<? extends Quantity<?>> getUnit(String parameterName) {
        Optional<WundergroundUpdateReceiverParameterMapping> mapping = lookupMapping(parameterName);
        return mapping
                .<Unit<? extends Quantity<?>>> map(
                        wundergroundUpdateReceiverParameterMapping -> wundergroundUpdateReceiverParameterMapping.unit)
                .orElse(null);
    }

    private static Optional<WundergroundUpdateReceiverParameterMapping> lookupMapping(String parameterName) {
        return Arrays.stream(KNOWN_MAPPINGS)
                .filter((WundergroundUpdateReceiverParameterMapping m) -> m.parameterName.equals(parameterName)
                        || (m.isIndexable && m.pattern != null && m.pattern.matcher(parameterName).matches()))
                .findFirst();
    }
}
