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
import org.openhab.core.thing.Thing;

/**
 * Tests the action dispatching performed by {@link TedeeLockHandler}.
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class TedeeLockHandlerTest {

    private static final int DEVICE_ID = 12345;

    private final TedeeTransportProvider provider = mock(TedeeTransportProvider.class);
    private final Thing thing = mock(Thing.class);
    private final TedeeLockHandler handler = new TedeeLockHandler(thing);

    /**
     * Verifies that the {@code UNLOCK} action invokes the unlock operation.
     *
     * @throws TedeeApiException if the action execution fails
     */
    @Test
    void shouldUseUnlock() throws TedeeApiException {
        handler.executeAction(provider, DEVICE_ID, "UNLOCK");

        verify(provider).unlock(DEVICE_ID);
    }

    /**
     * Verifies that the {@code UNLOCK_OR_PULL} action invokes the unlock-or-pull operation.
     *
     * @throws TedeeApiException if the action execution fails
     */
    @Test
    void shouldUseUnlockOrPull() throws TedeeApiException {
        handler.executeAction(provider, DEVICE_ID, "UNLOCK_OR_PULL");

        verify(provider).unlockOrPull(DEVICE_ID);
    }

    /**
     * Verifies that the {@code UNLOCK_NO_PULL} action invokes the unlock-without-pull operation.
     *
     * @throws TedeeApiException if the action execution fails
     */
    @Test
    void shouldUseUnlockWithoutPull() throws TedeeApiException {
        handler.executeAction(provider, DEVICE_ID, "UNLOCK_NO_PULL");

        verify(provider).unlockWithoutPull(DEVICE_ID);
    }

    /**
     * Verifies that the {@code PULL} action invokes the pull operation.
     *
     * @throws TedeeApiException if the action execution fails
     */
    @Test
    void shouldUsePull() throws TedeeApiException {
        handler.executeAction(provider, DEVICE_ID, "PULL");

        verify(provider).pull(DEVICE_ID);
    }

    /**
     * Verifies that the {@code LOCK} action invokes the lock operation.
     *
     * @throws TedeeApiException if the action execution fails
     */
    @Test
    void shouldUseLock() throws TedeeApiException {
        handler.executeAction(provider, DEVICE_ID, "LOCK");

        verify(provider).lock(DEVICE_ID);
    }

    /**
     * Verifies that an unsupported action does not invoke any provider operation.
     *
     * @throws TedeeApiException if the action execution fails
     */
    @Test
    void shouldIgnoreUnsupportedAction() throws TedeeApiException {
        handler.executeAction(provider, DEVICE_ID, "INVALID");

        verifyNoInteractions(provider);
    }
}