/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.handler;

import static org.eclipse.smarthome.core.library.types.OnOffType.*;
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
 * Tests for {@link NestCameraHandler}.
 *
 * @author Wouter Born - Increase test coverage
 */
public class NestCameraHandlerTest extends NestThingHandlerOSGiTest {

    private static final ThingUID CAMERA_UID = new ThingUID(THING_TYPE_CAMERA, "camera1");
    private static final int CHANNEL_COUNT = 9;

    public NestCameraHandlerTest() {
        super(NestCameraHandler.class);
    }

    @Override
    protected Thing buildThing(Bridge bridge) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(NestDeviceConfiguration.DEVICE_ID, CAMERA1_DEVICE_ID);

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

        assertThatItemHasState(CHANNEL_APP_URL, new StringType(
                "nestmobile://cameras/CjZfTEs4ajlyUlh3Q0tFQk90RG83SnNrTnh6V2ZIQk9JbTNDTG91Q1QzRlFaenJ2b2tLX0R6RlESFm9wNVB2NW93NmJ6cUdvMkZQSGUxdEEaNld0Mkl5b2tIR0tKX2FpUVd1SkRnQjc2ejhSWFl3SFFxWXFrSWx2QlpxN1gyeWNqdmRZVjdGQQ?auth=c.eQ5QBBPiFOTNzPHbmZPcE9yPZ7GayzLusifgQR2DQRFNyUS9ESvlhJF0D7vG8Y0TFV39zX1vIOsWrv8RKCMrFepNUb9FqHEboa4MtWLUsGb4tD9oBh0jrV4HooJUmz5sVA5KZR0dkxyLYyPc"));
        assertThatItemHasState(CHANNEL_AUDIO_INPUT_ENABLED, ON);
        assertThatItemHasState(CHANNEL_LAST_ONLINE_CHANGE, parseDateTimeType("2017-01-22T08:19:20.000Z"));
        assertThatItemHasState(CHANNEL_PUBLIC_SHARE_ENABLED, OFF);
        assertThatItemHasState(CHANNEL_PUBLIC_SHARE_URL, new StringType("https://video.nest.com/live/Ya2NJ8GQRE"));
        assertThatItemHasState(CHANNEL_SNAPSHOT_URL, new StringType(
                "https://www.dropcam.com/api/wwn.get_snapshot/CjZfTEs4ajlyUlh3Q0tFQk90RG83SnNrTnh6V2ZIQk9JbTNDTG91Q1QzRlFaenJ2b2tLX0R6RlESFm9wNVB2NW93NmJ6cUdvMkZQSGUxdEEaNld0Mkl5b2tIR0tKX2FpUVd1SkRnQjc2ejhSWFl3SFFxWXFrSWx2QlpxN1gyeWNqdmRZVjdGQQ?auth=c.eQ5QBBPiFOTNzPHbmZPcE9yPZ7GayzLusifgQR2DQRFNyUS9ESvlhJF0D7vG8Y0TFV39zX1vIOsWrv8RKCMrFepNUb9FqHEboa4MtWLUsGb4tD9oBh0jrV4HooJUmz5sVA5KZR0dkxyLYyPc"));
        assertThatItemHasState(CHANNEL_STREAMING, OFF);
        assertThatItemHasState(CHANNEL_VIDEO_HISTORY_ENABLED, OFF);
        assertThatItemHasState(CHANNEL_WEB_URL, new StringType(
                "https://home.nest.com/cameras/CjZfTEs4ajlyUlh3Q0tFQk90RG83SnNrTnh6V2ZIQk9JbTNDTG91Q1QzRlFaenJ2b2tLX0R6RlESFm9wNVB2NW93NmJ6cUdvMkZQSGUxdEEaNld0Mkl5b2tIR0tKX2FpUVd1SkRnQjc2ejhSWFl3SFFxWXFrSWx2QlpxN1gyeWNqdmRZVjdGQQ?auth=c.eQ5QBBPiFOTNzPHbmZPcE9yPZ7GayzLusifgQR2DQRFNyUS9ESvlhJF0D7vG8Y0TFV39zX1vIOsWrv8RKCMrFepNUb9FqHEboa4MtWLUsGb4tD9oBh0jrV4HooJUmz5sVA5KZR0dkxyLYyPc"));

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
        handleCommand(CHANNEL_STREAMING, ON);
        assertNestApiPropertyState(CAMERA1_DEVICE_ID, "is_streaming", "true");

        handleCommand(CHANNEL_STREAMING, OFF);
        assertNestApiPropertyState(CAMERA1_DEVICE_ID, "is_streaming", "false");

        handleCommand(CHANNEL_STREAMING, ON);
        assertNestApiPropertyState(CAMERA1_DEVICE_ID, "is_streaming", "true");
    }
}
