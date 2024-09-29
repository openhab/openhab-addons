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

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mybmw.internal.handler.backend.JsonStringDeserializer;
import org.openhab.binding.mybmw.internal.util.FileReader;
import org.openhab.binding.mybmw.internal.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ch.qos.logback.classic.Level;

/**
 * 
 * checks the vehicleBase response
 * 
 * @author Martin Grassl - Initial contribution
 */
public class VehicleBaseTest {

    private Logger logger = LoggerFactory.getLogger(VehicleBaseTest.class);

    @BeforeEach
    public void setupLogger() {
        Logger root = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

        if ("debug".equals(System.getenv("LOG_LEVEL"))) {
            ((ch.qos.logback.classic.Logger) root).setLevel(Level.DEBUG);
        } else if ("trace".equals(System.getenv("LOG_LEVEL"))) {
            ((ch.qos.logback.classic.Logger) root).setLevel(Level.TRACE);
        }

        logger.trace("tracing enabled");
        logger.debug("debugging enabled");
        logger.info("info enabled");
    }

    @Test
    public void testVehicleBaseDeserializationByGson() {
        String vehicleBaseJson = FileReader.fileToString("responses/MILD_HYBRID/vehicles_base.json");
        Gson gson = new Gson();

        VehicleBase[] vehicleBaseArray = gson.fromJson(vehicleBaseJson, VehicleBase[].class);

        assertNotNull(vehicleBaseArray);
    }

    @Test
    public void testVehicleBaseDeserializationByConverter() {
        String vehicleBaseJson = FileReader.fileToString("responses/MILD_HYBRID/vehicles_base.json");

        List<VehicleBase> vehicleBaseList = JsonStringDeserializer.getVehicleBaseList(vehicleBaseJson);

        assertNotNull(vehicleBaseList);

        assertEquals(1, vehicleBaseList.size(), "Number of Vehicles");
        VehicleBase vehicle = vehicleBaseList.get(0);
        assertEquals(Constants.ANONYMOUS + "MILD_HYBRID", vehicle.getVin(), "VIN");
        assertEquals("M340i xDrive", vehicle.getAttributes().getModel(), "Model");
        assertEquals(Constants.DRIVETRAIN_MILD_HYBRID, vehicle.getAttributes().getDriveTrain(), "DriveTrain");
        assertEquals("bmw", vehicle.getAttributes().getBrand(), "Brand");
        assertEquals(2022, vehicle.getAttributes().getYear(), "Year of Construction");
    }
}
