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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
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

    private @NonNullByDefault({}) ScenarioHandler fixture;

    private @NonNullByDefault({}) @Mock BoschHttpClient httpClient;
    private @NonNullByDefault({}) @Mock Request request;

    @BeforeEach
    void beforeEach() {
        fixture = new ScenarioHandler();
    }

    @Test
    void triggerScenarioShouldSendPOSTToBoschAPI() throws Exception {
        // GIVEN
        final var contentResponse = mock(ContentResponse.class);
        when(httpClient.getBoschSmartHomeUrl(anyString())).thenReturn("http://localhost/smartHome/scenarios")
                .thenReturn("http://localhost/smartHome/scenarios/1234/triggers");
        when(httpClient.createRequest(anyString(), any(HttpMethod.class))).thenReturn(request).thenReturn(request);
        when(httpClient.sendRequest(any(Request.class), any(), any(), any())).thenReturn(existingScenarios);
        when(request.send()).thenReturn(contentResponse);
        when(contentResponse.getStatus()).thenReturn(HttpStatus.OK_200);

        // WHEN
        fixture.triggerScenario(httpClient, "Scenario 1");

        // THEN
        verify(httpClient).getBoschSmartHomeUrl("scenarios");
        verify(request).send();
    }

    @Test
    void triggerScenarioShouldNotSendPOSTToScenarioNameDoesNotExist() throws Exception {
        // GIVEN
        when(httpClient.getBoschSmartHomeUrl(anyString())).thenReturn("http://localhost/smartHome/scenarios")
                .thenReturn("http://localhost/smartHome/scenarios/1234/triggers");
        when(httpClient.createRequest(anyString(), any(HttpMethod.class))).thenReturn(request).thenReturn(request);
        when(httpClient.sendRequest(any(Request.class), any(), any(), any())).thenReturn(existingScenarios);

        // WHEN
        fixture.triggerScenario(httpClient, "not existing Scenario");

        // THEN
        verify(httpClient).getBoschSmartHomeUrl("scenarios");
        verify(request, times(0)).send();
    }

    @ParameterizedTest
    @MethodSource("exceptionData")
    void triggerScenarioShouldNotPanicIfBoschAPIThrowsException(final Exception exception) throws Exception {
        // GIVEN
        when(httpClient.getBoschSmartHomeUrl(anyString())).thenReturn("http://localhost/smartHome/scenarios")
                .thenReturn("http://localhost/smartHome/scenarios/1234/triggers");
        when(httpClient.createRequest(anyString(), any(HttpMethod.class))).thenReturn(request);
        when(httpClient.sendRequest(any(Request.class), any(), any(), any())).thenThrow(exception);

        // WHEN
        fixture.triggerScenario(httpClient, "Scenario 1");

        // THEN
        verify(httpClient).getBoschSmartHomeUrl("scenarios");
        verify(request, times(0)).send();
    }

    @Test
    void triggerScenarioShouldNotPanicIfPOSTIsNotSuccessful() throws Exception {
        // GIVEN
        final var contentResponse = mock(ContentResponse.class);
        when(httpClient.getBoschSmartHomeUrl(anyString())).thenReturn("http://localhost/smartHome/scenarios")
                .thenReturn("http://localhost/smartHome/scenarios/1234/triggers");
        when(httpClient.createRequest(anyString(), any(HttpMethod.class))).thenReturn(request).thenReturn(request);
        when(httpClient.sendRequest(any(Request.class), any(), any(), any())).thenReturn(existingScenarios);
        when(request.send()).thenReturn(contentResponse);
        when(contentResponse.getStatus()).thenReturn(HttpStatus.METHOD_NOT_ALLOWED_405);

        // WHEN
        fixture.triggerScenario(httpClient, "Scenario 1");

        // THEN
        verify(httpClient).getBoschSmartHomeUrl("scenarios");
        verify(request).send();
    }

    @ParameterizedTest
    @MethodSource("httpExceptionData")
    void triggerScenarioShouldNotPanicIfPOSTThrowsException(final Exception exception) throws Exception {
        // GIVEN
        when(httpClient.getBoschSmartHomeUrl(anyString())).thenReturn("http://localhost/smartHome/scenarios")
                .thenReturn("http://localhost/smartHome/scenarios/1234/triggers");
        when(httpClient.createRequest(anyString(), any(HttpMethod.class))).thenReturn(request).thenReturn(request);
        when(httpClient.sendRequest(any(Request.class), any(), any(), any())).thenReturn(existingScenarios);
        when(request.send()).thenThrow(exception);

        // WHEN
        fixture.triggerScenario(httpClient, "Scenario 1");

        // THEN
        verify(httpClient).getBoschSmartHomeUrl("scenarios");
        verify(request).send();
    }

    @Test
    void prettyLogScenarios() {
        Scenario scenario1 = Scenario.createScenario("id1", "Scenario 1", "1708619045411");
        Scenario scenario2 = Scenario.createScenario("id2", "Scenario 2", "1708619065445");
        assertEquals(
                "[\n" + "  Scenario{name='Scenario 1', id='id1', lastTimeTriggered='1708619045411'}\n"
                        + "  Scenario{name='Scenario 2', id='id2', lastTimeTriggered='1708619065445'}\n" + "]",
                fixture.prettyLogScenarios(new Scenario[] { scenario1, scenario2 }));
    }
}
