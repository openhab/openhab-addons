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
package org.openhab.binding.solax.internal.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solax.internal.model.local.RawDataParser;
import org.openhab.binding.solax.internal.model.local.inverters.X1BoostAirMiniDataParser;
import org.openhab.binding.solax.internal.model.local.inverters.X1HybridG4DataParser;
import org.openhab.binding.solax.internal.model.local.inverters.X3HybridG4DataParser;
import org.openhab.binding.solax.internal.model.local.inverters.X3MicOrProG2DataParser;

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
    X1_BOOST_AIR_MINI(4, new X1BoostAirMiniDataParser()),
    X3_HYBRID_FIT(5),
    X3_20K_30K(6),
    X3_MIC_PRO(7),
    X1_SMART(8),
    X1_AC(9),
    A1_HYBRID(10),
    A1_FIT(11),
    A1_GRID(12),
    J1_ESS(13),
    X3_HYBRID_G4(14, new X3HybridG4DataParser()),
    X1_HYBRID_G4(15, new X1HybridG4DataParser()),
    X3_MIC_OR_PRO_G2(16, new X3MicOrProG2DataParser()),
    X1_SPT(17),
    X1_BOOST_OR_MINI_G4(18),
    A1_HYB_G2(19),
    A1_AC_G2(20),
    A1_SMT_G2(21),
    X3_FTH(22),
    X3_MGA_G2(23),
    UNKNOWN(-1);

    private int typeIndex;

    private @Nullable RawDataParser parser;

    private Set<String> supportedChannels = new HashSet<>();

    InverterType(int typeIndex) {
        this(typeIndex, null);
    }

    InverterType(int typeIndex, @Nullable RawDataParser parser) {
        this.typeIndex = typeIndex;
        this.parser = parser;
        if (parser != null) {
            this.supportedChannels = parser.getSupportedChannels();
        }
    }

    public static InverterType fromIndex(int index) {
        InverterType[] values = InverterType.values();
        return Objects.requireNonNull(
                Stream.of(values).filter(value -> value.typeIndex == index).findFirst().orElse(UNKNOWN));
    }

    public @Nullable RawDataParser getParser() {
        return parser;
    }

    public Set<String> getSupportedChannels() {
        return supportedChannels;
    }
}
