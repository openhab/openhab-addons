/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.awattar.internal.handler;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.awattar.internal.AwattarBindingConstants;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.test.java.JavaTest;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * The {@link AwattarBridgeHandlerRefreshTest} contains tests for the {@link AwattarBridgeHandler} refresh logic.
 *
 * @author Thomas Leber - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class AwattarBridgeHandlerRefreshTest extends JavaTest {
    public static final ThingUID BRIDGE_UID = new ThingUID(AwattarBindingConstants.THING_TYPE_BRIDGE, "testBridge");

    // bridge mocks
    private @Mock @NonNullByDefault({}) Bridge bridgeMock;
    private @Mock @NonNullByDefault({}) ThingHandlerCallback bridgeCallbackMock;
    private @Mock @NonNullByDefault({}) HttpClient httpClientMock;
    private @Mock @NonNullByDefault({}) TimeZoneProvider timeZoneProviderMock;
    private @Mock @NonNullByDefault({}) Request requestMock;
    private @Mock @NonNullByDefault({}) ContentResponse contentResponseMock;

    // best price handler mocks
    private @Mock @NonNullByDefault({}) Thing bestpriceMock;
    private @Mock @NonNullByDefault({}) ThingHandlerCallback bestPriceCallbackMock;

    private @NonNullByDefault({}) AwattarBridgeHandler bridgeHandler;

    @BeforeEach
    public void setUp() throws IOException, ExecutionException, InterruptedException, TimeoutException {
        try (InputStream inputStream = AwattarBridgeHandlerRefreshTest.class.getResourceAsStream("api_response.json")) {
            if (inputStream == null) {
                throw new IOException("inputstream is null");
            }
            byte[] bytes = inputStream.readAllBytes();
            if (bytes == null) {
                throw new IOException("Resulting byte-array empty");
            }
            when(contentResponseMock.getContentAsString()).thenReturn(new String(bytes, StandardCharsets.UTF_8));
        }
        when(contentResponseMock.getStatus()).thenReturn(HttpStatus.OK_200);
        when(httpClientMock.newRequest(anyString())).thenReturn(requestMock);
        when(requestMock.method(HttpMethod.GET)).thenReturn(requestMock);
        when(requestMock.timeout(10, TimeUnit.SECONDS)).thenReturn(requestMock);
        when(requestMock.send()).thenReturn(contentResponseMock);

        when(timeZoneProviderMock.getTimeZone()).thenReturn(ZoneId.of("GMT+2"));

        when(bridgeMock.getUID()).thenReturn(BRIDGE_UID);
        bridgeHandler = new AwattarBridgeHandler(bridgeMock, httpClientMock, timeZoneProviderMock);
        bridgeHandler.setCallback(bridgeCallbackMock);

        when(bridgeMock.getHandler()).thenReturn(bridgeHandler);

        // other mocks
        when(bestpriceMock.getBridgeUID()).thenReturn(BRIDGE_UID);

        when(bestPriceCallbackMock.getBridge(any())).thenReturn(bridgeMock);
        when(bestPriceCallbackMock.isChannelLinked(any())).thenReturn(true);
    }

    /**
     * Test the refreshIfNeeded method with a bridge that is offline.
     *
     * @throws SecurityException
     */
    @Test
    void testRefreshIfNeeded_ThingOffline() throws SecurityException {
        when(bridgeMock.getStatus()).thenReturn(ThingStatus.OFFLINE);

        bridgeHandler.refreshIfNeeded();

        verify(bridgeCallbackMock).statusUpdated(bridgeMock,
                new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
    }

    /**
     * Test the refreshIfNeeded method with a bridge that is online and the data is empty.
     *
     * @throws SecurityException
     */
    @Test
    void testRefreshIfNeeded_DataEmptry() throws SecurityException {
        when(bridgeMock.getStatus()).thenReturn(ThingStatus.ONLINE);

        bridgeHandler.refreshIfNeeded();

        verify(bridgeCallbackMock).statusUpdated(bridgeMock,
                new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
    }
}
