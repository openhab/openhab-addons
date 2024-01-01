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
package org.openhab.binding.nest.internal.sdm.dto;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.openhab.binding.nest.internal.sdm.dto.SDMDataUtil.fromJson;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.nest.internal.sdm.dto.SDMEvent.SDMDeviceEvent;
import org.openhab.binding.nest.internal.sdm.dto.SDMEvent.SDMRelationUpdate;
import org.openhab.binding.nest.internal.sdm.dto.SDMEvent.SDMRelationUpdateType;
import org.openhab.binding.nest.internal.sdm.dto.SDMEvent.SDMResourceUpdate;
import org.openhab.binding.nest.internal.sdm.dto.SDMEvent.SDMResourceUpdateEvents;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMHvacStatus;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMTemperatureTrait;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMThermostatHvacTrait;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMThermostatTemperatureSetpointTrait;

/**
 * Tests deserialization of {@link org.openhab.binding.nest.internal.sdm.dto.SDMEvent}s from JSON.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class SDMEventTest {

    @Test
    public void deserializeResourceUpdateEvent() throws IOException {
        SDMEvent event = fromJson("resource-update-event.json", SDMEvent.class);
        assertThat(event, is(notNullValue()));

        assertThat(event.eventId, is("053a5f98-8c9d-426e-acf1-6b8660558832"));
        assertThat(event.timestamp, is(ZonedDateTime.parse("2019-01-01T00:00:01Z")));

        assertThat(event.relationUpdate, is(nullValue()));

        SDMResourceUpdate resourceUpdate = event.resourceUpdate;
        assertThat(resourceUpdate, is(notNullValue()));
        assertThat(resourceUpdate.name.name, is("enterprises/project-id/devices/device-id"));

        SDMTraits traits = resourceUpdate.traits;
        assertThat(traits, is(notNullValue()));
        assertThat(traits.traitList(), hasSize(3));

        SDMResourceUpdateEvents events = resourceUpdate.events;
        assertThat(events, is(notNullValue()));
        assertThat(events.eventList(), hasSize(4));

        SDMDeviceEvent cameraMotionEvent = events.cameraMotionEvent;
        assertThat(cameraMotionEvent, is(notNullValue()));
        assertThat(cameraMotionEvent.eventSessionId, is("ESI1"));
        assertThat(cameraMotionEvent.eventId, is("EID1"));

        SDMDeviceEvent cameraPersonEvent = events.cameraPersonEvent;
        assertThat(cameraPersonEvent, is(notNullValue()));
        assertThat(cameraPersonEvent.eventSessionId, is("ESI2"));
        assertThat(cameraPersonEvent.eventId, is("EID2"));

        SDMDeviceEvent cameraSoundEvent = events.cameraSoundEvent;
        assertThat(cameraSoundEvent, is(notNullValue()));
        assertThat(cameraSoundEvent.eventSessionId, is("ESI3"));
        assertThat(cameraSoundEvent.eventId, is("EID3"));

        SDMDeviceEvent doorbellChimeEvent = events.doorbellChimeEvent;
        assertThat(doorbellChimeEvent, is(notNullValue()));
        assertThat(doorbellChimeEvent.eventSessionId, is("ESI4"));
        assertThat(doorbellChimeEvent.eventId, is("EID4"));

        SDMTemperatureTrait temperature = traits.temperature;
        assertThat(temperature, is(notNullValue()));
        assertThat(temperature.ambientTemperatureCelsius, is(new BigDecimal("19.73")));

        SDMThermostatHvacTrait thermostatHvac = traits.thermostatHvac;
        assertThat(thermostatHvac, is(notNullValue()));
        assertThat(thermostatHvac.status, is(SDMHvacStatus.OFF));

        SDMThermostatTemperatureSetpointTrait thermostatTemperatureSetpoint = traits.thermostatTemperatureSetpoint;
        assertThat(thermostatTemperatureSetpoint, is(notNullValue()));
        assertThat(thermostatTemperatureSetpoint.heatCelsius, is(new BigDecimal("14.92249")));
        assertThat(thermostatTemperatureSetpoint.coolCelsius, is(nullValue()));

        assertThat(event.userId, is("AVPHwEuBfnPOnTqzVFT4IONX2Qqhu9EJ4ubO-bNnQ-yi"));
        assertThat(event.resourceGroup, is(List.of(new SDMResourceName("enterprises/project-id/devices/device-id"))));
    }

    @Test
    public void deserializeRelationCreatedEvent() throws IOException {
        SDMEvent event = fromJson("relation-created-event.json", SDMEvent.class);
        assertThat(event, is(notNullValue()));

        assertThat(event.eventId, is("0120ecc7-3b57-4eb4-9941-91609f189fb4"));
        assertThat(event.timestamp, is(ZonedDateTime.parse("2019-01-01T00:00:01Z")));

        SDMRelationUpdate relationUpdate = event.relationUpdate;
        assertThat(relationUpdate, is(notNullValue()));
        assertThat(relationUpdate.type, is(SDMRelationUpdateType.CREATED));
        assertThat(relationUpdate.subject.name, is("enterprises/project-id/structures/structure-id"));
        assertThat(relationUpdate.object.name, is("enterprises/project-id/devices/device-id"));

        assertThat(event.resourceUpdate, is(nullValue()));
        assertThat(event.userId, is("AVPHwEuBfnPOnTqzVFT4IONX2Qqhu9EJ4ubO-bNnQ-yi"));
        assertThat(event.resourceGroup, is(nullValue()));
    }

    @Test
    public void deserializeRelationDeletedEvent() throws IOException {
        SDMEvent event = fromJson("relation-deleted-event.json", SDMEvent.class);
        assertThat(event, is(notNullValue()));

        assertThat(event.eventId, is("0120ecc7-3b57-4eb4-9941-91609f189fb4"));
        assertThat(event.timestamp, is(ZonedDateTime.parse("2019-01-01T00:00:01Z")));

        SDMRelationUpdate relationUpdate = event.relationUpdate;
        assertThat(relationUpdate, is(notNullValue()));
        assertThat(relationUpdate.type, is(SDMRelationUpdateType.DELETED));
        assertThat(relationUpdate.subject.name, is("enterprises/project-id/structures/structure-id"));
        assertThat(relationUpdate.object.name, is("enterprises/project-id/devices/device-id"));

        assertThat(event.resourceUpdate, is(nullValue()));
        assertThat(event.userId, is("AVPHwEuBfnPOnTqzVFT4IONX2Qqhu9EJ4ubO-bNnQ-yi"));
        assertThat(event.resourceGroup, is(nullValue()));
    }

    @Test
    public void deserializeRelationUpdatedEvent() throws IOException {
        SDMEvent event = fromJson("relation-updated-event.json", SDMEvent.class);
        assertThat(event, is(notNullValue()));

        assertThat(event.eventId, is("0120ecc7-3b57-4eb4-9941-91609f189fb4"));
        assertThat(event.timestamp, is(ZonedDateTime.parse("2019-01-01T00:00:01Z")));

        SDMRelationUpdate relationUpdate = event.relationUpdate;
        assertThat(relationUpdate, is(notNullValue()));
        assertThat(relationUpdate.type, is(SDMRelationUpdateType.UPDATED));
        assertThat(relationUpdate.subject.name, is("enterprises/project-id/structures/structure-id"));
        assertThat(relationUpdate.object.name, is("enterprises/project-id/devices/device-id"));

        assertThat(event.resourceUpdate, is(nullValue()));
        assertThat(event.userId, is("AVPHwEuBfnPOnTqzVFT4IONX2Qqhu9EJ4ubO-bNnQ-yi"));
        assertThat(event.resourceGroup, is(nullValue()));
    }
}
