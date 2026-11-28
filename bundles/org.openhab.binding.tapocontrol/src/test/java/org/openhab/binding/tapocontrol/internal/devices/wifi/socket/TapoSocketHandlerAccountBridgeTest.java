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
package org.openhab.binding.tapocontrol.internal.devices.wifi.socket;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.tapocontrol.internal.constants.TapoThingConstants;
import org.openhab.binding.tplinkbridge.TPLinkAccount;
import org.openhab.binding.tplinkbridge.TPLinkBridgeBindingConstants;
import org.openhab.binding.tplinkbridge.TPLinkCredentials;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;

/**
 * Tests a Tapo device whose parent is a shared {@code tplinkbridge:account}.
 *
 * @author Christopher Smith - Initial contribution
 */
class TapoSocketHandlerAccountBridgeTest {

    private final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
    private @Nullable TapoSocketHandler handler;

    @AfterEach
    void dispose() {
        TapoSocketHandler handler = this.handler;
        if (handler != null) {
            handler.dispose();
        }
    }

    private void initializeWithAccount(TPLinkCredentials credentials) {
        BridgeHandler accountHandler = mock(BridgeHandler.class, withSettings().extraInterfaces(TPLinkAccount.class));
        when(((TPLinkAccount) accountHandler).getCredentials()).thenReturn(credentials);
        ThingUID bridgeUID = new ThingUID(TPLinkBridgeBindingConstants.THING_TYPE_ACCOUNT, "home");
        Bridge bridge = BridgeBuilder.create(TPLinkBridgeBindingConstants.THING_TYPE_ACCOUNT, bridgeUID).build();
        bridge.setHandler(accountHandler);
        when(callback.getBridge(bridgeUID)).thenReturn(bridge);

        Thing thing = ThingBuilder.create(TapoThingConstants.P100_THING_TYPE, "plug").withBridge(bridgeUID)
                .withConfiguration(new Configuration(Map.of("ipAddress", "192.0.2.10", "protocol", "KLAP"))).build();
        TapoSocketHandler handler = new TapoSocketHandler(thing, mock(HttpClient.class));
        this.handler = handler;
        handler.setCallback(callback);
        handler.initialize();
    }

    @Test
    void startsDeviceWithAccountCredentials() {
        initializeWithAccount(new TPLinkCredentials("user@example.com", "secret"));

        verify(callback).statusUpdated(any(), argThat(info -> info.getStatus() == ThingStatus.UNKNOWN));
    }

    @Test
    void doesNotStartDeviceWhenAccountHasNoCredentials() {
        initializeWithAccount(new TPLinkCredentials("", ""));

        verify(callback, never()).statusUpdated(any(), argThat(info -> info.getStatus() == ThingStatus.UNKNOWN));
    }
}
