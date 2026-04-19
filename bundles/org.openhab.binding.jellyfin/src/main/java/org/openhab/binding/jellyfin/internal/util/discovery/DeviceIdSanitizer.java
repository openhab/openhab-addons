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
package org.openhab.binding.jellyfin.internal.util.discovery;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Sanitizes Jellyfin device IDs for safe use in openHAB {@code ThingUID}s.
 *
 * <p>
 * {@code ThingUID} identifiers only allow alphanumeric characters, hyphens ({@code -}),
 * and underscores ({@code _}). This class replaces every character that does not match
 * that set with a hyphen.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public final class DeviceIdSanitizer {

    private DeviceIdSanitizer() {
    }

    /**
     * Replaces every character in {@code deviceId} that is not alphanumeric, a hyphen,
     * or an underscore with a hyphen ({@code -}).
     *
     * @param deviceId the raw device ID from the Jellyfin session
     * @return the sanitized device ID, safe for use in a {@code ThingUID}
     */
    public static String sanitize(String deviceId) {
        return deviceId.replaceAll("[^a-zA-Z0-9_-]", "-");
    }
}
