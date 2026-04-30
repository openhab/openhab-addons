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
package org.openhab.binding.fronius.internal.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.openhab.binding.fronius.internal.api.dto.storage.StorageController;
import org.openhab.binding.fronius.internal.api.dto.storage.StorageRealtimeResponse;

import com.google.gson.Gson;

/**
 * Unit test to verify parsing of the storage realtime JSON response.
 *
 * We use String for BatteryCell status as it can be either a number
 * or a string (hexadecimal?) depending on the battery state.
 *
 * This test ensures that the JSON parsing correctly reads numeric values into the
 * String field.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
class StorageRealtimeResponseTest {

    @Test
    void parseSampleStorageJson() {
        Gson gson = new Gson();
        String json = """
                {
                   "Body" : {
                      "Data" : {
                        "Controller" : {
                             "Capacity_Maximum" : 6416.0,
                             "Current_DC" : 0.0,
                             "DesignedCapacity" : 6416.0,
                             "Details" : {
                               "Manufacturer" : "Fronius",
                               "Model" : "Reserva",
                               "Serial" : "12345678"
                             },
                             "Enable" : 1,
                             "StateOfCharge_Relative" : 95.0,
                             "Status_BatteryCell" : 3.0,
                             "Temperature_Cell" : 19.25,
                             "TimeStamp" : 1777554799,
                             "Voltage_DC" : 213.59999999999999
                         },
                         "Modules" : []
                      }
                   },
                   "Head" : {
                      "RequestArguments" : {
                         "Scope" : "System"
                      },
                      "Status" : {
                         "Code" : 0,
                         "Reason" : "",
                         "UserMessage" : ""
                      },
                      "Timestamp" : "2026-04-30T13:13:23+00:00"
                   }
                }
                """;

        StorageRealtimeResponse resp = gson.fromJson(json, StorageRealtimeResponse.class);
        assertNotNull(resp, "Response should not be null");
        assertNotNull(resp.getBody(), "Body should not be null");
        assertNotNull(resp.getBody().getData(), "Data should not be null");

        StorageController controller = resp.getBody().getData().getController();
        assertNotNull(controller, "Controller should not be null");

        assertEquals(6416.0, controller.getCapacityMaximum(), 1e-6);
        assertEquals(0.0, controller.getCurrentDC(), 1e-6);
        assertEquals(6416.0, controller.getDesignedCapacity(), 1e-6);
        assertNotNull(controller.getDetails(), "Details should not be null");
        assertEquals("Fronius", controller.getDetails().getManufacturer());
        assertEquals("Reserva", controller.getDetails().getModel());
        assertEquals("12345678", controller.getDetails().getSerial());
        assertEquals(1, controller.getEnable());
        assertEquals(95.0, controller.getStateOfChargeRelative(), 1e-6);
        assertNotNull(controller.getStatusBatteryCell(), "Status_BatteryCell should be present");
        assertEquals(19.25, controller.getTemperatureCell(), 1e-6);
        assertEquals(1777554799, controller.getTimeStamp());
        assertEquals(213.6, controller.getVoltageDC(), 1e-6);
    }
}
