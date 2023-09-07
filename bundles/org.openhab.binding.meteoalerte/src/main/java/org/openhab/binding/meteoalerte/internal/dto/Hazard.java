/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.meteoalerte.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public enum Hazard {
    @SerializedName("1")
    VENT,
    @SerializedName("2")
    PLUIE,
    @SerializedName("3")
    ORAGES,
    @SerializedName("4")
    CRUES,
    @SerializedName("5")
    NEIGE_VERGLAS,
    @SerializedName("6")
    CANICULE,
    @SerializedName("7")
    GRAND_FROID,
    @SerializedName("8")
    AVALANCHES,
    @SerializedName("9")
    VAGUES_SUBMERSION,
    ALL,
    UNKNOWN;
}
