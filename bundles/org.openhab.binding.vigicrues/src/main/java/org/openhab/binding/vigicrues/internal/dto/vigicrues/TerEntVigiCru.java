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

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link TerEntVigiCru} is the Java class used to map the JSON
 * response to a vigicrue api endpoint request.
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class TerEntVigiCru {

    public class VicTerEntVigiCru {
        @SerializedName("vic:aNMoinsUn")
        public List<VicANMoinsUn> vicANMoinsUn;
        /*
         * Currently unused, maybe interesting in the future
         *
         * @SerializedName("@id")
         * public String id;
         *
         * @SerializedName("vic:CdEntVigiCru")
         * public String vicCdEntVigiCru;
         *
         * @SerializedName("vic:TypEntVigiCru")
         * public String vicTypEntVigiCru;
         *
         * @SerializedName("vic:LbEntVigiCru")
         * public String vicLbEntVigiCru;
         *
         * @SerializedName("vic:DtHrCreatEntVigiCru")
         * public String vicDtHrCreatEntVigiCru;
         *
         * @SerializedName("vic:DtHrMajEntVigiCru")
         * public String vicDtHrMajEntVigiCru;
         *
         * @SerializedName("vic:StEntVigiCru")
         * public String vicStEntVigiCru;
         * public int count_aNMoinsUn;
         *
         * @SerializedName("LinkInfoCru")
         * public String linkInfoCru;
         */
    }

    @SerializedName("vic:TerEntVigiCru")
    public VicTerEntVigiCru vicTerEntVigiCru;
}
