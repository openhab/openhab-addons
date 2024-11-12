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
package org.openhab.binding.dirigera.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dirigera.internal.handler.light.LightCommand;
import org.openhab.core.library.types.HSBType;

/**
 * {@link TestGeneric} some basic tests
 *
 * @author Bernd Weymann - Initial Contribution
 */
@NonNullByDefault
class TestGeneric {

    @Test
    void testStringFormatWithNull() {
        try {
            String error = String.format(
                    "{\"http-error-flag\":true,\"http-error-status\":%s,\"http-error-message\":\"%s\"}", "5", null);
            JSONObject errorJSON = new JSONObject(error);
            assertFalse(errorJSON.isNull("http-error-message"));
        } catch (Throwable t) {
            fail();
        }
    }

    @Test
    void hsbCloseTo() {
        HSBType first = new HSBType("180, 100, 100");
        HSBType second = new HSBType("177, 97, 50");
        boolean isClose = first.closeTo(second, 0.02);
        assertTrue(isClose);
    }

    @Test
    void lightCommandQueueTest() {
        ArrayList<LightCommand> lightRequestQueue = new ArrayList<>();
        JSONObject dummy1 = new JSONObject();
        dummy1.put("dunny1", false);
        LightCommand brightness1 = new LightCommand(dummy1, LightCommand.Action.BRIGHTNESS);
        lightRequestQueue.add(brightness1);
        JSONObject dummy2 = new JSONObject();
        dummy2.put("dunny2", true);
        LightCommand brightness2 = new LightCommand(dummy2, LightCommand.Action.BRIGHTNESS);
        assertTrue(lightRequestQueue.contains(brightness1));
        assertTrue(lightRequestQueue.contains(brightness2));
        assertTrue(brightness1.equals(brightness2));
        JSONObject dummy3 = null;
        assertFalse(brightness1.equals(dummy3));
        LightCommand color = new LightCommand(dummy2, LightCommand.Action.COLOR);
        assertFalse(lightRequestQueue.contains(color));
    }
}
