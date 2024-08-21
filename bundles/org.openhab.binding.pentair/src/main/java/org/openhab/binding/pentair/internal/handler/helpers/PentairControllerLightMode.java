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
 * The {@link PentairControllerLightMode } enum constants used to define the different light modes of the controller.
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public enum PentairControllerLightMode {
    EMPTY(-1, ""),
    OFF(0, "Off"),
    ON(1, "On"),
    COLORSYNC(128, "Color Sync"),
    COLORSWIM(144, "Color Swim"),
    COLORSET(160, "COLORSET"),
    PARTY(177, "PARTY"),
    ROMANCE(178, "ROMANCE"),
    CARIBBENA(179, "CARIBBEAN"),
    AMERICAN(180, "AMERICAN"),
    SUNSET(181, "SUNSET"),
    ROYAL(182, "ROYAL"),
    BLUE(193, "BLUE"),
    GREEN(194, "GREEN"),
    RED(195, "RED"),
    WHITE(96, "WHITE"),
    MAGENTA(197, "MAGENTA");

    private final int number;
    private final String name;

    private PentairControllerLightMode(int n, String name) {
        this.number = n;
        this.name = name;
    }

    public int getModeNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public static PentairControllerLightMode valueOfModeNumber(int modeNumber) {
        return Objects.requireNonNull(Arrays.stream(values()).filter(value -> (value.getModeNumber() == modeNumber))
                .findFirst().orElse(EMPTY));
    }
}
