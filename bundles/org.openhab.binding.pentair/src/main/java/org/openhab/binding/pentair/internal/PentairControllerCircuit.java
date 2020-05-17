/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.pentair.internal;

import static org.openhab.binding.pentair.internal.PentairBindingConstants.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Class to manage Controller Circuits/Features
 *
 * @author Jeff James - initial contribution
 *
 */
@NonNullByDefault
public class PentairControllerCircuit {
    //@formatter:off
    public static final Map<Integer, String> CIRCUITNAME = MapUtils.mapOf(
            0, "NOT USED",
            1, "AERATOR",
            2, "AIR BLOWER",
            3, "AUX 1",
            4, "AUX 2",
            5, "AUX 3",
            6, "AUX 4",
            7, "AUX 5",
            8, "AUX 6",
            9, "AUX 7",
            10, "AUX 8",
            11, "AUX 9",
            12, "AUX 10",
            13, "BACKWASH",
            14, "BACK LIGHT",
            15, "BBQ LIGHT",
            16, "BEACH LIGHT",
            17, "BOOSTER PUMP",
            18, "BUG LIGHT",
            19, "CABANA LTS",
            20, "CHEM. FEEDER",
            21, "CHLORINATOR",
            22, "CLEANER",
            23, "COLOR WHEEL",
            24, "DECK LIGHT",
            25, "DRAIN LINE",
            26, "DRIVE LIGHT",
            27, "EDGE PUMP",
            28, "ENTRY LIGHT",
            29, "FAN",
            30, "FIBER OPTIC",
            31, "FIBER WORKS",
            32, "FILL LINE",
            33, "FLOOR CLNR",
            34, "FOGGER",
            35, "FOUNTAIN",
            36, "FOUNTAIN 1",
            37, "FOUNTAIN 2",
            38, "FOUNTAIN 3",
            39, "FOUNTAINS",
            40, "FRONT LIGHT",
            41, "GARDEN LTS",
            42, "GAZEBO LTS",
            43, "HIGH SPEED",
            44, "HI-TEMP",
            45, "HOUSE LIGHT",
            46, "JETS",
            47, "LIGHTS",
            48, "LOW SPEED",
            49, "LO-TEMP",
            50, "MALIBU LTS",
            51, "MIST",
            52, "MUSIC",
            53, "NOT USED",
            54, "OZONATOR",
            55, "PATH LIGHTS",
            56, "PATIO LTS",
            57, "PERIMETER L",
            58, "PG2000",
            59, "POND LIGHT",
            60, "POOL PUMP",
            61, "POOL",
            62, "POOL HIGH",
            63, "POOL LIGHT",
            64, "POOL LOW",
            65, "SAM",
            66, "POOL SAM 1",
            67, "POOL SAM 2",
            68, "POOL SAM 3",
            69, "SECURITY LT",
            70, "SLIDE",
            71, "SOLAR",
            72, "SPA",
            73, "SPA HIGH",
            74, "SPA LIGHT",
            75, "SPA LOW",
            76, "SPA SAL",
            77, "SPA SAM",
            78, "SPA WTRFLL",
            79, "SPILLWAY",
            80, "SPRINKLERS",
            81, "STREAM",
            82, "STATUE LT",
            83, "SWIM JETS",
            84, "WTR FEATURE",
            85, "WTR FEAT LT",
            86, "WATERFALL",
            87, "WATERFALL 1",
            88, "WATERFALL 2",
            89, "WATERFALL 3",
            90, "WHIRLPOOL",
            91, "WTRFL LGHT",
            92, "YARD LIGHT",
            93, "AUX EXTRA",
            94, "FEATURE 1",
            95, "FEATURE 2",
            96, "FEATURE 3",
            97, "FEATURE 4",
            98, "FEATURE 5",
            99, "FEATURE 6",
            100, "FEATURE 7",
            101, "FEATURE 8",
            200, "USERNAME-01",
            201, "USERNAME-02",
            202, "USERNAME-03",
            203, "USERNAME-04",
            204, "USERNAME-05",
            205, "USERNAME-06",
            206, "USERNAME-07",
            207, "USERNAME-08",
            208, "USERNAME-09",
            209, "USERNAME-10");

    public static final Map<Integer, String> CIRCUITFUNCTION = MapUtils.mapOf(
            0, "GENERIC",
            1, "SPA",
            2, "POOL",
            5, "MASTER CLEANER",
            7, "LIGHT",
            9, "SAM LIGHT",
            10, "SAL LIGHT",
            11, "PHOTON GEN",
            12, "COLOR WHEEL",
            13, "VALVES",
            14, "SPILLWAY",
            15, "FLOOR CLEANER",
            16, "INTELLIBRITE",
            17, "MAGICSTREAM",
            19, "NOT USED",
            64, "FREEZE PROTECTION ON");

    public static final Map<Integer, String> GROUPNAME = MapUtils.mapOf(
            1, CONTROLLER_SPACIRCUIT,
            2, CONTROLLER_AUX1CIRCUIT,
            3, CONTROLLER_AUX2CIRCUIT,
            4, CONTROLLER_AUX3CIRCUIT,
            5, CONTROLLER_AUX4CIRCUIT,
            6, CONTROLLER_POOLCIRCUIT,
            7, CONTROLLER_AUX5CIRCUIT,
            8, CONTROLLER_AUX6CIRCUIT,
            9, CONTROLLER_AUX7CIRCUIT,
            10, CONTROLLER_AUX8CIRCUIT,
            11, CONTROLLER_FEATURE1,
            12, CONTROLLER_FEATURE2,
            13, CONTROLLER_FEATURE3,
            14, CONTROLLER_FEATURE4,
            15, CONTROLLER_FEATURE5,
            16, CONTROLLER_FEATURE6,
            17, CONTROLLER_FEATURE7,
            18, CONTROLLER_FEATURE8
            );
    public static final Map<String, Integer> GROUPNAME_INV = MapUtils.invertMap(GROUPNAME);

    //@formatter:on

    public int id;
    public int name;
    public int function;

    public boolean on;
    public int minsrun;

    public PentairControllerCircuit(int i) {
        id = i;
    }

    public void setName(int n) {
        name = n;
    }

    public void setFunction(int f) {
        function = f;
    }

    public String getNameStr() {
        return CIRCUITNAME.get(name);
    }

    public String getFunctionStr() {
        return CIRCUITFUNCTION.get(function);
    }

    public String getGroup() {
        return GROUPNAME.get(id);
    }

    public int getMinsRun() {
        return minsrun;
    }

    public void setOnOROff(boolean b) {
        on = b;
    }
}
