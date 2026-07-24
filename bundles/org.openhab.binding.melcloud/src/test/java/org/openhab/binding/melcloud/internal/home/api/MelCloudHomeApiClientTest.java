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
package org.openhab.binding.melcloud.internal.home.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mockStatic;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Optional;
import java.util.Properties;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.openhab.binding.melcloud.internal.exceptions.MelCloudCommException;
import org.openhab.binding.melcloud.internal.home.api.dto.MelCloudHomeAtaControlRequest;
import org.openhab.binding.melcloud.internal.home.api.dto.MelCloudHomeUserContext;
import org.openhab.core.io.net.http.HttpUtil;

/**
 * Unit tests for {@link MelCloudHomeApiClient} (see ADR-003). {@link HttpUtil}'s static {@code executeUrl} method is
 * mocked via Mockito's inline mock maker ({@link MockedStatic}) so the wire-level contract can be verified without
 * any real network access.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class MelCloudHomeApiClientTest {

    private static final String ACCESS_TOKEN = "test-access-token";
    private static final String UNIT_ID = "unit-1";

    private final MelCloudHomeApiClient client = new MelCloudHomeApiClient();

    private @Nullable MockedStatic<HttpUtil> httpUtilMock;

    @BeforeEach
    void setUp() {
        httpUtilMock = mockStatic(HttpUtil.class);
    }

    @AfterEach
    void tearDown() {
        MockedStatic<HttpUtil> mock = httpUtilMock;
        if (mock != null) {
            mock.close();
        }
    }

    @Test
    void whenContextResponseIsValid_thenFetchUserContextParsesIt() throws MelCloudCommException {
        // Arrange
        String json = "{\"buildings\":[{\"id\":\"b1\",\"name\":\"Home\",\"airToAirUnits\":[{\"id\":\"" + UNIT_ID
                + "\",\"givenDisplayName\":\"Living Room\"}],\"airToWaterUnits\":[]}],\"guestBuildings\":[]}";
        mockGetResponse(json);

        // Act
        MelCloudHomeUserContext context = client.fetchUserContext(ACCESS_TOKEN);

        // Assert
        assertEquals(1, context.getAllAtaUnits().size());
        assertEquals(UNIT_ID, context.getAllAtaUnits().get(0).id);
    }

    @Test
    void whenContextResponseIsEmptyBody_thenFetchUserContextThrows() {
        // Arrange
        mockGetResponse("");

        // Act & Assert
        assertThrows(MelCloudCommException.class, () -> client.fetchUserContext(ACCESS_TOKEN));
    }

    @Test
    void whenContextResponseIsMalformedJson_thenFetchUserContextThrows() {
        // Arrange
        mockGetResponse("{not-json");

        // Act & Assert
        assertThrows(MelCloudCommException.class, () -> client.fetchUserContext(ACCESS_TOKEN));
    }

    @Test
    void whenServerReturnsClientError_thenFetchUserContextThrowsCommException() {
        // Arrange
        mockGetError(new IOException("Server returned HTTP response code: 401 for URL"));

        // Act & Assert
        assertThrows(MelCloudCommException.class, () -> client.fetchUserContext(ACCESS_TOKEN));
    }

    @Test
    void whenServerReturnsServerError_thenFetchUserContextThrowsCommException() {
        // Arrange
        mockGetError(new IOException("Server returned HTTP response code: 500 for URL"));

        // Act & Assert
        assertThrows(MelCloudCommException.class, () -> client.fetchUserContext(ACCESS_TOKEN));
    }

    @Test
    void whenConnectionTimesOut_thenFetchUserContextThrowsCommException() {
        // Arrange
        mockGetError(new IOException("Read timed out"));

        // Act & Assert
        assertThrows(MelCloudCommException.class, () -> client.fetchUserContext(ACCESS_TOKEN));
    }

    @Test
    void whenEnergyTelemetryResponseIsEmpty_thenFetchLatestEnergyWhReturnsEmpty() throws MelCloudCommException {
        // Arrange
        mockGetResponse("");

        // Act
        Optional<Double> latest = client.fetchLatestEnergyWh(ACCESS_TOKEN, UNIT_ID, Instant.EPOCH, Instant.now(),
                "cumulative_energy_consumed_since_last_upload");

        // Assert
        assertTrue(latest.isEmpty());
    }

    @Test
    void whenControlAtaUnitIsCalled_thenUnsetFieldsAreSerializedAsExplicitNulls()
            throws MelCloudCommException, IOException {
        // Arrange
        ArgumentCaptor<InputStream> bodyCaptor = ArgumentCaptor.forClass(InputStream.class);
        MockedStatic<HttpUtil> mock = httpUtilMock;
        assertNotNull(mock);
        mock.when(() -> HttpUtil.executeUrl(eq("PUT"), anyString(), any(Properties.class), bodyCaptor.capture(),
                eq("application/json"), anyInt())).thenReturn("");

        MelCloudHomeAtaControlRequest request = new MelCloudHomeAtaControlRequest();
        request.power = true;

        // Act
        client.controlAtaUnit(ACCESS_TOKEN, UNIT_ID, request);

        // Assert
        String body = new String(bodyCaptor.getValue().readAllBytes());
        assertTrue(body.contains("\"power\":true"));
        assertTrue(body.contains("\"operationMode\":null"));
    }

    private void mockGetResponse(String response) {
        MockedStatic<HttpUtil> mock = httpUtilMock;
        assertNotNull(mock);
        mock.when(
                () -> HttpUtil.executeUrl(eq("GET"), anyString(), any(Properties.class), isNull(), isNull(), anyInt()))
                .thenReturn(response);
    }

    private void mockGetError(IOException exception) {
        MockedStatic<HttpUtil> mock = httpUtilMock;
        assertNotNull(mock);
        mock.when(
                () -> HttpUtil.executeUrl(eq("GET"), anyString(), any(Properties.class), isNull(), isNull(), anyInt()))
                .thenThrow(exception);
    }
}
