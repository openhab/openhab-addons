/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.bluelink.internal.dto.eu.ccs2;

/**
 * Start climate command request for CCU/CCS2 vehicles.
 *
 * @author Florian Hotze - Initial contribution
 */
public record Ccs2StartClimateRequest(String command, int ignitionDuration, int strgWhlHeating, int hvacTempType,
        double hvacTemp, int sideRearMirrorHeating, String drvSeatLoc, SeatClimateInfo seatClimateInfo, String tempUnit,
        int windshieldFrontDefogState) {
    public Ccs2StartClimateRequest(double temperature, boolean heat, boolean defrost, int ignitionDuration) {
        this("start", ignitionDuration, heat ? 1 : 0, 1, temperature, defrost ? 1 : 0, "R", SeatClimateInfo.OFF, "C",
                defrost ? 1 : 0);
    }

    public record SeatClimateInfo(int drvSeatClimateState, int psgSeatClimateState, int rrSeatClimateState,
            int rlSeatClimateState) {
        private static final SeatClimateInfo OFF = new SeatClimateInfo(0, 0, 0, 0);
    }
}
