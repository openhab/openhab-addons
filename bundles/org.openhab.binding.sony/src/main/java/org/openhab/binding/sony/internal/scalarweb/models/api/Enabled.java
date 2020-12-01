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

/**
 * This class represents an enable request and is used for serialization only
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class Enabled {
    /** Whether enabling is enabled */
    private final boolean enabled;

    /**
     * Creates the enabling from the passed parameter
     *
     * @param enabled true if enabled, false otherwise
     */
    public Enabled(final boolean enabled) {
        super();
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return "Enabled [enabled=" + enabled + "]";
    }
}
