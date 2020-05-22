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
package org.openhab.transform.geohash.internal;

import static org.junit.Assert.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.transform.TransformationException;
import org.junit.Test;

/**
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class GeohashTransformationServiceTest {

    private GeohashTransformationService processor = new GeohashTransformationService();

    @Test
    public void testTransformToGeohash() throws TransformationException {
        PointType pointParis = PointType.valueOf("48.8566140,2.3522219,177");
        // method under test
        String transformedResponse = processor.transform("", pointParis.toFullString());

        assertEquals("u09tvw", transformedResponse);
    }

    @Test
    public void testTransformFromGeohash() throws TransformationException {
        String hash = "u09tvw";
        // method under test
        String transformedResponse = processor.transform("", hash);

        assertEquals("48.85894775390625,2.3565673828125", transformedResponse);
    }

    @Test(expected = TransformationException.class)
    public void testInvalidGeohash() throws TransformationException {
        // check that transformation of an invalid geohash returns null
        String hash = "invalidHash";
        @SuppressWarnings("unused")
        String transformedResponse = processor.transform("", hash);
    }

}
