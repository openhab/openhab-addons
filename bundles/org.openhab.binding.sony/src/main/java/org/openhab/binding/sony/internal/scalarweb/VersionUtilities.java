/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.scalarweb;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This utility class provides utility functions for method versions (which are all strings). Versions are typically
 * "x.y" (where X is the major version, and Y is the minor version)
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class VersionUtilities {
    /**
     * Parses a version into a double.
     *
     * @param version a non-null, non-empty version
     * @return a double representing the version or 1.0 if the version cannot be parsed
     */
    public static double parse(final String version) {
        Validate.notEmpty(version, "version cannot be empty");
        try {
            return Double.parseDouble(version);
        } catch (final NumberFormatException e) {
            return 1.0;
        }
    }

    /**
     * Determines if the specified version is equal to any version in the passed list
     * 
     * @param version a possibly null, possiby empty version (null/empty will always result in a return of false)
     * @param otherVersion a list of other versions to compare against
     * @return true if the version equals any version in the list or false otherwise
     */
    public static boolean equals(final @Nullable String version, final String... otherVersion) {
        if (version == null || StringUtils.isEmpty(version)) {
            return false;
        }
        for (final String v : otherVersion) {
            if (StringUtils.equalsIgnoreCase(version, v)) {
                return true;
            }
        }
        return false;
    }
}
