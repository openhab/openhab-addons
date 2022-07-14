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
package org.openhab.transform.map.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
import org.openhab.core.test.java.JavaTest;
import org.openhab.core.transform.Transformation;
import org.openhab.core.transform.TransformationException;
import org.openhab.core.transform.TransformationRegistry;

/**
 * @author Gaël L'hopital - Initial contribution
 * @author Jan N. Klug - Refactored to use {@link TransformationRegistry}
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class MapTransformationServiceTest extends JavaTest {
    private static final String SOURCE_CLOSED = "CLOSED";
    private static final String SOURCE_UNKNOWN = "UNKNOWN";

    private static final String NON_DEFAULTED_TRANSFORMATION_DE = "map" + File.separator + "doorstatus_de.map";
    private static final String NON_DEFAULTED_TRANSFORMATION_FR = "map" + File.separator + "doorstatus_fr.map";
    private static final String DEFAULTED_TRANSFORMATION = "map" + File.separator + "doorstatus_defaulted.map";
    private static final String UNKNOWN_TRANSFORMATION = "map" + File.separator + "de.map";

    private static final String SRC_FOLDER = "conf" + File.separator + "transform";

    @Mock
    private @NonNullByDefault({}) TransformationRegistry transformationRegistry;

    private @NonNullByDefault({}) MapTransformationService processor;
    private final Map<String, Transformation> configurationMap = new HashMap<>();

    @BeforeEach
    public void setUp() throws IOException {
        configurationMap.clear();
        Files.walk(Path.of(SRC_FOLDER)).filter(Files::isRegularFile).forEach(file -> {
            try {
                String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
                String uid = Path.of(SRC_FOLDER).relativize(file).toString();
                Transformation transformation = new Transformation(uid, uid, "map",
                        Map.of(Transformation.FUNCTION, content));
                configurationMap.put(uid, transformation);
            } catch (IOException ignored) {
            }
        });

        Mockito.when(transformationRegistry.get(anyString(), eq(null)))
                .thenAnswer((Answer<Transformation>) invocation -> {
                    Object[] args = invocation.getArguments();
                    return configurationMap.get(args[0]);
                });

        processor = new MapTransformationService(transformationRegistry);
    }

    @Test
    public void testTransformSucceeds() throws TransformationException {
        String transformedResponse = processor.transform(NON_DEFAULTED_TRANSFORMATION_DE, SOURCE_CLOSED);
        assertEquals("zu", transformedResponse);
    }

    @Test
    public void testTransformFailsWithoutDefault() {
        assertThrows(TransformationException.class,
                () -> processor.transform(NON_DEFAULTED_TRANSFORMATION_DE, SOURCE_UNKNOWN));
    }

    @Test
    public void testTransformSucceedsWithDefault() throws TransformationException {
        assertEquals("Default Value", processor.transform(DEFAULTED_TRANSFORMATION, SOURCE_UNKNOWN));
    }

    @Test
    public void testTransformFailsOnUnknownTransformation() {
        assertThrows(TransformationException.class, () -> processor.transform(UNKNOWN_TRANSFORMATION, SOURCE_CLOSED));
    }

    @Test
    public void setTransformationIsRemoved() throws TransformationException {
        assertEquals("zu", processor.transform(NON_DEFAULTED_TRANSFORMATION_DE, SOURCE_CLOSED));

        Transformation transformation = configurationMap.remove(NON_DEFAULTED_TRANSFORMATION_DE);
        processor.removed(Objects.requireNonNull(transformation));

        assertThrows(TransformationException.class,
                () -> processor.transform(NON_DEFAULTED_TRANSFORMATION_DE, SOURCE_CLOSED));
    }

    @Test
    public void setTransformationIsNotUpdatedIfOldElementMissing() throws TransformationException {
        // update configuration
        Transformation transformationDE = Objects.requireNonNull(configurationMap.get(NON_DEFAULTED_TRANSFORMATION_DE));
        Transformation transformationFR = Objects.requireNonNull(configurationMap.get(NON_DEFAULTED_TRANSFORMATION_FR));
        Transformation transformationModified = new Transformation(transformationDE.getUID(),
                transformationDE.getLabel(), transformationDE.getType(), transformationDE.getConfiguration());
        processor.updated(transformationDE, transformationModified);

        // assert there is no modified cached version
        assertEquals("zu", processor.transform(NON_DEFAULTED_TRANSFORMATION_DE, SOURCE_CLOSED));
    }

    @Test
    public void setTransformationIsUpdatedIfOldElementPresent() throws TransformationException {
        // ensure old transformation is cached
        processor.transform(NON_DEFAULTED_TRANSFORMATION_DE, SOURCE_CLOSED);

        // update configuration
        Transformation transformationDE = Objects.requireNonNull(configurationMap.get(NON_DEFAULTED_TRANSFORMATION_DE));
        Transformation transformationFR = Objects.requireNonNull(configurationMap.get(NON_DEFAULTED_TRANSFORMATION_FR));
        Transformation transformationModified = new Transformation(transformationDE.getUID(),
                transformationDE.getLabel(), transformationDE.getType(), transformationFR.getConfiguration());
        processor.updated(transformationDE, transformationModified);

        // ensure modified configuration is applied
        assertEquals("fermé", processor.transform(NON_DEFAULTED_TRANSFORMATION_DE, SOURCE_CLOSED));
    }
}
