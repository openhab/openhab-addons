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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.philipsair.internal.FakeHttpDevice.Call;
import org.openhab.binding.philipsair.internal.FakeHttpDevice.Endpoint;
import org.openhab.binding.philipsair.internal.connection.PhilipsAirAPIConnection;
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
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;

import com.google.gson.Gson;

/**
 * Test cases for {@link PhilipsAirHandler} using the encrypted HTTP protocol. The HTTP client is replaced by a
 * {@link FakeHttpDevice}, the encryption, parsing and channel updates are exercised for real.
 *
 * @author Michał Boroński - Initial contribution
 * @author Marcel Verpaalen - Re-enable tests for current handler behavior
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
public class PhilipsAirHandlerTest extends JavaTest {

    private static final String FAKE_KEY = "1F09722BE668AF0B8DC78051B70E4F76";
    private static final String OLD_KEY = "FFEEDDCCBBAA99887766554433221100";
    private static final String OTHER_KEY = "0123456789ABCDEF0123456789ABCDEF";
    private static final String HOST = "1.1.1.1";
    private static final ThingUID THING_UID = new ThingUID(THING_TYPE_AC2889_10, "1");

    private static final String DEVICE = "{\"name\":\"Philips\",\"type\":\"AC2889\",\"modelid\":\"AC2889/10\",\"swversion\":\"1.0.4\"}";
    private static final String FILTERS_JSON = "{\"fltsts0\":10,\"fltsts1\":2000,\"fltsts2\":3000}";
    private static final String FILTERS_WITH_WICK_JSON = "{\"fltsts0\":10,\"fltsts1\":2000,\"fltsts2\":3000,\"wicksts\":400}";
    private static final String STATUS = "{\"om\":\"s\",\"pwr\":\"1\",\"cl\":false,\"aqil\":75,\"uil\":\"1\",\"dt\":0,\"dtrs\":0,\"mode\":\"P\",\"func\":\"PH\",\"rhset\":40,\"rh\":56,\"temp\":21,\"pm25\":8,\"iaql\":2,\"aqit\":4,\"ddp\":\"1\",\"err\":0,\"wl\":0}";
    // the status with the values of the channels of the settings the devices of some models report
    private static final String STATUS_WITH_SETTINGS = "{\"om\":\"s\",\"pwr\":\"1\",\"cl\":false,\"aqil\":75,\"uil\":\"1\",\"dt\":0,\"dtrs\":0,\"mode\":\"P\",\"func\":\"PH\",\"rhset\":40,\"rh\":56,\"temp\":21,\"pm25\":8,\"iaql\":2,\"aqit\":4,\"ddp\":\"1\",\"err\":0,\"wl\":0,\"tvoc\":3,\"rssi\":-50,\"beep\":true,\"standby\":false,\"allslp\":true,\"dispon\":true,\"dispbr\":\"115\",\"lamp\":\"2\"}";
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
            "controls#standby-sensors:Switch", "controls#allergy-sleep:Switch", "controls-ui#button-light:Switch",
            "controls-ui#light-level:Number:Dimensionless", "controls-ui#displayed-index:String",
            "controls-ui#beep:Switch", "controls-ui#display:Switch", "controls-ui#display-brightness:String",
            "controls-ui#lamp-mode:String", "sensors#pm25:Number:Density", "sensors#allergen-index:Number",
            "sensors#air-quality-threshold:Number", "sensors#error-code:String",
            "sensors#humidity:Number:Dimensionless", "sensors#temperature:Number:Temperature",
            "sensors#water-level:Number:Dimensionless", "sensors#tvoc:Number", "sensors#rssi:Number:Power",
            "filters#pre-filter-life:Number:Time", "filters#hepa-filter-life:Number:Time",
            "filters#carbon-filter-life:Number:Time", "filters#wick-filter-life:Number:Time");

    private static final Gson GSON = new Gson();

    private final FakeHttpDevice device = new FakeHttpDevice(HOST, FAKE_KEY);
    private final Set<ChannelUID> linkedChannels = new HashSet<>();
    private final AtomicInteger connectionsCreated = new AtomicInteger();
    private final AtomicInteger connectionRefreshInterval = new AtomicInteger();

    private @Mock @NonNullByDefault({}) ThingHandlerCallback callback;
    private @Mock @NonNullByDefault({}) PhilipsAirStateDescriptionOptionProvider stateDescriptionProvider;

    private @NonNullByDefault({}) PhilipsAirHandler handler;

    @AfterEach
    public void tearDown() {
        if (handler != null) {
            handler.dispose();
        }
    }

    private PhilipsAirHandler createHandler(List<String> channels, Configuration extraConfig) {
        Configuration config = new Configuration();
        config.put(PhilipsAirConfiguration.CONFIG_REFRESH_INTERVAL, 5);
        config.put(PhilipsAirConfiguration.CONFIG_HOST, HOST);
        config.put(PhilipsAirConfiguration.CONFIG_KEY, FAKE_KEY);
        extraConfig.getProperties().forEach(config::put);

        List<Channel> thingChannels = new ArrayList<>();
        for (String channel : channels) {
            String[] parts = channel.split(":", 2);
            ChannelUID channelUID = new ChannelUID(THING_UID, parts[0]);
            thingChannels.add(ChannelBuilder.create(channelUID, parts[1]).build());
            linkedChannels.add(channelUID);
        }
        Thing thing = PhilipsAirHandlerFixture.thing(THING_TYPE_AC2889_10, THING_UID, config, Map.of(), thingChannels);
        PhilipsAirHandler handler = new PhilipsAirHandler(thing, device.httpClient(), stateDescriptionProvider) {
            @Override
            PhilipsAirAPIConnection createConnection(PhilipsAirConfiguration config) {
                connectionsCreated.incrementAndGet();
                connectionRefreshInterval.set(config.getRefreshInterval());
                return super.createConnection(config);
            }
        };
        handler.setCallback(callback);
        this.handler = handler;
        return handler;
    }

    /**
     * Scripts the responses of the device. The requests of the other URLs are answered independent of each other and
     * of the time they are requested.
     */
    private void respondWith(String deviceJson, String statusJson) throws Exception {
        respondWith(deviceJson, statusJson, FILTERS_JSON);
    }

    private void respondWith(String deviceJson, String statusJson, String filtersJson) throws Exception {
        device.respond(Endpoint.DEVICE, HttpMethod.GET, deviceJson);
        device.respond(Endpoint.STATUS, HttpMethod.GET, statusJson);
        device.respond(Endpoint.FILTERS, HttpMethod.GET, filtersJson);
    }

    private static boolean isOnline(ThingStatusInfo statusInfo) {
        return statusInfo.getStatus() == ThingStatus.ONLINE;
    }

    /**
     * Initializes the handler and waits until the first poll, which is scheduled without delay, has published its
     * result. The status is the last thing published by an update.
     */
    private void initializeAndWaitForOnline(PhilipsAirHandler handler) {
        // the channels of the thing are the linked ones, the ones the handler adds are not
        when(callback.isChannelLinked(any(ChannelUID.class)))
                .thenAnswer(invocation -> linkedChannels.contains(invocation.getArgument(0)));
        // lenient, as it is only used by the tests of which the thing lacks channels the device reports
        lenient().when(callback.createChannelBuilder(any(ChannelUID.class), any(ChannelTypeUID.class)))
                .thenAnswer(invocation -> ChannelBuilder.create((ChannelUID) invocation.getArgument(0), "Number"));
        handler.initialize();
        waitForAssert(() -> verify(callback, atLeastOnce()).statusUpdated(any(Thing.class),
                argThat(PhilipsAirHandlerTest::isOnline)), 10000, 100);
    }

    private State lastState(String channel) {
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        // atLeastOnce, as the periodic poll may have published the state again
        verify(callback, atLeastOnce()).stateUpdated(eq(new ChannelUID(THING_UID, channel)), stateCaptor.capture());
        List<State> states = stateCaptor.getAllValues();
        return states.get(states.size() - 1);
    }

    /**
     * Asserts that every state published for the channel so far is the expected one. This also fails when a wrong
     * state was published and corrected later.
     */
    private void assertAllStates(String channel, State expected) {
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(callback, atLeastOnce()).stateUpdated(eq(new ChannelUID(THING_UID, channel)), stateCaptor.capture());
        for (State state : stateCaptor.getAllValues()) {
            assertEquals(expected, state, channel);
            if (expected instanceof QuantityType<?> expectedQuantity && state instanceof QuantityType<?> quantity) {
                assertEquals(expectedQuantity.getUnit(), quantity.getUnit(), channel);
            }
        }
    }

    @Test
    public void initializeGoesOnlineAfterFirstData() throws Exception {
        respondWith(DEVICE, STATUS);
        PhilipsAirHandler handler = createHandler(List.of("controls#power:Switch"), new Configuration());

        initializeAndWaitForOnline(handler);

        InOrder statusOrder = inOrder(callback);
        statusOrder.verify(callback, atLeastOnce()).statusUpdated(any(Thing.class),
                argThat(statusInfo -> statusInfo.getStatus() == ThingStatus.UNKNOWN));
        statusOrder.verify(callback, atLeastOnce()).statusUpdated(any(Thing.class),
                argThat(PhilipsAirHandlerTest::isOnline));
        // no other status in between or after
        ArgumentCaptor<ThingStatusInfo> statuses = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback, atLeastOnce()).statusUpdated(any(Thing.class), statuses.capture());
        assertTrue(statuses.getAllValues().stream().map(ThingStatusInfo::getStatus)
                .allMatch(status -> status == ThingStatus.UNKNOWN || status == ThingStatus.ONLINE));
        assertEquals(ThingStatus.UNKNOWN, statuses.getAllValues().get(0).getStatus());
        assertEquals(ThingStatus.ONLINE, statuses.getAllValues().get(statuses.getAllValues().size() - 1).getStatus());
        // the first update requests the device info, the status and the filters (to detect the optional filter channel)
        assertEquals(
                List.of("http://1.1.1.1/di/v1/products/1/device", "http://1.1.1.1/di/v1/products/1/air",
                        "http://1.1.1.1/di/v1/products/1/fltsts"),
                device.calls().stream().map(Call::url).limit(3).toList());
        assertTrue(device.calls().stream().allMatch(call -> call.method() == HttpMethod.GET));
        // the key is configured, so there is no key exchange
        assertEquals(0, device.count(Endpoint.SECURITY, HttpMethod.PUT));
    }

    @Test
    public void stateUpdatesForAllChannels() throws Exception {
        respondWith(DEVICE, STATUS_WITH_SETTINGS, FILTERS_WITH_WICK_JSON);
        PhilipsAirHandler handler = createHandler(ALL_CHANNELS, new Configuration());

        initializeAndWaitForOnline(handler);

        assertAllStates("controls#power", OnOffType.ON);
        assertAllStates("controls#fan-speed", new StringType("s"));
        assertAllStates("controls#child-lock", OnOffType.OFF);
        assertAllStates("controls#mode", new StringType("P"));
        assertAllStates("controls#timer", new DecimalType(0));
        assertAllStates("controls#timer-remaining", new QuantityType<>(0, Units.MINUTE));
        assertAllStates("controls#target-humidity", new QuantityType<>(40, Units.PERCENT));
        assertAllStates("controls#function", new StringType("PH"));
        assertAllStates("controls#standby-sensors", OnOffType.OFF);
        assertAllStates("controls#allergy-sleep", OnOffType.ON);
        assertAllStates("controls-ui#button-light", OnOffType.ON);
        assertAllStates("controls-ui#light-level", new QuantityType<>(75, Units.PERCENT));
        assertAllStates("controls-ui#displayed-index", new StringType("1"));
        assertAllStates("controls-ui#beep", OnOffType.ON);
        assertAllStates("controls-ui#display", OnOffType.ON);
        assertAllStates("controls-ui#display-brightness", new StringType("115"));
        assertAllStates("controls-ui#lamp-mode", new StringType("2"));
        assertAllStates("sensors#pm25", new QuantityType<>(8, DENSITY_UNIT));
        assertAllStates("sensors#allergen-index", new DecimalType(2));
        assertAllStates("sensors#air-quality-threshold", new DecimalType(4));
        assertAllStates("sensors#error-code", new StringType("0"));
        assertAllStates("sensors#humidity", new QuantityType<>(56, HUMIDITY_UNIT));
        assertAllStates("sensors#temperature", new QuantityType<>(21, TEMPERATURE_UNIT));
        assertAllStates("sensors#water-level", new QuantityType<>(0, Units.PERCENT));
        assertAllStates("sensors#tvoc", new DecimalType(3));
        assertAllStates("sensors#rssi", new QuantityType<>(-50, Units.DECIBEL_MILLIWATTS));
        assertAllStates("filters#pre-filter-life", new QuantityType<>(10, Units.HOUR));
        assertAllStates("filters#hepa-filter-life", new QuantityType<>(2000, Units.HOUR));
        assertAllStates("filters#carbon-filter-life", new QuantityType<>(3000, Units.HOUR));
        assertAllStates("filters#wick-filter-life", new QuantityType<>(400, Units.HOUR));
    }

    @Test
    public void stateUpdatesApplyOffsets() throws Exception {
        respondWith(DEVICE, STATUS);
        Configuration offsets = new Configuration();
        offsets.put(PhilipsAirConfiguration.CONFIG_TEMPERATURE_OFFSET, 1.0);
        offsets.put(PhilipsAirConfiguration.CONFIG_HUMIDITY_OFFSET, -1.0);
        PhilipsAirHandler handler = createHandler(
                List.of("sensors#humidity:Number:Dimensionless", "sensors#temperature:Number:Temperature"), offsets);

        initializeAndWaitForOnline(handler);

        assertAllStates("sensors#humidity", new QuantityType<>(55, HUMIDITY_UNIT));
        assertAllStates("sensors#temperature", new QuantityType<>(22, TEMPERATURE_UNIT));
    }

    @ParameterizedTest
    @CsvSource({ "50.0, 100", "-100.0, 0" })
    public void humidityWithOffsetStaysWithinTheRange(double offset, float expected) throws Exception {
        respondWith(DEVICE, STATUS);
        Configuration offsets = new Configuration();
        offsets.put(PhilipsAirConfiguration.CONFIG_HUMIDITY_OFFSET, offset);
        PhilipsAirHandler handler = createHandler(List.of("sensors#humidity:Number:Dimensionless"), offsets);

        initializeAndWaitForOnline(handler);

        assertAllStates("sensors#humidity", new QuantityType<>(expected, HUMIDITY_UNIT));
    }

    static Stream<Arguments> commandCases() {
        return Stream.of(
                Arguments.of("power", "controls#power:Switch", OnOffType.OFF, OnOffType.ON, OnOffType.OFF, STATUS,
                        STATUS_PWR_OFF, "{\"pwr\":\"0\"}"),
                Arguments.of("button light", "controls-ui#button-light:Switch", OnOffType.OFF, OnOffType.ON,
                        OnOffType.OFF, STATUS, STATUS_UIL_OFF, "{\"uil\":\"0\"}"),
                Arguments.of("displayed index", "controls-ui#displayed-index:String", new StringType("0"),
                        new StringType("1"), new StringType("0"), STATUS, STATUS_DDP_0, "{\"ddp\":\"0\"}"),
                Arguments.of("manual fan speed", "controls#fan-speed:String", new StringType("1"), new StringType("s"),
                        new StringType("1"), STATUS, STATUS_OM_1, "{\"om\":\"1\",\"mode\":\"M\"}"),
                Arguments.of("silent fan speed", "controls#fan-speed:String", new StringType("s"), new StringType("1"),
                        new StringType("s"), STATUS_OM_1, STATUS, "{\"om\":\"s\",\"mode\":\"M\"}"),
                Arguments.of("light level", "controls-ui#light-level:Number:Dimensionless", new DecimalType(25),
                        new QuantityType<>(75, Units.PERCENT), new QuantityType<>(25, Units.PERCENT), STATUS,
                        STATUS_AQIL_25, "{\"aqil\":25}"),
                Arguments.of("timer", "controls#timer:Number", new DecimalType(1), new DecimalType(0),
                        new DecimalType(1), STATUS, STATUS_DT_1, "{\"dt\":1}"),
                Arguments.of("mode", "controls#mode:String", new StringType("A"), new StringType("P"),
                        new StringType("A"), STATUS, STATUS_MODE_A, "{\"mode\":\"A\"}"),
                Arguments.of("child lock", "controls#child-lock:Switch", OnOffType.ON, OnOffType.OFF, OnOffType.ON,
                        STATUS, STATUS_CL_ON, "{\"cl\":true}"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("commandCases")
    public void commandIsSent(String name, String channel, Command command, State stateBefore, State stateAfter,
            String statusBefore, String commandResponse, String expectedBody) throws Exception {
        respondWith(DEVICE, statusBefore);
        device.respondToCommandWithStatus(commandResponse);
        String channelId = channel.split(":", 2)[0];
        PhilipsAirHandler handler = createHandler(List.of(channel), new Configuration());

        initializeAndWaitForOnline(handler);
        assertEquals(stateBefore, lastState(channelId));

        handler.handleCommand(new ChannelUID(THING_UID, channelId), command);

        waitForAssert(() -> assertEquals(stateAfter, lastState(channelId)));
        List<Call> commands = device.calls(Endpoint.STATUS, HttpMethod.PUT);
        assertEquals(1, commands.size());
        assertEquals("http://1.1.1.1/di/v1/products/1/air", commands.get(0).url());
        assertEquals(expectedBody, device.decryptedBody(commands.get(0)));
    }

    @Test
    public void configurationIsOnlyPersistedWhenChanged() throws Exception {
        respondWith(DEVICE, STATUS);
        PhilipsAirHandler handler = createHandler(List.of("controls#power:Switch"), new Configuration());

        initializeAndWaitForOnline(handler);
        assertEquals("AC2889/10", handler.getThing().getProperties().get(Thing.PROPERTY_MODEL_ID));
        assertEquals("1.0.4", handler.getThing().getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION));

        assertRefreshDoesNotUpdateThing(handler, "controls#power");
    }

    @Test
    public void missingDevicePropertiesDoNotPersistTheThing() throws Exception {
        respondWith("{\"name\":\"Philips\",\"modelid\":\"AC2889/10\"}", STATUS);
        PhilipsAirHandler handler = createHandler(List.of("controls#power:Switch"), new Configuration());

        initializeAndWaitForOnline(handler);
        assertFalse(handler.getThing().getProperties().containsKey(Thing.PROPERTY_FIRMWARE_VERSION));

        assertRefreshDoesNotUpdateThing(handler, "controls#power");
    }

    /**
     * A refresh is executed asynchronously. The status is the last thing an update publishes, so the thing and the
     * configuration were not updated by it when the status is published again.
     */
    private void assertRefreshDoesNotUpdateThing(PhilipsAirHandler handler, String channel) {
        clearInvocations(callback);

        handler.handleCommand(new ChannelUID(THING_UID, channel), RefreshType.REFRESH);

        verify(callback, timeout(10000).atLeastOnce()).statusUpdated(any(Thing.class),
                argThat(PhilipsAirHandlerTest::isOnline));
        verify(callback, never()).thingUpdated(any());
        verify(callback, never()).configurationUpdated(any());
    }

    @Test
    public void linkedFilterChannelRequestsTheFiltersOfADeviceThatDoesNotPushItsStatus() throws Exception {
        respondWith(DEVICE, STATUS);
        PhilipsAirHandler handler = createHandler(
                List.of("controls#power:Switch", "filters#pre-filter-life:Number:Time"), new Configuration());

        initializeAndWaitForOnline(handler);

        assertEquals(new QuantityType<>(10, Units.HOUR), lastState("filters#pre-filter-life"));
        int filterRequests = device.count(Endpoint.FILTERS, HttpMethod.GET);
        assertTrue(filterRequests >= 1);

        // unlike the probe for the wick filter, which stops once the device answered, every poll requests the filters.
        // The responses are cached for the refresh interval, so this is the periodic poll after it.
        waitForAssert(() -> assertTrue(device.count(Endpoint.FILTERS, HttpMethod.GET) > filterRequests), 15000, 100);
    }

    @Test
    public void filtersAreOnlyRequestedOnceWithoutLinkedFilterChannel() throws Exception {
        respondWith(DEVICE, STATUS);
        PhilipsAirHandler handler = createHandler(List.of("controls#power:Switch"), new Configuration());

        initializeAndWaitForOnline(handler);
        // the first update probes the filters to detect the optional wick filter channel, which succeeded
        assertEquals(1, device.count(Endpoint.FILTERS, HttpMethod.GET));
        int statusRequests = device.count(Endpoint.STATUS, HttpMethod.GET);

        // the responses are cached for the refresh interval, so this is the periodic poll after it
        waitForAssert(() -> assertTrue(device.count(Endpoint.STATUS, HttpMethod.GET) > statusRequests), 15000, 100);

        assertEquals(1, device.count(Endpoint.FILTERS, HttpMethod.GET));
    }

    @Test
    public void refreshIntervalBelowTheMinimumIsRaisedToTheMinimum() throws Exception {
        respondWith(DEVICE, STATUS);
        Configuration tooShort = new Configuration();
        tooShort.put(PhilipsAirConfiguration.CONFIG_REFRESH_INTERVAL, 1);
        PhilipsAirHandler handler = createHandler(List.of("controls#power:Switch"), tooShort);

        initializeAndWaitForOnline(handler);

        assertEquals(PhilipsAirConfiguration.MIN_REFRESH_INTERVAL, connectionRefreshInterval.get());
    }

    @Test
    public void refreshIntervalAboveTheMinimumIsKept() throws Exception {
        respondWith(DEVICE, STATUS);
        Configuration longer = new Configuration();
        longer.put(PhilipsAirConfiguration.CONFIG_REFRESH_INTERVAL, 60);
        PhilipsAirHandler handler = createHandler(List.of("controls#power:Switch"), longer);

        initializeAndWaitForOnline(handler);

        assertEquals(60, connectionRefreshInterval.get());
    }

    @Test
    public void blankHostIsAConfigurationError() {
        Configuration blankHost = new Configuration();
        blankHost.put(PhilipsAirConfiguration.CONFIG_HOST, "");
        blankHost.put(PhilipsAirConfiguration.CONFIG_KEY, "");
        PhilipsAirHandler handler = createHandler(List.of(), blankHost);

        handler.initialize();

        // nothing else was published, like the unknown status that precedes the connection
        verify(callback).statusUpdated(any(Thing.class), eq(new ThingStatusInfo(ThingStatus.OFFLINE,
                ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.config-error.missing-host")));
        verifyNoMoreInteractions(callback);
        assertEquals(0, connectionsCreated.get());
        // there is no connection that could be used, with an empty key it would start with a key exchange
        assertTrue(device.calls().isEmpty());
    }

    @Test
    public void exchangedKeyIsStoredInTheConfiguration() throws Exception {
        respondWith(DEVICE, STATUS);
        device.respondToKeyExchange();
        Configuration noKey = new Configuration();
        noKey.put(PhilipsAirConfiguration.CONFIG_KEY, "");
        PhilipsAirHandler handler = createHandler(List.of("controls#power:Switch"), noKey);
        assertEquals("", handler.getThing().getConfiguration().get(PhilipsAirConfiguration.CONFIG_KEY));

        initializeAndWaitForOnline(handler);

        assertEquals(FAKE_KEY, handler.getThing().getConfiguration().get(PhilipsAirConfiguration.CONFIG_KEY));
        verify(callback, atLeastOnce()).thingUpdated(any());
        assertEquals(1, device.count(Endpoint.SECURITY, HttpMethod.PUT));
    }

    @Test
    public void renewedKeyIsStoredInTheConfiguration() throws Exception {
        respondWith(DEVICE, STATUS);
        // the device lost the key, so its first response cannot be decrypted with the stored key
        device.respondRaw(Endpoint.DEVICE, HttpMethod.GET, 200, FakeHttpDevice.encrypt(DEVICE, OTHER_KEY),
                FakeHttpDevice.encrypt(DEVICE, FAKE_KEY));
        device.respondToKeyExchange();
        Configuration oldKey = new Configuration();
        oldKey.put(PhilipsAirConfiguration.CONFIG_KEY, OLD_KEY);
        PhilipsAirHandler handler = createHandler(List.of("controls#power:Switch"), oldKey);

        initializeAndWaitForOnline(handler);

        assertEquals(FAKE_KEY, handler.getThing().getConfiguration().get(PhilipsAirConfiguration.CONFIG_KEY));
        verify(callback, atLeastOnce()).thingUpdated(any());
        assertEquals(1, device.count(Endpoint.SECURITY, HttpMethod.PUT));
        assertEquals(2, device.count(Endpoint.DEVICE, HttpMethod.GET));
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
    public void commandDataIsPrepared() {
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

        commandDto = handler.prepareCommandData("function", StringType.valueOf("PH"));
        assertEquals("{\"func\":\"PH\"}", GSON.toJson(commandDto));
    }

    @Test
    public void unsupportedCommandsAreNotSent() throws Exception {
        respondWith(DEVICE, STATUS);
        device.respondToCommandWithStatus(STATUS_PWR_OFF);
        PhilipsAirHandler handler = createHandler(
                List.of("controls#power:Switch", "controls#fan-speed:String",
                        "controls-ui#light-level:Number:Dimensionless", "sensors#pm25:Number:Density"),
                new Configuration());
        initializeAndWaitForOnline(handler);

        // none of these is a valid command for the channel
        handler.handleCommand(new ChannelUID(THING_UID, "sensors#pm25"), DecimalType.valueOf("5"));
        handler.handleCommand(new ChannelUID(THING_UID, "controls#fan-speed"), OnOffType.ON);
        handler.handleCommand(new ChannelUID(THING_UID, "controls-ui#light-level"), OnOffType.ON);
        handler.handleCommand(new ChannelUID(THING_UID, "controls#power"), StringType.valueOf("1"));
        // commands are executed in order, so the commands above are processed once this one has its effect
        handler.handleCommand(new ChannelUID(THING_UID, "controls#power"), OnOffType.OFF);

        waitForAssert(() -> assertEquals(OnOffType.OFF, lastState("controls#power")));
        List<Call> commands = device.calls(Endpoint.STATUS, HttpMethod.PUT);
        assertEquals(1, commands.size());
        assertEquals("{\"pwr\":\"0\"}", device.decryptedBody(commands.get(0)));
    }
}
