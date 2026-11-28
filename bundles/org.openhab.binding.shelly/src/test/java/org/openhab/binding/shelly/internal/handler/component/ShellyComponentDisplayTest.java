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
package org.openhab.binding.shelly.internal.handler.component;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.SHELLYRPC_METHOD_UI_SCREEN_SET;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api1.Shelly1HttpApi;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RpcRequest;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiRpc;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.RefreshType;

/**
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
public class ShellyComponentDisplayTest {

    @ParameterizedTest
    @EnumSource(OnOffType.class)
    void switchCommandSetsScreenAndChannel(OnOffType onOff) throws Exception {
        Shelly2ApiRpc api = mock(Shelly2ApiRpc.class);
        ShellyThingInterface handler = mock(ShellyThingInterface.class);
        when(handler.getApi()).thenReturn(api);
        when(handler.getThingName()).thenReturn("wd");

        ShellyComponentDisplay.handleCommand(handler, onOff);

        ArgumentCaptor<Shelly2RpcRequest> request = ArgumentCaptor.forClass(Shelly2RpcRequest.class);
        verify(api).apiRequest(request.capture());
        assertEquals(SHELLYRPC_METHOD_UI_SCREEN_SET, request.getValue().method);
        assertEquals(onOff == OnOffType.ON, request.getValue().params.on);
        verify(handler).updateChannel(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_SCREEN, onOff);
    }

    @Test
    void refreshCommandSendsNothing() throws Exception {
        Shelly2ApiRpc api = mock(Shelly2ApiRpc.class);
        ShellyThingInterface handler = mock(ShellyThingInterface.class);
        when(handler.getApi()).thenReturn(api);

        ShellyComponentDisplay.handleCommand(handler, RefreshType.REFRESH);

        verifyNoInteractions(api);
        verify(handler, never()).updateChannel(anyString(), anyString(), any());
    }

    @Test
    void gen1ApiIsRejected() {
        ShellyThingInterface handler = mock(ShellyThingInterface.class);
        when(handler.getApi()).thenReturn(mock(Shelly1HttpApi.class));

        assertThrows(ShellyApiException.class, () -> ShellyComponentDisplay.handleCommand(handler, OnOffType.ON));
    }
}
