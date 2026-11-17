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
package org.openhab.binding.knx.internal.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.knx.internal.client.KNXClient;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

import io.calimero.IndividualAddress;

/**
 * Checks that a bridge shows the address it sends from as thing property once the connection is up.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class OwnAddressPropertyTest {
    private static final IndividualAddress OWN = new IndividualAddress(1, 2, 3);

    private final Bridge thing = mock(Bridge.class);
    private final KNXClient client = mock(KNXClient.class);
    private final TestBridge handler = new TestBridge(thing, client);

    private OwnAddressPropertyTest() {
        when(thing.getUID()).thenReturn(new ThingUID("knx", "ip", "test"));
        handler.setCallback(mock(ThingHandlerCallback.class));
    }

    @Test
    void theAddressIsSetWhenTheBridgeGoesOnline() {
        when(client.ownAddress()).thenReturn(Optional.of(OWN));

        handler.updateStatus(ThingStatus.ONLINE);

        verify(thing).setProperty("ownAddress", "1.2.3");
    }

    @Test
    void theAddressIsSetWhenTheBridgeGoesOnlineWithADescription() {
        when(client.ownAddress()).thenReturn(Optional.of(OWN));

        handler.updateStatus(ThingStatus.ONLINE, "made-up warning");

        verify(thing).setProperty("ownAddress", "1.2.3");
    }

    @Test
    void aBridgeThatIsNotOnlineSetsNothing() {
        when(client.ownAddress()).thenReturn(Optional.of(OWN));

        handler.updateStatus(ThingStatus.OFFLINE);
        handler.updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, "made-up");
        handler.updateStatus(ThingStatus.UNKNOWN);

        verify(thing, never()).setProperty(any(), any());
    }

    @Test
    void withoutAnAddressNothingIsSet() {
        when(client.ownAddress()).thenReturn(Optional.empty());

        handler.updateStatus(ThingStatus.ONLINE);

        verify(thing, never()).setProperty(eq("ownAddress"), any());
    }

    @Test
    void aClientThatFailsDoesNotBreakTheStatus() {
        when(client.ownAddress()).thenThrow(new IllegalStateException("made-up failure"));

        handler.updateStatus(ThingStatus.ONLINE);

        verify(thing, never()).setProperty(eq("ownAddress"), any());
    }

    private static class TestBridge extends KNXBridgeBaseThingHandler {
        private final KNXClient client;

        TestBridge(Bridge thing, KNXClient client) {
            super(thing);
            this.client = client;
        }

        @Override
        public void initialize() {
        }

        @Override
        protected KNXClient getClient() {
            return client;
        }
    }
}
