/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.transform.xslt.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.OpenHAB;
import org.openhab.core.transform.TransformationException;

/**
 * @author Thomas.Eichstaedt-Engelen - Initial contribution
 */
public class XsltTransformationServiceTest extends AbstractTransformationServiceTest {

    private XsltTransformationService processor;

    private final Path transformHttpPath = Paths.get(OpenHAB.getConfigFolder()).resolve("transform/http");

    @BeforeEach
    public void init() throws IOException {
        if (!Files.exists(transformHttpPath)) {
            Files.createDirectories(transformHttpPath);
        }

        processor = new XsltTransformationService();
    }

    @Test
    public void testTransformByXSLT() throws TransformationException, IOException {
        Files.copy(getClass().getResourceAsStream("google_weather.xsl"),
                transformHttpPath.resolve("google_weather.xsl"), StandardCopyOption.REPLACE_EXISTING);

        // method under test
        String transformedResponse = processor.transform("http/google_weather.xsl", source);

        // Asserts
        assertEquals("8", transformedResponse);
    }
}
