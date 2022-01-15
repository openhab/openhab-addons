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
package org.openhab.binding.hue.internal;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents the version of the API of the form 1.0 or 1.2.1
 *
 * @author Samuel Leisering - Initial contribution
 */
public class ApiVersion {
    private final int major;
    private final int minor;
    private final int micro;

    private static final Pattern VERSION_PATTERN = Pattern.compile("^([0-9]+)\\.([0-9]+)(\\.([0-9]+))?$");

    public ApiVersion(int major, int minor, int micro) {
        this.major = major;
        this.minor = minor;
        this.micro = micro;
    }

    public static ApiVersion of(String version) {
        Matcher matcher = VERSION_PATTERN.matcher(version);
        if (matcher.matches()) {
            int major = Integer.parseInt(matcher.group(1));
            int minor = Integer.parseInt(matcher.group(2));
            String microString = matcher.group(4);
            int micro = Integer.parseInt(microString == null ? "0" : microString);

            ApiVersion apiVersion = new ApiVersion(major, minor, micro);
            return apiVersion;
        }

        throw new IllegalArgumentException("Version \"" + version + "\" is not valid");
    }

    /**
     * returns the major version part of the version
     *
     * @return the major part of the version
     */
    public int getMajor() {
        return major;
    }

    /**
     * returns the minor version part of the version
     *
     * @return the minor part of the version
     */
    public int getMinor() {
        return minor;
    }

    /**
     * returns the micro version part of the version
     *
     * @return the micro part of the version
     */
    public int getMicro() {
        return micro;
    }

    /**
     * compare API versions according to {@link Comparator#compare(Object, Object)}
     *
     * @param other
     * @return
     */
    public int compare(ApiVersion other) {
        int c = Integer.compare(major, other.major);
        if (c == 0) {
            c = Integer.compare(minor, other.minor);
            if (c == 0) {
                c = Integer.compare(micro, other.micro);
            }
        }
        return c;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + major;
        result = prime * result + micro;
        result = prime * result + minor;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ApiVersion other = (ApiVersion) obj;
        if (major != other.major) {
            return false;
        }
        if (micro != other.micro) {
            return false;
        }
        return minor == other.minor;
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + micro;
    }
}
