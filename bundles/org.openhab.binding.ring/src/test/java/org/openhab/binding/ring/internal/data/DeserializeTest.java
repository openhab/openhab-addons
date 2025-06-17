/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.ring.internal.data;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.ring.internal.RestClient;
import org.openhab.binding.ring.internal.api.RingEventTO;

import com.google.gson.Gson;

/**
 * The {@link DeserializeTest} class contains de-serialization tests
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
class DeserializeTest {
    private static final String RESOURCE_PATH = "src" + File.separator + "test" + File.separator + "resources"
            + File.separator;

    private final Gson gson = new Gson();

    @Test
    void testEventDeserialization() throws FileNotFoundException {
        String input = new Scanner(new File(RESOURCE_PATH + "event_response.json")).useDelimiter("\\Z").next();

        List<RingEventTO> events = Objects.requireNonNull(gson.fromJson(input, RestClient.RING_EVENT_LIST_TYPE));

        assertThat(events.size(), is(1));
        RingEventTO event = events.getFirst();

        assertThat(event.id, is(7511772057612656721L));
        assertThat(event.getCreatedAt().getZonedDateTime(ZoneId.of("GMT")),
                equalTo(ZonedDateTime.of(2025, 6, 3, 17, 12, 3, 567000000, ZoneId.of("GMT"))));
        assertThat(event.kind, equalTo("ding"));
        assertThat(event.doorbot.id, equalTo("6000000000"));
        assertThat(event.doorbot.description, equalTo("Haustür"));
    }
}
