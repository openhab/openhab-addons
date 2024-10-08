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
package org.openhab.binding.meteofrance.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public enum Risk {
    @SerializedName("1")
    VERT("#15ed13", 0),
    @SerializedName("2")
    JAUNE("#f9ff00", 1),
    @SerializedName("3")
    ORANGE("#f7a401", 2),
    @SerializedName("4")
    ROUGE("#e71919", 3),
    UNKNOWN("#000000", -1);

    public final String rgbColor;
    public final int riskLevel;

    Risk(String rgbColor, int riskLevel) {
        this.rgbColor = rgbColor;
        this.riskLevel = riskLevel;
    }
}
