/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.airparif.internal.api;

import java.net.URI;
import java.util.EnumSet;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * {@link AirParifApi} class defines paths used to interact with server api
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class AirParifApi {
    private static final UriBuilder AIRPARIF_BUILDER = UriBuilder.fromPath("/").scheme("https").host("api.airparif.fr");
    public static final URI VERSION_URI = AIRPARIF_BUILDER.clone().path("version").build();
    public static final URI KEY_INFO_URI = AIRPARIF_BUILDER.clone().path("key-info").build();
    public static final URI HORAIR_URI = AIRPARIF_BUILDER.clone().path("horair").path("itineraire").build();
    public static final URI EPISODES_URI = AIRPARIF_BUILDER.clone().path("episodes").path("en-cours-et-prevus").build();

    private static final UriBuilder INDICES_BUILDER = AIRPARIF_BUILDER.clone().path("indices").path("prevision");
    public static final URI PREV_BULLETIN_URI = INDICES_BUILDER.clone().path("bulletin").build();

    private static final UriBuilder POLLENS_BUILDER = AIRPARIF_BUILDER.clone().path("pollens");
    public static final URI POLLENS_URI = POLLENS_BUILDER.clone().path("bulletin").build();

    public enum Scope {
        @SerializedName("Cartes et résultats Hor'Air")
        MAPS,
        @SerializedName("Pollens")
        POLLENS,
        @SerializedName("Épisodes")
        EVENTS,
        @SerializedName("Indices")
        INDEXES,
        UNKNOWN;
    }

    public enum Appreciation {
        GOOD("Bon"),
        AVERAGE("Moyen"),
        DEGRATED("Dégradé"),
        BAD("Mauvais"),
        VERY_BAD("Très Mauvais"),
        EXTREMELY_BAD("Extrêmement Mauvais"),
        UNKNOWN("");

        public final String apiName;

        Appreciation(String apiName) {
            this.apiName = apiName;
        }

        public static final EnumSet<Appreciation> AS_SET = EnumSet.allOf(Appreciation.class);
    }

    public enum Pollen {
        @SerializedName("cypres")
        CYPRESS("cypres"),
        @SerializedName("noisetier")
        HAZEL("noisetier"),
        @SerializedName("aulne")
        ALDER("aulne"),
        @SerializedName("peuplier")
        POPLAR("peuplier"),
        @SerializedName("saule")
        WILLOW("saule"),
        @SerializedName("frene")
        ASH("frene"),
        @SerializedName("charme")
        HORNBEAM("charme"),
        @SerializedName("bouleau")
        BIRCH("bouleau"),
        @SerializedName("platane")
        PLANE("platane"),
        @SerializedName("chene")
        OAK("chene"),
        @SerializedName("olivier")
        OLIVE("olivier"),
        @SerializedName("tilleul")
        LINDEN("tilleul"),
        @SerializedName("chataignier")
        CHESTNUT("chataignier"),
        @SerializedName("rumex")
        RUMEX("rumex"),
        @SerializedName("graminees")
        GRASSES("graminees"),
        @SerializedName("plantain")
        PLANTAIN("plantain"),
        @SerializedName("urticacees")
        URTICACEAE("urticacees"),
        @SerializedName("armoises")
        WORMWOOD("armoises"),
        @SerializedName("ambroisies")
        RAGWEED("ambroisies");

        public final String apiName;

        Pollen(String apiName) {
            this.apiName = apiName;
        }

        public static final EnumSet<Pollen> AS_SET = EnumSet.allOf(Pollen.class);
    }
}
