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

import static com.pi4j.gpio.extension.pcf.PCF8574Pin.*;
import static org.openhab.binding.pcf8574.internal.Pcf8574BindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import com.pi4j.io.gpio.Pin;

/**
 * @author Tomasz Jagusz - Initial contribution, based on MCP23017 by Anatol Ogorek
 */
public class PinMapper {

    private static final Map<String, Pin> PIN_MAP = new HashMap<>();

    static {
        PIN_MAP.put(CHANNEL_00, GPIO_00);
        PIN_MAP.put(CHANNEL_01, GPIO_01);
        PIN_MAP.put(CHANNEL_02, GPIO_02);
        PIN_MAP.put(CHANNEL_03, GPIO_03);
        PIN_MAP.put(CHANNEL_04, GPIO_04);
        PIN_MAP.put(CHANNEL_05, GPIO_05);
        PIN_MAP.put(CHANNEL_06, GPIO_06);
        PIN_MAP.put(CHANNEL_07, GPIO_07);
    }

    public static Pin get(String pinCode) {
        return PIN_MAP.get(pinCode);
    }
}
