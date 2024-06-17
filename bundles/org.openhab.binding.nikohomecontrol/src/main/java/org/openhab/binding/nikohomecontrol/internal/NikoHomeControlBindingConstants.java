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
package org.openhab.binding.nikohomecontrol.internal;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link NikoHomeControlBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public class NikoHomeControlBindingConstants {

    public static final String BINDING_ID = "nikohomecontrol";

    // Listener threadname prefix
    public static final String THREAD_NAME_PREFIX = "OH-binding-";

    // List of all Thing Type UIDs

    // bridge
    public static final ThingTypeUID BRIDGEI_THING_TYPE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID BRIDGEII_THING_TYPE = new ThingTypeUID(BINDING_ID, "bridge2");

    // generic thing types
    public static final ThingTypeUID THING_TYPE_PUSHBUTTON = new ThingTypeUID(BINDING_ID, "pushButton");
    public static final ThingTypeUID THING_TYPE_ON_OFF_LIGHT = new ThingTypeUID(BINDING_ID, "onOff");
    public static final ThingTypeUID THING_TYPE_DIMMABLE_LIGHT = new ThingTypeUID(BINDING_ID, "dimmer");
    public static final ThingTypeUID THING_TYPE_BLIND = new ThingTypeUID(BINDING_ID, "blind");
    public static final ThingTypeUID THING_TYPE_THERMOSTAT = new ThingTypeUID(BINDING_ID, "thermostat");
    public static final ThingTypeUID THING_TYPE_ENERGYMETER_LIVE = new ThingTypeUID(BINDING_ID, "energyMeterLive");
    public static final ThingTypeUID THING_TYPE_ENERGYMETER = new ThingTypeUID(BINDING_ID, "energyMeter");
    public static final ThingTypeUID THING_TYPE_GASMETER = new ThingTypeUID(BINDING_ID, "gasMeter");
    public static final ThingTypeUID THING_TYPE_WATERMETER = new ThingTypeUID(BINDING_ID, "waterMeter");
    public static final ThingTypeUID THING_TYPE_ACCESS = new ThingTypeUID(BINDING_ID, "access");
    public static final ThingTypeUID THING_TYPE_ACCESS_RINGANDCOMEIN = new ThingTypeUID(BINDING_ID,
            "accessRingAndComeIn");
    public static final ThingTypeUID THING_TYPE_ALARM = new ThingTypeUID(BINDING_ID, "alarm");

    // thing type sets
    public static final Set<ThingTypeUID> BRIDGE_THING_TYPES_UIDS = Set.of(BRIDGEI_THING_TYPE, BRIDGEII_THING_TYPE);
    public static final Set<ThingTypeUID> ACTION_THING_TYPES_UIDS = Set.of(THING_TYPE_PUSHBUTTON,
            THING_TYPE_ON_OFF_LIGHT, THING_TYPE_DIMMABLE_LIGHT, THING_TYPE_BLIND);
    public static final Set<ThingTypeUID> THERMOSTAT_THING_TYPES_UIDS = Set.of(THING_TYPE_THERMOSTAT);
    public static final Set<ThingTypeUID> METER_THING_TYPES_UIDS = Set.of(THING_TYPE_ENERGYMETER_LIVE,
            THING_TYPE_ENERGYMETER, THING_TYPE_GASMETER, THING_TYPE_WATERMETER);
    public static final Set<ThingTypeUID> ACCESS_THING_TYPES_UIDS = Set.of(THING_TYPE_ACCESS,
            THING_TYPE_ACCESS_RINGANDCOMEIN);
    public static final Set<ThingTypeUID> ALARM_THING_TYPES_UIDS = Set.of(THING_TYPE_ALARM);
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .of(BRIDGE_THING_TYPES_UIDS.stream(), ACTION_THING_TYPES_UIDS.stream(),
                    THERMOSTAT_THING_TYPES_UIDS.stream(), METER_THING_TYPES_UIDS.stream(),
                    ACCESS_THING_TYPES_UIDS.stream(), ALARM_THING_TYPES_UIDS.stream())
            .flatMap(i -> i).collect(Collectors.toSet());

    // List of all Channel ids
    public static final String CHANNEL_BUTTON = "button";
    public static final String CHANNEL_SWITCH = "switch";
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_ROLLERSHUTTER = "rollershutter";

    public static final String CHANNEL_MEASURED = "measured";
    public static final String CHANNEL_SETPOINT = "setpoint";
    public static final String CHANNEL_OVERRULETIME = "overruletime";
    public static final String CHANNEL_MODE = "mode";
    public static final String CHANNEL_DEMAND = "demand";
    public static final String CHANNEL_HEATING_MODE = "heatingmode";
    public static final String CHANNEL_HEATING_DEMAND = "heatingdemand";

    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_ENERGY = "energy";
    public static final String CHANNEL_GAS = "gas";
    public static final String CHANNEL_WATER = "water";
    public static final String CHANNEL_ENERGY_DAY = "energyday";
    public static final String CHANNEL_GAS_DAY = "gasday";
    public static final String CHANNEL_WATER_DAY = "waterday";
    public static final String CHANNEL_ENERGY_LAST = "energylast";
    public static final String CHANNEL_GAS_LAST = "gaslast";
    public static final String CHANNEL_WATER_LAST = "waterlast";

    public static final String CHANNEL_BELL_BUTTON = "bellbutton";
    public static final String CHANNEL_RING_AND_COME_IN = "ringandcomein";
    public static final String CHANNEL_LOCK = "lock";

    public static final String CHANNEL_ARM = "arm";
    public static final String CHANNEL_ARMED = "armed";
    public static final String CHANNEL_STATE = "state";

    public static final String CHANNEL_ALARM = "alarm";
    public static final String CHANNEL_NOTICE = "notice";

    // Bridge config properties
    public static final String CONFIG_HOST_NAME = "addr";
    public static final String CONFIG_PORT = "port";
    public static final String CONFIG_REFRESH = "refresh";
    public static final String CONFIG_PROFILE = "profile";
    public static final String CONFIG_PASSWORD = "password";

    // Thing config properties
    public static final String CONFIG_ACTION_ID = "actionId";
    public static final String CONFIG_STEP_VALUE = "step";
    public static final String CONFIG_INVERT = "invert";

    public static final String CONFIG_THERMOSTAT_ID = "thermostatId";
    public static final String CONFIG_OVERRULETIME = "overruleTime";

    public static final String METER_ID = "meterId";
    public static final String CONFIG_METER_REFRESH = "refresh";

    public static final String CONFIG_ACCESS_ID = "accessId";

    public static final String CONFIG_ALARM_ID = "alarmId";

    // Thing properties
    public static final String PROPERTY_DEVICE_TYPE = "deviceType";
    public static final String PROPERTY_DEVICE_TECHNOLOGY = "deviceTechnology";
    public static final String PROPERTY_DEVICE_MODEL = "deviceModel";
}
