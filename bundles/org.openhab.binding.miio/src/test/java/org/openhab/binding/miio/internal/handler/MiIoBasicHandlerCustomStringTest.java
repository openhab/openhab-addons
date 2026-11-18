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
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.types.Command;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

/**
 * Tests the commands sent for actions with the STRING, CUSTOMSTRING and JSON parameter types.
 *
 * @author Marcel Verpaalen - Initial contribution
 * @author Marcel Verpaalen - Add JSON parameter type
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class MiIoBasicHandlerCustomStringTest {

    private static final String CHANNEL = "test";

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

        List<String> send(String actionsJson, String command) {
            return send(actionsJson, new StringType(command));
        }

        List<String> send(String actionsJson, Command command) {
            String channelJson = """
                    {"property": "", "channel": "%s", "type": "String", "refresh": false, "actions": %s}
                    """.formatted(CHANNEL, actionsJson);
            MiIoBasicChannel channel = new Gson().fromJson(channelJson, MiIoBasicChannel.class);
            assertNotNull(channel);
            actions.put(channelUID, channel);
            sentCommands.clear();
            handleCommand(channelUID, command);
            return new ArrayList<>(sentCommands);
        }

        List<String> sendToDatabaseChannel(String fileName, String channelId, String command) throws IOException {
            try (InputStream is = getClass().getResourceAsStream("/database/" + fileName)) {
                assertNotNull(is, "Database file not found: " + fileName);
                MiIoBasicDevice device = new Gson().fromJson(
                        JsonParser.parseReader(new InputStreamReader(is, StandardCharsets.UTF_8)),
                        MiIoBasicDevice.class);
                for (MiIoBasicChannel channel : device.getDevice().getChannels()) {
                    actions.put(new ChannelUID(thingUID, channel.getChannel()), channel);
                }
            }
            sentCommands.clear();
            handleCommand(new ChannelUID(thingUID, channelId), new StringType(command));
            return new ArrayList<>(sentCommands);
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

    private static String action(String parameterType, String parameters) {
        return """
                {"command": "set_x", "parameterType": "%s", "parameters": %s}
                """.formatted(parameterType, parameters);
    }

    @Test
    public void customStringReplacesValueToken() {
        assertEquals(List.of("set_x[\"Auto\"]"),
                handler.send("[" + action("CUSTOMSTRING", "[\"$value$\"]") + "]", "Auto"));
    }

    @Test
    public void customStringReplacesValueTokenCaseInsensitive() {
        assertEquals(List.of("set_x[1,\"auto\"]"),
                handler.send("[" + action("CUSTOMSTRING", "[1, \"$VALUE$\"]") + "]", "auto"));
    }

    @Test
    public void customStringKeepsSurroundingText() {
        assertEquals(List.of("set_x[\"color,auto,100\"]"),
                handler.send("[" + action("CUSTOMSTRING", "[\"color,$value$,100\"]") + "]", "auto"));
    }

    @Test
    public void customStringInsertsCommandLiterally() {
        assertEquals(List.of("set_x[\"a$1\\\\b\"]"),
                handler.send("[" + action("CUSTOMSTRING", "[\"$value$\"]") + "]", "a$1\\b"));
    }

    @Test
    public void customStringWithoutParametersSendsText() {
        assertEquals(List.of("set_x[\"Auto\"]"), handler.send("[" + action("CUSTOMSTRING", "[]") + "]", "Auto"));
        assertEquals(List.of("set_x[\"Auto\"]"),
                handler.send("[{\"command\": \"set_x\", \"parameterType\": \"CUSTOMSTRING\"}]", "Auto"));
    }

    @Test
    public void customStringWithoutValueTokenSendsText() {
        assertEquals(List.of("set_x[\"auto\",2]"),
                handler.send("[" + action("CUSTOMSTRING", "[\"fixed\", 2]") + "]", "auto"));
        assertEquals(List.of("set_x[\"auto\"]"), handler.send("[" + action("CUSTOMSTRING", "[1]") + "]", "auto"));
    }

    @Test
    public void valuePositionIsNotCarriedOverToNextAction() {
        String actions = "[" + action("CUSTOMSTRING", "[1, 2, \"$value$\"]") + "," + action("CUSTOMSTRING", "[]") + ","
                + action("STRING", "[\"a\", \"b\"]") + "]";
        assertEquals(List.of("set_x[1,2,\"auto\"]", "set_x[\"auto\"]", "set_x[\"auto\",\"b\"]"),
                handler.send(actions, "auto"));
    }

    @Test
    public void textIsSentAsTyped() {
        assertEquals(List.of("set_x[\"Auto\"]"), handler.send("[" + action("STRING", "[]") + "]", "Auto"));
        assertEquals(List.of("set_x[1,\"Living Room\"]"),
                handler.send("[" + action("STRING", "[1, \"$value$\"]") + "]", "Living Room"));
        assertEquals(List.of("set_x[\"color,Auto,100\"]"),
                handler.send("[" + action("CUSTOMSTRING", "[\"color,$value$,100\"]") + "]", "Auto"));
    }

    @Test
    public void lowerCaseConditionConvertsText() {
        String condition = ", \"condition\": {\"name\": \"LowerCase\"}}";
        assertEquals(List.of("set_x[\"on\"]"),
                handler.send("[{\"command\": \"set_x\", \"parameterType\": \"STRING\"" + condition + "]", "ON"));
        assertEquals(List.of("set_x[\"auto\"]"), handler
                .send("[{\"command\": \"set_x\", \"parameterType\": \"CUSTOMSTRING\"" + condition + "]", "Auto"));
    }

    private static final String JSON_ACTION = "[{\"command\": \"action\", \"parameterType\": \"JSON\", \"siid\": 3, \"aiid\": 1}]";

    @Test
    public void jsonListIsSentAsActionInput() {
        assertEquals(
                List.of("action{\"did\":\"test\",\"siid\":3,\"aiid\":1,\"in\":[\"C7795\",\"u0064\",11,180,0,0,2]}"),
                handler.send(JSON_ACTION, "[\"C7795\", \"u0064\", 11, 180, 0, 0, 2]"));
        assertEquals(List.of("action{\"did\":\"test\",\"siid\":3,\"aiid\":1,\"in\":[]}"),
                handler.send(JSON_ACTION, "[]"));
        assertEquals(List.of("action{\"did\":\"test\",\"siid\":3,\"aiid\":1,\"in\":[\"C7795\",11,180]}"),
                handler.send(JSON_ACTION, "[\"C7795\", 11, 180]"));
    }

    @Test
    public void jsonOtherThanListIsNotSent() {
        assertEquals(List.of(), handler.send(JSON_ACTION, "C7795"));
        assertEquals(List.of(), handler.send(JSON_ACTION, "{\"a\": 1}"));
        assertEquals(List.of(), handler.send(JSON_ACTION, "[\"C7795\", 11"));
        assertEquals(List.of(), handler.send(JSON_ACTION, "[\"C7795\",,180]"));
        assertEquals(List.of(), handler.send(JSON_ACTION, "[C7795, 11]"));
        assertEquals(List.of(), handler.send(JSON_ACTION, "[1,2] x"));
        assertEquals(List.of(), handler.send(JSON_ACTION, ""));
    }

    @Test
    public void jsonWithOtherThanNumbersAndTextIsNotSent() {
        assertEquals(List.of(), handler.send(JSON_ACTION, "[NaN]"));
        assertEquals(List.of(), handler.send(JSON_ACTION, "[1;2]"));
        assertEquals(List.of(), handler.send(JSON_ACTION, "[true]"));
        assertEquals(List.of(), handler.send(JSON_ACTION, "[null]"));
        assertEquals(List.of(), handler.send(JSON_ACTION, "[[1]]"));
        assertEquals(List.of(), handler.send(JSON_ACTION, "[{\"a\": 1}]"));
    }

    @Test
    public void jsonIgnoresNonTextCommand() {
        assertEquals(List.of(), handler.send(JSON_ACTION, new DecimalType(5)));
        assertEquals(List.of(), handler.send(JSON_ACTION, new QuantityType<>(180, SIUnits.CELSIUS)));
    }

    @Test
    public void jsonIsNotSentForLegacyCommand() {
        assertEquals(List.of(), handler.send("[{\"command\": \"set_x\", \"parameterType\": \"JSON\"}]", "[1, 2]"));
    }

    @Test
    public void databaseEnumerationsAreSentInLowerCase() throws IOException {
        assertEquals(List.of("set_mode[\"silent\"]"),
                handler.sendToDatabaseChannel("zhimi.humidifier.v1.json", "mode", "Silent"));
        assertEquals(List.of("set_doorbell_push[\"on\"]"),
                handler.sendToDatabaseChannel("lumi.gateway.json", "doorbellPush", "ON"));
    }

    @Test
    public void databaseStartCookSendsActionInput() throws IOException {
        assertEquals(List.of("action{\"did\":\"start_cook\",\"siid\":3,\"aiid\":1,\"in\":[\"M0\",15,180,0,0,1]}"),
                handler.sendToDatabaseChannel("careli.fryer.maf02-miot.json", "start_cook", "[\"M0\",15,180,0,0,1]"));
        assertEquals(List.of(
                "action{\"did\":\"start_cook\",\"siid\":3,\"aiid\":1,\"in\":[\"M0\",\"u0064u0065u006du006f\",15,180,0,0,1]}"),
                handler.sendToDatabaseChannel("careli.fryer.maf05a-miot.json", "start_cook",
                        "[\"M0\",\"u0064u0065u006du006f\",15,180,0,0,1]"));
    }

    @Test
    public void databaseFreeTextIsSentAsTyped() throws IOException {
        assertEquals(List.of("set_name[\"Living Room\"]"),
                handler.sendToDatabaseChannel("yeelink.light.lamp1.json", "name", "Living Room"));
    }
}
