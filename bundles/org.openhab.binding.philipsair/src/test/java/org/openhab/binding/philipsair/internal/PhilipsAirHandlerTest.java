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
package org.openhab.binding.philipsair.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.philipsair.internal.connection.PhilipsAirCipher;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierWritableDataDTO;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.test.java.JavaTest;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;

import com.google.gson.Gson;

/**
 * Test cases for {@link PhilipsAirHandler} using the encrypted HTTP protocol. The HTTP client is mocked, the
 * encryption, parsing and channel updates are exercised for real.
 *
 * @author michalboronski - Initial contribution
 * @author Marcel Verpaalen - Re-enable tests for current handler behavior
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PhilipsAirHandlerTest extends JavaTest {

    private static final String FAKE_KEY = "1F09722BE668AF0B8DC78051B70E4F76";
    private static final ThingUID THING_UID = new ThingUID(THING_TYPE_AC2889_10, "1");

    private static final String DEVICE = "{\"name\":\"Philips\",\"type\":\"AC2889\",\"modelid\":\"AC2889/10\",\"swversion\":\"1.0.4\"}";
    private static final String STATUS = "{\"om\":\"s\",\"pwr\":\"1\",\"cl\":false,\"aqil\":75,\"uil\":\"1\",\"dt\":0,\"dtrs\":0,\"mode\":\"P\",\"func\":\"PH\",\"rhset\":40,\"rh\":56,\"temp\":21,\"pm25\":8,\"iaql\":2,\"aqit\":4,\"ddp\":\"1\",\"err\":0,\"wl\":0}";
    private static final String STATUS_PWR_OFF = "{\"om\":\"0\",\"pwr\":\"0\",\"cl\":false,\"aqil\":75,\"uil\":\"1\",\"dt\":0,\"dtrs\":0,\"mode\":\"P\",\"func\":\"PH\",\"rhset\":40,\"rh\":56,\"temp\":21,\"pm25\":2,\"iaql\":1,\"aqit\":4,\"ddp\":\"1\",\"err\":0,\"wl\":0}";
    private static final String STATUS_UIL_OFF = "{\"om\":\"s\",\"pwr\":\"1\",\"cl\":false,\"aqil\":75,\"uil\":\"0\",\"dt\":0,\"dtrs\":0,\"mode\":\"P\",\"func\":\"PH\",\"rhset\":40,\"rh\":56,\"temp\":21,\"pm25\":6,\"iaql\":2,\"aqit\":4,\"ddp\":\"1\",\"err\":0,\"wl\":0}";
    private static final String STATUS_DDP_0 = "{\"om\":\"s\",\"pwr\":\"1\",\"cl\":false,\"aqil\":75,\"uil\":\"0\",\"dt\":0,\"dtrs\":0,\"mode\":\"P\",\"func\":\"PH\",\"rhset\":40,\"rh\":56,\"temp\":21,\"pm25\":9,\"iaql\":3,\"aqit\":4,\"ddp\":\"0\",\"err\":0,\"wl\":0}";
    private static final String STATUS_OM_1 = "{\"om\":\"1\",\"pwr\":\"1\",\"cl\":false,\"aqil\":75,\"uil\":\"1\",\"dt\":0,\"dtrs\":0,\"mode\":\"M\",\"func\":\"PH\",\"rhset\":40,\"rh\":56,\"temp\":21,\"pm25\":18,\"iaql\":4,\"aqit\":4,\"ddp\":\"1\",\"err\":0,\"wl\":0}";
    private static final String STATUS_AQIL_25 = "{\"om\":\"1\",\"pwr\":\"1\",\"cl\":false,\"aqil\":25,\"uil\":\"1\",\"dt\":0,\"dtrs\":0,\"mode\":\"P\",\"func\":\"PH\",\"rhset\":40,\"rh\":56,\"temp\":21,\"pm25\":13,\"iaql\":3,\"aqit\":4,\"ddp\":\"1\",\"err\":0,\"wl\":0}";
    private static final String STATUS_DT_1 = "{\"om\":\"s\",\"pwr\":\"1\",\"cl\":false,\"aqil\":25,\"uil\":\"1\",\"dt\":1,\"dtrs\":60,\"mode\":\"P\",\"func\":\"PH\",\"rhset\":40,\"rh\":56,\"temp\":21,\"pm25\":11,\"iaql\":3,\"aqit\":4,\"ddp\":\"1\",\"err\":0,\"wl\":0}";
    private static final String STATUS_MODE_A = "{\"om\":\"1\",\"pwr\":\"1\",\"cl\":false,\"aqil\":25,\"uil\":\"1\",\"dt\":1,\"dtrs\":57,\"mode\":\"A\",\"func\":\"PH\",\"rhset\":40,\"rh\":56,\"temp\":21,\"pm25\":7,\"iaql\":2,\"aqit\":4,\"ddp\":\"1\",\"err\":0,\"wl\":0}";
    private static final String STATUS_CL_ON = "{\"om\":\"1\",\"pwr\":\"1\",\"cl\":true,\"aqil\":25,\"uil\":\"1\",\"dt\":1,\"dtrs\":57,\"mode\":\"A\",\"func\":\"PH\",\"rhset\":40,\"rh\":56,\"temp\":21,\"pm25\":7,\"iaql\":2,\"aqit\":4,\"ddp\":\"1\",\"err\":0,\"wl\":0}";

    private static final List<String> ALL_CHANNELS = List.of("controls#pwr:Switch", "controls#om:String",
            "controls#cl:Switch", "controls#mode:String", "controls#dt:Number", "controls#dtrs:Number",
            "controls#rhset:Number", "controls#func:String", "controls-ui#uil:Switch", "controls-ui#aqil:Number",
            "controls-ui#ddp:String", "sensors#pm25:Number:Density", "sensors#iaql:Number", "sensors#aqit:Number",
            "sensors#err:String", "sensors#rh:Number:Dimensionless", "sensors#temp:Number:Temperature",
            "sensors#wl:Number");

    private static final Gson GSON = new Gson();
    private final PhilipsAirCipher cipher;

    private @Mock @NonNullByDefault({}) ThingHandlerCallback callback;
    private @Mock @NonNullByDefault({}) HttpClient httpClient;
    private @Mock @NonNullByDefault({}) Request request;
    private @Mock @NonNullByDefault({}) ContentResponse response;

    private @Nullable PhilipsAirHandler handler;

    public PhilipsAirHandlerTest() throws Exception {
        cipher = new PhilipsAirCipher();
        cipher.initKey(FAKE_KEY);
    }

    @BeforeEach
    public void setUp() throws Exception {
        when(httpClient.newRequest(anyString())).thenReturn(request);
        when(request.method(any(HttpMethod.class))).thenReturn(request);
        when(request.content(any(ContentProvider.class))).thenReturn(request);
        when(request.timeout(anyLong(), any(TimeUnit.class))).thenReturn(request);
        when(request.send()).thenReturn(response);
        when(response.getStatus()).thenReturn(200);
        when(callback.createChannelBuilder(any(ChannelUID.class), any()))
                .thenAnswer(invocation -> ChannelBuilder.create((ChannelUID) invocation.getArgument(0), "Number"));
    }

    @AfterEach
    public void tearDown() {
        PhilipsAirHandler handler = this.handler;
        if (handler != null) {
            handler.dispose();
        }
    }

    private PhilipsAirHandler createHandler(List<String> channels, Configuration extraConfig) {
        Configuration config = new Configuration();
        config.put(PhilipsAirConfiguration.CONFIG_DEF_REFRESH_INTERVAL, 5);
        config.put(PhilipsAirConfiguration.CONFIG_HOST, "1.1.1.1");
        config.put(PhilipsAirConfiguration.CONFIG_KEY, FAKE_KEY);
        extraConfig.getProperties().forEach(config::put);

        ThingBuilder thingBuilder = ThingBuilder.create(THING_TYPE_AC2889_10, THING_UID).withConfiguration(config);
        for (String channel : channels) {
            String[] parts = channel.split(":", 2);
            ChannelUID channelUID = new ChannelUID(THING_UID, parts[0]);
            thingBuilder.withChannel(ChannelBuilder.create(channelUID, parts[1]).build());
            when(callback.isChannelLinked(channelUID)).thenReturn(true);
        }
        PhilipsAirHandler handler = new PhilipsAirHandler(thingBuilder.build(), httpClient);
        handler.setCallback(callback);
        this.handler = handler;
        return handler;
    }

    /**
     * Mocks the encrypted device responses. The handler requests the device info first, then the status and
     * finally (only for commands) the command response.
     */
    private void mockResponses(String... responses) throws Exception {
        String first = cipher.encrypt(responses[0]);
        String[] next = new String[responses.length - 1];
        for (int i = 1; i < responses.length; i++) {
            next[i - 1] = cipher.encrypt(responses[i]);
        }
        when(response.getContentAsString()).thenReturn(first, next);
    }

    /**
     * Initializes the handler and waits until the asynchronously created connection has delivered the first data.
     */
    private void initializeAndRefresh(PhilipsAirHandler handler, String channel) {
        ChannelUID channelUID = new ChannelUID(THING_UID, channel);
        handler.initialize();
        waitForAssert(() -> {
            handler.handleCommand(channelUID, RefreshType.REFRESH);
            verify(callback, atLeastOnce()).stateUpdated(eq(channelUID), any());
        }, 10000, 100);
    }

    private State lastState(String channel) {
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(callback, atLeastOnce()).stateUpdated(eq(new ChannelUID(THING_UID, channel)), stateCaptor.capture());
        List<State> states = stateCaptor.getAllValues();
        return states.get(states.size() - 1);
    }

    @Test
    public void initializeGoesOnlineAfterFirstData() throws Exception {
        mockResponses(DEVICE, STATUS);
        PhilipsAirHandler handler = createHandler(List.of("controls#pwr:Switch"), new Configuration());

        initializeAndRefresh(handler, "controls#pwr");

        ArgumentCaptor<ThingStatusInfo> statusInfoCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback, atLeast(2)).statusUpdated(any(Thing.class), statusInfoCaptor.capture());
        List<ThingStatusInfo> statusInfos = statusInfoCaptor.getAllValues();
        assertEquals(ThingStatus.OFFLINE, statusInfos.get(0).getStatus());
        assertEquals(ThingStatus.ONLINE, statusInfos.get(statusInfos.size() - 1).getStatus());
    }

    @Test
    public void stateUpdatesForAllChannels() throws Exception {
        mockResponses(DEVICE, STATUS);
        PhilipsAirHandler handler = createHandler(ALL_CHANNELS, new Configuration());

        initializeAndRefresh(handler, "controls#pwr");

        assertEquals(OnOffType.ON, lastState("controls#pwr"));
        assertEquals(new StringType("s"), lastState("controls#om"));
        assertEquals(OnOffType.OFF, lastState("controls#cl"));
        assertEquals(new StringType("P"), lastState("controls#mode"));
        assertEquals(new DecimalType(0), lastState("controls#dt"));
        assertEquals(new DecimalType(0), lastState("controls#dtrs"));
        assertEquals(new DecimalType(40), lastState("controls#rhset"));
        assertEquals(new StringType("PH"), lastState("controls#func"));
        assertEquals(OnOffType.ON, lastState("controls-ui#uil"));
        assertEquals(new DecimalType(75), lastState("controls-ui#aqil"));
        assertEquals(new StringType("1"), lastState("controls-ui#ddp"));
        assertEquals(new QuantityType<>(8, DENSITY_UNIT), lastState("sensors#pm25"));
        assertEquals(new DecimalType(2), lastState("sensors#iaql"));
        assertEquals(new DecimalType(4), lastState("sensors#aqit"));
        assertEquals(new StringType("0"), lastState("sensors#err"));
        assertEquals(56, ((QuantityType<?>) lastState("sensors#rh")).intValue());
        assertEquals(21, ((QuantityType<?>) lastState("sensors#temp")).intValue());
        assertEquals(new DecimalType(0), lastState("sensors#wl"));
    }

    @Test
    public void stateUpdatesApplyOffsets() throws Exception {
        mockResponses(DEVICE, STATUS);
        Configuration offsets = new Configuration();
        offsets.put(PhilipsAirConfiguration.CONFIG_DEF_TEMPERATURE_OFFSET, 1.0);
        offsets.put(PhilipsAirConfiguration.CONFIG_DEF_HUMIDITY_OFFSET, -1.0);
        PhilipsAirHandler handler = createHandler(
                List.of("sensors#rh:Number:Dimensionless", "sensors#temp:Number:Temperature"), offsets);

        initializeAndRefresh(handler, "sensors#rh");

        QuantityType<?> humidity = (QuantityType<?>) lastState("sensors#rh");
        assertEquals(55f, humidity.floatValue());
        assertEquals(HUMIDITY_UNIT, humidity.getUnit());
        QuantityType<?> temperature = (QuantityType<?>) lastState("sensors#temp");
        assertEquals(22f, temperature.floatValue());
        assertEquals(TEMPERATURE_UNIT, temperature.getUnit());
    }

    private void sendCommandTemplate(String channel, Command command, State stateBefore, State stateAfter,
            String statusBefore, String commandResponse) throws Exception {
        mockResponses(DEVICE, statusBefore, commandResponse);
        String channelId = channel.split(":", 2)[0];
        PhilipsAirHandler handler = createHandler(List.of(channel), new Configuration());

        initializeAndRefresh(handler, channelId);
        assertEquals(stateBefore, lastState(channelId));

        handler.handleCommand(new ChannelUID(THING_UID, channelId), command);

        assertEquals(stateAfter, lastState(channelId));
        verify(request).method(HttpMethod.PUT);
    }

    @Test
    public void testSendCommandPWR() throws Exception {
        sendCommandTemplate("controls#pwr:Switch", OnOffType.OFF, OnOffType.ON, OnOffType.OFF, STATUS, STATUS_PWR_OFF);
    }

    @Test
    public void testSendCommandUIL() throws Exception {
        sendCommandTemplate("controls-ui#uil:Switch", OnOffType.OFF, OnOffType.ON, OnOffType.OFF, STATUS,
                STATUS_UIL_OFF);
    }

    @Test
    public void testSendCommandDDP() throws Exception {
        sendCommandTemplate("controls-ui#ddp:String", new StringType("0"), new StringType("1"), new StringType("0"),
                STATUS, STATUS_DDP_0);
    }

    @Test
    public void testSendCommandOM1() throws Exception {
        sendCommandTemplate("controls#om:String", new StringType("1"), new StringType("s"), new StringType("1"), STATUS,
                STATUS_OM_1);
    }

    @Test
    public void testSendCommandOMs() throws Exception {
        sendCommandTemplate("controls#om:String", new StringType("s"), new StringType("1"), new StringType("s"),
                STATUS_OM_1, STATUS);
    }

    @Test
    public void testSendCommandAqil() throws Exception {
        sendCommandTemplate("controls-ui#aqil:Number", new DecimalType(25), new DecimalType(75), new DecimalType(25),
                STATUS, STATUS_AQIL_25);
    }

    @Test
    public void testSendCommandDt() throws Exception {
        sendCommandTemplate("controls#dt:Number", new DecimalType(1), new DecimalType(0), new DecimalType(1), STATUS,
                STATUS_DT_1);
    }

    @Test
    public void testSendCommandMode() throws Exception {
        sendCommandTemplate("controls#mode:String", new StringType("A"), new StringType("P"), new StringType("A"),
                STATUS, STATUS_MODE_A);
    }

    @Test
    public void testSendCommandCL() throws Exception {
        sendCommandTemplate("controls#cl:Switch", OnOffType.ON, OnOffType.OFF, OnOffType.ON, STATUS, STATUS_CL_ON);
    }

    @Test
    public void testPrepareCommands() {
        PhilipsAirHandler handler = createHandler(List.of(), new Configuration());

        // testing not obvious (non intuitive types) command conversions
        PhilipsAirPurifierWritableDataDTO commandDto = handler.prepareCommandData("ddp", StringType.valueOf("0"));
        assertEquals("{\"ddp\":\"0\"}", GSON.toJson(commandDto));

        commandDto = handler.prepareCommandData("uil", OnOffType.ON);
        assertEquals("{\"uil\":\"1\"}", GSON.toJson(commandDto));

        commandDto = handler.prepareCommandData("pwr", OnOffType.ON);
        assertEquals("{\"pwr\":\"1\"}", GSON.toJson(commandDto));

        commandDto = handler.prepareCommandData("aqil", DecimalType.valueOf("25"));
        assertEquals("{\"aqil\":25}", GSON.toJson(commandDto));

        commandDto = handler.prepareCommandData("cl", OnOffType.ON);
        assertEquals("{\"cl\":true}", GSON.toJson(commandDto));

        commandDto = handler.prepareCommandData("om", StringType.valueOf("2"));
        assertEquals("{\"om\":\"2\",\"mode\":\"M\"}", GSON.toJson(commandDto));

        commandDto = handler.prepareCommandData("rhset", DecimalType.valueOf("50"));
        assertEquals("{\"rhset\":50}", GSON.toJson(commandDto));
    }
}
