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
package org.openhab.binding.shelly.internal.api2;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.shelly.internal.api2.dto.Shelly2DeviceJsonDTO.SHELLYRPC_METHOD_SYS_SETCONFIG;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2WsConfigResponse.Shelly2WsConfigResult;
import org.openhab.binding.shelly.internal.config.ShellyApiConfiguration;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;

/**
 * @author Markus Michels - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
@SuppressWarnings("null")
class Shelly2DebugLogControllerTest {

    private @NonNullByDefault({}) @Mock Shelly2ApiRpc api;
    private @NonNullByDefault({}) @Mock ShellyApiConfiguration config;
    private @NonNullByDefault({}) @Mock WebSocketClient webSocketClient;
    private @NonNullByDefault({}) @Mock ShellyThingInterface thing;

    private @NonNullByDefault({}) Shelly2DebugLogController controller;

    @BeforeEach
    void setUp() throws Exception {
        when(api.getThing()).thenReturn(thing);
        when(config.getDeviceSocketAddress()).thenReturn(null);
        controller = new Shelly2DebugLogController("test", api, config, webSocketClient);
    }

    @Test
    void thingStopDisarmsDeviceAlthoughResumeFailedToConnect() throws Exception {
        controller.resume();

        controller.close(true);

        verify(api).apiRequest(eq(SHELLYRPC_METHOD_SYS_SETCONFIG), any(), eq(Shelly2WsConfigResult.class));
    }

    @Test
    void thingStopDoesNotTouchDeviceThatWasNeverArmed() throws Exception {
        controller.close(true);

        verify(api, never()).apiRequest(anyString(), any(), any());
    }

    @Test
    void reinitializationKeepsDeviceArmed() throws Exception {
        controller.resume();

        controller.close(false);

        verify(api, never()).apiRequest(anyString(), any(), any());
    }
}
