/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.anel.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.anel.internal.state.AnelState;
import org.openhab.binding.anel.internal.state.AnelStateUpdater;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;

/**
 * This class tests {@link AnelStateUpdater}.
 *
 * @author Patrick Koenemann - Initial contribution
 */
@NonNullByDefault
public class AnelStateUpdaterTest implements IAnelTestStatus, IAnelConstants {

    private final AnelStateUpdater stateUpdater = new AnelStateUpdater();

    @Test
    public void noStateChange() {
        // given
        final AnelState oldState = AnelState.of(STATUS_HUT_V5);
        final AnelState newState = AnelState.of(STATUS_HUT_V5.replace(":80:", ":81:")); // port is irrelevant
        // when
        Map<String, State> updates = stateUpdater.getChannelUpdates(oldState, newState);
        // then
        assertThat(updates.entrySet(), is(empty()));
    }

    @Test
    public void fromNullStateUpdatesHome() {
        // given
        final AnelState newState = AnelState.of(STATUS_HOME_V46);
        // when
        Map<String, State> updates = stateUpdater.getChannelUpdates(null, newState);
        // then
        final Map<String, State> expected = new HashMap<>();
        expected.put(CHANNEL_NAME, new StringType("NET-CONTROL"));
        for (int i = 1; i <= 8; i++) {
            expected.put(CHANNEL_RELAY_NAME.get(i - 1), new StringType("Nr. " + i));
            expected.put(CHANNEL_RELAY_STATE.get(i - 1), OnOffType.from(i % 2 == 1));
            expected.put(CHANNEL_RELAY_LOCKED.get(i - 1), OnOffType.from(i > 3));
        }
        assertThat(updates, equalTo(expected));
    }

    @Test
    public void fromNullStateUpdatesHutPowerSensor() {
        // given
        final AnelState newState = AnelState.of(STATUS_HUT_V61_POW_SENSOR);
        // when
        Map<String, State> updates = stateUpdater.getChannelUpdates(null, newState);
        // then
        assertThat(updates.size(), is(5 + 8 * 6));
        assertThat(updates.get(CHANNEL_NAME), equalTo(new StringType("NET-CONTROL")));
        assertTemperature(updates.get(CHANNEL_TEMPERATURE), 27.7);

        assertThat(updates.get(CHANNEL_SENSOR_BRIGHTNESS), equalTo(new DecimalType("7")));
        assertThat(updates.get(CHANNEL_SENSOR_HUMIDITY), equalTo(new DecimalType("40.7")));
        assertTemperature(updates.get(CHANNEL_SENSOR_TEMPERATURE), 20.61);

        for (int i = 1; i <= 8; i++) {
            assertThat(updates.get(CHANNEL_RELAY_NAME.get(i - 1)), equalTo(new StringType("Nr. " + i)));
            assertThat(updates.get(CHANNEL_RELAY_STATE.get(i - 1)), equalTo(OnOffType.from(i <= 3 || i >= 7)));
            assertThat(updates.get(CHANNEL_RELAY_LOCKED.get(i - 1)), equalTo(OnOffType.OFF));
        }
        for (int i = 1; i <= 8; i++) {
            assertThat(updates.get(CHANNEL_IO_NAME.get(i - 1)), equalTo(new StringType("IO-" + i)));
            assertThat(updates.get(CHANNEL_IO_STATE.get(i - 1)), equalTo(OnOffType.OFF));
            assertThat(updates.get(CHANNEL_IO_MODE.get(i - 1)), equalTo(OnOffType.OFF));
        }
    }

    @Test
    public void singleRelayStateChange() {
        // given
        final AnelState oldState = AnelState.of(STATUS_HUT_V61_POW_SENSOR);
        final AnelState newState = AnelState.of(STATUS_HUT_V61_POW_SENSOR.replace("Nr. 4,0", "Nr. 4,1"));
        // when
        Map<String, State> updates = stateUpdater.getChannelUpdates(oldState, newState);
        // then
        final Map<String, State> expected = new HashMap<>();
        expected.put(CHANNEL_RELAY_STATE.get(3), OnOffType.ON);
        assertThat(updates, equalTo(expected));
    }

    @Test
    public void temperatureChange() {
        // given
        final AnelState oldState = AnelState.of(STATUS_HUT_V65);
        final AnelState newState = AnelState.of(STATUS_HUT_V65.replaceFirst(":27\\.0(.)C:", ":27.1°C:"));
        // when
        Map<String, State> updates = stateUpdater.getChannelUpdates(oldState, newState);
        // then
        assertThat(updates.size(), is(1));
        assertTemperature(updates.get(CHANNEL_TEMPERATURE), 27.1);
    }

    @Test
    public void singleSensorStatesChange() {
        // given
        final AnelState oldState = AnelState.of(STATUS_HUT_V61_SENSOR);
        final AnelState newState = AnelState.of(STATUS_HUT_V61_SENSOR.replace(":s:20.61:40.7:7.0:", ":s:20.6:40:7.1:"));
        // when
        Map<String, State> updates = stateUpdater.getChannelUpdates(oldState, newState);
        // then
        assertThat(updates.size(), is(3));
        assertThat(updates.get(CHANNEL_SENSOR_BRIGHTNESS), equalTo(new DecimalType("7.1")));
        assertThat(updates.get(CHANNEL_SENSOR_HUMIDITY), equalTo(new DecimalType("40")));
        assertTemperature(updates.get(CHANNEL_SENSOR_TEMPERATURE), 20.6);
    }

    private void assertTemperature(@Nullable State state, double value) {
        assertThat(state, isA(QuantityType.class));
        if (state instanceof QuantityType<?> temperature) {
            assertThat(temperature.doubleValue(), closeTo(value, 0.0001d));
        }
    }
}
