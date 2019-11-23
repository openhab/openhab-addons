/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.venstarthermostat.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link VenstarThermostatBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author William Welliver - Initial contribution
 */
@NonNullByDefault
public class VenstarThermostatBindingConstants {

    public static final String BINDING_ID = "venstarthermostat";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_COLOR_TOUCH = new ThingTypeUID(BINDING_ID, "colorTouchThermostat");

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_COLOR_TOUCH);
    // List of all Channel ids
    public final static String CHANNEL_TEMPERATURE = "temperature";
    public final static String CHANNEL_HUMIDITY = "humidity";
    public final static String CHANNEL_EXTERNAL_TEMPERATURE = "outdoorTemperature";

    public final static String CHANNEL_HEATING_SETPOINT = "heatingSetpoint";
    public final static String CHANNEL_COOLING_SETPOINT = "coolingSetpoint";
    public final static String CHANNEL_SYSTEM_STATE = "systemState";
    public final static String CHANNEL_SYSTEM_MODE = "systemMode";
    public final static String CHANNEL_SYSTEM_STATE_RAW = "systemStateRaw";
    public final static String CHANNEL_SYSTEM_MODE_RAW = "systemModeRaw";

    public final static String CONFIG_USERNAME = "username";
    public final static String CONFIG_PASSWORD = "password";
    public final static String CONFIG_REFRESH = "refresh";

    public final static String PROPERTY_URL = "url";
    public final static String PROPERTY_UUID = "uuid";

    public final static String REFRESH_INVALID = "refresh-invalid";
    public final static String EMPTY_INVALID = "empty-invalid";
}
