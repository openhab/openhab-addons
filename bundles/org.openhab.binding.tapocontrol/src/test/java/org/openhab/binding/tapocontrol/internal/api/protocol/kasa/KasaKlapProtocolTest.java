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
package org.openhab.binding.tapocontrol.internal.api.protocol.kasa;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.openhab.binding.tapocontrol.internal.TapoControlHandlerFactory.GSON;
import static org.openhab.binding.tapocontrol.internal.constants.TapoComConstants.DEVICE_CMD_GETINFO;
import static org.openhab.binding.tapocontrol.internal.constants.TapoErrorCode.ERR_API_HAND_SHAKE_FAILED;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.openhab.binding.tapocontrol.internal.api.TapoDeviceConnector;
import org.openhab.binding.tapocontrol.internal.api.protocol.klap.KlapProtocol;
import org.openhab.binding.tapocontrol.internal.devices.dto.TapoBaseDeviceData;
import org.openhab.binding.tapocontrol.internal.dto.TapoRequest;
import org.openhab.binding.tapocontrol.internal.dto.TapoResponse;
import org.openhab.binding.tapocontrol.internal.helpers.TapoCredentials;
import org.openhab.binding.tapocontrol.internal.helpers.TapoErrorHandler;

/**
 * Tests for {@link KasaKlapProtocol}.
 *
 * @author Christopher Smith - Initial contribution
 */
@NonNullByDefault
class KasaKlapProtocolTest {
    private static final TapoCredentials ACCOUNT = new TapoCredentials("user@example.com", "account-password");
    private static final TapoCredentials KASA_SETUP = new TapoCredentials("user@example.com",
            KasaKlapProtocol.KASA_SETUP_PASSWORD);

    private final TapoDeviceConnector connector = mock(TapoDeviceConnector.class);
    private final KlapProtocol klap = mock(KlapProtocol.class);
    private final KasaKlapProtocol protocol = new KasaKlapProtocol(connector, false, klap);

    @Test
    void logsInWithAccountPasswordFirst() throws TapoErrorHandler {
        when(klap.login(ACCOUNT)).thenReturn(true);
        when(klap.isLoggedIn()).thenReturn(true);

        assertThat(protocol.login(ACCOUNT), is(true));

        verify(klap, never()).login(KASA_SETUP);
        assertThat(protocol.isLoggedIn(), is(true));
    }

    @Test
    void fallsBackToKasaSetupPasswordAndPrefersItOnNextLogin() throws TapoErrorHandler {
        when(klap.login(ACCOUNT)).thenThrow(new TapoErrorHandler(ERR_API_HAND_SHAKE_FAILED));
        when(klap.login(KASA_SETUP)).thenReturn(true);

        assertThat(protocol.login(ACCOUNT), is(true));
        protocol.logout();
        assertThat(protocol.login(ACCOUNT), is(true));

        InOrder order = inOrder(klap);
        order.verify(klap).login(ACCOUNT);
        order.verify(klap, times(2)).login(KASA_SETUP);
        order.verifyNoMoreInteractions();
    }

    @Test
    void reportsHandshakeErrorWhenNoCredentialsMatch() throws TapoErrorHandler {
        when(klap.login(any())).thenThrow(new TapoErrorHandler(ERR_API_HAND_SHAKE_FAILED));

        assertThrows(TapoErrorHandler.class, () -> protocol.login(ACCOUNT));
        assertThat(protocol.isLoggedIn(), is(false));
    }

    @Test
    void sendsIotSysinfoQueryOverKlap() throws TapoErrorHandler {
        when(klap.login(ACCOUNT)).thenReturn(true);
        when(klap.exchange("{\"system\":{\"get_sysinfo\":{}}}")).thenReturn("""
                {"system":{"get_sysinfo":{"err_code":0,"model":"EP10(US)","relay_state":1,"rssi":-43,
                "sw_ver":"1.1.1 Build 250908 Rel.112508","alias":"Elephants"}}}
                """);
        protocol.login(ACCOUNT);

        protocol.sendRequest(new TapoRequest(DEVICE_CMD_GETINFO));

        ArgumentCaptor<TapoResponse> response = ArgumentCaptor.forClass(TapoResponse.class);
        verify(connector).handleResponse(response.capture(), eq(DEVICE_CMD_GETINFO));
        assertThat(response.getValue().result().get("device_on").getAsBoolean(), is(true));
        assertThat(response.getValue().result().get("fw_ver").getAsString(), is("1.1.1 Build 250908 Rel.112508"));
        TapoBaseDeviceData data = Objects
                .requireNonNull(GSON.fromJson(response.getValue().result(), TapoBaseDeviceData.class));
        assertThat(data.getNickname(), is("Elephants"));
        assertThat(data.getSignalLevel(), is(4));
    }

    @Test
    void doesNotSendAfterLogout() throws TapoErrorHandler {
        when(klap.login(ACCOUNT)).thenReturn(true);
        protocol.login(ACCOUNT);
        protocol.logout();

        assertThrows(TapoErrorHandler.class, () -> protocol.sendRequest(new TapoRequest(DEVICE_CMD_GETINFO)));
        verify(klap, never()).exchange(any());
        verify(klap).logout();
    }
}
