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

import static org.junit.Assert.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Test;
import org.openhab.binding.bmwconnecteddrive.internal.util.FileReader;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link DestinationTest} Test json responses from ConnectedDrive Portal
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class DestinationTest {
    private final Logger logger = LoggerFactory.getLogger(DestinationTest.class);
    private static final Gson GSON = new Gson();

    @Test
    public void testDestinations() {
        String resource1 = FileReader.readFileInString("src/test/resources/webapi/destinations.json");
        DestinationContainer container = GSON.fromJson(resource1, DestinationContainer.class);
        List<Destination> destinations = container.destinations;
        assertEquals("Number of Vehicles", 9, destinations.size());
        destinations.forEach(entry -> {
            logger.info(entry.getAddress());
            assertFalse("No Null contained", entry.getAddress().contains(Constants.NULL));
        });
    }
}
