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
package org.openhab.binding.echonetlite.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Michael Barker - Initial contribution
 */
@NonNullByDefault
public enum Esv {
    SetI(0x60),
    SetC(0x61),
    Get(0x62),
    INF_REQ(0x63),
    SetMI(0x64),
    SetMC(0x65),
    GetM(0x66),
    INFM_REQ(0x67),
    AddMI(0x68),
    AddMC(0x69),
    DelMI(0x6a),
    DelMC(0x6b),
    CheckM(0x6c),
    AddMSI(0x6d),
    AddMSC(0x6e),
    Set_Res(0x71),
    Get_Res(0x72),
    INF(0x73),
    INFC(0x74),
    SetM_Res(0x75),
    GetM_Res(0x76),
    INFM(0x77),
    INFMC(0x78),
    AddM_Res(0x79),
    INFC_Res(0x7a),
    DelM_Res(0x7b),
    CheckM_Res(0x7d),
    INFMC_Res(0x7d),
    AddMS_Res(0x7e),
    SetI_SNA(0x50),
    SetC_SNA(0x51),
    Get_SNA(0x52),
    INF_SNA(0x53),
    SetMI_SNA(0x54),
    SetMC_SNA(0x55),
    GetM_SNA(0x56),
    INFM_SNA(0x57),
    AddMI_SNA(0x58),
    AddMC_SNA(0x59),
    DelMI_SNA(0x5a),
    DelMC_SNA(0x5b),
    CheckM_SNA(0x5c),
    AddMSI_SNA(0x5d),
    AddMSC_SNA(0x5e),
    Unknown(0x00);

    private final byte code;

    Esv(int code) {
        this.code = (byte) (code & 0xFF);
    }

    public static Esv forCode(byte b) {
        final Esv[] values = values();
        for (Esv value : values) {
            if (value.code == b) {
                return value;
            }
        }

        throw new IllegalArgumentException("Unable to find Esv for: " + b);
    }

    public byte code() {
        return code;
    }
}
