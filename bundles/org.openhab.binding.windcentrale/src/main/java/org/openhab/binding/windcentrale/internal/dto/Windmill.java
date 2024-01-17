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
package org.openhab.binding.windcentrale.internal.dto;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Enumerates the Windcentrale windmills. The project codes are used in API requests and responses.
 * The other details are used as Thing properties.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public enum Windmill {

    DE_GROTE_GEERT(1, "WND-GG", "De Grote Geert", 9910, "Enercon E-70", 2008, "Delfzijl", "Groningen",
            "53.280605, 6.955141", "https://www.windcentrale.nl/molens/de-grote-geert-2/"),
    DE_JONGE_HELD(2, "WND-JH", "De Jonge Held", 10154, "Enercon E-70", 2008, "Delfzijl", "Groningen",
            "53.277648, 6.954906", "https://www.windcentrale.nl/molens/de-jonge-held/"),
    HET_RODE_HERT(31, "WND-RH", "Het Rode Hert", 6648, "Vestas V80", 2005, "Culemborg", "Gelderland",
            "51.935831, 5.192109", "https://www.windcentrale.nl/molens/het-rode-hert/"),
    DE_RANKE_ZWAAN(41, "WND-RZ", "De Ranke Zwaan", 6164, "Vestas V80", 2005, "Culemborg", "Gelderland",
            "51.934915, 5.19989", "https://www.windcentrale.nl/molens/de-ranke-zwaan-2/"),
    DE_WITTE_JUFFER(51, "WND-WJ", "De Witte Juffer", 5721, "Vestas V80", 2005, "Culemborg", "Gelderland",
            "51.935178, 5.195860", "https://www.windcentrale.nl/molens/de-witte-juffer/"),
    DE_BONTE_HEN(111, "WND-BH", "De Bonte Hen", 5579, "Vestas V52", 2009, "Burgerbrug", "Noord-Holland",
            "52.757051, 4.684678", "https://www.windcentrale.nl/molens/de-bonte-hen-2/"),
    DE_TROUWE_WACHTER(121, "WND-TW", "De Trouwe Wachter", 5602, "Vestas V52", 2009, "Burgerbrug", "Noord-Holland",
            "52.758745, 4.686041", "https://www.windcentrale.nl/molens/de-trouwe-wachter-2/"),
    DE_BLAUWE_REIGER(131, "WND-BR", "De Blauwe Reiger", 5534, "Vestas V52", 2009, "Burgerbrug", "Noord-Holland",
            "52.760482, 4.687438", "https://www.windcentrale.nl/molens/de-blauwe-reiger/"),
    DE_VIER_WINDEN(141, "WND-VW", "De Vier Winden", 5512, "Vestas V52", 2009, "Burgerbrug", "Noord-Holland",
            "52.762219, 4.688837", "https://www.windcentrale.nl/molens/de-vier-winden-2/"),
    DE_BOERENZWALUW(201, "WND-BZ", "De Boerenzwaluw", 3000, "Enercon E-44", 2015, "Burum", "Friesland",
            "53.265572, 6.213929", "https://www.windcentrale.nl/molens/de-boerenzwaluw/"),
    HET_VLIEGENDE_HERT(211, "WND-VH", "Het Vliegend Hert", 10000, "Lagerwey L82", 2019, "Rouveen", "Overijssel",
            "52.595422, 6.223335", "https://www.windcentrale.nl/molens/het-vliegend-hert/");

    private final int id;
    private final String projectCode;
    private final String name;
    private final int totalShares;
    private final String type;
    private final int buildYear;
    private final String municipality;
    private final String province;
    private final String coordinates;
    private final String detailsUrl;

    Windmill(int id, String projectCode, String name, int totalShares, String type, int buildYear, String municipality,
            String province, String coordinates, String detailsUrl) {
        this.id = id;
        this.projectCode = projectCode;
        this.name = name;
        this.totalShares = totalShares;
        this.type = type;
        this.buildYear = buildYear;
        this.municipality = municipality;
        this.province = province;
        this.coordinates = coordinates;
        this.detailsUrl = detailsUrl;
    }

    public int getId() {
        return id;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public String getName() {
        return name;
    }

    public int getTotalShares() {
        return totalShares;
    }

    public String getType() {
        return type;
    }

    public int getBuildYear() {
        return buildYear;
    }

    public String getMunicipality() {
        return municipality;
    }

    public String getProvince() {
        return province;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public String getDetailsUrl() {
        return detailsUrl;
    }

    @Override
    public String toString() {
        return name;
    }

    public static @Nullable Windmill fromName(String name) {
        return Arrays.stream(values()) //
                .filter(windmill -> windmill.name.equals(name)) //
                .findFirst().orElse(null);
    }

    public static @Nullable Windmill fromProjectCode(String projectCode) {
        return Arrays.stream(values()) //
                .filter(windmill -> windmill.projectCode.equals(projectCode)) //
                .findFirst().orElse(null);
    }
}
