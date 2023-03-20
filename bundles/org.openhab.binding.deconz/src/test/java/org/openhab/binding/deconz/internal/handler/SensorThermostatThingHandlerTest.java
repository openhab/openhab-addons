/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.deconz.internal.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.deconz.internal.BindingConstants.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.deconz.DeconzTest;
import org.openhab.binding.deconz.internal.Util;
import org.openhab.binding.deconz.internal.dto.BridgeFullState;
import org.openhab.binding.deconz.internal.dto.SensorMessage;
import org.openhab.binding.deconz.internal.netutils.WebSocketConnection;
import org.openhab.binding.deconz.internal.types.LightType;
import org.openhab.binding.deconz.internal.types.LightTypeDeserializer;
import org.openhab.binding.deconz.internal.types.ResourceType;
import org.openhab.binding.deconz.internal.types.ThermostatMode;
import org.openhab.binding.deconz.internal.types.ThermostatModeGsonTypeAdapter;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.test.java.JavaTest;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link SensorThermostatThingHandlerTest} contains test classes for the {@link SensorThermostatThingHandler}
 *
 * @author Jan N. Klug - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class SensorThermostatThingHandlerTest extends JavaTest {

    private static final ThingUID BRIDGE_UID = new ThingUID(BRIDGE_TYPE, "bridge");
    private static final ThingUID THING_UID = new ThingUID(THING_TYPE_THERMOSTAT, "thing");

    private @Mock @NonNullByDefault({}) Bridge bridge;
    private @Mock @NonNullByDefault({}) ThingHandlerCallback callback;

    private @Mock @NonNullByDefault({}) DeconzBridgeHandler bridgeHandler;
    private @Mock @NonNullByDefault({}) WebSocketConnection webSocketConnection;
    private @Mock @NonNullByDefault({}) BridgeFullState bridgeFullState;

    private @NonNullByDefault({}) Gson gson;
    private @NonNullByDefault({}) Thing thing;
    private @NonNullByDefault({}) SensorThermostatThingHandler thingHandler;
    private @NonNullByDefault({}) SensorMessage sensorMessage;

    @BeforeEach
    public void setup() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LightType.class, new LightTypeDeserializer());
        gsonBuilder.registerTypeAdapter(ThermostatMode.class, new ThermostatModeGsonTypeAdapter());
        gson = gsonBuilder.create();

        ThingBuilder thingBuilder = ThingBuilder.create(THING_TYPE_THERMOSTAT, THING_UID);
        thingBuilder.withBridge(BRIDGE_UID);
        for (String channelId : List.of(CHANNEL_TEMPERATURE, CHANNEL_HEATSETPOINT, CHANNEL_THERMOSTAT_MODE,
                CHANNEL_TEMPERATURE_OFFSET, CHANNEL_LAST_UPDATED)) {
            Channel channel = ChannelBuilder.create(new ChannelUID(THING_UID, channelId))
                    .withType(new ChannelTypeUID(BINDING_ID, channelId)).build();
            thingBuilder.withChannel(channel);
        }
        thingBuilder.withConfiguration(new Configuration(Map.of(CONFIG_ID, "1")));
        thing = thingBuilder.build();

        thingHandler = new SensorThermostatThingHandler(thing, gson);
        thingHandler.setCallback(callback);

        when(callback.getBridge(BRIDGE_UID)).thenReturn(bridge);
        when(callback.createChannelBuilder(any(ChannelUID.class), any(ChannelTypeUID.class)))
                .thenAnswer(i -> ChannelBuilder.create((ChannelUID) i.getArgument(0)).withType(i.getArgument(1)));
        doAnswer(i -> {
            thing = i.getArgument(0);
            thingHandler.thingUpdated(thing);
            return null;
        }).when(callback).thingUpdated(any(Thing.class));

        when(bridge.getStatusInfo()).thenReturn(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, ""));
        when(bridge.getHandler()).thenReturn(bridgeHandler);

        when(bridgeHandler.getWebSocketConnection()).thenReturn(webSocketConnection);
        when(bridgeHandler.getBridgeFullState())
                .thenReturn(CompletableFuture.completedFuture(Optional.of(bridgeFullState)));

        when(bridgeFullState.getMessage(ResourceType.SENSORS, "1")).thenAnswer(i -> sensorMessage);
    }

    @Test
    public void testDanfoss() throws IOException {
        Set<TestParam> expected = Set.of(
                // standard channels
                new TestParam(CHANNEL_TEMPERATURE, new QuantityType<>("21.45 °C")),
                new TestParam(CHANNEL_HEATSETPOINT, new QuantityType<>("21.00 °C")),
                new TestParam(CHANNEL_THERMOSTAT_MODE, new StringType("HEAT")),
                new TestParam(CHANNEL_TEMPERATURE_OFFSET, new QuantityType<>("0.0 °C")),
                new TestParam(CHANNEL_LAST_UPDATED, Util.convertTimestampToDateTime("2023-03-18T05:52:29.506")),
                // battery
                new TestParam(CHANNEL_BATTERY_LEVEL, new DecimalType(41)),
                new TestParam(CHANNEL_BATTERY_LOW, OnOffType.OFF),
                // last seen
                new TestParam(CHANNEL_LAST_SEEN, Util.convertTimestampToDateTime("2023-03-18T05:58Z")),
                // dynamic channels
                new TestParam(CHANNEL_EXTERNAL_WINDOW_OPEN, OpenClosedType.CLOSED),
                new TestParam(CHANNEL_VALVE_POSITION, new QuantityType<>("1 %")),
                new TestParam(CHANNEL_THERMOSTAT_LOCKED, OnOffType.OFF),
                new TestParam(CHANNEL_THERMOSTAT_ON, OnOffType.OFF),
                new TestParam(CHANNEL_WINDOW_OPEN, OpenClosedType.CLOSED));

        assertThermostat("json/thermostat/danfoss.json", expected);
    }

    @Test
    public void testNamron() throws IOException {
        Set<TestParam> expected = Set.of(
                // standard channels
                new TestParam(CHANNEL_TEMPERATURE, new QuantityType<>("20.39 °C")),
                new TestParam(CHANNEL_HEATSETPOINT, new QuantityType<>("22.00 °C")),
                new TestParam(CHANNEL_THERMOSTAT_MODE, new StringType("OFF")),
                new TestParam(CHANNEL_TEMPERATURE_OFFSET, new QuantityType<>("0.0 °C")),
                new TestParam(CHANNEL_LAST_UPDATED, Util.convertTimestampToDateTime("2023-03-18T18:10:39.296")),
                // last seen
                new TestParam(CHANNEL_LAST_SEEN, Util.convertTimestampToDateTime("2023-03-18T18:10Z")),
                // dynamic channels
                new TestParam(CHANNEL_THERMOSTAT_LOCKED, OnOffType.OFF),
                new TestParam(CHANNEL_THERMOSTAT_ON, OnOffType.OFF));

        assertThermostat("json/thermostat/namron_ZB_E1.json", expected);
    }

    @Test
    public void testEurotronicValid() throws IOException {
        Set<TestParam> expected = Set.of(
                // standard channels
                new TestParam(CHANNEL_TEMPERATURE, new QuantityType<>("16.50 °C")),
                new TestParam(CHANNEL_HEATSETPOINT, new QuantityType<>("25.00 °C")),
                new TestParam(CHANNEL_THERMOSTAT_MODE, new StringType("AUTO")),
                new TestParam(CHANNEL_TEMPERATURE_OFFSET, new QuantityType<>("0.0 °C")),
                new TestParam(CHANNEL_LAST_UPDATED, Util.convertTimestampToDateTime("2020-05-31T20:24:55.819")),
                // battery
                new TestParam(CHANNEL_BATTERY_LEVEL, new DecimalType(85)),
                new TestParam(CHANNEL_BATTERY_LOW, OnOffType.OFF),
                // last seen
                new TestParam(CHANNEL_LAST_SEEN, Util.convertTimestampToDateTime("2020-05-31T20:24:55.819")),
                // dynamic channels
                new TestParam(CHANNEL_VALVE_POSITION, new QuantityType<>("99 %")),
                new TestParam(CHANNEL_THERMOSTAT_ON, OnOffType.ON));

        assertThermostat("json/thermostat/eurotronic.json", expected);
    }

    @Test
    public void testEurotronicInvalid() throws IOException {
        Set<TestParam> expected = Set.of(
                // standard channels
                new TestParam(CHANNEL_TEMPERATURE, new QuantityType<>("16.50 °C")),
                new TestParam(CHANNEL_HEATSETPOINT, new QuantityType<>("25.00 °C")),
                new TestParam(CHANNEL_THERMOSTAT_MODE, new StringType("AUTO")),
                new TestParam(CHANNEL_TEMPERATURE_OFFSET, new QuantityType<>("0.0 °C")),
                new TestParam(CHANNEL_LAST_UPDATED, Util.convertTimestampToDateTime("2020-05-31T20:24:55.819")),
                // battery
                new TestParam(CHANNEL_BATTERY_LEVEL, new DecimalType(85)),
                new TestParam(CHANNEL_BATTERY_LOW, OnOffType.OFF),
                // last seen
                new TestParam(CHANNEL_LAST_SEEN, Util.convertTimestampToDateTime("2020-05-31T20:24:55.819")),
                // dynamic channels
                new TestParam(CHANNEL_VALVE_POSITION, UnDefType.UNDEF),
                new TestParam(CHANNEL_THERMOSTAT_ON, OnOffType.ON));

        assertThermostat("json/thermostat/eurotronic-invalid.json", expected);
    }

    private void assertThermostat(String fileName, Set<TestParam> expected) throws IOException {
        sensorMessage = DeconzTest.getObjectFromJson(fileName, SensorMessage.class, gson);

        thingHandler.initialize();

        ArgumentCaptor<ThingStatusInfo> captor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback, times(6)).statusUpdated(eq(thing), captor.capture());

        List<ThingStatusInfo> statusInfoList = captor.getAllValues();
        assertThat(statusInfoList.get(0).getStatus(), is(ThingStatus.UNKNOWN));
        assertThat(statusInfoList.get(5).getStatus(), is(ThingStatus.ONLINE));

        assertThat(thing.getChannels().size(), is(expected.size()));
        for (TestParam testParam : expected) {
            Channel channel = thing.getChannel(testParam.channelId());
            assertThat(channel + "expected but missing", channel, is(notNullValue()));

            State state = testParam.state;
            if (state != null) {
                verify(callback, times(3).description(channel + " did not receive an update"))
                        .stateUpdated(eq(channel.getUID()), eq(state));
            }
        }
    }

    private record TestParam(String channelId, @Nullable State state) {
    }
}
