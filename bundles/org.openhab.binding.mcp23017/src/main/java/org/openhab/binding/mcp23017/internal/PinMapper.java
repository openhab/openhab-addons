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
package org.openhab.binding.mcp23017.internal;

import static com.pi4j.gpio.extension.mcp.MCP23017Pin.*;
import static org.openhab.binding.mcp23017.internal.Mcp23017BindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import com.pi4j.io.gpio.Pin;

/**
 * @author Anatol Ogorek - Initial contribution
 */
public class PinMapper {

    private static final Map<String, Pin> PIN_MAP = new HashMap<>();

    static {
        PIN_MAP.put(CHANNEL_A0, GPIO_A0);
        PIN_MAP.put(CHANNEL_A1, GPIO_A1);
        PIN_MAP.put(CHANNEL_A2, GPIO_A2);
        PIN_MAP.put(CHANNEL_A3, GPIO_A3);
        PIN_MAP.put(CHANNEL_A4, GPIO_A4);
        PIN_MAP.put(CHANNEL_A5, GPIO_A5);
        PIN_MAP.put(CHANNEL_A6, GPIO_A6);
        PIN_MAP.put(CHANNEL_A7, GPIO_A7);
        PIN_MAP.put(CHANNEL_B0, GPIO_B0);
        PIN_MAP.put(CHANNEL_B1, GPIO_B1);
        PIN_MAP.put(CHANNEL_B2, GPIO_B2);
        PIN_MAP.put(CHANNEL_B3, GPIO_B3);
        PIN_MAP.put(CHANNEL_B4, GPIO_B4);
        PIN_MAP.put(CHANNEL_B5, GPIO_B5);
        PIN_MAP.put(CHANNEL_B6, GPIO_B6);
        PIN_MAP.put(CHANNEL_B7, GPIO_B7);
    }

    public static Pin get(String pinCode) {
        return PIN_MAP.get(pinCode);
    }
}
