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
import org.openhab.binding.nest.internal.wwn.config.WWNStructureConfiguration;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ThingBuilder;

/**
 * Tests for {@link WWNStructureHandler}.
 *
 * @author Wouter Born - Initial contribution
 */
public class WWNStructureHandlerTest extends WWNThingHandlerOSGiTest {

    private static final ThingUID STRUCTURE_UID = new ThingUID(THING_TYPE_STRUCTURE, "structure1");
    private static final int CHANNEL_COUNT = 11;

    public WWNStructureHandlerTest() {
        super(WWNStructureHandler.class);
    }

    @Override
    protected Thing buildThing(Bridge bridge) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(WWNStructureConfiguration.STRUCTURE_ID, STRUCTURE1_STRUCTURE_ID);

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
