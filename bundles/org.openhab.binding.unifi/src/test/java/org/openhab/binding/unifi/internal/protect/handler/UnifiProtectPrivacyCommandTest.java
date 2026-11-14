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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.unifi.internal.protect.UnifiProtectBindingConstants.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.InOrder;
import org.openhab.binding.unifi.internal.protect.api.hybrid.UniFiProtectHybridClient;
import org.openhab.binding.unifi.internal.protect.api.priv.client.UniFiProtectPrivateClient;
import org.openhab.binding.unifi.internal.protect.api.priv.dto.devices.Camera;
import org.openhab.binding.unifi.internal.protect.media.UnifiMediaService;
import org.openhab.binding.unifi.internal.protect.util.PrivacyZoneHelper;
import org.openhab.binding.unifi.internal.protect.util.TranslationService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.ThingTypeRegistry;

/**
 * Tests privacy command ordering, lifecycle safety, and independent camera controls.
 *
 * @author Joseph Matz - Initial contribution
 */
@NonNullByDefault
@Timeout(10)
class UnifiProtectPrivacyCommandTest {

    private static final ThingUID CAMERA_UID = new ThingUID(THING_TYPE_CAMERA, "nvr", "camera1");
    private static final ChannelUID PRIVACY_UID = new ChannelUID(CAMERA_UID, CHANNEL_PRIVACY_MODE);

    private final UniFiProtectHybridClient api = mock(UniFiProtectHybridClient.class);
    private final UniFiProtectPrivateClient client = mock(UniFiProtectPrivateClient.class);
    private final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
    private final UnifiMediaService media = mock(UnifiMediaService.class);
    private final Thing thing = ThingBuilder.create(THING_TYPE_CAMERA, CAMERA_UID)
            .withConfiguration(new Configuration(Map.of("deviceId", "camera1", "enableWebRTC", false)))
            .withChannels(ChannelBuilder.create(PRIVACY_UID, "Switch")
                    .withType(new ChannelTypeUID(CAMERA_UID.getBindingId(), CHANNEL_PRIVACY_MODE)).build())
            .build();
    private final UnifiProtectCameraHandler handler = spy(
            new UnifiProtectCameraHandler(thing, media, mock(TranslationService.class), mock(ThingTypeRegistry.class)));

    @BeforeEach
    void setUp() {
        handler.setCallback(callback);
        handler.deviceId = "camera1";
        doReturn(api).when(handler).getApiClient();
        when(api.getPrivateClient()).thenReturn(client);
    }

    @AfterEach
    void tearDown() {
        handler.dispose();
    }

    @Test
    void secondCommandWaitsForFirstStatePublication() {
        CompletableFuture<Camera> on = new CompletableFuture<>();
        CompletableFuture<Camera> off = new CompletableFuture<>();
        when(client.setCameraPrivacyMode("camera1", true)).thenReturn(on);
        when(client.setCameraPrivacyMode("camera1", false)).thenReturn(off);

        handler.handleCommand(PRIVACY_UID, OnOffType.ON);
        handler.handleCommand(PRIVACY_UID, OnOffType.OFF);
        verify(client, never()).setCameraPrivacyMode("camera1", false);
        verify(callback, never()).stateUpdated(eq(PRIVACY_UID), any());

        on.complete(camera(true));
        off.complete(camera(false));
        InOrder order = inOrder(client, callback);
        order.verify(client).setCameraPrivacyMode("camera1", true);
        order.verify(callback).stateUpdated(PRIVACY_UID, OnOffType.ON);
        order.verify(client).setCameraPrivacyMode("camera1", false);
        order.verify(callback).stateUpdated(PRIVACY_UID, OnOffType.OFF);
    }

    @Test
    void failedCommandDoesNotBlockNextCommandOrPublishFalseState() {
        CompletableFuture<Camera> on = new CompletableFuture<>();
        when(client.setCameraPrivacyMode("camera1", true)).thenReturn(on);
        when(client.setCameraPrivacyMode("camera1", false))
                .thenReturn(CompletableFuture.completedFuture(camera(false)));
        handler.handleCommand(PRIVACY_UID, OnOffType.ON);
        handler.handleCommand(PRIVACY_UID, OnOffType.OFF);
        on.completeExceptionally(new IllegalStateException("API failure"));
        verify(callback, never()).stateUpdated(PRIVACY_UID, OnOffType.ON);
        verify(callback).stateUpdated(PRIVACY_UID, OnOffType.OFF);
        verify(client).setCameraPrivacyMode("camera1", false);
    }

    @Test
    void synchronousCompletionDoesNotLoseNextCommand() {
        when(client.setCameraPrivacyMode("camera1", true)).thenReturn(CompletableFuture.completedFuture(camera(true)));
        when(client.setCameraPrivacyMode("camera1", false))
                .thenReturn(CompletableFuture.completedFuture(camera(false)));
        handler.handleCommand(PRIVACY_UID, OnOffType.ON);
        handler.handleCommand(PRIVACY_UID, OnOffType.OFF);
        InOrder order = inOrder(callback);
        order.verify(callback).stateUpdated(PRIVACY_UID, OnOffType.ON);
        order.verify(callback).stateUpdated(PRIVACY_UID, OnOffType.OFF);
    }

    @Test
    void disposalSuppressesQueuedCommandsAndLateState() {
        CompletableFuture<Camera> on = new CompletableFuture<>();
        when(client.setCameraPrivacyMode("camera1", true)).thenReturn(on);
        handler.handleCommand(PRIVACY_UID, OnOffType.ON);
        handler.handleCommand(PRIVACY_UID, OnOffType.OFF);
        handler.dispose();
        clearInvocations(callback);
        on.complete(camera(true));
        handler.handleCommand(PRIVACY_UID, OnOffType.OFF);
        verify(client, never()).setCameraPrivacyMode("camera1", false);
        verify(callback, never()).stateUpdated(eq(PRIVACY_UID), any());
        assertFalse(on.isCancelled(), "Disposal must not cancel a shared client transaction");
    }

    @Test
    void replacementClientDoesNotReceiveOldQueuedCommandOrStaleState() {
        CompletableFuture<Camera> on = new CompletableFuture<>();
        when(client.setCameraPrivacyMode("camera1", true)).thenReturn(on);
        handler.handleCommand(PRIVACY_UID, OnOffType.ON);
        handler.handleCommand(PRIVACY_UID, OnOffType.OFF);
        UniFiProtectHybridClient replacement = mock(UniFiProtectHybridClient.class);
        doReturn(replacement).when(handler).getApiClient();
        on.complete(camera(true));
        verify(client, never()).setCameraPrivacyMode("camera1", false);
        verifyNoInteractions(replacement);
        verify(callback, never()).stateUpdated(eq(PRIVACY_UID), any());
    }

    @Test
    void microphoneAndRecordingRemainIndependentWhilePrivacyIsPending() {
        CompletableFuture<Camera> on = new CompletableFuture<>();
        when(client.setCameraPrivacyMode("camera1", true)).thenReturn(on);
        when(client.setCameraMicEnabled("camera1", false)).thenReturn(CompletableFuture.completedFuture(camera(false)));
        when(client.setCameraRecordingMode("camera1", "motion"))
                .thenReturn(CompletableFuture.completedFuture(camera(false)));
        handler.handleCommand(PRIVACY_UID, OnOffType.ON);
        handler.handleCommand(new ChannelUID(CAMERA_UID, CHANNEL_MIC_ENABLED), OnOffType.OFF);
        handler.handleCommand(new ChannelUID(CAMERA_UID, CHANNEL_RECORDING_MODE), new StringType("motion"));
        verify(client).setCameraMicEnabled("camera1", false);
        verify(client).setCameraRecordingMode("camera1", "motion");
        assertFalse(on.isDone());
        on.complete(camera(true));
        verify(client, never()).setCameraMicEnabled("camera1", true);
        verify(client, never()).setCameraRecordingMode("camera1", "never");
        verify(client, never()).setCameraRecordingMode("camera1", "always");
    }

    @Test
    void cameraRefreshReconstructsPrivacyStateFromPersistedMask() {
        handler.updateFromPrivateDevice(camera(true));
        verify(callback).stateUpdated(PRIVACY_UID, OnOffType.ON);
        handler.updateFromPrivateDevice(camera(false));
        verify(callback).stateUpdated(PRIVACY_UID, OnOffType.OFF);
        verifyNoInteractions(client);
    }

    private static Camera camera(boolean enabled) {
        Camera camera = new Camera();
        Camera.PrivacyZone zone = new Camera.PrivacyZone();
        zone.name = PrivacyZoneHelper.OPENHAB_PRIVACY_ZONE_NAME;
        camera.privacyZones = enabled ? List.of(zone) : List.of();
        return camera;
    }
}
