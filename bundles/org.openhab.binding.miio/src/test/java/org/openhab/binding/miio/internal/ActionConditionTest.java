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
package org.openhab.binding.miio.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.miio.internal.basic.ActionConditions;
import org.openhab.binding.miio.internal.basic.MiIoDeviceActionCondition;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * Test case for {@link ActionConditions}
 *
 * @author Marcel Verpaalen - Initial contribution
 *
 */
@NonNullByDefault
public class ActionConditionTest {

    @Test
    public void assertBrightnessExisting() {
        MiIoDeviceActionCondition condition = new MiIoDeviceActionCondition();
        condition.setName("BrightnessExisting");
        Map<String, Object> deviceVariables = Collections.emptyMap();
        JsonElement value = new JsonPrimitive(1);
        JsonElement resp = ActionConditions.executeAction(condition, deviceVariables, value, null);
        // dimmed to 1
        assertNotNull(resp);
        assertEquals(new JsonPrimitive(1), resp);

        // fully on
        value = new JsonPrimitive(100);
        resp = ActionConditions.executeAction(condition, deviceVariables, value, null);
        assertNotNull(resp);
        assertEquals(new JsonPrimitive(100), resp);

        // >100
        value = new JsonPrimitive(200);
        resp = ActionConditions.executeAction(condition, deviceVariables, value, null);
        assertNotNull(resp);
        assertEquals(new JsonPrimitive(100), resp);

        // switched off = invalid brightness
        value = new JsonPrimitive(0);
        resp = ActionConditions.executeAction(condition, deviceVariables, value, null);
        assertNull(resp);
        assertNotEquals(new JsonPrimitive(0), resp);

        value = new JsonPrimitive(-1);
        resp = ActionConditions.executeAction(condition, deviceVariables, value, null);
        assertNull(resp);
        assertNotEquals(new JsonPrimitive(-1), resp);
    }
}
