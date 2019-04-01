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
package org.openhab.binding.mcp23017.internal;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link Mcp23017BindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Anatol Ogorek - Initial contribution
 */
@NonNullByDefault
public class Mcp23017BindingConstants {

    public static final String BINDING_ID = "mcp23017";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_MCP23017 = new ThingTypeUID(BINDING_ID, BINDING_ID);

    public static final String DEFAULT_STATE = "default_state";
    public static final String ADDRESS = "address";
    public static final String BUS_NUMBER = "bus_number";
    public static final String PIN = "pin";
    public static final String PULL_MODE = "pull_mode";
    public static final String DEFAULT_PULL_MODE = "OFF";

    public static final String ACTIVE_LOW = "active_low";

    public static final String ACTIVE_LOW_ENABLED = "y";

    public static final String CHANNEL_GROUP_INPUT = "input";
    public static final String CHANNEL_GROUP_OUTPUT = "output";

    public static final Set<String> SUPPORTED_CHANNEL_GROUPS = Arrays.asList(CHANNEL_GROUP_INPUT, CHANNEL_GROUP_OUTPUT)
            .stream().collect(Collectors.toSet());

    public static final String CHANNEL_A0 = "A0";
    public static final String CHANNEL_A1 = "A1";
    public static final String CHANNEL_A2 = "A2";
    public static final String CHANNEL_A3 = "A3";
    public static final String CHANNEL_A4 = "A4";
    public static final String CHANNEL_A5 = "A5";
    public static final String CHANNEL_A6 = "A6";
    public static final String CHANNEL_A7 = "A7";
    public static final String CHANNEL_B0 = "B0";
    public static final String CHANNEL_B1 = "B1";
    public static final String CHANNEL_B2 = "B2";
    public static final String CHANNEL_B3 = "B3";
    public static final String CHANNEL_B4 = "B4";
    public static final String CHANNEL_B5 = "B5";
    public static final String CHANNEL_B6 = "B6";
    public static final String CHANNEL_B7 = "B7";

    public static final Set<String> SUPPORTED_CHANNELS = Arrays
            .asList(CHANNEL_A0, CHANNEL_A1, CHANNEL_A2, CHANNEL_A3, CHANNEL_A4, CHANNEL_A5, CHANNEL_A6, CHANNEL_A7,
                    CHANNEL_B0, CHANNEL_B1, CHANNEL_B2, CHANNEL_B3, CHANNEL_B4, CHANNEL_B5, CHANNEL_B6, CHANNEL_B7)
            .stream().collect(Collectors.toSet());

}
