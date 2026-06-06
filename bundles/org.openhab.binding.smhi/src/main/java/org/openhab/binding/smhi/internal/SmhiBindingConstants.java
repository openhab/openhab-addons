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
package org.openhab.binding.smhi.internal;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.measure.MetricPrefix;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.smhi.provider.ParameterMetadata;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.semantics.SemanticTag;
import org.openhab.core.semantics.model.DefaultSemanticTags;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SmhiBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Anders Alfredsson - Initial contribution
 */
@NonNullByDefault
public class SmhiBindingConstants {

    public static final String BINDING_ID = "smhi";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_FORECAST = new ThingTypeUID(BINDING_ID, "forecast");

    public static final String TIMESERIES_GROUP_ID = "timeseries";

    // Smhi's ids for parameters, also used as channel ids
    public static final String PRESSURE = "air_pressure_at_mean_sea_level";
    public static final String TEMPERATURE = "air_temperature";
    public static final String CLOUD_BASE_ALTITUDE = "cloud_base_altitude";
    public static final String CLOUD_TOP_ALTITUDE = "cloud_top_altitude";
    public static final String VISIBILITY = "visibility_in_air";
    public static final String WIND_DIRECTION = "wind_from_direction";
    public static final String WIND_SPEED = "wind_speed";
    public static final String RELATIVE_HUMIDITY = "relative_humidity";
    public static final String THUNDER_PROBABILITY = "thunderstorm_probability";
    public static final String TOTAL_CLOUD_COVER = "cloud_area_fraction";
    public static final String LOW_CLOUD_COVER = "low_type_cloud_area_fraction";
    public static final String MEDIUM_CLOUD_COVER = "medium_type_cloud_area_fraction";
    public static final String HIGH_CLOUD_COVER = "high_type_cloud_area_fraction";
    public static final String GUST = "wind_speed_of_gust";
    public static final String PRECIPITATION_MIN = "precipitation_amount_min";
    public static final String PRECIPITATION_MAX = "precipitation_amount_max";
    public static final String PRECIPITATION_MEAN = "precipitation_amount_mean";
    public static final String PRECIPITATION_MEDIAN = "precipitation_amount_median";
    public static final String PRECIPITATION_PROBABILITY = "probability_of_precipitation";
    public static final String PERCENT_FROZEN = "precipitation_frozen_part";
    public static final String FROZEN_PROBABILITY = "probability_of_frozen_precipitation";
    public static final String PRECIPITATION_CATEGORY = "predominant_precipitation_type_at_surface";
    public static final String WEATHER_SYMBOL = "symbol_code";

    public static final String TEMPERATURE_MAX = "temperature_max";
    public static final String TEMPERATURE_MIN = "temperature_min";
    public static final String WIND_MAX = "wind_speed_max";
    public static final String WIND_MIN = "wind_speed_min";
    public static final String PRECIPITATION_TOTAL = "precipitation_amount_total";

    public static final Set<String> STANDARD_CHANNELS = Set.of(PRESSURE, TEMPERATURE, TEMPERATURE_MAX, TEMPERATURE_MIN,
            WIND_DIRECTION, WIND_SPEED, WIND_MAX, WIND_MIN, RELATIVE_HUMIDITY, TOTAL_CLOUD_COVER, GUST,
            PRECIPITATION_MIN, PRECIPITATION_MAX, PRECIPITATION_TOTAL, PRECIPITATION_PROBABILITY,
            PRECIPITATION_CATEGORY, WEATHER_SYMBOL);

    public static final Set<String> AGGREGATE_CHANNELS = Set.of(TEMPERATURE_MAX, TEMPERATURE_MIN, WIND_MAX, WIND_MIN,
            PRECIPITATION_TOTAL);

    public static final String BASE_URL = "https://opendata-download-metfcst.smhi.se/api/category/snow1g/version/1/";
    public static final String CREATED_TIME_URL = BASE_URL + "createdtime.json";
    public static final String POINT_FORECAST_URL = BASE_URL + "geotype/point/lon/%.6f/lat/%.6f/data.json";
    public static final String PARAMETER_METADATA_URL = BASE_URL + "parameter.json";

    public static final BigDecimal OCTAS_TO_PERCENT = BigDecimal.valueOf(12.5);
    public static final BigDecimal FRACTION_TO_PERCENT = BigDecimal.valueOf(100);
    public static final BigDecimal DEFAULT_MISSING_VALUE = BigDecimal.valueOf(9999);

    public static final List<ParameterMetadata> AGGREGATE_CHANNELS_METADATA = List.of(
            new ParameterMetadata(TEMPERATURE_MAX, "", "Highest temperature of the day", "", BigDecimal.ZERO, "Cel",
                    DEFAULT_MISSING_VALUE),
            new ParameterMetadata(TEMPERATURE_MIN, "", "Lowest temperature of the day", "", BigDecimal.ZERO, "Cel",
                    DEFAULT_MISSING_VALUE),
            new ParameterMetadata(WIND_MAX, "", "Highest wind speed of the day", "", BigDecimal.ZERO, "m/s",
                    DEFAULT_MISSING_VALUE),
            new ParameterMetadata(WIND_MIN, "", "Lowest wind speed of the day", "", BigDecimal.ZERO, "m/s",
                    DEFAULT_MISSING_VALUE),
            new ParameterMetadata(PRECIPITATION_TOTAL, "", "Total amount of precipitation during the day", "",
                    BigDecimal.ZERO, "mm", DEFAULT_MISSING_VALUE));

    public static final Map<String, Unit<?>> UNIT_MAP = Map.ofEntries(Map.entry("Cel", SIUnits.CELSIUS),
            Map.entry("degree", Units.DEGREE_ANGLE), Map.entry("m/s", Units.METRE_PER_SECOND),
            Map.entry("m s**-1", Units.METRE_PER_SECOND), Map.entry("percent", Units.PERCENT),
            Map.entry("hPa", MetricPrefix.HECTO(SIUnits.PASCAL)), Map.entry("km", MetricPrefix.KILO(SIUnits.METRE)),
            Map.entry("fraction", Units.PERCENT), Map.entry("octas", Units.PERCENT), Map.entry("m", SIUnits.METRE),
            Map.entry("kg/m2", Units.MILLIMETRE_PER_HOUR), Map.entry("%", Units.PERCENT),
            Map.entry("mm", MetricPrefix.MILLI(SIUnits.METRE)));

    public static final Map<Unit<?>, SemanticTag> SEMANTIC_PROPERTIES = Map.ofEntries(
            Map.entry(MetricPrefix.HECTO(SIUnits.PASCAL), DefaultSemanticTags.Property.PRESSURE),
            Map.entry(SIUnits.CELSIUS, DefaultSemanticTags.Property.TEMPERATURE),
            Map.entry(Units.DEGREE_ANGLE, DefaultSemanticTags.Property.WIND),
            Map.entry(Units.METRE_PER_SECOND, DefaultSemanticTags.Property.WIND),
            Map.entry(Units.MILLIMETRE_PER_HOUR, DefaultSemanticTags.Property.PRECIPITATION),
            Map.entry(MetricPrefix.MILLI(SIUnits.METRE), DefaultSemanticTags.Property.PRECIPITATION));

    // TODO: Remove after last 2026 release
    public static final String PMP3G_PRESSURE = "msl";
    public static final String PMP3G_TEMPERATURE = "t";
    public static final String PMP3G_VISIBILITY = "vis";
    public static final String PMP3G_WIND_DIRECTION = "wd";
    public static final String PMP3G_WIND_SPEED = "ws";
    public static final String PMP3G_RELATIVE_HUMIDITY = "r";
    public static final String PMP3G_THUNDER_PROBABILITY = "tstm";
    public static final String PMP3G_TOTAL_CLOUD_COVER = "tcc_mean";
    public static final String PMP3G_LOW_CLOUD_COVER = "lcc_mean";
    public static final String PMP3G_MEDIUM_CLOUD_COVER = "mcc_mean";
    public static final String PMP3G_HIGH_CLOUD_COVER = "hcc_mean";
    public static final String PMP3G_GUST = "gust";
    public static final String PMP3G_PRECIPITATION_MIN = "pmin";
    public static final String PMP3G_PRECIPITATION_MAX = "pmax";
    public static final String PMP3G_PRECIPITATION_MEAN = "pmean";
    public static final String PMP3G_PRECIPITATION_MEDIAN = "pmedian";
    public static final String PMP3G_PERCENT_FROZEN = "spp";
    public static final String PMP3G_PRECIPITATION_CATEGORY = "pcat";
    public static final String PMP3G_WEATHER_SYMBOL = "wsymb2";

    public static final String PMP3G_TEMPERATURE_MAX = "tmax";
    public static final String PMP3G_TEMPERATURE_MIN = "tmin";
    public static final String PMP3G_WIND_MAX = "wsmax";
    public static final String PMP3G_WIND_MIN = "wsmin";
    public static final String PMP3G_PRECIPITATION_TOTAL = "ptotal";

    public static final Map<String, String> PMP3G_BACKWARD_COMP = Map.ofEntries(Map.entry(PMP3G_PRESSURE, PRESSURE),
            Map.entry(PMP3G_TEMPERATURE, TEMPERATURE), Map.entry(PMP3G_VISIBILITY, VISIBILITY),
            Map.entry(PMP3G_WIND_DIRECTION, WIND_DIRECTION), Map.entry(PMP3G_WIND_SPEED, WIND_SPEED),
            Map.entry(PMP3G_RELATIVE_HUMIDITY, RELATIVE_HUMIDITY),
            Map.entry(PMP3G_THUNDER_PROBABILITY, THUNDER_PROBABILITY),
            Map.entry(PMP3G_TOTAL_CLOUD_COVER, TOTAL_CLOUD_COVER), Map.entry(PMP3G_LOW_CLOUD_COVER, LOW_CLOUD_COVER),
            Map.entry(PMP3G_MEDIUM_CLOUD_COVER, MEDIUM_CLOUD_COVER),
            Map.entry(PMP3G_HIGH_CLOUD_COVER, HIGH_CLOUD_COVER), Map.entry(PMP3G_GUST, GUST),
            Map.entry(PMP3G_PRECIPITATION_MIN, PRECIPITATION_MIN),
            Map.entry(PMP3G_PRECIPITATION_MAX, PRECIPITATION_MAX),
            Map.entry(PMP3G_PRECIPITATION_MEAN, PRECIPITATION_MEAN),
            Map.entry(PMP3G_PRECIPITATION_MEDIAN, PRECIPITATION_MEDIAN),
            Map.entry(PMP3G_PERCENT_FROZEN, PERCENT_FROZEN),
            Map.entry(PMP3G_PRECIPITATION_CATEGORY, PRECIPITATION_CATEGORY),
            Map.entry(PMP3G_WEATHER_SYMBOL, WEATHER_SYMBOL), Map.entry(PMP3G_TEMPERATURE_MAX, TEMPERATURE_MAX),
            Map.entry(PMP3G_TEMPERATURE_MIN, TEMPERATURE_MIN), Map.entry(PMP3G_WIND_MAX, WIND_MAX),
            Map.entry(PMP3G_WIND_MIN, WIND_MIN), Map.entry(PMP3G_PRECIPITATION_TOTAL, PRECIPITATION_TOTAL));

    public static final Map<Integer, Integer> PMP3G_PCAT_BACKWARD_COMP = Map.ofEntries(Map.entry(0, 0), Map.entry(5, 1),
            Map.entry(6, 1), Map.entry(7, 2), Map.entry(4, 2), Map.entry(1, 3), Map.entry(2, 3), Map.entry(11, 4),
            Map.entry(3, 5), Map.entry(12, 6));
    // TODO: end
}
