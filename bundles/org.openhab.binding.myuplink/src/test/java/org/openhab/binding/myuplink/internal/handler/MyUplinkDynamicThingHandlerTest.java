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
package org.openhab.binding.myuplink.internal.handler;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.myuplink.internal.MyUplinkBindingConstants;
import org.openhab.binding.myuplink.internal.model.ChannelType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Unit Tests to verify behaviour of MyUplinkDynamicThingHandler implementation.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
public class MyUplinkDynamicThingHandlerTest {

    private Thing thing = mock(Thing.class);
    private MyUplinkDynamicThingHandler handler = new MyUplinkDynamicThingHandlerImpl(thing);

    private final String testChannelDataTemperature = """
            {"category":"NIBEF VVM 320 E","parameterId":"40121","parameterName":"Add. heat (BT63)","parameterUnit":"°C","writable":false,"timestamp":"2024-05-10T05:35:50+00:00","value":39.0,"strVal":"39Â°C","smartHomeCategories":[],"minValue":null,"maxValue":null,"stepValue":1.0,"enumValues":[],"scaleValue":"0.1","zoneId":null}
            """;

    @BeforeEach
    public void initMock() {
        when(thing.getUID()).thenReturn(new ThingUID(MyUplinkBindingConstants.BINDING_ID, "test"));
    }

    @Test
    public void testFromJsonDataTemperature() {
        Gson gson = new Gson();
        JsonObject json = gson.fromJson(testChannelDataTemperature, JsonObject.class);
        json = json == null ? new JsonObject() : json;

        Channel result = handler.getOrCreateChannel("4711", json);
        assertThat(result.getAcceptedItemType(), is(ChannelType.TEMPERATURE.getAcceptedType()));
    }
}
