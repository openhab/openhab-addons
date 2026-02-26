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
 * The {@link ZappiChargingMode} enumeration is used to model the various Zappi
 * charging modes.
 *
 * @author Rene Scherer - Initial contribution
 *
 */
@NonNullByDefault
public enum ZappiChargingMode {
    BOOST(0),
    FAST(1),
    ECO(2),
    ECO_PLUS(3),
    STOP(4);

    private final int intValue;

    public static ZappiChargingMode fromInt(int intValue) {
        switch (intValue) {
            case 0:
                return BOOST;
            case 1:
                return FAST;
            case 2:
                return ECO;
            case 3:
                return ECO_PLUS;
            case 4:
                return STOP;
            default:
                return ECO_PLUS;
        }
    }

    ZappiChargingMode(final int intValue) {
        this.intValue = intValue;
    }

    public int getIntValue() {
        return intValue;
    }
}
