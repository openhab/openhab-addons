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
package org.openhab.binding.dmx.internal.handler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.openhab.binding.dmx.internal.DmxBindingConstants.*;
import static org.openhab.binding.dmx.test.TestBridgeHandler.THING_TYPE_TEST_BRIDGE;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dmx.test.AbstractDmxThingTestParent;
import org.openhab.binding.dmx.test.TestBridgeHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;

/**
 * Tests cases for {@link org.openhab.binding.dmx.internal.handler.ChaserThingHandler}.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class ChaserThingHandlerTest extends AbstractDmxThingTestParent {

    private static final String TEST_CHANNEL = "100";
    private static final String TEST_STEPS_INFINITE = "1000:100:1000|1000:200:-1";
    private static final String TEST_STEPS_REPEAT = "1000:115:1000|1000:210:1000";
    private static final ThingUID THING_UID_CHASER = new ThingUID(THING_TYPE_CHASER, "testchaser");
    private static final ChannelUID CHANNEL_UID_SWITCH = new ChannelUID(THING_UID_CHASER, CHANNEL_SWITCH);

    private @NonNullByDefault({}) Map<String, Object> bridgeProperties;
    private @NonNullByDefault({}) Map<String, Object> thingProperties;
    private @NonNullByDefault({}) Thing chaserThing;
    private @NonNullByDefault({}) TestBridgeHandler dmxBridgeHandler;
    private @NonNullByDefault({}) ChaserThingHandler chaserThingHandler;

    @BeforeEach
    public void setUp() {
        super.setup();

        thingProperties = new HashMap<>();
        thingProperties.put(CONFIG_DMX_ID, TEST_CHANNEL);
    }

    @Test
    public void testThingStatus() {
        thingProperties.put(CONFIG_CHASER_STEPS, TEST_STEPS_INFINITE);
        initialize();
        assertThingStatus(chaserThing);
    }

    @Test
    public void testThingStatusNoBridge() {
        thingProperties.put(CONFIG_CHASER_STEPS, TEST_STEPS_INFINITE);
        initialize();
        // check that thing is offline if no bridge found
        ChaserThingHandler chaserHandlerWithoutBridge = new ChaserThingHandler(chaserThing) {
            @Override
            protected @Nullable Bridge getBridge() {
                return null;
            }
        };
        assertThingStatusWithoutBridge(chaserHandlerWithoutBridge);
    }

    @Test
    public void holdInfiniteChaser() {
        initializeTestBridge();
        thingProperties.put(CONFIG_CHASER_STEPS, TEST_STEPS_INFINITE);
        initialize();

        long currentTime = System.currentTimeMillis();

        chaserThingHandler.handleCommand(new ChannelUID(chaserThing.getUID(), CHANNEL_SWITCH), OnOffType.ON);
        // step I
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, 1000);
        waitForAssert(() -> assertThat(dmxBridgeHandler.getDmxChannelValue(100), is(equalTo(100))));
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, 1000);
        waitForAssert(() -> assertThat(dmxBridgeHandler.getDmxChannelValue(100), is(equalTo(100))));
        // step II (holds forever)
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, 1000);
        waitForAssert(() -> assertThat(dmxBridgeHandler.getDmxChannelValue(100), is(equalTo(200))));
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, 2000);
        waitForAssert(() -> assertThat(dmxBridgeHandler.getDmxChannelValue(100), is(equalTo(200))));
    }

    @Test
    public void runningChaser() {
        initializeTestBridge();
        thingProperties.put(CONFIG_CHASER_STEPS, TEST_STEPS_REPEAT);
        initialize();

        long currentTime = System.currentTimeMillis();

        chaserThingHandler.handleCommand(new ChannelUID(chaserThing.getUID(), CHANNEL_SWITCH), OnOffType.ON);
        // step I
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, 1000);
        waitForAssert(() -> assertThat(dmxBridgeHandler.getDmxChannelValue(100), is(equalTo(115))));
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, 1000);
        waitForAssert(() -> assertThat(dmxBridgeHandler.getDmxChannelValue(100), is(equalTo(115))));
        // step II
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, 1000);
        waitForAssert(() -> assertThat(dmxBridgeHandler.getDmxChannelValue(100), is(equalTo(210))));
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, 1000);
        waitForAssert(() -> assertThat(dmxBridgeHandler.getDmxChannelValue(100), is(equalTo(210))));
        // step I (repeated)
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, 1000);
        waitForAssert(() -> assertThat(dmxBridgeHandler.getDmxChannelValue(100), is(equalTo(115))));
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, 1000);
        waitForAssert(() -> assertThat(dmxBridgeHandler.getDmxChannelValue(100), is(equalTo(115))));
    }

    @Test
    public void runningChaserWithResume() {
        initializeTestBridge();
        thingProperties.put(CONFIG_CHASER_STEPS, TEST_STEPS_REPEAT);
        thingProperties.put(CONFIG_CHASER_RESUME_AFTER, true);
        initialize();

        dmxBridgeHandler.setDmxChannelValue(100, 193);

        long currentTime = System.currentTimeMillis();
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, 0);

        waitForAssert(() -> assertThat(dmxBridgeHandler.getDmxChannelValue(100), is(equalTo(193))));

        chaserThingHandler.handleCommand(new ChannelUID(chaserThing.getUID(), CHANNEL_SWITCH), OnOffType.ON);

        // step I
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, 1000);
        waitForAssert(() -> assertThat(dmxBridgeHandler.getDmxChannelValue(100), is(equalTo(115))));
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, 1000);
        waitForAssert(() -> assertThat(dmxBridgeHandler.getDmxChannelValue(100), is(equalTo(115))));
        // step II
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, 1000);
        waitForAssert(() -> assertThat(dmxBridgeHandler.getDmxChannelValue(100), is(equalTo(210))));
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, 1000);
        waitForAssert(() -> assertThat(dmxBridgeHandler.getDmxChannelValue(100), is(equalTo(210))));
        // resume old state
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, 1000);
        waitForAssert(() -> assertThat(dmxBridgeHandler.getDmxChannelValue(100), is(equalTo(193))));
    }

    private void initialize() {
        chaserThing = ThingBuilder.create(THING_TYPE_CHASER, "testchaser").withLabel("Chaser Thing")
                .withBridge(bridge.getUID()).withConfiguration(new Configuration(thingProperties))
                .withChannel(
                        ChannelBuilder.create(CHANNEL_UID_SWITCH, "Switch").withType(SWITCH_CHANNEL_TYPEUID).build())
                .build();

        chaserThingHandler = new ChaserThingHandler(chaserThing) {
            @Override
            protected @Nullable Bridge getBridge() {
                return bridge;
            }
        };
        initializeHandler(chaserThingHandler);
    }

    private void initializeTestBridge() {
        bridgeProperties = new HashMap<>();
        bridge = BridgeBuilder.create(THING_TYPE_TEST_BRIDGE, "testbridge").withLabel("Test Bridge")
                .withConfiguration(new Configuration(bridgeProperties)).build();

        dmxBridgeHandler = new TestBridgeHandler(bridge);
        bridge.setHandler(dmxBridgeHandler);
        ThingHandlerCallback bridgeHandler = mock(ThingHandlerCallback.class);
        doAnswer(answer -> {
            ((Thing) answer.getArgument(0)).setStatusInfo(answer.getArgument(1));
            return null;
        }).when(bridgeHandler).statusUpdated(any(), any());
        dmxBridgeHandler.setCallback(bridgeHandler);
        dmxBridgeHandler.initialize();
    }
}
