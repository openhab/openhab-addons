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
package org.openhab.binding.zwavejs.internal.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.zwavejs.internal.DataUtil;
import org.openhab.binding.zwavejs.internal.api.dto.messages.ResultMessage;
import org.openhab.binding.zwavejs.internal.discovery.NodeDiscoveryService;
import org.openhab.binding.zwavejs.internal.handler.mock.ZwaveJSBridgeHandlerMock;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class ZwaveJSBridgeHandlerTest {

    @Test
    public void testInvalidConfiguration() {
        final Bridge thing = ZwaveJSBridgeHandlerMock.mockBridge("");
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final ZwaveJSBridgeHandler handler = ZwaveJSBridgeHandlerMock.createAndInitHandler(callback, thing);

        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.OFFLINE)
                    && arg.getStatusDetail().equals(ThingStatusDetail.CONFIGURATION_ERROR)));
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testValidConfiguration() {
        final Bridge thing = ZwaveJSBridgeHandlerMock.mockBridge("loclahost");
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final ZwaveJSBridgeHandler handler = ZwaveJSBridgeHandlerMock.createAndInitHandler(callback, thing);

        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testDiscoveryForActiveNodes() throws IOException {
        final Bridge thing = ZwaveJSBridgeHandlerMock.mockBridge("localhost");
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final ZwaveJSBridgeHandler handler = ZwaveJSBridgeHandlerMock.createAndInitHandler(callback, thing);
        final NodeDiscoveryService discoveryService = mock(NodeDiscoveryService.class);
        doNothing().when(handler).getFullState();
        handler.registerDiscoveryListener(discoveryService);

        ResultMessage resultMessage = DataUtil.fromJson("store_4.json", ResultMessage.class);

        handler.onEvent(resultMessage);

        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
            verify(discoveryService, times(25)).addNodeDiscovery(any());
        } finally {
            handler.dispose();
        }
    }
}
