/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

/**
 * Holds the calculated sun phase informations.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class SunPhase {
    private SunPhaseName name;

    /**
     * Returns the sun phase.
     */
    public SunPhaseName getName() {
        return name;
    }

    /**
     * Sets the sun phase.
     */
    public void setName(SunPhaseName name) {
        this.name = name;
    }
}
