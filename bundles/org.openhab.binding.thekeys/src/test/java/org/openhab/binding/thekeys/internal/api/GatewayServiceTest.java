/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.thekeys.internal.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

import java.io.EOFException;
import java.io.IOException;
import java.net.ConnectException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.openhab.binding.thekeys.internal.gateway.TheKeysGatewayConfiguration;

/**
 * Test the gateway api
 *
 * @author Jordan Martin - Initial contribution
 */
@NonNullByDefault
class GatewayServiceTest {

    @Test
    void shouldGenerateCorrectEncodedData() {
        String timestamp = "1647349435";
        String encoded = GatewayService.hmacSha256(timestamp, "sEcReT");
        assertEquals("jejmupCqAu5wOsraszmzcl3UyCi0hgsIdxWvfcvdzBg=", encoded);
    }

    @Test
    void shouldComputeCorrectRequestBody() {
        GatewayService gatewayService = getGatewayApi(new TheKeysHttpClient());

        String requestBody = gatewayService.computeRequestBodyWithHash("1647350684", 123456);

        assertEquals("identifier=123456&ts=1647350684&hash=fcbTI/x8qKsN/nhLxjRE5toEO3tDK/F0ubQXHCsnf88=", requestBody);
    }

    @ParameterizedTest
    @ValueSource(classes = { ConnectException.class, EOFException.class })
    void shouldRetryOnSpecificExceptions(Class<? extends Throwable> clazz) throws IOException {
        // Given
        TheKeysHttpClient httpMock = Mockito.mock(TheKeysHttpClient.class);
        Mockito.when(httpMock.get(any(), anyInt(), any())).thenThrow(clazz);
        GatewayService gatewayService = getGatewayApi(httpMock);

        // When
        assertThrows(IOException.class, gatewayService::getLocks);

        // Then
        Mockito.verify(httpMock, Mockito.times(4)).get(any(), anyInt(), any());
    }

    @Test
    void shouldNotRetryForOthersExceptions() throws IOException {
        // Given
        TheKeysHttpClient httpMock = Mockito.mock(TheKeysHttpClient.class);
        Mockito.when(httpMock.get(any(), anyInt(), any())).thenThrow(IOException.class);
        GatewayService gatewayService = getGatewayApi(httpMock);

        // When
        assertThrows(IOException.class, gatewayService::getLocks);

        // Then
        Mockito.verify(httpMock, Mockito.times(1)).get(any(), anyInt(), any());
    }

    private GatewayService getGatewayApi(TheKeysHttpClient httpClient) {
        TheKeysGatewayConfiguration conf = new TheKeysGatewayConfiguration();
        conf.code = "sEcReT";
        return new GatewayService(conf, httpClient);
    }
}
