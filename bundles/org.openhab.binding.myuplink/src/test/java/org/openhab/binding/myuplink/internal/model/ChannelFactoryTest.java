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
package org.openhab.binding.myuplink.internal.model;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.myuplink.internal.MyUplinkBindingConstants;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ThingUID;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Unit Tests to verify behaviour of ChannelFactory implementation.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class ChannelFactoryTest {

    private final ThingUID TEST_THING_UID = new ThingUID(MyUplinkBindingConstants.BINDING_ID, "genericThing", "myUnit");
    private final String TEST_CHANNEL_ID = "4711";

    private final String testChannelDataTemperature = """
            {"category":"NIBEF VVM 320 E","parameterId":"40121","parameterName":"Add. heat (BT63)","parameterUnit":"°C","writable":false,"timestamp":"2024-05-10T05:35:50+00:00","value":39.0,"strVal":"39Â°C","smartHomeCategories":[],"minValue":null,"maxValue":null,"stepValue":1.0,"enumValues":[],"scaleValue":"0.1","zoneId":null}
            """;

    @Test
    public void testFromJsonDataTemperature() {
        Gson gson = new Gson();
        JsonObject json = gson.fromJson(testChannelDataTemperature, JsonObject.class);
        json = json == null ? new JsonObject() : json;

        Channel result = ChannelFactory.createChannel(TEST_THING_UID, TEST_CHANNEL_ID, json);
        assertThat(result.getAcceptedItemType(), is(ChannelType.TEMPERATURE.getAcceptedType()));
        assertThat(result.getUID().getThingUID(), is(TEST_THING_UID));
        assertThat(result.getUID().getId(), is(TEST_CHANNEL_ID));
        assertThat(result.getDescription(), is("Add. heat (BT63)"));
        assertThat(result.getLabel(), is("Add. heat (BT63)"));
    }
}
