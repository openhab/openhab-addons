/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.groupepsa.internal.rest.api.dto;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.filosganga.geogson.gson.GeometryAdapterFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * Test parsing real JSON response from OPEL api to VehicleStatus
 * 
 * @author Christoph Pfeifer - Initial contribution
 *
 */
class JsonResponseVehicleStatusTest {

    private static final String API_RESPONSE = "{\"createdAt\":\"2023-04-16T16:22:12Z\",\"updatedAt\":\"2023-04-16T16:22:12Z\",\"engines\":[{\"type\":\"Thermic\",\"extension\":{\"thermic\":{\"coolant\":{\"temp\":215},\"air\":{\"temp\":215}}},\"createdAt\":\"2023-01-03T13:40:01Z\"}],\"ignition\":{\"createdAt\":\"2023-04-16T16:22:12Z\",\"type\":\"Stop\"},\"battery\":{\"voltage\":88,\"createdAt\":\"2023-04-16T16:22:12Z\"},\"privacy\":{\"createdAt\":\"2023-04-16T16:22:12Z\",\"state\":\"None\"},\"service\":{\"createdAt\":\"2023-01-03T11:58:01Z\",\"type\":\"Electric\"},\"environment\":{\"luminosity\":{\"createdAt\":\"2023-04-16T16:22:12Z\",\"day\":true},\"air\":{\"createdAt\":\"2023-04-16T16:22:12Z\",\"temp\":12.5}},\"odometer\":{\"createdAt\":\"2023-04-16T16:22:12Z\",\"mileage\":2613.7},\"_links\":{\"self\":{\"href\":\"https://api.groupe-psa.com/connectedcar/v4/user/vehicles/1ef345/status?profile=endUser\"},\"vehicle\":{\"href\":\"https://api.groupe-psa.com/connectedcar/v4/user/vehicles/1ef345\"}},\"preconditioning\":{\"airConditioning\":{\"createdAt\":\"2023-04-16T16:22:12Z\",\"updatedAt\":\"2023-04-16T16:22:12Z\",\"status\":\"Disabled\",\"programs\":[{\"enabled\":false,\"slot\":1,\"recurrence\":\"Daily\",\"start\":\"PT0S\"},{\"enabled\":false,\"slot\":2,\"recurrence\":\"Daily\",\"start\":\"PT0S\"},{\"enabled\":false,\"slot\":3,\"recurrence\":\"Daily\",\"start\":\"PT0S\"},{\"enabled\":false,\"slot\":4,\"recurrence\":\"Daily\",\"start\":\"PT0S\"}]}},\"energies\":[{\"createdAt\":\"2023-01-03T13:40:01Z\",\"type\":\"Fuel\",\"subType\":\"FossilEnergy\",\"extension\":{\"fuel\":{\"consumptions\":{\"total\":0}}}},{\"createdAt\":\"2023-04-16T16:22:12Z\",\"type\":\"Electric\",\"subType\":\"ElectricEnergy\",\"level\":91,\"autonomy\":338,\"extension\":{\"electric\":{\"battery\":{\"load\":{\"createdAt\":\"2023-04-16T16:22:12Z\",\"capacity\":36608,\"residual\":13024}},\"charging\":{\"plugged\":false,\"status\":\"Disconnected\",\"chargingRate\":0,\"chargingMode\":\"No\",\"nextDelayedTime\":\"PT0S\"}}}}],\"preconditionning\":{\"airConditioning\":{\"createdAt\":\"2023-04-16T16:22:12Z\",\"updatedAt\":\"2023-04-16T16:22:12Z\",\"status\":\"Disabled\",\"programs\":[{\"enabled\":false,\"slot\":1,\"recurrence\":\"Daily\",\"start\":\"PT0S\"},{\"enabled\":false,\"slot\":2,\"recurrence\":\"Daily\",\"start\":\"PT0S\"},{\"enabled\":false,\"slot\":3,\"recurrence\":\"Daily\",\"start\":\"PT0S\"},{\"enabled\":false,\"slot\":4,\"recurrence\":\"Daily\",\"start\":\"PT0S\"}]}},\"energy\":[{\"createdAt\":\"2023-01-03T13:40:01Z\",\"updatedAt\":\"2023-01-03T13:40:01Z\",\"type\":\"Fuel\"},{\"createdAt\":\"2023-04-16T16:22:12Z\",\"updatedAt\":\"2023-04-16T16:22:12Z\",\"type\":\"Electric\",\"level\":91,\"autonomy\":338,\"charging\":{\"plugged\":false,\"status\":\"Disconnected\",\"chargingRate\":0,\"chargingMode\":\"No\",\"nextDelayedTime\":\"PT0S\"}}]}";

    private Gson gson;

    @BeforeEach
    public void setUp() {
        gson = new GsonBuilder().registerTypeAdapterFactory(new GeometryAdapterFactory())
                .registerTypeAdapter(ZonedDateTime.class, new JsonDeserializer<ZonedDateTime>() {
                    @Override
                    public @Nullable ZonedDateTime deserialize(JsonElement json, Type typeOfT,
                            JsonDeserializationContext context) throws JsonParseException {
                        return ZonedDateTime.parse(json.getAsJsonPrimitive().getAsString());
                    }
                }).registerTypeAdapter(Duration.class, new JsonDeserializer<Duration>() {
                    @Override
                    public @Nullable Duration deserialize(JsonElement json, Type typeOfT,
                            JsonDeserializationContext context) throws JsonParseException {
                        return Duration.parse(json.getAsJsonPrimitive().getAsString());
                    }
                }).create();
    }

    @Test
    void testGetVehicleStatus() {
        final VehicleStatus vehicleStatus = gson.fromJson(API_RESPONSE, VehicleStatus.class);
        Assertions.assertNotNull(vehicleStatus);
        Assertions.assertNotNull(vehicleStatus.getOdometer().getMileage());
        Assertions.assertNotNull(vehicleStatus.getIgnition().getType());
        Assertions.assertNotNull(vehicleStatus.getEnvironment().getAir().getTemp());
        Assertions.assertNotNull(vehicleStatus.getEnvironment().getLuminosity().isDay());
    }
}
