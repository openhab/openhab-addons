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
package org.openhab.binding.bmwconnecteddrive.internal.dto.statistics;

import static org.junit.Assert.assertNotNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Test;
import org.openhab.binding.bmwconnecteddrive.internal.util.FileReader;

import com.google.gson.Gson;

/**
 * The {@link LastTripTest} Test json responses from ConnectedDrive Portal
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class LastTripTest {
    private static final Gson GSON = new Gson();

    @Test
    public void testUserInfo() {
        String content = FileReader.readFileInString("src/test/resources/webapi/last-trip.json");
        LastTrip lt = GSON.fromJson(content, LastTrip.class);
        Trip trip = lt.lastTrip;
        assertNotNull(trip);
        System.out.println(trip.totalDistance);
    }
}
