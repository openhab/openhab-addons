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
package org.openhab.binding.nest.internal.wwn.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.openhab.binding.nest.internal.wwn.WWNBindingConstants.*;
import static org.openhab.binding.nest.internal.wwn.dto.WWNDataUtil.*;
import static org.openhab.core.library.types.OnOffType.*;
import static org.openhab.core.library.unit.ImperialUnits.FAHRENHEIT;
import static org.openhab.core.library.unit.SIUnits.CELSIUS;

import java.io.IOException;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.nest.internal.wwn.config.WWNDeviceConfiguration;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ThingBuilder;

/**
 * Tests for {@link WWNThermostatHandler}.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class WWNThermostatHandlerTest extends WWNThingHandlerOSGiTest {

    private static final ThingUID THERMOSTAT_UID = new ThingUID(THING_TYPE_THERMOSTAT, "thermostat1");
    private static final int CHANNEL_COUNT = 25;

    public WWNThermostatHandlerTest() {
        super(WWNThermostatHandler.class);
    }

    @Override
    protected Thing buildThing(Bridge bridge) {
        Map<String, Object> properties = Map.of(WWNDeviceConfiguration.DEVICE_ID, THERMOSTAT1_DEVICE_ID);

        return ThingBuilder.create(THING_TYPE_THERMOSTAT, THERMOSTAT_UID).withLabel("Test Thermostat")
                .withBridge(bridge.getUID()).withChannels(buildChannels(THING_TYPE_THERMOSTAT, THERMOSTAT_UID))
                .withConfiguration(new Configuration(properties)).build();
    }

    @Test
    public void completeThermostatCelsiusUpdate() throws IOException {
        assertThat(thing.getChannels().size(), is(CHANNEL_COUNT));
        assertThat(thing.getStatus(), is(ThingStatus.OFFLINE));

        waitForAssert(() -> assertThat(bridge.getStatus(), is(ThingStatus.ONLINE)));
        putStreamingEventData(fromFile(COMPLETE_DATA_FILE_NAME, CELSIUS));
        waitForAssert(() -> assertThat(thing.getStatus(), is(ThingStatus.ONLINE)));

        assertThatItemHasState(CHANNEL_CAN_COOL, OFF);
        assertThatItemHasState(CHANNEL_CAN_HEAT, ON);
        assertThatItemHasState(CHANNEL_ECO_MAX_SET_POINT, new QuantityType<>(24, CELSIUS));
        assertThatItemHasState(CHANNEL_ECO_MIN_SET_POINT, new QuantityType<>(12.5, CELSIUS));
        assertThatItemHasState(CHANNEL_FAN_TIMER_ACTIVE, OFF);
        assertThatItemHasState(CHANNEL_FAN_TIMER_DURATION, new QuantityType<>(15, Units.MINUTE));
        assertThatItemHasState(CHANNEL_FAN_TIMER_TIMEOUT, parseDateTimeType("1970-01-01T00:00:00.000Z"));
        assertThatItemHasState(CHANNEL_HAS_FAN, ON);
        assertThatItemHasState(CHANNEL_HAS_LEAF, ON);
        assertThatItemHasState(CHANNEL_HUMIDITY, new QuantityType<>(25, Units.PERCENT));
        assertThatItemHasState(CHANNEL_LAST_CONNECTION, parseDateTimeType("2017-02-02T21:00:06.000Z"));
        assertThatItemHasState(CHANNEL_LOCKED, OFF);
        assertThatItemHasState(CHANNEL_LOCKED_MAX_SET_POINT, new QuantityType<>(22, CELSIUS));
        assertThatItemHasState(CHANNEL_LOCKED_MIN_SET_POINT, new QuantityType<>(20, CELSIUS));
        assertThatItemHasState(CHANNEL_MAX_SET_POINT, new QuantityType<>(24, CELSIUS));
        assertThatItemHasState(CHANNEL_MIN_SET_POINT, new QuantityType<>(20, CELSIUS));
        assertThatItemHasState(CHANNEL_MODE, new StringType("HEAT"));
        assertThatItemHasState(CHANNEL_PREVIOUS_MODE, new StringType("HEAT"));
        assertThatItemHasState(CHANNEL_SET_POINT, new QuantityType<>(15.5, CELSIUS));
        assertThatItemHasState(CHANNEL_STATE, new StringType("OFF"));
        assertThatItemHasState(CHANNEL_SUNLIGHT_CORRECTION_ACTIVE, OFF);
        assertThatItemHasState(CHANNEL_SUNLIGHT_CORRECTION_ENABLED, ON);
        assertThatItemHasState(CHANNEL_TEMPERATURE, new QuantityType<>(19, CELSIUS));
        assertThatItemHasState(CHANNEL_TIME_TO_TARGET, new QuantityType<>(0, Units.MINUTE));
        assertThatItemHasState(CHANNEL_USING_EMERGENCY_HEAT, OFF);

        assertThatAllItemStatesAreNotNull();
    }

    @Test
    public void completeThermostatFahrenheitUpdate() throws IOException {
        assertThat(thing.getChannels().size(), is(CHANNEL_COUNT));
        assertThat(thing.getStatus(), is(ThingStatus.OFFLINE));

        waitForAssert(() -> assertThat(bridge.getStatus(), is(ThingStatus.ONLINE)));
        putStreamingEventData(fromFile(COMPLETE_DATA_FILE_NAME, FAHRENHEIT));
        waitForAssert(() -> assertThat(thing.getStatus(), is(ThingStatus.ONLINE)));

        assertThatItemHasState(CHANNEL_CAN_COOL, OFF);
        assertThatItemHasState(CHANNEL_CAN_HEAT, ON);
        assertThatItemHasState(CHANNEL_ECO_MAX_SET_POINT, new QuantityType<>(76, FAHRENHEIT));
        assertThatItemHasState(CHANNEL_ECO_MIN_SET_POINT, new QuantityType<>(55, FAHRENHEIT));
        assertThatItemHasState(CHANNEL_FAN_TIMER_ACTIVE, OFF);
        assertThatItemHasState(CHANNEL_FAN_TIMER_DURATION, new QuantityType<>(15, Units.MINUTE));
        assertThatItemHasState(CHANNEL_FAN_TIMER_TIMEOUT, parseDateTimeType("1970-01-01T00:00:00.000Z"));
        assertThatItemHasState(CHANNEL_HAS_FAN, ON);
        assertThatItemHasState(CHANNEL_HAS_LEAF, ON);
        assertThatItemHasState(CHANNEL_HUMIDITY, new QuantityType<>(25, Units.PERCENT));
        assertThatItemHasState(CHANNEL_LAST_CONNECTION, parseDateTimeType("2017-02-02T21:00:06.000Z"));
        assertThatItemHasState(CHANNEL_LOCKED, OFF);
        assertThatItemHasState(CHANNEL_LOCKED_MAX_SET_POINT, new QuantityType<>(72, FAHRENHEIT));
        assertThatItemHasState(CHANNEL_LOCKED_MIN_SET_POINT, new QuantityType<>(68, FAHRENHEIT));
        assertThatItemHasState(CHANNEL_MAX_SET_POINT, new QuantityType<>(75, FAHRENHEIT));
        assertThatItemHasState(CHANNEL_MIN_SET_POINT, new QuantityType<>(68, FAHRENHEIT));
        assertThatItemHasState(CHANNEL_MODE, new StringType("HEAT"));
        assertThatItemHasState(CHANNEL_PREVIOUS_MODE, new StringType("HEAT"));
        assertThatItemHasState(CHANNEL_SET_POINT, new QuantityType<>(60, FAHRENHEIT));
        assertThatItemHasState(CHANNEL_STATE, new StringType("OFF"));
        assertThatItemHasState(CHANNEL_SUNLIGHT_CORRECTION_ACTIVE, OFF);
        assertThatItemHasState(CHANNEL_SUNLIGHT_CORRECTION_ENABLED, ON);
        assertThatItemHasState(CHANNEL_TEMPERATURE, new QuantityType<>(66, FAHRENHEIT));
        assertThatItemHasState(CHANNEL_TIME_TO_TARGET, new QuantityType<>(0, Units.MINUTE));
        assertThatItemHasState(CHANNEL_USING_EMERGENCY_HEAT, OFF);

        assertThatAllItemStatesAreNotNull();
    }

    @Test
    public void incompleteThermostatUpdate() throws IOException {
        assertThat(thing.getChannels().size(), is(CHANNEL_COUNT));
        assertThat(thing.getStatus(), is(ThingStatus.OFFLINE));

        waitForAssert(() -> assertThat(bridge.getStatus(), is(ThingStatus.ONLINE)));
        putStreamingEventData(fromFile(COMPLETE_DATA_FILE_NAME));
        waitForAssert(() -> assertThat(thing.getStatus(), is(ThingStatus.ONLINE)));
        assertThatAllItemStatesAreNotNull();

        putStreamingEventData(fromFile(INCOMPLETE_DATA_FILE_NAME));
        waitForAssert(() -> assertThat(thing.getStatus(), is(ThingStatus.UNKNOWN)));
        assertThatAllItemStatesAreNull();
    }

    @Test
    public void thermostatGone() throws IOException {
        waitForAssert(() -> assertThat(bridge.getStatus(), is(ThingStatus.ONLINE)));
        putStreamingEventData(fromFile(COMPLETE_DATA_FILE_NAME));
        waitForAssert(() -> assertThat(thing.getStatus(), is(ThingStatus.ONLINE)));

        putStreamingEventData(fromFile(EMPTY_DATA_FILE_NAME));
        waitForAssert(() -> assertThat(thing.getStatus(), is(ThingStatus.OFFLINE)));
        assertThat(thing.getStatusInfo().getStatusDetail(), is(ThingStatusDetail.GONE));
    }

    @Test
    public void channelRefresh() throws IOException {
        waitForAssert(() -> assertThat(bridge.getStatus(), is(ThingStatus.ONLINE)));
        putStreamingEventData(fromFile(COMPLETE_DATA_FILE_NAME));
        waitForAssert(() -> assertThat(thing.getStatus(), is(ThingStatus.ONLINE)));
        assertThatAllItemStatesAreNotNull();

        updateAllItemStatesToNull();
        assertThatAllItemStatesAreNull();

        refreshAllChannels();
        assertThatAllItemStatesAreNotNull();
    }

    @Test
    public void handleFanTimerActiveCommands() throws IOException {
        handleCommand(CHANNEL_FAN_TIMER_ACTIVE, ON);
        assertNestApiPropertyState(THERMOSTAT1_DEVICE_ID, "fan_timer_active", "true");

        handleCommand(CHANNEL_FAN_TIMER_ACTIVE, OFF);
        assertNestApiPropertyState(THERMOSTAT1_DEVICE_ID, "fan_timer_active", "false");

        handleCommand(CHANNEL_FAN_TIMER_ACTIVE, ON);
        assertNestApiPropertyState(THERMOSTAT1_DEVICE_ID, "fan_timer_active", "true");
    }

    @Test
    public void handleFanTimerDurationCommands() throws IOException {
        int[] durations = { 15, 30, 45, 60, 120, 240, 480, 960, 15 };
        for (int duration : durations) {
            handleCommand(CHANNEL_FAN_TIMER_DURATION, new QuantityType<>(duration, Units.MINUTE));
            assertNestApiPropertyState(THERMOSTAT1_DEVICE_ID, "fan_timer_duration", String.valueOf(duration));
        }
    }

    @Test
    public void handleMaxSetPointCelsiusCommands() throws IOException {
        celsiusCommandsTest(CHANNEL_MAX_SET_POINT, "target_temperature_high_c");
    }

    @Test
    public void handleMaxSetPointFahrenheitCommands() throws IOException {
        fahrenheitCommandsTest(CHANNEL_MAX_SET_POINT, "target_temperature_high_f");
    }

    @Test
    public void handleMinSetPointCelsiusCommands() throws IOException {
        celsiusCommandsTest(CHANNEL_MIN_SET_POINT, "target_temperature_low_c");
    }

    @Test
    public void handleMinSetPointFahrenheitCommands() throws IOException {
        fahrenheitCommandsTest(CHANNEL_MIN_SET_POINT, "target_temperature_low_f");
    }

    @Test
    public void handleChannelModeCommands() throws IOException {
        handleCommand(CHANNEL_MODE, new StringType("HEAT"));
        assertNestApiPropertyState(THERMOSTAT1_DEVICE_ID, "hvac_mode", "heat");

        handleCommand(CHANNEL_MODE, new StringType("COOL"));
        assertNestApiPropertyState(THERMOSTAT1_DEVICE_ID, "hvac_mode", "cool");

        handleCommand(CHANNEL_MODE, new StringType("HEAT_COOL"));
        assertNestApiPropertyState(THERMOSTAT1_DEVICE_ID, "hvac_mode", "heat-cool");

        handleCommand(CHANNEL_MODE, new StringType("ECO"));
        assertNestApiPropertyState(THERMOSTAT1_DEVICE_ID, "hvac_mode", "eco");

        handleCommand(CHANNEL_MODE, new StringType("OFF"));
        assertNestApiPropertyState(THERMOSTAT1_DEVICE_ID, "hvac_mode", "off");

        handleCommand(CHANNEL_MODE, new StringType("HEAT"));
        assertNestApiPropertyState(THERMOSTAT1_DEVICE_ID, "hvac_mode", "heat");
    }

    @Test
    public void handleSetPointCelsiusCommands() throws IOException {
        celsiusCommandsTest(CHANNEL_SET_POINT, "target_temperature_c");
    }

    @Test
    public void handleSetPointFahrenheitCommands() throws IOException {
        fahrenheitCommandsTest(CHANNEL_SET_POINT, "target_temperature_f");
    }

    private void celsiusCommandsTest(String channelId, String apiPropertyName) throws IOException {
        waitForAssert(() -> assertThat(bridge.getStatus(), is(ThingStatus.ONLINE)));
        putStreamingEventData(fromFile(COMPLETE_DATA_FILE_NAME, CELSIUS));
        waitForAssert(() -> assertThat(thing.getStatus(), is(ThingStatus.ONLINE)));

        handleCommand(channelId, new QuantityType<>(20, CELSIUS));
        assertNestApiPropertyState(THERMOSTAT1_DEVICE_ID, apiPropertyName, "20.0");

        handleCommand(channelId, new QuantityType<>(21.123, CELSIUS));
        assertNestApiPropertyState(THERMOSTAT1_DEVICE_ID, apiPropertyName, "21.0");

        handleCommand(channelId, new QuantityType<>(22.541, CELSIUS));
        assertNestApiPropertyState(THERMOSTAT1_DEVICE_ID, apiPropertyName, "22.5");

        handleCommand(channelId, new QuantityType<>(23.74, CELSIUS));
        assertNestApiPropertyState(THERMOSTAT1_DEVICE_ID, apiPropertyName, "23.5");

        handleCommand(channelId, new QuantityType<>(23.75, CELSIUS));
        assertNestApiPropertyState(THERMOSTAT1_DEVICE_ID, apiPropertyName, "24.0");

        handleCommand(channelId, new QuantityType<>(70, FAHRENHEIT));
        assertNestApiPropertyState(THERMOSTAT1_DEVICE_ID, apiPropertyName, "21.0");
    }

    private void fahrenheitCommandsTest(String channelId, String apiPropertyName) throws IOException {
        waitForAssert(() -> assertThat(bridge.getStatus(), is(ThingStatus.ONLINE)));
        putStreamingEventData(fromFile(COMPLETE_DATA_FILE_NAME, FAHRENHEIT));
        waitForAssert(() -> assertThat(thing.getStatus(), is(ThingStatus.ONLINE)));

        handleCommand(channelId, new QuantityType<>(70, FAHRENHEIT));
        assertNestApiPropertyState(THERMOSTAT1_DEVICE_ID, apiPropertyName, "70");

        handleCommand(channelId, new QuantityType<>(71.123, FAHRENHEIT));
        assertNestApiPropertyState(THERMOSTAT1_DEVICE_ID, apiPropertyName, "71");

        handleCommand(channelId, new QuantityType<>(71.541, FAHRENHEIT));
        assertNestApiPropertyState(THERMOSTAT1_DEVICE_ID, apiPropertyName, "72");

        handleCommand(channelId, new QuantityType<>(72.74, FAHRENHEIT));
        assertNestApiPropertyState(THERMOSTAT1_DEVICE_ID, apiPropertyName, "73");

        handleCommand(channelId, new QuantityType<>(73.75, FAHRENHEIT));
        assertNestApiPropertyState(THERMOSTAT1_DEVICE_ID, apiPropertyName, "74");

        handleCommand(channelId, new QuantityType<>(21, CELSIUS));
        assertNestApiPropertyState(THERMOSTAT1_DEVICE_ID, apiPropertyName, "70");
    }
}
