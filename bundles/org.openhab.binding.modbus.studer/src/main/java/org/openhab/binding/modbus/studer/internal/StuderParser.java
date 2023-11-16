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
package org.openhab.binding.modbus.studer.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.OnOffType;

/**
 * The {@link StuderParser} class with helper method
 * and possible values for mode and state
 *
 * @author Giovanni Mirulla - Initial contribution
 */
@NonNullByDefault
public class StuderParser {
    public enum ModeXtender {
        INVALID(0),
        INVERTER(1),
        CHARGER(2),
        BOOST(3),
        INJECTION(4),
        UNKNOWN(-1);

        private final int code;

        ModeXtender(int code) {
            this.code = code;
        }

        public int code() {
            return this.code;
        }
    }

    public static ModeXtender getModeXtenderByCode(int code) {
        switch (code) {
            case 0:
                return ModeXtender.INVALID;
            case 1:
                return ModeXtender.INVERTER;
            case 2:
                return ModeXtender.CHARGER;
            case 3:
                return ModeXtender.BOOST;
            case 4:
                return ModeXtender.INJECTION;
            default:
                return ModeXtender.UNKNOWN;
        }
    }

    public static OnOffType getStateByCode(int code) {
        switch (code) {
            case 0:
                return OnOffType.OFF;
            case 1:
                return OnOffType.ON;
            default:
                return OnOffType.OFF;
        }
    }

    public enum VTType {
        VT80(0),
        VT65(1),
        UNKNOWN(-1);

        private final int code;

        VTType(int code) {
            this.code = code;
        }

        public int code() {
            return this.code;
        }
    }

    public static VTType getVTTypeByCode(int code) {
        switch (code) {
            case 0:
                return VTType.VT80;
            case 1:
                return VTType.VT65;
            default:
                return VTType.UNKNOWN;
        }
    }

    public enum VTMode {
        NIGHT(0),
        STARTUP(1),
        CHARGER(3),
        SECURITY(5),
        OFF(6),
        CHARGE(8),
        CHARGEV(9),
        CHARGEI(10),
        CHARGET(11),
        CHIBSP(12),
        UNKNOWN(-1);

        private final int code;

        VTMode(int code) {
            this.code = code;
        }

        public int code() {
            return this.code;
        }
    }

    public static VTMode getVTModeByCode(int code) {
        switch (code) {
            case 0:
                return VTMode.NIGHT;
            case 1:
                return VTMode.STARTUP;
            case 3:
                return VTMode.CHARGER;
            case 5:
                return VTMode.SECURITY;
            case 6:
                return VTMode.OFF;
            case 8:
                return VTMode.CHARGE;
            case 9:
                return VTMode.CHARGEV;
            case 10:
                return VTMode.CHARGEI;
            case 11:
                return VTMode.CHARGET;
            case 12:
                return VTMode.CHIBSP;
            default:
                return VTMode.UNKNOWN;
        }
    }

    public enum VSMode {
        NIGHT(0),
        SECURITY(1),
        OFF(2),
        CHARGE(3),
        CHARGEV(4),
        CHARGEI(5),
        CHARGEP(6),
        CHARGEIPV(7),
        CHARGET(8),
        CHIBSP(10),
        UNKNOWN(-1);

        private final int code;

        VSMode(int code) {
            this.code = code;
        }

        public int code() {
            return this.code;
        }
    }

    public static VSMode getVSModeByCode(int code) {
        switch (code) {
            case 0:
                return VSMode.NIGHT;
            case 1:
                return VSMode.SECURITY;
            case 2:
                return VSMode.OFF;
            case 3:
                return VSMode.CHARGE;
            case 4:
                return VSMode.CHARGEV;
            case 5:
                return VSMode.CHARGEI;
            case 6:
                return VSMode.CHARGEP;
            case 7:
                return VSMode.CHARGEIPV;
            case 8:
                return VSMode.CHARGET;
            case 10:
                return VSMode.CHIBSP;
            default:
                return VSMode.UNKNOWN;
        }
    }

    /**
     * Convert an hex string to float
     *
     * @param hex string to convert from
     * @return the converted float
     */
    public @Nullable Float hexToFloat(String hex) {
        String t = hex.replace(" ", "");
        float f = Float.intBitsToFloat((int) Long.parseLong(t, 16));
        if (Float.isNaN(f)) {
            return null;
        } else {
            return f;
        }
    }
}
