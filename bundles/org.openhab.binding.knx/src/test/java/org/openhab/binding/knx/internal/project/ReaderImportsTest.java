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
package org.openhab.binding.knx.internal.project;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The reader is a library: its main code imports only the JDK parts it needs, the annotations, the logger and
 * its own package, nothing of openHAB or of the rest of the binding.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class ReaderImportsTest {

    private static final Path MAIN = Path.of("src/main/java/org/openhab/binding/knx/internal/project");
    private static final int MIN_FILES = 10;
    private static final List<String> ALLOWED = List.of("java.", "javax.xml.stream.", "javax.crypto.",
            "org.eclipse.jdt.annotation.", "org.slf4j.", "org.openhab.binding.knx.internal.project.");

    /** The imports of the {@code .java} files under the folder that are not allowed, as {@code file: import}. */
    private static List<String> forbidden(Path folder, int[] files) throws IOException {
        List<String> found = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(folder)) {
            for (Path path : (Iterable<Path>) paths.filter(p -> p.toString().endsWith(".java"))::iterator) {
                files[0]++;
                for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                    String text = line.trim();
                    if (!text.startsWith("import ")) {
                        continue;
                    }
                    String name = text.substring("import ".length()).replaceFirst("^static\\s+", "")
                            .replaceFirst("\\s*;.*$", "").trim();
                    if (ALLOWED.stream().noneMatch(name::startsWith)) {
                        found.add(path.getFileName() + ": " + name);
                    }
                }
            }
        }
        return found;
    }

    @Test
    void theMainCodeOfTheReaderImportsNothingOfOpenhabOrTheRestOfTheBinding() throws Exception {
        int[] files = new int[1];

        List<String> found = forbidden(MAIN, files);

        assertTrue(files[0] >= MIN_FILES, "the reader's main folder holds only " + files[0] + " files");
        assertEquals(List.of(), found);
    }

    @Test
    void anImportOutsideTheAllowedPackagesIsFound(@TempDir Path folder) throws Exception {
        Files.writeString(folder.resolve("Fine.java"),
                "package x;\nimport java.util.List;\nimport static java.util.Objects.requireNonNull;\n"
                        + "import javax.xml.stream.XMLStreamReader;\nimport javax.crypto.Cipher;\n"
                        + "import org.eclipse.jdt.annotation.Nullable;\nimport org.slf4j.Logger;\n"
                        + "import org.openhab.binding.knx.internal.project.KnxProject;\nclass Fine {}\n");
        Files.writeString(folder.resolve("Bad.java"),
                "package x;\nimport java.util.List;\nimport org.openhab.binding.knx.internal.KNXBindingConstants;\n"
                        + "import javax.xml.parsers.DocumentBuilder;\nimport static org.openhab.core.OpenHAB.getConfigFolder;\n"
                        + "import com.google.gson.Gson;\nclass Bad {}\n");
        int[] files = new int[1];

        List<String> found = forbidden(folder, files);

        assertEquals(2, files[0]);
        assertEquals(List.of("Bad.java: org.openhab.binding.knx.internal.KNXBindingConstants",
                "Bad.java: javax.xml.parsers.DocumentBuilder", "Bad.java: org.openhab.core.OpenHAB.getConfigFolder",
                "Bad.java: com.google.gson.Gson"), found);
    }
}
