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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class RequestAccessCodeTest {
    @Test
    @DisplayName("should update configuration when response is correct")
    void requestAccessCode_shouldUpdateConfiguration_whenResponseIsCorrect() throws Exception {
        // given
        var httpClient = mock(HttpClient.class);
        var request = mock(Request.class);
        var response = mock(ContentResponse.class);
        var bridge = mock(Bridge.class);
        var configuration = new Configuration();
        var printerHandler = new PrinterHandler(bridge, httpClient) {
            @Override
            public Configuration editConfiguration() {
                return configuration;
            }

            @Override
            public void updateConfiguration(Configuration configuration) {
                // do nothing
            }
        };
        when(httpClient.POST(anyString())).thenReturn(request);
        when(request.send()).thenReturn(response);
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn("{\"accessToken\": \"new-access-token\"}");

        // when
        printerHandler.requestAccessCode("user", "code");

        // then
        assertThat(configuration.get("accessCode")).isEqualTo("new-access-token");
        assertThat(configuration.get("hostname")).isEqualTo(PrinterConfiguration.CLOUD_MODE_HOSTNAME);
    }

    @Test
    @DisplayName("should throw exception when response is incorrect")
    void requestAccessCode_shouldThrowException_whenResponseIsIncorrect() throws Exception {
        // given
        var httpClient = mock(HttpClient.class);
        var request = mock(Request.class);
        var response = mock(ContentResponse.class);
        var bridge = mock(Bridge.class);
        var printerHandler = spy(new PrinterHandler(bridge, httpClient));
        var configuration = new Configuration();
        lenient().when(bridge.getConfiguration()).thenReturn(configuration);
        lenient().doReturn(bridge).when(printerHandler).getThing();
        when(httpClient.POST(anyString())).thenReturn(request);
        when(request.send()).thenReturn(response);
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn("{\"error\": \"Incorrect code\"}");

        // when
        assertThrows(BambuApiException.class, () -> printerHandler.requestAccessCode("user", "code"));
    }

    @ParameterizedTest
    @ValueSource(classes = { InterruptedException.class, TimeoutException.class, ExecutionException.class })
    @DisplayName("should throw exception when response is http error")
    void requestAccessCode_shouldThrowException_whenResponseIsHttpError(Class<Exception> exceptionClass)
            throws Exception {
        // given
        var httpClient = mock(HttpClient.class);
        var request = mock(Request.class);
        var bridge = mock(Bridge.class);
        var printerHandler = spy(new PrinterHandler(bridge, httpClient));
        lenient().doReturn(bridge).when(printerHandler).getThing();
        when(httpClient.POST(anyString())).thenReturn(request);
        when(request.send()).thenThrow(exceptionClass);

        // when
        assertThrows(BambuApiException.class, () -> printerHandler.requestAccessCode("user", "code"));
    }

    @Test
    @DisplayName("should throw exception when response is invalid json")
    void requestAccessCode_shouldThrowException_whenResponseIsInvalidJson() throws Exception {
        // given
        var httpClient = mock(HttpClient.class);
        var request = mock(Request.class);
        var response = mock(ContentResponse.class);
        var bridge = mock(Bridge.class);
        var printerHandler = spy(new PrinterHandler(bridge, httpClient));
        var configuration = new Configuration();
        lenient().when(bridge.getConfiguration()).thenReturn(configuration);
        lenient().doReturn(bridge).when(printerHandler).getThing();
        when(httpClient.POST(anyString())).thenReturn(request);
        when(request.send()).thenReturn(response);
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn("not a json");

        // when
        assertThrows(BambuApiException.class, () -> printerHandler.requestAccessCode("user", "code"));
    }
}
