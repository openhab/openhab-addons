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
package org.openhab.binding.homeconnectdirect.internal.common.utils;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Operating system utility methods.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class OSUtils {
    private static final String NAME = getSystemProperty("os.name", "Unknown OS");
    private static final String ARCH = getSystemProperty("os.arch", "Unknown");
    private static final String LINUX = "Linux";

    private OSUtils() {
        // Utility class
    }

    public static String getOSName() {
        return NAME;
    }

    public static String getOSArch() {
        return ARCH;
    }

    public static boolean isLinux() {
        return LINUX.equalsIgnoreCase(NAME);
    }

    private static String getSystemProperty(String key, String defaultValue) {
        String value;
        try {
            value = System.getProperty(key, defaultValue);
        } catch (SecurityException e) {
            value = defaultValue;
        }
        if (value == null) {
            value = defaultValue;
        }

        return value;
    }
}
