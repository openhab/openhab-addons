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
package org.openhab.binding.mspa.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.json.JSONArray;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.stubbing.OngoingStubbing;
import org.openhab.binding.mspa.internal.discovery.MSpaDiscoveryService;
import org.openhab.core.test.storage.VolatileStorage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;

/**
 * {@link MSpaBaseAccountTest} tests account status behavior of MSpaBaseAccount when getDeviceList() is called with
 * different responses, including token expiration and non-JSON response.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MSpaBaseAccountTest {

    private static class TestAccount extends MSpaBaseAccount {
        private ThingStatus lastStatus = ThingStatus.UNINITIALIZED;
        private ThingStatusDetail lastDetail = ThingStatusDetail.NONE;
        private @Nullable String lastMessage = "";
        private boolean tokenRefreshed = false;
        private final Thing mockThing;

        public TestAccount(HttpClient httpClient) {
            super(mock(Bridge.class), httpClient, mock(MSpaDiscoveryService.class), new VolatileStorage<String>());
            mockThing = mock(Thing.class);
            // assign protected thing field from BaseBridgeHandler so MSpaBaseAccount uses it
            this.thing = mockThing;
            // make getStatus dependent on tokenRefreshed
            when(mockThing.getStatus())
                    .thenAnswer(invocation -> tokenRefreshed ? ThingStatus.ONLINE : ThingStatus.OFFLINE);
        }

        @Override
        public void requestToken() {
            // simulate successful token refresh
            tokenRefreshed = true;
            updateStatus(ThingStatus.ONLINE);
        }

        @Override
        public void clearToken() {
            // no-op for test
        }

        @Override
        public void updateStatus(ThingStatus status) {
            updateStatus(status, ThingStatusDetail.NONE, null);
        }

        @Override
        public void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String message) {
            this.lastStatus = status;
            this.lastDetail = statusDetail;
            this.lastMessage = message;
        }
    }

    private static Stream<Arguments> responsesProvider() {
        String success = "{\"code\":0,\"data\":{\"list\":[{\"device_id\":\"d1\",\"product_id\":\"p1\"}]}}";
        String tokenExpired = "{\"code\":10001,\"message\":\"token expired\"}";
        String nonJson = "Not a JSON response";
        // case 1: immediate success
        List<String> caseSuccess = List.of(success);
        // case 2: token expired then success
        List<String> caseRetry = List.of(tokenExpired, success);
        // case 3: non-JSON response
        List<String> caseNonJson = List.of(nonJson);
        return Stream.of(Arguments.of(caseSuccess, true, ThingStatus.ONLINE, ThingStatusDetail.NONE, null),
                Arguments.of(caseRetry, true, ThingStatus.ONLINE, ThingStatusDetail.NONE, null),
                Arguments.of(caseNonJson, false, ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/status.mspa.pool.request-failed [\"Not a JSON response\"]"));
    }

    @ParameterizedTest
    @MethodSource("responsesProvider")
    public void testGetDeviceList_statusBehavior(List<String> responses, boolean expectPresent,
            ThingStatus expectedStatus, ThingStatusDetail expectedDetail, @Nullable String expectedMessageSubstring)
            throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        Request request = mock(Request.class);
        when(httpClient.newRequest(any(String.class))).thenReturn(request);
        when(request.timeout(anyLong(), any(TimeUnit.class))).thenReturn(request);

        // build a sequence of ContentResponse mocks according to provided response strings
        ContentResponse[] crs = new ContentResponse[responses.size()];
        for (int i = 0; i < responses.size(); i++) {
            ContentResponse cr = mock(ContentResponse.class);
            when(cr.getStatus()).thenReturn(200);
            when(cr.getContentAsString()).thenReturn(responses.get(i));
            crs[i] = cr;
        }

        OngoingStubbing<ContentResponse> stub = when(request.send()).thenReturn(crs[0]);
        for (int i = 1; i < crs.length; i++) {
            stub = stub.thenReturn(crs[i]);
        }

        TestAccount account = new TestAccount(httpClient);
        Optional<JSONArray> opt = account.getDeviceList();
        assertEquals(expectPresent, opt.isPresent(), "Device list delivered");
        assertEquals(expectedStatus, account.lastStatus, "Status should match");
        assertEquals(expectedDetail, account.lastDetail, "Status detail should match");
        assertEquals(expectedMessageSubstring, account.lastMessage, "Status detail should match");
    }
}
