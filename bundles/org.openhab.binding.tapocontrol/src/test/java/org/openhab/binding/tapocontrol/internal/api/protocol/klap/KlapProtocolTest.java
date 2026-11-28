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
package org.openhab.binding.tapocontrol.internal.api.protocol.klap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.openhab.binding.tapocontrol.internal.constants.TapoErrorCode.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.tapocontrol.internal.api.TapoConnectorInterface;
import org.openhab.binding.tapocontrol.internal.dto.TapoRequest;
import org.openhab.binding.tapocontrol.internal.dto.TapoResponse;
import org.openhab.binding.tapocontrol.internal.helpers.TapoCredentials;
import org.openhab.binding.tapocontrol.internal.helpers.TapoErrorHandler;
import org.openhab.binding.tplinkbridge.klap.KlapAuthenticationException;
import org.openhab.binding.tplinkbridge.klap.KlapTransport;

/**
 * Tests that {@link KlapProtocol} drives the shared {@link KlapTransport} and maps its results to Tapo responses and
 * errors.
 *
 * @author Christopher Smith - Initial contribution
 */
class KlapProtocolTest {
    private static final TapoCredentials CREDENTIALS = new TapoCredentials("user@example.com", "secret");
    private static final String DEVICE_INFO = "{\"error_code\":0,\"result\":{\"device_on\":true}}";

    private final TapoConnectorInterface connector = mock(TapoConnectorInterface.class);
    private final KlapTransport transport = mock(KlapTransport.class);
    private KlapProtocol protocol;

    @BeforeEach
    void setUp() {
        when(connector.getBaseUrl()).thenReturn("127.0.0.1:80");
        when(connector.getThingUID()).thenReturn("tapocontrol:P100:test");
        protocol = new KlapProtocol(connector, transport);
    }

    @AfterEach
    void tearDown() {
        protocol.logout();
    }

    @Test
    void logsInThroughTransport() throws Exception {
        when(transport.isLoggedIn()).thenReturn(true);

        assertThat(protocol.login(CREDENTIALS), is(true));

        verify(transport).setHost("127.0.0.1:80");
        verify(transport, times(1)).login();
    }

    @Test
    void rejectedCredentialsFailWithoutUdpRetry() throws Exception {
        doThrow(new KlapAuthenticationException("rejected")).when(transport).login();

        TapoErrorHandler error = assertThrows(TapoErrorHandler.class, () -> protocol.login(CREDENTIALS));

        assertThat(error.getCode(), is(ERR_API_HAND_SHAKE_FAILED.getCode()));
        verify(transport, times(1)).login();
    }

    @Test
    void blankCredentialsFailWithoutContactingTheDevice() {
        HttpClient httpClient = mock(HttpClient.class);
        when(connector.getHttpClient()).thenReturn(httpClient);
        KlapProtocol realTransportProtocol = new KlapProtocol(connector);

        TapoErrorHandler error = assertThrows(TapoErrorHandler.class,
                () -> realTransportProtocol.login(new TapoCredentials("", "")));

        assertThat(error.getCode(), is(ERR_API_HAND_SHAKE_FAILED.getCode()));
        verify(httpClient, never()).newRequest(anyString());
        realTransportProtocol.logout();
    }

    @Test
    void wakeUpAddressHasNoPort() {
        assertThat(KlapProtocol.hostWithoutPort("192.168.1.20:80"), is("192.168.1.20"));
        assertThat(KlapProtocol.hostWithoutPort("fe80::1:80"), is("fe80::1"));
        assertThat(KlapProtocol.hostWithoutPort("[fe80::1]:80"), is("fe80::1"));
        assertThat(KlapProtocol.hostWithoutPort("tapo-plug.local:80"), is("tapo-plug.local"));
    }

    @Test
    void failedHandshakeIsRetriedOnceAfterUdpWakeUp() throws Exception {
        doThrow(new IOException("no answer")).doNothing().when(transport).login();
        when(transport.isLoggedIn()).thenReturn(true);

        assertThat(protocol.login(CREDENTIALS), is(true));

        verify(transport, times(2)).login();
    }

    @Test
    void synchronousRequestDeliversDecryptedResponse() throws Exception {
        when(transport.exchange(any())).thenReturn(DEVICE_INFO);

        protocol.sendRequest(new TapoRequest("get_device_info"));

        ArgumentCaptor<TapoResponse> response = ArgumentCaptor.forClass(TapoResponse.class);
        verify(connector).handleResponse(response.capture(), eq("get_device_info"));
        assertThat(response.getValue().result().get("device_on").getAsBoolean(), is(true));
    }

    @Test
    void asynchronousRequestDeliversDecryptedResponse() throws Exception {
        when(transport.exchange(any())).thenReturn(DEVICE_INFO);

        protocol.sendAsyncRequest(new TapoRequest("get_device_info"));

        verify(connector, timeout(10_000)).handleResponse(any(), eq("get_device_info"));
    }

    @Test
    void asynchronousResponseIsHandledBeforeTheNextRequestIsSent() throws Exception {
        AtomicInteger exchanges = new AtomicInteger();
        AtomicBoolean handling = new AtomicBoolean();
        AtomicBoolean overlapped = new AtomicBoolean();
        CountDownLatch secondExchange = new CountDownLatch(1);
        when(transport.exchange(any())).thenAnswer(invocation -> {
            if (exchanges.incrementAndGet() > 1) {
                overlapped.compareAndSet(false, handling.get());
                secondExchange.countDown();
            }
            return DEVICE_INFO;
        });
        doAnswer(invocation -> {
            if (handling.compareAndSet(false, true)) {
                // give a second request the chance to overtake while the first response is handled
                secondExchange.await(1, TimeUnit.SECONDS);
                handling.set(false);
            }
            return null;
        }).when(connector).handleResponse(any(), any());

        protocol.sendAsyncRequest(new TapoRequest("get_device_info"));
        protocol.sendAsyncRequest(new TapoRequest("set_device_info"));

        verify(connector, timeout(10_000).times(2)).handleResponse(any(), any());
        assertThat(overlapped.get(), is(false));
    }

    @Test
    void timeoutIsReportedAsConnectTimeout() throws Exception {
        when(transport.exchange(any())).thenThrow(new IOException("timeout", new TimeoutException()));

        TapoErrorHandler error = assertThrows(TapoErrorHandler.class,
                () -> protocol.sendRequest(new TapoRequest("get_device_info")));

        assertThat(error.getCode(), is(ERR_BINDING_CONNECT_TIMEOUT.getCode()));
    }

    @Test
    void asynchronousFailureIsReportedToConnector() throws Exception {
        when(transport.exchange(any())).thenThrow(new IOException("refused"));

        protocol.sendAsyncRequest(new TapoRequest("get_device_info"));

        ArgumentCaptor<TapoErrorHandler> error = ArgumentCaptor.forClass(TapoErrorHandler.class);
        verify(connector, timeout(10_000)).handleError(error.capture());
        assertThat(error.getValue().getCode(), is(ERR_BINDING_SEND_REQUEST.getCode()));
    }
}
