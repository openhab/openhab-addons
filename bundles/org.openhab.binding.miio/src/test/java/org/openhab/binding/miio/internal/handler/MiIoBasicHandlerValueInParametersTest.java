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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.miio.internal.MiIoBindingConstants;
import org.openhab.binding.miio.internal.basic.BasicChannelTypeProvider;
import org.openhab.binding.miio.internal.basic.MiIoBasicChannel;
import org.openhab.binding.miio.internal.basic.MiIoBasicDevice;
import org.openhab.binding.miio.internal.basic.MiIoDatabaseWatchService;
import org.openhab.binding.miio.internal.cloud.CloudConnector;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.types.Command;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

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

        List<String> send(String channel, Command command) {
            sentCommands.clear();
            handleCommand(new ChannelUID(thingUID, channel), command);
            return new ArrayList<>(sentCommands);
        }

        List<String> send(Command command) {
            return send(CHANNEL, command);
        }

        @Override
        protected int sendCommand(String command, String params, String cloudServer, String sender) {
            sentCommands.add(command + params);
            return 1;
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
    public void databaseActionsWithConstantInputAreUnchanged() throws IOException {
        handler.loadDatabase("dreame.vacuum.mc1808-miot.json");

        assertEquals(
                List.of("action{\"did\":\"vacuumaction\",\"siid\":18,\"aiid\":1,\"in\":[{\"piid\":1,\"value\":2}]}"),
                handler.send("vacuumaction", new StringType("vacuum")));
        assertEquals(List.of("action{\"did\":\"vacuumaction\",\"siid\":18,\"aiid\":2,\"in\":[]}"),
                handler.send("vacuumaction", new StringType("stop")));
    }
}
