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
package org.openhab.binding.samsungtv.internal.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.samsungtv.internal.config.SamsungTvConfiguration;
import org.openhab.binding.samsungtv.internal.handler.SamsungTvHandler;
import org.openhab.core.io.net.http.WebSocketFactory;

/**
 * Tests for {@link RemoteControllerService}
 *
 * @author Nick Waterton - Initial contribution
 */
@NonNullByDefault
public class RemoteControllerServiceTest {

    private final SamsungTvHandler handler = mock(SamsungTvHandler.class);
    private final List<Runnable> queued = new ArrayList<>();

    @BeforeEach
    public void setUp() {
        handler.configuration = new SamsungTvConfiguration();
        WebSocketFactory webSocketFactory = mock(WebSocketFactory.class);
        when(webSocketFactory.createWebSocketClient(anyString(), any())).thenReturn(mock(WebSocketClient.class));
        when(handler.getWebSocketFactory()).thenReturn(webSocketFactory);
        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
        doAnswer(invocation -> queued.add(invocation.getArgument(0))).when(scheduler).execute(any());
        when(handler.getScheduler()).thenReturn(scheduler);
        when(handler.fetchPowerState()).thenReturn("on");
    }

    @Test
    public void powerUpdateQueuedBeforeStopIsDropped() throws Exception {
        RemoteControllerService service = new RemoteControllerService("192.168.1.10", 8002, false, handler);
        service.powerUpdatedAsync(true, false);
        service.stop();
        assertFalse(queued.isEmpty());
        queued.forEach(Runnable::run);

        verify(handler, never()).fetchPowerState();
        verify(handler, never()).valueReceived(anyString(), any());
    }

    @Test
    public void powerUpdateIsApplied() throws Exception {
        RemoteControllerService service = new RemoteControllerService("192.168.1.10", 8002, false, handler);
        service.powerUpdatedAsync(true, false);
        queued.forEach(Runnable::run);

        verify(handler).fetchPowerState();
        verify(handler, atLeastOnce()).valueReceived(anyString(), any());
    }
}
