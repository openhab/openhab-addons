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
package org.openhab.io.yamlcomposer.internal;

import java.nio.file.Path;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.OpenHAB;

/**
 * Static configuration constants and system path resolution for the YAML Composer.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public final class ComposerConfig {

    private static final Path DEFAULT_CONFIG_ROOT = normalizeAbsolute(Path.of(OpenHAB.getConfigFolder()));
    private static final Path DEFAULT_USERDATA_ROOT = normalizeAbsolute(Path.of(OpenHAB.getUserDataFolder()));

    private static volatile Path configRootOverride = DEFAULT_CONFIG_ROOT;
    private static volatile Path userDataRootOverride = DEFAULT_USERDATA_ROOT;

    public static final Path SOURCE_ROOT_DIRECTORY = Path.of("yamlcomposer");
    public static final Path OUTPUT_ROOT_DIRECTORY = Path.of("yaml", "composed");

    // Special section keys
    public static final String TEMPLATES_KEY = "templates";
    public static final String VARIABLES_KEY = "variables";
    public static final String PACKAGES_KEY = "packages";

    // Preprocessing limits
    public static final int MAX_INCLUDE_DEPTH = 100;

    private ComposerConfig() {
        // Static utility class
    }

    public static Path configRoot() {
        return configRootOverride;
    }

    public static Path userDataRoot() {
        return userDataRootOverride;
    }

    public static Path sourceRoot() {
        return configRoot().resolve(SOURCE_ROOT_DIRECTORY);
    }

    public static Path outputRoot() {
        return configRoot().resolve(OUTPUT_ROOT_DIRECTORY);
    }

    static void setRootsForTesting(Path configRoot, Path userDataRoot) {
        configRootOverride = normalizeAbsolute(configRoot);
        userDataRootOverride = normalizeAbsolute(userDataRoot);
    }

    static void resetRootsForTesting() {
        configRootOverride = DEFAULT_CONFIG_ROOT;
        userDataRootOverride = DEFAULT_USERDATA_ROOT;
    }

    private static Path normalizeAbsolute(Path path) {
        return path.toAbsolutePath().normalize();
    }

    /**
     * Resolves the output path for a generated YAML file.
     *
     * @param sourcePath source file's absolute path
     * @return the path where the generated output should be written
     */
    public static Path resolveOutputPath(Path sourcePath) {
        Path relativeToSourceRoot = sourceRoot().relativize(sourcePath);
        return outputRoot().resolve(relativeToSourceRoot);
    }
}
