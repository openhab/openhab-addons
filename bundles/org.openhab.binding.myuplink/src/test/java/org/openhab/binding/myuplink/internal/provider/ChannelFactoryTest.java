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
package org.openhab.binding.myuplink.internal.provider;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.openhab.binding.myuplink.internal.MyUplinkBindingConstants.*;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.myuplink.internal.MyUplinkBindingConstants;
import org.openhab.binding.myuplink.internal.Utils;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.test.storage.VolatileStorageService;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Unit Tests to verify behaviour of ChannelFactory implementation.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class ChannelFactoryTest {

    private final MyUplinkChannelTypeProvider channelTypeProvider = new MyUplinkChannelTypeProvider(
            new VolatileStorageService());
    private final ChannelFactory channelFactory = new ChannelFactory(channelTypeProvider, new ChannelTypeRegistry());

    private static final ThingUID TEST_THING_UID = new ThingUID(MyUplinkBindingConstants.BINDING_ID, "genericThing",
            "myUnit");

    private final String testChannelDataTemperature = """
            {"category":"NIBEF VVM 320 E","parameterId":"40121","parameterName":"Add. heat (BT63)","parameterUnit":"°C","writable":false,"timestamp":"2024-05-10T05:35:50+00:00","value":39.0,"strVal":"39Â°C","smartHomeCategories":[],"minValue":null,"maxValue":null,"stepValue":1.0,"enumValues":[],"scaleValue":"0.1","zoneId":null}
            """;

    private final String testChannelEnumWritableSwitch = """
            {"category":"NIBEF VVM 320 E","parameterId":"50004","parameterName":"Temporary lux","parameterUnit":"","writable":true,"timestamp":"2024-05-05T13:41:09+00:00","value":0.0,"strVal":"off","smartHomeCategories":[],"minValue":null,"maxValue":null,"stepValue":1.0,"enumValues":[{"value":"0","text":"off","icon":""},{"value":"1","text":"on","icon":""}],"scaleValue":"1","zoneId":null}
            """;

    private final String testChannelEnumSwitch = """
            {"category":"NIBEF VVM 320 E","parameterId":"49992","parameterName":"Pump: Heating medium (GP6)","parameterUnit":"","writable":false,"timestamp":"2024-05-05T13:41:09+00:00","value":0.0,"strVal":"Off","smartHomeCategories":[],"minValue":null,"maxValue":null,"stepValue":1.0,"enumValues":[{"value":"0","text":"Off","icon":""},{"value":"1","text":"On","icon":""}],"scaleValue":"1","zoneId":null}
            """;

    private final String testChannelEnumPriority = """
            {"category":"NIBEF VVM 320 E","parameterId":"49994","parameterName":"Priority","parameterUnit":"","writable":false,"timestamp":"2024-05-10T03:31:23+00:00","value":10.0,"strVal":"Off","smartHomeCategories":[],"minValue":null,"maxValue":null,"stepValue":1.0,"enumValues":[{"value":"10","text":"Off","icon":""},{"value":"20","text":"Hot water","icon":""},{"value":"30","text":"Heating","icon":""},{"value":"40","text":"Pool","icon":""},{"value":"41","text":"Pool 2","icon":""},{"value":"50","text":"TransÂ­fer","icon":""},{"value":"60","text":"Cooling","icon":""}],"scaleValue":"1","zoneId":null}
            """;

    private final String testChannelEnumCompressorStatus = """
            {"category":"Slave 1 (EB101)","parameterId":"44064","parameterName":"Status compressor (EB101)","parameterUnit":"","writable":false,"timestamp":"2024-05-10T03:31:28+00:00","value":20.0,"strVal":"off","smartHomeCategories":[],"minValue":null,"maxValue":null,"stepValue":1.0,"enumValues":[{"value":"20","text":"off","icon":""},{"value":"40","text":"starts","icon":""},{"value":"60","text":"runs","icon":""},{"value":"100","text":"stops","icon":""}],"scaleValue":"1","zoneId":null}
            """;

    private final String testChannelEnumAddHeatStatus = """
            {"category":"NIBEF VVM 320 E","parameterId":"49993","parameterName":"Int elec add heat","parameterUnit":"","writable":false,"timestamp":"2024-05-05T13:41:27+00:00","value":4.0,"strVal":"Blocked","smartHomeCategories":[],"minValue":null,"maxValue":null,"stepValue":1.0,"enumValues":[{"value":"0","text":"Alarm","icon":""},{"value":"1","text":"Alarm","icon":""},{"value":"2","text":"Active","icon":""},{"value":"3","text":"Off","icon":""},{"value":"4","text":"Blocked","icon":""},{"value":"5","text":"Off","icon":""},{"value":"6","text":"Active","icon":""}],"scaleValue":"1","zoneId":null}
            """;

    private final String testChannelEnumHeatPumpStatusWithLowerCaseTexts = """
            { "category": "Heat pump 1", "parameterId": "62017", "parameterName": "Status", "parameterUnit": "", "writable": false, "timestamp": "2024-05-21T16:22:21+00:00", "value": 1, "strVal": "Off, ready to start", "smartHomeCategories": [], "minValue": null, "maxValue": null, "stepValue": 1, "enumValues": [ { "value": "0", "text": "Off, start delay", "icon": "" }, { "value": "1", "text": "OFF, ready to start", "icon": "" }, { "value": "2", "text": "Wait until flow", "icon": "" }, { "value": "3", "text": "On", "icon": "" }, { "value": "4", "text": "Defrost", "icon": "" }, { "value": "5", "text": "Cooling", "icon": "" }, { "value": "6", "text": "Blocked", "icon": "" }, { "value": "7", "text": "Off, alarm", "icon": "" }, { "value": "8", "text": "Function test", "icon": "" }, { "value": "30", "text": "not defined", "icon": "" }, { "value": "31", "text": "Comp. disabled", "icon": "" }, { "value": "32", "text": "Comm. error", "icon": "" }, { "value": "33", "text": "Hot Water", "icon": "" } ], "scaleValue": "1", "zoneId": null }
            """;

    @Test
    public void testFromJsonDataTemperature() {
        var gson = new Gson();
        var json = gson.fromJson(testChannelDataTemperature, JsonObject.class);
        json = json == null ? new JsonObject() : json;

        var result = channelFactory.createChannel(TEST_THING_UID, json);
        assertThat(result.getAcceptedItemType(), is("Number:Temperature"));
        assertThat(Objects.requireNonNull(result.getChannelTypeUID()).getId(), is("type-temperature"));
        assertThat(result.getUID().getThingUID(), is(TEST_THING_UID));
        assertThat(result.getUID().getId(), is("40121"));
        assertThat(result.getDescription(), is("Add. heat (BT63)"));
        assertThat(result.getLabel(), is("Add. heat (BT63)"));
    }

    @Test
    public void testFromJsonDataEnumWritableSwitch() {
        var gson = new Gson();
        var json = gson.fromJson(testChannelEnumWritableSwitch, JsonObject.class);
        json = json == null ? new JsonObject() : json;

        var result = channelFactory.createChannel(TEST_THING_UID, json);
        assertThat(result.getAcceptedItemType(), is(CoreItemFactory.SWITCH));
        assertThat(Objects.requireNonNull(result.getChannelTypeUID()).getId(), is("rwtype-switch"));
        assertThat(result.getUID().getThingUID(), is(TEST_THING_UID));
        assertThat(result.getUID().getId(), is("50004"));
        assertThat(result.getDescription(), is("Temporary lux"));
        assertThat(result.getLabel(), is("Temporary lux"));
    }

    @Test
    public void testFromJsonDataEnumSwitch() {
        var gson = new Gson();
        var json = gson.fromJson(testChannelEnumSwitch, JsonObject.class);
        json = json == null ? new JsonObject() : json;

        var result = channelFactory.createChannel(TEST_THING_UID, json);
        assertThat(result.getAcceptedItemType(), is(CoreItemFactory.NUMBER));
        assertThat(Objects.requireNonNull(result.getChannelTypeUID()).getId(), is("type-on-off"));
        assertThat(result.getUID().getThingUID(), is(TEST_THING_UID));
        assertThat(result.getUID().getId(), is("49992"));
        assertThat(result.getDescription(), is("Pump: Heating medium (GP6)"));
        assertThat(result.getLabel(), is("Pump: Heating medium (GP6)"));
    }

    @Test
    public void testFromJsonDataEnumPriority() {
        var gson = new Gson();
        var json = gson.fromJson(testChannelEnumPriority, JsonObject.class);
        json = json == null ? new JsonObject() : json;

        var result = channelFactory.createChannel(TEST_THING_UID, json);
        assertThat(result.getAcceptedItemType(), is(CoreItemFactory.NUMBER));
        assertThat(Objects.requireNonNull(result.getChannelTypeUID()).getId(), is("type-enum-49994"));
    }

    @Test
    public void testFromJsonDataEnumCompressorStatus() {
        var gson = new Gson();
        var json = gson.fromJson(testChannelEnumCompressorStatus, JsonObject.class);
        json = json == null ? new JsonObject() : json;

        var result = channelFactory.createChannel(TEST_THING_UID, json);
        assertThat(result.getAcceptedItemType(), is(CoreItemFactory.NUMBER));
        assertThat(Objects.requireNonNull(result.getChannelTypeUID()).getId(), is("type-enum-44064"));
    }

    @Test
    public void testFromJsonDataEnumAddHeatStatus() {
        var gson = new Gson();
        var json = gson.fromJson(testChannelEnumAddHeatStatus, JsonObject.class);
        json = json == null ? new JsonObject() : json;

        var result = channelFactory.createChannel(TEST_THING_UID, json);
        assertThat(result.getAcceptedItemType(), is(CoreItemFactory.NUMBER));
        assertThat(Objects.requireNonNull(result.getChannelTypeUID()).getId(), is("type-enum-49993"));

        var type = channelTypeProvider.getChannelType(new ChannelTypeUID(BINDING_ID, "type-enum-49993"), null);
        assertNotNull(type);
        assertThat(Objects.requireNonNull(type.getState()).getOptions().size(), is(7));
    }

    @Test
    public void testFromJsonDataEnumHeatPumpStatus() {
        var gson = new Gson();
        var json = gson.fromJson(testChannelEnumHeatPumpStatusWithLowerCaseTexts, JsonObject.class);
        json = json == null ? new JsonObject() : json;

        var result = channelFactory.createChannel(TEST_THING_UID, json);
        assertThat(result.getAcceptedItemType(), is(CoreItemFactory.NUMBER));
        assertThat(Objects.requireNonNull(result.getChannelTypeUID()).getId(), is("type-enum-62017"));

        var type = channelTypeProvider.getChannelType(new ChannelTypeUID(BINDING_ID, "type-enum-62017"), null);
        assertNotNull(type);
        assertThat(Objects.requireNonNull(type.getState()).getOptions().size(), is(13));
    }

    @Test
    public void testHeatPumpStatusEnumValues() {
        var gson = new Gson();
        var json = gson.fromJson(testChannelEnumHeatPumpStatusWithLowerCaseTexts, JsonObject.class);
        json = json == null ? new JsonObject() : json;

        var enumValues = Utils.getAsJsonArray(json, JSON_KEY_CHANNEL_ENUM_VALUES);
        var list = channelFactory.extractEnumValues(enumValues);

        assertThat(list.size(), is(13));

        list.forEach(enumMapping -> {
            switch (enumMapping.getValue()) {
                case "0" -> assertThat(enumMapping.getLabel(), is("Off, Start Delay"));
                case "1" -> assertThat(enumMapping.getLabel(), is("Off, Ready To Start"));
                case "2" -> assertThat(enumMapping.getLabel(), is("Wait Until Flow"));
                case "3" -> assertThat(enumMapping.getLabel(), is("On"));
                case "4" -> assertThat(enumMapping.getLabel(), is("Defrost"));
                case "5" -> assertThat(enumMapping.getLabel(), is("Cooling"));
                case "6" -> assertThat(enumMapping.getLabel(), is("Blocked"));
                case "7" -> assertThat(enumMapping.getLabel(), is("Off, Alarm"));
                case "8" -> assertThat(enumMapping.getLabel(), is("Function Test"));
                case "30" -> assertThat(enumMapping.getLabel(), is("Not Defined"));
                case "31" -> assertThat(enumMapping.getLabel(), is("Comp. Disabled"));
                case "32" -> assertThat(enumMapping.getLabel(), is("Comm. Error"));
                case "33" -> assertThat(enumMapping.getLabel(), is("Hot Water"));
                default -> assertNotNull(null, "unknown enum value");
            }
        });
    }
}
