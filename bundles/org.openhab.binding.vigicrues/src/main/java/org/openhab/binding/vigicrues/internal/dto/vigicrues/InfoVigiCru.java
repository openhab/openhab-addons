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
package org.openhab.binding.vigicrues.internal.dto.vigicrues;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link InfoVigiCru} is the Java class used to map the JSON
 * response to a vigicrue api endpoint request.
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class InfoVigiCru {

    @SerializedName("type")
    public String type;

    @SerializedName("features")
    public List<Feature> features;

    public class Feature {
        @SerializedName("type")
        public String type;

        @SerializedName("properties")
        public FeatureProperties properties;

        public class FeatureProperties {

            @SerializedName("CdTCC")
            public String cdTCC;

            @SerializedName("id")
            public int id;

            @SerializedName("CdEntCru")
            public String cdEntCru;

            @SerializedName("typentcru")
            public String typEntCru;

            @SerializedName("lbentcru")
            public String lbEntCru;

            @SerializedName("acroentcru")
            public String acroEntCru;

            @SerializedName("cddient_1")
            public String cdDient1;

            @SerializedName("dhcentcru")
            public String dhcEntCru;

            @SerializedName("dhmentcru")
            public String dhmEntCru;

            @SerializedName("stentcru")
            public String stEntCru;

            @SerializedName("cdensup_1")
            public String cdEnSup1;

            @SerializedName("typensup_1")
            public String typEnSup1;

            @SerializedName("cdint")
            public String cdInt;

            @SerializedName("NivInfViCr")
            public int nivInfViCr;

            @SerializedName("geometry")
            public Geometry geometry;

            public class Geometry {
                @SerializedName("type")
                public String type;
            }
        }
    }
}
