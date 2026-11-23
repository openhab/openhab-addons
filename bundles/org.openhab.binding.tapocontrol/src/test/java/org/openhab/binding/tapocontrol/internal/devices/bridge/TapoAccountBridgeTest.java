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
package org.openhab.binding.tapocontrol.internal.devices.bridge;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.openhab.binding.tapocontrol.internal.helpers.TapoCredentials;
import org.openhab.binding.tplinkbridge.TPLinkAccount;
import org.openhab.binding.tplinkbridge.TPLinkCredentials;

/**
 * Tests for {@link TapoAccountBridge}.
 *
 * @author Christopher Smith - Initial contribution
 */
class TapoAccountBridgeTest {

    @Test
    void readsCurrentAccountCredentialsOnEveryCall() {
        TPLinkAccount account = mock(TPLinkAccount.class);
        when(account.getCredentials()).thenReturn(new TPLinkCredentials("old@example.com", "old"),
                new TPLinkCredentials("new@example.com", "new"));
        TapoAccountBridge bridge = new TapoAccountBridge(account, mock(HttpClient.class));

        assertEquals(new TapoCredentials("old@example.com", "old"), bridge.getCredentials());
        assertEquals(new TapoCredentials("new@example.com", "new"), bridge.getCredentials());
    }

    @Test
    void hasNoTapoBridge() {
        HttpClient httpClient = mock(HttpClient.class);
        TapoAccountBridge bridge = new TapoAccountBridge(mock(TPLinkAccount.class), httpClient);

        assertNull(bridge.getTapoBridge());
        assertSame(httpClient, bridge.getHttpClient());
    }
}
