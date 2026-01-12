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
package org.openhab.binding.dirigera.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dirigera.internal.interfaces.ResourceProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ResourceReader} is responsible to read json file from main/resources
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ResourceReader implements ResourceProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceReader.class);
    private static final Map<String, String> TEMPLATES = new HashMap<>();

    private static ResourceProvider provider = new ResourceReader();

    public static String getResource(String resourcePath) {
        return provider.getResourceFile(resourcePath);
    }

    @Override
    public String getResourceFile(String resourcePath) {
        Thread.dumpStack();
        String template = TEMPLATES.get(resourcePath);
        if (template == null) {
            template = getBundleResource(resourcePath);
            if (!template.isBlank()) {
                TEMPLATES.put(resourcePath, template);
            } else {
                LOGGER.warn("DIRIGERA MODEL empty template for {}", resourcePath);
                template = "{}";
            }
        }
        return template;
    }

    private String getBundleResource(String fileName) {
        try {
            Bundle myself = FrameworkUtil.getBundle(ResourceReader.class);
            // do this check for unit tests to avoid NullPointerException
            if (myself != null) {
                URL url = myself.getResource(fileName);
                InputStream input = url.openStream();
                // https://www.baeldung.com/java-scanner-usedelimiter
                try (Scanner scanner = new Scanner(input).useDelimiter("\\A")) {
                    String result = scanner.hasNext() ? scanner.next() : "";
                    String resultReplaceAll = result.replaceAll("[\\n\\r\\s]", "");
                    scanner.close();
                    return resultReplaceAll;
                }
            }
        } catch (IOException e) {
            LOGGER.warn("DIRIGERA MODEL no template found for {}", fileName);
        }
        LOGGER.warn("DIRIGERA MODEL resource file  {} cannot be provided", fileName);
        return "";
    }

    public static void setProvider(ResourceProvider resourceProvider) {
        provider = resourceProvider;
    }
}
