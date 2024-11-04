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

import java.util.EnumSet;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public enum Hazard {
    @SerializedName("1")
    VENT("vent"),
    @SerializedName("2")
    PLUIE("pluie-inondation"),
    @SerializedName("3")
    ORAGES("orage"),
    @SerializedName("4")
    CRUES("inondation"),
    @SerializedName("5")
    NEIGE_VERGLAS("neige"),
    @SerializedName("6")
    CANICULE("canicule"),
    @SerializedName("7")
    GRAND_FROID("grand-froid"),
    @SerializedName("8")
    AVALANCHES("avalanches"),
    @SerializedName("9")
    VAGUES_SUBMERSION("vague-submersion"),
    ALL(""),
    UNKNOWN("");

    public static final EnumSet<Hazard> AS_SET = EnumSet.allOf(Hazard.class);

    public final String channelName;

    Hazard(String channelName) {
        this.channelName = channelName;
    }

    public boolean isChannel() {
        return !channelName.isEmpty();
    }
}
