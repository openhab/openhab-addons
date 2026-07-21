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
package org.openhab.binding.smartthings.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.smartthings.internal.type.SmartThingsException;
import org.openhab.core.thing.ThingUID;

/**
 * Tests for {@link SmartThingsAuthService}.
 */
@NonNullByDefault
class SmartThingsAuthServiceTest {

    @Test
    @SuppressWarnings("null")
    void authorizeRoutesToTheHandlerRegisteredForTheBridge() throws SmartThingsException {
        SmartThingsAuthService authService = new SmartThingsAuthService();
        ThingUID firstBridgeUID = new ThingUID("smartthings:account:first");
        ThingUID secondBridgeUID = new ThingUID("smartthings:account:second");
        SmartThingsOAuthHandler firstHandler = mock(SmartThingsOAuthHandler.class);
        SmartThingsOAuthHandler secondHandler = mock(SmartThingsOAuthHandler.class);

        when(secondHandler.authorize("https://openhab.example/smartthings/second", "code-2"))
                .thenReturn("authorized-second");
        authService.setSmartThingsOAuthHandler(firstBridgeUID, firstHandler);
        authService.setSmartThingsOAuthHandler(secondBridgeUID, secondHandler);

        String result = authService.authorize(secondBridgeUID, "https://openhab.example/smartthings/second",
                "step2:state", "code-2");

        assertEquals("authorized-second", result);
        verifyNoInteractions(firstHandler);
        verify(secondHandler).authorize("https://openhab.example/smartthings/second", "code-2");
    }
}
