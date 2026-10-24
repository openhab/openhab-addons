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
package org.openhab.binding.gme.internal.handler;

import static org.mockito.Mockito.*;

import java.time.LocalDate;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.gme.internal.GmeBindingConstants;
import org.openhab.binding.gme.internal.api.GmeAuthManager;
import org.openhab.binding.gme.internal.model.GmeGranularity;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;

/**
 * Tests errors propagated from the GME API to the child Thing status.
 */
@NonNullByDefault
@SuppressWarnings("null")
class GmePunHandlerTest {

    @Test
    void invalidMarketDataSetsCommunicationErrorInsteadOfLeavingThingUnknown() throws Exception {
        ThingUID bridgeUID = new ThingUID(GmeBindingConstants.THING_TYPE_API, "account");
        Thing thing = ThingBuilder.create(GmeBindingConstants.THING_TYPE_PUN, "price").withBridge(bridgeUID).build();

        Bridge bridge = mock(Bridge.class);
        GmeApiBridgeHandler bridgeHandler = mock(GmeApiBridgeHandler.class);
        GmeAuthManager authManager = mock(GmeAuthManager.class);
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);

        when(callback.getBridge(bridgeUID)).thenReturn(bridge);
        when(bridge.getStatus()).thenReturn(ThingStatus.ONLINE);
        when(bridge.getHandler()).thenReturn(bridgeHandler);
        when(bridgeHandler.getAuthManager()).thenReturn(authManager);
        when(bridgeHandler.getRefreshInterval()).thenReturn(60);
        when(bridgeHandler.getGranularity()).thenReturn(GmeGranularity.PT60);
        when(bridgeHandler.getMarketZone()).thenReturn("");
        when(authManager.requestMarketPrices(any(LocalDate.class), eq(GmeGranularity.PT60)))
                .thenThrow(new IllegalStateException("GME RequestData returned invalid market data"));

        GmePunHandler handler = new GmePunHandler(thing);
        handler.setCallback(callback);
        try {
            handler.initialize();

            verify(callback, timeout(5000)).statusUpdated(eq(thing),
                    argThat(info -> info.getStatus() == ThingStatus.OFFLINE
                            && info.getStatusDetail() == ThingStatusDetail.COMMUNICATION_ERROR));
            verify(authManager, times(1)).requestMarketPrices(any(LocalDate.class), eq(GmeGranularity.PT60));
        } finally {
            handler.dispose();
        }
    }
}
