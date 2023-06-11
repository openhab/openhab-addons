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
package org.openhab.binding.millheat.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * The {@link MillheatBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public class MillheatBindingConstants {
    private static final String BINDING_ID = "millheat";
    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_HOME = new ThingTypeUID(BINDING_ID, "home");
    public static final ThingTypeUID THING_TYPE_ROOM = new ThingTypeUID(BINDING_ID, "room");
    public static final ThingTypeUID THING_TYPE_HEATER = new ThingTypeUID(BINDING_ID, "heater");
    // List of all Channel ids
    public static final String CHANNEL_CURRENT_TEMPERATURE = "currentTemperature";
    public static final String CHANNEL_COMFORT_TEMPERATURE = "comfortTemperature";
    public static final String CHANNEL_SLEEP_TEMPERATURE = "sleepTemperature";
    public static final String CHANNEL_AWAY_TEMPERATURE = "awayTemperature";
    public static final String CHANNEL_HEATING_ACTIVE = "heatingActive";
    public static final String CHANNEL_FAN_ACTIVE = "fanActive";
    public static final String CHANNEL_TARGET_TEMPERATURE = "targetTemperature";
    public static final String CHANNEL_CURRENT_POWER = "currentEnergy";
    public static final String CHANNEL_CURRENT_MODE = "currentMode";
    public static final String CHANNEL_PROGRAM = "program";
    public static final String CHANNEL_INDEPENDENT = "independent";
    public static final String CHANNEL_WINDOW_STATE = "window";
    public static final String CHANNEL_MASTER_SWITCH = "masterSwitch";

    // Vacation mode channels
    public static final String CHANNEL_HOME_VACATION_TARGET_TEMPERATURE = "vacationModeTargetTemperature";
    public static final String CHANNEL_HOME_VACATION_MODE = "vacationMode";
    public static final String CHANNEL_HOME_VACATION_MODE_ADVANCED = "vacationModeAdvanced";
    public static final String CHANNEL_HOME_VACATION_MODE_START = "vacationModeStart";
    public static final String CHANNEL_HOME_VACATION_MODE_END = "vacationModeEnd";

    public static final String CHANNEL_TYPE_MASTER_SWITCH = "masterSwitch";
    public static final String CHANNEL_TYPE_TARGET_TEMPERATURE_HEATER = "targetTemperatureHeater";

    public static final ChannelTypeUID CHANNEL_TYPE_MASTER_SWITCH_UID = new ChannelTypeUID(BINDING_ID,
            CHANNEL_TYPE_MASTER_SWITCH);
    public static final ChannelTypeUID CHANNEL_TYPE_TARGET_TEMPERATURE_HEATER_UID = new ChannelTypeUID(BINDING_ID,
            CHANNEL_TYPE_TARGET_TEMPERATURE_HEATER);
}
