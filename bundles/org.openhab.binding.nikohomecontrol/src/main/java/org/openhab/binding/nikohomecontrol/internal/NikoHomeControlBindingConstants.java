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
package org.openhab.binding.nikohomecontrol.internal;

import java.util.Collections;
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
    public static final ThingTypeUID THING_TYPE_ENERGYMETER = new ThingTypeUID(BINDING_ID, "energyMeter");

    // thing type sets
    public static final Set<ThingTypeUID> BRIDGE_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(BRIDGEI_THING_TYPE, BRIDGEII_THING_TYPE).collect(Collectors.toSet()));
    public static final Set<ThingTypeUID> ACTION_THING_TYPES_UIDS = Collections.unmodifiableSet(
            Stream.of(THING_TYPE_PUSHBUTTON, THING_TYPE_ON_OFF_LIGHT, THING_TYPE_DIMMABLE_LIGHT, THING_TYPE_BLIND)
                    .collect(Collectors.toSet()));
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_PUSHBUTTON, THING_TYPE_ON_OFF_LIGHT, THING_TYPE_DIMMABLE_LIGHT,
                    THING_TYPE_BLIND, THING_TYPE_THERMOSTAT, THING_TYPE_ENERGYMETER).collect(Collectors.toSet()));

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

    public static final String CONFIG_THERMOSTAT_ID = "thermostatId";
    public static final String CONFIG_OVERRULETIME = "overruleTime";

    public static final String CONFIG_ENERGYMETER_ID = "energyMeterId";

    // Thing properties
    public static final String PROPERTY_DEVICE_TYPE = "deviceType";
    public static final String PROPERTY_DEVICE_TECHNOLOGY = "deviceTechnology";
    public static final String PROPERTY_DEVICE_MODEL = "deviceModel";
}
