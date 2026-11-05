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
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Mower hardware identification reported by version-1 Robonect firmware.
 *
 * @author Nico Brüttner - Initial contribution
 */
public class VersionHardware {

    private @Nullable Integer serial;
    private @Nullable String production;

    /**
     * Returns the hardware serial number.
     *
     * @return the hardware serial number, or {@code null} if not reported
     */
    public @Nullable Integer getSerial() {
        return serial;
    }

    /**
     * Returns the hardware production information.
     *
     * @return the hardware production information, or {@code null} if not reported
     */
    public @Nullable String getProduction() {
        return production;
    }
}
