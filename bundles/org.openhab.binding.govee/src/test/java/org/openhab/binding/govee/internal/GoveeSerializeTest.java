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
package org.openhab.binding.govee.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.govee.internal.model.Color;
import org.openhab.binding.govee.internal.model.ColorData;
import org.openhab.binding.govee.internal.model.EmptyValueQueryStatusData;
import org.openhab.binding.govee.internal.model.GenericGoveeMsg;
import org.openhab.binding.govee.internal.model.GenericGoveeRequest;
import org.openhab.binding.govee.internal.model.ValueIntData;

import com.google.gson.Gson;

/**
 * @author Stefan Höhn - Initial contribution
 */
@NonNullByDefault
public class GoveeSerializeTest {

    private static final Gson GSON = new Gson();
    private final String lightOffJsonString = "{\"msg\":{\"cmd\":\"turn\",\"data\":{\"value\":0}}}";
    private final String lightOnJsonString = "{\"msg\":{\"cmd\":\"brightness\",\"data\":{\"value\":100}}}";
    private final String lightColorJsonString = "{\"msg\":{\"cmd\":\"colorwc\",\"data\":{\"color\":{\"r\":0,\"g\":1,\"b\":2},\"colorTemInKelvin\":3}}}";
    private final String lightBrightnessJsonString = "{\"msg\":{\"cmd\":\"brightness\",\"data\":{\"value\":99}}}";
    private final String lightQueryJsonString = "{\"msg\":{\"cmd\":\"devStatus\",\"data\":{}}}";

    @Test
    public void testSerializeMessage() {
        GenericGoveeRequest lightOff = new GenericGoveeRequest(new GenericGoveeMsg("turn", new ValueIntData(0)));
        assertEquals(lightOffJsonString, GSON.toJson(lightOff));
        GenericGoveeRequest lightOn = new GenericGoveeRequest(new GenericGoveeMsg("brightness", new ValueIntData(100)));
        assertEquals(lightOnJsonString, GSON.toJson(lightOn));
        GenericGoveeRequest lightColor = new GenericGoveeRequest(
                new GenericGoveeMsg("colorwc", new ColorData(new Color(0, 1, 2), 3)));
        assertEquals(lightColorJsonString, GSON.toJson(lightColor));
        GenericGoveeRequest lightBrightness = new GenericGoveeRequest(
                new GenericGoveeMsg("brightness", new ValueIntData(99)));
        assertEquals(lightBrightnessJsonString, GSON.toJson(lightBrightness));
        GenericGoveeRequest lightQuery = new GenericGoveeRequest(
                new GenericGoveeMsg("devStatus", new EmptyValueQueryStatusData()));
        assertEquals(lightQueryJsonString, GSON.toJson(lightQuery));
    }
}
