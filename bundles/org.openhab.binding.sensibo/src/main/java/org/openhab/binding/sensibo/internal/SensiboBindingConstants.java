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
package org.openhab.binding.sensibo.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SensiboBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public class SensiboBindingConstants {
    public static final String BINDING_ID = "sensibo";
    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_SENSIBOSKY = new ThingTypeUID(BINDING_ID, "sensibosky");
    // Fixed channels
    public static final String CHANNEL_CURRENT_TEMPERATURE = "currentTemperature";
    public static final String CHANNEL_CURRENT_HUMIDITY = "currentHumidity";
    public static final String CHANNEL_MASTER_SWITCH = "masterSwitch";
    public static final String CHANNEL_TIMER = "timer";

    // Dynamic channels
    public static final String CHANNEL_FAN_LEVEL = "fanLevel";
    public static final String CHANNEL_MODE = "mode";
    public static final String CHANNEL_SWING_MODE = "swingMode";
    public static final String CHANNEL_TARGET_TEMPERATURE = "targetTemperature";

    public static final String CHANNEL_TYPE_FAN_LEVEL = "fanLevel";
    public static final String CHANNEL_TYPE_MODE = "mode";
    public static final String CHANNEL_TYPE_SWING_MODE = "swing";
    public static final String CHANNEL_TYPE_TARGET_TEMPERATURE = "targetTemperature";

    public static final Set<String> DYNAMIC_CHANNEL_TYPES = Collections.unmodifiableSet(Stream
            .of(CHANNEL_TYPE_FAN_LEVEL, CHANNEL_TYPE_MODE, CHANNEL_TYPE_SWING_MODE, CHANNEL_TYPE_TARGET_TEMPERATURE)
            .collect(Collectors.toSet()));
}
