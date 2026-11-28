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
package org.openhab.binding.tplinkbridge.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.tplinkbridge.TPLinkBridgeBindingConstants;
import org.openhab.binding.tplinkbridge.TPLinkCredentials;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;

/**
 * Tests for {@link TPLinkAccountHandler}.
 *
 * @author Christopher Smith - Initial contribution
 */
@NonNullByDefault
class TPLinkAccountHandlerTest {

    private final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);

    private TPLinkAccountHandler initialize(Map<String, Object> config) {
        Bridge bridge = BridgeBuilder.create(TPLinkBridgeBindingConstants.THING_TYPE_ACCOUNT, "home")
                .withConfiguration(new Configuration(config)).build();
        TPLinkAccountHandler handler = new TPLinkAccountHandler(bridge);
        handler.setCallback(callback);
        handler.initialize();
        return handler;
    }

    @Test
    void goesOnlineWithCredentials() {
        TPLinkAccountHandler handler = initialize(Map.of("username", " user@example.com ", "password", "secret"));

        verify(callback).statusUpdated(any(), argThat(info -> info.getStatus() == ThingStatus.ONLINE));
        assertEquals(new TPLinkCredentials("user@example.com", "secret"), handler.getCredentials());
    }

    @Test
    void reportsConfigurationErrorWithoutPassword() {
        TPLinkAccountHandler handler = initialize(Map.of("username", "user@example.com"));

        verify(callback).statusUpdated(any(), argThat(info -> info.getStatus() == ThingStatus.OFFLINE
                && info.getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR));
        assertFalse(handler.getCredentials().areSet());
    }

    @Test
    void credentialsToStringHidesPassword() {
        String text = new TPLinkCredentials("user@example.com", "secret").toString();

        assertFalse(text.contains("secret"));
        assertTrue(text.contains("user@example.com"));
    }
}
