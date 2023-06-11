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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mybmw.internal.dto.vehicle.Vehicle;
import org.openhab.binding.mybmw.internal.util.FileReader;
import org.openhab.binding.mybmw.internal.utils.Constants;
import org.openhab.binding.mybmw.internal.utils.Converter;
import org.openhab.binding.mybmw.internal.utils.RemoteServiceUtils;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.CommandOption;

/**
 * The {@link VehiclePropertiesTest} tests stored fingerprint responses from BMW API
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class VehiclePropertiesTest {

    @Test
    public void testUserInfo() {
        String content = FileReader.readFileInString("src/test/resources/responses/I01_REX/vehicles.json");
        List<Vehicle> vl = Converter.getVehicleList(content);

        assertEquals(1, vl.size(), "Number of Vehicles");
        Vehicle v = vl.get(0);
        assertEquals(Constants.ANONYMOUS, v.vin, "VIN");
        assertEquals("i3 94 (+ REX)", v.model, "Model");
        assertEquals(Constants.BEV, v.driveTrain, "DriveTrain");
        assertEquals("BMW", v.brand, "Brand");
        assertEquals(2017, v.year, "Year of Construction");
    }

    @Test
    public void testChannelUID() {
        ThingTypeUID thingTypePHEV = new ThingTypeUID("mybmw", "plugin-hybrid-vehicle");
        assertEquals("plugin-hybrid-vehicle", thingTypePHEV.getId(), "Vehicle Type");
    }

    @Test
    public void testRemoteServiceOptions() {
        String commandReference = "[CommandOption [command=light-flash, label=Flash Lights], CommandOption [command=vehicle-finder, label=Vehicle Finder], CommandOption [command=door-lock, label=Door Lock], CommandOption [command=door-unlock, label=Door Unlock], CommandOption [command=horn-blow, label=Horn Blow], CommandOption [command=climate-now-start, label=Start Climate], CommandOption [command=climate-now-stop, label=Stop Climate]]";
        List<CommandOption> l = RemoteServiceUtils.getOptions(true);
        assertEquals(commandReference, l.toString(), "Commad Options");
    }
}
