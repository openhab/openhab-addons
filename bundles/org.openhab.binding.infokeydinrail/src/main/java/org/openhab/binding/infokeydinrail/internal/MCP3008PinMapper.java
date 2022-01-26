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
package org.openhab.binding.infokeydinrail.internal;

import static com.pi4j.gpio.extension.mcp.MCP3008Pin.*;
import static org.openhab.binding.infokeydinrail.internal.InfokeyBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;

/**
 * @author Themistoklis Anastasopoulos - Initial contribution
 */

public class MCP3008PinMapper {

    private static final Map<String, Pin> MCP3008_PIN_MAP = new HashMap<>();
    private static final Map<Integer, Pin> MCP3008_CS_PIN_MAP = new HashMap<>();

    static {
        MCP3008_PIN_MAP.put(CHANNEL_0, CH0);
        MCP3008_PIN_MAP.put(CHANNEL_1, CH1);
        MCP3008_PIN_MAP.put(CHANNEL_2, CH2);
        MCP3008_PIN_MAP.put(CHANNEL_3, CH3);
        MCP3008_PIN_MAP.put(CHANNEL_4, CH4);
        MCP3008_PIN_MAP.put(CHANNEL_5, CH5);
        MCP3008_PIN_MAP.put(CHANNEL_6, CH6);
        MCP3008_PIN_MAP.put(CHANNEL_7, CH7);

        MCP3008_CS_PIN_MAP.put(6, RaspiPin.GPIO_22);
        MCP3008_CS_PIN_MAP.put(8, RaspiPin.GPIO_10);
        MCP3008_CS_PIN_MAP.put(12, RaspiPin.GPIO_26);
        MCP3008_CS_PIN_MAP.put(13, RaspiPin.GPIO_23);
        MCP3008_CS_PIN_MAP.put(16, RaspiPin.GPIO_27);
        MCP3008_CS_PIN_MAP.put(17, RaspiPin.GPIO_00);
        MCP3008_CS_PIN_MAP.put(18, RaspiPin.GPIO_01);
        MCP3008_CS_PIN_MAP.put(22, RaspiPin.GPIO_03);
        MCP3008_CS_PIN_MAP.put(23, RaspiPin.GPIO_04);
        MCP3008_CS_PIN_MAP.put(24, RaspiPin.GPIO_05);
        MCP3008_CS_PIN_MAP.put(26, RaspiPin.GPIO_25);
        MCP3008_CS_PIN_MAP.put(27, RaspiPin.GPIO_02);
    }

    public static Pin get(String pinCode) {
        return MCP3008_PIN_MAP.get(pinCode);
    }

    public static Pin getCS(Integer pinCode) {
        return MCP3008_CS_PIN_MAP.get(pinCode);
    }
}
