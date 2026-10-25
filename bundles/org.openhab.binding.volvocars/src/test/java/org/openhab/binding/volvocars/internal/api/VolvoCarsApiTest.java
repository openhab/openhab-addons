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
package org.openhab.binding.volvocars.internal.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.volvocars.internal.VolvoCarsBindingConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.volvocars.internal.api.dto.CommandResult;
import org.openhab.binding.volvocars.internal.api.dto.Location;
import org.openhab.binding.volvocars.internal.api.dto.ValueField;
import org.openhab.binding.volvocars.internal.api.dto.Vehicle;
import org.openhab.binding.volvocars.internal.config.AccountConfiguration;
import org.openhab.binding.volvocars.internal.servlet.VolvoCarsAuthServletTestAccess;
import org.openhab.core.test.storage.VolatileStorage;

/**
 * Tests the parsing of Volvo Cars API responses by {@link VolvoCarsApi}.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
class VolvoCarsApiTest {
    private static final String VIN = "YV1TESTVIN0000001";
    private static final String VEHICLE_URL = CONNECTED_VEHICLE_ENDPOINT + "/" + VIN;

    private final HttpMocks http = new HttpMocks();
    private final VolvoCarsApi api;

    VolvoCarsApiTest() throws VolvoCarsException {
        AccountConfiguration config = new AccountConfiguration();
        config.clientId = "client";
        config.clientSecret = "secret";
        config.redirectUri = "http://openhab:8080/volvocars";
        VolatileStorage<String> storage = new VolatileStorage<>();
        VolvoCarsOAuthClient oAuthClient = new VolvoCarsOAuthClient(http.httpClient, storage, "test", config);
        String state = Objects.requireNonNull(
                VolvoCarsAuthServletTestAccess.parseQuery(oAuthClient.createAuthorizationUrl()).get("state"));
        http.respond(TOKEN_URL, 200, "{\"access_token\": \"a\", \"refresh_token\": \"r\", \"expires_in\": 1799}");
        oAuthClient.authorize("code", state);
        api = new VolvoCarsApi(http.httpClient, oAuthClient, "key");
    }

    @BeforeEach
    void setUp() throws IOException {
        http.respond(CONNECTED_VEHICLE_ENDPOINT, 200, resource("vehicles.json"));
        http.respond(VEHICLE_URL, 200, resource("vehicle.json"));
        http.respond(VEHICLE_URL + "/doors", 200, resource("doors.json"));
        http.respond(VEHICLE_URL + "/fuel", 200, resource("fuel.json"));
        http.respond(VEHICLE_URL + "/commands", 200, resource("commands.json"));
        http.respond(ENERGY_ENDPOINT + "/" + VIN + "/state", 200, resource("energy-state.json"));
        http.respond(ENERGY_ENDPOINT + "/" + VIN + "/capabilities", 200, resource("energy-capabilities.json"));
        http.respond(LOCATION_ENDPOINT + "/" + VIN + "/location", 200, resource("location.json"));
    }

    @Test
    void vehicles() throws VolvoCarsException {
        assertEquals(List.of(VIN, "YV1TESTVIN0000002"), api.getVehicles());
    }

    @Test
    void vehicleDetails() throws VolvoCarsException {
        Vehicle vehicle = api.getVehicle(VIN);

        assertEquals("XC40", vehicle.getModel());
        assertEquals(2024, vehicle.modelYear);
        assertEquals(78.0, vehicle.batteryCapacityKWh);
        assertTrue(vehicle.hasBattery());
        assertFalse(vehicle.hasCombustionEngine());
    }

    @Test
    void connectedVehicleValues() throws VolvoCarsException {
        Map<String, ValueField> doors = api.getValues(VIN, "doors");

        assertEquals("LOCKED", field(doors, "centralLock").getString());
        assertEquals("AJAR", field(doors, "tailgate").getString());
        assertTrue(field(doors, "hood").isValid());
    }

    @Test
    void numbersEncodedAsStrings() throws VolvoCarsException {
        Map<String, ValueField> fuel = api.getValues(VIN, "fuel");

        assertEquals(new BigDecimal("47.3"), field(fuel, "fuelAmount").getNumber());
        assertEquals("l", field(fuel, "fuelAmount").unit);
    }

    @Test
    void energyStateMarksUnsupportedValuesInvalid() throws VolvoCarsException {
        Map<String, ValueField> energy = api.getEnergyState(VIN);

        assertTrue(field(energy, "batteryChargeLevel").isValid());
        assertEquals(new BigDecimal("53"), field(energy, "batteryChargeLevel").getNumber());
        assertFalse(field(energy, "chargingPower").isValid());
    }

    @Test
    void energyCapabilities() throws VolvoCarsException {
        Set<String> capabilities = api.getSupportedEnergyCapabilities(VIN);

        assertEquals(Set.of("batteryChargeLevel", "electricRange", "chargingStatus"), capabilities);
    }

    @Test
    void unsupportedEnergyApi() throws VolvoCarsException {
        http.respond(ENERGY_ENDPOINT + "/" + VIN + "/capabilities", 200, "{\"isSupported\": false}");

        assertTrue(api.getSupportedEnergyCapabilities(VIN).isEmpty());
    }

    @Test
    void location() throws VolvoCarsException {
        Location location = Objects.requireNonNull(api.getLocation(VIN));

        assertEquals(List.of(11.85, 57.72, 0.0), Objects.requireNonNull(location.geometry).coordinates);
        assertEquals("90", Objects.requireNonNull(location.properties).heading);
    }

    private static ValueField field(Map<String, ValueField> values, String key) {
        return Objects.requireNonNull(values.get(key), key);
    }

    @Test
    void availableCommands() throws VolvoCarsException {
        assertEquals(Set.of("LOCK", "UNLOCK", "CLIMATIZATION_START", "CLIMATIZATION_STOP"),
                api.getAvailableCommands(VIN));
    }

    @Test
    void commandResult() throws VolvoCarsException {
        http.respond(VEHICLE_URL + "/commands/lock", 200,
                "{\"data\": {\"vin\": \"" + VIN + "\", \"invokeStatus\": \"COMPLETED\", \"message\": \"\"}}");

        CommandResult result = api.executeCommand(VIN, "lock", null);

        assertEquals("COMPLETED", result.invokeStatus);
    }

    @Test
    void responseWithoutDataWrapper() throws VolvoCarsException {
        http.respond(VEHICLE_URL + "/odometer", 200,
                "{\"odometer\": {\"value\": 12345, \"unit\": \"km\", \"timestamp\": \"2026-10-24T20:32:09Z\"}}");

        assertEquals(new BigDecimal("12345"), field(api.getValues(VIN, "odometer"), "odometer").getNumber());
    }

    @Test
    void errorResponseContainsDetailsAndStatus() {
        http.respond(VEHICLE_URL + "/warnings", 403, """
                {"status": 403, "error": {"message": "Forbidden", "description": "Scope not granted"}}
                """);

        VolvoCarsException e = assertThrows(VolvoCarsException.class, () -> api.getValues(VIN, "warnings"));

        assertEquals(403, e.getHttpStatus());
        assertFalse(e.isAuthorizationRequired());
        assertTrue(Objects.requireNonNull(e.getMessage()).contains("Scope not granted"));
    }

    @Test
    void energyErrorResponseContainsCode() {
        http.respond(ENERGY_ENDPOINT + "/" + VIN + "/state", 404, """
                {"error": {"code": "VEHICLE_NOT_FOUND", "message": "Vehicle not found", "details": []}}
                """);

        VolvoCarsException e = assertThrows(VolvoCarsException.class, () -> api.getEnergyState(VIN));

        assertEquals(404, e.getHttpStatus());
        assertTrue(Objects.requireNonNull(e.getMessage()).endsWith(": VEHICLE_NOT_FOUND Vehicle not found"));
    }

    @Test
    void unknownResourceReturns404() {
        VolvoCarsException e = assertThrows(VolvoCarsException.class, () -> api.getValues(VIN, "unknown"));

        assertEquals(404, e.getHttpStatus());
    }

    private String resource(String name) throws IOException {
        try (InputStream stream = Objects.requireNonNull(getClass().getResourceAsStream(name))) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
