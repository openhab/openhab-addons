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

import java.time.Instant;
import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * {@link AtmoFranceDto} class defines DTO used to interact with server api
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
public class AtmoFranceDto {
    public record LoginResponse(String token) {
    }

    public record ErrorResponse(int code, String message) {
    }

    public record AtmoResponse(String type, String name, Crs crs, List<Feature<IndexProperties>> features) {
    }

    public record PollensResponse(String type, String name, Crs crs, List<Feature<PollensProperties>> features) {
    }

    public record Crs(String type, Properties properties) {
    }

    public record Properties(String name) {
    }

    public record Feature<T extends BaseProperties> (String type, T properties, Geometry geometry) {
    }

    public record Geometry(String type, List<Double> coordinates) {
    }

    public class BaseProperties {
        @SerializedName("aasqa")
        public String aasqa;

        @SerializedName("date_maj")
        public Instant dateMaj;
        @SerializedName("date_dif")
        public Instant dateDif;
        @SerializedName("date_ech")
        public Instant dateEch;

        @SerializedName("code_zone")
        public String codeZone;
        @SerializedName("lib_zone")
        public String libZone;
        @SerializedName("type_zone")
        public String typeZone;

        @SerializedName("lib_qual")
        public String libQual;
        @SerializedName("source")
        public String source;
    }

    public class PollensProperties extends BaseProperties {
        @SerializedName("alerte")
        public boolean alerte;

        @SerializedName("code_ambr")
        private int codeAmbr;
        @SerializedName("code_arm")
        private int codeArm;
        @SerializedName("code_aul")
        private int codeAul;
        @SerializedName("code_boul")
        private int codeBoul;
        @SerializedName("code_gram")
        private int codeGram;
        @SerializedName("code_oliv")
        private int codeOliv;

        @SerializedName("code_qual")
        public PollenIndex codeQual;

        @SerializedName("conc_ambr")
        public double concAmbr;
        @SerializedName("conc_arm")
        public double concArm;
        @SerializedName("conc_aul")
        public double concAul;
        @SerializedName("conc_boul")
        public double concBoul;
        @SerializedName("conc_gram")
        public double concGram;
        @SerializedName("conc_oliv")
        public double concOliv;

        @SerializedName("pollen_resp")
        public String pollenResp;
        @SerializedName("name")
        public Object name;

        public PollenIndex getTaxon(Taxon taxon) {
            int result = switch (taxon) {
                case ALDER -> codeAul;
                case BIRCH -> codeBoul;
                case OLIVE -> codeOliv;
                case GRASSES -> codeGram;
                case WORMWOOD -> codeArm;
                case RAGWEED -> codeAmbr;
                default -> 9;
            };
            return PollenIndex.valueOf(result);
        }
    }

    public class IndexProperties extends BaseProperties {
        @SerializedName("code_no2")
        public AtmoIndex no2;
        @SerializedName("code_o3")
        public AtmoIndex o3;
        @SerializedName("code_pm10")
        public AtmoIndex pm10;
        @SerializedName("code_pm25")
        public AtmoIndex pm25;
        @SerializedName("code_so2")
        public AtmoIndex so2;

        @SerializedName("code_qual")
        public AtmoIndex codeQual;
        @SerializedName("coul_qual")
        public String coulQual;

        @SerializedName("epsg_reg")
        public String epsgReg;
        @SerializedName("x_reg")
        public double xReg;
        @SerializedName("x_wgs84")
        public double xWgs84;
        @SerializedName("y_reg")
        public double yReg;
        @SerializedName("y_wgs84")
        public double yWgs84;
        @SerializedName("gml_id2")
        public Object gmlId2;
    }
}
