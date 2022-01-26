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

import static com.pi4j.gpio.extension.mcp.MCP23017Pin.*;
import static org.openhab.binding.infokeydinrail.internal.InfokeyBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;

/**
 * @author Themistoklis Anastasopoulos - Initial contribution
 */
public class PinMapper {

    private static final Map<String, Pin> PIN_MAP = new HashMap<>();
    private static final Map<Integer, Pin> RASPI_PIN_MAP = new HashMap<>();

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

        RASPI_PIN_MAP.put(6, RaspiPin.GPIO_22);
        RASPI_PIN_MAP.put(12, RaspiPin.GPIO_26);
        RASPI_PIN_MAP.put(13, RaspiPin.GPIO_23);
        RASPI_PIN_MAP.put(16, RaspiPin.GPIO_27);
        RASPI_PIN_MAP.put(17, RaspiPin.GPIO_00);
        RASPI_PIN_MAP.put(18, RaspiPin.GPIO_01);
        RASPI_PIN_MAP.put(22, RaspiPin.GPIO_03);
        RASPI_PIN_MAP.put(23, RaspiPin.GPIO_04);
        RASPI_PIN_MAP.put(24, RaspiPin.GPIO_05);
        RASPI_PIN_MAP.put(26, RaspiPin.GPIO_25);
        RASPI_PIN_MAP.put(27, RaspiPin.GPIO_02);
    }

    public static Pin get(String pinCode) {
        return PIN_MAP.get(pinCode);
    }

    public static Pin getRaspiPin(Integer pinCode) {
        return RASPI_PIN_MAP.get(pinCode);
    }
}
