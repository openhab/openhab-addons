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
package org.openhab.binding.dreamscreen.internal;

import org.eclipse.smarthome.core.library.types.DecimalType;

/**
 * The {@link DreamScreenScene} defines the light scenes
 *
 * @author Bruce Brouwer - Initial contribution
 */
enum DreamScreenEnumScene {
    COLOR(0, -1),
    RANDOM_COLOR(1, 0),
    FIRESIDE(1, 1),
    TWINKLE(1, 2),
    OCEAN(1, 3),
    RAINBOW(1, 4),
    JULY_4TH(1, 5),
    HOLIDAY(1, 6),
    POP(1, 7),
    ENCHANTED_FOREST(1, 8);

    final byte deviceAmbientSceneType;
    final byte deviceAmbientScene;

    private DreamScreenEnumScene(int deviceAmbientSceneType, int deviceAmbientScene) {
        this.deviceAmbientSceneType = (byte) deviceAmbientSceneType;
        this.deviceAmbientScene = (byte) deviceAmbientScene;
    }

    static DreamScreenEnumScene fromDevice(byte deviceAmbientSceneType, byte deviceAmbientScene) {
        return DreamScreenEnumScene.values()[deviceAmbientSceneType == 0 ? 0 : deviceAmbientScene + 1];
    }

    static DreamScreenEnumScene fromState(DecimalType state) {
        return DreamScreenEnumScene.values()[state.intValue()];
    }

    public DecimalType state() {
        return new DecimalType(this.ordinal());
    }
}
