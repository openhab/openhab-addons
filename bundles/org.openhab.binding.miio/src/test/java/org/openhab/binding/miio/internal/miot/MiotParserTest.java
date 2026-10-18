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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.http.HttpStatus;
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

    private static final String WHITESPACE_SPEC = """
            {
              "type": "urn:miot-spec-v2:device:vacuum:0000A006:test-model:1",
              "description": "Robot Cleaner",
              "services": [
                {
                  "iid": 2,
                  "type": "urn:miot-spec-v2:service:vacuum:00007810:test-model:1",
                  "description": " Robot\\n Cleaner ",
                  "properties": [
                    { "iid": 1, "type": "urn:miot-spec-v2:property:status:00000007:test-model:1",
                      "description": "\\tDevice\\u00a0 Status\\r\\n", "format": "uint8", "access": ["read", "notify"],
                      "value-list": [ { "value": 10, "description": " In The\\n Dry " },
                                      { "value": 11, "description": "In\\tThe  Dust\\u00a0" } ] }
                  ],
                  "actions": [
                    { "iid": 1, "type": "urn:miot-spec-v2:action:set-status:00002801:test-model:1",
                      "description": "  Set \\u3000Status ", "in": [1] }
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

    private static final String ACTION_ONLY_SPEC = """
            {
              "type": "urn:miot-spec-v2:device:vacuum:0000A006:test-model:1",
              "description": "Robot Cleaner",
              "services": [
                {
                  "iid": 3,
                  "type": "urn:miot-spec-v2:service:identify:00007801:test-model:1",
                  "description": "Identify",
                  "actions": [
                    { "iid": 1, "type": "urn:miot-spec-v2:action:identify:00002801:test-model:1",
                      "description": "Identify", "in": [], "out": [] },
                    { "iid": 2, "type": "urn:miot-spec-v2:action:reset:00002802:test-model:1",
                      "description": "Reset" }
                  ]
                }
              ]
            }
            """;

    private static final String RELEASED_INSTANCES = """
            {
              "instances": [
                { "status": "released", "model": "other.vacuum.model", "version": 9, "type": "urn:other:9", "ts": 1 },
                { "model": "test.vacuum.model", "version": 1, "type": "urn:test:1", "ts": 1 },
                { "model": "test.vacuum.model", "version": 3, "type": "urn:test:3", "ts": 1 },
                { "model": "test.vacuum.model", "version": 2, "type": "urn:test:2", "ts": 1 }
              ],
              "total": 4
            }
            """;

    private static final String ALL_INSTANCES = """
            {
              "instances": [
                { "status": "debug", "model": "test.vacuum.model", "version": 1, "type": "urn:test:1" },
                { "status": "preview", "model": "test.vacuum.model", "version": 2, "type": "urn:test:2" },
                { "status": "debug", "model": "test.vacuum.model", "version": 5, "type": "urn:test:5" },
                { "status": "debug", "model": "debug.vacuum.model", "version": 1, "type": "urn:debug:1" },
                { "status": "debug", "model": "debug.vacuum.model", "version": 4, "type": "urn:debug:4" },
                { "status": "released", "model": "mixed.vacuum.model", "version": 1, "type": "urn:mixed:1" },
                { "status": "debug", "model": "mixed.vacuum.model", "version": 2, "type": "urn:mixed:2" }
              ]
            }
            """;

    private static final String ODD_INSTANCES = """
            {
              "instances": [
                { "status": "debug", "model": "status.vacuum.model", "version": 1, "type": "urn:status:1" },
                { "status": "deprecated", "model": "status.vacuum.model", "version": 8, "type": "urn:status:8" },
                { "model": "status.vacuum.model", "version": 9, "type": "urn:status:9" },
                { "status": null, "model": "status.vacuum.model", "version": 10, "type": "urn:status:10" },
                { "status": "released", "model": "broken.vacuum.model", "version": 1, "type": "urn:broken:1" },
                { "status": "released", "model": "broken.vacuum.model", "version": null, "type": "urn:broken:null" },
                { "status": "released", "model": "broken.vacuum.model", "type": "urn:broken:none" },
                { "status": "released", "model": "broken.vacuum.model", "version": 7 }
              ]
            }
            """;

    private static final String INCOMPLETE_SPEC = """
            {
              "type": "urn:miot-spec-v2:device:vacuum:0000A006:test-model:1",
              "description": "Robot Cleaner",
              "services": [
                {
                  "iid": 11,
                  "type": "urn:miot-spec-v2:service:custom:00007801:test-model:1",
                  "properties": [
                    { "iid": 1, "type": "urn:miot-spec-v2:property:mute:00000001:test-model:1",
                      "access": ["read", "write"] }
                  ],
                  "actions": [
                    { "iid": 1, "type": "urn:miot-spec-v2:action:set-mute:00002801:test-model:1", "in": [1] }
                  ]
                }
              ]
            }
            """;

    private @Nullable String selectUrn(String instances, String model, @Nullable Integer version) throws IOException {
        ModelUrnsDTO instance = MiotParser.selectInstance(new StringReader(instances), model, version);
        return instance == null ? null : instance.getType();
    }

    @Test
    public void highestReleasedVersionIsSelected() throws IOException {
        assertEquals("urn:test:3", selectUrn(RELEASED_INSTANCES, "test.vacuum.model", null));
    }

    @Test
    public void releasedIsPreferredOverPreviewAndPreviewOverDebug() throws IOException {
        assertEquals("urn:test:2", selectUrn(ALL_INSTANCES, "test.vacuum.model", null));
        assertEquals("urn:debug:4", selectUrn(ALL_INSTANCES, "debug.vacuum.model", null));
        assertEquals("urn:mixed:1", selectUrn(ALL_INSTANCES, "mixed.vacuum.model", null));
    }

    @Test
    public void requestedVersionIsSelected() throws IOException {
        assertEquals("urn:test:2", selectUrn(RELEASED_INSTANCES, "test.vacuum.model", 2));
        assertEquals("urn:mixed:2", selectUrn(ALL_INSTANCES, "mixed.vacuum.model", 2));
        assertNull(selectUrn(ALL_INSTANCES, "test.vacuum.model", 3));
    }

    @Test
    public void modelWithoutSpecIsNotFound() throws IOException {
        assertNull(selectUrn(RELEASED_INSTANCES, "unknown.vacuum.model", null));
        assertNull(selectUrn("{ \"instances\": [] }", "test.vacuum.model", null));
    }

    @Test
    public void unknownOrMissingStatusIsRankedBelowDebug() throws IOException {
        assertEquals("urn:status:1", selectUrn(ODD_INSTANCES, "status.vacuum.model", null));
        assertEquals("urn:status:9", selectUrn(ODD_INSTANCES, "status.vacuum.model", 9));
    }

    @Test
    public void instanceWithoutVersionOrUrnIsIgnored() throws IOException {
        assertEquals("urn:broken:1", selectUrn(ODD_INSTANCES, "broken.vacuum.model", null));
        assertNull(selectUrn(ODD_INSTANCES, "broken.vacuum.model", 7));
    }

    @Test
    public void interruptWhileReadingSpecListIsRestored() {
        HttpClient httpClient = mock(HttpClient.class);
        Request request = mock(Request.class);
        Response response = mock(Response.class);
        when(httpClient.newRequest(anyString())).thenReturn(request);
        when(request.timeout(anyLong(), any())).thenReturn(request);
        doAnswer(invocation -> {
            invocation.<Response.Listener> getArgument(0).onHeaders(response);
            return null;
        }).when(request).send(any());
        // the headers have arrived, the interrupt hits the read of the content that never arrives
        when(response.getStatus()).thenAnswer(invocation -> {
            Thread.currentThread().interrupt();
            return HttpStatus.OK_200;
        });

        try {
            assertThrows(MiotParseException.class, () -> MiotParser.parse("test.vacuum.model", httpClient));
            assertTrue(Thread.currentThread().isInterrupted(), "the interrupt must not be lost");
        } finally {
            Thread.interrupted();
        }
        verify(request).abort(any());
    }

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
        List<@Nullable String> values = Objects.requireNonNull(stateDescription.getOptions()).stream()
                .map(OptionsValueListDTO::getValue).toList();
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
    public void actionsOfServiceWithoutPropertiesAreOnActionsChannel() throws MiotParseException {
        List<MiIoBasicChannel> channels = new MiotParser("test.vacuum.model")
                .getDevice(JsonParser.parseString(ACTION_ONLY_SPEC)).getDevice().getChannels();

        assertEquals(List.of("actions"), channels.stream().map(MiIoBasicChannel::getChannel).toList());
        List<MiIoDeviceAction> actions = channels.get(0).getActions();
        assertEquals(List.of(1, 2), actions.stream().map(MiIoDeviceAction::getAiid).toList());
        // the second action has no list of inputs in the spec, which is the same as an empty list
        assertEquals(List.of(CommandParameterType.EMPTY, CommandParameterType.EMPTY),
                actions.stream().map(MiIoDeviceAction::getparameterType).toList());
    }

    @Test
    public void missingDescriptionAndFormatAreTolerated() throws MiotParseException {
        List<MiIoBasicChannel> channels = new MiotParser("test.vacuum.model")
                .getDevice(JsonParser.parseString(INCOMPLETE_SPEC)).getDevice().getChannels();

        assertEquals(List.of("mute", "custom_set_mute"), channels.stream().map(MiIoBasicChannel::getChannel).toList());
        assertEquals(List.of("Custom - Mute", "Custom - Set Mute"),
                channels.stream().map(MiIoBasicChannel::getFriendlyName).toList());
        assertEquals(List.of("String", "String"), channels.stream().map(MiIoBasicChannel::getType).toList());
    }

    @Test
    public void toJsonWritesWholeNumbersWithoutDecimal() throws MiotParseException {
        MiIoBasicDevice device = new MiotParser("test.vacuum.model").getDevice(JsonParser.parseString(INPUT_SPEC));
        String json = MiotParser.toJson(device);

        assertFalse(json.matches("(?s).*\\d\\.0\\D.*"),
                "no whole number may keep its decimal, also not the last one of a list");
        assertTrue(json.replaceAll("\\s", "").contains("\"parameters\":[1,2]"));
    }

    @Test
    public void whitespaceInDescriptionsIsNormalized() throws MiotParseException {
        List<MiIoBasicChannel> channels = new MiotParser("test.vacuum.model")
                .getDevice(JsonParser.parseString(WHITESPACE_SPEC)).getDevice().getChannels();

        assertEquals(List.of("Robot Cleaner - Device Status", "Robot Cleaner - Set Status"),
                channels.stream().map(MiIoBasicChannel::getFriendlyName).toList());
        for (MiIoBasicChannel channel : channels) {
            StateDescriptionDTO stateDescription = channel.getStateDescription();
            assertNotNull(stateDescription);
            assertEquals(List.of("In The Dry", "In The Dust"), Objects.requireNonNull(stateDescription.getOptions())
                    .stream().map(OptionsValueListDTO::getLabel).toList());
            assertEquals("Value mapping [\"10\"=\"In The Dry\",\"11\"=\"In The Dust\"]", channel.getReadmeComment());
        }
    }

    @Test
    public void toJsonKeepsDoubleSpacesInTexts() throws MiotParseException {
        MiIoBasicDevice device = new MiotParser("test.vacuum.model")
                .getDevice(JsonParser.parseString(DOUBLE_SPACE_SPEC));
        // a text edited by hand can have a double space, which is not indentation
        Objects.requireNonNull(
                Objects.requireNonNull(device.getDevice().getChannels().get(0).getStateDescription()).getOptions())
                .get(1).setLabel("In The  Dust");
        String json = MiotParser.toJson(device);

        assertTrue(json.contains("\"In The  Dust\""), "double space in a text must be preserved");
        assertFalse(json.contains("In The" + "\t" + "Dust"), "no tab may replace a double space in a text");
        assertTrue(json.contains("\r\n\t\t\"channels\""), "indentation is converted to tabs with CRLF line endings");
        assertFalse(json.contains("\n  "), "no space indentation is left");
    }
}
