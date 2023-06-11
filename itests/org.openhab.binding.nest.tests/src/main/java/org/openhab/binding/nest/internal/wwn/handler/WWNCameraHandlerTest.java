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
import static org.openhab.core.library.types.OnOffType.*;

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
 * Tests for {@link WWNCameraHandler}.
 *
 * @author Wouter Born - Initial contribution
 */
public class WWNCameraHandlerTest extends WWNThingHandlerOSGiTest {

    private static final ThingUID CAMERA_UID = new ThingUID(THING_TYPE_CAMERA, "camera1");
    private static final int CHANNEL_COUNT = 20;

    public WWNCameraHandlerTest() {
        super(WWNCameraHandler.class);
    }

    @Override
    protected Thing buildThing(Bridge bridge) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(WWNDeviceConfiguration.DEVICE_ID, CAMERA1_DEVICE_ID);

        return ThingBuilder.create(THING_TYPE_CAMERA, CAMERA_UID).withLabel("Test Camera").withBridge(bridge.getUID())
                .withChannels(buildChannels(THING_TYPE_CAMERA, CAMERA_UID))
                .withConfiguration(new Configuration(properties)).build();
    }

    @Test
    public void completeCameraUpdate() throws IOException {
        assertThat(thing.getChannels().size(), is(CHANNEL_COUNT));
        assertThat(thing.getStatus(), is(ThingStatus.OFFLINE));

        waitForAssert(() -> assertThat(bridge.getStatus(), is(ThingStatus.ONLINE)));
        putStreamingEventData(fromFile(COMPLETE_DATA_FILE_NAME));
        waitForAssert(() -> assertThat(thing.getStatus(), is(ThingStatus.ONLINE)));

        // Camera channel group
        assertThatItemHasState(CHANNEL_CAMERA_APP_URL, new StringType("https://camera_app_url"));
        assertThatItemHasState(CHANNEL_CAMERA_AUDIO_INPUT_ENABLED, ON);
        assertThatItemHasState(CHANNEL_CAMERA_LAST_ONLINE_CHANGE, parseDateTimeType("2017-01-22T08:19:20.000Z"));
        assertThatItemHasState(CHANNEL_CAMERA_PUBLIC_SHARE_ENABLED, OFF);
        assertThatItemHasState(CHANNEL_CAMERA_PUBLIC_SHARE_URL, new StringType("https://camera_public_share_url"));
        assertThatItemHasState(CHANNEL_CAMERA_SNAPSHOT_URL, new StringType("https://camera_snapshot_url"));
        assertThatItemHasState(CHANNEL_CAMERA_STREAMING, OFF);
        assertThatItemHasState(CHANNEL_CAMERA_VIDEO_HISTORY_ENABLED, OFF);
        assertThatItemHasState(CHANNEL_CAMERA_WEB_URL, new StringType("https://camera_web_url"));

        // Last event channel group
        assertThatItemHasState(CHANNEL_LAST_EVENT_ACTIVITY_ZONES, new StringType("id1,id2"));
        assertThatItemHasState(CHANNEL_LAST_EVENT_ANIMATED_IMAGE_URL,
                new StringType("https://last_event_animated_image_url"));
        assertThatItemHasState(CHANNEL_LAST_EVENT_APP_URL, new StringType("https://last_event_app_url"));
        assertThatItemHasState(CHANNEL_LAST_EVENT_END_TIME, parseDateTimeType("2017-01-22T07:40:38.680Z"));
        assertThatItemHasState(CHANNEL_LAST_EVENT_HAS_MOTION, ON);
        assertThatItemHasState(CHANNEL_LAST_EVENT_HAS_PERSON, OFF);
        assertThatItemHasState(CHANNEL_LAST_EVENT_HAS_SOUND, OFF);
        assertThatItemHasState(CHANNEL_LAST_EVENT_IMAGE_URL, new StringType("https://last_event_image_url"));
        assertThatItemHasState(CHANNEL_LAST_EVENT_START_TIME, parseDateTimeType("2017-01-22T07:40:19.020Z"));
        assertThatItemHasState(CHANNEL_LAST_EVENT_URLS_EXPIRE_TIME, parseDateTimeType("2017-02-05T07:40:19.020Z"));
        assertThatItemHasState(CHANNEL_LAST_EVENT_WEB_URL, new StringType("https://last_event_web_url"));

        assertThatAllItemStatesAreNotNull();
    }

    @Test
    public void incompleteCameraUpdate() throws IOException {
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
    public void cameraGone() throws IOException {
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
    public void handleStreamingCommands() throws IOException {
        handleCommand(CHANNEL_CAMERA_STREAMING, ON);
        assertNestApiPropertyState(CAMERA1_DEVICE_ID, "is_streaming", "true");

        handleCommand(CHANNEL_CAMERA_STREAMING, OFF);
        assertNestApiPropertyState(CAMERA1_DEVICE_ID, "is_streaming", "false");

        handleCommand(CHANNEL_CAMERA_STREAMING, ON);
        assertNestApiPropertyState(CAMERA1_DEVICE_ID, "is_streaming", "true");
    }
}
