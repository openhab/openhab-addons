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
package org.openhab.binding.atv.internal.client.settings;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Settings related to MRP (MediaRemote Protocol).
 *
 * @param identifier device identifier for the MRP service (may be {@code null})
 * @param credentials pairing credentials (may be {@code null})
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public record MrpSettings(@Nullable String identifier, @Nullable String credentials) {

    /**
     * Creates empty default settings.
     *
     * @return default settings
     */
    public static MrpSettings ofDefaults() {
        return new MrpSettings(null, null);
    }

    /**
     * Returns a copy with another identifier.
     *
     * @param identifier new identifier
     * @return updated copy
     */
    public MrpSettings withIdentifier(String identifier) {
        return new MrpSettings(identifier, credentials);
    }

    /**
     * Returns a copy with other credentials.
     *
     * @param credentials new credentials
     * @return updated copy
     */
    public MrpSettings withCredentials(String credentials) {
        return new MrpSettings(identifier, credentials);
    }
}
