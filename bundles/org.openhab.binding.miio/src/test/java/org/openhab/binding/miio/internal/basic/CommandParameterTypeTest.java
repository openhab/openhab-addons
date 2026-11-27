/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.miio.internal.basic;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Test case for reading and writing the {@link CommandParameterType} of a database action
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class CommandParameterTypeTest {

    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private static CommandParameterType read(String action) {
        MiIoDeviceAction deviceAction = GSON.fromJson(action, MiIoDeviceAction.class);
        assertNotNull(deviceAction);
        return deviceAction.getparameterType();
    }

    @Test
    public void parameterTypeIsNotCaseSensitiveTest() {
        assertEquals(CommandParameterType.ONOFF, read("{\"parameterType\":\"ONOFF\"}"));
        assertEquals(CommandParameterType.ONOFF, read("{\"parameterType\":\"onoff\"}"));
        assertEquals(CommandParameterType.ONOFFBOOL, read("{\"parameterType\":\"OnOffBool\"}"));
    }

    @Test
    public void missingParameterTypeIsEmptyTest() {
        assertEquals(CommandParameterType.EMPTY, read("{\"command\":\"set_power\"}"));
    }

    @Test
    public void unrecognizedParameterTypeIsUnknownTest() {
        assertEquals(CommandParameterType.UNKNOWN, read("{\"parameterType\":\"bogus\"}"));
        assertEquals(CommandParameterType.UNKNOWN, read("{\"parameterType\":\"\"}"));
        assertEquals(CommandParameterType.UNKNOWN, read("{\"parameterType\":null}"));
    }

    @Test
    public void parameterTypeIsWrittenInUpperCaseTest() {
        MiIoDeviceAction deviceAction = new MiIoDeviceAction();
        deviceAction.setparameterType(CommandParameterType.ONOFFNUMBER);
        assertEquals("ONOFFNUMBER", GSON.toJsonTree(deviceAction).getAsJsonObject().get("parameterType").getAsString());
    }
}
