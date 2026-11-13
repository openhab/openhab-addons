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
package org.openhab.binding.deconz.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.deconz.internal.BindingConstants.*;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.deconz.internal.Util;
import org.openhab.binding.deconz.internal.dto.SensorConfig;
import org.openhab.binding.deconz.internal.dto.ThermostatUpdateConfig;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;

import com.google.gson.Gson;

/**
 * The {@link SensorThermostatThingHandlerTest} contains test classes for the {@link SensorThermostatThingHandler}
 *
 * @author Jan N. Klug - Initial contribution
 * @author Leo Siepel - External window command tests
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class SensorThermostatThingHandlerTest extends BaseDeconzThingHandlerTest {

    @ParameterizedTest
    @EnumSource(OnOffType.class)
    public void testExternalWindowCommand(OnOffType command) {
        Thing thing = ThingBuilder.create(THING_TYPE_THERMOSTAT, "window").build();
        ChannelUID channelUID = new ChannelUID(thing.getUID(), CHANNEL_EXTERNAL_WINDOW_OPEN);
        SensorThermostatThingHandler handler = spy(new SensorThermostatThingHandler(thing, new Gson()));
        doNothing().when(handler).sendCommand(any(), eq(command), eq(channelUID), isNull());

        handler.handleCommand(channelUID, command);

        ArgumentCaptor<ThermostatUpdateConfig> captor = ArgumentCaptor.forClass(ThermostatUpdateConfig.class);
        verify(handler).sendCommand(captor.capture(), eq(command), eq(channelUID), isNull());
        assertEquals(command == OnOffType.ON, captor.getValue().externalwindowopen);
    }

    @Test
    public void testExternalWindowIgnoresUnsupportedCommands() {
        Thing thing = ThingBuilder.create(THING_TYPE_THERMOSTAT, "window").build();
        ChannelUID channelUID = new ChannelUID(thing.getUID(), CHANNEL_EXTERNAL_WINDOW_OPEN);
        SensorThermostatThingHandler handler = spy(new SensorThermostatThingHandler(thing, new Gson()));

        handler.handleCommand(channelUID, new StringType("OPEN"));
        handler.handleCommand(channelUID, RefreshType.REFRESH);

        verify(handler, never()).sendCommand(any(), any(), any(), any());
    }

    @ParameterizedTest
    @EnumSource(OnOffType.class)
    public void testExternalWindowState(OnOffType state) {
        Thing thing = ThingBuilder.create(THING_TYPE_THERMOSTAT, "window").build();
        ChannelUID channelUID = new ChannelUID(thing.getUID(), CHANNEL_EXTERNAL_WINDOW_OPEN);
        SensorThermostatThingHandler handler = new SensorThermostatThingHandler(thing, new Gson());
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        handler.setCallback(callback);
        SensorConfig config = new SensorConfig();
        config.externalwindowopen = state == OnOffType.ON;

        handler.valueUpdated(channelUID, config);

        verify(callback).stateUpdated(channelUID, state);
    }

    @Test
    public void testDanfoss() throws IOException {
        createThing(THING_TYPE_THERMOSTAT, List.of(CHANNEL_HEATSETPOINT, CHANNEL_LAST_UPDATED, CHANNEL_TEMPERATURE,
                CHANNEL_TEMPERATURE_OFFSET, CHANNEL_THERMOSTAT_MODE), SensorThermostatThingHandler::new);

        Set<TestParam> expected = Set.of(
                // standard channels
                new TestParam(CHANNEL_HEATSETPOINT, new QuantityType<>("21.00 °C")),
                new TestParam(CHANNEL_LAST_UPDATED, Util.convertTimestampToDateTime("2023-03-18T05:52:29.506")),
                new TestParam(CHANNEL_TEMPERATURE, new QuantityType<>("21.45 °C")),
                new TestParam(CHANNEL_TEMPERATURE_OFFSET, new QuantityType<>("0.0 °C")),
                new TestParam(CHANNEL_THERMOSTAT_MODE, new StringType("HEAT")),
                // battery
                new TestParam(CHANNEL_BATTERY_LEVEL, new DecimalType(41)),
                new TestParam(CHANNEL_BATTERY_LOW, OnOffType.OFF),
                // last seen
                new TestParam(CHANNEL_LAST_SEEN, Util.convertTimestampToDateTime("2023-03-18T05:58Z")),
                // dynamic channels
                new TestParam(CHANNEL_EXTERNAL_WINDOW_OPEN, OnOffType.OFF),
                new TestParam(CHANNEL_THERMOSTAT_LOCKED, OnOffType.OFF),
                new TestParam(CHANNEL_THERMOSTAT_ON, OnOffType.OFF),
                new TestParam(CHANNEL_VALVE_POSITION, new QuantityType<>("1 %")),
                new TestParam(CHANNEL_WINDOW_OPEN, OpenClosedType.CLOSED));

        assertThing("json/thermostat/danfoss.json", expected);
    }

    @Test
    public void testNamron() throws IOException {
        createThing(THING_TYPE_THERMOSTAT, List.of(CHANNEL_TEMPERATURE, CHANNEL_HEATSETPOINT, CHANNEL_THERMOSTAT_MODE,
                CHANNEL_TEMPERATURE_OFFSET, CHANNEL_LAST_UPDATED), SensorThermostatThingHandler::new);

        Set<TestParam> expected = Set.of(
                // standard channels
                new TestParam(CHANNEL_HEATSETPOINT, new QuantityType<>("22.00 °C")),
                new TestParam(CHANNEL_LAST_UPDATED, Util.convertTimestampToDateTime("2023-03-18T18:10:39.296")),
                new TestParam(CHANNEL_TEMPERATURE, new QuantityType<>("20.39 °C")),
                new TestParam(CHANNEL_TEMPERATURE_OFFSET, new QuantityType<>("0.0 °C")),
                new TestParam(CHANNEL_THERMOSTAT_MODE, new StringType("OFF")),
                // last seen
                new TestParam(CHANNEL_LAST_SEEN, Util.convertTimestampToDateTime("2023-03-18T18:10Z")),
                // dynamic channels
                new TestParam(CHANNEL_THERMOSTAT_LOCKED, OnOffType.OFF),
                new TestParam(CHANNEL_THERMOSTAT_ON, OnOffType.OFF));

        assertThing("json/thermostat/namron_ZB_E1.json", expected);
    }

    @Test
    public void testEurotronicValid() throws IOException {
        createThing(THING_TYPE_THERMOSTAT, List.of(CHANNEL_HEATSETPOINT, CHANNEL_LAST_UPDATED, CHANNEL_TEMPERATURE,
                CHANNEL_TEMPERATURE_OFFSET, CHANNEL_THERMOSTAT_MODE), SensorThermostatThingHandler::new);

        Set<TestParam> expected = Set.of(
                // standard channels
                new TestParam(CHANNEL_HEATSETPOINT, new QuantityType<>("25.00 °C")),
                new TestParam(CHANNEL_LAST_UPDATED, Util.convertTimestampToDateTime("2020-05-31T20:24:55.819")),
                new TestParam(CHANNEL_TEMPERATURE, new QuantityType<>("16.50 °C")),
                new TestParam(CHANNEL_TEMPERATURE_OFFSET, new QuantityType<>("0.0 °C")),
                new TestParam(CHANNEL_THERMOSTAT_MODE, new StringType("AUTO")),
                // battery
                new TestParam(CHANNEL_BATTERY_LEVEL, new DecimalType(85)),
                new TestParam(CHANNEL_BATTERY_LOW, OnOffType.OFF),
                // last seen
                new TestParam(CHANNEL_LAST_SEEN, Util.convertTimestampToDateTime("2020-05-31T20:24:55.819")),
                // dynamic channels
                new TestParam(CHANNEL_THERMOSTAT_ON, OnOffType.ON),
                new TestParam(CHANNEL_VALVE_POSITION, new QuantityType<>("99 %")));

        assertThing("json/thermostat/eurotronic.json", expected);
    }

    @Test
    public void testEurotronicInvalid() throws IOException {
        createThing(THING_TYPE_THERMOSTAT, List.of(CHANNEL_HEATSETPOINT, CHANNEL_LAST_UPDATED, CHANNEL_TEMPERATURE,
                CHANNEL_TEMPERATURE_OFFSET, CHANNEL_THERMOSTAT_MODE), SensorThermostatThingHandler::new);

        Set<TestParam> expected = Set.of(
                // standard channels
                new TestParam(CHANNEL_HEATSETPOINT, new QuantityType<>("25.00 °C")),
                new TestParam(CHANNEL_LAST_UPDATED, Util.convertTimestampToDateTime("2020-05-31T20:24:55.819")),
                new TestParam(CHANNEL_TEMPERATURE, new QuantityType<>("16.50 °C")),
                new TestParam(CHANNEL_TEMPERATURE_OFFSET, new QuantityType<>("0.0 °C")),
                new TestParam(CHANNEL_THERMOSTAT_MODE, new StringType("AUTO")),
                // battery
                new TestParam(CHANNEL_BATTERY_LEVEL, new DecimalType(85)),
                new TestParam(CHANNEL_BATTERY_LOW, OnOffType.OFF),
                // last seen
                new TestParam(CHANNEL_LAST_SEEN, Util.convertTimestampToDateTime("2020-05-31T20:24:55.819")),
                // dynamic channels
                new TestParam(CHANNEL_THERMOSTAT_ON, OnOffType.ON),
                new TestParam(CHANNEL_VALVE_POSITION, UnDefType.UNDEF));

        assertThing("json/thermostat/eurotronic-invalid.json", expected);
    }
}
