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
package org.openhab.binding.pcf8574.internal;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link Pcf8574BindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Tomasz Jagusz - Initial contribution, based on MCP23017 by Anatol Ogorek
 */
@NonNullByDefault
public class Pcf8574BindingConstants {

    public static final String BINDING_ID = "pcf8574";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_PCF8574 = new ThingTypeUID(BINDING_ID, BINDING_ID);

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

    public static final String CHANNEL_00 = "00";
    public static final String CHANNEL_01 = "01";
    public static final String CHANNEL_02 = "02";
    public static final String CHANNEL_03 = "03";
    public static final String CHANNEL_04 = "04";
    public static final String CHANNEL_05 = "05";
    public static final String CHANNEL_06 = "06";
    public static final String CHANNEL_07 = "07";

    public static final Set<String> SUPPORTED_CHANNELS = Arrays
            .asList(CHANNEL_00, CHANNEL_01, CHANNEL_02, CHANNEL_03, CHANNEL_04, CHANNEL_05, CHANNEL_06, CHANNEL_07)
            .stream().collect(Collectors.toSet());

}
