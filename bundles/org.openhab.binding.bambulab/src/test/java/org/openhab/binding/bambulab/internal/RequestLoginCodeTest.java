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
package org.openhab.binding.bambulab.internal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.thing.Bridge;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class RequestLoginCodeTest {
    @Test
    @DisplayName("should not throw exception when response is correct")
    void requestLoginCode_shouldNotThrowException_whenResponseIsCorrect() throws Exception {
        // given
        var httpClient = mock(HttpClient.class);
        var request = mock(Request.class);
        var response = mock(ContentResponse.class);
        when(httpClient.POST(anyString())).thenReturn(request);
        when(request.send()).thenReturn(response);
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn("{\"loginType\": \"verifyCode\"}");
        var printerHandler = new PrinterHandler(mock(Bridge.class), httpClient);

        // when
        printerHandler.requestLoginCode("user", "pass");

        // then
        // no exception
    }

    @Test
    @DisplayName("should throw exception when response is incorrect")
    void requestLoginCode_shouldThrowException_whenResponseIsIncorrect() throws Exception {
        // given
        var httpClient = mock(HttpClient.class);
        var request = mock(Request.class);
        var response = mock(ContentResponse.class);
        when(httpClient.POST(anyString())).thenReturn(request);
        when(request.send()).thenReturn(response);
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn("{\"error\": \"Incorrect password\"}");
        var printerHandler = new PrinterHandler(mock(Bridge.class), httpClient);

        // when
        assertThrows(BambuApiException.class, () -> printerHandler.requestLoginCode("user", "pass"));
    }

    @ParameterizedTest
    @ValueSource(classes = { InterruptedException.class, TimeoutException.class, ExecutionException.class })
    @DisplayName("should throw exception when response is http error")
    void requestLoginCode_shouldThrowException_whenResponseIsHttpError(Class<Exception> exceptionClass)
            throws Exception {
        // given
        var httpClient = mock(HttpClient.class);
        var request = mock(Request.class);
        when(httpClient.POST(anyString())).thenReturn(request);
        when(request.send()).thenThrow(exceptionClass);
        var printerHandler = new PrinterHandler(mock(Bridge.class), httpClient);

        // when
        assertThrows(BambuApiException.class, () -> printerHandler.requestLoginCode("user", "pass"));
    }

    @Test
    @DisplayName("should throw exception when response is invalid json")
    void requestLoginCode_shouldThrowException_whenResponseIsInvalidJson() throws Exception {
        // given
        var httpClient = mock(HttpClient.class);
        var request = mock(Request.class);
        var response = mock(ContentResponse.class);
        when(httpClient.POST(anyString())).thenReturn(request);
        when(request.send()).thenReturn(response);
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn("not a json");
        var printerHandler = new PrinterHandler(mock(Bridge.class), httpClient);

        // when
        assertThrows(BambuApiException.class, () -> printerHandler.requestLoginCode("user", "pass"));
    }

    @Test
    @DisplayName("should throw exception when loginType is invalid")
    void requestLoginCode_shouldThrowException_whenLoginTypeIsInvalid() throws Exception {
        // given
        var httpClient = mock(HttpClient.class);
        var request = mock(Request.class);
        var response = mock(ContentResponse.class);
        when(httpClient.POST(anyString())).thenReturn(request);
        when(request.send()).thenReturn(response);
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn("{\"loginType\": \"invalid\"}");
        var printerHandler = new PrinterHandler(mock(Bridge.class), httpClient);

        // when
        assertThrows(BambuApiException.class, () -> printerHandler.requestLoginCode("user", "pass"));
    }
}
