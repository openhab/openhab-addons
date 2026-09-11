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
package org.openhab.binding.rachio.internal.api.json;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

/**
 * Tests webhook DTO normalization at the JSON boundary.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
public class RachioEventGsonDTOTest {

    @Test
    public void explicitJsonNullsAreNormalized() {
        RachioEventGsonDTO event = new Gson().fromJson(
                "{\"eventId\":null,\"eventType\":null,\"apiResult\":null,\"payload\":{\"valveId\":null}}",
                RachioEventGsonDTO.class);
        assertNotNull(event);

        event.normalizeNullValues();

        assertEquals("", event.eventId);
        assertEquals("", event.eventType);
        assertNotNull(event.getApiResult());
        assertNotNull(event.payload);
        assertEquals("", java.util.Objects.requireNonNull(event.payload).valveId);
        assertDoesNotThrow(event::hasStrongModernWebhookMarkers);
    }
}
