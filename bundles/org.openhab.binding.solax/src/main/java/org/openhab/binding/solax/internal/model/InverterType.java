/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.solax.internal.model;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link InverterType} class is enum representing the different inverter types with a simple logic to convert from
 * int(coming from the JSON) to a more meaningful enum value.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public enum InverterType {

    X1_LX(1),
    X_HYBRID(2),
    X1_HYBRID_FIT(3),
    X1_BOOST_AIR_MINI(4),
    X3_HYBRID_FIT(5),
    X3_20K_30K(6),
    X3_MIC_PRO(7),
    X1_SMART(8),
    X1_AC(9),
    A1_HYBRID(10),
    A1_FIT(11),
    A1_GRID(12),
    J1_ESS(13),
    X3_HYBRID_G4(14),
    X1_HYBRID_G4(15),
    UNKNOWN(-1);

    private int typeIndex;

    InverterType(int typeIndex) {
        this.typeIndex = typeIndex;
    }

    public static InverterType fromIndex(int index) {
        InverterType[] values = InverterType.values();
        return Stream.of(values).filter(value -> value.typeIndex == index).findFirst().orElse(UNKNOWN);
    }
}
