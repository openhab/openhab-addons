/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import static org.openhab.core.library.types.OnOffType.OFF;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.openhab.binding.nest.internal.wwn.config.WWNDeviceConfiguration;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ThingBuilder;

/**
 * Tests for {@link WWNSmokeDetectorHandler}.
 *
 * @author Wouter Born - Initial contribution
 */
public class WWNSmokeDetectorHandlerTest extends WWNThingHandlerOSGiTest {

    private static final ThingUID SMOKE_DETECTOR_UID = new ThingUID(THING_TYPE_SMOKE_DETECTOR, "smoke1");
    private static final int CHANNEL_COUNT = 7;

    public WWNSmokeDetectorHandlerTest() {
        super(WWNSmokeDetectorHandler.class);
    }

    @Override
    protected Thing buildThing(Bridge bridge) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(WWNDeviceConfiguration.DEVICE_ID, SMOKE1_DEVICE_ID);

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
