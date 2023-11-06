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
package org.openhab.binding.mybmw.internal.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mybmw.internal.utils.Converter;

/**
 * The {@link LocaleTest} is testing locale settings
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
public class LocaleTest {

    @Test
    public void testDistance() {
        double lat = 45.678;
        double lon = 8.765;
        double distance = 0.005;
        double dist = Converter.measureDistance(lat, lon, lat + distance, lon + distance);
        assertTrue(dist < 1, "Distance below 1 km");
    }
}
