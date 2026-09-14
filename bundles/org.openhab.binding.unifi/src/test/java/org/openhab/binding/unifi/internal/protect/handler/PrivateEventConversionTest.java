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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.unifi.internal.protect.api.priv.dto.gson.JsonUtil;
import org.openhab.binding.unifi.internal.protect.api.priv.dto.system.Event;
import org.openhab.binding.unifi.internal.protect.api.pub.dto.ObjectType;
import org.openhab.binding.unifi.internal.protect.api.pub.dto.events.BaseEvent;
import org.openhab.binding.unifi.internal.protect.api.pub.dto.events.CameraMotionEvent;
import org.openhab.binding.unifi.internal.protect.api.pub.dto.events.CameraSmartDetectLineEvent;
import org.openhab.binding.unifi.internal.protect.api.pub.dto.events.CameraSmartDetectLoiterEvent;
import org.openhab.binding.unifi.internal.protect.api.pub.dto.events.CameraSmartDetectZoneEvent;
import org.openhab.binding.unifi.internal.protect.api.pub.dto.events.EventType;

/**
 * Tests the conversion of private updates-WebSocket events into the public camera events the
 * regular dispatch reuses. The bugs found while building the fallback were in this routing rather
 * than in deserialization, so each supported type is covered plus the types that must be ignored.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public class PrivateEventConversionTest {

    private static final String CAMERA_ID = "62e3a86301824803e700041d";

    private static Event event(String type, String extra) {
        return JsonUtil.fromJson("""
                {"camera":"%s","device":"%s","id":"evt-1","start":1784985677286,"type":"%s"%s}\
                """.formatted(CAMERA_ID, CAMERA_ID, type, extra), Event.class);
    }

    @Test
    public void motionConvertsToPublicMotionEvent() {
        BaseEvent pub = UnifiProtectNVRHandler.toPublicCameraEvent(event("motion", ""));

        assertNotNull(pub);
        assertInstanceOf(CameraMotionEvent.class, pub);
        assertEquals(EventType.CAMERA_MOTION, pub.type);
        assertEquals(CAMERA_ID, pub.device);
        assertEquals(1784985677286L, pub.start);
    }

    @Test
    public void smartDetectZoneKeepsItsObjectTypes() {
        BaseEvent pub = UnifiProtectNVRHandler
                .toPublicCameraEvent(event("smartDetectZone", ",\"smartDetectTypes\":[\"person\",\"face\"]"));

        assertNotNull(pub);
        assertInstanceOf(CameraSmartDetectZoneEvent.class, pub);
        assertEquals(EventType.SMART_DETECT_ZONE, pub.type);
        assertEquals(List.of(ObjectType.PERSON, ObjectType.FACE), ((CameraSmartDetectZoneEvent) pub).smartDetectTypes);
    }

    @Test
    public void smartDetectLineConvertsToLineEvent() {
        BaseEvent pub = UnifiProtectNVRHandler
                .toPublicCameraEvent(event("smartDetectLine", ",\"smartDetectTypes\":[\"vehicle\"]"));

        assertNotNull(pub);
        assertInstanceOf(CameraSmartDetectLineEvent.class, pub);
        assertEquals(EventType.SMART_DETECT_LINE, pub.type);
    }

    @Test
    public void smartDetectLoiterZoneConvertsToLoiterEvent() {
        BaseEvent pub = UnifiProtectNVRHandler
                .toPublicCameraEvent(event("smartDetectLoiterZone", ",\"smartDetectTypes\":[\"person\"]"));

        assertNotNull(pub);
        assertInstanceOf(CameraSmartDetectLoiterEvent.class, pub);
        assertEquals(EventType.SMART_DETECT_LOITER_ZONE, pub.type);
        assertEquals(List.of(ObjectType.PERSON), ((CameraSmartDetectLoiterEvent) pub).smartDetectTypes);
    }

    @Test
    public void typesHandledOnlyByThePublicPathAreIgnored() {
        // Audio and ring have no private fallback on purpose, so they must not be converted.
        assertNull(UnifiProtectNVRHandler.toPublicCameraEvent(event("smartAudioDetect", "")));
        assertNull(UnifiProtectNVRHandler.toPublicCameraEvent(event("ring", "")));
    }

    @Test
    public void unknownAndCameralessEventsAreIgnored() {
        assertNull(UnifiProtectNVRHandler.toPublicCameraEvent(event("disconnect", "")));

        Event noCamera = JsonUtil.fromJson("""
                {"id":"evt-2","type":"motion","start":1784985677286}\
                """, Event.class);
        assertNull(UnifiProtectNVRHandler.toPublicCameraEvent(noCamera));
    }
}
