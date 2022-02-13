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
package org.openhab.transform.scale.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Locale;

import javax.measure.quantity.Dimensionless;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.transform.TransformationException;

/**
 * @author Gaël L'hopital - Initial contribution
 */
public class ScaleTransformServiceTest {
    private ScaleTransformationService processor;

    @BeforeEach
    public void init() {
        processor = new ScaleTransformationService() {
            @Override
            protected Locale getLocale() {
                return Locale.US;
            }
        };
    }

    @Test
    public void testTransformByScale() throws TransformationException {
        // need to be sure we'll have the german version
        String existingscale = "scale/humidex_de.scale";
        String source = "10";
        String transformedResponse = processor.transform(existingscale, source);
        assertEquals("nicht wesentlich", transformedResponse);

        existingscale = "scale/limits.scale";
        source = "10";
        transformedResponse = processor.transform(existingscale, source);
        assertEquals("middle", transformedResponse);
    }

    @Test
    public void testTransformByScaleLimits() throws TransformationException {
        String existingscale = "scale/limits.scale";

        // Testing upper bound opened range
        String source = "500";
        String transformedResponse = processor.transform(existingscale, source);
        assertEquals("extreme", transformedResponse);

        // Testing lower bound opened range
        source = "-10";
        transformedResponse = processor.transform(existingscale, source);
        assertEquals("low", transformedResponse);

        // Testing unfinite up and down range
        existingscale = "scale/catchall.scale";
        source = "-10";
        transformedResponse = processor.transform(existingscale, source);
        assertEquals("catchall", transformedResponse);
    }

    @Test
    public void testTransformByScaleUndef() throws TransformationException {
        // check that for undefined/non numeric value we return empty string
        // Issue #1107
        String existingscale = "scale/humidex_fr.scale";
        String source = "-";
        String transformedResponse = processor.transform(existingscale, source);
        assertEquals("", transformedResponse);
    }

    @Test
    public void testTransformByScaleErrorInBounds() throws TransformationException {
        // the tested file contains inputs that generate a conversion error of the bounds
        // of range
        String existingscale = "scale/erroneous.scale";
        String source = "15";
        try {
            @SuppressWarnings("unused")
            String transformedResponse = processor.transform(existingscale, source);
            fail();
        } catch (TransformationException e) {
            // awaited result
        }
    }

    @Test
    public void testTransformByScaleErrorInValue() throws TransformationException {
        // checks that an error is raised when trying to scale an erroneous value
        String existingscale = "scale/evaluationorder.scale";
        String source = "azerty";
        String transformedResponse = processor.transform(existingscale, source);
        assertEquals("", transformedResponse);
    }

    @Test
    public void testEvaluationOrder() throws TransformationException {
        // Ensures that only first matching scale as presented in the file is taken in account
        String evaluationOrder = "scale/evaluationorder.scale";
        // This value matches two lines of the scale file
        String source = "12";

        String transformedResponse = processor.transform(evaluationOrder, source);
        assertEquals("first", transformedResponse);
    }

    @Test
    public void testTransformQuantityType() throws TransformationException {
        QuantityType<Dimensionless> airQuality = new QuantityType<>("992 ppm");
        String aqScaleFile = "scale/netatmo_aq.scale";
        String expected = "Correcte (992 ppm) !";

        String transformedResponse = processor.transform(aqScaleFile, airQuality.toString());
        assertEquals(expected, transformedResponse);
    }

    @Test
    public void testCatchNonNumericValue() throws TransformationException {
        // checks that an error is raised when trying to scale an erroneous value
        String existingscale = "scale/catchnonnumeric.scale";
        String source = "azerty";
        String transformedResponse = processor.transform(existingscale, source);
        assertEquals("Non Numeric", transformedResponse);
    }

    @Test
    public void testTransformAndFormat() throws TransformationException {
        String existingscale = "scale/netatmo_aq.scale";
        String source = "992";
        String transformedResponse = processor.transform(existingscale, source);
        assertEquals("Correcte (992) !", transformedResponse);
    }

    @Test
    public void testValueExceedsRange() throws TransformationException {
        String existingscale = "scale/humidex.scale";
        String source = "200";
        String transformedResponse = processor.transform(existingscale, source);
        assertEquals("", transformedResponse);
    }
}
