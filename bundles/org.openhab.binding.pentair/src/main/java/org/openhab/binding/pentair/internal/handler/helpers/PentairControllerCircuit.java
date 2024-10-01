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

/**
 * The {@link PentairControllerCircuit } class is used to define circuit/features of the controller
 *
 * @author Jeff James - initial contribution
 *
 */
@NonNullByDefault
public class PentairControllerCircuit {
    public enum CircuitName {
        EMPTY(-1, ""),
        NOTUSED(0, "NOT USED"),
        AERATOR(1, "AERATOR"),
        AIRBLOWER(2, "AIR BLOWER"),
        AUX1(3, "AUX 1"),
        AUX2(4, "AUX 2"),
        AUX3(5, "AUX 3"),
        AUX4(6, "AUX 4"),
        AUX5(7, "AUX 5"),
        AUX6(8, "AUX 6"),
        AUX7(9, "AUX 7"),
        AUX8(10, "AUX 8"),
        AUX9(11, "AUX 9"),
        AUX10(12, "AUX 10"),
        BACKWASH(13, "BACKWASH"),
        BACKLIGHT(14, "BACK LIGHT"),
        BBQLIGHT(15, "BBQ LIGHT"),
        BEACHLIGHT(16, "BEACH LIGHT"),
        BOOSTERPUMP(17, "BOOSTER PUMP"),
        BUGLIGHT(18, "BUG LIGHT"),
        CABANALTS(19, "CABANA LTS"),
        CHEMFEEDER(20, "CHEM. 2FEEDER"),
        CHLORINATOR(21, "CHLORINATOR"),
        CLEANER(22, "CLEANER"),
        COLORWHEEL(23, "COLOR WHEEL"),
        DECKLIGHT(24, "DECK LIGHT"),
        DRAINLINE(25, "DRAIN LINE"),
        DRIVELIGHT(26, "DRIVE LIGHT"),
        EDGEPUMP(27, "EDGE PUMP"),
        ENTRYLIGHT(28, "ENTRY LIGHT"),
        FAN(29, "FAN"),
        FIBEROPTIC(30, "FIBER OPTIC"),
        FIBERWORKS(31, "FIBER WORKS"),
        FILLLINE(32, "FILL LINE"),
        FLOORCLNR(33, "FLOOR CLNR"),
        FOGGER(34, "FOGGER"),
        FOUNTAIN(35, "FOUNTAIN"),
        FOUNTAIN1(36, "FOUNTAIN 1"),
        FOUNTAIN2(37, "FOUNTAIN 2"),
        FOUNTAIN3(38, "FOUNTAIN 3"),
        FOUNTAINS(39, "FOUNTAINS"),
        FRONTLIGHT(40, "FRONT LIGHT"),
        GARDENLTS(41, "GARDEN LTS"),
        GAZEBOLTS(42, "GAZEBO LTS"),
        HIGHSPEED(43, "HIGH SPEED"),
        HITEMP(44, "HI-TEMP"),
        HOUSELIGHT(45, "HOUSE LIGHT"),
        JETS(46, "JETS"),
        LIGHTS(47, "LIGHTS"),
        LOWSPEED(48, "LOW SPEED"),
        LOTEMP(49, "LO-TEMP"),
        MALIBULTS(50, "MALIBU LTS"),
        MIST(51, "MIST"),
        MUSIC(52, "MUSIC"),
        NOTUSED2(53, "NOT USED"),
        OZONATOR(54, "OZONATOR"),
        PATHLIGHTS(55, "PATH LIGHTS"),
        PATIOLTS(56, "PATIO LTS"),
        PERIMETERL(57, "PERIMETER L"),
        PG2000(58, "PG2000"),
        PONDLIGHT(59, "POND LIGHT"),
        POOLPUMP(60, "POOL PUMP"),
        POOL(61, "POOL"),
        POOLHIGH(62, "POOL HIGH"),
        POOLLIGHT(63, "POOL LIGHT"),
        POOLLOW(64, "POOL LOW"),
        SAM(65, "SAM"),
        POOLSAM1(66, "POOL SAM 1"),
        POOLSAM2(67, "POOL SAM 2"),
        POOLSAM3(68, "POOL SAM 3"),
        SECURITYLT(69, "SECURITY LT"),
        SLIDE(70, "SLIDE"),
        SOLAR(71, "SOLAR"),
        SPA(72, "SPA"),
        SPAHIGH(73, "SPA HIGH"),
        SPALIGHT(74, "SPA LIGHT"),
        SPALOW(75, "SPA LOW"),
        SPASAL(76, "SPA SAL"),
        SPASAM(77, "SPA SAM"),
        SPAWTRFLL(78, "SPA WTRFLL"),
        SPILLWAY(79, "SPILLWAY"),
        SPRINKLERS(80, "SPRINKLERS"),
        STREAM(81, "STREAM"),
        STAUTELT(82, "STATUE LT"),
        SWIMJETS(83, "SWIM JETS"),
        WTRFEATURE(84, "WTR FEATURE"),
        WTRFEATLT(85, "WTR FEAT LT"),
        WATERFALL(86, "WATERFALL"),
        WATERFALL1(87, "WATERFALL 1"),
        WATERFALL2(88, "WATERFALL 2"),
        WATERFALL3(89, "WATERFALL 3"),
        WHIRLPOOL(90, "WHIRLPOOL"),
        WTRFLLGHT(91, "WTRFL LGHT"),
        YARDLIGHT(92, "YARD LIGHT"),
        AUXEXTRA(93, "AUX EXTRA"),
        FEATURE1(94, "FEATURE 1"),
        FEATURE2(95, "FEATURE 2"),
        FEATURE3(96, "FEATURE 3"),
        FEATURE4(97, "FEATURE 4"),
        FEATURE5(98, "FEATURE 5"),
        FEATURE6(99, "FEATURE 6"),
        FEATURE7(100, "FEATURE 7"),
        FEATURE8(101, "FEATURE 8"),
        USERNAME01(200, "USERNAME-01"),
        USERNAME02(201, "USERNAME-02"),
        USERNAME03(202, "USERNAME-03"),
        USERNAME04(203, "USERNAME-04"),
        USERNAME05(204, "USERNAME-05"),
        USERNAME06(205, "USERNAME-06"),
        USERNAME07(206, "USERNAME-07"),
        USERNAME08(207, "USERNAME-08"),
        USERNAME09(208, "USERNAME-09"),
        USERNAME10(209, "USERNAME-10");

        private final int number;
        private final String friendlyName;

        CircuitName(int n, String friendlyName) {
            this.number = n;
            this.friendlyName = friendlyName;
        }

        public int getCode() {
            return number;
        }

        public String getFriendlyName() {
            return friendlyName;
        }

        public static CircuitName valueOfModeNumber(int number) {
            return Objects.requireNonNull(Arrays.stream(values()).filter(value -> (value.getCode() == number))
                    .findFirst().orElse(CircuitName.EMPTY));
        }
    }

    public enum CircuitFunction {
        EMPTY(-1, ""),
        GENERIC(0, "GENERIC"),
        SPA(1, "SPA"),
        POOL(2, "POOL"),
        MASTERCLEANER(5, "MASTER CLEANER"),
        LIGHT(7, "LIGHT"),
        SAMLIGHT(9, "SAM LIGHT"),
        SALLIGHT(10, "SAL LIGHT"),
        PHOTONGEN(11, "PHOTON GEN"),
        COLORWHEEL(12, "COLOR WHEEL"),
        VALVES(13, "VALVES"),
        SPILLWAY(14, "SPILLWAY"),
        FLOORCLEANER(15, "FLOOR CLEANER"),
        INTELLIBRITE(16, "INTELLIBRITE"),
        MAGICSTREAM(17, "MAGICSTREAM"),
        NOTUSED(19, "NOT USED"),
        FREEZEPROTECT(64, "FREEZE PROTECTION ON");

        private final int code;
        private final String friendlyName;

        private CircuitFunction(int code, String friendlyName) {
            this.code = code;
            this.friendlyName = friendlyName;
        }

        public int getCode() {
            return code;
        }

        public String getFriendlyName() {
            return friendlyName;
        }

        public static CircuitFunction valueOfModeNumber(int number) {
            return Objects.requireNonNull(Arrays.stream(values()).filter(value -> (value.getCode() == number))
                    .findFirst().orElse(CircuitFunction.EMPTY));
        }
    }

    public final int id;
    public CircuitName circuitName = CircuitName.EMPTY;
    public CircuitFunction circuitFunction = CircuitFunction.EMPTY;

    public PentairControllerCircuit(int id) {
        this.id = id;
    }

    public void setName(int n) {
        circuitName = CircuitName.valueOfModeNumber(n);
    }

    public void setName(CircuitName circuitName) {
        this.circuitName = circuitName;
    }

    public void setFunction(int f) {
        circuitFunction = CircuitFunction.valueOfModeNumber(f);
    }

    public void setFunction(CircuitFunction circuitFunction) {
        this.circuitFunction = circuitFunction;
    }
}
