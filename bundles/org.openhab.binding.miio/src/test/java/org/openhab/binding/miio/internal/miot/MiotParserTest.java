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
package org.openhab.binding.miio.internal.miot;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.miio.internal.basic.CommandParameterType;
import org.openhab.binding.miio.internal.basic.MiIoBasicChannel;
import org.openhab.binding.miio.internal.basic.MiIoBasicDevice;
import org.openhab.binding.miio.internal.basic.MiIoDeviceAction;
import org.openhab.binding.miio.internal.basic.MiIoDeviceActionCondition;
import org.openhab.binding.miio.internal.basic.OptionsValueListDTO;
import org.openhab.binding.miio.internal.basic.StateDescriptionDTO;

import com.google.gson.JsonParser;

/**
 * Test case for {@link MiotParser}
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class MiotParserTest {

    private static final String SPEC = """
            {
              "type": "urn:miot-spec-v2:device:vacuum:0000A006:test-model:1",
              "description": "Robot Cleaner",
              "services": [
                {
                  "iid": 2,
                  "type": "urn:miot-spec-v2:service:vacuum:00007810:test-model:1",
                  "description": "Robot Cleaner",
                  "properties": [
                    { "iid": 1, "type": "urn:miot-spec-v2:property:fault:00000009:test-model:1",
                      "description": "Device Fault", "format": "uint32", "access": ["read", "notify"],
                      "value-range": [0, 4294967295, 1] },
                    { "iid": 2, "type": "urn:miot-spec-v2:property:target-temperature:00000021:test-model:1",
                      "description": "Target Temperature", "format": "float", "access": ["read", "write"],
                      "unit": "celsius", "value-range": [16.5, 30, 0.5] }
                  ]
                },
                {
                  "iid": 8,
                  "type": "urn:miot-spec-v2:service:brush-cleaner:00007815:test-model:1",
                  "description": "Brush Cleaner",
                  "properties": [
                    { "iid": 1, "type": "urn:miot-spec-v2:property:brush-life-level:00000079:test-model:1",
                      "description": "Brush Life Level", "format": "uint8", "access": ["read", "notify"],
                      "unit": "percentage", "value-range": [0, 100, 1] }
                  ],
                  "actions": [
                    { "iid": 1, "type": "urn:miot-spec-v2:action:reset-brush-life:00002830:test-model:1",
                      "description": "Reset Brush Life", "in": [], "out": [] }
                  ]
                },
                {
                  "iid": 9,
                  "type": "urn:miot-spec-v2:service:brush-cleaner:00007815:test-model:1",
                  "description": "Brush Cleaner",
                  "properties": [
                    { "iid": 1, "type": "urn:miot-spec-v2:property:brush-life-level:00000079:test-model:1",
                      "description": "Brush Life Level", "format": "uint8", "access": ["read", "notify"],
                      "unit": "percentage", "value-range": [0, 100, 1] }
                  ],
                  "actions": [
                    { "iid": 1, "type": "urn:miot-spec-v2:action:reset-brush-life:00002830:test-model:1",
                      "description": "Reset Brush Life", "in": [], "out": [] }
                  ]
                },
                {
                  "iid": 10,
                  "type": "urn:miot-spec-v2:service:brush-cleaner:00007815:test-model:1",
                  "description": "Brush Cleaner",
                  "properties": [
                    { "iid": 1, "type": "urn:miot-spec-v2:property:brush-life-level:00000079:test-model:1",
                      "description": "Brush Life Level", "format": "uint8", "access": ["read", "notify"],
                      "unit": "percentage", "value-range": [0, 100, 1] }
                  ],
                  "actions": [
                    { "iid": 1, "type": "urn:miot-spec-v2:action:reset-brush-life1:00002830:test-model:1",
                      "description": "Reset Brush Life", "in": [], "out": [] }
                  ]
                }
              ]
            }
            """;

    private static final String INPUT_SPEC = """
            {
              "type": "urn:miot-spec-v2:device:vacuum:0000A006:test-model:1",
              "description": "Robot Cleaner",
              "services": [
                {
                  "iid": 11,
                  "type": "urn:miot-spec-v2:service:custom:00007801:test-model:1",
                  "description": "Custom",
                  "properties": [
                    { "iid": 1, "type": "urn:miot-spec-v2:property:mute:00000001:test-model:1",
                      "description": "Mute", "format": "bool", "access": ["read", "write"] },
                    { "iid": 2, "type": "urn:miot-spec-v2:property:volume:00000002:test-model:1",
                      "description": "Volume", "format": "uint8", "access": ["read", "write"],
                      "unit": "percentage", "value-range": [0, 100, 1] },
                    { "iid": 3, "type": "urn:miot-spec-v2:property:language:00000003:test-model:1",
                      "description": "Language", "format": "string", "access": ["read", "write"] },
                    { "iid": 4, "type": "urn:miot-spec-v2:property:mode:00000004:test-model:1",
                      "description": "Mode", "format": "uint8", "access": ["read", "write"],
                      "value-list": [ { "value": 0, "description": "Auto" }, { "value": 1, "description": "Quiet" } ] }
                  ],
                  "actions": [
                    { "iid": 1, "type": "urn:miot-spec-v2:action:set-mute:00002801:test-model:1",
                      "description": "Set Mute", "in": [1], "out": [] },
                    { "iid": 2, "type": "urn:miot-spec-v2:action:set-volume:00002802:test-model:1",
                      "description": "Set Volume", "in": [2], "out": [] },
                    { "iid": 3, "type": "urn:miot-spec-v2:action:set-language:00002803:test-model:1",
                      "description": "Set Language", "in": [3], "out": [] },
                    { "iid": 4, "type": "urn:miot-spec-v2:action:set-mode:00002804:test-model:1",
                      "description": "Set Mode", "in": [4], "out": [] },
                    { "iid": 5, "type": "urn:miot-spec-v2:action:set-both:00002805:test-model:1",
                      "description": "Set Both", "in": [1, 2], "out": [] },
                    { "iid": 6, "type": "urn:miot-spec-v2:action:start-all:00002806:test-model:1",
                      "description": "Start All", "in": [], "out": [] },
                    { "iid": 7, "type": "urn:miot-spec-v2:action:set-missing:00002807:test-model:1",
                      "description": "Set Missing", "in": [9], "out": [] }
                  ]
                }
              ]
            }
            """;

    private static final String DOUBLE_SPACE_SPEC = """
            {
              "type": "urn:miot-spec-v2:device:vacuum:0000A006:test-model:1",
              "description": "Robot Cleaner",
              "services": [
                {
                  "iid": 2,
                  "type": "urn:miot-spec-v2:service:vacuum:00007810:test-model:1",
                  "description": "Robot Cleaner",
                  "properties": [
                    { "iid": 1, "type": "urn:miot-spec-v2:property:status:00000007:test-model:1",
                      "description": "Status", "format": "uint8", "access": ["read", "notify"],
                      "value-list": [ { "value": 10, "description": "In The Dry" },
                                      { "value": 11, "description": "In The  Dust" } ] }
                  ]
                }
              ]
            }
            """;

    private MiIoBasicChannel parseChannel(String channelId) throws MiotParseException {
        MiotParser parser = new MiotParser("test.vacuum.model");
        List<MiIoBasicChannel> channels = parser.getDevice(JsonParser.parseString(SPEC)).getDevice().getChannels();
        return channels.stream().filter(c -> channelId.equals(c.getChannel())).findFirst().orElseThrow();
    }

    @Test
    public void valueRangeBeyondIntegerIsPreserved() throws MiotParseException {
        StateDescriptionDTO stateDescription = parseChannel("fault").getStateDescription();

        assertNotNull(stateDescription);
        assertEquals(new BigDecimal("4294967295"), stateDescription.getMaximum());
    }

    @Test
    public void decimalValueRangeIsPreserved() throws MiotParseException {
        StateDescriptionDTO stateDescription = parseChannel("target_temperature").getStateDescription();

        assertNotNull(stateDescription);
        assertEquals(new BigDecimal("16.5"), stateDescription.getMinimum());
        assertEquals(new BigDecimal("0.5"), stateDescription.getStep());
        assertEquals("%.1f %unit%", stateDescription.getPattern());
    }

    @Test
    public void actionsOfServicesWithSameTypeHaveUniqueValues() throws MiotParseException {
        MiIoBasicChannel actions = parseChannel("actions");
        StateDescriptionDTO stateDescription = actions.getStateDescription();

        assertNotNull(stateDescription);
        List<@Nullable String> values = stateDescription.getOptions().stream().map(OptionsValueListDTO::getValue)
                .toList();
        assertEquals(List.of("brush-cleaner-reset-brush-life", "brush-cleaner-reset-brush-life1",
                "brush-cleaner-reset-brush-life11"), values);

        List<String> matchValues = actions.getActions().stream().map(MiIoDeviceAction::getCondition)
                .map(Objects::requireNonNull).map((MiIoDeviceActionCondition c) -> c.getParameters().getAsJsonArray()
                        .get(0).getAsJsonObject().get("matchValue").getAsString())
                .toList();
        assertEquals(values, matchValues);
        assertEquals(List.of(8, 9, 10), actions.getActions().stream().map(MiIoDeviceAction::getSiid).toList());
    }

    private List<MiIoBasicChannel> parseInputSpec() throws MiotParseException {
        return new MiotParser("test.vacuum.model").getDevice(JsonParser.parseString(INPUT_SPEC)).getDevice()
                .getChannels();
    }

    private MiIoBasicChannel inputChannel(String channelId) throws MiotParseException {
        return parseInputSpec().stream().filter(c -> channelId.equals(c.getChannel())).findFirst().orElseThrow();
    }

    private void assertInputAction(MiIoBasicChannel channel, int aiid, int piid, CommandParameterType parameterType) {
        assertEquals(1, channel.getActions().size());
        MiIoDeviceAction action = channel.getActions().get(0);
        assertEquals("action", action.getCommand());
        assertEquals(11, action.getSiid());
        assertEquals(aiid, action.getAiid());
        assertEquals(parameterType, action.getparameterType());
        assertEquals(JsonParser.parseString("[{\"piid\": " + piid + ", \"value\": \"$value$\"}]"),
                action.getParameters());
        assertNull(action.getCondition());
        assertEquals("", channel.getProperty());
    }

    @Test
    public void actionWithBooleanInputGetsSwitchChannel() throws MiotParseException {
        MiIoBasicChannel channel = inputChannel("custom_set_mute");

        assertEquals("Switch", channel.getType());
        assertEquals("Custom - Set Mute", channel.getFriendlyName());
        assertInputAction(channel, 1, 1, CommandParameterType.ONOFFBOOL);
    }

    @Test
    public void actionWithNumberInputGetsNumberChannelWithRange() throws MiotParseException {
        MiIoBasicChannel channel = inputChannel("custom_set_volume");

        assertTrue(channel.getType().startsWith("Number"));
        StateDescriptionDTO stateDescription = channel.getStateDescription();
        assertNotNull(stateDescription);
        assertEquals(new BigDecimal("100"), stateDescription.getMaximum());
        assertInputAction(channel, 2, 2, CommandParameterType.NUMBER);
    }

    @Test
    public void actionWithStringInputGetsStringChannel() throws MiotParseException {
        MiIoBasicChannel channel = inputChannel("custom_set_language");

        assertEquals("String", channel.getType());
        assertInputAction(channel, 3, 3, CommandParameterType.STRING);
    }

    @Test
    public void actionWithValueListInputKeepsOptions() throws MiotParseException {
        MiIoBasicChannel channel = inputChannel("custom_set_mode");

        StateDescriptionDTO stateDescription = channel.getStateDescription();
        assertNotNull(stateDescription);
        assertEquals(List.of("0", "1"), Objects.requireNonNull(stateDescription.getOptions()).stream()
                .map(OptionsValueListDTO::getValue).toList());
        assertInputAction(channel, 4, 4, CommandParameterType.NUMBER);
    }

    @Test
    public void actionsWithoutSingleInputStayOnActionsChannel() throws MiotParseException {
        List<MiIoBasicChannel> channels = parseInputSpec();
        MiIoBasicChannel actions = channels.stream().filter(c -> "actions".equals(c.getChannel())).findFirst()
                .orElseThrow();

        assertEquals(List.of(5, 6, 7), actions.getActions().stream().map(MiIoDeviceAction::getAiid).toList());
        assertEquals(List.of(CommandParameterType.UNKNOWN, CommandParameterType.EMPTY, CommandParameterType.UNKNOWN),
                actions.getActions().stream().map(MiIoDeviceAction::getparameterType).toList());
        assertEquals(
                List.of("actions", "mute", "volume", "language", "mode", "custom_set_mute", "custom_set_volume",
                        "custom_set_language", "custom_set_mode"),
                channels.stream().map(MiIoBasicChannel::getChannel).toList());
    }

    @Test
    public void toJsonKeepsDoubleSpacesInTexts() throws MiotParseException {
        MiIoBasicDevice device = new MiotParser("test.vacuum.model")
                .getDevice(JsonParser.parseString(DOUBLE_SPACE_SPEC));
        String json = MiotParser.toJson(device);

        assertTrue(json.contains("\"In The  Dust\""), "double space in a text must be preserved");
        assertFalse(json.contains("In The" + "\t" + "Dust"), "no tab may replace a double space in a text");
        assertTrue(json.contains("\r\n\t\t\"channels\""), "indentation is converted to tabs with CRLF line endings");
        assertFalse(json.contains("\n  "), "no space indentation is left");
    }
}
