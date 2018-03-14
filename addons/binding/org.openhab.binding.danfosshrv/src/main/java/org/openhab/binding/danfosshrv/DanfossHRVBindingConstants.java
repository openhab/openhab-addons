/**
 * Copyright (c) 2014,2018 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.danfosshrv;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link DanfossHRVBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Ralf Duckstein - Initial contribution
 */
@NonNullByDefault
public class DanfossHRVBindingConstants {

    private static final String BINDING_ID = "danfosshrv";

    // The only thing type UIDs
    public static final ThingTypeUID THING_TYPE_HRV = new ThingTypeUID(BINDING_ID, "hrv");

    // The thing type as a set
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_HRV);

    // List of all Channel ids
    public static final String CHANNEL_MODE = "mode";
    public static final String CHANNEL_FAN_SPEED = "fan_speed";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_BATTERY_LIFE = "battery_life";
    public static final String CHANNEL_CURRENT_TIME = "current_time";
}
