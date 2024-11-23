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
package org.openhab.binding.deconz.internal.handler;

import static org.openhab.binding.deconz.internal.BindingConstants.*;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.deconz.internal.Util;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.UnDefType;

/**
 * The {@link SensorThingHandlerTest} contains test classes for the {@link SensorThingHandler}
 *
 * @author Jan N. Klug - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class SensorThingHandlerTest extends BaseDeconzThingHandlerTest {

    @Test
    public void testAirQuality() throws IOException {
        createThing(THING_TYPE_AIRQUALITY_SENSOR,
                List.of(CHANNEL_AIRQUALITY, CHANNEL_AIRQUALITYPPB, CHANNEL_LAST_UPDATED), SensorThingHandler::new);

        Set<TestParam> expected = Set.of(
                // standard channels
                new TestParam(CHANNEL_AIRQUALITY, new StringType("good")),
                new TestParam(CHANNEL_AIRQUALITYPPB, new QuantityType<>("129 ppb")),
                new TestParam(CHANNEL_LAST_UPDATED, Util.convertTimestampToDateTime("2021-12-29T01:18:41.184")),
                // battery
                new TestParam(CHANNEL_BATTERY_LEVEL, new DecimalType(100)),
                new TestParam(CHANNEL_BATTERY_LOW, OnOffType.OFF),
                // last seen
                new TestParam(CHANNEL_LAST_SEEN, Util.convertTimestampToDateTime("2021-12-29T01:18Z")));

        assertThing("json/sensors/airquality.json", expected);
    }

    @Test
    public void testCarbonMonoxide() throws IOException {
        createThing(THING_TYPE_CARBONMONOXIDE_SENSOR, List.of(CHANNEL_CARBONMONOXIDE, CHANNEL_LAST_UPDATED),
                SensorThingHandler::new);

        Set<TestParam> expected = Set.of(
                // standard channels
                new TestParam(CHANNEL_CARBONMONOXIDE, OnOffType.ON),
                new TestParam(CHANNEL_LAST_UPDATED, UnDefType.UNDEF),
                // battery
                new TestParam(CHANNEL_BATTERY_LEVEL, new DecimalType(100)),
                new TestParam(CHANNEL_BATTERY_LOW, OnOffType.OFF));

        assertThing("json/sensors/carbonmonoxide.json", expected);
    }

    @Test
    public void testFire() throws IOException {
        createThing(THING_TYPE_FIRE_SENSOR, List.of(CHANNEL_FIRE, CHANNEL_LAST_UPDATED), SensorThingHandler::new);

        Set<TestParam> expected = Set.of(
                // standard channels
                new TestParam(CHANNEL_FIRE, OnOffType.OFF), new TestParam(CHANNEL_LAST_UPDATED, UnDefType.UNDEF),
                // battery
                new TestParam(CHANNEL_BATTERY_LEVEL, new DecimalType(98)),
                new TestParam(CHANNEL_BATTERY_LOW, OnOffType.OFF));

        assertThing("json/sensors/fire.json", expected);
    }

    @Test
    public void testSwitch() throws IOException {
        createThing(THING_TYPE_SWITCH, List.of(CHANNEL_BUTTON, CHANNEL_BUTTONEVENT, CHANNEL_LAST_UPDATED),
                SensorThingHandler::new);

        Set<TestParam> expected = Set.of(
                // standard channels
                new TestParam(CHANNEL_BUTTON, new DecimalType(1002)), new TestParam(CHANNEL_BUTTONEVENT, null),
                new TestParam(CHANNEL_LAST_UPDATED, Util.convertTimestampToDateTime("2022-02-15T13:36:16.271")),
                // battery
                new TestParam(CHANNEL_BATTERY_LEVEL, new DecimalType(7)),
                new TestParam(CHANNEL_BATTERY_LOW, OnOffType.ON));

        assertThing("json/sensors/switch.json", expected);
    }

    @Test
    public void testVibration() throws IOException {
        createThing(THING_TYPE_VIBRATION_SENSOR, List.of(CHANNEL_LAST_UPDATED, CHANNEL_VIBRATION),
                SensorThingHandler::new);

        Set<TestParam> expected = Set.of(
                // standard channels
                new TestParam(CHANNEL_LAST_UPDATED, Util.convertTimestampToDateTime("2022-09-09T18:13:44.653")),
                new TestParam(CHANNEL_VIBRATION, OnOffType.ON),
                // battery
                new TestParam(CHANNEL_BATTERY_LEVEL, new DecimalType(100)),
                new TestParam(CHANNEL_BATTERY_LOW, OnOffType.OFF),
                // last seen
                new TestParam(CHANNEL_LAST_SEEN, Util.convertTimestampToDateTime("2022-09-09T18:13Z")),
                // dynamic channels
                new TestParam(CHANNEL_ORIENTATION_X, new DecimalType(3)),
                new TestParam(CHANNEL_ORIENTATION_Y, new DecimalType(-1)),
                new TestParam(CHANNEL_ORIENTATION_Z, new DecimalType(-87)),
                new TestParam(CHANNEL_TEMPERATURE, new QuantityType<>("26.00 °C")),
                new TestParam(CHANNEL_TILTANGLE, new QuantityType<>("176 °")),
                new TestParam(CHANNEL_VIBRATION_STRENGTH, new DecimalType(110)));

        assertThing("json/sensors/vibration.json", expected);
    }
}
