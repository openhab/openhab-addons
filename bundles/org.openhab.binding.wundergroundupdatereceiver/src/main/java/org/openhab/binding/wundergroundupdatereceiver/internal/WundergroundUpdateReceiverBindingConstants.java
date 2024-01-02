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
package org.openhab.binding.wundergroundupdatereceiver.internal;

import static org.openhab.core.library.unit.SIUnits.METRE;

import java.util.Set;

import javax.measure.Unit;
import javax.measure.quantity.Length;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.DefaultSystemChannelTypeProvider;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

import tech.units.indriya.format.SimpleUnitFormat;
import tech.units.indriya.function.MultiplyConverter;
import tech.units.indriya.unit.TransformedUnit;

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
    public static final String REPRESENTATION_PROPERTY = "stationId";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_UPDATE_RECEIVER = new ThingTypeUID(BINDING_ID,
            "wundergroundUpdateReceiver");
    static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_UPDATE_RECEIVER);

    public static final String NOW = "now";

    public static final String UNCATEGORIZED = "Uncategorized";

    // Excluded technical paramter names
    public static final String REALTIME_MARKER = "realtime";
    public static final String PASSWORD = "PASSWORD";
    public static final String ACTION = "action";

    // List of default synthetic channeltypes added to a new thing
    public static final String DATEUTC_DATETIME = "dateutc-datetime";
    public static final String LAST_RECEIVED = "last-received";
    public static final String LAST_RECEIVED_DATETIME = LAST_RECEIVED + "-datetime";
    public static final String LAST_QUERY = "last-query";
    public static final String LAST_QUERY_STATE = LAST_QUERY + "-state";
    public static final String LAST_QUERY_TRIGGER = LAST_QUERY + "-trigger";

    // Channel groups
    public static final String METADATA_GROUP = "metadata";
    public static final String WIND_GROUP = "wind";
    public static final String TEMPERATURE_GROUP = "temperature";
    public static final String HUMIDITY_GROUP = "humidity";
    public static final String RAIN_GROUP = "rain";
    public static final String SUNLIGHT_GROUP = "sunlight";
    public static final String PRESSURE_GROUP = "pressure";
    public static final String POLLUTION_GROUP = "pollution";

    // Known or observed request parameters received from devices submitting to wunderground.com
    public static final String DATEUTC = "dateutc";
    public static final String SOFTWARE_TYPE = "softwaretype";
    public static final String LOW_BATTERY = "lowbatt";
    public static final String REALTIME_FREQUENCY = "rtfreq";
    public static final String WIND_DIRECTION = "winddir";
    public static final String WIND_SPEED = "windspeedmph";
    public static final String GUST_SPEED = "windgustmph";
    public static final String GUST_DIRECTION = "windgustdir";
    public static final String WIND_SPEED_AVG_2MIN = "windspdmph_avg2m";
    public static final String WIND_DIRECTION_AVG_2MIN = "winddir_avg2m";
    public static final String GUST_SPEED_AVG_10MIN = "windgustmph_10m";
    public static final String GUST_DIRECTION_AVG_10MIN = "windgustdir_10m";
    public static final String TEMPERATURE = "tempf";
    public static final String TEMPERATURE_INDEXED = "^temp(\\d+)f$";
    public static final String WIND_CHILL = "windchillf";
    public static final String INDOOR_TEMPERATURE = "indoortempf";
    public static final String SOIL_TEMPERATURE = "soiltempf";
    public static final String SOIL_TEMPERATURE_INDEXED = "^soiltemp(\\d+)f$";
    public static final String HUMIDITY = "humidity";
    public static final String INDOOR_HUMIDITY = "indoorhumidity";
    public static final String DEWPOINT = "dewptf";
    public static final String SOIL_MOISTURE = "soilmoisture";
    public static final String SOIL_MOISTURE_INDEXED = "^soilmoisture(\\d+)$";
    public static final String LEAF_WETNESS = "leafwetness";
    public static final String LEAF_WETNESS_INDEXED = "^leafwetness(\\d+)$";
    public static final String RAIN_IN = "rainin";
    public static final String DAILY_RAIN_IN = "dailyrainin";
    public static final String WEEKLY_RAIN_IN = "weeklyrainin";
    public static final String MONTHLY_RAIN_IN = "monthlyrainin";
    public static final String YEARLY_RAIN_IN = "yearlyrainin";
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
    public static final String AQ_PM2_5 = "AqPM2-5";
    public static final String AQ_PM10 = "AqPM10";
    public static final String AQ_OZONE = "AqOZONE";

    public static final ChannelTypeUID LAST_RECEIVED_DATETIME_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID,
            LAST_RECEIVED_DATETIME);
    public static final ChannelTypeUID LAST_QUERY_STATE_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID,
            LAST_QUERY_STATE);
    public static final ChannelTypeUID LAST_QUERY_TRIGGER_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID,
            LAST_QUERY_TRIGGER);
    public static final ChannelTypeUID DATEUTC_DATETIME_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID,
            DATEUTC_DATETIME);
    public static final ChannelTypeUID DATEUTC_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID, DATEUTC);
    public static final ChannelTypeUID LOW_BATTERY_CHANNELTYPEUID = DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_LOW_BATTERY
            .getUID();
    public static final ChannelTypeUID SOFTWARETYPE_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID, "softwaretype");
    public static final ChannelTypeUID REALTIME_FREQUENCY_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID,
            "realtime-frequency");
    public static final ChannelTypeUID WIND_SPEED_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID, "wind-speed");
    public static final ChannelTypeUID WIND_DIRECTION_CHANNELTYPEUID = DefaultSystemChannelTypeProvider.SYSTEM_WIND_DIRECTION
            .getUID();
    public static final ChannelTypeUID GUST_SPEED_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID, "wind-gust-speed");
    public static final ChannelTypeUID GUST_DIRECTION_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID,
            "wind-gust-direction");
    public static final ChannelTypeUID WIND_SPEED_AVG_2MIN_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID,
            "wind-speed-avg-2min");
    public static final ChannelTypeUID WIND_DIRECTION_AVG_2MIN_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID,
            "wind-direction-avg-2min");
    public static final ChannelTypeUID GUST_SPEED_10MIN_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID,
            "wind-gust-speed-10min");
    public static final ChannelTypeUID GUST_DIRECTION_10MIN_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID,
            "wind-gust-direction-10min");
    public static final ChannelTypeUID TEMPERATURE_CHANNELTYPEUID = DefaultSystemChannelTypeProvider.SYSTEM_OUTDOOR_TEMPERATURE
            .getUID();
    public static final ChannelTypeUID HUMIDITY_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID, "humidity");
    public static final ChannelTypeUID INDOOR_HUMIDITY_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID,
            "indoor-humidity");
    public static final ChannelTypeUID DEW_POINT_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID, "dew-point");
    public static final ChannelTypeUID WIND_CHILL_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID, "wind-chill");
    // for extra outdoor sensors use temp2f, temp3f, and so on
    public static final ChannelTypeUID INDOOR_TEMPERATURE_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID,
            "indoor-temperature");
    // for sensors 2,3,4 use soiltemp2f, soiltemp3f, and soiltemp4f
    public static final ChannelTypeUID SOIL_TEMPERATURE_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID,
            "soil-temperature");
    public static final ChannelTypeUID RAIN_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID, "rain");
    public static final ChannelTypeUID RAIN_DAILY_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID, "rain-daily");
    public static final ChannelTypeUID RAIN_WEEKLY_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID, "rain-weekly");
    public static final ChannelTypeUID RAIN_MONTHLY_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID, "rain-monthly");
    public static final ChannelTypeUID RAIN_YEARLY_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID, "rain-yearly");
    public static final ChannelTypeUID METAR_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID, "metar");
    public static final ChannelTypeUID CLOUDS_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID, "clouds");
    // for sensors 2,3,4 use soilmoisture2, soilmoisture3, and soilmoisture4
    public static final ChannelTypeUID SOIL_MOISTURE_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID, "soil-moisture");
    // for sensor 2 use leafwetness2
    public static final ChannelTypeUID LEAFWETNESS_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID, "leafwetness");
    public static final ChannelTypeUID SOLARRADIATION_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID, "solarradiation");
    public static final ChannelTypeUID UV_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID, "uv");
    public static final ChannelTypeUID VISIBILITY_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID, "visibility");
    public static final ChannelTypeUID BAROMETRIC_PRESSURE_CHANNELTYPEUID = DefaultSystemChannelTypeProvider.SYSTEM_BAROMETRIC_PRESSURE
            .getUID();
    public static final ChannelTypeUID NITRIC_OXIDE_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID, "nitric-oxide");
    public static final ChannelTypeUID NITROGEN_DIOXIDE_MEASURED_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID,
            "nitrogen-dioxide-measured");
    public static final ChannelTypeUID NITROGEN_DIOXIDE_NOX_NO_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID,
            "nitrogen-dioxide-nox-no");
    public static final ChannelTypeUID NITROGEN_DIOXIDE_NOY_NO_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID,
            "nitrogen-dioxide-noy-no");
    public static final ChannelTypeUID NITROGEN_OXIDES_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID,
            "nitrogen-oxides");
    public static final ChannelTypeUID TOTAL_REACTIVE_NITROGEN_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID,
            "total-reactive-nitrogen");
    public static final ChannelTypeUID NO3_ION_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID, "no3-ion");
    public static final ChannelTypeUID SO4_ION_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID, "so4-ion");
    public static final ChannelTypeUID SULFUR_DIOXIDE_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID, "sulfur-dioxide");
    public static final ChannelTypeUID SULFUR_DIOXIDE_TRACE_LEVELS_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID,
            "sulfur-dioxide-trace-levels");
    public static final ChannelTypeUID CARBON_MONOXIDE_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID,
            "carbon-monoxide");
    public static final ChannelTypeUID CARBON_MONOXIDE_TRACE_LEVELS_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID,
            "carbon-monoxide-trace-levels");
    public static final ChannelTypeUID ELEMENTAL_CARBON_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID,
            "elemental-carbon");
    public static final ChannelTypeUID ORGANIC_CARBON_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID, "organic-carbon");
    public static final ChannelTypeUID BLACK_CARBON_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID, "black-carbon");
    public static final ChannelTypeUID AETHALOMETER_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID, "aethalometer");
    public static final ChannelTypeUID PM2_5_MASS_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID, "pm2_5-mass");
    public static final ChannelTypeUID PM10_MASS_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID, "pm10-mass");
    public static final ChannelTypeUID OZONE_CHANNELTYPEUID = new ChannelTypeUID(BINDING_ID, "ozone");

    public static final Unit<Length> NAUTICAL_MILE = addUnit(
            new TransformedUnit<>("NM", METRE, MultiplyConverter.of(1852.0)));

    static {
        SimpleUnitFormat.getInstance().label(NAUTICAL_MILE, "NM");
    }

    private static <U extends Unit<?>> U addUnit(U unit) {
        return unit;
    }
}
