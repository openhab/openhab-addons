/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.zwavejs.internal.handler.mock;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.openhab.binding.zwavejs.internal.BindingConstants;
import org.openhab.binding.zwavejs.internal.config.ZwaveJSBridgeConfiguration;
import org.openhab.binding.zwavejs.internal.handler.ZwaveJSBridgeHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.WebSocketFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * The {@link ZwaveJSBridgeHandlerMock} is responsible for mocking {@link ZwaveJSBridgeHandler}
 * 
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class ZwaveJSBridgeHandlerMock extends ZwaveJSBridgeHandler {

    private static Configuration createConfig(String hostname) {
        final Configuration config = new Configuration();
        config.put(BindingConstants.CONFIG_HOSTNAME, hostname);
        return config;
    }

    public static Bridge mockBridge(String hostname) {
        return mockBridge(hostname, null);
    }

    public static Bridge mockBridge(String hostname, @Nullable ThingStatus status) {
        final Bridge bridge = mock(Bridge.class);
        when(bridge.getUID()).thenReturn(new ThingUID(BindingConstants.BINDING_ID, "test-bridge"));
        when(bridge.getConfiguration()).thenReturn(createConfig(hostname));
        when(bridge.getStatus()).thenReturn(status == null ? ThingStatus.ONLINE : status);
        return bridge;
    }

    public static ZwaveJSBridgeHandlerMock createAndInitHandler(final ThingHandlerCallback callback,
            final Bridge thing) {
        WebSocketFactory wsFactory = mock(WebSocketFactory.class);
        final ZwaveJSBridgeHandlerMock handler = spy(new ZwaveJSBridgeHandlerMock(thing, wsFactory));

        handler.setCallback(callback);
        handler.initialize();

        return handler;
    }

    public ZwaveJSBridgeHandlerMock(Bridge bridge, WebSocketFactory wsFactory) {
        super(bridge, wsFactory);

        executorService = Mockito.mock(ScheduledExecutorService.class);
        doAnswer((InvocationOnMock invocation) -> {
            ((Runnable) invocation.getArguments()[0]).run();
            return null;
        }).when(executorService).scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class));
    }

    @Override
    protected void startClient(ZwaveJSBridgeConfiguration config) {
        // dont connect in unit test mode
    }
}
