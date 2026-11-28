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
package org.openhab.binding.knx.internal.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.knx.internal.handler.KNXBridgeBaseThingHandler.CommandExtensionData;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;

import io.calimero.link.KNXNetworkLink;
import io.calimero.secure.Security;

/**
 * Tests for {@link AbstractKNXClient}.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class AbstractKNXClientTest {

    private static class FailOnceClient extends AbstractKNXClient {
        private int attempts;

        FailOnceClient(ScheduledExecutorService scheduler, StatusUpdateCallback callback) {
            super(60, new ThingUID("knx:ip:test"), 2, 50, 3, scheduler, new CommandExtensionData(new TreeMap<>()),
                    Security.newSecurity(), callback);
        }

        @Override
        protected KNXNetworkLink establishConnection() {
            if (attempts++ == 0) {
                throw new IllegalStateException("no usable network interface");
            }
            return new DummyKNXNetworkLink();
        }
    }

    @Test
    void reconnectsAfterUnexpectedRuntimeException() throws Exception {
        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
        StatusUpdateCallback callback = mock(StatusUpdateCallback.class);
        FailOnceClient client = new FailOnceClient(scheduler, callback);

        try {
            client.initialize();
        } catch (RuntimeException e) {
            // a scheduled connect job swallows this the same way
        }

        verify(callback).updateStatus(eq(ThingStatusDetail.COMMUNICATION_ERROR), anyString());
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Callable<Boolean>> reconnect = ArgumentCaptor.forClass(Callable.class);
        verify(scheduler).schedule(reconnect.capture(), eq(60L), eq(TimeUnit.SECONDS));

        assertTrue(reconnect.getValue().call());

        verify(callback).updateStatus(ThingStatus.ONLINE);
        assertTrue(client.isConnected());
        client.dispose();
    }
}
