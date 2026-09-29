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
package org.openhab.binding.zwavejs.internal.api;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.binding.zwavejs.internal.handler.ZwaveEventListener;

@NonNullByDefault
public class ZWaveJSClientTest {

    private final WebSocketClient webSocketClient = mock(WebSocketClient.class);
    private final ZWaveJSClient client = new ZWaveJSClient(webSocketClient);

    @ParameterizedTest
    @ValueSource(strings = { "not-json", "{\"type\":\"future-message\"}", "{}", "null" })
    public void invalidMessageDoesNotReportConnectionError(String message) {
        ZwaveEventListener listener = mock(ZwaveEventListener.class);
        client.addEventListener(listener);

        client.onWebSocketText(message);

        verifyNoInteractions(listener);
    }
}
