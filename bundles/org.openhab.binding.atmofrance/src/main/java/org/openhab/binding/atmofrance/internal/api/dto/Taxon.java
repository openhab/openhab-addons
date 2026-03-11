/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.atmofrance.internal.api.dto;

import java.util.EnumSet;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public enum Taxon {
    // Currently available
    @SerializedName("aulne")
    ALDER("aulne"),
    @SerializedName("bouleau")
    BIRCH("bouleau"),
    @SerializedName("olivier")
    OLIVE("olivier"),
    @SerializedName("graminees")
    GRASSES("graminees"),
    @SerializedName("armoises")
    WORMWOOD("armoises"),
    @SerializedName("ambroisies")
    RAGWEED("ambroisies"),

    // Not available as of today
    @SerializedName("cypres")
    CYPRESS("cypres"),
    @SerializedName("noisetier")
    HAZEL("noisetier"),
    @SerializedName("peuplier")
    POPLAR("peuplier"),
    @SerializedName("saule")
    WILLOW("saule"),
    @SerializedName("frene")
    ASH("frene"),
    @SerializedName("charme")
    HORNBEAM("charme"),
    @SerializedName("platane")
    PLANE("platane"),
    @SerializedName("chene")
    OAK("chene"),
    @SerializedName("tilleul")
    LINDEN("tilleul"),
    @SerializedName("chataignier")
    CHESTNUT("chataignier"),
    @SerializedName("rumex")
    RUMEX("rumex"),
    @SerializedName("plantain")
    PLANTAIN("plantain"),
    @SerializedName("urticacees")
    URTICACEAE("urticacees");

    public final String apiName;

    Taxon(String apiName) {
        this.apiName = apiName;
    }

    public static final EnumSet<Taxon> AS_SET = EnumSet.allOf(Taxon.class);
}
