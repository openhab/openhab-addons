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
 * The {@link EddiMode} enumeration is used to model the various Eddi modes.
 *
 * @author Stephen Cook - Initial contribution
 *
 */
@NonNullByDefault
public enum EddiMode {
    // starts a manual boost
    START_MANUAL_BOOST(10),

    // cancels boost
    CANCEL_BOOST(1),

    // eddi to stopped
    STOP(0),

    // eddi to normal mode
    NORMAL(1);

    private final int intValue;

    EddiMode(final int intValue) {
        this.intValue = intValue;
    }

    public int getIntValue() {
        return intValue;
    }
}
