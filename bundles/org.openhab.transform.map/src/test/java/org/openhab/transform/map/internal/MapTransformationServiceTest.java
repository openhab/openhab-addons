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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.osgi.framework.BundleContext;

/**
 * @author Gaël L'hopital - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
public class MapTransformationServiceTest {

    private static final String SOURCE_CLOSED = "CLOSED";
    private static final String SOURCE_UNKNOWN = "UNKNOWN";
    private static final String EXISTING_FILENAME_DE = "map/doorstatus_de.map";
    private static final String SHOULD_BE_LOCALIZED_FILENAME = "map/doorstatus.map";
    private static final String DEFAULTED_FILENAME = "map/doorstatus_defaulted.map";
    private static final String INEXISTING_FILENAME = "map/de.map";
    private static final String BASE_FOLDER = "target";
    private static final String SRC_FOLDER = "conf";
    private static final String CONFIG_FOLDER = BASE_FOLDER + File.separator + SRC_FOLDER;
    private static final String USED_FILENAME = CONFIG_FOLDER + File.separator + "transform/" + EXISTING_FILENAME_DE;

    private @Mock BundleContext bundleContext;

    private TestableMapTransformationService processor;

    private class TestableMapTransformationService extends MapTransformationService {
        @Override
        protected String getSourcePath() {
            return BASE_FOLDER + File.separator + super.getSourcePath();
        }

        @Override
        protected Locale getLocale() {
            return Locale.US;
        }

        @Override
        public void activate(BundleContext context) {
            super.activate(context);
        }

        @Override
        public void deactivate() {
            super.deactivate();
        }
    };

    @BeforeEach
    public void setUp() throws IOException {
        processor = new TestableMapTransformationService();
        processor.activate(bundleContext);
        copyDirectory(SRC_FOLDER, CONFIG_FOLDER);
    }

    @AfterEach
    public void tearDown() throws IOException {
        processor.deactivate();

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
    public void testTransformByMap() throws Exception {
        // Test that we find a translation in an existing file
        String transformedResponse = processor.transform(EXISTING_FILENAME_DE, SOURCE_CLOSED);
        assertEquals("zu", transformedResponse);

        Properties properties = new Properties();
        try (FileReader reader = new FileReader(USED_FILENAME); FileWriter writer = new FileWriter(USED_FILENAME)) {
            properties.load(reader);
            properties.setProperty(SOURCE_CLOSED, "changevalue");
            properties.store(writer, "");

            // This tests that the requested transformation file has been removed from
            // the cache
            waitForAssert(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    final String transformedResponse = processor.transform(EXISTING_FILENAME_DE, SOURCE_CLOSED);
                    assertEquals("changevalue", transformedResponse);
                    return null;
                }
            }, 10000, 100);

            properties.setProperty(SOURCE_CLOSED, "zu");
            properties.store(writer, "");

            waitForAssert(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    final String transformedResponse = processor.transform(EXISTING_FILENAME_DE, SOURCE_CLOSED);
                    assertEquals("zu", transformedResponse);
                    return null;
                }
            }, 10000, 100);
        } catch (IOException e1) {
            PrintStream err = System.err;
            if (err != null) {
                e1.printStackTrace(err);
            }
        }

        // Checks that an unknown input in an existing file give the expected
        // transformed response that shall be empty string (Issue #1107) if not found in the file
        transformedResponse = processor.transform(EXISTING_FILENAME_DE, SOURCE_UNKNOWN);
        assertEquals("", transformedResponse);

        // Test that an inexisting file raises correct exception as expected
        try {
            transformedResponse = processor.transform(INEXISTING_FILENAME, SOURCE_CLOSED);
            fail();
        } catch (Exception e) {
            // That's what we expect.
        }

        // Test that we find a localized version of desired file
        transformedResponse = processor.transform(SHOULD_BE_LOCALIZED_FILENAME, SOURCE_CLOSED);
        // as we don't know the real locale at the moment the
        // test is run, we test that the string has actually been transformed
        assertNotEquals(SOURCE_CLOSED, transformedResponse);
        transformedResponse = processor.transform(SHOULD_BE_LOCALIZED_FILENAME, SOURCE_CLOSED);
        assertNotEquals(SOURCE_CLOSED, transformedResponse);
    }

    @Test
    public void testTransformByMapWithDefault() throws Exception {
        // Standard behaviour with no default value
        String transformedResponse = processor.transform(SHOULD_BE_LOCALIZED_FILENAME, "toBeDefaulted");
        assertEquals("", transformedResponse);
        // Modified behaviour with a file containing default value definition
        transformedResponse = processor.transform(DEFAULTED_FILENAME, "toBeDefaulted");
        assertEquals("Default Value", transformedResponse);
    }

    protected void waitForAssert(Callable<Void> assertion, int timeout, int sleepTime) throws Exception {
        int waitingTime = 0;
        while (waitingTime < timeout) {
            try {
                assertion.call();
                return;
            } catch (AssertionError error) {
                waitingTime += sleepTime;
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        assertion.call();
    }
}
