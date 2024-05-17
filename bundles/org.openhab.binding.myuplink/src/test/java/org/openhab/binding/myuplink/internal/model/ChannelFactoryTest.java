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

    private final String testChannelEnumWritableSwitch = """
            {"category":"NIBEF VVM 320 E","parameterId":"50004","parameterName":"Temporary lux","parameterUnit":"","writable":true,"timestamp":"2024-05-05T13:41:09+00:00","value":0.0,"strVal":"off","smartHomeCategories":[],"minValue":null,"maxValue":null,"stepValue":1.0,"enumValues":[{"value":"0","text":"off","icon":""},{"value":"1","text":"on","icon":""}],"scaleValue":"1","zoneId":null}
            """;

    private final String testChannelEnumSwitch = """
            {"category":"NIBEF VVM 320 E","parameterId":"49992","parameterName":"Pump: Heating medium (GP6)","parameterUnit":"","writable":false,"timestamp":"2024-05-05T13:41:09+00:00","value":0.0,"strVal":"Off","smartHomeCategories":[],"minValue":null,"maxValue":null,"stepValue":1.0,"enumValues":[{"value":"0","text":"Off","icon":""},{"value":"1","text":"On","icon":""}],"scaleValue":"1","zoneId":null}
            """;

    private final String testChannelEnumUnknown = """
            {"category":"NIBEF VVM 320 E","parameterId":"99999","parameterName":"Unknown","parameterUnit":"","writable":false,"timestamp":"2024-05-05T13:41:09+00:00","value":0.0,"strVal":"wtf","smartHomeCategories":[],"minValue":null,"maxValue":null,"stepValue":1.0,"enumValues":[{"value":"0","text":"wtf","icon":""},{"value":"1","text":"On","icon":""}],"scaleValue":"1","zoneId":null}
            """;

    private final String testChannelEnumPriority = """
            {"category":"NIBEF VVM 320 E","parameterId":"49994","parameterName":"Priority","parameterUnit":"","writable":false,"timestamp":"2024-05-10T03:31:23+00:00","value":10.0,"strVal":"Off","smartHomeCategories":[],"minValue":null,"maxValue":null,"stepValue":1.0,"enumValues":[{"value":"10","text":"Off","icon":""},{"value":"20","text":"Hot water","icon":""},{"value":"30","text":"Heating","icon":""},{"value":"40","text":"Pool","icon":""},{"value":"41","text":"Pool 2","icon":""},{"value":"50","text":"TransÂ­fer","icon":""},{"value":"60","text":"Cooling","icon":""}],"scaleValue":"1","zoneId":null}
            """;

    private final String testChannelEnumCompressorStatus = """
            {"category":"Slave 1 (EB101)","parameterId":"44064","parameterName":"Status compressor (EB101)","parameterUnit":"","writable":false,"timestamp":"2024-05-10T03:31:28+00:00","value":20.0,"strVal":"off","smartHomeCategories":[],"minValue":null,"maxValue":null,"stepValue":1.0,"enumValues":[{"value":"20","text":"off","icon":""},{"value":"40","text":"starts","icon":""},{"value":"60","text":"runs","icon":""},{"value":"100","text":"stops","icon":""}],"scaleValue":"1","zoneId":null}
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

    @Test
    public void testFromJsonDataEnumWritableSwitch() {
        Gson gson = new Gson();
        JsonObject json = gson.fromJson(testChannelEnumWritableSwitch, JsonObject.class);
        json = json == null ? new JsonObject() : json;

        Channel result = ChannelFactory.createChannel(TEST_THING_UID, TEST_CHANNEL_ID, json);
        assertThat(result.getAcceptedItemType(), is(ChannelType.RW_SWITCH.getAcceptedType()));
        assertThat(result.getUID().getThingUID(), is(TEST_THING_UID));
        assertThat(result.getUID().getId(), is(TEST_CHANNEL_ID));
        assertThat(result.getDescription(), is("Temporary lux"));
        assertThat(result.getLabel(), is("Temporary lux"));
    }

    @Test
    public void testFromJsonDataEnumSwitch() {
        Gson gson = new Gson();
        JsonObject json = gson.fromJson(testChannelEnumSwitch, JsonObject.class);
        json = json == null ? new JsonObject() : json;

        Channel result = ChannelFactory.createChannel(TEST_THING_UID, TEST_CHANNEL_ID, json);
        assertThat(result.getAcceptedItemType(), is(ChannelType.SWITCH.getAcceptedType()));
        assertThat(result.getUID().getThingUID(), is(TEST_THING_UID));
        assertThat(result.getUID().getId(), is(TEST_CHANNEL_ID));
        assertThat(result.getDescription(), is("Pump: Heating medium (GP6)"));
        assertThat(result.getLabel(), is("Pump: Heating medium (GP6)"));
    }

    @Test
    public void testFromJsonDataEnumUnknown() {
        Gson gson = new Gson();
        JsonObject json = gson.fromJson(testChannelEnumUnknown, JsonObject.class);
        json = json == null ? new JsonObject() : json;

        Channel result = ChannelFactory.createChannel(TEST_THING_UID, TEST_CHANNEL_ID, json);
        assertThat(result.getAcceptedItemType(), is(ChannelType.DOUBLE.getAcceptedType()));
    }

    @Test
    public void testFromJsonDataEnumPriority() {
        Gson gson = new Gson();
        JsonObject json = gson.fromJson(testChannelEnumPriority, JsonObject.class);
        json = json == null ? new JsonObject() : json;

        Channel result = ChannelFactory.createChannel(TEST_THING_UID, TEST_CHANNEL_ID, json);
        assertThat(result.getAcceptedItemType(), is(ChannelType.PRIORITY.getAcceptedType()));
    }

    @Test
    public void testFromJsonDataEnumCompressorStatus() {
        Gson gson = new Gson();
        JsonObject json = gson.fromJson(testChannelEnumCompressorStatus, JsonObject.class);
        json = json == null ? new JsonObject() : json;

        Channel result = ChannelFactory.createChannel(TEST_THING_UID, TEST_CHANNEL_ID, json);
        assertThat(result.getAcceptedItemType(), is(ChannelType.COMPRESSOR_STATUS.getAcceptedType()));
    }
}
