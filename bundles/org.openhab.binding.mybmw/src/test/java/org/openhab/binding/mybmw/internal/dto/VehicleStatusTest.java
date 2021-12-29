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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mybmw.internal.dto.vehicle.Vehicle;
import org.openhab.binding.mybmw.internal.util.FileReader;
import org.openhab.binding.mybmw.internal.utils.Constants;
import org.openhab.binding.mybmw.internal.utils.Converter;
import org.openhab.binding.mybmw.internal.utils.VehicleStatusUtils;
import org.openhab.core.library.types.DateTimeType;

/**
 * The {@link VehicleStatusTest} Test json responses from ConnectedDrive Portal
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
public class VehicleStatusTest {

    @Test
    public void testServiceDate() {
        String json = FileReader.readFileInString("src/test/resources/vehicle-status-services.json");
        Vehicle v = Converter.getVehicle(Constants.ANONYMOUS, json);
        assertEquals(Constants.ANONYMOUS, v.vin, "VIN check");
        assertEquals("2023-11-01T00:00",
                ((DateTimeType) VehicleStatusUtils.getNextServiceDate(v.properties.serviceRequired)).getZonedDateTime()
                        .toLocalDateTime().toString(),
                "Service Date");

        assertEquals("2021-12-21T16:46:02", Converter.getZonedDateTime(v.properties.lastUpdatedAt), "Last update time");
    }
}
