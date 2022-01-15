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
package org.openhab.transform.javascript.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.core.transform.TransformationException;
import org.osgi.framework.BundleContext;

/**
 * @author Pauli Anttila - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
public class JavaScriptTransformationServiceTest {

    private static final String BASE_FOLDER = "target";
    private static final String SRC_FOLDER = "conf";
    private static final String CONFIG_FOLDER = BASE_FOLDER + File.separator + SRC_FOLDER;

    private @Mock BundleContext bundleContext;

    private TestableJavaScriptTransformationService processor;

    private class TestableJavaScriptTransformationService extends JavaScriptTransformationService {
        public TestableJavaScriptTransformationService(JavaScriptEngineManager manager) {
            super(manager);
        }
    };

    @BeforeEach
    public void setUp() throws IOException {
        JavaScriptEngineManager manager = new JavaScriptEngineManager();
        processor = new TestableJavaScriptTransformationService(manager);
        copyDirectory(SRC_FOLDER, CONFIG_FOLDER);
    }

    @AfterEach
    public void tearDown() throws IOException {
        try (Stream<Path> walk = Files.walk(Path.of(CONFIG_FOLDER))) {
            walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
    }

    private void copyDirectory(String from, String to) throws IOException {
        Files.walk(Paths.get(from)).forEach(fromPath -> {
            Path toPath = Paths.get(to, fromPath.toString().substring(from.length()));
            try {
                Files.copy(fromPath, toPath);
            } catch (IOException e) {
            }
        });
    }

    @Test
    public void testInlineScript() throws Exception {
        final String DATA = "100";
        final String SCRIPT = "| input / 10";

        String transformedResponse = processor.transform(SCRIPT, DATA);
        assertEquals("10.0", transformedResponse);
    }

    @Test
    public void testInlineScriptIncludingPipe() throws Exception {
        final String DATA = "1";
        final String SCRIPT = "| false || (input == '1')";

        String transformedResponse = processor.transform(SCRIPT, DATA);
        assertEquals("true", transformedResponse);
    }

    @Test
    public void testReadmeExampleWithoutSubFolder() throws Exception {
        final String DATA = "foo bar baz";
        final String SCRIPT = "readme.js";

        String transformedResponse = processor.transform(SCRIPT, DATA);
        assertEquals("3", transformedResponse);
    }

    @Test
    public void testReadmeExampleWithSubFolders() throws Exception {
        final String DATA = "foo bar baz";
        final String SCRIPT = "js/readme/readme.js";

        String transformedResponse = processor.transform(SCRIPT, DATA);
        assertEquals("3", transformedResponse);
    }

    @Test
    public void testReadmeScaleExample() throws Exception {
        final String DATA = "214";
        final String SCRIPT = "scale.js?correctionFactor=1.1&divider=10.js";

        String transformedResponse = processor.transform(SCRIPT, DATA);
        assertEquals("23.54", transformedResponse);
    }

    @Test
    public void testAdditionalVariables() throws Exception {
        final String DATA = "100";
        final String SCRIPT = "sum.js?a=10&b=1";

        String transformedResponse = processor.transform(SCRIPT, DATA);
        assertEquals("111", transformedResponse);
    }

    @Test
    public void testIllegalVariableName() throws Exception {
        final String DATA = "100";
        final String SCRIPT = "sum.js?a=10&input=fail&b=1";

        Exception exception = assertThrows(TransformationException.class, () -> processor.transform(SCRIPT, DATA));
        assertEquals("'input' word is reserved and can't be used in additional parameters", exception.getMessage());
    }

    @Test
    public void testIllegalQuestionmarkSequence() throws Exception {
        final String DATA = "100";
        final String SCRIPT = "sum.js?a=1&test=ab?d&b=2";

        Exception exception = assertThrows(TransformationException.class, () -> processor.transform(SCRIPT, DATA));
        assertEquals("Questionmark should be defined only once in the filename", exception.getMessage());
    }

    @Test
    public void testIllegalAmbersandSequence() throws Exception {
        final String DATA = "foo";
        final String SCRIPT = "returntest.js?a=1&test=ab&d&b=2";

        Exception exception = assertThrows(TransformationException.class, () -> processor.transform(SCRIPT, DATA));
        assertEquals("Illegal filename syntax", exception.getMessage());
    }

    @Test
    public void testEncodedSpecialCharacters() throws Exception {
        final String DATA = "100";
        final String SCRIPT = "returntest.js?a=1&test=ab%3Fd%26f&b=2";

        String transformedResponse = processor.transform(SCRIPT, DATA);
        assertEquals("ab?d&f", transformedResponse);
    }
}
