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
package org.openhab.binding.astro.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Holds the calculated sun phase informations.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public class SunPhase {
    private @Nullable SunPhaseName name;

    /**
     * Returns the sun phase.
     */
    @Nullable
    public SunPhaseName getName() {
        return name;
    }

    /**
     * Sets the sun phase.
     */
    public void setName(@Nullable SunPhaseName name) {
        this.name = name;
    }
}
