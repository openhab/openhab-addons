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
package org.openhab.binding.dreamscreen.internal.model;

import static org.openhab.binding.dreamscreen.internal.DreamScreenBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.StringType;

/**
 * {@link DreamScreenScene} defines the enum for Screen Modes.
 *
 * @author Bruce Brouwer - Initial contribution
 */
@NonNullByDefault
public enum DreamScreenScene {

    COLOR(SCENE_COLOR, 0, -1),
    RANDOM_COLOR(SCENE_RANDOM, 1, 0),
    FIRESIDE(SCENE_FIRESIDE, 1, 1),
    TWINKLE(SCENE_TWINKLE, 1, 2),
    OCEAN(SCENE_OCEAN, 1, 3),
    RAINBOW(SCENE_RAINBOW, 1, 4),
    JULY_4TH(SCENE_JULY_4TH, 1, 5),
    HOLIDAY(SCENE_HOLIDAY, 1, 6),
    POP(SCENE_POP, 1, 7),
    ENCHANTED_FOREST(SCENE_ENCHANTED_FOREST, 1, 8);

    public final String name;
    public final byte ambientModeType;
    public final byte ambientScene;

    private DreamScreenScene(String name, int ambientModeType, int ambientScene) {
        this.name = name;
        this.ambientModeType = (byte) ambientModeType;
        this.ambientScene = (byte) ambientScene;
    }

    public static DreamScreenScene fromDevice(byte ambientModeType, byte ambientScene) {
        return ambientModeType == COLOR.ambientModeType ? COLOR : fromDeviceScene(ambientScene);
    }

    public static DreamScreenScene fromDeviceScene(byte ambientScene) {
        if (ambientScene == COLOR.ambientScene) {
            return COLOR;
        } else if (ambientScene == RANDOM_COLOR.ambientScene) {
            return RANDOM_COLOR;
        } else if (ambientScene == FIRESIDE.ambientScene) {
            return FIRESIDE;
        } else if (ambientScene == TWINKLE.ambientScene) {
            return TWINKLE;
        } else if (ambientScene == OCEAN.ambientScene) {
            return OCEAN;
        } else if (ambientScene == RAINBOW.ambientScene) {
            return RAINBOW;
        } else if (ambientScene == JULY_4TH.ambientScene) {
            return JULY_4TH;
        } else if (ambientScene == HOLIDAY.ambientScene) {
            return HOLIDAY;
        } else if (ambientScene == POP.ambientScene) {
            return POP;
        } else if (ambientScene == ENCHANTED_FOREST.ambientScene) {
            return ENCHANTED_FOREST;
        }
        throw new IllegalArgumentException("Invalid scene state");
    }

    public static DreamScreenScene fromState(StringType state) {
        switch (state.toString().toLowerCase()) {
            case SCENE_COLOR:
                return COLOR;
            case SCENE_RANDOM:
                return RANDOM_COLOR;
            case SCENE_FIRESIDE:
                return FIRESIDE;
            case SCENE_TWINKLE:
                return TWINKLE;
            case SCENE_OCEAN:
                return OCEAN;
            case SCENE_RAINBOW:
                return RAINBOW;
            case SCENE_JULY_4TH:
                return JULY_4TH;
            case SCENE_HOLIDAY:
                return HOLIDAY;
            case SCENE_POP:
                return POP;
            case SCENE_ENCHANTED_FOREST:
                return ENCHANTED_FOREST;
        }
        throw new IllegalArgumentException("Invalid scene state");
    }

    public StringType state() {
        return new StringType(name);
    }
}
