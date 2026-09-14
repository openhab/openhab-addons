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
package org.openhab.binding.unifi.internal.protect.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.unifi.internal.protect.api.priv.dto.gson.JsonUtil;
import org.openhab.binding.unifi.internal.protect.api.priv.dto.system.Event;
import org.openhab.binding.unifi.internal.protect.api.priv.dto.types.EventType;
import org.openhab.binding.unifi.internal.protect.api.priv.dto.types.SmartDetectObjectType;

/**
 * Tests deserialization of the event payload delivered on the private updates WebSocket.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public class EventDeserializationTest {

    // Verbatim smartDetectZone frame from a UniFi Protect private updates WebSocket.
    private static final String SMART_DETECT_ZONE_EVENT = """
            {"camera":"62e3a86301824803e700041d","createdAt":"2026-07-25T13:21:22.506090292Z",\
            "device":"62e3a86301824803e700041d","id":"4b61ed79-7789-4ac1-8cce-927ded49c44e",\
            "locked":false,"score":88,"smartDetectTypes":["face","person"],"start":1784985677286,\
            "type":"smartDetectZone","updatedAt":"2026-07-25T13:21:22.506090961Z"}\
            """;

    @Test
    public void smartDetectZoneEventResolvesTheCamera() {
        Event event = JsonUtil.getGson().fromJson(SMART_DETECT_ZONE_EVENT, Event.class);

        assertNotNull(event);
        assertEquals(EventType.SMART_DETECT, event.type);
        // (thumbnail/heatmap updates, camera event routing) silently drops the event.
        assertEquals("62e3a86301824803e700041d", event.cameraId);
        assertEquals("4b61ed79-7789-4ac1-8cce-927ded49c44e", event.id);
        assertNotNull(event.smartDetectTypes);
        assertTrue(event.smartDetectTypes.contains(SmartDetectObjectType.PERSON));
    }

    @Test
    public void thumbnailAndHeatmapAreResolved() {
        Event event = JsonUtil.getGson().fromJson("""
                {"camera":"62e3a86301824803e700041d","id":"e1","type":"motion",\
                "thumbnail":"e-thumb-1","heatmap":"e-heat-1"}\
                """, Event.class);

        assertNotNull(event);
        assertEquals("e-thumb-1", event.thumbnailId);
        assertEquals("e-heat-1", event.heatmapId);
    }

    @Test
    public void legacyFieldNamesStillDeserialize() {
        Event event = JsonUtil.getGson().fromJson("""
                {"cameraId":"cam-legacy","id":"e2","type":"smartDetectLine",\
                "thumbnailId":"t-legacy","heatmapId":"h-legacy"}\
                """, Event.class);

        assertNotNull(event);
        assertEquals("cam-legacy", event.cameraId);
        assertEquals("t-legacy", event.thumbnailId);
        assertEquals("h-legacy", event.heatmapId);
    }
}
