/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
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
@SuppressWarnings("null")
public class DestinationTest {
    private final Logger logger = LoggerFactory.getLogger(DestinationTest.class);
    private static final Gson GSON = new Gson();

    @Test
    public void testDestinations() {
        String resource1 = FileReader.readFileInString("src/test/resources/webapi/destinations.json");
        DestinationContainer container = GSON.fromJson(resource1, DestinationContainer.class);
        List<Destination> destinations = container.destinations;
        assertEquals(9, destinations.size(), "Number of Vehicles");
        destinations.forEach(entry -> {
            logger.info(entry.getAddress());
            assertFalse(entry.getAddress().contains(Constants.NULL), "No Null contained");
        });
    }
}
