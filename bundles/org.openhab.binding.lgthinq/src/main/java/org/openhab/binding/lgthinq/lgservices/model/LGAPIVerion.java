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
package org.openhab.binding.lgthinq.lgservices.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link LGAPIVerion}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public enum LGAPIVerion {
    V1_0(1.0),
    V2_0(2.0),
    UNDEF(0.0);

    private final double version;

    LGAPIVerion(double v) {
        version = v;
    }

    public double getValue() {
        return version;
    }
}
