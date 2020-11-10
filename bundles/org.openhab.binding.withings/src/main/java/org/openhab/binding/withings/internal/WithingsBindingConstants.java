/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.withings.internal;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link WithingsBindingConstants} class defines common constants, which are used
 * across the whole binding.
 *
 * @author Sven Strohschein - Initial contribution
 */
@NonNullByDefault
public class WithingsBindingConstants {

    private static final String BINDING_ID = "withings";
    public static final String VENDOR = "Withings";

    public static final int REFRESH_SECONDS = 300;
    public static final int TOKEN_REFRESH_SECONDS = 3600;

    public static final String CONFIG_AUTH_CODE = "authCode";

    public static final String PROPERTY_DEVICE_ID = "deviceId";
    public static final String PROPERTY_DEVICE_MODEL = "model";
    public static final String PROPERTY_PERSON_USER_ID = "userId";

    public static final String CHANNEL_SCALE_BATTERY_LEVEL = "scaleBatteryLevel";
    public static final String CHANNEL_SCALE_LAST_CONNECTION = "scaleLastConnection";
    public static final String CHANNEL_SLEEP_MONITOR_LAST_CONNECTION = "sleepMonitorLastConnection";
    public static final String CHANNEL_PERSON_WEIGHT = "personWeight";
    public static final String CHANNEL_PERSON_HEIGHT = "personHeight";
    public static final String CHANNEL_PERSON_FAT_MASS = "personFatMass";
    public static final String CHANNEL_PERSON_LAST_SLEEP_START = "personLastSleepStart";
    public static final String CHANNEL_PERSON_LAST_SLEEP_END = "personLastSleepEnd";
    public static final String CHANNEL_PERSON_LAST_SLEEP_SCORE = "personLastSleepScore";

    public static final ThingTypeUID APIBRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, "withingsapi");

    public static final ThingTypeUID SCALE_THING_TYPE = new ThingTypeUID(BINDING_ID, "Scale");
    public static final ThingTypeUID SLEEP_MONITOR_THING_TYPE = new ThingTypeUID(BINDING_ID, "SleepMonitor");
    public static final ThingTypeUID PERSON_THING_TYPE = new ThingTypeUID(BINDING_ID, "Person");

    public static final Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES_UIDS = Stream
            .of(SCALE_THING_TYPE, SLEEP_MONITOR_THING_TYPE, PERSON_THING_TYPE).collect(Collectors.toSet());

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .concat(SUPPORTED_DEVICE_THING_TYPES_UIDS.stream(), Stream.of(APIBRIDGE_THING_TYPE))
            .collect(Collectors.toSet());
}
