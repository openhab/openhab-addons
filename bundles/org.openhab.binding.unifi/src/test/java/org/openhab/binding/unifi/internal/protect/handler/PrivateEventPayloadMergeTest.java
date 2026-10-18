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
package org.openhab.binding.unifi.internal.protect.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.unifi.internal.protect.api.priv.dto.gson.JsonUtil;
import org.openhab.binding.unifi.internal.protect.api.priv.dto.system.Event;
import org.openhab.binding.unifi.internal.protect.api.pub.dto.events.BaseEvent;
import org.openhab.binding.unifi.internal.protect.api.pub.dto.events.EventType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.type.ThingTypeRegistry;

import com.google.gson.JsonObject;

/**
 * Tests that incremental UPDATE frames from the private updates WebSocket are merged into the last
 * full payload for that event. Protect sends the complete event only on "add"; later "update"
 * frames carry just the changed fields, which cannot be converted on their own.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public class PrivateEventPayloadMergeTest {

    private static final String EVENT_ID = "4b61ed79-7789-4ac1-8cce-927ded49c44e";
    private static final String CAMERA_ID = "62e3a86301824803e700041d";

    // Verbatim shape of a smartDetectZone "add" from the private updates WebSocket.
    private static final String FULL_ADD = """
            {"camera":"%s","device":"%s","id":"%s","score":88,\
            "smartDetectTypes":["person"],"start":1784985677286,"type":"smartDetectZone"}\
            """.formatted(CAMERA_ID, CAMERA_ID, EVENT_ID);

    // A real "update" delta: only the fields that changed.
    private static final String PARTIAL_UPDATE = """
            {"end":1784985699000,"score":93}\
            """;

    private @NonNullByDefault({}) UnifiProtectNVRHandler handler;

    private static JsonObject json(String s) {
        return JsonUtil.fromJson(s, JsonObject.class);
    }

    @BeforeEach
    public void setUp() {
        handler = new UnifiProtectNVRHandler(mock(Bridge.class), mock(ThingTypeRegistry.class));
    }

    @Test
    public void partialUpdateIsMergedIntoTheAddPayload() {
        handler.trackPrivateEventPayload("add", EVENT_ID, json(FULL_ADD));

        @Nullable
        JsonObject merged = handler.trackPrivateEventPayload("update", EVENT_ID, json(PARTIAL_UPDATE));

        assertNotNull(merged);
        // Fields carried only by the add survive.
        assertEquals("smartDetectZone", merged.get("type").getAsString());
        assertEquals(CAMERA_ID, merged.get("camera").getAsString());
        assertEquals(1784985677286L, merged.get("start").getAsLong());
        // Fields from the delta are applied, overwriting where they overlap.
        assertEquals(1784985699000L, merged.get("end").getAsLong());
        assertEquals(93, merged.get("score").getAsInt());
    }

    @Test
    public void mergedPartialUpdateStillConvertsToAPublicEvent() {
        handler.trackPrivateEventPayload("add", EVENT_ID, json(FULL_ADD));
        JsonObject merged = handler.trackPrivateEventPayload("update", EVENT_ID, json(PARTIAL_UPDATE));

        assertNotNull(merged);
        Event event = JsonUtil.fromJson(merged.toString(), Event.class);
        BaseEvent pub = UnifiProtectNVRHandler.toPublicCameraEvent(event);

        // Without the merge this is null, because the delta has no type or camera, and the
        // *_UPDATE dispatch plus the contact latch refresh would be lost.
        assertNotNull(pub);
        assertEquals(EventType.SMART_DETECT_ZONE, pub.type);
        assertEquals(CAMERA_ID, pub.device);
        assertEquals(1784985699000L, pub.end);
    }

    @Test
    public void repeatedUpdatesKeepAccumulating() {
        handler.trackPrivateEventPayload("add", EVENT_ID, json(FULL_ADD));
        handler.trackPrivateEventPayload("update", EVENT_ID, json("{\"score\":90}"));
        JsonObject second = handler.trackPrivateEventPayload("update", EVENT_ID, json(PARTIAL_UPDATE));

        assertNotNull(second);
        assertEquals("smartDetectZone", second.get("type").getAsString());
        assertEquals(93, second.get("score").getAsInt());
        assertEquals(1784985699000L, second.get("end").getAsLong());
    }

    @Test
    public void updateWithoutAKnownAddIsPassedThroughUnchanged() {
        JsonObject out = handler.trackPrivateEventPayload("update", "never-added", json(PARTIAL_UPDATE));

        assertNotNull(out);
        assertFalse(out.has("type"));
        assertEquals(1784985699000L, out.get("end").getAsLong());
    }

    @Test
    public void concurrentUpdatesDoNotLoseEachOthersChanges() throws Exception {
        handler.trackPrivateEventPayload("add", EVENT_ID, json(FULL_ADD));

        // Each thread contributes its own distinct field. If the read/merge/write were not atomic,
        // threads merging from the same snapshot would overwrite one another and fields would go
        // missing -- which is what would silently undo the incremental-update handling.
        int threads = 16;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                int n = i;
                futures.add(pool.submit(() -> {
                    start.await();
                    handler.trackPrivateEventPayload("update", EVENT_ID, json("{\"f" + n + "\":" + n + "}"));
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> f : futures) {
                f.get(10, TimeUnit.SECONDS);
            }
        } finally {
            pool.shutdownNow();
        }

        JsonObject finalPayload = handler.trackPrivateEventPayload("update", EVENT_ID, json("{}"));
        assertNotNull(finalPayload);
        assertEquals("smartDetectZone", finalPayload.get("type").getAsString());
        for (int i = 0; i < threads; i++) {
            assertTrue(finalPayload.has("f" + i), "field f" + i + " was lost by a concurrent merge");
        }
    }

    @Test
    public void removeDropsTheCachedPayload() {
        handler.trackPrivateEventPayload("add", EVENT_ID, json(FULL_ADD));
        handler.trackPrivateEventPayload("remove", EVENT_ID, json("{}"));

        JsonObject afterRemove = handler.trackPrivateEventPayload("update", EVENT_ID, json(PARTIAL_UPDATE));

        assertNotNull(afterRemove);
        assertFalse(afterRemove.has("type"), "a removed event must not keep merging into its old payload");
    }
}
