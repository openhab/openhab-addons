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
package org.openhab.binding.luxtronikheatpump.internal.enums;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents all heat pump types
 *
 * @author Stefan Giehl - Initial contribution
 */
@NonNullByDefault
public enum HeatpumpType {
    TYPE_ERC(0, "ERC"),
    TYPE_SW1(1, "SW1"),
    TYPE_SW2(2, "SW2"),
    TYPE_WW1(3, "WW1"),
    TYPE_WW2(4, "WW2"),
    TYPE_L1I(5, "L1I"),
    TYPE_L2I(6, "L2I"),
    TYPE_L1A(7, "L1A"),
    TYPE_L2A(8, "L2A"),
    TYPE_KSW(9, "KSW"),
    TYPE_KLW(10, "KLW"),
    TYPE_SWC(11, "SWC"),
    TYPE_LWC(12, "LWC"),
    TYPE_L2G(13, "L2G"),
    TYPE_WZS(14, "WZS"),
    TYPE_L1I407(15, "L1I407"),
    TYPE_L2I407(16, "L2I407"),
    TYPE_L1A407(17, "L1A407"),
    TYPE_L2A407(18, "L2A407"),
    TYPE_L2G407(19, "L2G407"),
    TYPE_LWC407(20, "LWC407"),
    TYPE_L1AREV(21, "L1AREV"),
    TYPE_L2AREV(22, "L2AREV"),
    TYPE_WWC1(23, "WWC1"),
    TYPE_WWC2(24, "WWC2"),
    TYPE_L2G404(25, "L2G404"),
    TYPE_WZW(26, "WZW"),
    TYPE_L1S(27, "L1S"),
    TYPE_L1H(28, "L1H"),
    TYPE_L2H(29, "L2H"),
    TYPE_WZWD(30, "WZWD"),
    TYPE_ERC2(31, "ERC"),
    TYPE_WWB_20(40, "WWB_20"),
    TYPE_LD5(41, "LD5"),
    TYPE_LD7(42, "LD7"),
    TYPE_SW_37_45(43, "SW 37_45"),
    TYPE_SW_58_69(44, "SW 58_69"),
    TYPE_SW_29_56(45, "SW 29_56"),
    TYPE_LD5_230V(46, "LD5 (230V)"),
    TYPE_LD7_230V(47, "LD7 (230 V)"),
    TYPE_LD9(48, "LD9"),
    TYPE_LD5_REV(49, "LD5 REV"),
    TYPE_LD7_REV(50, "LD7 REV"),
    TYPE_LD5_REV_230V(51, "LD5 REV 230V"),
    TYPE_LD7_REV_230V(52, "LD7 REV 230V"),
    TYPE_LD9_REV_230V(53, "LD9 REV 230V"),
    TYPE_SW_291(54, "SW 291"),
    TYPE_LW_SEC(55, "LW SEC"),
    TYPE_HMD_2(56, "HMD 2"),
    TYPE_MSW_4(57, "MSW 4"),
    TYPE_MSW_6(58, "MSW 6"),
    TYPE_MSW_8(59, "MSW 8"),
    TYPE_MSW_10(60, "MSW 10"),
    TYPE_MSW_12(61, "MSW 12"),
    TYPE_MSW_14(62, "MSW 14"),
    TYPE_MSW_17(63, "MSW 17"),
    TYPE_MSW_19(64, "MSW 19"),
    TYPE_MSW_23(65, "MSW 23"),
    TYPE_MSW_26(66, "MSW 26"),
    TYPE_MSW_30(67, "MSW 30"),
    TYPE_MSW_4S(68, "MSW 4S"),
    TYPE_MSW_6S(69, "MSW 6S"),
    TYPE_MSW_8S(70, "MSW 8S"),
    TYPE_MSW_10S(71, "MSW 10S"),
    TYPE_MSW_13S(72, "MSW 13S"),
    TYPE_MSW_16S(73, "MSW 16S"),
    TYPE_MSW2_6S(74, "MSW2-6S"),
    TYPE_MSW4_16(75, "MSW4-16"),
    TYPE_LD2AG(76, "LD2AG"),
    TYPE_LWD90V(77, "LWD90V"),
    TYPE_MSW3_12(78, "MSW3-12"),
    TYPE_MSW3_12S(79, "MSW3-12S"),
    TYPE_MSW2_9S(80, "MSW2-9S"),
    TYPE_LW8(81, "LW 8"),
    TYPE_LW12(82, "LW 12"),
    TYPE_UNKNOWN(-1, "Unknown");

    private final String name;
    private final Integer code;
    private static final Logger LOGGER = LoggerFactory.getLogger(HeatpumpType.class);

    private HeatpumpType(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static HeatpumpType fromCode(Integer code) {
        for (HeatpumpType error : HeatpumpType.values()) {
            if (error.code.equals(code)) {
                return error;
            }
        }

        LOGGER.warn("Unknown heatpump type code {}", code);

        return TYPE_UNKNOWN;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return code + ": " + name;
    }
}
