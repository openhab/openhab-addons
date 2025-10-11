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
package org.openhab.binding.myenergi.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ZappiBoostMode} enumeration is used to model the various Zappi
 * boost charging modes.
 *
 * @author Rene Scherer - Initial contribution
 *
 */
@NonNullByDefault
public enum ZappiBoostMode {
    // stops the current boost cycle
    STOP(2),

    // starts a new boost cycle immediately up to the given KWh charge
    NORMAL(10),

    // starts a new smart boost cycle up to given KWh charge and departure time
    SMART(11);

    private final int intValue;

    ZappiBoostMode(final int intValue) {
        this.intValue = intValue;
    }

    public int getIntValue() {
        return intValue;
    }
}
