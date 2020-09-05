/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.bmwconnecteddrive.internal.dto;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.junit.Test;
import org.openhab.binding.bmwconnecteddrive.internal.dto.discovery.Vehicle;
import org.openhab.binding.bmwconnecteddrive.internal.dto.discovery.VehiclesContainer;
import org.openhab.binding.bmwconnecteddrive.internal.util.FileReader;

import com.google.gson.Gson;

/**
 * The {@link ConnectedDriveTest} Test json responses from ConnectedDrive Portal
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ConnectedDriveTest {
    private static final Gson GSON = new Gson();

    @Test
    public void testUserInfo() {
        String resource1 = FileReader.readFileInString("src/test/resources/webapi/connected-drive-account-info.json");
        VehiclesContainer container = GSON.fromJson(resource1, VehiclesContainer.class);
        List<Vehicle> vehicles = container.vehicles;
        assertEquals("Number of Vehicles", 1, vehicles.size());
        Vehicle v = vehicles.get(0);
        assertEquals("VIN", "MY_REAL_VIN", v.vin);
        assertEquals("Model", "i3 94 (+ REX)", v.model);
        assertEquals("DriveTrain", "BEV_REX", v.driveTrain);
        assertEquals("Brand", "BMW_I", v.brand);
        assertEquals("Year of Construction", 2017, v.yearOfConstruction);
    }

    @Test
    public void testChannelUID() {
        ThingTypeUID thingTypePHEV = new ThingTypeUID("bmwconnecteddrive", "plugin-hybrid-car");
        assertEquals("Car Type", "plugin-hybrid-car", thingTypePHEV.getId());
    }
}
