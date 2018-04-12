/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.opensprinkler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link OpenSprinklerBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Chris Graham - Initial contribution
 */
@NonNullByDefault
public class OpenSprinklerBindingConstants {
    public static final String BINDING_ID = "opensprinkler";

    // List of all Thing ids
    public static final String OPENSPRINKLER = "http";
    public static final String OPENSPRINKLERPI = "pi";

    // List of all Thing Type UIDs
    public static final ThingTypeUID OPENSPRINKLER_THING = new ThingTypeUID(BINDING_ID, OPENSPRINKLER);
    public static final ThingTypeUID OPENSPRINKLERPI_THING = new ThingTypeUID(BINDING_ID, OPENSPRINKLERPI);

    public static final int DEFAULT_WAIT_BEFORE_INITIAL_REFRESH = 30;
    public static final int DEFAULT_REFRESH_RATE = 60;
    public static final short DISCOVERY_SUBNET_MASK = 24;
    public static final int DISCOVERY_THREAD_POOL_SIZE = 15;
    public static final int DISCOVERY_THREAD_POOL_SHUTDOWN_WAIT_TIME_SECONDS = 300;
    public static final boolean DISCOVERY_DEFAULT_AUTO_DISCOVER = false;
    public static final int DISCOVERY_DEFAULT_TIMEOUT_RATE = 500;
    public static final int DISCOVERY_DEFAULT_IP_TIMEOUT_RATE = 750;

    // List of all Channel ids
    public static final String SENSOR_RAIN = "rainsensor";

    public static final String STATION_01 = "station01";
    public static final String STATION_02 = "station02";
    public static final String STATION_03 = "station03";
    public static final String STATION_04 = "station04";
    public static final String STATION_05 = "station05";
    public static final String STATION_06 = "station06";
    public static final String STATION_07 = "station07";
    public static final String STATION_08 = "station08";
    public static final String STATION_09 = "station09";
    public static final String STATION_10 = "station10";
    public static final String STATION_11 = "station11";
    public static final String STATION_12 = "station12";
    public static final String STATION_13 = "station13";
    public static final String STATION_14 = "station14";
    public static final String STATION_15 = "station15";
    public static final String STATION_16 = "station16";
    public static final String STATION_17 = "station17";
    public static final String STATION_18 = "station18";
    public static final String STATION_19 = "station19";
    public static final String STATION_20 = "station20";
    public static final String STATION_21 = "station21";
    public static final String STATION_22 = "station22";
    public static final String STATION_23 = "station23";
    public static final String STATION_24 = "station24";
    public static final String STATION_25 = "station25";
    public static final String STATION_26 = "station26";
    public static final String STATION_27 = "station27";
    public static final String STATION_28 = "station28";
    public static final String STATION_29 = "station29";
    public static final String STATION_30 = "station30";
    public static final String STATION_31 = "station31";
    public static final String STATION_32 = "station32";
    public static final String STATION_33 = "station33";
    public static final String STATION_34 = "station34";
    public static final String STATION_35 = "station35";
    public static final String STATION_36 = "station36";
    public static final String STATION_37 = "station37";
    public static final String STATION_38 = "station38";
    public static final String STATION_39 = "station39";
    public static final String STATION_40 = "station40";
    public static final String STATION_41 = "station41";
    public static final String STATION_42 = "station42";
    public static final String STATION_43 = "station43";
    public static final String STATION_44 = "station44";
    public static final String STATION_45 = "station45";
    public static final String STATION_46 = "station46";
    public static final String STATION_47 = "station47";
    public static final String STATION_48 = "station48";

    /**
     * Enumeration of station constants for mapping station channel names to ints and back.
     *
     * @author CrackerStealth
     */
    public enum Station {
        STATION01(0, STATION_01),
        STATION02(1, STATION_02),
        STATION03(2, STATION_03),
        STATION04(3, STATION_04),
        STATION05(4, STATION_05),
        STATION06(5, STATION_06),
        STATION07(6, STATION_07),
        STATION08(7, STATION_08),
        STATION09(8, STATION_09),
        STATION10(9, STATION_10),
        STATION11(10, STATION_11),
        STATION12(11, STATION_12),
        STATION13(12, STATION_13),
        STATION14(13, STATION_14),
        STATION15(14, STATION_15),
        STATION16(15, STATION_16),
        STATION17(16, STATION_17),
        STATION18(17, STATION_18),
        STATION19(18, STATION_19),
        STATION20(19, STATION_20),
        STATION21(20, STATION_21),
        STATION22(21, STATION_22),
        STATION23(22, STATION_23),
        STATION24(23, STATION_24),
        STATION25(24, STATION_25),
        STATION26(25, STATION_26),
        STATION27(26, STATION_27),
        STATION28(27, STATION_28),
        STATION29(28, STATION_29),
        STATION30(29, STATION_30),
        STATION31(30, STATION_31),
        STATION32(31, STATION_32),
        STATION33(32, STATION_33),
        STATION34(33, STATION_34),
        STATION35(34, STATION_35),
        STATION36(35, STATION_36),
        STATION37(36, STATION_37),
        STATION38(37, STATION_38),
        STATION39(38, STATION_39),
        STATION40(39, STATION_40),
        STATION41(40, STATION_41),
        STATION42(41, STATION_42),
        STATION43(42, STATION_43),
        STATION44(43, STATION_44),
        STATION45(44, STATION_45),
        STATION46(45, STATION_46),
        STATION47(46, STATION_47),
        STATION48(47, STATION_48),
        SENSORRAIN(-1, SENSOR_RAIN);

        private final int number;
        private final String id;

        private Station(final int number, final String id) {
            this.number = number;
            this.id = id;
        }

        @Override
        public String toString() {
            return String.valueOf(number);
        }

        public int toNumber() {
            return number;
        }

        public static Station get(int valueSelectorNumber) throws IllegalArgumentException {
            for (Station c : Station.values()) {
                if (c.number == valueSelectorNumber) {
                    return c;
                }
            }

            throw new IllegalArgumentException("Not a valid value selector.");
        }

        public static Station get(String valueSelectorText) throws IllegalArgumentException {
            for (Station c : Station.values()) {
                if (c.id.equals(valueSelectorText)) {
                    return c;
                }
            }

            throw new IllegalArgumentException("Not a valid value selector.");
        }

        public String channelID() {
            return this.id;
        }
    }
}
