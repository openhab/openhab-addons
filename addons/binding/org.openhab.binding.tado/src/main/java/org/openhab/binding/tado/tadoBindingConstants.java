/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tado;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link tadoBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Ben Woodford - Initial contribution
 */
public class tadoBindingConstants {

    public static final String BINDING_ID = "tado";

    public static final String CLIENT_ID = "tado-webapp";

    public final static String API_NAME = "Tado API";
    public final static String API_VERSION = "api/v2/";
    public final static String API_URI = "https://my.tado.com/";
    public final static String ACCESS_TOKEN_URI = "oauth/token";
    public final static String API_REFERER = "https://my.tado.com/";

    public final static String EMAIL = "email";
    public final static String PASSWORD = "password";
    public final static String USE_CELSIUS = "useCelsius";
    public final static String REFRESH_INTERVAL = "refreshInterval";

    public final static String KEY_CELSIUS = "celsius";
    public final static String KEY_FAHRENHEIT = "fahrenheit";

    public static final String HOMES = "homes";
    public static final String HOME_ID_PATH = "/{homeId}/";
    public static final String ZONES = "zones";
    public static final String ZONE_ID_PATH = "/{zoneId}/";
    public static final String STATE = "state";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_THERMOSTAT = new ThingTypeUID(BINDING_ID, "thermostat");

    public final static String CHANNEL_MODE = "mode";
    public final static String CHANNEL_HUMIDITY = "humidity";
    public final static String CHANNEL_INSIDE_TEMPERATURE = "insideTemperature";
    public final static String CHANNEL_OUTSIDE_TEMPERATURE = "outsideTemperature";
    public final static String CHANNEL_SOLAR_INTENSITY = "solarIntensity";
    public final static String CHANNEL_WEATHER_STATE = "weatherState";
    public final static String CHANNEL_LINK_STATE = "linkState";
    public final static String CHANNEL_HEATING_STATE = "heatingState";
    public final static String CHANNEL_TARGET_TEMPERATURE = "targetTemperature";
    public final static String CHANNEL_SERVER_STATUS = "serverStatus";

    public final static String HOME_ID = "homeId";
    public final static String ZONE_ID = "zoneId";

}
