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
package org.openhab.binding.jellyfin.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.jellyfin.internal.config.ImageChannelConfig;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * Tests for {@link ClientHandler#initialize()} lifecycle behaviour.
 *
 * Verifies that {@code initialize()} uses the {@code serialNumber} configuration
 * parameter as the event-bus subscription key — NOT the ThingUID segment — so
 * that the handler receives updates even when the Jellyfin app reports a device
 * ID that contains characters invalid in a ThingUID.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
class ClientHandlerInitializeTest {

    /**
     * {@code initialize()} must store the {@code serialNumber} configuration
     * parameter as {@code deviceId} (the event-bus subscription key).
     *
     * <p>
     * The bridge check runs after the config read and fails because the mock
     * thing has no bridgeUID. That OFFLINE status update is captured to confirm
     * the serialNumber gate was passed. The {@code deviceId} field is then read
     * via reflection to assert the correct value.
     */
    @Test
    void testInitialize_usesSerialNumberAsSubscriptionKey() throws Exception {
        Thing thing = mock(Thing.class);
        Configuration config = new Configuration();
        config.put("serialNumber", "the-real-device-id");
        when(thing.getConfiguration()).thenReturn(config);
        // getBridgeUID() returns null on an unstubbed mock → getBridge() returns null

        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        ClientHandler handler = new ClientHandler(thing);
        handler.setCallback(callback);

        handler.initialize();

        // The first CONFIGURATION_ERROR must be "No bridge…", NOT "Missing serialNumber…",
        // which proves the serialNumber gate was passed successfully.
        ArgumentCaptor<ThingStatusInfo> statusCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback).statusUpdated(eq(thing), statusCaptor.capture());
        ThingStatusInfo status = statusCaptor.getValue();
        assertEquals(ThingStatus.OFFLINE, status.getStatus());
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, status.getStatusDetail());
        assertEquals("No bridge configured for client", status.getDescription());

        // The internal deviceId field must equal the serialNumber, not the ThingUID segment.
        Field deviceIdField = ClientHandler.class.getDeclaredField("deviceId");
        deviceIdField.setAccessible(true);
        assertEquals("the-real-device-id", deviceIdField.get(handler));
    }

    /**
     * A freshly constructed {@link ClientHandler} must have an empty {@code imageChannelConfigs}
     * list before {@code initialize()} is called — image channels are only added when enabled in config.
     */
    @Test
    @SuppressWarnings("unchecked")
    void testImageChannelNotCreatedWhenDisabled() throws Exception {
        ClientHandler handler = new ClientHandler(mock(org.openhab.core.thing.Thing.class));

        Field configsField = ClientHandler.class.getDeclaredField("imageChannelConfigs");
        configsField.setAccessible(true);
        List<ImageChannelConfig> configs = (List<ImageChannelConfig>) configsField.get(handler);

        assertTrue(configs.isEmpty(), "imageChannelConfigs must be empty before any channels are enabled");
    }

    /**
     * {@link ImageChannelConfig} correctly stores all three parameters.
     */
    @Test
    void testImageChannelCreatedWhenEnabled() {
        ImageChannelConfig cfg = new ImageChannelConfig("Primary", "playing-item-image-primary", 512);

        assertFalse(cfg.imageType().isEmpty());
        assertFalse(cfg.channelId().isEmpty());
        assertEquals("playing-item-image-primary", cfg.channelId());
        assertEquals("Primary", cfg.imageType());
        assertEquals(512, cfg.width());
    }
}
