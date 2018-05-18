/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.handler;

import static org.eclipse.smarthome.core.library.types.OnOffType.OFF;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.openhab.binding.nest.NestBindingConstants.*;
import static org.openhab.binding.nest.internal.data.NestDataUtil.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.junit.Test;
import org.openhab.binding.nest.internal.config.NestDeviceConfiguration;

/**
 * Tests for {@link NestSmokeDetectorHandler}.
 *
 * @author Wouter Born - Increase test coverage
 */
public class NestSmokeDetectorHandlerTest extends NestThingHandlerOSGiTest {

    private static final ThingUID SMOKE_DETECTOR_UID = new ThingUID(THING_TYPE_SMOKE_DETECTOR, "smoke1");
    private static final int CHANNEL_COUNT = 7;

    public NestSmokeDetectorHandlerTest() {
        super(NestSmokeDetectorHandler.class);
    }

    @Override
    protected Thing buildThing(Bridge bridge) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(NestDeviceConfiguration.DEVICE_ID, SMOKE1_DEVICE_ID);

        return ThingBuilder.create(THING_TYPE_SMOKE_DETECTOR, SMOKE_DETECTOR_UID).withLabel("Test Smoke Detector")
                .withBridge(bridge.getUID()).withChannels(buildChannels(THING_TYPE_SMOKE_DETECTOR, SMOKE_DETECTOR_UID))
                .withConfiguration(new Configuration(properties)).build();
    }

    @Test
    public void completeSmokeDetectorUpdate() throws IOException {
        assertThat(thing.getChannels().size(), is(CHANNEL_COUNT));
        assertThat(thing.getStatus(), is(ThingStatus.OFFLINE));

        waitForAssert(() -> assertThat(bridge.getStatus(), is(ThingStatus.ONLINE)));
        putStreamingEventData(fromFile(COMPLETE_DATA_FILE_NAME));
        waitForAssert(() -> assertThat(thing.getStatus(), is(ThingStatus.ONLINE)));

        assertThatItemHasState(CHANNEL_CO_ALARM_STATE, new StringType("OK"));
        assertThatItemHasState(CHANNEL_LAST_CONNECTION, parseDateTimeType("2017-02-02T20:53:05.338Z"));
        assertThatItemHasState(CHANNEL_LAST_MANUAL_TEST_TIME, parseDateTimeType("2016-10-31T23:59:59.000Z"));
        assertThatItemHasState(CHANNEL_LOW_BATTERY, OFF);
        assertThatItemHasState(CHANNEL_MANUAL_TEST_ACTIVE, OFF);
        assertThatItemHasState(CHANNEL_SMOKE_ALARM_STATE, new StringType("OK"));
        assertThatItemHasState(CHANNEL_UI_COLOR_STATE, new StringType("GREEN"));

        assertThatAllItemStatesAreNotNull();
    }

    @Test
    public void incompleteSmokeDetectorUpdate() throws IOException {
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
    public void smokeDetectorGone() throws IOException {
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
}
