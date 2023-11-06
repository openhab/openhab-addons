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
package org.openhab.binding.dmx.internal.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.openhab.binding.dmx.internal.DmxBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dmx.test.AbstractDmxThingTestParent;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;

/**
 * Tests cases for {@link org.openhab.binding.dmx.internal.handler.TunableWhiteThingHandler}.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class TunableWhiteThingHandlerTest extends AbstractDmxThingTestParent {
    private static final String TEST_CHANNEL_CONFIG = "100/2";
    private static final int TEST_FADE_TIME = 1500;
    private static final ThingUID THING_UID_DIMMER = new ThingUID(THING_TYPE_TUNABLEWHITE, "testdimmer");
    private static final ChannelUID CHANNEL_UID_BRIGHTNESS = new ChannelUID(THING_UID_DIMMER, CHANNEL_BRIGHTNESS);
    private static final ChannelUID CHANNEL_UID_BRIGHTNESS_CW = new ChannelUID(THING_UID_DIMMER, CHANNEL_BRIGHTNESS_CW);
    private static final ChannelUID CHANNEL_UID_BRIGHTNESS_WW = new ChannelUID(THING_UID_DIMMER, CHANNEL_BRIGHTNESS_WW);
    private static final ChannelUID CHANNEL_UID_COLOR_TEMP = new ChannelUID(THING_UID_DIMMER,
            CHANNEL_COLOR_TEMPERATURE);

    private @NonNullByDefault({}) Map<String, Object> thingProperties;
    private @NonNullByDefault({}) Thing dimmerThing;
    private @NonNullByDefault({}) TunableWhiteThingHandler dimmerThingHandler;

    @BeforeEach
    public void setUp() {
        super.setup();

        thingProperties = new HashMap<>();
        thingProperties.put(CONFIG_DMX_ID, TEST_CHANNEL_CONFIG);
        thingProperties.put(CONFIG_DIMMER_FADE_TIME, TEST_FADE_TIME);
        thingProperties.put(CONFIG_DIMMER_TURNONVALUE, "127,127");
        thingProperties.put(CONFIG_DIMMER_DYNAMICTURNONVALUE, true);
        dimmerThing = ThingBuilder.create(THING_TYPE_TUNABLEWHITE, "testdimmer").withLabel("Dimmer Thing")
                .withBridge(bridge.getUID()).withConfiguration(new Configuration(thingProperties))
                .withChannel(ChannelBuilder.create(CHANNEL_UID_BRIGHTNESS, "Brightness")
                        .withType(BRIGHTNESS_CHANNEL_TYPEUID).build())
                .withChannel(ChannelBuilder.create(CHANNEL_UID_BRIGHTNESS_CW, "Brightness CW")
                        .withType(BRIGHTNESS_CHANNEL_TYPEUID).build())
                .withChannel(ChannelBuilder.create(CHANNEL_UID_BRIGHTNESS_WW, "Brightness WW")
                        .withType(BRIGHTNESS_CHANNEL_TYPEUID).build())
                .withChannel(ChannelBuilder.create(CHANNEL_UID_COLOR_TEMP, "Color temperature")
                        .withType(COLOR_TEMPERATURE_CHANNEL_TYPEUID).build())
                .build();
        dimmerThingHandler = new TunableWhiteThingHandler(dimmerThing) {
            @Override
            protected @Nullable Bridge getBridge() {
                return bridge;
            }
        };
        initializeHandler(dimmerThingHandler);
    }

    @Test
    public void testThingStatus() {
        assertThingStatus(dimmerThing);
    }

    @Test
    public void testThingStatusNoBridge() {
        // check that thing is offline if no bridge found
        TunableWhiteThingHandler dimmerHandlerWithoutBridge = new TunableWhiteThingHandler(dimmerThing) {
            @Override
            protected @Nullable Bridge getBridge() {
                return null;
            }
        };
        assertThingStatusWithoutBridge(dimmerHandlerWithoutBridge);
    }

    @Test
    public void testOnOffCommand() {
        // on
        long currentTime = System.currentTimeMillis();

        dimmerThingHandler.handleCommand(CHANNEL_UID_BRIGHTNESS, OnOffType.ON);
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, TEST_FADE_TIME);

        waitForAssert(() -> {
            assertChannelStateUpdate(CHANNEL_UID_BRIGHTNESS,
                    state -> assertEquals(OnOffType.ON, state.as(OnOffType.class)));
            assertChannelStateUpdate(CHANNEL_UID_BRIGHTNESS_CW,
                    state -> assertThat(((PercentType) state).doubleValue(), is(closeTo(50, 0.5))));
            assertChannelStateUpdate(CHANNEL_UID_BRIGHTNESS_WW,
                    state -> assertThat(((PercentType) state).doubleValue(), is(closeTo(50, 0.5))));
        });

        // off
        dimmerThingHandler.handleCommand(CHANNEL_UID_BRIGHTNESS, OnOffType.OFF);
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, TEST_FADE_TIME);

        waitForAssert(() -> {
            assertChannelStateUpdate(CHANNEL_UID_BRIGHTNESS,
                    state -> assertEquals(OnOffType.OFF, state.as(OnOffType.class)));
            assertChannelStateUpdate(CHANNEL_UID_BRIGHTNESS_CW, state -> assertEquals(PercentType.ZERO, state));
            assertChannelStateUpdate(CHANNEL_UID_BRIGHTNESS_WW, state -> assertEquals(PercentType.ZERO, state));
        });
    }

    @Test
    public void testDynamicTurnOnValue() {
        long currentTime = System.currentTimeMillis();
        int testValue = 75;

        // turn on with arbitrary value
        dimmerThingHandler.handleCommand(CHANNEL_UID_BRIGHTNESS, new PercentType(testValue));
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, TEST_FADE_TIME);

        waitForAssert(() -> {
            assertChannelStateUpdate(CHANNEL_UID_BRIGHTNESS,
                    state -> assertThat(((PercentType) state).doubleValue(), is(closeTo(testValue, 1.0))));
        });

        // turn off and hopefully store last value
        dimmerThingHandler.handleCommand(CHANNEL_UID_BRIGHTNESS, OnOffType.OFF);
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, TEST_FADE_TIME);

        waitForAssert(() -> {
            assertChannelStateUpdate(CHANNEL_UID_BRIGHTNESS,
                    state -> assertEquals(OnOffType.OFF, state.as(OnOffType.class)));
        });

        // turn on and restore value
        dimmerThingHandler.handleCommand(CHANNEL_UID_BRIGHTNESS, OnOffType.ON);
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, TEST_FADE_TIME);

        waitForAssert(() -> {
            assertChannelStateUpdate(CHANNEL_UID_BRIGHTNESS,
                    state -> assertThat(((PercentType) state).doubleValue(), is(closeTo(testValue, 1.0))));
        });
    }

    @Test
    public void testPercentTypeCommand() {
        assertPercentTypeCommands(dimmerThingHandler, CHANNEL_UID_BRIGHTNESS, TEST_FADE_TIME);
    }

    @Test
    public void testColorTemperature() {
        long currentTime = System.currentTimeMillis();

        dimmerThingHandler.handleCommand(CHANNEL_UID_BRIGHTNESS, OnOffType.ON);
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, TEST_FADE_TIME);

        waitForAssert(() -> {
            assertChannelStateUpdate(CHANNEL_UID_BRIGHTNESS,
                    state -> assertThat(((PercentType) state).doubleValue(), is(closeTo(100.0, 0.5))));
            assertChannelStateUpdate(CHANNEL_UID_COLOR_TEMP,
                    state -> assertThat(((PercentType) state).doubleValue(), is(closeTo(50.0, 0.5))));
        });

        // cool white
        dimmerThingHandler.handleCommand(CHANNEL_UID_COLOR_TEMP, PercentType.ZERO);
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, TEST_FADE_TIME);

        waitForAssert(() -> {
            assertChannelStateUpdate(CHANNEL_UID_COLOR_TEMP,
                    state -> assertThat(((PercentType) state).doubleValue(), is(closeTo(0.0, 0.1))));
            assertChannelStateUpdate(CHANNEL_UID_BRIGHTNESS,
                    state -> assertThat(((PercentType) state).doubleValue(), is(closeTo(100.0, 0.5))));
            assertChannelStateUpdate(CHANNEL_UID_BRIGHTNESS_CW,
                    state -> assertThat(((PercentType) state).doubleValue(), is(closeTo(100.0, 0.5))));
            assertChannelStateUpdate(CHANNEL_UID_BRIGHTNESS_WW,
                    state -> assertThat(((PercentType) state).doubleValue(), is(closeTo(0.0, 0.5))));
        });

        // warm white
        dimmerThingHandler.handleCommand(CHANNEL_UID_COLOR_TEMP, PercentType.HUNDRED);
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, TEST_FADE_TIME);

        waitForAssert(() -> {
            assertChannelStateUpdate(CHANNEL_UID_COLOR_TEMP,
                    state -> assertThat(((PercentType) state).doubleValue(), is(closeTo(100.0, 0.1))));
            assertChannelStateUpdate(CHANNEL_UID_BRIGHTNESS,
                    state -> assertThat(((PercentType) state).doubleValue(), is(closeTo(100.0, 0.5))));
            assertChannelStateUpdate(CHANNEL_UID_BRIGHTNESS_CW,
                    state -> assertThat(((PercentType) state).doubleValue(), is(closeTo(0.0, 0.5))));
            assertChannelStateUpdate(CHANNEL_UID_BRIGHTNESS_WW,
                    state -> assertThat(((PercentType) state).doubleValue(), is(closeTo(100.0, 0.5))));
        });

        // intermediate white
        dimmerThingHandler.handleCommand(CHANNEL_UID_COLOR_TEMP, new PercentType(75));
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, TEST_FADE_TIME);

        waitForAssert(() -> {
            assertChannelStateUpdate(CHANNEL_UID_COLOR_TEMP,
                    state -> assertThat(((PercentType) state).doubleValue(), is(closeTo(75.0, 0.1))));
            assertChannelStateUpdate(CHANNEL_UID_BRIGHTNESS,
                    state -> assertThat(((PercentType) state).doubleValue(), is(closeTo(100.0, 1.0))));
            assertChannelStateUpdate(CHANNEL_UID_BRIGHTNESS_CW,
                    state -> assertThat(((PercentType) state).doubleValue(), is(closeTo(25.0, 0.5))));
            assertChannelStateUpdate(CHANNEL_UID_BRIGHTNESS_WW,
                    state -> assertThat(((PercentType) state).doubleValue(), is(closeTo(75.0, 0.5))));
        });
    }
}
