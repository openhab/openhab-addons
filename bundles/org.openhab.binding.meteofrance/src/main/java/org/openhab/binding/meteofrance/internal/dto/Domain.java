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

import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public enum Domain {
    @SerializedName("FRA")
    FRANCE("France"),

    @SerializedName("ZDF_EST")
    ZDF_EST("Défense Est"),
    @SerializedName("ZDF_PARIS")
    ZDF_PARIS("Défense Paris"),
    @SerializedName("ZDF_NORD")
    ZDF_NORD("Défense Nord"),
    @SerializedName("ZDF_SUD_EST")
    ZDF_SUD_EST("Défense Sud-Est"),
    @SerializedName("ZDF_SUD_OUEST")
    ZDF_SUD_OUEST("Défense Sud-Ouest"),
    @SerializedName("ZDF_SUD")
    ZDF_SUD("Défense Sud"),
    @SerializedName("ZDF_OUEST")
    ZDF_OUEST("Défense Ouest"),

    @SerializedName("01")
    AIN("Ain"),
    @SerializedName("02")
    AISNE("Aisne"),
    @SerializedName("03")
    ALLIER("Allier"),
    @SerializedName("04")
    ALPES_DE_HAUTE_PROVENCE("Alpes-de-Haute-Provence"),
    @SerializedName("05")
    HAUTES_ALPES("Hautes-Alpes"),
    @SerializedName("06")
    ALPES_MARITIMES("Alpes-Maritimes"),
    @SerializedName("07")
    ARDECHE("Ardèche"),
    @SerializedName("08")
    ARDENNES("Ardennes"),
    @SerializedName("09")
    ARIEGE("Ariège"),
    @SerializedName("10")
    AUBE("Aube"),
    @SerializedName("11")
    AUDE("Aude"),
    @SerializedName("12")
    AVEYRON("Aveyron"),
    @SerializedName("13")
    BOUCHES_DU_RHONE("Bouches-du-Rhône"),
    @SerializedName("14")
    CALVADOS("Calvados"),
    @SerializedName("15")
    CANTAL("Cantal"),
    @SerializedName("16")
    CHARENTE("Charente"),
    @SerializedName("17")
    CHARENTE_MARITIME("Charente-Maritime"),
    @SerializedName("18")
    CHER("Cher"),
    @SerializedName("19")
    CORREZE("Corrèze"),
    @SerializedName("2A")
    CORSE_DU_SUD("Corse-du-Sud"),
    @SerializedName("2B")
    HAUTE_CORSE("Haute-Corse"),
    @SerializedName("21")
    COTE_D_OR("Côte-d'Or"),
    @SerializedName("22")
    COTES_D_ARMOR("Côtes-d'Armor"),
    @SerializedName("23")
    CREUSE("Creuse"),
    @SerializedName("24")
    DORDOGNE("Dordogne"),
    @SerializedName("25")
    DOUBS("Doubs"),
    @SerializedName("26")
    DROME("Drôme"),
    @SerializedName("27")
    EURE("Eure"),
    @SerializedName("28")
    EURE_ET_LOIR("Eure-et-Loir"),
    @SerializedName("29")
    FINISTERE("Finistère"),
    @SerializedName("30")
    GARD("Gard"),
    @SerializedName("31")
    HAUTE_GARONNE("Haute-Garonne"),
    @SerializedName("32")
    GERS("Gers"),
    @SerializedName("33")
    GIRONDE("Gironde"),
    @SerializedName("34")
    HERAULT("Hérault"),
    @SerializedName("35")
    ILLE_ET_VILAINE("Ille-et-Vilaine"),
    @SerializedName("36")
    INDRE("Indre"),
    @SerializedName("37")
    INDRE_ET_LOIRE("Indre-et-Loire"),
    @SerializedName("38")
    ISERE("Isère"),
    @SerializedName("39")
    JURA("Jura"),
    @SerializedName("40")
    LANDES("Landes"),
    @SerializedName("41")
    LOIR_ET_CHER("Loir-et-Cher"),
    @SerializedName("42")
    LOIRE("Loire"),
    @SerializedName("43")
    HAUTE_LOIRE("Haute-Loire"),
    @SerializedName("44")
    LOIRE_ATLANTIQUE("Loire-Atlantique"),
    @SerializedName("45")
    LOIRET("Loiret"),
    @SerializedName("46")
    LOT("Lot"),
    @SerializedName("47")
    LOT_ET_GARONNE("Lot-et-Garonne"),
    @SerializedName("48")
    LOZERE("Lozère"),
    @SerializedName("49")
    MAINE_ET_LOIRE("Maine-et-Loire"),
    @SerializedName("50")
    MANCHE("Manche"),
    @SerializedName("51")
    MARNE("Marne"),
    @SerializedName("52")
    HAUTE_MARNE("Haute-Marne"),
    @SerializedName("53")
    MAYENNE("Mayenne"),
    @SerializedName("54")
    MEURTHE_ET_MOSELLE("Meurthe-et-Moselle"),
    @SerializedName("55")
    MEUSE("Meuse"),
    @SerializedName("56")
    MORBIHAN("Morbihan"),
    @SerializedName("57")
    MOSELLE("Moselle"),
    @SerializedName("58")
    NIEVRE("Nièvre"),
    @SerializedName("59")
    NORD("Nord"),
    @SerializedName("60")
    OISE("Oise"),
    @SerializedName("61")
    ORNE("Orne"),
    @SerializedName("62")
    PAS_DE_CALAIS("Pas-de-Calais"),
    @SerializedName("63")
    PUY_DE_DOME("Puy-de-Dôme"),
    @SerializedName("64")
    PYRENEES_ATLANTIQUES("Pyrénées-Atlantiques"),
    @SerializedName("65")
    HAUTES_PYRENEES("Hautes-Pyrénées"),
    @SerializedName("66")
    PYRENEES_ORIENTALES("Pyrénées-Orientales"),
    @SerializedName("67")
    BAS_RHIN("Bas-Rhin"),
    @SerializedName("68")
    HAUT_RHIN("Haut-Rhin"),
    @SerializedName("69")
    RHONE("Rhône"),
    @SerializedName("70")
    HAUTE_SAONE("Haute-Saône"),
    @SerializedName("71")
    SAONE_ET_LOIRE("Saône-et-Loire"),
    @SerializedName("72")
    SARTHE("Sarthe"),
    @SerializedName("73")
    SAVOIE("Savoie"),
    @SerializedName("74")
    HAUTE_SAVOIE("Haute-Savoie"),
    @SerializedName("75")
    PARIS("Paris"),
    @SerializedName("76")
    SEINE_MARITIME("Seine-Maritime"),
    @SerializedName("77")
    SEINE_ET_MARNE("Seine-et-Marne"),
    @SerializedName("78")
    YVELINES("Yvelines"),
    @SerializedName("79")
    DEUX_SEVRES("Deux-Sèvres"),
    @SerializedName("80")
    SOMME("Somme"),
    @SerializedName("81")
    TARN("Tarn"),
    @SerializedName("82")
    TARN_ET_GARONNE("Tarn-et-Garonne"),
    @SerializedName("83")
    VAR("Var"),
    @SerializedName("84")
    VAUCLUSE("Vaucluse"),
    @SerializedName("85")
    VENDEE("Vendée"),
    @SerializedName("86")
    VIENNE("Vienne"),
    @SerializedName("87")
    HAUTE_VIENNE("Haute-Vienne"),
    @SerializedName("88")
    VOSGES("Vosges"),
    @SerializedName("89")
    YONNE("Yonne"),
    @SerializedName("90")
    TERRITOIRE_DE_BELFORT("Territoire de Belfort"),
    @SerializedName("91")
    ESSONNE("Essonne"),
    @SerializedName("92")
    HAUTS_DE_SEINE("Hauts-de-Seine"),
    @SerializedName("93")
    SEINE_SAINT_DENIS("Seine-Saint-Denis"),
    @SerializedName("94")
    VAL_DE_MARNE("Val-de-Marne"),
    @SerializedName("95")
    VAL_D_OISE("Val-d'Oise"),
    @SerializedName("99")
    ANDORRE("Andorre"),

    UNKNOWN("Unknown");

    public final String description;
    private final @Nullable String apiId;

    Domain(String description) {
        this.description = description;
        this.apiId = getSerializedName();
    }

    @Nullable
    private String getSerializedName() {
        try {
            Field f = getClass().getField(this.name());
            SerializedName a = f.getAnnotation(SerializedName.class);
            if (a != null) {
                return a.value();
            }
        } catch (NoSuchFieldException ignored) {
        }
        return null;
    }

    public static Domain getByApiId(String searched) {
        return Objects.requireNonNull(EnumSet.allOf(Domain.class).stream().filter(dom -> searched.equals(dom.apiId))
                .findFirst().orElse(UNKNOWN));
    }
}
