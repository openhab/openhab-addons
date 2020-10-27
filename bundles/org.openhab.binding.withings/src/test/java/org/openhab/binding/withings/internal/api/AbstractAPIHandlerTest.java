/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.withings.internal.api;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.withings.internal.service.AccessTokenService;

/**
 * @author Sven Strohschein - Initial contribution
 */
public abstract class AbstractAPIHandlerTest {

    protected AccessTokenService accessTokenServiceMock;
    protected HttpClient httpClientMock;

    public AbstractAPIHandlerTest() {
        accessTokenServiceMock = mock(AccessTokenService.class);
        httpClientMock = mock(HttpClient.class);
    }

    protected void mockAccessToken() {
        when(accessTokenServiceMock.getAccessToken()).thenReturn(Optional.of("accessToken"));
    }

    protected void mockRequest(String response) {
        Request requestMock = createRequestMock();

        when(httpClientMock.POST(anyString())).thenReturn(requestMock);

        ContentResponse responseMock = mock(ContentResponse.class);
        when(responseMock.getContentAsString()).thenReturn(response);

        try {
            when(requestMock.send()).thenReturn(responseMock);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    protected void mockRequestWithException() {
        Request requestMock = createRequestMock();

        when(httpClientMock.POST(anyString())).thenReturn(requestMock);

        try {
            when(requestMock.send()).thenThrow(new TimeoutException());
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private static Request createRequestMock() {
        Request requestMock = mock(Request.class);
        when(requestMock.timeout(anyLong(), any())).thenReturn(requestMock);
        when(requestMock.idleTimeout(anyLong(), any())).thenReturn(requestMock);
        when(requestMock.header(any(HttpHeader.class), any())).thenReturn(requestMock);
        when(requestMock.content(any())).thenReturn(requestMock);
        return requestMock;
    }
}
