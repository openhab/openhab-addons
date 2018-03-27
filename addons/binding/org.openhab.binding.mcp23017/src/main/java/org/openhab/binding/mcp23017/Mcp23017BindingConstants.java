/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mcp23017;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.Lists;

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

    public static final String CHANNEL_GROUP_INPUT = "input";
    public static final String CHANNEL_GROUP_OUTPUT = "output";

    public static final List<String> SUPPORTED_CHANNEL_GROUPS = Lists.newArrayList(CHANNEL_GROUP_INPUT,
            CHANNEL_GROUP_OUTPUT);

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

    public static final List<String> SUPPORTED_CHANNELS = Lists.newArrayList(CHANNEL_A0, CHANNEL_A1, CHANNEL_A2,
            CHANNEL_A3, CHANNEL_A4, CHANNEL_A5, CHANNEL_A6, CHANNEL_A7, CHANNEL_B0, CHANNEL_B1, CHANNEL_B2, CHANNEL_B3,
            CHANNEL_B4, CHANNEL_B5, CHANNEL_B6, CHANNEL_B7);

}
