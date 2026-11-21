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
package org.openhab.binding.miio.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.miio.internal.MiIoBindingConstants;
import org.openhab.binding.miio.internal.MiIoCommand;
import org.openhab.binding.miio.internal.MiIoSendCommand;
import org.openhab.binding.miio.internal.basic.BasicChannelTypeProvider;
import org.openhab.binding.miio.internal.basic.MiIoBasicChannel;
import org.openhab.binding.miio.internal.basic.MiIoBasicDevice;
import org.openhab.binding.miio.internal.basic.MiIoDatabaseWatchService;
import org.openhab.binding.miio.internal.cloud.CloudConnector;
import org.openhab.binding.miio.internal.miot.MiotParseException;
import org.openhab.binding.miio.internal.miot.MiotParser;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.types.Command;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * Tests the commands sent for actions that have the channel value inside a parameter, such as the input of MIoT
 * actions.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class MiIoBasicHandlerValueInParametersTest {

    private static final String CHANNEL = "test";
    private static final String ACTION_PREFIX = "action{\"did\":\"test\",\"siid\":4,\"aiid\":1";

    private @Mock @NonNullByDefault({}) Thing thing;
    private @Mock @NonNullByDefault({}) MiIoDatabaseWatchService miIoDatabaseWatchService;
    private @Mock @NonNullByDefault({}) CloudConnector cloudConnector;
    private @Mock @NonNullByDefault({}) ChannelTypeRegistry channelTypeRegistry;
    private @Mock @NonNullByDefault({}) BasicChannelTypeProvider basicChannelTypeProvider;
    private @Mock @NonNullByDefault({}) TranslationProvider translationProvider;
    private @Mock @NonNullByDefault({}) LocaleProvider localeProvider;

    private final ThingUID thingUID = new ThingUID(MiIoBindingConstants.THING_TYPE_BASIC, "TestThing");
    private final ChannelUID channelUID = new ChannelUID(thingUID, CHANNEL);
    private @NonNullByDefault({}) CapturingHandler handler;

    private class CapturingHandler extends MiIoBasicHandler {
        private final List<String> sentCommands = new ArrayList<>();
        private int lastCommandId;
        private boolean connected = true;
        private Instant now = Instant.parse("2026-01-01T00:00:00Z");

        CapturingHandler(Thing thing, MiIoDatabaseWatchService miIoDatabaseWatchService, CloudConnector cloudConnector,
                ChannelTypeRegistry channelTypeRegistry, BasicChannelTypeProvider basicChannelTypeProvider,
                TranslationProvider translationProvider, LocaleProvider localeProvider) {
            super(thing, miIoDatabaseWatchService, cloudConnector, channelTypeRegistry, basicChannelTypeProvider,
                    translationProvider, localeProvider);
        }

        void loadChannel(String channelFields, String actionsJson) {
            String channelJson = """
                    {"property": "", "channel": "%s", %s, "refresh": false, "actions": %s}
                    """.formatted(CHANNEL, channelFields, actionsJson);
            MiIoBasicChannel channel = new Gson().fromJson(channelJson, MiIoBasicChannel.class);
            assertNotNull(channel);
            actions.put(channelUID, channel);
        }

        void loadChannel(MiIoBasicChannel channel) {
            actions.put(new ChannelUID(thingUID, channel.getChannel()), channel);
        }

        void loadDatabase(String fileName) throws IOException {
            try (InputStream is = getClass().getResourceAsStream("/database/" + fileName)) {
                assertNotNull(is, "Database file not found: " + fileName);
                MiIoBasicDevice device = new Gson().fromJson(
                        JsonParser.parseReader(new InputStreamReader(is, StandardCharsets.UTF_8)),
                        MiIoBasicDevice.class);
                for (MiIoBasicChannel channel : device.getDevice().getChannels()) {
                    actions.put(new ChannelUID(thingUID, channel.getChannel()), channel);
                }
            }
        }

        void poll(String property, JsonPrimitive value) {
            updateChannel(null, property, value);
        }

        void answer(int commandId, String response) {
            MiIoSendCommand command = new MiIoSendCommand(commandId, MiIoCommand.SET_PROPERTIES, new JsonObject(), "");
            command.setResponse(JsonParser.parseString(response).getAsJsonObject());
            onMessageReceived(command);
        }

        List<String> send(String channel, Command command) {
            sentCommands.clear();
            handleCommand(new ChannelUID(thingUID, channel), command);
            return new ArrayList<>(sentCommands);
        }

        List<String> send(Command command) {
            return send(CHANNEL, command);
        }

        @Override
        Instant now() {
            return now;
        }

        @Override
        protected int sendCommand(String command, String params, String cloudServer, String sender) {
            sentCommands.add(command + params);
            return connected ? ++lastCommandId : 0;
        }
    }

    @BeforeEach
    public void setUp() {
        when(thing.getUID()).thenReturn(thingUID);
        handler = new CapturingHandler(thing, miIoDatabaseWatchService, cloudConnector, channelTypeRegistry,
                basicChannelTypeProvider, translationProvider, localeProvider);
    }

    private static String miotAction(String parameterType, String parameters) {
        return miotAction(parameterType, parameters, "");
    }

    private static String miotAction(String parameterType, String parameters, String condition) {
        return """
                {"command": "action", "parameterType": "%s", "siid": 4, "aiid": 1, "parameters": %s%s}
                """.formatted(parameterType, parameters, condition.isEmpty() ? "" : ", \"condition\": " + condition);
    }

    private static List<String> sent(String in) {
        return List.of(ACTION_PREFIX + ",\"in\":" + in + "}");
    }

    @Test
    public void numberIsInsertedAsNumber() {
        handler.loadChannel("\"type\": \"Number\"",
                "[" + miotAction("NUMBER", "[{\"piid\": 1, \"value\": \"$value$\"}]") + "]");

        assertEquals(sent("[{\"piid\":1,\"value\":18}]"), handler.send(new DecimalType(18)));
    }

    @Test
    public void quantityIsConvertedToChannelUnit() {
        handler.loadChannel("\"type\": \"Number:Time\", \"unit\": \"seconds\"",
                "[" + miotAction("NUMBER", "[{\"piid\": 1, \"value\": \"$value$\"}]") + "]");

        assertEquals(sent("[{\"piid\":1,\"value\":120}]"), handler.send(new QuantityType<>(2, Units.MINUTE)));
    }

    @Test
    public void textIsInsertedAsTyped() {
        handler.loadChannel("\"type\": \"String\"",
                "[" + miotAction("STRING", "[{\"piid\": 1, \"value\": \"$value$\"}]") + "]");

        assertEquals(sent("[{\"piid\":1,\"value\":\"Living Room\"}]"), handler.send(new StringType("Living Room")));
    }

    @Test
    public void switchIsInsertedAccordingToParameterType() {
        handler.loadChannel("\"type\": \"Switch\"",
                "[" + miotAction("ONOFFBOOL", "[{\"piid\": 1, \"value\": \"$value$\"}]") + "]");
        assertEquals(sent("[{\"piid\":1,\"value\":true}]"), handler.send(OnOffType.ON));
        assertEquals(sent("[{\"piid\":1,\"value\":false}]"), handler.send(OnOffType.OFF));

        handler.loadChannel("\"type\": \"Switch\"",
                "[" + miotAction("ONOFFNUMBER", "[{\"piid\": 1, \"value\": \"$value$\"}]") + "]");
        assertEquals(sent("[{\"piid\":1,\"value\":1}]"), handler.send(OnOffType.ON));
    }

    @Test
    public void valueIsInsertedInsideText() {
        handler.loadChannel("\"type\": \"Number\"", "[" + miotAction("NUMBER",
                "[{\"piid\": 1, \"value\": 18}, {\"piid\": 10, \"value\": \"{\\\"selects\\\":[[$VALUE$,1,1,2,1]]}\"}]")
                + "]");

        assertEquals(sent("[{\"piid\":1,\"value\":18},{\"piid\":10,\"value\":\"{\\\"selects\\\":[[3,1,1,2,1]]}\"}]"),
                handler.send(new DecimalType(3)));
    }

    @Test
    public void everyTokenIsReplaced() {
        handler.loadChannel("\"type\": \"Number\"", "[" + miotAction("NUMBER",
                "[{\"piid\": 1, \"value\": \"$value$\"}, {\"piid\": 2, \"value\": [\"$value$\", \"id-$value$\"]}]")
                + "]");

        assertEquals(sent("[{\"piid\":1,\"value\":7},{\"piid\":2,\"value\":[7,\"id-7\"]}]"),
                handler.send(new DecimalType(7)));
    }

    @Test
    public void textWithSpecialCharactersIsInsertedLiterally() {
        handler.loadChannel("\"type\": \"String\"",
                "[" + miotAction("STRING", "[{\"piid\": 1, \"value\": \"id:$value$\"}]") + "]");

        assertEquals(sent("[{\"piid\":1,\"value\":\"id:a$1\\\\b\"}]"), handler.send(new StringType("a$1\\b")));
    }

    @Test
    public void conditionSelectsTheActionAndTheValue() {
        String roomAction = miotAction("STRING", "[{\"piid\": 1, \"value\": \"$value$\"}]",
                "{\"name\": \"matchValue\", \"parameters\": [{\"matchValue\": \"kitchen\", \"returnValue\": 3}, {\"matchValue\": \"room.*\"}]}");
        handler.loadChannel("\"type\": \"String\"", "[" + roomAction + "]");

        assertEquals(sent("[{\"piid\":1,\"value\":3}]"), handler.send(new StringType("kitchen")));
        assertEquals(sent("[{\"piid\":1,\"value\":\"room5\"}]"), handler.send(new StringType("room5")));
        assertEquals(List.of(), handler.send(new StringType("other")));
    }

    @Test
    public void commandWithoutValueIsNotSent() {
        handler.loadChannel("\"type\": \"String\"",
                "[" + miotAction("NUMBER", "[{\"piid\": 1, \"value\": \"$value$\"}]") + "]");

        assertEquals(List.of(), handler.send(new StringType("text")));
    }

    @Test
    public void parametersAreNotChangedBySending() {
        handler.loadChannel("\"type\": \"Number\"",
                "[" + miotAction("NUMBER", "[{\"piid\": 1, \"value\": \"$value$\"}]") + "]");

        assertEquals(sent("[{\"piid\":1,\"value\":1}]"), handler.send(new DecimalType(1)));
        assertEquals(sent("[{\"piid\":1,\"value\":2}]"), handler.send(new DecimalType(2)));
    }

    @Test
    public void actionsWithoutTokenAreUnchanged() {
        handler.loadChannel("\"type\": \"String\"",
                "[" + miotAction("EMPTY", "[{\"piid\": 1, \"value\": 2}, {\"piid\": 3, \"value\": \"a\"}]") + "]");
        assertEquals(sent("[{\"piid\":1,\"value\":2},{\"piid\":3,\"value\":\"a\"}]"),
                handler.send(new StringType("any")));

        handler.loadChannel("\"type\": \"String\"", "[" + miotAction("EMPTY", "[]") + "]");
        assertEquals(sent("[]"), handler.send(new StringType("any")));

        handler.loadChannel("\"type\": \"Number\"", "[" + miotAction("NUMBER", "[]") + "]");
        assertEquals(sent("5"), handler.send(new DecimalType(5)));
    }

    @Test
    public void tokenThatCannotBeReplacedIsNotSent() {
        // EMPTY ignores the command, so there is no value for the token
        handler.loadChannel("\"type\": \"String\"",
                "[" + miotAction("EMPTY", "[{\"piid\": 1, \"value\": \"$value$\"}]") + "]");

        assertEquals(List.of(), handler.send(new StringType("any")));

        String returnValueWithToken = miotAction("STRING", "[]",
                "{\"name\": \"matchValue\", \"parameters\": [{\"matchValue\": \"a\", \"returnValue\": [{\"piid\": 1, \"value\": \"$value$\"}]}]}");
        handler.loadChannel("\"type\": \"String\"", "[" + returnValueWithToken + "]");
        assertEquals(List.of(), handler.send(new StringType("a")));
    }

    @Test
    public void legacyCommandsAreUnchanged() {
        handler.loadChannel("\"type\": \"Number\"",
                "[{\"command\": \"set_x\", \"parameterType\": \"NUMBER\", \"parameters\": [\"a\", \"$value$\"]}]");
        assertEquals(List.of("set_x[\"a\",5]"), handler.send(new DecimalType(5)));
    }

    @Test
    public void valueIsInsertedInsideParameterOfLegacyCommand() {
        handler.loadChannel("\"type\": \"Number\"",
                "[{\"command\": \"set_x\", \"parameterType\": \"NUMBER\", \"parameters\": [\"a\", {\"key\": \"$value$\", \"text\": \"v$value$\"}]}]");
        assertEquals(List.of("set_x[\"a\",{\"key\":5,\"text\":\"v5\"}]"), handler.send(new DecimalType(5)));

        handler.loadChannel("\"type\": \"Switch\"",
                "[{\"command\": \"set_x\", \"parameterType\": \"ONOFF\", \"parameters\": [{\"key\": \"$value$\"}]}]");
        assertEquals(List.of("set_x[{\"key\":\"on\"}]"), handler.send(OnOffType.ON));
    }

    @Test
    public void legacyCommandWithTokenThatCannotBeReplacedIsNotSent() {
        // EMPTY and ONOFFPARA have no value for the token
        handler.loadChannel("\"type\": \"Switch\"",
                "[{\"command\": \"set_x\", \"parameterType\": \"EMPTY\", \"parameters\": [{\"key\": \"$value$\"}]}]");
        assertEquals(List.of(), handler.send(OnOffType.ON));

        handler.loadChannel("\"type\": \"Switch\"",
                "[{\"command\": \"set_*\", \"parameterType\": \"ONOFFPARA\", \"parameters\": [{\"key\": \"$value$\"}]}]");
        assertEquals(List.of(), handler.send(OnOffType.ON));

        handler.loadChannel("\"type\": \"Switch\"", "[{\"command\": \"set_*\", \"parameterType\": \"ONOFFPARA\"}]");
        assertEquals(List.of("set_on[]"), handler.send(OnOffType.ON));
    }

    @Test
    public void generatedActionInputChannelsAreSent() throws MiotParseException {
        String spec = """
                {
                  "type": "urn:miot-spec-v2:device:vacuum:0000A006:test-model:1",
                  "description": "Robot Cleaner",
                  "services": [
                    {
                      "iid": 4,
                      "type": "urn:miot-spec-v2:service:custom:00007801:test-model:1",
                      "description": "Custom",
                      "properties": [
                        { "iid": 1, "type": "urn:miot-spec-v2:property:room-id:00000001:test-model:1",
                          "description": "Room Id", "format": "uint8", "access": ["read", "write"] },
                        { "iid": 2, "type": "urn:miot-spec-v2:property:room-name:00000002:test-model:1",
                          "description": "Room Name", "format": "string", "access": ["read", "write"] }
                      ],
                      "actions": [
                        { "iid": 1, "type": "urn:miot-spec-v2:action:set-room:00002801:test-model:1",
                          "description": "Set Room", "in": [1], "out": [] },
                        { "iid": 2, "type": "urn:miot-spec-v2:action:rename-room:00002802:test-model:1",
                          "description": "Rename Room", "in": [2], "out": [] }
                      ]
                    }
                  ]
                }
                """;
        MiotParser parser = new MiotParser("test.model");
        for (MiIoBasicChannel channel : parser.getDevice(JsonParser.parseString(spec)).getDevice().getChannels()) {
            handler.loadChannel(channel);
        }

        assertEquals(
                List.of("action{\"did\":\"custom_set_room\",\"siid\":4,\"aiid\":1,\"in\":[{\"piid\":1,\"value\":3}]}"),
                handler.send("custom_set_room", new DecimalType(3)));
        assertEquals(List.of(
                "action{\"did\":\"custom_rename_room\",\"siid\":4,\"aiid\":2,\"in\":[{\"piid\":2,\"value\":\"Living Room\"}]}"),
                handler.send("custom_rename_room", new StringType("Living Room")));
    }

    @Test
    public void databaseActionsWithConstantInputAreUnchanged() throws IOException {
        handler.loadDatabase("dreame.vacuum.p2114a-miot.json");

        assertEquals(
                List.of("action{\"did\":\"actions\",\"siid\":4,\"aiid\":4,\"in\":[{\"piid\":10,\"value\":\"2,1\"}]}"),
                handler.send("actions", new StringType("vacuum-start-mop-wash")));
        assertEquals(List.of("action{\"did\":\"actions\",\"siid\":4,\"aiid\":2,\"in\":[]}"),
                handler.send("actions", new StringType("vacuum-extend-stop-clean")));
    }

    @ParameterizedTest
    @ValueSource(strings = { "yeelink.light.ceila-miot.json", "yeelink.light.ceilb-miot.json",
            "yeelink.light.ceil34-miot.json", "yeelink.light.fancl2-miot.json", "yeelink.light.stripb-miot.json" })
    public void miotDimmerAlsoSwitchesThePowerProperty(String fileName) throws IOException {
        handler.loadDatabase(fileName);
        String powerOff = "set_properties[{\"did\":\"on\",\"siid\":2,\"piid\":1,\"value\":false}]";
        String powerOn = "set_properties[{\"did\":\"on\",\"siid\":2,\"piid\":1,\"value\":true}]";

        assertEquals(List.of(powerOff), handler.send("brightness", OnOffType.OFF));
        assertEquals(List.of(powerOff), handler.send("brightness", PercentType.ZERO));
        assertEquals(List.of(powerOn), handler.send("brightness", OnOffType.ON));
        assertEquals(List.of("set_properties[{\"did\":\"brightness\",\"siid\":2,\"piid\":3,\"value\":50}]", powerOn),
                handler.send("brightness", new PercentType(50)));
    }

    @Test
    public void miotColorAlsoSetsBrightnessAndPower() throws IOException {
        handler.loadDatabase("yeelink.light.ceilb-miot.json");
        String powerOff = "set_properties[{\"did\":\"on1\",\"siid\":3,\"piid\":1,\"value\":false}]";
        String powerOn = "set_properties[{\"did\":\"on1\",\"siid\":3,\"piid\":1,\"value\":true}]";
        String bright50 = "set_properties[{\"did\":\"brightness1\",\"siid\":3,\"piid\":3,\"value\":50}]";

        assertEquals(
                List.of("set_properties[{\"did\":\"color\",\"siid\":3,\"piid\":5,\"value\":128}]", bright50, powerOn),
                handler.send("color", new HSBType("240,100,50")));
        assertEquals(List.of(bright50, powerOn), handler.send("color", new PercentType(50)));
        assertEquals(List.of(powerOff), handler.send("color", OnOffType.OFF));
        // the color itself is also sent, converted with the HSB brightness (black), see the COLOR conversion
        List<String> sent = handler.send("color", new HSBType("240,100,0"));
        assertEquals(powerOff, sent.get(sent.size() - 1));
        assertEquals(List.of(powerOff), handler.send("brightness1", OnOffType.OFF));
    }

    @ParameterizedTest
    @ValueSource(strings = { "yeelink.light.bslamp2.json", "yeelink.light.ceil43.json", "yeelink.light.ceiling.json",
            "yeelink.light.ceiling4.json" })
    public void nightLightDimmerSwitchesThePower(String fileName) throws IOException {
        handler.loadDatabase(fileName);
        String powerOff = "set_power[\"off\",\"smooth\",500]";

        assertEquals(List.of(powerOff), handler.send("nightlightBrightness", OnOffType.OFF));
        assertEquals(List.of(powerOff), handler.send("nightlightBrightness", PercentType.ZERO));
        assertEquals(List.of("set_power[\"on\",\"smooth\",500,5]"), handler.send("nightlightBrightness", OnOffType.ON));
        assertEquals(List.of("set_scene[\"nightlight\",50]"),
                handler.send("nightlightBrightness", new PercentType(50)));
    }

    private static MiIoBasicChannel channel(String json) {
        MiIoBasicChannel channel = new Gson().fromJson(json, MiIoBasicChannel.class);
        assertNotNull(channel);
        return channel;
    }

    @Test
    public void miotSettingSentBeforeAnotherCommandIsUsedByIt() {
        handler.loadChannel(channel("""
                {"property": "target-temperature", "siid": 2, "piid": 4, "channel": "target", "type": "Number",
                 "actions": [{"command": "set_properties", "parameterType": "NUMBER"}]}
                """));
        handler.loadChannel(channel("""
                {"property": "", "channel": "start", "type": "String", "actions": [{"command": "set_properties",
                 "parameterType": "NONE", "parameters": [{"did": "target-temperature", "siid": 2, "piid": 4,
                 "value": "$target-temperature$"}], "condition": {"name": "matchValue", "parameters":
                 [{"matchValue": "start", "returnValue": 1}]}}]}
                """));
        handler.poll("target-temperature", new JsonPrimitive(90));

        assertEquals(List.of("set_properties[{\"did\":\"target\",\"siid\":2,\"piid\":4,\"value\":80}]"),
                handler.send("target", new DecimalType(80)));
        assertEquals(List.of("set_properties[{\"did\":\"target-temperature\",\"siid\":2,\"piid\":4,\"value\":80}]"),
                handler.send("start", new StringType("start")));
    }

    private void loadTargetAndStartChannels() {
        handler.loadChannel(channel("""
                {"property": "target-temperature", "siid": 2, "piid": 4, "channel": "target", "type": "Number",
                 "actions": [{"command": "set_properties", "parameterType": "NUMBER"}]}
                """));
        handler.loadChannel(channel("""
                {"property": "", "channel": "start", "type": "String", "actions": [{"command": "set_properties",
                 "parameterType": "NONE", "parameters": [{"did": "target-temperature", "siid": 2, "piid": 4,
                 "value": "$target-temperature$"}], "condition": {"name": "matchValue", "parameters":
                 [{"matchValue": "start", "returnValue": 1}]}}]}
                """));
        handler.poll("target-temperature", new JsonPrimitive(90));
    }

    private List<String> startWith(int targetTemperature) {
        return List.of("set_properties[{\"did\":\"target-temperature\",\"siid\":2,\"piid\":4,\"value\":"
                + targetTemperature + "}]");
    }

    @Test
    public void pollAnsweredBeforeTheSettingIsAnsweredDoesNotReplaceIt() {
        loadTargetAndStartChannels();

        handler.send("target", new DecimalType(80));
        int settingId = handler.lastCommandId;
        handler.poll("target-temperature", new JsonPrimitive(90));
        assertEquals(startWith(80), handler.send("start", new StringType("start")));

        handler.answer(settingId, "{\"result\": [{\"did\": \"target\", \"siid\": 2, \"piid\": 4, \"code\": 0}]}");
        assertEquals(startWith(80), handler.send("start", new StringType("start")));
        handler.poll("target-temperature", new JsonPrimitive(85));
        assertEquals(startWith(85), handler.send("start", new StringType("start")));
    }

    @Test
    public void settingThatIsNeverAnsweredNoLongerBlocksPollsAfterAMinute() {
        loadTargetAndStartChannels();

        handler.send("target", new DecimalType(80));
        handler.now = handler.now.plus(Duration.ofSeconds(59));
        handler.poll("target-temperature", new JsonPrimitive(90));
        assertEquals(startWith(80), handler.send("start", new StringType("start")));

        handler.now = handler.now.plus(Duration.ofSeconds(2));
        handler.poll("target-temperature", new JsonPrimitive(85));
        assertEquals(startWith(85), handler.send("start", new StringType("start")));
    }

    @Test
    public void settingThatIsNotQueuedIsNotKept() {
        loadTargetAndStartChannels();
        handler.connected = false;

        handler.send("target", new DecimalType(80));
        assertEquals(startWith(90), handler.send("start", new StringType("start")));
    }

    @ParameterizedTest
    @ValueSource(strings = { "{\"error\": \"No Response from device\"}",
            "{\"result\": [{\"did\": \"target\", \"siid\": 2, \"piid\": 4, \"code\": -4005}]}" })
    public void settingRejectedByTheDeviceIsReverted(String response) {
        loadTargetAndStartChannels();

        handler.send("target", new DecimalType(80));
        handler.answer(handler.lastCommandId, response);
        assertEquals(startWith(90), handler.send("start", new StringType("start")));
    }

    private static final String ACCEPTED = "{\"result\": [{\"did\": \"target\", \"siid\": 2, \"piid\": 4, \"code\": 0}]}";
    private static final String REJECTED = "{\"result\": [{\"did\": \"target\", \"siid\": 2, \"piid\": 4, \"code\": -4005}]}";

    @Test
    public void twoRejectedSettingsRevertToTheConfirmedValue() {
        loadTargetAndStartChannels();

        handler.send("target", new DecimalType(80));
        int firstId = handler.lastCommandId;
        handler.send("target", new DecimalType(70));
        int secondId = handler.lastCommandId;

        handler.answer(firstId, REJECTED);
        assertEquals(startWith(70), handler.send("start", new StringType("start")));
        handler.answer(secondId, REJECTED);
        assertEquals(startWith(90), handler.send("start", new StringType("start")));
    }

    @Test
    public void rejectedSettingRevertsToAnEarlierAcceptedSetting() {
        loadTargetAndStartChannels();

        handler.send("target", new DecimalType(80));
        int firstId = handler.lastCommandId;
        handler.send("target", new DecimalType(70));
        int secondId = handler.lastCommandId;

        handler.answer(firstId, ACCEPTED);
        assertEquals(startWith(70), handler.send("start", new StringType("start")));
        handler.answer(secondId, REJECTED);
        assertEquals(startWith(80), handler.send("start", new StringType("start")));
    }

    @Test
    public void earlierRejectedSettingKeepsTheLaterAcceptedSetting() {
        loadTargetAndStartChannels();

        handler.send("target", new DecimalType(80));
        int firstId = handler.lastCommandId;
        handler.send("target", new DecimalType(70));
        int secondId = handler.lastCommandId;

        handler.answer(firstId, REJECTED);
        handler.answer(secondId, ACCEPTED);
        assertEquals(startWith(70), handler.send("start", new StringType("start")));
    }

    @Test
    public void legacyNumberSentBeforeAnotherCommandIsUsedByIt() {
        handler.loadChannel(channel("""
                {"property": "appointStart", "channel": "appointStart", "type": "Number", "actions": [{"command":
                 "set_appoint", "parameterType": "NUMBER", "parameters": [1, "$value$", "$appointEnd$"]}]}
                """));
        handler.loadChannel(channel("""
                {"property": "appointEnd", "channel": "appointEnd", "type": "Number", "actions": [{"command":
                 "set_appoint", "parameterType": "NUMBER", "parameters": [1, "$appointStart$", "$value$"]}]}
                """));
        handler.poll("appointStart", new JsonPrimitive(6));
        handler.poll("appointEnd", new JsonPrimitive(8));

        assertEquals(List.of("set_appoint[1,5,8]"), handler.send("appointStart", new DecimalType(5)));
        assertEquals(List.of("set_appoint[1,5,9]"), handler.send("appointEnd", new DecimalType(9)));
    }

    /**
     * Loads a channel whose command sends the current values of the given properties.
     */
    private void loadShowChannel(String... properties) {
        StringBuilder parameters = new StringBuilder("\"$value$\"");
        for (String property : properties) {
            parameters.append(", \"$").append(property).append("$\"");
        }
        handler.loadChannel(channel("""
                {"property": "", "channel": "show", "type": "String", "actions": [{"command": "show",
                 "parameterType": "STRING", "parameters": [%s]}]}
                """.formatted(parameters)));
    }

    private List<String> show() {
        return handler.send("show", new StringType("x"));
    }

    @Test
    public void legacySwitchAndTextAreKept() {
        handler.loadChannel(channel("""
                {"property": "power", "channel": "power", "type": "Switch", "actions": [{"command": "set_power",
                 "parameterType": "ONOFF"}]}
                """));
        handler.loadChannel(channel("""
                {"property": "mode", "channel": "mode", "type": "String", "actions": [{"command": "set_mode",
                 "parameterType": "STRING", "condition": {"name": "LowerCase"}}]}
                """));
        loadShowChannel("power", "mode");
        handler.poll("power", new JsonPrimitive("off"));
        handler.poll("mode", new JsonPrimitive("auto"));

        handler.send("power", OnOffType.ON);
        handler.send("mode", new StringType("Silent"));
        assertEquals(List.of("show[\"x\",on,silent]"), show());
    }

    @Test
    public void legacyValueNestedUnderThePropertyNameIsKept() {
        handler.loadChannel(channel("""
                {"property": "lp_threshold", "channel": "lp_threshold", "type": "Number", "actions": [{"command":
                 "set_lowpower_autooff", "parameterType": "NUMBER", "parameters": [{"lp_autooff": 1,
                 "lp_threshold": "$value$"}]}]}
                """));
        handler.loadChannel(channel("""
                {"property": "lp_other", "channel": "lp_other", "type": "Number", "actions": [{"command":
                 "set_other", "parameterType": "NUMBER", "parameters": [{"level": "$value$"}]}]}
                """));
        loadShowChannel("lp_threshold", "lp_other");
        handler.poll("lp_threshold", new JsonPrimitive(10));
        handler.poll("lp_other", new JsonPrimitive(1));

        handler.send("lp_threshold", new DecimalType(20));
        handler.send("lp_other", new DecimalType(2));
        assertEquals(List.of("show[\"x\",20,1]"), show());
    }

    @Test
    public void legacyLightCompanionActionsDoNotChangeTheProperty() throws IOException {
        handler.loadDatabase("yeelink.light.bslamp2.json");
        loadShowChannel("bright", "rgb", "nl_br");
        handler.poll("bright", new JsonPrimitive(30));
        handler.poll("rgb", new JsonPrimitive(255));
        handler.poll("nl_br", new JsonPrimitive(5));

        handler.send("brightness", new PercentType(60));
        handler.send("rgbColor", new HSBType("120,100,40"));
        handler.send("nightlightBrightness", OnOffType.ON);
        assertEquals(List.of("show[\"x\",60,255,5]"), show());
    }

    @Test
    public void valueChangedByAConditionIsNotKept() {
        handler.loadChannel(channel("""
                {"property": "mode", "channel": "mode", "type": "Number", "actions": [{"command": "set_mode",
                 "parameterType": "NUMBER", "condition": {"name": "matchValue", "parameters": [{"matchValue": "2",
                 "returnValue": 7}]}}]}
                """));
        loadShowChannel("mode");
        handler.poll("mode", new JsonPrimitive(1));

        assertEquals(List.of("set_mode[7]"), handler.send("mode", new DecimalType(2)));
        assertEquals(List.of("show[\"x\",1]"), show());
    }

    @Test
    public void miotValueChangedByAConditionIsKept() {
        handler.loadChannel(channel("""
                {"property": "mode", "siid": 2, "piid": 3, "channel": "mode", "type": "String", "actions":
                 [{"command": "set_properties", "parameterType": "STRING", "condition": {"name": "matchValue",
                 "parameters": [{"matchValue": "left", "returnValue": 2}]}}]}
                """));
        loadShowChannel("mode");
        handler.poll("mode", new JsonPrimitive(1));

        handler.send("mode", new StringType("left"));
        assertEquals(List.of("show[\"x\",2]"), show());
    }

    @Test
    public void miotNestedPropertyAndActionInputAreNotKept() {
        handler.loadChannel(channel("""
                {"property": "brightness", "siid": 2, "piid": 2, "channel": "brightness", "type": "Dimmer",
                 "actions": [{"command": "set_properties", "parameterType": "ONOFFBOOL", "parameters": [{"did": "on",
                 "siid": 2, "piid": 1, "value": "$value$"}]}]}
                """));
        handler.loadChannel(channel("""
                {"property": "start-time", "channel": "start", "type": "Number", "actions":
                 [{"command": "action", "parameterType": "NUMBER", "siid": 3, "aiid": 1}]}
                """));
        loadShowChannel("brightness", "start-time");
        handler.poll("brightness", new JsonPrimitive(30));
        handler.poll("start-time", new JsonPrimitive(5));

        handler.send("brightness", OnOffType.ON);
        handler.send("start", new DecimalType(9));
        assertEquals(List.of("show[\"x\",30,5]"), show());
    }
}
