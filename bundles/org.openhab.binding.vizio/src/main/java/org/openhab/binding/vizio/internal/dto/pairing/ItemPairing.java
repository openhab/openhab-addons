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
package org.openhab.binding.vizio.internal.dto.pairing;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ItemPairing} class contains data from the Vizio TV in response to starting the pairing process
 *
 * @author Michael Lobstein - Initial contribution
 */
public class ItemPairing {
    @SerializedName("PAIRING_REQ_TOKEN")
    private Integer pairingReqToken = -1;
    @SerializedName("CHALLENGE_TYPE")
    private Integer challengeType = -1;

    public Integer getPairingReqToken() {
        return pairingReqToken;
    }

    public void setPairingReqToken(Integer pairingReqToken) {
        this.pairingReqToken = pairingReqToken;
    }

    public Integer getChallengeType() {
        return challengeType;
    }

    public void setChallengeType(Integer challengeType) {
        this.challengeType = challengeType;
    }
}
