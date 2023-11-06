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
package org.openhab.binding.vigicrues.internal.dto.vigicrues;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link InfoVigiCru} is the Java class used to map the JSON
 * response to a vigicrue api endpoint request.
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class InfoVigiCru {
    public class VicInfoVigiCru {
        @SerializedName("vic:NivInfoVigiCru")
        public int vicNivInfoVigiCru;
        @SerializedName("vic:SituActuInfoVigiCru")
        public String vicSituActuInfoVigiCru;
        @SerializedName("vic:QualifInfoVigiCru")
        public String vicQualifInfoVigiCru;
        /*
         * Currently unused, maybe interesting in the future
         *
         * @SerializedName("vic:RefInfoVigiCru")
         * private String vicRefInfoVigiCru;
         *
         * @SerializedName("vic:TypInfoVigiCru")
         * private int vicTypInfoVigiCru;
         *
         * @SerializedName("vic:DtHrInfoVigiCru")
         * private String vicDtHrInfoVigiCru;
         *
         * @SerializedName("vic:DtHrSuivInfoVigiCru")
         * private String vicDtHrSuivInfoVigiCru;
         *
         * @SerializedName("vic:EstNivCalInfoVigiCru")
         * private Boolean vicEstNivCalInfoVigiCru;
         *
         * @SerializedName("vic:StInfoVigiCru")
         * private int vicStInfoVigiCru;
         */
    }

    @SerializedName("vic:InfoVigiCru")
    public VicInfoVigiCru vicInfoVigiCru;
}
