/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.tibber.internal.pulse;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.binding.tibber.internal.TibberPulseBindingConstants;
import org.openhab.binding.tibber.internal.pulse.handler.BridgeHandler;

/**
 * Tests the scenarios "Pulse things without a cloud thing" and "Cloud thing without Pulse things" of the
 * {@code thing-lifecycle} spec at the level of the class files: the classes of the Pulse part refer to no class of the
 * cloud part (handler, calculator, history, websocket, and so on). The only classes of the binding's {@code internal}
 * package that they may name are the constants of the Pulse part and the constants class that owns the binding id.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
@Timeout(30)
class PulseIndependenceTest {

    private static final String INTERNAL = "org/openhab/binding/tibber/internal/";
    private static final Pattern INTERNAL_REFERENCE = Pattern.compile(Pattern.quote(INTERNAL) + "[A-Za-z0-9_$/]*");
    private static final Set<String> ALLOWED_CLASSES = Set.of(INTERNAL + "pulse",
            INTERNAL + "TibberPulseBindingConstants", INTERNAL + "TibberBindingConstants");

    private static boolean isAllowed(String reference) {
        return ALLOWED_CLASSES.contains(reference) || reference.startsWith(INTERNAL + "pulse/");
    }

    private static List<String> violations(String classFileText) {
        List<String> found = new ArrayList<>();
        Matcher matcher = INTERNAL_REFERENCE.matcher(classFileText);
        while (matcher.find()) {
            if (!isAllowed(matcher.group())) {
                found.add(matcher.group());
            }
        }
        return found;
    }

    private static Path pulseClasses() throws URISyntaxException {
        Path pulseClass = Path.of(BridgeHandler.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                .resolve(INTERNAL + "pulse");
        assertTrue(Files.isDirectory(pulseClass), "class files expected below " + pulseClass);
        return pulseClass;
    }

    @Test
    void whenTheClassFilesOfThePulsePartAreReadThenTheyReferToNoClassOfTheCloudPart() throws Exception {
        List<String> found = new ArrayList<>();
        int files = 0;
        int pulseReferences = 0;
        try (Stream<Path> paths = Files.walk(pulseClasses())) {
            for (Path path : paths.filter(p -> p.toString().endsWith(".class")).toList()) {
                String text = readClassFile(path);
                files++;
                pulseReferences += text.split(Pattern.quote(INTERNAL + "pulse/"), -1).length - 1;
                violations(text).forEach(v -> found.add(path.getFileName() + " -> " + v));
            }
        }

        assertTrue(files >= 20, "too few class files scanned: " + files);
        assertTrue(pulseReferences > files, "the scan does not see references between the classes of the Pulse part");
        assertEquals(List.of(), found);
    }

    @Test
    void whenTheFactoryAndTheConstantsOfThePulsePartAreReadThenTheyReferToNoClassOfTheCloudPart() throws Exception {
        Path root = Path
                .of(TibberPulseBindingConstants.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        for (String name : List.of("TibberPulseBindingConstants", "TibberPulseHandlerFactory")) {
            String text = readClassFile(root.resolve(INTERNAL + name + ".class"));

            // They may name the Pulse classes, each other, and the constants class of the binding id; the cloud
            // factory and the cloud handler are not among them.
            List<String> found = violations(text).stream()
                    .filter(v -> !v.equals(INTERNAL + "TibberPulseHandlerFactory")).toList();
            assertEquals(List.of(), found, name);
        }
    }

    @Test
    void whenAReferenceToACloudClassIsFoundThenTheCheckReportsIt() {
        String text = "x" + INTERNAL + "handler/TibberHandler;y" + INTERNAL + "pulse/sml/SmlReading;z" + INTERNAL
                + "TibberHandlerFactory;" + INTERNAL + "TibberBindingConstants;";

        assertEquals(List.of(INTERNAL + "handler/TibberHandler", INTERNAL + "TibberHandlerFactory"), violations(text));
    }

    private static String readClassFile(Path path) throws IOException {
        // The constant pool of a class file is mostly ASCII; ISO-8859-1 maps every byte to one character.
        return new String(Files.readAllBytes(path), StandardCharsets.ISO_8859_1);
    }
}
