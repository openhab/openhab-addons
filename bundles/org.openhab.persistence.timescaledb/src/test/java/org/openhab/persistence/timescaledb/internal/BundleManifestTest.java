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
package org.openhab.persistence.timescaledb.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.jar.Manifest;

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Verifies structural requirements of the OSGi bundle to ensure it deploys and integrates
 * correctly into the openHAB runtime and UI.
 *
 * <ul>
 * <li>Checks that all {@code Import-Package} entries are available in the openHAB OSGi
 * runtime (prevents "Unresolved requirement" errors on startup).</li>
 * <li>Checks that {@code OH-INF/addon/addon.xml} exists (required for the addon to appear
 * in the openHAB UI and settings).</li>
 * </ul>
 *
 * @author Contributors to the openHAB project - Initial contribution
 */
@NonNullByDefault({ DefaultLocation.RETURN_TYPE, DefaultLocation.PARAMETER })
class BundleManifestTest {

    /**
     * Package prefixes known to be available in the openHAB OSGi runtime.
     * Each entry must end with '.' so that e.g. "org.slf4j." matches both
     * the exact package "org.slf4j" and any sub-package "org.slf4j.Logger".
     */
    private static final Set<String> KNOWN_OSGI_PACKAGES = Set.of("java.", // Java SE
            "javax.", // Java SE extensions
            "org.osgi.", // OSGi framework
            "org.slf4j.", // SLF4J (embedded in openHAB core)
            "org.openhab.", // openHAB core
            "org.w3c.dom.", // Java XML DOM
            "org.xml.sax.", // Java XML SAX
            "org.ietf.jgss.", // Java GSSAPI
            "io.micrometer.", // Micrometer metrics (embedded in openHAB core)
            "com.google.gson." // Gson (embedded in openHAB core)
    );

    @Test
    void bundleManifestImportsOnlyPackagesAvailableInOpenHABRuntime() throws IOException {
        Path manifestPath = Path.of("target/classes/META-INF/MANIFEST.MF");
        assumeTrue(Files.exists(manifestPath), "MANIFEST.MF not yet generated — run 'mvn compile' first");

        String importPackageHeader;
        try (InputStream in = Files.newInputStream(manifestPath)) {
            Manifest manifest = new Manifest(in);
            importPackageHeader = manifest.getMainAttributes().getValue("Import-Package");
        }
        assertNotNull(importPackageHeader, "Import-Package header must be present in MANIFEST.MF");

        // Append '.' so that e.g. "org.slf4j" matches prefix "org.slf4j."
        // and "org.slf4j.Logger" also matches "org.slf4j.".
        List<String> unknownPackages = parsePackageNames(importPackageHeader).stream()
                .filter(pkg -> KNOWN_OSGI_PACKAGES.stream().noneMatch(known -> (pkg + ".").startsWith(known))).toList();

        assertTrue(unknownPackages.isEmpty(), () -> """
                Bundle imports packages not available in the openHAB OSGi runtime: %s

                These are likely optional features of embedded libraries (e.g. GSSAPI auth, \
                bytecode weaving).
                Fix: add '!the.package.*' to <bnd.importpackage> in pom.xml for each package.
                Example: add ',!%s.*' to the existing bnd.importpackage property.
                """.formatted(unknownPackages, unknownPackages.get(0)));
    }

    @Test
    void servicePidFollowsOpenHABConvention() {
        assertTrue(TimescaleDBPersistenceService.CONFIGURATION_PID.matches("org\\.openhab\\.[a-z]+"),
                "CONFIGURATION_PID must be 'org.openhab.<name>' (no extra segments) so that "
                        + "timescaledb.cfg is resolved correctly by the openHAB ConfigDispatcher. Got: "
                        + TimescaleDBPersistenceService.CONFIGURATION_PID);
    }

    @Test
    void addonXmlExists() {
        assertTrue(Files.exists(Path.of("src/main/resources/OH-INF/addon/addon.xml")),
                "OH-INF/addon/addon.xml is missing — the addon will not appear in the openHAB UI. "
                        + "Create src/main/resources/OH-INF/addon/addon.xml.");
    }

    /**
     * Parses the OSGi {@code Import-Package} header value into a list of package names,
     * stripping directives and attributes (e.g. {@code version="[1.0,2)"}).
     *
     * <p>
     * The header is comma-separated, but commas also appear inside quoted version ranges
     * like {@code version="[1.16,2)"}. This parser handles quoted strings correctly.
     */
    static List<String> parsePackageNames(String importPackageHeader) {
        List<String> packages = new ArrayList<>();
        int start = 0;
        boolean inQuotes = false;
        for (int i = 0; i < importPackageHeader.length(); i++) {
            char c = importPackageHeader.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                addPackageName(packages, importPackageHeader.substring(start, i));
                start = i + 1;
            }
        }
        addPackageName(packages, importPackageHeader.substring(start));
        return packages;
    }

    private static void addPackageName(List<String> result, String clause) {
        String trimmed = clause.trim();
        if (trimmed.isEmpty()) {
            return;
        }
        // A clause is "package.name;attr=val;directive:=val" — take only the package name part
        int semicolon = trimmed.indexOf(';');
        result.add(semicolon >= 0 ? trimmed.substring(0, semicolon).trim() : trimmed);
    }
}
