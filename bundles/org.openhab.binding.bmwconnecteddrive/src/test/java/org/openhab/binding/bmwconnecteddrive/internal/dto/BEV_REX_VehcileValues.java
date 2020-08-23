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
import org.openhab.binding.bmwconnecteddrive.internal.util.FileReader;

import com.google.gson.Gson;

/**
 * The {@link BEV_REX_VehcileValues} Test json responses from ConnectedDrive Portal
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class BEV_REX_VehcileValues {
    private static final Gson GSON = new Gson();

    @Test
    public void testtestBEV_REX_Values() {
        String resource1 = FileReader.readFileInString("src/test/resources/vehicle.json");
        BevRexAttributesMap attributesMap = GSON.fromJson(resource1, BevRexAttributesMap.class);
        BevRexAttributes attributes = attributesMap.attributesMap;
        System.out.println("Mileage " + attributes.mileage);
        assertEquals("Mileage", 17236.0, attributes.mileage, 0.1);
        System.out.println(attributes.beRemainingRangeFuel);
    }
}
