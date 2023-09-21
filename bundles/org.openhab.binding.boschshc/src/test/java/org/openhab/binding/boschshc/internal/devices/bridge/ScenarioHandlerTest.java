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
package org.openhab.binding.boschshc.internal.devices.bridge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Scenario;

/**
 * Unit tests for {@link ScenarioHandler}.
 *
 * @author Patrick Gell - Initial contribution
 *
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
public class ScenarioHandlerTest {

    @Test
    public void executeScenario_ShouldLoadAllScenarios_IfAvailableScenariosAreEmpty() throws Exception {
        // GIVEN
        final var httpClient = mock(BoschHttpClient.class);
        final var request = mock(Request.class);
        final var response = mock(ContentResponse.class);
        when(httpClient.getBoschSmartHomeUrl("scenarios")).thenReturn("http://localhost/smartHome/scenarios");
        when(httpClient.createRequest(anyString(), any(HttpMethod.class))).thenReturn(request);
        when(request.send()).thenReturn(response);
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn(getJsonStringFromFile());

        final Map<String, Scenario> availableScenarios = new HashMap<>();
        final var handler = new ScenarioHandler(availableScenarios);

        // WHEN
        handler.executeScenario(httpClient, "fooBar");

        // THEN
        verify(httpClient).getBoschSmartHomeUrl("scenarios");
        assertEquals(3, availableScenarios.size());
    }

    @Test
    public void executeScenario_ShouldMakePostCall_IfScenarioExists() throws Exception {
        // GIVEN
        final var httpClient = mock(BoschHttpClient.class);
        final var request = mock(Request.class);
        final var response = mock(ContentResponse.class);
        final Map<String, Scenario> availableScenarios = new HashMap<>();
        final var testScenario = new Scenario();
        testScenario.id = UUID.randomUUID().toString();
        testScenario.name = "fooBar";
        availableScenarios.put(testScenario.name, testScenario);
        final var endpoint = String.format("scenarios/%s/triggers", testScenario.id);
        when(httpClient.getBoschSmartHomeUrl(endpoint)).thenReturn(endpoint);
        when(httpClient.createRequest(endpoint, HttpMethod.POST)).thenReturn(request);
        when(request.send()).thenReturn(response);
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn("");
        final var handler = new ScenarioHandler(availableScenarios);

        // WHEN
        handler.executeScenario(httpClient, testScenario.name);

        // THEN
        verify(request, times(1)).send();
    }

    private static Stream<Arguments> provideExceptionsForTest() {
        return Stream.of(Arguments.of(new InterruptedException("call interrupted")),
                Arguments.of(new TimeoutException("call timed out")),
                Arguments.of(new ExecutionException(new Exception())));
    }

    @ParameterizedTest
    @MethodSource("provideExceptionsForTest")
    public void executeScenario_ShouldNotThrowException_IfApiCallsHaveException(final Exception exception)
            throws Exception {
        // GIVEN
        final var httpClient = mock(BoschHttpClient.class);
        final var request = mock(Request.class);
        when(httpClient.getBoschSmartHomeUrl("scenarios")).thenReturn("http://localhost/smartHome/scenarios");
        when(httpClient.createRequest(anyString(), any(HttpMethod.class))).thenReturn(request);
        when(request.send()).thenThrow(exception);
        final Map<String, Scenario> availableScenarios = new HashMap<>();
        final var handler = new ScenarioHandler(availableScenarios);

        // WHEN
        handler.executeScenario(httpClient, "fooBar");

        // THEN
        assertTrue(availableScenarios.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(ints = { 404, 405 })
    public void executeScenario_ShouldNotThrowException_IfApiCallReturnsError(final int statusCode) throws Exception {
        // GIVEN
        final var httpClient = mock(BoschHttpClient.class);
        final var request = mock(Request.class);
        final var response = mock(ContentResponse.class);
        when(httpClient.getBoschSmartHomeUrl("scenarios")).thenReturn("http://localhost/smartHome/scenarios");
        when(httpClient.createRequest(anyString(), any(HttpMethod.class))).thenReturn(request);
        when(request.send()).thenReturn(response);
        when(response.getStatus()).thenReturn(statusCode);
        final Map<String, Scenario> availableScenarios = new HashMap<>();
        final var handler = new ScenarioHandler(availableScenarios);

        // WHEN
        handler.executeScenario(httpClient, "fooBar");

        // THEN
        assertTrue(availableScenarios.isEmpty());
    }

    @Test
    public void executeScenario_ShouldNotThrowException_IfResponseIsNoJson() throws Exception {
        // GIVEN
        final var httpClient = mock(BoschHttpClient.class);
        final var request = mock(Request.class);
        final var response = mock(ContentResponse.class);
        when(httpClient.getBoschSmartHomeUrl("scenarios")).thenReturn("http://localhost/smartHome/scenarios");
        when(httpClient.createRequest(anyString(), any(HttpMethod.class))).thenReturn(request);
        when(request.send()).thenReturn(response);
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn("this is not a valid json");

        final Map<String, Scenario> availableScenarios = new HashMap<>();
        final var handler = new ScenarioHandler(availableScenarios);

        // WHEN
        handler.executeScenario(httpClient, "fooBar");

        // THEN
        assertTrue(availableScenarios.isEmpty());
    }

    private String getJsonStringFromFile() throws IOException {
        try (InputStream input = this.getClass().getClassLoader()
                .getResourceAsStream("scenarios/GET_scenarios_result.json")) {
            if (input == null) {
                return "";
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                return stringBuilder.toString();
            }
        }
    }
}
