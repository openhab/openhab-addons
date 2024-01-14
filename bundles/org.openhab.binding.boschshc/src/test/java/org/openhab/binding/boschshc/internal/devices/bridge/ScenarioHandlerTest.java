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
package org.openhab.binding.boschshc.internal.devices.bridge;

import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Scenario;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;

/**
 * Unit tests for {@link ScenarioHandler}.
 *
 * @author Patrick Gell - Initial contribution
 *
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class ScenarioHandlerTest {

    private final Scenario[] existingScenarios = List.of(
            Scenario.createScenario(UUID.randomUUID().toString(), "Scenario 1",
                    String.valueOf(System.currentTimeMillis())),
            Scenario.createScenario(UUID.randomUUID().toString(), "Scenario 2",
                    String.valueOf(System.currentTimeMillis()))

    ).toArray(Scenario[]::new);

    protected static Exception[] exceptionData() {
        return List.of(new BoschSHCException(), new InterruptedException(), new TimeoutException(),
                new ExecutionException(new BoschSHCException())).toArray(Exception[]::new);
    }

    protected static Exception[] httpExceptionData() {
        return List
                .of(new InterruptedException(), new TimeoutException(), new ExecutionException(new BoschSHCException()))
                .toArray(Exception[]::new);
    }

    @Test
    void triggerScenarioShouldSendPOSTToBoschAPI() throws Exception {
        // GIVEN
        final var httpClient = mock(BoschHttpClient.class);
        final var request = mock(Request.class);
        final var contentResponse = mock(ContentResponse.class);
        when(httpClient.getBoschSmartHomeUrl(anyString())).thenReturn("http://localhost/smartHome/scenarios")
                .thenReturn("http://localhost/smartHome/scenarios/1234/triggers");
        when(httpClient.createRequest(anyString(), any(HttpMethod.class))).thenReturn(request).thenReturn(request);
        when(httpClient.sendRequest(any(Request.class), any(), any(), any())).thenReturn(existingScenarios);
        when(request.send()).thenReturn(contentResponse);
        when(contentResponse.getStatus()).thenReturn(HttpStatus.OK_200);

        final var handler = new ScenarioHandler();

        // WHEN
        handler.triggerScenario(httpClient, "Scenario 1");

        // THEN
        verify(httpClient).getBoschSmartHomeUrl("scenarios");
        verify(request).send();
    }

    @Test
    void triggerScenarioShouldNoSendPOSTToScenarioNameDoesNotExist() throws Exception {
        // GIVEN
        final var httpClient = mock(BoschHttpClient.class);
        final var request = mock(Request.class);
        when(httpClient.getBoschSmartHomeUrl(anyString())).thenReturn("http://localhost/smartHome/scenarios")
                .thenReturn("http://localhost/smartHome/scenarios/1234/triggers");
        when(httpClient.createRequest(anyString(), any(HttpMethod.class))).thenReturn(request).thenReturn(request);
        when(httpClient.sendRequest(any(Request.class), any(), any(), any())).thenReturn(existingScenarios);

        final var handler = new ScenarioHandler();

        // WHEN
        handler.triggerScenario(httpClient, "not existing Scenario");

        // THEN
        verify(httpClient).getBoschSmartHomeUrl("scenarios");
        verify(request, times(0)).send();
    }

    @ParameterizedTest
    @MethodSource("exceptionData")
    void triggerScenarioShouldNotPanicIfBoschAPIThrowsException(final Exception exception) throws Exception {
        // GIVEN
        final var httpClient = mock(BoschHttpClient.class);
        final var request = mock(Request.class);
        when(httpClient.getBoschSmartHomeUrl(anyString())).thenReturn("http://localhost/smartHome/scenarios")
                .thenReturn("http://localhost/smartHome/scenarios/1234/triggers");
        when(httpClient.createRequest(anyString(), any(HttpMethod.class))).thenReturn(request);
        when(httpClient.sendRequest(any(Request.class), any(), any(), any())).thenThrow(exception);

        final var handler = new ScenarioHandler();

        // WHEN
        handler.triggerScenario(httpClient, "Scenario 1");

        // THEN
        verify(httpClient).getBoschSmartHomeUrl("scenarios");
        verify(request, times(0)).send();
    }

    @Test
    void triggerScenarioShouldNotPanicIfPOSTIsNotSuccessful() throws Exception {
        // GIVEN
        final var httpClient = mock(BoschHttpClient.class);
        final var request = mock(Request.class);
        final var contentResponse = mock(ContentResponse.class);
        when(httpClient.getBoschSmartHomeUrl(anyString())).thenReturn("http://localhost/smartHome/scenarios")
                .thenReturn("http://localhost/smartHome/scenarios/1234/triggers");
        when(httpClient.createRequest(anyString(), any(HttpMethod.class))).thenReturn(request).thenReturn(request);
        when(httpClient.sendRequest(any(Request.class), any(), any(), any())).thenReturn(existingScenarios);
        when(request.send()).thenReturn(contentResponse);
        when(contentResponse.getStatus()).thenReturn(HttpStatus.METHOD_NOT_ALLOWED_405);

        final var handler = new ScenarioHandler();

        // WHEN
        handler.triggerScenario(httpClient, "Scenario 1");

        // THEN
        verify(httpClient).getBoschSmartHomeUrl("scenarios");
        verify(request).send();
    }

    @ParameterizedTest
    @MethodSource("httpExceptionData")
    void triggerScenarioShouldNotPanicIfPOSTThrowsException(final Exception exception) throws Exception {
        // GIVEN
        final var httpClient = mock(BoschHttpClient.class);
        final var request = mock(Request.class);
        when(httpClient.getBoschSmartHomeUrl(anyString())).thenReturn("http://localhost/smartHome/scenarios")
                .thenReturn("http://localhost/smartHome/scenarios/1234/triggers");
        when(httpClient.createRequest(anyString(), any(HttpMethod.class))).thenReturn(request).thenReturn(request);
        when(httpClient.sendRequest(any(Request.class), any(), any(), any())).thenReturn(existingScenarios);
        when(request.send()).thenThrow(exception);

        final var handler = new ScenarioHandler();

        // WHEN
        handler.triggerScenario(httpClient, "Scenario 1");

        // THEN
        verify(httpClient).getBoschSmartHomeUrl("scenarios");
        verify(request).send();
    }
}
