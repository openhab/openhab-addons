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
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierDataDTO;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierFiltersDTO;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierWritableDataDTO;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
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
 * @author Michał Boroński - Initial contribution
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

    private static final List<String> ALL_CHANNELS = List.of("controls#power:Switch", "controls#fan-speed:String",
            "controls#child-lock:Switch", "controls#mode:String", "controls#timer:Number",
            "controls#timer-remaining:Number", "controls#target-humidity:Number", "controls#function:String",
            "controls-ui#button-light:Switch", "controls-ui#light-level:Number:Dimensionless",
            "controls-ui#displayed-index:String", "sensors#pm25:Number:Density", "sensors#allergen-index:Number",
            "sensors#air-quality-threshold:Number", "sensors#error-code:String",
            "sensors#humidity:Number:Dimensionless", "sensors#temperature:Number:Temperature",
            "sensors#water-level:Number:Dimensionless");

    private static final Gson GSON = new Gson();
    private final PhilipsAirCipher cipher;

    private @Mock @NonNullByDefault({}) ThingHandlerCallback callback;
    private @Mock @NonNullByDefault({}) HttpClient httpClient;
    private @Mock @NonNullByDefault({}) PhilipsAirStateDescriptionOptionProvider stateDescriptionProvider;
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
        PhilipsAirHandler handler = new PhilipsAirHandler(thingBuilder.build(), httpClient, stateDescriptionProvider);
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
        PhilipsAirHandler handler = createHandler(List.of("controls#power:Switch"), new Configuration());

        initializeAndRefresh(handler, "controls#power");

        ArgumentCaptor<ThingStatusInfo> statusInfoCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback, atLeast(2)).statusUpdated(any(Thing.class), statusInfoCaptor.capture());
        List<ThingStatusInfo> statusInfos = statusInfoCaptor.getAllValues();
        assertEquals(ThingStatus.UNKNOWN, statusInfos.get(0).getStatus());
        assertEquals(ThingStatus.ONLINE, statusInfos.get(statusInfos.size() - 1).getStatus());
    }

    @Test
    public void stateUpdatesForAllChannels() throws Exception {
        mockResponses(DEVICE, STATUS);
        PhilipsAirHandler handler = createHandler(ALL_CHANNELS, new Configuration());

        initializeAndRefresh(handler, "controls#power");

        assertEquals(OnOffType.ON, lastState("controls#power"));
        assertEquals(new StringType("s"), lastState("controls#fan-speed"));
        assertEquals(OnOffType.OFF, lastState("controls#child-lock"));
        assertEquals(new StringType("P"), lastState("controls#mode"));
        assertEquals(new DecimalType(0), lastState("controls#timer"));
        assertEquals(new QuantityType<>(0, Units.MINUTE), lastState("controls#timer-remaining"));
        assertEquals(new DecimalType(40), lastState("controls#target-humidity"));
        assertEquals(new StringType("PH"), lastState("controls#function"));
        assertEquals(OnOffType.ON, lastState("controls-ui#button-light"));
        assertEquals(new QuantityType<>(75, Units.PERCENT), lastState("controls-ui#light-level"));
        assertEquals(new StringType("1"), lastState("controls-ui#displayed-index"));
        assertEquals(new QuantityType<>(8, DENSITY_UNIT), lastState("sensors#pm25"));
        assertEquals(new DecimalType(2), lastState("sensors#allergen-index"));
        assertEquals(new DecimalType(4), lastState("sensors#air-quality-threshold"));
        assertEquals(new StringType("0"), lastState("sensors#error-code"));
        assertEquals(56, ((QuantityType<?>) lastState("sensors#humidity")).intValue());
        assertEquals(21, ((QuantityType<?>) lastState("sensors#temperature")).intValue());
        assertEquals(new QuantityType<>(0, Units.PERCENT), lastState("sensors#water-level"));
    }

    @Test
    public void stateUpdatesApplyOffsets() throws Exception {
        mockResponses(DEVICE, STATUS);
        Configuration offsets = new Configuration();
        offsets.put(PhilipsAirConfiguration.CONFIG_DEF_TEMPERATURE_OFFSET, 1.0);
        offsets.put(PhilipsAirConfiguration.CONFIG_DEF_HUMIDITY_OFFSET, -1.0);
        PhilipsAirHandler handler = createHandler(
                List.of("sensors#humidity:Number:Dimensionless", "sensors#temperature:Number:Temperature"), offsets);

        initializeAndRefresh(handler, "sensors#humidity");

        QuantityType<?> humidity = (QuantityType<?>) lastState("sensors#humidity");
        assertEquals(55f, humidity.floatValue());
        assertEquals(HUMIDITY_UNIT, humidity.getUnit());
        QuantityType<?> temperature = (QuantityType<?>) lastState("sensors#temperature");
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

        waitForAssert(() -> {
            assertEquals(stateAfter, lastState(channelId));
            verify(request).method(HttpMethod.PUT);
        });
    }

    @Test
    public void testSendCommandPWR() throws Exception {
        sendCommandTemplate("controls#power:Switch", OnOffType.OFF, OnOffType.ON, OnOffType.OFF, STATUS,
                STATUS_PWR_OFF);
    }

    @Test
    public void testSendCommandUIL() throws Exception {
        sendCommandTemplate("controls-ui#button-light:Switch", OnOffType.OFF, OnOffType.ON, OnOffType.OFF, STATUS,
                STATUS_UIL_OFF);
    }

    @Test
    public void testSendCommandDDP() throws Exception {
        sendCommandTemplate("controls-ui#displayed-index:String", new StringType("0"), new StringType("1"),
                new StringType("0"), STATUS, STATUS_DDP_0);
    }

    @Test
    public void testSendCommandOM1() throws Exception {
        sendCommandTemplate("controls#fan-speed:String", new StringType("1"), new StringType("s"), new StringType("1"),
                STATUS, STATUS_OM_1);
    }

    @Test
    public void testSendCommandOMs() throws Exception {
        sendCommandTemplate("controls#fan-speed:String", new StringType("s"), new StringType("1"), new StringType("s"),
                STATUS_OM_1, STATUS);
    }

    @Test
    public void testSendCommandAqil() throws Exception {
        sendCommandTemplate("controls-ui#light-level:Number:Dimensionless", new DecimalType(25),
                new QuantityType<>(75, Units.PERCENT), new QuantityType<>(25, Units.PERCENT), STATUS, STATUS_AQIL_25);
    }

    @Test
    public void testSendCommandDt() throws Exception {
        sendCommandTemplate("controls#timer:Number", new DecimalType(1), new DecimalType(0), new DecimalType(1), STATUS,
                STATUS_DT_1);
    }

    @Test
    public void testSendCommandMode() throws Exception {
        sendCommandTemplate("controls#mode:String", new StringType("A"), new StringType("P"), new StringType("A"),
                STATUS, STATUS_MODE_A);
    }

    @Test
    public void testSendCommandCL() throws Exception {
        sendCommandTemplate("controls#child-lock:Switch", OnOffType.ON, OnOffType.OFF, OnOffType.ON, STATUS,
                STATUS_CL_ON);
    }

    @Test
    public void configurationIsOnlyPersistedWhenChanged() throws Exception {
        mockResponses(DEVICE, STATUS);
        PhilipsAirHandler handler = createHandler(List.of("controls#power:Switch"), new Configuration());

        initializeAndRefresh(handler, "controls#power");
        assertEquals("AC2889/10", handler.getThing().getProperties().get(Thing.PROPERTY_MODEL_ID));
        assertEquals("1.0.4", handler.getThing().getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION));
        long thingUpdates = countThingUpdates();

        handler.handleCommand(new ChannelUID(THING_UID, "controls#power"), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(THING_UID, "controls#power"), RefreshType.REFRESH);

        assertEquals(thingUpdates, countThingUpdates());
    }

    @Test
    public void missingDevicePropertiesDoNotPersistTheThing() throws Exception {
        mockResponses("{\"name\":\"Philips\",\"modelid\":\"AC2889/10\"}", STATUS);
        PhilipsAirHandler handler = createHandler(List.of("controls#power:Switch"), new Configuration());

        initializeAndRefresh(handler, "controls#power");
        assertFalse(handler.getThing().getProperties().containsKey(Thing.PROPERTY_FIRMWARE_VERSION));
        long thingUpdates = countThingUpdates();

        handler.handleCommand(new ChannelUID(THING_UID, "controls#power"), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(THING_UID, "controls#power"), RefreshType.REFRESH);

        assertEquals(thingUpdates, countThingUpdates());
    }

    private long countThingUpdates() {
        return mockingDetails(callback).getInvocations().stream().filter(invocation -> List
                .of("thingUpdated", "configurationUpdated").contains(invocation.getMethod().getName())).count();
    }

    @Test
    public void filtersDoNotDependOnStatus() {
        PhilipsAirHandler handler = createHandler(List.of(), new Configuration());
        PhilipsAirPurifierFiltersDTO filters = GSON.fromJson(
                "{\"fltsts0\":10,\"fltsts1\":2000,\"fltsts2\":3000,\"wicksts\":400}",
                PhilipsAirPurifierFiltersDTO.class);

        assertEquals(new QuantityType<>(10, Units.HOUR),
                handler.getValue(new ChannelUID(THING_UID, "filters#pre-filter-life"), null, null, filters));
        // fltsts1 is the HEPA filter, fltsts2 the active carbon filter
        assertEquals(new QuantityType<>(2000, Units.HOUR),
                handler.getValue(new ChannelUID(THING_UID, "filters#hepa-filter-life"), null, null, filters));
        assertEquals(new QuantityType<>(3000, Units.HOUR),
                handler.getValue(new ChannelUID(THING_UID, "filters#carbon-filter-life"), null, null, filters));
        assertEquals(new QuantityType<>(400, Units.HOUR),
                handler.getValue(new ChannelUID(THING_UID, "filters#wick-filter-life"), null, null, filters));
    }

    @Test
    public void tvocAndRssiValues() {
        PhilipsAirHandler handler = createHandler(List.of(), new Configuration());
        PhilipsAirPurifierDataDTO data = GSON.fromJson("{\"tvoc\":3,\"rssi\":-50}", PhilipsAirPurifierDataDTO.class);

        assertEquals(3, handler.getValue(new ChannelUID(THING_UID, "sensors#tvoc"), data, null, null));
        assertEquals(new QuantityType<>(-50, Units.DECIBEL_MILLIWATTS),
                handler.getValue(new ChannelUID(THING_UID, "sensors#rssi"), data, null, null));
    }

    @Test
    public void displayedIndexIsPassedAsReported() {
        PhilipsAirHandler handler = createHandler(List.of(), new Configuration());
        ChannelUID channelUID = new ChannelUID(THING_UID, "controls-ui#displayed-index");

        assertEquals("PM2.5", handler.getValue(channelUID,
                GSON.fromJson("{\"ddp\":\"PM2.5\"}", PhilipsAirPurifierDataDTO.class), null, null));
    }

    @Test
    public void missingValuesAreNull() {
        PhilipsAirHandler handler = createHandler(List.of(), new Configuration());
        PhilipsAirPurifierDataDTO data = GSON.fromJson("{}", PhilipsAirPurifierDataDTO.class);

        for (String channel : List.of("controls#power", "controls#fan-speed", "controls#child-lock", "controls#mode",
                "controls#timer", "controls-ui#button-light", "controls-ui#light-level", "controls-ui#displayed-index",
                "sensors#air-quality-threshold", "sensors#humidity", "sensors#temperature", "sensors#water-level",
                "controls#target-humidity", "controls#function", "controls#timer-remaining", "sensors#pm25",
                "sensors#allergen-index", "sensors#error-code", "sensors#tvoc", "sensors#rssi")) {
            assertNull(handler.getValue(new ChannelUID(THING_UID, channel), data, null, null), channel);
        }
        PhilipsAirPurifierFiltersDTO filters = GSON.fromJson("{}", PhilipsAirPurifierFiltersDTO.class);
        for (String channel : List.of("filters#pre-filter-life", "filters#hepa-filter-life",
                "filters#carbon-filter-life", "filters#wick-filter-life")) {
            assertNull(handler.getValue(new ChannelUID(THING_UID, channel), null, null, filters), channel);
        }
    }

    @Test
    public void testPrepareCommands() {
        PhilipsAirHandler handler = createHandler(List.of(), new Configuration());

        // testing not obvious (non intuitive types) command conversions
        PhilipsAirPurifierWritableDataDTO commandDto = handler.prepareCommandData("displayed-index",
                StringType.valueOf("0"));
        assertEquals("{\"ddp\":\"0\"}", GSON.toJson(commandDto));

        commandDto = handler.prepareCommandData("button-light", OnOffType.ON);
        assertEquals("{\"uil\":\"1\"}", GSON.toJson(commandDto));

        commandDto = handler.prepareCommandData("power", OnOffType.ON);
        assertEquals("{\"pwr\":\"1\"}", GSON.toJson(commandDto));

        commandDto = handler.prepareCommandData("light-level", DecimalType.valueOf("25"));
        assertEquals("{\"aqil\":25}", GSON.toJson(commandDto));

        commandDto = handler.prepareCommandData("light-level", DecimalType.valueOf("30"));
        assertEquals("{\"aqil\":25}", GSON.toJson(commandDto));

        commandDto = handler.prepareCommandData("light-level", new QuantityType<>(63, Units.PERCENT));
        assertEquals("{\"aqil\":75}", GSON.toJson(commandDto));

        commandDto = handler.prepareCommandData("light-level", DecimalType.valueOf("150"));
        assertEquals("{\"aqil\":100}", GSON.toJson(commandDto));

        commandDto = handler.prepareCommandData("light-level", DecimalType.valueOf("-10"));
        assertEquals("{\"aqil\":0}", GSON.toJson(commandDto));

        commandDto = handler.prepareCommandData("child-lock", OnOffType.ON);
        assertEquals("{\"cl\":true}", GSON.toJson(commandDto));

        commandDto = handler.prepareCommandData("fan-speed", StringType.valueOf("2"));
        assertEquals("{\"om\":\"2\",\"mode\":\"M\"}", GSON.toJson(commandDto));

        commandDto = handler.prepareCommandData("target-humidity", DecimalType.valueOf("50"));
        assertEquals("{\"rhset\":50}", GSON.toJson(commandDto));

        // Number:Dimensionless items send quantities
        commandDto = handler.prepareCommandData("target-humidity", new QuantityType<>(60, Units.PERCENT));
        assertEquals("{\"rhset\":60}", GSON.toJson(commandDto));

        commandDto = handler.prepareCommandData("target-humidity", new QuantityType<>(0.4, Units.ONE));
        assertEquals("{\"rhset\":40}", GSON.toJson(commandDto));
    }

    @Test
    public void unsupportedCommandsAreNotSent() {
        PhilipsAirHandler handler = createHandler(List.of(), new Configuration());

        assertNull(handler.prepareCommandData("pm25", DecimalType.valueOf("5")));
        assertNull(handler.prepareCommandData("fan-speed", OnOffType.ON));
        assertNull(handler.prepareCommandData("light-level", OnOffType.ON));
        assertNull(handler.prepareCommandData("power", StringType.valueOf("1")));
    }
}
