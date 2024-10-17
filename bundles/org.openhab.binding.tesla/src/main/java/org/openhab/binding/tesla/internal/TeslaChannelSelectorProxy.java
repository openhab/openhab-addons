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
package org.openhab.binding.tesla.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.openhab.core.types.UnDefType;

/**
 * The {@link TeslaChannelSelectorProxy} class is a helper class to instantiate
 * and parameterize the {@link TeslaChannelSelector} Enum
 *
 * @author Karel Goderis - Initial contribution
 */
@NonNullByDefault
public class TeslaChannelSelectorProxy {

    public enum TeslaChannelSelector {

        API("api_version", "api", DecimalType.class, true),
        AR_DESTINATION("active_route_destination", "destinationname", StringType.class, false),
        AR_LATITUDE("active_route_latitude", "destinationlocation", DecimalType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                proxy.arLatitude = s;
                return new PointType(new StringType(proxy.arLatitude), new StringType(proxy.arLongitude),
                        new StringType(proxy.elevation));
            }
        },
        AR_LONGITUDE("active_route_longitude", "destinationlocation", DecimalType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                proxy.arLongitude = s;
                return new PointType(new StringType(proxy.arLatitude), new StringType(proxy.arLongitude),
                        new StringType(proxy.elevation));
            }
        },
        AR_DISTANCE_TO_ARRIVAL("active_route_miles_to_arrival", "distancetoarrival", DecimalType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                State someState = super.getState(s);
                BigDecimal value = ((DecimalType) someState).toBigDecimal();
                return new QuantityType<>(value, ImperialUnits.MILE);
            }
        },
        AR_MINUTES_TO_ARRIVAL("active_route_minutes_to_arrival", "minutestoarrival", DecimalType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                State someState = super.getState(s);
                BigDecimal value = ((DecimalType) someState).toBigDecimal();
                return new QuantityType<>(value, Units.MINUTE);
            }
        },
        AR_TRAFFIC_MINUTES_DELAY("active_route_traffic_minutes_delay", "trafficminutesdelay", DecimalType.class,
                false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                State someState = super.getState(s);
                BigDecimal value = ((DecimalType) someState).toBigDecimal();
                return new QuantityType<>(value, Units.MINUTE);
            }
        },
        AUTO_COND("is_auto_conditioning_on", "autoconditioning", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        AUTOPARK_STATE("autopark_state", "autoparkstate", StringType.class, false),
        AUTOPARK_STATE_V2("autopark_state_v2", "autoparkstate2", StringType.class, false),
        AUTOPARK_STYLE("autopark_style", "autoparkstyle", StringType.class, false),
        BATTERY_CURRENT("battery_current", "batterycurrent", DecimalType.class, false),
        BATTERY_HEATER("battery_heater_on", "batteryheater", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        BATTERY_HEATER_NO_POWER("battery_heater_no_power", "batteryheaternopower", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        BATTERY_LEVEL("battery_level", "batterylevel", DecimalType.class, false),
        BATTERY_RANGE("battery_range", "batteryrange", DecimalType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                State someState = super.getState(s);
                BigDecimal value = ((DecimalType) someState).toBigDecimal();
                return new QuantityType<>(value, ImperialUnits.MILE);
            }
        },
        CALENDAR_SUPPORTED("calendar_supported", "calendarsupported", OnOffType.class, true) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        CALENDAR_ENABLED("calendar_enabled", "calendarenabled", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        CAR_VERSION("car_version", "version", StringType.class, true),
        CENTER_DISPLAY("center_display_state", "centerdisplay", DecimalType.class, false),
        CHARGE(null, "charge", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        CHARGE_CABLE("conn_charge_cable", "chargecable", StringType.class, false),
        CHARGE_CURRENT_REQUEST("charge_current_request", "chargecurrent", DecimalType.class, false),
        CHARGE_CURRENT_REQUEST_MAX("charge_current_request_max", "chargemaxcurrent", DecimalType.class, false),
        CHARGE_ENABLE_REQUEST("charge_enable_request", "chargeenablerequest", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        CHARGE_ENERGY_ADDED("charge_energy_added", "chargeenergyadded", DecimalType.class, false),
        CHARGE_LIMIT_SOC("charge_limit_soc", "chargelimit", PercentType.class, false),
        CHARGE_LIMIT_SOC_MAX("charge_limit_soc_max", "chargelimitmaximum", PercentType.class, false),
        CHARGE_LIMIT_SOC_MIN("charge_limit_soc_min", "chargelimitminimum", PercentType.class, false),
        CHARGE_LIMIT_SOC_STD("charge_limit_soc_std", "chargelimitsocstandard", PercentType.class, false),
        CHARGE_PORT_LATCH("charge_port_latch", "chargeportlatch", StringType.class, false),
        CHARGE_MILES_ADDED_IDEAL("charge_miles_added_ideal", "chargeidealdistanceadded", DecimalType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                State someState = super.getState(s);
                BigDecimal value = ((DecimalType) someState).toBigDecimal();
                return new QuantityType<>(value, ImperialUnits.MILES_PER_HOUR);
            }
        },
        CHARGE_MILES_ADDED_RANGE("charge_miles_added_rated", "chargerateddistanceadded", DecimalType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                State someState = super.getState(s);
                BigDecimal value = ((DecimalType) someState).toBigDecimal();
                return new QuantityType<>(value, ImperialUnits.MILES_PER_HOUR);
            }
        },
        CHARGE_RATE("charge_rate", "chargerate", DecimalType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                State someState = super.getState(s);
                BigDecimal value = ((DecimalType) someState).toBigDecimal();
                return new QuantityType<>(value, ImperialUnits.MILES_PER_HOUR);
            }
        },
        CHARGE_AMPS("charge_amps", "chargingamps", DecimalType.class, false),
        CHARGE_STARTING_RANGE("charge_starting_range", "chargestartingrange", StringType.class, false),
        CHARGE_STARTING_SOC("charge_starting_soc", "chargestartingsoc", StringType.class, false),
        CHARGE_STATE("charging_state", "chargingstate", StringType.class, false),
        CHARGE_TO_MAX("charge_to_max_range", "chargetomax", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        CHARGE_TO_MAX_COUNTER("max_range_charge_counter", "maxcharges", DecimalType.class, false),
        CHARGEPORT("charge_port_door_open", "chargeport", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        CHARGER_ACTUAL_CURRENT("charger_actual_current", "chargercurrent", DecimalType.class, false),
        CHARGER_PHASES("charger_phases", "chargerphases", DecimalType.class, false),
        CHARGER_PILOT_CURRENT("charger_pilot_current", "chargermaxcurrent", DecimalType.class, false),
        CHARGER_POWER("charger_power", "chargerpower", DecimalType.class, false),
        CHARGER_VOLTAGE("charger_voltage", "chargervoltage", DecimalType.class, false),
        CLIMATE_ON("is_climate_on", "climate", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        CLIMATE_MAX_TEMP("max_avail_temp", "maxavailabletemp", DecimalType.class, false),
        CLIMATE_MIN_TEMP("min_avail_temp", "minavailabletemp", DecimalType.class, false),
        COLOR("exterior_color", "color", StringType.class, true),
        DARK_RIMS("dark_rims", "darkrims", OnOffType.class, true) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        DISPLAY_NAME("display_name", "name", StringType.class, true),
        DF("df", "driverfrontdoor", OpenClosedType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("OPEN");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("CLOSED");
                }
                return super.getState(s);
            }
        },
        DOOR_LOCK("locked", "doorlock", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        DR("dr", "driverreardoor", OpenClosedType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("OPEN");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("CLOSED");
                }
                return super.getState(s);
            }
        },
        DRIVER_TEMP("driver_temp_setting", "drivertemp", DecimalType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                State someState = super.getState(s);
                BigDecimal value = ((DecimalType) someState).toBigDecimal();
                return new QuantityType<>(value, SIUnits.CELSIUS);
            }
        },
        ELEVATION("elevation", "location", DecimalType.class, false) {

            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                proxy.elevation = s;
                return new PointType(new StringType(proxy.latitude), new StringType(proxy.longitude),
                        new StringType(proxy.elevation));
            }
        },
        EST_BATTERY_RANGE("est_battery_range", "estimatedbatteryrange", DecimalType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                State someState = super.getState(s);
                BigDecimal value = ((DecimalType) someState).toBigDecimal();
                return new QuantityType<>(value, ImperialUnits.MILE);
            }
        },
        EST_HEADING("est_heading", "headingestimation", DecimalType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                State someState = super.getState(s);
                BigDecimal value = ((DecimalType) someState).toBigDecimal();
                return new QuantityType<>(value, Units.DEGREE_ANGLE);
            }
        },
        EST_RANGE("est_range", "estimatedrange", DecimalType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                State someState = super.getState(s);
                BigDecimal value = ((DecimalType) someState).toBigDecimal();
                return new QuantityType<>(value, ImperialUnits.MILE);
            }
        },
        EU_VEHICLE("eu_vehicle", "european", OnOffType.class, true) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        FAN_STATUS("fan_status", "fan", DecimalType.class, false),
        FAST_CHARGER("fast_charger_present", "fastcharger", OnOffType.class, true) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        FAST_CHARGER_TYPE("fast_charger_type", "fastchargertype", StringType.class, true),
        FAST_CHARGER_BRAND("fast_charger_brand", "fastchargerbrand", StringType.class, true),
        FLASH(null, "flashlights", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        FRONT_DEFROSTER("is_front_defroster_on", "frontdefroster", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        FT("ft", "fronttrunk", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        GPS_AS_OF("gps_as_of", "gpstimestamp", DateTimeType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                Date date = new Date();
                SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

                date.setTime(Long.valueOf(s) * 1000);
                return super.getState(dateFormatter.format(date));
            }
        },
        GUI_DISTANCE_UNITS("gui_distance_units", "distanceunits", StringType.class, true),
        GUI_TEMPERATURE_UNITS("gui_temperature_units", "temperatureunits", StringType.class, true),
        GUI_CHARGE_RATE_UNITS("gui_charge_rate_units", "chargerateunits", StringType.class, true),
        GUI_24H_TIME("gui_24_hour_time", "24hourtime", StringType.class, true),
        GUI_RANGE_DISPLAY("gui_range_display", "rangedisplay", StringType.class, true),
        HAS_SPOILER("has_spoiler", "spoiler", OnOffType.class, true) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        HEADING("heading", "heading", DecimalType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                State someState = super.getState(s);
                BigDecimal value = ((DecimalType) someState).toBigDecimal();
                return new QuantityType<>(value, Units.DEGREE_ANGLE);
            }
        },
        HONK_HORN(null, "honkhorn", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        HOMELINK_NEARBY("homelink_nearby", "homelink", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        IDEAL_BATTERY_RANGE("ideal_battery_range", "idealbatteryrange", DecimalType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                State someState = super.getState(s);
                BigDecimal value = ((DecimalType) someState).toBigDecimal();
                return new QuantityType<>(value, ImperialUnits.MILE);
            }
        },
        INSIDE_TEMP("inside_temp", "insidetemp", DecimalType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                State someState = super.getState(s);
                BigDecimal value = ((DecimalType) someState).toBigDecimal();
                return new QuantityType<>(value, SIUnits.CELSIUS);
            }
        },
        LAST_AUTOPARK_ERROR("last_autopark_error", "lastautoparkerror", StringType.class, false),
        LATITUDE("latitude", "location", DecimalType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                proxy.latitude = s;
                return new PointType(new StringType(proxy.latitude), new StringType(proxy.longitude),
                        new StringType(proxy.elevation));
            }
        },
        LATITUDE_EVENT("est_lat", "location", DecimalType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                proxy.latitude = s;
                return new PointType(new StringType(proxy.latitude), new StringType(proxy.longitude),
                        new StringType(proxy.elevation));
            }
        },
        LEFT_TEMP_DIR("left_temp_direction", "lefttempdirection", DecimalType.class, false),
        LONGITUDE("longitude", "location", DecimalType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                proxy.longitude = s;
                return new PointType(new StringType(proxy.latitude), new StringType(proxy.longitude),
                        new StringType(proxy.elevation));
            }
        },
        LONGITUDE_EVENT("est_lng", "location", DecimalType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                proxy.longitude = s;
                return new PointType(new StringType(proxy.latitude), new StringType(proxy.longitude),
                        new StringType(proxy.elevation));
            }
        },
        MANAGED_CHARGING("managed_charging_active", "managedcharging", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        MANAGED_CHARGING_CANCELLED("managed_charging_user_canceled", "managedchargingcancelled", OnOffType.class,
                false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        MANAGED_CHARGING_START("managed_charging_start_time", "managedchargingstart", StringType.class, false),
        MOBILE_ENABLED("mobile_enabled", "mobileenabled", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        MOTORIZED_CHARGE_PORT("motorized_charge_port", "motorizedchargeport", OnOffType.class, true) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        NATIVE_LATITUDE("native_latitude", "nativelocation", DecimalType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                proxy.nativeLatitude = s;
                return new PointType(new StringType(proxy.nativeLatitude), new StringType(proxy.nativeLongitude));
            }
        },
        NATIVE_LONGITUDE("native_longitude", "nativelocation", DecimalType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                proxy.nativeLongitude = s;
                return new PointType(new StringType(proxy.nativeLatitude), new StringType(proxy.nativeLongitude));
            }
        },
        NATIVE_LOCATION_SUPPORTED("native_location_supported", "nativelocationsupported", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        NATIVE_TYPE("native_type", "nativetype", StringType.class, false),
        NOT_ENOUGH_POWER_TO_HEAT("not_enough_power_to_heat", "notenoughpower", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        NOTIFICATIONS_ENABLED("notifications_enabled", "notificationsenabled", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        NOTIFICATIONS_SUPPORTED("notifications_supported", "notificationssupported", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        ODOMETER("odometer", "odometer", DecimalType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                State someState = super.getState(s);
                BigDecimal value = ((DecimalType) someState).toBigDecimal();
                return new QuantityType<>(value, ImperialUnits.MILE);
            }
        },
        OPEN_FRUNK(null, "openfrunk", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        OPEN_TRUNK(null, "opentrunk", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        OPTION_CODES("option_codes", "options", StringType.class, true),
        OUTSIDE_TEMP("outside_temp", "outsidetemp", DecimalType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                State someState = super.getState(s);
                BigDecimal value = ((DecimalType) someState).toBigDecimal();
                return new QuantityType<>(value, SIUnits.CELSIUS);
            }
        },
        PARSED_CALENDAR("parsed_calendar_supported", "parsedcalendar", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        PASSENGER_TEMP("passenger_temp_setting", "passengertemp", DecimalType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                State someState = super.getState(s);
                BigDecimal value = ((DecimalType) someState).toBigDecimal();
                return new QuantityType<>(value, SIUnits.CELSIUS);
            }
        },
        PERF_CONFIG("perf_config", "configuration", StringType.class, true),
        PF("pf", "passengerfrontdoor", OpenClosedType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("OPEN");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("CLOSED");
                }
                return super.getState(s);
            }
        },
        POWER("power", "power", DecimalType.class, false),
        PR("pr", "passengerreardoor", OpenClosedType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("OPEN");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("CLOSED");
                }
                return super.getState(s);
            }
        },
        PRECONDITIONING("is_preconditioning", "preconditioning", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        RANGE("range", "range", DecimalType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                State someState = super.getState(s);
                BigDecimal value = ((DecimalType) someState).toBigDecimal();
                if (properties.containsKey("distanceunits") && "km/hr".equals(properties.get("distanceunits"))) {
                    return new QuantityType<>(value, MetricPrefix.KILO(SIUnits.METRE));
                } else {
                    return new QuantityType<>(value, ImperialUnits.MILE);
                }
            }
        },
        REAR_DEFROSTER("is_rear_defroster_on", "reardefroster", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        REAR_SEAT_HEATERS("rear_seat_heaters", "rearseatheaters", OnOffType.class, true) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        REMOTE_START("remote_start", "remotestart", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        REMOTE_START_ENABLED("remote_start_enabled", "remotestartenabled", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        REMOTE_START_SUPPORTED("remote_start_supported", "remotestartsupported", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        RESET_VALET_PIN(null, "resetvaletpin", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        RHD("rhd", "rhd", OnOffType.class, true) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        RIGHT_TEMP_DIR("right_temp_direction", "righttempdirection", DecimalType.class, false),
        ROOF_COLOR("roof_color", "roof", StringType.class, true),
        RT("rt", "reartrunk", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        SEAT_HEATER_LEFT("seat_heater_left", "leftseatheater", DecimalType.class, false),
        SEAT_HEATER_RIGHT("seat_heater_right", "rightseatheater", DecimalType.class, false),
        SEAT_HEATER_REAR_LEFT("seat_heater_rear_left", "leftrearseatheater", DecimalType.class, false),
        SEAT_HEATER_REAR_RIGHT("seat_heater_rear_right", "rightrearseatheater", DecimalType.class, false),
        SEAT_HEATER_REAR_CENTER("seat_heater_rear_center", "centerrearseatheater", DecimalType.class, false),
        SEAT_HEATER_REAR_RIGHT_BACK("seat_heater_rear_right_back", "rightrearbackseatheater", DecimalType.class, false),
        SEAT_HEATER_REAR_RIGHT_LEFT("seat_heater_rear_left_back", "leftrearbackseatheater", DecimalType.class, false),
        SEAT_TYPE("seat_type", "seattype", DecimalType.class, true),
        SCHEDULED_CHARGING_PENDING("scheduled_charging_pending", "scheduledchargingpending", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        SCHEDULED_CHARGING_START_TIME("scheduled_charging_start_time", "scheduledchargingstart", DateTimeType.class,
                false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                Date date = new Date();
                SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

                date.setTime(Long.valueOf(s) * 1000);
                return super.getState(dateFormatter.format(date));
            }
        },
        SENTRY_MODE("sentry_mode", "sentrymode", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        SENTRY_MODE_AVAILABLE("sentry_mode_available", "sentrymodeavailable", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        SHIFTSTATE("shift_state", "shiftstate", StringType.class, false),
        SIDEMIRROR_HEATING("side_mirror_heaters", "sidemirrorheaters", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        SMART_PRECONDITIONING("smart_preconditioning", "smartpreconditioning", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        SOC("soc", "soc", PercentType.class, false),
        SOFTWARE_UPDATE_STATUS("status", "softwareupdatestatus", StringType.class, false),
        SOFTWARE_UPDATE_VERSION("version", "softwareupdateversion", StringType.class, false),
        SPEED("speed", "speed", DecimalType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                State someState = super.getState(s);
                if (someState != UnDefType.UNDEF) {
                    BigDecimal value = ((DecimalType) someState).toBigDecimal();
                    return new QuantityType<>(value, ImperialUnits.MILES_PER_HOUR);
                } else {
                    return UnDefType.UNDEF;
                }
            }
        },
        STATE("state", "state", StringType.class, false),
        STEERINGWHEEL_HEATER("steering_wheel_heater", "steeringwheelheater", OnOffType.class, true) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        SUN_ROOF_PRESENT("sun_roof_installed", "sunroofinstalled", OnOffType.class, true) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        SUN_ROOF_STATE("sun_roof_state", "sunroofstate", StringType.class, false),
        SUN_ROOF("sun_roof_percent_open", "sunroof", PercentType.class, false),
        COMBINED_TEMP(null, "combinedtemp", DecimalType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                State someState = super.getState(s);
                BigDecimal value = ((DecimalType) someState).toBigDecimal();
                return new QuantityType<>(value, SIUnits.CELSIUS);
            }
        },
        TIME_TO_FULL_CHARGE("time_to_full_charge", "timetofullcharge", DecimalType.class, false),
        TIMESTAMP("timestamp", "eventstamp", DateTimeType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                Date date = new Date(Long.parseLong(s));
                SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                return super.getState(dateFormatter.format(date));
            }
        },
        TRIP_CARGING("trip_charging", "tripcharging", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        USABLE_BATTERY_LEVEL("usable_battery_level", "usablebatterylevel", DecimalType.class, false),
        USER_CHARGE_ENABLE_REQUEST("user_charge_enable_request", "userchargeenablerequest", StringType.class, false),
        VEHICLE_NAME("vehicle_name", "name", StringType.class, true) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                return super.getState(s.replace("\"", ""));
            }
        },
        VALET_MODE("valet_mode", "valetmode", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        VALET_PIN("valet_pin_needed", "valetpin", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        VIN("vin", "vin", StringType.class, true),
        WAKEUP(null, "wakeup", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        WIPERBLADE_HEATER("wiper_blade_heater", "wiperbladeheater", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if ("true".equals(s) || "1".equals(s)) {
                    return super.getState("ON");
                }
                if ("false".equals(s) || "0".equals(s)) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        WHEEL_TYPE("wheel_type", "wheeltype", StringType.class, true);

        private final @Nullable String restID;
        private final String channelID;
        private Class<? extends Type> typeClass;
        private final boolean isProperty;

        private TeslaChannelSelector(@Nullable String restID, String channelID, Class<? extends Type> typeClass,
                boolean isProperty) {
            this.restID = restID;
            this.channelID = channelID;
            this.typeClass = typeClass;
            this.isProperty = isProperty;
        }

        @Override
        public String toString() {
            String restID = this.restID;
            return restID != null ? restID : "null";
        }

        public String getChannelID() {
            return channelID;
        }

        public Class<? extends Type> getTypeClass() {
            return typeClass;
        }

        public boolean isProperty() {
            return isProperty;
        }

        public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
            return getState(s);
        }

        public State getState(String s) {
            try {
                Method valueOf = typeClass.getMethod("valueOf", String.class);
                State state = (State) valueOf.invoke(typeClass, s);
                if (state != null) {
                    return state;
                }
            } catch (NoSuchMethodException | IllegalArgumentException | IllegalAccessException
                    | InvocationTargetException e) {
            }

            return UnDefType.UNDEF;
        }

        public static TeslaChannelSelector getValueSelectorFromChannelID(String valueSelectorText)
                throws IllegalArgumentException {
            for (TeslaChannelSelector c : TeslaChannelSelector.values()) {
                if (c.channelID.equals(valueSelectorText)) {
                    return c;
                }
            }

            throw new IllegalArgumentException("Not valid value selector. Received Selector: " + valueSelectorText);
        }

        public static TeslaChannelSelector getValueSelectorFromRESTID(String valueSelectorText)
                throws IllegalArgumentException {
            for (TeslaChannelSelector c : TeslaChannelSelector.values()) {
                String restID = c.restID;
                if (restID != null && restID.equals(valueSelectorText)) {
                    return c;
                }
            }

            throw new IllegalArgumentException("Not valid value selector. Received Selector: " + valueSelectorText);
        }
    }

    public String latitude = "0";
    public String longitude = "0";
    public String elevation = "0";
    public String nativeLatitude = "0";
    public String nativeLongitude = "0";
    public String arLatitude = "0";
    public String arLongitude = "0";

    public State getState(String s, TeslaChannelSelector selector, Map<String, String> properties) {
        return selector.getState(s, this, properties);
    }
}
