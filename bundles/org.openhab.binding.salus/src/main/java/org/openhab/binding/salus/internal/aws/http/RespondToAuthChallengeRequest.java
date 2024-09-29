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
package org.openhab.binding.salus.internal.aws.http;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Copied from org.openhab.binding.windcentrale.internal.dto.RespondToAuthChallengeRequest
 * 
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
class RespondToAuthChallengeRequest {

    public String challengeName = "PASSWORD_VERIFIER";
    public String clientId = "";
    public Map<String, String> challengeResponses = new LinkedHashMap<>();

    public RespondToAuthChallengeRequest(String clientId, String username, String passwordClaimSecretBlock,
            String passwordClaimSignature, String timestamp) {
        this.clientId = clientId;
        challengeResponses.put("USERNAME", username);
        challengeResponses.put("PASSWORD_CLAIM_SECRET_BLOCK", passwordClaimSecretBlock);
        challengeResponses.put("PASSWORD_CLAIM_SIGNATURE", passwordClaimSignature);
        challengeResponses.put("TIMESTAMP", timestamp);
    }
}
