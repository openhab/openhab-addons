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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.unifi.internal.protect.UnifiProtectBindingConstants.*;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.unifi.internal.protect.api.pub.dto.ObjectType;
import org.openhab.binding.unifi.internal.protect.api.pub.dto.events.CameraMotionEvent;
import org.openhab.binding.unifi.internal.protect.api.pub.dto.events.CameraSmartDetectZoneEvent;
import org.openhab.binding.unifi.internal.protect.api.pub.dto.events.EventType;
import org.openhab.binding.unifi.internal.protect.api.pub.dto.events.RingEvent;
import org.openhab.binding.unifi.internal.protect.handler.UnifiProtectAbstractDeviceHandler.WSEventType;
import org.openhab.binding.unifi.internal.protect.media.UnifiMediaService;
import org.openhab.binding.unifi.internal.protect.util.TranslationService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.ThingTypeRegistry;

/**
 * Tests that camera contact channel updates are decoupled from dynamic trigger channels in
 * {@link UnifiProtectCameraHandler}.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public class UnifiProtectCameraEventHandlerTest {

    private static final ThingUID BRIDGE_UID = new ThingUID(THING_TYPE_NVR, "nvr");
    private static final ThingUID CAMERA_UID = new ThingUID(THING_TYPE_CAMERA, BRIDGE_UID, "camera1");

    private final UnifiMediaService mediaService = mock(UnifiMediaService.class);
    private final TranslationService translationService = mock(TranslationService.class);
    private final ThingTypeRegistry thingTypeRegistry = mock(ThingTypeRegistry.class);
    private final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);

    private Channel createChannel(String channelId, @Nullable String itemType, ChannelKind kind) {
        ChannelUID uid = new ChannelUID(CAMERA_UID, channelId);
        ChannelBuilder cb = ChannelBuilder.create(uid, itemType)
                .withType(new ChannelTypeUID(CAMERA_UID.getBindingId(), channelId)).withKind(kind);
        return cb.build();
    }

    private UnifiProtectCameraHandler createHandler(List<Channel> channels) {
        Thing thing = ThingBuilder.create(THING_TYPE_CAMERA, CAMERA_UID).withBridge(BRIDGE_UID).withChannels(channels)
                .withConfiguration(new Configuration(Map.of("deviceId", "camera1", "enableWebRTC", false))).build();
        UnifiProtectCameraHandler handler = new UnifiProtectCameraHandler(thing, mediaService, translationService,
                thingTypeRegistry);
        handler.setCallback(callback);
        return handler;
    }

    @Test
    public void motionEventUpdatesContactChannelEvenWithoutTriggerChannel() {
        Channel motionContact = createChannel(CHANNEL_MOTION_CONTACT, "Contact", ChannelKind.STATE);
        UnifiProtectCameraHandler handler = createHandler(List.of(motionContact));

        CameraMotionEvent event = new CameraMotionEvent();
        event.type = EventType.CAMERA_MOTION;
        event.device = "camera1";

        handler.handleEvent(event, WSEventType.ADD);

        verify(callback).stateUpdated(eq(new ChannelUID(CAMERA_UID, CHANNEL_MOTION_CONTACT)), eq(OpenClosedType.OPEN));
        verify(callback, never()).channelTriggered(any(), any(), any());
    }

    @Test
    public void motionEventTriggersChannelWhenPresent() {
        Channel motionContact = createChannel(CHANNEL_MOTION_CONTACT, "Contact", ChannelKind.STATE);
        Channel motionStart = createChannel(CHANNEL_MOTION_START, null, ChannelKind.TRIGGER);
        UnifiProtectCameraHandler handler = createHandler(List.of(motionContact, motionStart));

        CameraMotionEvent event = new CameraMotionEvent();
        event.type = EventType.CAMERA_MOTION;
        event.device = "camera1";

        handler.handleEvent(event, WSEventType.ADD);

        verify(callback).stateUpdated(eq(new ChannelUID(CAMERA_UID, CHANNEL_MOTION_CONTACT)), eq(OpenClosedType.OPEN));
        verify(callback).channelTriggered(any(), eq(new ChannelUID(CAMERA_UID, CHANNEL_MOTION_START)), any());
    }

    @Test
    public void smartDetectZoneEventUpdatesContactChannelEvenWithoutTriggerChannel() {
        Channel smartDetectContact = createChannel(CHANNEL_SMART_DETECT_ZONE_CONTACT, "Contact", ChannelKind.STATE);
        UnifiProtectCameraHandler handler = createHandler(List.of(smartDetectContact));

        CameraSmartDetectZoneEvent event = new CameraSmartDetectZoneEvent();
        event.type = EventType.SMART_DETECT_ZONE;
        event.device = "camera1";
        event.smartDetectTypes = List.of(ObjectType.PERSON);

        handler.handleEvent(event, WSEventType.ADD);

        verify(callback).stateUpdated(eq(new ChannelUID(CAMERA_UID, CHANNEL_SMART_DETECT_ZONE_CONTACT)),
                eq(OpenClosedType.OPEN));
        verify(callback, never()).channelTriggered(any(), any(), any());
    }

    @Test
    public void ringEventUpdatesContactChannelEvenWithoutTriggerChannel() {
        Channel ringContact = createChannel(CHANNEL_RING_CONTACT, "Contact", ChannelKind.STATE);
        UnifiProtectCameraHandler handler = createHandler(List.of(ringContact));

        RingEvent event = new RingEvent();
        event.type = EventType.RING;
        event.device = "camera1";

        handler.handleEvent(event, WSEventType.ADD);

        verify(callback).stateUpdated(eq(new ChannelUID(CAMERA_UID, CHANNEL_RING_CONTACT)), eq(OpenClosedType.OPEN));
        verify(callback, never()).channelTriggered(any(), any(), any());
    }
}
