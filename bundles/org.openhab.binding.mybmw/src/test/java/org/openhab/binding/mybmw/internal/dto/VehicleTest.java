/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.mybmw.internal.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mybmw.internal.dto.vehicle.Vehicle;
import org.openhab.binding.mybmw.internal.util.FileReader;
import org.openhab.binding.mybmw.internal.utils.Converter;

import com.google.gson.Gson;

/**
 * The {@link VehicleTest} Test json responses from ConnectedDrive Portal
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
public class VehicleTest {
    private static final Gson GSON = new Gson();

    @Test
    public void testBevRexValues() {
        String vehiclesJSON = FileReader.readFileInString("src/test/resources/responses/I01_REX/vehicles.json");
        List<Vehicle> vehicleList = Converter.getVehicleList(vehiclesJSON);
        assertEquals(1, vehicleList.size(), "Vehicles found");
        Vehicle v = vehicleList.get(0);
        assertEquals("BMW", v.brand, "Car brand");
        assertEquals(true, v.properties.areDoorsClosed, "Doors Closed");
        assertEquals(76, v.properties.electricRange.distance.value, "Electric Range");
        assertEquals(6.789, v.properties.vehicleLocation.coordinates.longitude, 0.1, "Location lon");
    }
}
