/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.jellyfin.internal.client.model;

import java.util.Comparator;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Model to help with Jellyfin server versions. The format is similar to SemVer.
 *
 * @author Patrik Gfeller, based on Android SDK by Peter Feller - Initial contribution (AI generated code by "Claude
 *         Sonnet 3.7")
 */
public class ServerVersion implements Comparable<ServerVersion> {

    private static final Comparator<ServerVersion> COMPARATOR = Comparator.comparing(ServerVersion::getMajor)
            .thenComparing(ServerVersion::getMinor).thenComparing(ServerVersion::getPatch)
            .thenComparing(version -> version.getBuild() != null ? version.getBuild() : 0);

    private static final Pattern VERSION_PATTERN = Pattern.compile("^\\d+\\.\\d+\\.\\d+(?:\\.\\d+)?$");

    private final int major;
    private final int minor;
    private final int patch;
    private final Integer build;

    /**
     * Create a new ServerVersion
     *
     * @param major The major version number
     * @param minor The minor version number
     * @param patch The patch version number
     */
    public ServerVersion(int major, int minor, int patch) {
        this(major, minor, patch, null);
    }

    /**
     * Create a new ServerVersion
     *
     * @param major The major version number
     * @param minor The minor version number
     * @param patch The patch version number
     * @param build The optional build number
     */
    public ServerVersion(int major, int minor, int patch, Integer build) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.build = build;
    }

    /**
     * Get the major version number
     *
     * @return The major version number
     */
    public int getMajor() {
        return major;
    }

    /**
     * Get the minor version number
     *
     * @return The minor version number
     */
    public int getMinor() {
        return minor;
    }

    /**
     * Get the patch version number
     *
     * @return The patch version number
     */
    public int getPatch() {
        return patch;
    }

    /**
     * Get the build number
     *
     * @return The build number, or null if not set
     */
    public Integer getBuild() {
        return build;
    }

    @Override
    public int compareTo(ServerVersion other) {
        return COMPARATOR.compare(this, other);
    }

    /**
     * Convert version to string. Format is "[major].[minor].[patch].[build]".
     * [build] is omitted if null.
     *
     * @return The formatted version string
     */
    @Override
    public String toString() {
        if (build != null) {
            return String.format("%d.%d.%d.%d", major, minor, patch, build);
        } else {
            return String.format("%d.%d.%d", major, minor, patch);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ServerVersion that = (ServerVersion) o;
        return major == that.major && minor == that.minor && patch == that.patch && Objects.equals(build, that.build);
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch, build);
    }

    /**
     * Create an instance of ServerVersion from a string. The string must be in the format
     * "^\d+\.\d+\.\d+(?:\.\d+)?$". Example: 1.0.0, 10.6.4 or 10.7.0.0
     *
     * @param str The version string to parse
     * @return A ServerVersion instance, or null if the string couldn't be parsed
     */
    public static ServerVersion fromString(String str) {
        if (str == null || !VERSION_PATTERN.matcher(str).matches()) {
            return null;
        }

        String[] parts = str.split("\\.");

        try {
            int major = Integer.parseInt(parts[0]);
            int minor = Integer.parseInt(parts[1]);
            int patch = Integer.parseInt(parts[2]);
            Integer build = parts.length > 3 ? Integer.parseInt(parts[3]) : null;

            return new ServerVersion(major, minor, patch, build);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
