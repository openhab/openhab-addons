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
package org.openhab.binding.bmwcardata.internal.api;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.WWWAuthenticationProtocolHandler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.bmwcardata.internal.MapStorage;
import org.openhab.binding.bmwcardata.internal.api.dto.TelematicValue;
import org.openhab.binding.bmwcardata.internal.api.dto.VehicleMapping;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.google.gson.Gson;
import com.google.gson.JsonPrimitive;

/**
 * Tests the {@link BMWCarDataApi}.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
class BMWCarDataApiTest {

    private static final WireMockServer SERVER = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    private static final HttpClient HTTP_CLIENT = new HttpClient();
    private static final String VIN = "WBA12345678901234";

    private @NonNullByDefault({}) BMWCarDataApi api;
    private @NonNullByDefault({}) RequestBudget budget;

    @BeforeAll
    static void startServer() throws Exception {
        SERVER.start();
        HTTP_CLIENT.start();
        // as configured by the handler factory
        HTTP_CLIENT.getProtocolHandlers().remove(WWWAuthenticationProtocolHandler.NAME);
    }

    @AfterAll
    static void stopServer() throws Exception {
        SERVER.stop();
        HTTP_CLIENT.stop();
    }

    @BeforeEach
    void setUp() {
        SERVER.resetAll();
        budget = new RequestBudget(new MapStorage(), "budget", 3, Clock.systemUTC());
        api = new BMWCarDataApi(HTTP_CLIENT, new Gson(), () -> "token", budget, "http://localhost:" + SERVER.port());
    }

    @Test
    void getTelematicDataSendsHeadersAndContainer() throws Exception {
        SERVER.stubFor(get(urlPathEqualTo("/customers/vehicles/" + VIN + "/telematicData")).willReturn(okJson("""
                {"telematicData":{"vehicle.vehicle.travelledDistance":
                  {"value":"12345","unit":"km","timestamp":"2026-10-01T10:00:00.000Z"}}}""")));

        Map<String, TelematicValue> data = api.getTelematicData(VIN, "container-1");

        TelematicValue value = data.get("vehicle.vehicle.travelledDistance");
        assertNotNull(value);
        assertEquals("km", value.unit);
        assertEquals(new JsonPrimitive("12345"), value.value);
        SERVER.verify(getRequestedFor(urlPathEqualTo("/customers/vehicles/" + VIN + "/telematicData"))
                .withQueryParam("containerId", equalTo("container-1"))
                .withHeader("Authorization", equalTo("Bearer token")).withHeader("x-version", equalTo("v1")));
    }

    @Test
    void getVehicleMappingsAcceptsArrayAndObject() throws Exception {
        SERVER.stubFor(get("/customers/vehicles/mappings").willReturn(okJson("""
                [{"vin":"%s","mappingType":"PRIMARY","mappedSince":"2025-01-01T00:00:00Z"}]""".formatted(VIN))));
        List<VehicleMapping> mappings = api.getVehicleMappings();
        assertEquals(1, mappings.size());
        assertEquals(VIN, mappings.get(0).vin);
        assertEquals(VehicleMapping.MAPPING_TYPE_PRIMARY, mappings.get(0).mappingType);

        SERVER.stubFor(get("/customers/vehicles/mappings").willReturn(okJson("""
                {"mappings":[{"vin":"%s","mappingType":"SECONDARY"}]}""".formatted(VIN))));
        assertEquals("SECONDARY", api.getVehicleMappings().get(0).mappingType);

        SERVER.stubFor(get("/customers/vehicles/mappings").willReturn(okJson("""
                {"vin":"%s","mappingType":"PRIMARY"}""".formatted(VIN))));
        mappings = api.getVehicleMappings();
        assertEquals(1, mappings.size());
        assertEquals(VIN, mappings.get(0).vin);

        SERVER.stubFor(get("/customers/vehicles/mappings").willReturn(okJson("{\"unexpected\":true}")));
        assertThrows(BMWCarDataException.class, () -> api.getVehicleMappings());
    }

    @Test
    void createContainerReturnsId() throws Exception {
        SERVER.stubFor(post("/customers/containers").willReturn(okJson("{\"containerId\":\"c-1\"}")));

        assertEquals("c-1", api.createContainer("openHAB", "purpose", List.of("vehicle.isMoving")));

        SERVER.verify(postRequestedFor(urlEqualTo("/customers/containers"))
                .withRequestBody(matchingJsonPath("$.technicalDescriptors[0]", equalTo("vehicle.isMoving")))
                .withRequestBody(matchingJsonPath("$.name", equalTo("openHAB"))));
    }

    @Test
    void requestsAreCountedAndWithheldWhenBudgetIsUsedUp() throws Exception {
        String path = "/customers/vehicles/" + VIN + "/basicData";
        SERVER.stubFor(get(path).willReturn(okJson("{\"brand\":\"BMW\"}")));

        for (int i = 0; i < 3; i++) {
            api.getBasicData(VIN);
        }
        BMWCarDataException e = assertThrows(BMWCarDataException.class, () -> api.getBasicData(VIN));

        assertTrue(e.isBudgetExhausted());
        assertTrue(e.isRateLimited());
        assertEquals(3, budget.used());
        SERVER.verify(3, getRequestedFor(urlEqualTo(path)));
    }

    @Test
    void bmwRateLimitMarksBudgetExhausted() {
        String path = "/customers/vehicles/" + VIN + "/basicData";
        SERVER.stubFor(get(path).willReturn(
                aResponse().withStatus(403).withBody("{\"exveErrorId\":\"CU-429\",\"exveErrorMsg\":\"Rate limit\"}")));

        BMWCarDataException e = assertThrows(BMWCarDataException.class, () -> api.getBasicData(VIN));

        assertEquals("CU-429", e.getErrorId());
        assertTrue(e.isRateLimited());
        assertFalse(e.isBudgetExhausted());
        assertFalse(budget.hasRoomFor(1));
    }

    @Test
    void errorResponsesAreClassified() {
        String path = "/customers/vehicles/" + VIN + "/basicData";
        SERVER.stubFor(get(path).willReturn(unauthorized()));
        assertThrows(BMWCarDataAuthException.class, () -> api.getBasicData(VIN));

        SERVER.stubFor(get(path).willReturn(aResponse().withStatus(429).withBody("{\"exveErrorId\":\"CU-429\"}")));
        BMWCarDataException e = assertThrows(BMWCarDataException.class, () -> api.getBasicData(VIN));
        assertTrue(e.isRateLimited());
        assertFalse(e instanceof BMWCarDataAuthException);
    }

    @Test
    void invalidIdentifiersAreRejectedWithoutRequest() {
        assertThrows(BMWCarDataException.class, () -> api.getBasicData("../containers"));
        assertThrows(BMWCarDataException.class, () -> api.getTelematicData(VIN, "a/b"));
        assertTrue(SERVER.getAllServeEvents().isEmpty());
    }
}
