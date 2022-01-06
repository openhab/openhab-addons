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
package org.openhab.binding.tradfri.internal.model;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link TradfriVersion} class is a default implementation for comparing TRÅDFRI versions.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class TradfriVersion implements Comparable<TradfriVersion> {
    private static final String VERSION_PATTERN = "[0-9]+(\\.[0-9]+)*";
    private static final String VERSION_DELIMITER = "\\.";
    final List<Integer> parts;

    /**
     * Create a new instance.
     *
     * @param version the version string
     */
    public TradfriVersion(final String version) {
        if (!version.matches(VERSION_PATTERN)) {
            throw new IllegalArgumentException("TradfriVersion cannot be created as version has invalid format.");
        }
        parts = Arrays.stream(version.split(VERSION_DELIMITER)).map(part -> Integer.parseInt(part))
                .collect(Collectors.toList());
    }

    @Override
    public int compareTo(final TradfriVersion other) {
        int minSize = Math.min(parts.size(), other.parts.size());
        for (int i = 0; i < minSize; ++i) {
            int diff = parts.get(i) - other.parts.get(i);
            if (diff == 0) {
                continue;
            } else if (diff < 0) {
                return -1;
            } else {
                return 1;
            }
        }
        for (int i = minSize; i < parts.size(); ++i) {
            if (parts.get(i) != 0) {
                return 1;
            }
        }
        for (int i = minSize; i < other.parts.size(); ++i) {
            if (other.parts.get(i) != 0) {
                return -1;
            }
        }
        return 0;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return compareTo((TradfriVersion) obj) == 0;
    }

    @Override
    public String toString() {
        return parts.stream().map(String::valueOf).collect(Collectors.joining("."));
    }
}
