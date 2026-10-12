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
package org.openhab.binding.tedee.internal.handler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.tedee.internal.api.TedeeApiException;
import org.openhab.binding.tedee.internal.api.TedeeClient;
import org.openhab.core.thing.Thing;

@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class TedeeLockHandlerTest {

    private static final int DEVICE_ID = 12345;
    private final TedeeClient client = mock(TedeeClient.class);
    private final Thing thing = mock(Thing.class);
    private final TedeeLockHandler handler = new TedeeLockHandler(thing);

    @Test
    void shouldUseUnlockOrPullForUnlock() throws TedeeApiException {
        handler.executeAction(client, DEVICE_ID, "UNLOCK");

        verify(client).unlockOrPull(DEVICE_ID);
    }

    @Test
    void shouldUseUnlockWithoutPull() throws TedeeApiException {
        handler.executeAction(client, DEVICE_ID, "UNLOCK_NO_PULL");

        verify(client).unlockWithoutPull(DEVICE_ID);
    }

    @Test
    void shouldUsePull() throws TedeeApiException {
        handler.executeAction(client, DEVICE_ID, "PULL");

        verify(client).pull(DEVICE_ID);
    }

    @Test
    void shouldUseLock() throws TedeeApiException {
        handler.executeAction(client, DEVICE_ID, "LOCK");

        verify(client).lock(DEVICE_ID);
    }

    @Test
    void shouldIgnoreUnsupportedAction() throws TedeeApiException {
        handler.executeAction(client, DEVICE_ID, "INVALID");

        verifyNoInteractions(client);
    }
}
