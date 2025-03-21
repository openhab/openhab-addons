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
package org.openhab.binding.lgthinq.lgservices.model.devices.ac;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ACCanonicalSnapshot}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public enum ACFanSpeed {
    F1(2.0),
    F2(3.0),
    F3(4.0),
    F4(5.0),
    F5(6.0),
    F_AUTO(8.0),
    F_UNK(-1);

    double speed;

    ACFanSpeed(double v) {
        speed = v;
    }

    public static ACFanSpeed statusOf(double value) {
        return switch ((int) value) {
            case 2 -> F1;
            case 3 -> F2;
            case 4 -> F3;
            case 5 -> F4;
            case 6 -> F5;
            case 8 -> F_AUTO;
            default -> F_UNK;
        };
    }
}
