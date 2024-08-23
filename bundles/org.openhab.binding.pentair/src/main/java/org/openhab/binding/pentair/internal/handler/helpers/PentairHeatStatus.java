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
package org.openhab.binding.pentair.internal.handler.helpers;

import java.util.Arrays;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.pentair.internal.parser.PentairStandardPacket;

/**
 * The {@link PentairHeatStatus } class contain heat set point info. Includes public variables.
 *
 * @author Jeff James - initial contribution
 *
 */
@NonNullByDefault
public class PentairHeatStatus {
    public enum HeatMode {
        EMPTY(-1, ""),
        NONE(0, "None"),
        HEATER(1, "Heater"),
        SOLARPREFERRED(2, "Solar Preferred"),
        SOLAR(3, "Solar");

        private final int code;
        private final String friendlyName;

        private HeatMode(int code, String friendlyName) {
            this.code = code;
            this.friendlyName = friendlyName;
        }

        public int getCode() {
            return code;
        }

        public String getFriendlyName() {
            return friendlyName;
        }

        public static HeatMode valueOfCode(int code) {
            return Objects.requireNonNull(
                    Arrays.stream(values()).filter(value -> (value.getCode() == code)).findFirst().orElse(EMPTY));
        }
    }

    @SuppressWarnings("unused")
    private static final int POOLTEMP = 1 + PentairStandardPacket.STARTOFDATA;
    @SuppressWarnings("unused")
    private static final int AIRTEMP = 2 + PentairStandardPacket.STARTOFDATA;
    private static final int POOLSETPOINT = 3 + PentairStandardPacket.STARTOFDATA;
    private static final int SPASETPOINT = 4 + PentairStandardPacket.STARTOFDATA;
    private static final int HEATMODE = 5 + PentairStandardPacket.STARTOFDATA;
    @SuppressWarnings("unused")
    private static final int SOLARTEMP = 8 + PentairStandardPacket.STARTOFDATA;

    public int poolSetPoint;
    public HeatMode poolHeatMode = HeatMode.EMPTY;
    public int spaSetPoint;
    public HeatMode spaHeatMode = HeatMode.EMPTY;

    public PentairHeatStatus() {
    }

    public PentairHeatStatus(PentairStandardPacket p) {
        parsePacket(p);
    }

    public void parsePacket(PentairStandardPacket p) {
        poolSetPoint = p.getByte(POOLSETPOINT);
        poolHeatMode = HeatMode.valueOfCode(p.getByte(HEATMODE) & 0x03);

        spaSetPoint = p.getByte(SPASETPOINT);
        spaHeatMode = HeatMode.valueOfCode((p.getByte(HEATMODE) >> 2) & 0x03);
    }

    @Override
    public String toString() {
        String str = String.format("poolSetPoint: %d, poolHeatMode: %s, spaSetPoint: %d, spaHeatMode: %s", poolSetPoint,
                poolHeatMode.name(), spaSetPoint, spaHeatMode.name());

        return str;
    }
}
