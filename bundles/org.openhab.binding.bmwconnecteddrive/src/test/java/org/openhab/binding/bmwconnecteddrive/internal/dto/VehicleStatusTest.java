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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Test;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.Position;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.VehicleStatus;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.VehicleStatusContainer;
import org.openhab.binding.bmwconnecteddrive.internal.util.FileReader;

import com.google.gson.Gson;

/**
 * The {@link VehicleStatusTest} Test json responses from ConnectedDrive Portal
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class VehicleStatusTest {
    private static final Gson GSON = new Gson();

    @Test
    public void testtestBevRexValues() {
        String resource1 = FileReader.readFileInString("src/test/resources/webapi/vehicle-status.json");
        VehicleStatusContainer status = GSON.fromJson(resource1, VehicleStatusContainer.class);
        VehicleStatus vStatus = status.vehicleStatus;
        assertEquals("Mileage", 17273.0, vStatus.mileage, 0.1);
        Position p = vStatus.position;
        assertEquals("Heading", 219, p.heading);

        assertEquals("DCS Activation", "NA", vStatus.dcsCchActivation);
        assertEquals("DCS Ongoing", false, vStatus.dcsCchOngoing);
    }
}
