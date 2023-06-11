/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
 * The {@link VicANMoinsUn} is the Java class used to map the JSON
 * response to an vigicrue api endpoint request.
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class VicANMoinsUn {
    @SerializedName("vic.CdEntVigiCru")
    public String vicCdEntVigiCru;

    /*
     * Currently unused, maybe interesting in the future
     *
     * @SerializedName("@id")
     * private String id;
     *
     * @SerializedName("vic.TypEntVigiCru")
     * private String vicTypEntVigiCru;
     *
     * @SerializedName("vic.LbEntVigiCru")
     * private String vicLbEntVigiCru;
     *
     * @SerializedName("LinkEntity")
     * private String linkEntity;
     *
     * @SerializedName("LinkInfoCru")
     * private String linkInfoCru;
     */
}
