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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents the wake on lan (WOL) value and is used for serialization/deserialization only
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class WolMode {
    /** Whether enabled or not */
    private @Nullable Boolean enabled;

    /**
     * Constructor used for deserialization only
     */
    public WolMode() {
    }

    /**
     * Constructs the WOL mode
     * 
     * @param enabled true for enabled, false otherwise
     */
    public WolMode(final boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Checks if WOL is enabled
     *
     * @return true if enabled, false otherwise
     */
    public @Nullable Boolean isEnabled() {
        return enabled;
    }

    @Override
    public String toString() {
        return "WolMode [enabled=" + enabled + "]";
    }
}
