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
package org.openhab.binding.sony.internal.scalarweb.models.api;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sony.internal.scalarweb.VersionUtilities;
import org.openhab.binding.sony.internal.scalarweb.gson.SupportedApiInfoDeserializer;

/**
 * This class represents a supported API and all the version information for it. This will be used in deserialization
 * and in serialization via {@link SupportedApiInfoDeserializer}
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class SupportedApiInfo {
    /** the API name */
    private final String name;

    /** The map of version information by version number */
    private final Map<String, SupportedApiVersionInfo> versions;

    /** The latest version (if found) */
    private final @Nullable SupportedApiVersionInfo latestVersion;

    /**
     * Constructs the supported API via the parameters
     * 
     * @param name a non-null, non-empty name
     * @param versions a non-null, possibly empty list of versions
     */
    public SupportedApiInfo(final String name, final List<SupportedApiVersionInfo> versions) {
        Validate.notEmpty(name, "name cannot be empty");
        Objects.requireNonNull(versions, "versions cannot be null");

        this.name = name;
        this.versions = Collections
                .unmodifiableMap(versions.stream().collect(Collectors.toMap(k -> k.version, v -> v)));

        final Optional<SupportedApiVersionInfo> latest = versions.stream().max((c1, c2) -> {
            final double d1 = VersionUtilities.parse(c1.version);
            final double d2 = VersionUtilities.parse(c2.version);
            if (d1 < d2) {
                return -1;
            }
            if (d2 > d1) {
                return 1;
            }
            return 0;
        });

        this.latestVersion = latest.isPresent() ? latest.get() : null;
    }

    /**
     * Returns the API name
     * 
     * @return a non-null, non-empty name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the versions
     * 
     * @return a non-null, possibly emtpy collection of version info
     */
    public Collection<SupportedApiVersionInfo> getVersions() {
        return versions.values();
    }

    /**
     * Get's the version info for the given version (or null if not found)
     * 
     * @param version a non-null, possibly empty version
     * @return the version info if found, null if not
     */
    public @Nullable SupportedApiVersionInfo getVersions(final String version) {
        Objects.requireNonNull(version, "version cannot be null");
        return versions.get(version);
    }

    /**
     * Get's the latest version for the API or null if there is none
     * 
     * @return the latest version info or null if none
     */
    public @Nullable SupportedApiVersionInfo getLatestVersion() {
        return latestVersion;
    }

    @Override
    public String toString() {
        return "SupportedApiInfo [name=" + name + ", versions=" + versions + "]";
    }
}
