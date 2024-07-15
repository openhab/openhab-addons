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
package org.openhab.binding.tsmart.internal;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link TSmartBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author James Melville - Initial contribution
 */
@NonNullByDefault
public class TSmartBindingConstants {
    private static final String BINDING_ID = "tsmart";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_T_SMART = new ThingTypeUID(BINDING_ID, "thermostat");

    // List of all Channel ids
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_TEMPERATURE_HIGH = "temperature-high";
    public static final String CHANNEL_TEMPERATURE_LOW = "temperature-low";
    public static final String CHANNEL_SETPOINT = "setpoint";
    public static final String CHANNEL_MODE = "mode";
    public static final String CHANNEL_SMART_STATE = "smart-state";
    public static final String CHANNEL_RELAY = "relay";

    // list properties
    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_HOSTNAME = "hostname";

    // UDP port used by the t-smart device
    public static final int T_SMART_PORT = 1337;

    // device modes
    public static final List<String> MODES = Arrays
            .asList(new String[] { "Manual", "Eco", "Smart", "Timer", "Travel", "Boost" });
    public static final String[] SMART_MODES = new String[] { "Uninitialised", "Idle", "Recording", "Recording" };
}
