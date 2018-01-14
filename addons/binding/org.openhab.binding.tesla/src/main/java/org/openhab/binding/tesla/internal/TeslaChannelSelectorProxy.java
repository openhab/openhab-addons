/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tesla.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.tesla.TeslaBindingConstants;

/**
 * The {@link TeslaChannelSelectorProxy} class is a helper class to instantiate
 * and parameterize the {@link TeslaChannelSelector} Enum
 *
 * @author Karel Goderis - Initial contribution
 */
public class TeslaChannelSelectorProxy {

    public enum TeslaChannelSelector {

        AUTO_COND("is_auto_conditioning_on", "autoconditioning", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("ON");
                }
                if (s.equals("false") || s.equals("0")) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        API("api_version", "api", DecimalType.class, true),
        BATTERY_CURRENT("battery_current", "batterycurrent", DecimalType.class, false),
        BATTERY_HEATER("battery_heater_on", "batteryheater", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("ON");
                }
                if (s.equals("false") || s.equals("0")) {
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
                double odo = ((DecimalType) someState).doubleValue();
                if (properties.containsKey("distanceunits") && properties.get("distanceunits").equals("km/hr")) {
                    return super.getState(String.valueOf(odo * 1.609344));
                } else {
                    return someState;
                }

            }
        },
        CALENDAR_SUPPORTED("calendar_supported", "calendarsupported", OnOffType.class, true) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("ON");
                }
                if (s.equals("false") || s.equals("0")) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        CALENDAR_ENABLED("calendar_enabled", "calendarenabled", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("ON");
                }
                if (s.equals("false") || s.equals("0")) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        CAR_VERSION("car_version", "version", StringType.class, true),
        CENTER_DISPLAY("center_display_state", "centerdisplay", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("ON");
                }
                if (s.equals("false") || s.equals("0")) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        CHARGE(null, "charge", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("ON");
                }
                if (s.equals("false") || s.equals("0")) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        CHARGE_ENABLE_REQUEST("charge_enable_request", "chargeenablerequest", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("ON");
                }
                if (s.equals("false") || s.equals("0")) {
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
        CHARGE_MILES_ADDED_IDEAL("charge_miles_added_ideal", "chargeidealmilesadded", DecimalType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                State someState = super.getState(s);
                double odo = ((DecimalType) someState).doubleValue();
                if (properties.containsKey("distanceunits") && properties.get("distanceunits").equals("km/hr")) {
                    return super.getState(String.valueOf(odo * 1.609344));
                } else {
                    return someState;
                }

            }
        },
        CHARGE_MILES_ADDED_RANGE("charge_miles_added_rated", "chargeratedmilesadded", DecimalType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                State someState = super.getState(s);
                double odo = ((DecimalType) someState).doubleValue();
                if (properties.containsKey("distanceunits") && properties.get("distanceunits").equals("km/hr")) {
                    return super.getState(String.valueOf(odo * 1.609344));
                } else {
                    return someState;
                }

            }
        },
        CHARGE_RATE("charge_rate", "chargerate", DecimalType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                State someState = super.getState(s);
                double odo = ((DecimalType) someState).doubleValue();
                if (properties.containsKey("chargerateunits") && properties.get("chargerateunits").equals("km/hr")) {
                    return super.getState(String.valueOf(odo * 1.609344));
                } else {
                    return someState;
                }

            }
        },
        CHARGE_STARTING_RANGE("charge_starting_range", "chargestartingrange", StringType.class, false),
        CHARGE_STARTING_SOC("charge_starting_soc", "chargestartingsoc", StringType.class, false),
        CHARGE_STATE("charging_state", "chargingstate", StringType.class, false),
        CHARGE_TO_MAX("charge_to_max_range", "chargetomax", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("ON");
                }
                if (s.equals("false") || s.equals("0")) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        CHARGE_TO_MAX_COUNTER("max_range_charge_counter", "maxcharges", DecimalType.class, false),
        CHARGEPORT("charge_port_door_open", "chargeport", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("ON");
                }
                if (s.equals("false") || s.equals("0")) {
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
        COLOR("exterior_color", "color", StringType.class, true),
        DARK_RIMS("dark_rims", "darkrims", OnOffType.class, true) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("ON");
                }
                if (s.equals("false") || s.equals("0")) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        DISPLAY_NAME("display_name", "name", StringType.class, true),
        DF("df", "driverfrontdoor", OpenClosedType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("OPEN");
                }
                if (s.equals("false") || s.equals("0")) {
                    return super.getState("CLOSED");
                }
                return super.getState(s);
            }
        },
        DOOR_LOCK("locked", "doorlock", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("ON");
                }
                if (s.equals("false") || s.equals("0")) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        DR("dr", "driverreardoor", OpenClosedType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("OPEN");
                }
                if (s.equals("false") || s.equals("0")) {
                    return super.getState("CLOSED");
                }
                return super.getState(s);
            }
        },
        DRIVER_TEMP("driver_temp_setting", "drivertemp", DecimalType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                State someState = super.getState(s);
                if (properties.containsKey("temperatureunits") && properties.get("temperatureunits").equals("F")) {
                    return super.getState(String.valueOf(CelsiusToFahrenheit(((DecimalType) someState))));
                } else {
                    return someState;
                }
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
                double odo = ((DecimalType) someState).doubleValue();
                if (properties.containsKey("distanceunits") && properties.get("distanceunits").equals("km/hr")) {
                    return super.getState(String.valueOf(odo * 1.609344));
                } else {
                    return someState;
                }

            }
        },
        EST_HEADING("est_heading", "estimatedheading", DecimalType.class, false),
        EST_RANGE("est_range", "estimatedrange", DecimalType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                State someState = super.getState(s);
                double odo = ((DecimalType) someState).doubleValue();
                if (properties.containsKey("distanceunits") && properties.get("distanceunits").equals("km/hr")) {
                    return super.getState(String.valueOf(odo * 1.609344));
                } else {
                    return someState;
                }

            }
        },
        EU_VEHICLE("eu_vehicle", "european", OnOffType.class, true) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("ON");
                }
                if (s.equals("false") || s.equals("0")) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        FAN_STATUS("fan_status", "fan", DecimalType.class, false),
        FAST_CHARGER("fast_charger_present", "fastcharger", OnOffType.class, true) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("ON");
                }
                if (s.equals("false") || s.equals("0")) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        FAST_CHARGER_TYPE("fast_charger_type", "fastchargertype", StringType.class, true),
        FLASH(null, "flashlights", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("ON");
                }
                if (s.equals("false") || s.equals("0")) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        FRONT_DEFROSTER("is_front_defroster_on", "frontdefroster", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("ON");
                }
                if (s.equals("false") || s.equals("0")) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        FT("ft", "fronttrunk", OpenClosedType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("OPEN");
                }
                if (s.equals("false") || s.equals("0")) {
                    return super.getState("CLOSED");
                }
                return super.getState(s);
            }
        },
        GPS_AS_OF("gps_as_of", "gpstimestamp", DateTimeType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                Date date = new Date();
                SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

                date.setTime(Long.valueOf(s) * 1000);
                return super.getState(DATE_FORMATTER.format(date));
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
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("ON");
                }
                if (s.equals("false") || s.equals("0")) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        HEADING("heading", "heading", DecimalType.class, false),
        HONK_HORN(null, "honkhorn", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("ON");
                }
                if (s.equals("false") || s.equals("0")) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        IDEAL_BATTERY_RANGE("ideal_battery_range", "idealbatteryrange", DecimalType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                State someState = super.getState(s);
                double odo = ((DecimalType) someState).doubleValue();
                if (properties.containsKey("distanceunits") && properties.get("distanceunits").equals("km/hr")) {
                    return super.getState(String.valueOf(odo * 1.609344));
                } else {
                    return someState;
                }

            }
        },
        INSIDE_TEMP("inside_temp", "insidetemp", DecimalType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                State someState = super.getState(s);
                if (properties.containsKey("temperatureunits") && properties.get("temperatureunits").equals("F")) {
                    return super.getState(String.valueOf(CelsiusToFahrenheit(((DecimalType) someState))));
                } else {
                    return someState;
                }
            }
        },
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
        MOBILE_ENABLED(TeslaBindingConstants.TESLA_MOBILE_ENABLED_STATE, "mobileenabled", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true")) {
                    return super.getState("ON");
                }
                if (s.equals("false")) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        MOTORIZED_CHARGE_PORT("motorized_charge_port", "motorizedchargeport", OnOffType.class, true) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("ON");
                }
                if (s.equals("false") || s.equals("0")) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        NOT_ENOUGH_POWER_TO_HEAT("not_enough_power_to_heat", "notenoughpower", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("ON");
                }
                if (s.equals("false") || s.equals("0")) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        NOTIFICATIONS_ENABLED("notifications_enabled", "notificationsenabled", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("ON");
                }
                if (s.equals("false") || s.equals("0")) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        NOTIFICATIONS_SUPPORTED("notifications_supported", "notificationssupported", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("ON");
                }
                if (s.equals("false") || s.equals("0")) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        ODOMETER("odometer", "odometer", DecimalType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                State someState = super.getState(s);
                double odo = ((DecimalType) someState).doubleValue();
                if (properties.containsKey("distanceunits") && properties.get("distanceunits").equals("km/hr")) {
                    return super.getState(String.valueOf(odo * 1.609344));
                } else {
                    return someState;
                }

            }
        },
        OPTION_CODES("option_codes", "options", StringType.class, true),
        OUTSIDE_TEMP("outside_temp", "outsidetemp", DecimalType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                State someState = super.getState(s);
                if (properties.containsKey("temperatureunits") && properties.get("temperatureunits").equals("F")) {
                    return super.getState(String.valueOf(CelsiusToFahrenheit(((DecimalType) someState))));
                } else {
                    return someState;
                }
            }
        },
        PARSED_CALENDAR("parsed_calendar_supported", "parsedcalendar", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("ON");
                }
                if (s.equals("false") || s.equals("0")) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        PASSENGER_TEMP("passenger_temp_setting", "passengertemp", DecimalType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                State someState = super.getState(s);
                if (properties.containsKey("temperatureunits") && properties.get("temperatureunits").equals("F")) {
                    return super.getState(String.valueOf(CelsiusToFahrenheit(((DecimalType) someState))));
                } else {
                    return someState;
                }
            }
        },
        PERF_CONFIG("perf_config", "configuration", StringType.class, true),
        PF("pf", "passengerfrontdoor", OpenClosedType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("OPEN");
                }
                if (s.equals("false") || s.equals("0")) {
                    return super.getState("CLOSED");
                }
                return super.getState(s);
            }
        },
        POWER("power", "power", DecimalType.class, false),
        PR("pr", "passengerreardoor", OpenClosedType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("OPEN");
                }
                if (s.equals("false") || s.equals("0")) {
                    return super.getState("CLOSED");
                }
                return super.getState(s);
            }
        },
        RANGE("range", "range", DecimalType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                State someState = super.getState(s);
                double odo = ((DecimalType) someState).doubleValue();
                if (properties.containsKey("distanceunits") && properties.get("distanceunits").equals("km/hr")) {
                    return super.getState(String.valueOf(odo * 1.609344));
                } else {
                    return someState;
                }

            }
        },
        REAR_DEFROSTER("is_rear_defroster_on", "reardefroster", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("ON");
                }
                if (s.equals("false") || s.equals("0")) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        REAR_SEAT_HEATERS("rear_seat_heaters", "rearseatheaters", OnOffType.class, true) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("ON");
                }
                if (s.equals("false") || s.equals("0")) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        REMOTE_START("remote_start", "remotestart", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("ON");
                }
                if (s.equals("false") || s.equals("0")) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        REMOTE_START_ENABLED("remote_start_enabled", "remotestartenabled", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("ON");
                }
                if (s.equals("false") || s.equals("0")) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        REMOTE_START_SUPPORTED("remote_start_supported", "remotestartsuported", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("ON");
                }
                if (s.equals("false") || s.equals("0")) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        RHD("rhd", "rhd", OnOffType.class, true) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("ON");
                }
                if (s.equals("false") || s.equals("0")) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        ROOF_COLOR("roof_color", "roof", StringType.class, true),
        RT("rt", "reartrunk", OpenClosedType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("OPEN");
                }
                if (s.equals("false") || s.equals("0")) {
                    return super.getState("CLOSED");
                }
                return super.getState(s);
            }
        },
        SEAT_HEATER_LEFT("seat_heater_left", "leftseatheater", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("ON");
                }
                if (s.equals("false") || s.equals("0")) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        SEAT_HEATER_RIGHT("seat_heater_right", "rightseatheater", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("ON");
                }
                if (s.equals("false") || s.equals("0")) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        SEAT_HEATER_REAR_LEFT("seat_heater_rear_left", "leftrearseatheater", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("ON");
                }
                if (s.equals("false") || s.equals("0")) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        SEAT_HEATER_REAR_RIGHT("seat_heater_rear_right", "rightrearseatheater", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("ON");
                }
                if (s.equals("false") || s.equals("0")) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        SEAT_HEATER_REAR_CENTER("seat_heater_rear_center", "centerrearseatheater", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("ON");
                }
                if (s.equals("false") || s.equals("0")) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        SEAT_HEATER_REAR_RIGHT_BACK("seat_heater_rear_right_back", "rightrearbackseatheater", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("ON");
                }
                if (s.equals("false") || s.equals("0")) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        SEAT_HEATER_REAR_RIGHT_LEFT("seat_heater_rear_left_back", "leftrearbackseatheater", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("ON");
                }
                if (s.equals("false") || s.equals("0")) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        SEAT_TYPE("seat_type", "seattype", DecimalType.class, true),
        SCHEDULED_CHARGING_PENDING("scheduled_charging_pending", "scheduledchargingpending", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("ON");
                }
                if (s.equals("false") || s.equals("0")) {
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
                SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

                date.setTime(Long.valueOf(s) * 1000);
                return super.getState(DATE_FORMATTER.format(date));
            }
        },
        SHIFTSTATE("shift_state", "shiftstate", StringType.class, false),
        SMART_PRECONDITIONING("smart_preconditioning", "smartpreconditioning", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("ON");
                }
                if (s.equals("false") || s.equals("0")) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        SOC("soc", "soc", PercentType.class, false),
        SPEED("speed", "speed", DecimalType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                State someState = super.getState(s);
                if (someState != null) {
                    double odo = ((DecimalType) someState).doubleValue();
                    if (properties.containsKey("distanceunits") && properties.get("distanceunits").equals("km/hr")) {
                        return super.getState(String.valueOf(odo * 1.609344));
                    } else {
                        return someState;
                    }
                } else {
                    return someState;
                }
            }
        },
        STATE("state", "state", StringType.class, false),
        SUN_ROOF_PRESENT("sun_roof_installed", "sunroofinstalled", OnOffType.class, true) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("ON");
                }
                if (s.equals("false") || s.equals("0")) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        SUN_ROOF_STATE("sun_roof_state", "sunroofstate", StringType.class, false),
        SUN_ROOF("sun_roof_percent_open", "sunroof", PercentType.class, false),
        TEMPERATURE(null, "temperature", DecimalType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                State someState = super.getState(s);
                if (properties.containsKey("temperatureunits") && properties.get("temperatureunits").equals("F")) {
                    return super.getState(String.valueOf(CelsiusToFahrenheit(((DecimalType) someState))));
                } else {
                    return someState;
                }
            }
        },
        TIME_TO_FULL_CHARGE("time_to_full_charge", "timetofullcharge", DecimalType.class, false),
        TIMESTAMP("timestamp", "eventstamp", DateTimeType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                Date date = new Date(Long.valueOf(s));
                SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                return super.getState(dateFormatter.format(date));
            }
        },
        TRIP_CARGING("trip_charging", "tripcharging", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("ON");
                }
                if (s.equals("false") || s.equals("0")) {
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
                return super.getState(s.replaceAll("\"", ""));
            }
        },
        VALET_MODE("valet_mode", "valetmode", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("ON");
                }
                if (s.equals("false") || s.equals("0")) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        VALET_PIN("valet_pin_needed", "valetpin", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("ON");
                }
                if (s.equals("false") || s.equals("0")) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        VIN("vin", "vin", StringType.class, true),
        WAKEUP(null, "wakeup", OnOffType.class, false) {
            @Override
            public State getState(String s, TeslaChannelSelectorProxy proxy, Map<String, String> properties) {
                if (s.equals("true") || s.equals("1")) {
                    return super.getState("ON");
                }
                if (s.equals("false") || s.equals("0")) {
                    return super.getState("OFF");
                }
                return super.getState(s);
            }
        },
        WHEEL_TYPE("wheel_type", "wheeltype", StringType.class, true);

        private final String RESTID;
        private final String channelID;
        private Class<? extends Type> typeClass;
        private final boolean isProperty;

        private TeslaChannelSelector(String RESTID, String channelID, Class<? extends Type> typeClass,
                boolean isProperty) {
            this.RESTID = RESTID;
            this.channelID = channelID;
            this.typeClass = typeClass;
            this.isProperty = isProperty;
        }

        @Override
        public String toString() {
            return RESTID;
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
            } catch (NoSuchMethodException e) {
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            }

            return null;
        }

        public static TeslaChannelSelector getValueSelectorFromChannelID(String valueSelectorText)
                throws IllegalArgumentException {

            for (TeslaChannelSelector c : TeslaChannelSelector.values()) {
                if (c.channelID.equals(valueSelectorText)) {
                    return c;
                }
            }

            throw new IllegalArgumentException("Not valid value selector");
        }

        public static TeslaChannelSelector getValueSelectorFromRESTID(String valueSelectorText)
                throws IllegalArgumentException {

            for (TeslaChannelSelector c : TeslaChannelSelector.values()) {
                if (c.RESTID != null && c.RESTID.equals(valueSelectorText)) {
                    return c;
                }
            }

            throw new IllegalArgumentException("Not valid value selector");
        }
    }

    public String latitude = "0";
    public String longitude = "0";
    public String elevation = "0";

    public State getState(String s, TeslaChannelSelector selector, Map<String, String> properties) {
        return selector.getState(s, this, properties);
    }

    private static int CelsiusToFahrenheit(DecimalType c) {
        float cTemp = c.floatValue();
        return (int) Math.round((cTemp * 9.0 / 5.0) + 32.0);
    }

}
