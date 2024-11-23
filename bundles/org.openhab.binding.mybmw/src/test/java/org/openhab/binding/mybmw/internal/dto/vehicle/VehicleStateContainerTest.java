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
package org.openhab.binding.mybmw.internal.dto.vehicle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.openhab.binding.mybmw.internal.handler.backend.JsonStringDeserializer;
import org.openhab.binding.mybmw.internal.util.FileReader;
import org.openhab.binding.mybmw.internal.utils.VehicleStatusUtils;
import org.openhab.core.library.types.DateTimeType;

import com.google.gson.Gson;

/**
 * 
 * checks basic data of state of vehicle
 * 
 * @author Bernd Weymann - Initial contribution
 * @author Martin Grassl - refactoring
 */
public class VehicleStateContainerTest {
    @Test
    public void testVehicleStateDeserializationByGson() {
        String vehicleStateJson = FileReader.fileToString("responses/MILD_HYBRID/vehicles_state.json");
        Gson gson = new Gson();

        VehicleStateContainer vehicle = gson.fromJson(vehicleStateJson, VehicleStateContainer.class);

        assertNotNull(vehicle);
    }

    @Test
    public void testVehicleStateDeserializationByConverter() {
        String vehicleStateJson = FileReader.fileToString("responses/MILD_HYBRID/vehicles_state.json");

        VehicleStateContainer vehicleStateContainer = JsonStringDeserializer.getVehicleState(vehicleStateJson);

        assertNotNull(vehicleStateContainer);
        assertEquals("2024-06-01T00:00:00Z",
                ((DateTimeType) VehicleStatusUtils
                        .getNextServiceDate(vehicleStateContainer.getState().getRequiredServices())).getInstant()
                        .toString(),
                "Service Date");

        assertEquals("2022-12-21T15:41:23Z", vehicleStateContainer.getState().getLastUpdatedAt(), "Last update time");
    }
}
