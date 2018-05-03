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
import org.openhab.binding.nest.internal.config.NestStructureConfiguration;

/**
 * Tests for {@link NestStructureHandler}.
 *
 * @author Wouter Born - Increase test coverage
 */
public class NestStructureHandlerTest extends NestThingHandlerOSGiTest {

    private static final ThingUID STRUCTURE_UID = new ThingUID(THING_TYPE_STRUCTURE, "structure1");
    private static final int CHANNEL_COUNT = 11;

    public NestStructureHandlerTest() {
        super(NestStructureHandler.class);
    }

    @Override
    protected Thing buildThing(Bridge bridge) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(NestStructureConfiguration.STRUCTURE_ID, STRUCTURE1_STRUCTURE_ID);

        return ThingBuilder.create(THING_TYPE_STRUCTURE, STRUCTURE_UID).withLabel("Test Structure")
                .withBridge(bridge.getUID()).withChannels(buildChannels(THING_TYPE_STRUCTURE, STRUCTURE_UID))
                .withConfiguration(new Configuration(properties)).build();
    }

    @Test
    public void completeStructureUpdate() throws IOException {
        assertThat(thing.getChannels().size(), is(CHANNEL_COUNT));
        assertThat(thing.getStatus(), is(ThingStatus.OFFLINE));

        waitForAssert(() -> assertThat(bridge.getStatus(), is(ThingStatus.ONLINE)));
        putStreamingEventData(fromFile(COMPLETE_DATA_FILE_NAME));
        waitForAssert(() -> assertThat(thing.getStatus(), is(ThingStatus.ONLINE)));

        assertThatItemHasState(CHANNEL_AWAY, new StringType("HOME"));
        assertThatItemHasState(CHANNEL_CO_ALARM_STATE, new StringType("OK"));
        assertThatItemHasState(CHANNEL_COUNTRY_CODE, new StringType("US"));
        assertThatItemHasState(CHANNEL_ETA_BEGIN, parseDateTimeType("2017-02-02T03:10:08.000Z"));
        assertThatItemHasState(CHANNEL_PEAK_PERIOD_END_TIME, parseDateTimeType("2017-07-01T01:03:08.400Z"));
        assertThatItemHasState(CHANNEL_PEAK_PERIOD_START_TIME, parseDateTimeType("2017-06-01T13:31:10.870Z"));
        assertThatItemHasState(CHANNEL_POSTAL_CODE, new StringType("98056"));
        assertThatItemHasState(CHANNEL_RUSH_HOUR_REWARDS_ENROLLMENT, OFF);
        assertThatItemHasState(CHANNEL_SECURITY_STATE, new StringType("OK"));
        assertThatItemHasState(CHANNEL_SMOKE_ALARM_STATE, new StringType("OK"));
        assertThatItemHasState(CHANNEL_TIME_ZONE, new StringType("America/Los_Angeles"));

        assertThatAllItemStatesAreNotNull();
    }

    @Test
    public void incompleteStructureUpdate() throws IOException {
        assertThat(thing.getChannels().size(), is(CHANNEL_COUNT));
        assertThat(thing.getStatus(), is(ThingStatus.OFFLINE));

        waitForAssert(() -> assertThat(bridge.getStatus(), is(ThingStatus.ONLINE)));
        putStreamingEventData(fromFile(COMPLETE_DATA_FILE_NAME));
        waitForAssert(() -> assertThat(thing.getStatus(), is(ThingStatus.ONLINE)));
        assertThatAllItemStatesAreNotNull();

        putStreamingEventData(fromFile(INCOMPLETE_DATA_FILE_NAME));
        waitForAssert(() -> assertThat(thing.getStatus(), is(ThingStatus.ONLINE)));
        assertThatAllItemStatesAreNull();
    }

    @Test
    public void structureGone() throws IOException {
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
    public void handleAwayCommands() throws IOException {
        handleCommand(CHANNEL_AWAY, new StringType("AWAY"));
        assertNestApiPropertyState(STRUCTURE1_STRUCTURE_ID, "away", "away");

        handleCommand(CHANNEL_AWAY, new StringType("HOME"));
        assertNestApiPropertyState(STRUCTURE1_STRUCTURE_ID, "away", "home");

        handleCommand(CHANNEL_AWAY, new StringType("AWAY"));
        assertNestApiPropertyState(STRUCTURE1_STRUCTURE_ID, "away", "away");
    }
}
