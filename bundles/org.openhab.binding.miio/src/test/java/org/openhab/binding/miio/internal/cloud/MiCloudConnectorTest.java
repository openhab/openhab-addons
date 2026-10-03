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
package org.openhab.binding.miio.internal.cloud;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.miio.internal.cloud.MiCloudConnector.CloudLoginState;

/**
 * Test case for {@link MiCloudConnector}
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class MiCloudConnectorTest {

    private static final String URL = "https://api.io.mi.com/app/home/device_list";
    private static final Map<String, String> PARAMS = Map.of("data", "{}");

    private @Mock @NonNullByDefault({}) HttpClient httpClient;
    private @Mock @NonNullByDefault({}) CloudLoginListener listener;
    private @Mock @NonNullByDefault({}) Response response;

    private @NonNullByDefault({}) Request request;
    private @NonNullByDefault({}) MiCloudConnector connector;

    @BeforeEach
    public void setUp() throws MiCloudException {
        request = mock(Request.class, RETURNS_SELF);
        when(httpClient.isStarted()).thenReturn(true);
        when(httpClient.newRequest(anyString())).thenReturn(request);

        // ssecurity must be valid base64, as it is used to sign the request
        connector = new MiCloudConnector("user", "password", httpClient, "clientid", "12345", "servicetoken",
                "c2VjcmV0");
        connector.registerListener(listener);
        assertTrue(connector.login());
        assertEquals("servicetoken", connector.getServiceToken());
        clearInvocations(listener);
    }

    @Test
    public void unauthorizedWithoutAuthenticateHeaderClearsTokenAndSignalsAccessDeniedOnce() throws Exception {
        // Jetty fails a 401 without WWW-Authenticate header with an HttpResponseException wrapped in an
        // ExecutionException, instead of returning the response
        when(response.getStatus()).thenReturn(HttpStatus.UNAUTHORIZED_401);
        when(request.send()).thenThrow(new ExecutionException(
                new HttpResponseException("HTTP protocol violation: Authentication failed", response)));

        assertThrows(MiCloudException.class, () -> connector.request(URL, PARAMS));

        assertEquals("", connector.getServiceToken());
        assertFalse(connector.hasLoginToken());
        verify(listener, times(1)).onStatusUpdated(eq(CloudLoginState.ACCESS_DENIED), anyString());

        // the token is gone, so a follow-up request is refused locally without signalling again
        assertThrows(MiCloudException.class, () -> connector.request(URL, PARAMS));
        verify(request, times(1)).send();
        verify(listener, times(1)).onStatusUpdated(eq(CloudLoginState.ACCESS_DENIED), anyString());
    }

    @Test
    public void forbiddenClearsTokenAndSignalsAccessDenied() throws Exception {
        when(response.getStatus()).thenReturn(HttpStatus.FORBIDDEN_403);
        when(request.send()).thenThrow(new ExecutionException(new HttpResponseException("Forbidden", response)));

        assertThrows(MiCloudException.class, () -> connector.request(URL, PARAMS));

        assertEquals("", connector.getServiceToken());
        verify(listener, times(1)).onStatusUpdated(eq(CloudLoginState.ACCESS_DENIED), anyString());
    }

    @Test
    public void otherExecutionFailureKeepsTokenAndDoesNotSignalAccessDenied() throws Exception {
        when(response.getStatus()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR_500);
        when(request.send()).thenThrow(new ExecutionException(new HttpResponseException("Server error", response)));

        assertThrows(MiCloudException.class, () -> connector.request(URL, PARAMS));

        assertEquals("servicetoken", connector.getServiceToken());
        verify(listener, never()).onStatusUpdated(any(), anyString());
    }

    @Test
    public void testMapUrlRequestData() {
        assertEquals("{\"model\":\"roborock.vacuum.a08\",\"obj_name\":\"roboroommap/1234567/2\"}",
                MiCloudConnector.buildMapUrlRequestData("roboroommap%2F1234567%2F2", "roborock.vacuum.a08"));
        assertEquals("{\"obj_name\":\"robomap/1234567/0\"}",
                MiCloudConnector.buildMapUrlRequestData("robomap/1234567/0", ""));
    }
}
