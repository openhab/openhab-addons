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
        String resource1 = FileReader.readFileInString("src/test/resources/connected-drive-account-info.json");
        ConnectedDriveUserInfo userInfo = GSON.fromJson(resource1, ConnectedDriveUserInfo.class);
        List<Vehicle> vehicles = userInfo.getVehicles();
        assertEquals("Number of Vehicles", 1, vehicles.size());
        Vehicle v = vehicles.get(0);
        // assertEquals("VIN", "WBY1Z81040V905639", v.getVin());
        // assertEquals("Model", "i3 94 (+ REX)", v.getModel());
        // assertEquals("DriveTrain", "BEV_REX", v.getDriveTrain());
        // assertEquals("Brand", "BMW_I", v.getBrand());
        // assertEquals("Year of Construction", 2017, v.getYearOfConstruction());
        // System.out.println(v.getDealer());
        // System.out.println(v.getBodytype());
        // System.out.println(v.getSupportedServices());
        // System.out.println(v.getNotSupportedServices());
        // System.out.println(v.getActivatedServices());
        // System.out.println(v.getNotActivatedServices());
    }

    @Test
    public void testChannelUID() {
        ThingTypeUID THING_TYPE_PHEV = new ThingTypeUID("bmwconnecteddrive", "plugin-hybrid-car");
        assertEquals("Car Type", "plugin-hybrid-car", THING_TYPE_PHEV.getId());
    }
}
