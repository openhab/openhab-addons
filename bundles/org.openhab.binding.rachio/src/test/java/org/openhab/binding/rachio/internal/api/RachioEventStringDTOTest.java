/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.rachio.internal.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.rachio.internal.api.json.RachioEventGsonDTO;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Tests event serialization used by last-event channels.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
class RachioEventStringDTOTest {
    @Test
    void genericEventSerializesTimestampField() {
        RachioEventGsonDTO event = new RachioEventGsonDTO();
        event.timestamp = "2026-05-20T06:00:00Z";
        event.summary = "Schedule started";
        event.type = "SCHEDULE_STATUS";

        JsonObject json = JsonParser.parseString(Objects.requireNonNull(new RachioEventStringDTO(event).toJson()))
                .getAsJsonObject();

        assertThat(json.has("timestamp"), is(true));
        assertThat(json.get("timestamp").getAsString(), is("2026-05-20T06:00:00Z"));
    }

    @Test
    void zoneEventSerializesTimestampField() {
        RachioEventGsonDTO event = new RachioEventGsonDTO();
        event.timestamp = "2026-05-20T06:30:00Z";
        event.summary = "Zone started";
        event.type = "ZONE_STATUS";
        event.zoneName = "Front Lawn";
        event.zoneNumber = 1;

        JsonObject json = JsonParser.parseString(Objects.requireNonNull(new RachioEventStringDTO(event).toJson()))
                .getAsJsonObject();

        assertThat(json.has("timestamp"), is(true));
        assertThat(json.get("timestamp").getAsString(), is("2026-05-20T06:30:00Z"));
    }
}
