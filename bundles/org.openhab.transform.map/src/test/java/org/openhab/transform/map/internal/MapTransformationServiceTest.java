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
import org.openhab.core.transform.TransformationConfiguration;
import org.openhab.core.transform.TransformationConfigurationRegistry;
import org.openhab.core.transform.TransformationException;

/**
 * @author Gaël L'hopital - Initial contribution
 * @author Jan N. Klug - Refactored to use {@link TransformationConfigurationRegistry}
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
    private @NonNullByDefault({}) TransformationConfigurationRegistry transformationConfigurationRegistry;

    private @NonNullByDefault({}) MapTransformationService processor;
    private final Map<String, TransformationConfiguration> configurationMap = new HashMap<>();

    @BeforeEach
    public void setUp() throws IOException {
        configurationMap.clear();
        Files.walk(Path.of(SRC_FOLDER)).filter(Files::isRegularFile).forEach(file -> {
            try {
                String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
                String uid = Path.of(SRC_FOLDER).relativize(file).toString();
                TransformationConfiguration transformationConfiguration = new TransformationConfiguration(uid, uid,
                        "map", null, content);
                configurationMap.put(uid, transformationConfiguration);
            } catch (IOException ignored) {
            }
        });

        Mockito.when(transformationConfigurationRegistry.get(anyString(), eq(null)))
                .thenAnswer((Answer<TransformationConfiguration>) invocation -> {
                    Object[] args = invocation.getArguments();
                    return configurationMap.get(args[0]);
                });

        processor = new MapTransformationService(transformationConfigurationRegistry);
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
    public void setTransformationConfigurationIsRemoved() throws TransformationException {
        assertEquals("zu", processor.transform(NON_DEFAULTED_TRANSFORMATION_DE, SOURCE_CLOSED));

        TransformationConfiguration transformationConfiguration = configurationMap
                .remove(NON_DEFAULTED_TRANSFORMATION_DE);
        processor.removed(Objects.requireNonNull(transformationConfiguration));

        assertThrows(TransformationException.class,
                () -> processor.transform(NON_DEFAULTED_TRANSFORMATION_DE, SOURCE_CLOSED));
    }

    @Test
    public void setTransformationConfigurationIsNotUpdatedIfOldElementMissing() throws TransformationException {
        // update configuration
        TransformationConfiguration transformationConfigurationDE = Objects
                .requireNonNull(configurationMap.get(NON_DEFAULTED_TRANSFORMATION_DE));
        TransformationConfiguration transformationConfigurationFR = Objects
                .requireNonNull(configurationMap.get(NON_DEFAULTED_TRANSFORMATION_FR));
        TransformationConfiguration transformationConfigurationModified = new TransformationConfiguration(
                transformationConfigurationDE.getUID(), transformationConfigurationDE.getLabel(),
                transformationConfigurationDE.getType(), transformationConfigurationDE.getLanguage(),
                transformationConfigurationFR.getContent());
        processor.updated(transformationConfigurationDE, transformationConfigurationModified);

        // assert there is no modified cached version
        assertEquals("zu", processor.transform(NON_DEFAULTED_TRANSFORMATION_DE, SOURCE_CLOSED));
    }

    @Test
    public void setTransformationConfigurationIsUpdatedIfOldElementPresent() throws TransformationException {
        // ensure old transformation is cached
        processor.transform(NON_DEFAULTED_TRANSFORMATION_DE, SOURCE_CLOSED);

        // update configuration
        TransformationConfiguration transformationConfigurationDE = Objects
                .requireNonNull(configurationMap.get(NON_DEFAULTED_TRANSFORMATION_DE));
        TransformationConfiguration transformationConfigurationFR = Objects
                .requireNonNull(configurationMap.get(NON_DEFAULTED_TRANSFORMATION_FR));
        TransformationConfiguration transformationConfigurationModified = new TransformationConfiguration(
                transformationConfigurationDE.getUID(), transformationConfigurationDE.getLabel(),
                transformationConfigurationDE.getType(), transformationConfigurationDE.getLanguage(),
                transformationConfigurationFR.getContent());
        processor.updated(transformationConfigurationDE, transformationConfigurationModified);

        // ensure modified configuration is applied
        assertEquals("fermé", processor.transform(NON_DEFAULTED_TRANSFORMATION_DE, SOURCE_CLOSED));
    }
}
