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
package org.openhab.binding.vigicrues.internal.dto.vigicrues;

import java.util.List;
import java.util.stream.Stream;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link TronEntVigiCru} is the Java class used to map the JSON
 * response to a vigicrue api endpoint request.
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class TronEntVigiCru {
    public class VicStaEntVigiCru {
        @SerializedName("vic:CdEntVigiCru")
        public String vicCdEntVigiCru;
    }

    public class VicTronEntVigiCru {
        @SerializedName("vic:aNMoinsUn")
        public List<VicStaEntVigiCru> stations;
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
         *
         * @SerializedName("count_aNPlusUn")
         * public Integer countANPlusUn;
         *
         * @SerializedName("count_aNMoinsUn")
         * public Integer countANMoinsUn;
         *
         * @SerializedName("LinkInfoCru")
         * public String linkInfoCru;
         */
    }

    @SerializedName("vic:TronEntVigiCru")
    private VicTronEntVigiCru tronTerEntVigiCru;

    public Stream<VicStaEntVigiCru> getStations() {
        if (tronTerEntVigiCru != null && tronTerEntVigiCru.stations != null) {
            return tronTerEntVigiCru.stations.stream();
        } else {
            return Stream.empty();
        }
    }
}
