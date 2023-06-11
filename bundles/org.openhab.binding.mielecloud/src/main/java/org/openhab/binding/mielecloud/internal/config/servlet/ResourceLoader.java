/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.mielecloud.internal.config.servlet;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.osgi.framework.BundleContext;

/**
 * Provides access to resource files for servlets.
 *
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public final class ResourceLoader {
    private static final String BEGINNING_OF_INPUT = "\\A";

    private final String basePath;
    private final BundleContext bundleContext;

    /**
     * Creates a new {@link ResourceLoader}.
     *
     * @param basePath The base path to use for loading. A trailing {@code "/"} is removed.
     * @param bundleContext {@link BundleContext} to load from.
     */
    public ResourceLoader(String basePath, BundleContext bundleContext) {
        this.basePath = removeTrailingSlashes(basePath);
        this.bundleContext = bundleContext;
    }

    private String removeTrailingSlashes(String value) {
        String ret = value;
        while (ret.endsWith("/")) {
            ret = ret.substring(0, ret.length() - 1);
        }
        return ret;
    }

    /**
     * Opens a resource relative to the base path.
     *
     * @param filename The filename of the resource to load.
     * @return A stream reading from the resource file.
     * @throws FileNotFoundException If the requested resource file cannot be found.
     * @throws IOException If an error occurs while opening a stream to the resource.
     */
    public InputStream openResource(String filename) throws IOException {
        URL url = bundleContext.getBundle().getEntry(basePath + "/" + filename);
        if (url == null) {
            throw new FileNotFoundException("Cannot find '" + filename + "' relative to '" + basePath + "'");
        }

        return url.openStream();
    }

    /**
     * Loads the contents of a resource file as UTF-8 encoded {@link String}.
     *
     * @param filename The filename of the resource to load.
     * @return The contents of the file.
     * @throws FileNotFoundException If the requested resource file cannot be found.
     * @throws IOException If an error occurs while opening a stream to the resource or reading from it.
     */
    public String loadResourceAsString(String filename) throws IOException {
        try (Scanner scanner = new Scanner(openResource(filename), StandardCharsets.UTF_8.name())) {
            return scanner.useDelimiter(BEGINNING_OF_INPUT).next();
        }
    }
}
