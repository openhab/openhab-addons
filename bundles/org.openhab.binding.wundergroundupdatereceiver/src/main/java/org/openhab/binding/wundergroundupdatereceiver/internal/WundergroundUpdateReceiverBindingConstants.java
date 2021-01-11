/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import static java.util.Map.entry;
import static org.openhab.core.library.unit.ImperialUnits.*;
import static org.openhab.core.library.unit.SIUnits.METRE;
import static org.openhab.core.library.unit.Units.*;

import java.util.Map;
import java.util.Set;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Length;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

import tec.uom.se.format.SimpleUnitFormat;
import tec.uom.se.function.MultiplyConverter;
import tec.uom.se.unit.TransformedUnit;

/**
 * The {@link WundergroundUpdateReceiverBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Daniel Demus - Initial contribution
 */
@NonNullByDefault
public class WundergroundUpdateReceiverBindingConstants {

    public static final String BINDING_ID = "wundergroundupdatereceiver";

    public static final String STATION_ID_PARAMETER = "ID";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_UPDATE_RECEIVER = new ThingTypeUID(BINDING_ID,
            "wundergroundUpdateReceiver");

    public static final String LAST_RECEIVED_DATETIME = "last-received";
    public static final ChannelTypeUID LAST_RECEIVED_DATETIME_TYPE = new ChannelTypeUID(BINDING_ID,
            "last-received-datetime");
    public static final String LAST_QUERY = "last-query";

    // List of all Channel ids without groups
    public static final String DATEUTC = "dateutc";
    public static final String SOFTWARE_TYPE = "softwaretype";
    public static final String WIND_DIRECTION = "winddir";
    public static final String WIND_SPEED = "windspeedmph";
    public static final String GUST_SPEED = "windgustmph";
    public static final String GUST_DIRECTION = "windgustdir";
    public static final String WIND_SPEED_AVG_2MIN = "windspdmph_avg2m";
    public static final String WIND_DIRECTION_AVG_2MIN = "winddir_avg2m";
    public static final String GUST_SPEED_AVG_10MIN = "windgustmph_10m";
    public static final String GUST_DIRECTION_AVG_10MIN = "windgustdir_10m";
    public static final String TEMPERATURE = "tempf";
    public static final String INDOOR_TEMPERATURE = "indoortempf";
    public static final String SOIL_TEMPERATURE = "soiltempf";
    public static final String HUMIDITY = "humidity";
    public static final String INDOOR_HUMIDITY = "indoorhumidity";
    public static final String DEWPOINT = "dewptf";
    public static final String SOIL_MOISTURE = "soilmoisture";
    public static final String LEAF_WETNESS = "leafwetness";
    public static final String RAIN_IN = "rainin";
    public static final String DAILY_RAIN_IN = "dailyrainin";
    public static final String SOLAR_RADIATION = "solarradiation";
    public static final String UV = "UV";
    public static final String VISIBILITY = "visibility";
    public static final String WEATHER = "weather";
    public static final String CLOUDS = "clouds";
    public static final String BAROM_IN = "baromin";
    public static final String AQ_NO = "AqNO";
    public static final String AQ_NO2T = "AqNO2T";
    public static final String AQ_NO2 = "AqNO2";
    public static final String AQ_NO2Y = "AqNO2Y";
    public static final String AQ_NOX = "AqNOX";
    public static final String AQ_NOY = "AqNOY";
    public static final String AQ_NO3 = "AqNO3";
    public static final String AQ_SO4 = "AqSO4";
    public static final String AQ_SO2 = "AqSO2";
    public static final String AQ_SO2T = "AqSO2T";
    public static final String AQ_CO = "AqCO";
    public static final String AQ_COT = "AqCOT";
    public static final String AQ_EC = "AqEC";
    public static final String AQ_OC = "AqOC";
    public static final String AQ_BC = "AqBC";
    public static final String AQ_UV_AETH = "AqUV-AETH";
    public static final String AQ_PM2_5 = "AqPM2.5";
    public static final String AQ_PM10 = "AqPM10";
    public static final String AQ_OZONE = "AqOZONE";

    public static final Unit<Length> NAUTICAL_MILE = addUnit(
            new TransformedUnit<>("NM", METRE, new MultiplyConverter(1852.0)));

    public static final Set<String> CHANNEL_KEYS = Set.of(DATEUTC, SOFTWARE_TYPE, WIND_DIRECTION, WIND_SPEED,
            GUST_SPEED, GUST_DIRECTION, WIND_SPEED_AVG_2MIN, WIND_DIRECTION_AVG_2MIN, GUST_SPEED_AVG_10MIN,
            GUST_DIRECTION_AVG_10MIN, TEMPERATURE, INDOOR_TEMPERATURE, SOIL_TEMPERATURE, HUMIDITY, INDOOR_HUMIDITY,
            DEWPOINT, SOIL_MOISTURE, LEAF_WETNESS, RAIN_IN, DAILY_RAIN_IN, SOLAR_RADIATION, UV, VISIBILITY, WEATHER,
            CLOUDS, BAROM_IN, AQ_NO, AQ_NO2, AQ_NO2T, AQ_NO2Y, AQ_NOX, AQ_NOY, AQ_NO3, AQ_SO2, AQ_SO2T, AQ_SO4, AQ_CO,
            AQ_COT, AQ_EC, AQ_OC, AQ_BC, AQ_UV_AETH, AQ_PM2_5, AQ_PM10, AQ_OZONE);

    public static final Map<String, Unit<? extends Quantity<?>>> CHANNEL_UNIT_MAPPING = Map.ofEntries(
            entry(TEMPERATURE, FAHRENHEIT), entry(DEWPOINT, FAHRENHEIT), entry(SOIL_TEMPERATURE, FAHRENHEIT),
            entry(INDOOR_TEMPERATURE, FAHRENHEIT), entry(WIND_SPEED, MILES_PER_HOUR), entry(GUST_SPEED, MILES_PER_HOUR),
            entry(WIND_SPEED_AVG_2MIN, MILES_PER_HOUR), entry(GUST_SPEED_AVG_10MIN, MILES_PER_HOUR),
            entry(WIND_DIRECTION, DEGREE_ANGLE), entry(GUST_DIRECTION, DEGREE_ANGLE),
            entry(WIND_DIRECTION_AVG_2MIN, DEGREE_ANGLE), entry(GUST_DIRECTION_AVG_10MIN, DEGREE_ANGLE),
            entry(BAROM_IN, INCH_OF_MERCURY), entry(RAIN_IN, INCH), entry(DAILY_RAIN_IN, INCH),
            entry(SOLAR_RADIATION, IRRADIANCE), entry(AQ_NO, PARTS_PER_BILLION), entry(AQ_NO2, PARTS_PER_BILLION),
            entry(AQ_NO2T, PARTS_PER_BILLION), entry(AQ_NO2Y, PARTS_PER_BILLION), entry(AQ_NOX, PARTS_PER_BILLION),
            entry(AQ_NOY, PARTS_PER_BILLION), entry(AQ_SO2, PARTS_PER_BILLION), entry(AQ_SO2T, PARTS_PER_BILLION),
            entry(AQ_COT, PARTS_PER_BILLION), entry(AQ_OZONE, PARTS_PER_BILLION), entry(AQ_CO, PARTS_PER_MILLION),
            entry(AQ_NO3, MICROGRAM_PER_CUBICMETRE), entry(AQ_SO4, MICROGRAM_PER_CUBICMETRE),
            entry(AQ_EC, MICROGRAM_PER_CUBICMETRE), entry(AQ_OC, MICROGRAM_PER_CUBICMETRE),
            entry(AQ_BC, MICROGRAM_PER_CUBICMETRE), entry(AQ_UV_AETH, MICROGRAM_PER_CUBICMETRE),
            entry(AQ_PM2_5, MICROGRAM_PER_CUBICMETRE), entry(AQ_PM10, MICROGRAM_PER_CUBICMETRE),
            entry(HUMIDITY, PERCENT), entry(INDOOR_HUMIDITY, PERCENT), entry(SOIL_MOISTURE, PERCENT),
            entry(LEAF_WETNESS, PERCENT), entry(VISIBILITY, NAUTICAL_MILE));

    static {
        SimpleUnitFormat.getInstance().label(NAUTICAL_MILE, "NM");
    }

    private static <U extends Unit<?>> U addUnit(U unit) {
        return unit;
    }
}
