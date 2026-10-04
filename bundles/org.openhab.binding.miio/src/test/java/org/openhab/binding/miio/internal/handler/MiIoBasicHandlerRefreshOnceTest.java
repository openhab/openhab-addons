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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Tests the polling of channels with a custom refresh command and a negative refreshInterval (read once).
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class MiIoBasicHandlerRefreshOnceTest {

    private static final String CHANNEL_JSON = "{\"deviceMapping\":{\"id\":[\"test.model\"],\"propertyMethod\":\"get_prop\",\"channels\":[%s]}}";
    private static final String ONCE_CHANNEL = "{\"property\":\"\",\"channel\":\"once\",\"type\":\"String\",\"refresh\":true,\"refreshInterval\":-1,"
            + "\"customRefreshCommand\":\"/v2/test/query\",\"customRefreshParameters\":{\"model\":\"test.model\"}}";
    private static final String ONCE_TRANSFORMED_CHANNEL = "{\"property\":\"\",\"channel\":\"once\",\"type\":\"String\",\"refresh\":true,\"refreshInterval\":-1,"
            + "\"customRefreshCommand\":\"/v2/test/query\",\"transformation\":\"getJsonElement-recipes\"}";
    private static final String ONCE_PROPERTY = "{\"property\":\"power\",\"channel\":\"power\",\"type\":\"String\",\"refresh\":true,\"refreshInterval\":-1}";
    private static final String ONCE_MIOT_PROPERTY = "{\"property\":\"pwr\",\"siid\":2,\"piid\":1,\"channel\":\"power\",\"type\":\"String\",\"refresh\":true,\"refreshInterval\":-1}";
    private static final String ONCE_NUMBER_CHANNEL = "{\"property\":\"\",\"channel\":\"num\",\"type\":\"Number\",\"refresh\":true,\"refreshInterval\":-1,"
            + "\"customRefreshCommand\":\"/v2/test/number\"}";
    private static final String EVERY_PROPERTY = "{\"property\":\"mode\",\"channel\":\"mode\",\"type\":\"String\",\"refresh\":true}";
    private static final String EVERY_CYCLE_CHANNEL = "{\"property\":\"\",\"channel\":\"always\",\"type\":\"String\",\"refresh\":true,"
            + "\"customRefreshCommand\":\"/v2/test/always\"}";
    private static final String EVERY_SECOND_CHANNEL = "{\"property\":\"\",\"channel\":\"second\",\"type\":\"String\",\"refresh\":true,\"refreshInterval\":2,"
            + "\"customRefreshCommand\":\"/v2/test/second\"}";

    private @Mock @NonNullByDefault({}) Thing thing;
    private @Mock @NonNullByDefault({}) ThingHandlerCallback callback;
    private @Mock @NonNullByDefault({}) MiIoDatabaseWatchService miIoDatabaseWatchService;
    private @Mock @NonNullByDefault({}) CloudConnector cloudConnector;
    private @Mock @NonNullByDefault({}) ChannelTypeRegistry channelTypeRegistry;
    private @Mock @NonNullByDefault({}) BasicChannelTypeProvider basicChannelTypeProvider;
    private @Mock @NonNullByDefault({}) TranslationProvider translationProvider;
    private @Mock @NonNullByDefault({}) LocaleProvider localeProvider;

    private final ThingUID thingUID = new ThingUID(MiIoBindingConstants.THING_TYPE_BASIC, "TestThing");
    private @NonNullByDefault({}) CapturingHandler handler;
    private @NonNullByDefault({}) MiIoBasicDevice device;

    private class CapturingHandler extends MiIoBasicHandler {
        private final List<String> sentCommands = new ArrayList<>();
        private final List<JsonObject> sentJson = new ArrayList<>();
        private final Semaphore updates = new Semaphore(0);
        private int lastId = 0;

        CapturingHandler(Thing thing, MiIoDatabaseWatchService miIoDatabaseWatchService, CloudConnector cloudConnector,
                ChannelTypeRegistry channelTypeRegistry, BasicChannelTypeProvider basicChannelTypeProvider,
                TranslationProvider translationProvider, LocaleProvider localeProvider) {
            super(thing, miIoDatabaseWatchService, cloudConnector, channelTypeRegistry, basicChannelTypeProvider,
                    translationProvider, localeProvider);
        }

        /** Runs one polling cycle and returns the commands that were sent */
        List<String> poll(MiIoBasicDevice device) {
            sentCommands.clear();
            refreshCustomProperties(device, true);
            return new ArrayList<>(sentCommands);
        }

        /** Runs one polling cycle for the regular properties and returns the property requests that were sent */
        List<String> pollProperties(MiIoBasicDevice device) {
            sentCommands.clear();
            sentJson.clear();
            refreshProperties(device, "");
            List<String> sent = new ArrayList<>();
            sentJson.forEach(j -> sent.add(j.get("params").toString()));
            return sent;
        }

        /** Delivers the response for the last property request */
        void respondProperties(String result) {
            JsonObject request = sentJson.get(sentJson.size() - 1);
            MiIoSendCommand command = new MiIoSendCommand(request.get("id").getAsInt(), MiIoCommand.GET_PROPERTY,
                    request, "", "");
            command.setResponse(JsonParser.parseString("{\"id\":" + request.get("id") + ",\"result\":" + result + "}")
                    .getAsJsonObject());
            onMessageReceived(command);
        }

        /** Delivers the response for the last command sent for the channel */
        void respond(String channel, String response) {
            int id = cmds.entrySet().stream().filter(e -> channel.equals(e.getValue())).map(e -> e.getKey()).findFirst()
                    .orElseThrow();
            JsonObject json = JsonParser.parseString(response).getAsJsonObject();
            MiIoSendCommand command = new MiIoSendCommand(id, MiIoCommand.UNKNOWN, new JsonObject(), "de",
                    thingUID.getAsString());
            command.setResponse(json);
            onMessageReceived(command);
        }

        /** Waits for an update of the device data to be started, as done by the polling job or a refresh */
        boolean awaitUpdate() throws InterruptedException {
            return updates.tryAcquire(30, TimeUnit.SECONDS);
        }

        @Override
        protected synchronized void updateData() {
            updates.release();
        }

        @Override
        protected int sendCommand(String command, String params, String cloudServer, String sender) {
            sentCommands.add(command);
            JsonObject json = new JsonObject();
            json.addProperty("id", ++lastId);
            json.addProperty("method", command);
            json.add("params", JsonParser.parseString(params));
            sentJson.add(json);
            return lastId;
        }
    }

    @BeforeEach
    public void setUp() {
        when(thing.getUID()).thenReturn(thingUID);
        when(callback.isChannelLinked(any(ChannelUID.class))).thenReturn(true);
        handler = new CapturingHandler(thing, miIoDatabaseWatchService, cloudConnector, channelTypeRegistry,
                basicChannelTypeProvider, translationProvider, localeProvider);
        handler.setCallback(callback);
        handler.cloudServer = "de";
    }

    private void load(String... channels) {
        device = new Gson().fromJson(String.format(CHANNEL_JSON, String.join(",", channels)), MiIoBasicDevice.class);
        handler.refreshListCustomCommands.clear();
        handler.refreshList.clear();
        for (MiIoBasicChannel channel : device.getDevice().getChannels()) {
            if (channel.getChannelCustomRefreshCommand().isBlank()) {
                handler.refreshList.add(channel);
            } else {
                handler.refreshListCustomCommands.put(channel.getChannelCustomRefreshCommand(), channel);
            }
        }
    }

    @Test
    public void readOnceChannelIsNotRequestedAgainAfterSuccess() {
        load(ONCE_CHANNEL);

        assertEquals(List.of("/v2/test/query"), handler.poll(device));
        handler.respond("once", "{\"code\":0,\"result\":{\"recipes\":[]}}");
        assertEquals(List.of(), handler.poll(device));
        assertEquals(List.of(), handler.poll(device));
    }

    @Test
    public void readOnceChannelIsRetriedUntilSuccessful() {
        load(ONCE_CHANNEL);

        assertEquals(List.of("/v2/test/query"), handler.poll(device));
        // transport error
        handler.respond("once", "{\"error\":\"timeout\"}");
        assertEquals(List.of("/v2/test/query"), handler.poll(device));
        // cloud error without result
        handler.respond("once", "{\"code\":-8,\"message\":\"auth err\"}");
        assertEquals(List.of("/v2/test/query"), handler.poll(device));
        handler.respond("once", "{\"code\":0,\"result\":{}}");
        assertEquals(List.of(), handler.poll(device));
    }

    @Test
    public void readOnceChannelIsNotRequestedWhileNotLinked() {
        load(ONCE_CHANNEL);
        when(callback.isChannelLinked(any(ChannelUID.class))).thenReturn(false);

        assertEquals(List.of(), handler.poll(device));
        assertEquals(List.of(), handler.poll(device));
        when(callback.isChannelLinked(any(ChannelUID.class))).thenReturn(true);
        assertEquals(List.of("/v2/test/query"), handler.poll(device));
    }

    @Test
    public void refreshCommandReadsOnceChannelAgain() {
        load(ONCE_CHANNEL);

        handler.poll(device);
        handler.respond("once", "{\"code\":0,\"result\":{}}");
        assertEquals(List.of(), handler.poll(device));

        handler.handleCommand(new ChannelUID(thingUID, "once"), RefreshType.REFRESH);
        assertEquals(List.of("/v2/test/query"), handler.poll(device));
        handler.respond("once", "{\"code\":0,\"result\":{}}");
        assertEquals(List.of(), handler.poll(device));
    }

    @Test
    public void refreshOfOtherChannelDoesNotReadOnceChannelAgain() {
        load(ONCE_CHANNEL, EVERY_CYCLE_CHANNEL);

        handler.poll(device);
        handler.respond("once", "{\"code\":0,\"result\":{}}");
        handler.handleCommand(new ChannelUID(thingUID, "always"), RefreshType.REFRESH);
        assertEquals(List.of("/v2/test/always"), handler.poll(device));
    }

    @Test
    public void otherRefreshIntervalsAreUnchanged() {
        load(EVERY_CYCLE_CHANNEL, EVERY_SECOND_CHANNEL);

        for (int cycle = 0; cycle < 4; cycle++) {
            List<String> sent = handler.poll(device);
            assertTrue(sent.contains("/v2/test/always"), "cycle " + cycle);
            assertEquals(cycle % 2 == 0, sent.contains("/v2/test/second"), "cycle " + cycle);
        }
    }

    @Test
    public void readOncePropertyIsLeftOutOfLaterRequests() {
        load(ONCE_PROPERTY, EVERY_PROPERTY);

        assertEquals(List.of("[\"power\",\"mode\"]"), handler.pollProperties(device));
        handler.respondProperties("[\"on\",\"auto\"]");
        assertEquals(List.of("[\"mode\"]"), handler.pollProperties(device));
        handler.respondProperties("[\"auto\"]");
        assertEquals(List.of("[\"mode\"]"), handler.pollProperties(device));
    }

    @Test
    public void readOncePropertyIsRetriedUntilValueReceived() {
        load(ONCE_PROPERTY, EVERY_PROPERTY);

        handler.pollProperties(device);
        // property not supported / no value yet
        handler.respondProperties("[null,\"auto\"]");
        assertEquals(List.of("[\"power\",\"mode\"]"), handler.pollProperties(device));
        // response of unexpected size is ignored
        handler.respondProperties("[\"on\"]");
        assertEquals(List.of("[\"power\",\"mode\"]"), handler.pollProperties(device));
        handler.respondProperties("[\"on\",\"auto\"]");
        assertEquals(List.of("[\"mode\"]"), handler.pollProperties(device));
    }

    @Test
    public void readOnceMiotPropertyWithoutValueIsRetried() {
        load(ONCE_MIOT_PROPERTY);

        assertEquals(1, handler.pollProperties(device).size());
        handler.respondProperties("[{\"did\":\"pwr\",\"siid\":2,\"piid\":1,\"code\":-4004}]");
        assertEquals(1, handler.pollProperties(device).size());
        handler.respondProperties("[{\"did\":\"pwr\",\"siid\":2,\"piid\":1,\"code\":0,\"value\":\"on\"}]");
        assertEquals(0, handler.pollProperties(device).size());
    }

    @Test
    public void readOnceChannelIsRetriedWhenUpdateFails() {
        load(ONCE_NUMBER_CHANNEL);

        assertEquals(List.of("/v2/test/number"), handler.poll(device));
        handler.respond("num", "{\"code\":0,\"result\":[\"not a number\"]}");
        assertEquals(List.of("/v2/test/number"), handler.poll(device));
        handler.respond("num", "{\"code\":0,\"result\":[5]}");
        assertEquals(List.of(), handler.poll(device));
    }

    @Test
    public void errorResponseRemovesPendingCommand() {
        load(ONCE_CHANNEL);

        handler.poll(device);
        assertEquals(1, handler.cmds.size());
        handler.respond("once", "{\"error\":\"timeout\"}");
        assertTrue(handler.cmds.isEmpty());
        handler.poll(device);
        handler.respond("once", "{\"code\":-8,\"message\":\"auth err\"}");
        assertTrue(handler.cmds.isEmpty());
    }

    @Test
    public void refreshCommandReadsOncePropertyAgain() {
        load(ONCE_PROPERTY, EVERY_PROPERTY);

        handler.pollProperties(device);
        handler.respondProperties("[\"on\",\"auto\"]");
        handler.handleCommand(new ChannelUID(thingUID, "power"), RefreshType.REFRESH);
        assertEquals(List.of("[\"power\",\"mode\"]"), handler.pollProperties(device));
    }

    @Test
    public void refreshOfReadOnceChannelStartsUpdateWhileCacheIsValid() throws InterruptedException {
        load(ONCE_CHANNEL, EVERY_CYCLE_CHANNEL);

        handler.poll(device);
        handler.respond("once", "{\"code\":0,\"result\":{}}");
        // this refresh fills the cache, as the polling job is not needed for it (refreshInterval=0)
        handler.handleCommand(new ChannelUID(thingUID, "always"), RefreshType.REFRESH);
        assertTrue(handler.awaitUpdate());

        handler.handleCommand(new ChannelUID(thingUID, "once"), RefreshType.REFRESH);
        assertTrue(handler.awaitUpdate());
        assertTrue(handler.poll(device).contains("/v2/test/query"));
    }

    @Test
    public void refreshOfReadOnceChannelThatWasNotReadYetStartsUpdateWhileCacheIsValid() throws InterruptedException {
        load(ONCE_CHANNEL, EVERY_CYCLE_CHANNEL);

        handler.poll(device);
        handler.respond("once", "{\"error\":\"timeout\"}");
        handler.handleCommand(new ChannelUID(thingUID, "always"), RefreshType.REFRESH);
        assertTrue(handler.awaitUpdate());

        handler.handleCommand(new ChannelUID(thingUID, "once"), RefreshType.REFRESH);
        assertTrue(handler.awaitUpdate());
        assertTrue(handler.poll(device).contains("/v2/test/query"));
    }

    @Test
    public void readOnceTransformedChannelWithNullResultIsRetried() {
        load(ONCE_TRANSFORMED_CHANNEL);

        assertEquals(List.of("/v2/test/query"), handler.poll(device));
        handler.respond("once", "{\"code\":0,\"result\":[null]}");
        assertEquals(List.of("/v2/test/query"), handler.poll(device));
        handler.respond("once", "{\"code\":0,\"result\":null}");
        assertEquals(List.of("/v2/test/query"), handler.poll(device));
        verify(callback, never()).stateUpdated(any(ChannelUID.class), any(State.class));
        handler.respond("once", "{\"code\":0,\"result\":[{\"recipes\":\"Fries\"}]}");
        assertEquals(List.of(), handler.poll(device));
    }

    @Test
    public void readOnceMiotPropertyWithNullValueIsRetried() {
        load(ONCE_MIOT_PROPERTY);

        assertEquals(1, handler.pollProperties(device).size());
        handler.respondProperties("[{\"did\":\"pwr\",\"siid\":2,\"piid\":1,\"code\":0,\"value\":null}]");
        assertEquals(1, handler.pollProperties(device).size());
        verify(callback, never()).stateUpdated(any(ChannelUID.class), any(State.class));
        handler.respondProperties("[{\"did\":\"pwr\",\"siid\":2,\"piid\":1,\"code\":0,\"value\":\"on\"}]");
        assertEquals(0, handler.pollProperties(device).size());
        verify(callback).stateUpdated(new ChannelUID(thingUID, "power"), new StringType("on"));
    }

    @Test
    public void readOnceChannelWithNullResultIsRetried() {
        load(ONCE_CHANNEL);

        assertEquals(List.of("/v2/test/query"), handler.poll(device));
        handler.respond("once", "{\"code\":0,\"result\":null}");
        assertEquals(List.of("/v2/test/query"), handler.poll(device));
        handler.respond("once", "{\"code\":0,\"result\":[null]}");
        assertEquals(List.of("/v2/test/query"), handler.poll(device));
        verify(callback, never()).stateUpdated(any(ChannelUID.class), any(State.class));
        handler.respond("once", "{\"code\":0,\"result\":{}}");
        assertEquals(List.of(), handler.poll(device));
    }
}
