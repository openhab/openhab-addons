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
package org.openhab.binding.mybmw.internal.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mybmw.internal.dto.vehicle.Vehicle;
import org.openhab.binding.mybmw.internal.util.FileReader;
import org.openhab.binding.mybmw.internal.utils.Constants;
import org.openhab.binding.mybmw.internal.utils.Converter;
import org.openhab.binding.mybmw.internal.utils.VehicleStatusUtils;
import org.openhab.core.library.types.DateTimeType;

/**
 * The {@link VehicleStatusTest} tests stored fingerprint responses from BMW API
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
public class VehicleStatusTest {

    @Test
    public void testServiceDate() {
        String json = FileReader.readFileInString("src/test/resources/responses/I01_REX/vehicles.json");
        Vehicle v = Converter.getVehicle(Constants.ANONYMOUS, json);
        assertEquals(Constants.ANONYMOUS, v.vin, "VIN check");
        assertEquals("2023-11-01T00:00",
                ((DateTimeType) VehicleStatusUtils.getNextServiceDate(v.properties.serviceRequired)).getZonedDateTime()
                        .toLocalDateTime().toString(),
                "Service Date");

        ZonedDateTime zdt = ZonedDateTime.parse("2021-12-21T16:46:02Z").withZoneSameInstant(ZoneId.systemDefault());
        LocalDateTime ldt = zdt.toLocalDateTime();
        assertEquals(ldt.format(Converter.DATE_INPUT_PATTERN),
                Converter.zonedToLocalDateTime(v.properties.lastUpdatedAt), "Last update time");
    }

    @Test
    public void testBevRexValues() {
        String vehiclesJSON = FileReader.readFileInString("src/test/resources/responses/I01_REX/vehicles.json");
        List<Vehicle> vehicleList = Converter.getVehicleList(vehiclesJSON);
        assertEquals(1, vehicleList.size(), "Vehicles found");
        Vehicle v = vehicleList.get(0);
        assertEquals("BMW", v.brand, "Car brand");
        assertEquals(true, v.properties.areDoorsClosed, "Doors Closed");
        assertEquals(76, v.properties.electricRange.distance.value, "Electric Range");
        assertEquals(9.876, v.properties.vehicleLocation.coordinates.longitude, 0.1, "Location lon");
        assertEquals("immediateCharging", v.status.chargingProfile.chargingMode, "Charging Mode");
        assertEquals(2, v.status.chargingProfile.getTimerId(2).id, "Timer ID");
        assertEquals("[sunday]", v.status.chargingProfile.getTimerId(2).timerWeekDays.toString(), "Timer Weekdays");
    }

    @Test
    public void testGuessRange() {
        /**
         * PHEV G01
         * fuelIndicator electric unit = %
         * fuelIndicator fuel unit = l
         * fuelIndicator hybrid unit = null
         */
        String vehiclesJSON = FileReader.readFileInString("src/test/resources/responses/G01/vehicles_v2_bmw_0.json");
        List<Vehicle> vehicleList = Converter.getVehicleList(vehiclesJSON);
        assertEquals(1, vehicleList.size(), "Vehicles found");
        Vehicle vehicle = vehicleList.get(0);
        assertEquals(2, VehicleStatusUtils.getRange(Constants.UNIT_PRECENT_JSON, vehicle), "Electric Range");
        assertEquals(437, VehicleStatusUtils.getRange(Constants.UNIT_LITER_JSON, vehicle), "Fuel Range");
        assertEquals(439, VehicleStatusUtils.getRange(Constants.PHEV, vehicle), "Hybrid Range");

        /**
         * Electric REX I01
         * fuelIndicator electric unit = %
         * fuelIndicator fuel unit = null
         * fuelIndicator hybrid unit = null
         */
        vehiclesJSON = FileReader.readFileInString("src/test/resources/responses/I01_REX/vehicles_v2_bmw_0.json");
        vehicleList = Converter.getVehicleList(vehiclesJSON);
        assertEquals(1, vehicleList.size(), "Vehicles found");
        vehicle = vehicleList.get(0);
        assertEquals(164, VehicleStatusUtils.getRange(Constants.UNIT_PRECENT_JSON, vehicle), "Electric Range");
        assertEquals(64, VehicleStatusUtils.getRange(Constants.UNIT_LITER_JSON, vehicle), "Fuel Range");
        assertEquals(228, VehicleStatusUtils.getRange(Constants.PHEV, vehicle), "Hybrid Range");

        /**
         * PHEV G05
         * fuelIndicator electric unit = %
         * fuelIndicator fuel unit = %
         * fuelIndicator hybrid unit = null
         */
        vehiclesJSON = FileReader.readFileInString("src/test/resources/responses/G05/vehicles_v2_bmw_0.json");
        vehicleList = Converter.getVehicleList(vehiclesJSON);
        assertEquals(1, vehicleList.size(), "Vehicles found");
        vehicle = vehicleList.get(0);
        assertEquals(48, VehicleStatusUtils.getRange(Constants.UNIT_PRECENT_JSON, vehicle), "Electric Range");
        assertEquals(418, VehicleStatusUtils.getRange(Constants.UNIT_LITER_JSON, vehicle), "Fuel Range");
        assertEquals(466, VehicleStatusUtils.getRange(Constants.PHEV, vehicle), "Hybrid Range");
    }
}
