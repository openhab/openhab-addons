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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javax.measure.quantity.Dimensionless;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.transform.Transformation;
import org.openhab.core.transform.TransformationException;
import org.openhab.core.transform.TransformationRegistry;

/**
 * @author Gaël L'hopital - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class ScaleTransformServiceTest {
    private static final String SRC_FOLDER = "conf" + File.separator + "transform";

    @Mock
    private @NonNullByDefault({}) TransformationRegistry transformationConfigurationRegistry;
    private final Map<String, Transformation> configurationMap = new HashMap<>();
    private @NonNullByDefault({}) ScaleTransformationService processor;

    @BeforeEach
    public void init() throws IOException {
        configurationMap.clear();
        Files.walk(Path.of(SRC_FOLDER)).filter(Files::isRegularFile).forEach(file -> {
            try {
                String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
                String uid = Path.of(SRC_FOLDER).relativize(file).toString();
                Transformation transformationConfiguration = new Transformation(uid, uid, "scale",
                        Map.of(Transformation.FUNCTION, content));
                configurationMap.put(uid, transformationConfiguration);
            } catch (IOException ignored) {
            }
        });

        Mockito.when(transformationConfigurationRegistry.get(anyString(), eq(null)))
                .thenAnswer((Answer<Transformation>) invocation -> {
                    Object[] args = invocation.getArguments();
                    return configurationMap.get(args[0]);
                });
        processor = new ScaleTransformationService(transformationConfigurationRegistry);
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
        assertThrows(TransformationException.class, () -> processor.transform(existingscale, source));
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
        assertThrows(TransformationException.class, () -> processor.transform(existingscale, source));
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
        assertThrows(TransformationException.class, () -> processor.transform(existingscale, source));
    }
}
