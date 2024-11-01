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
package org.openhab.binding.wiz.internal.enums;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This enum represents the possible scene modes.
 *
 * @author Sara Geleskie Damiano - Initial contribution
 *
 */
@NonNullByDefault
public enum WizLightMode {
    Ocean("Ocean", 1),
    Romance("Romance", 2),
    Sunset("Sunset", 3),
    Party("Party", 4),
    Fireplace("Fireplace", 5),
    CozyWhite("Cozy White", 6),
    Forest("Forest", 7),
    PastelColors("Pastel Colors", 8),
    Wakeup("Wakeup", 9),
    BedTime("Bed Time", 10),
    WarmWhite("Warm White", 11),
    Daylight("Daylight", 12),
    CoolWhite("Cool White", 13),
    NightLight("Night Light", 14),
    Focus("Focus", 15),
    Relax("Relax", 16),
    TrueColors("True Colors", 17),
    TVTime("TV Time", 18),
    PlantGrowth("Plant Growth", 19),
    Spring("Spring", 20),
    Summer("Summer", 21),
    Fall("Fall", 22),
    DeepDive("Deep Dive", 23),
    Jungle("Jungle", 24),
    Mojito("Mojito", 25),
    Club("Club", 26),
    Christmas("Christmas", 27),
    Halloween("Halloween", 28),
    Candlelight("Candlelight", 29),
    GoldenWhite("Golden White", 30),
    Pulse("Pulse", 31),
    Steampunk("Steampunk", 32);

    private String colorModeName;
    private int sceneId;

    private WizLightMode(final String colorModeName, final int sceneId) {
        this.colorModeName = colorModeName;
        this.sceneId = sceneId;
    }

    /**
     * Gets the colorMode name for request colorMode
     *
     * @return the colorMode name
     */
    public String getColorMode() {
        return colorModeName;
    }

    public int getSceneId() {
        return sceneId;
    }

    private static final Map<Integer, WizLightMode> LIGHT_MODE_MAP_BY_ID;
    private static final Map<String, WizLightMode> LIGHT_MODE_MAP_BY_NAME;

    static {
        LIGHT_MODE_MAP_BY_ID = new HashMap<Integer, WizLightMode>();
        LIGHT_MODE_MAP_BY_NAME = new HashMap<String, WizLightMode>();

        for (WizLightMode v : WizLightMode.values()) {
            LIGHT_MODE_MAP_BY_ID.put(v.sceneId, v);
            LIGHT_MODE_MAP_BY_NAME.put(v.colorModeName.toLowerCase().replaceAll("\\W+", ""), v);
        }
    }

    public static @Nullable WizLightMode fromSceneId(int id) {
        WizLightMode r = null;
        if (id > 0 && id < 33) {
            r = LIGHT_MODE_MAP_BY_ID.get(id);
        }
        return r;
    }

    public static @Nullable WizLightMode fromSceneName(String name) {
        WizLightMode r = null;
        if (!name.isEmpty()) {
            r = LIGHT_MODE_MAP_BY_NAME.get(name.toLowerCase().replaceAll("\\W+", ""));
        }
        return r;
    }
}
