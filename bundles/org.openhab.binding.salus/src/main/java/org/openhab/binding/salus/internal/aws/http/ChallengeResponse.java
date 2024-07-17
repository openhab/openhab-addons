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

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Copied from org.openhab.binding.windcentrale.internal.dto.ChallengeResponse
 * 
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
class ChallengeResponse {

    public String challengeName = "";
    public Map<String, String> challengeParameters = Map.of();

    private String getChallengeParameter(String key) {
        return Objects.requireNonNullElse(challengeParameters.get(key), "");
    }

    public String getSalt() {
        return getChallengeParameter("SALT");
    }

    public String getSecretBlock() {
        return getChallengeParameter("SECRET_BLOCK");
    }

    public String getSrpB() {
        return getChallengeParameter("SRP_B");
    }

    public String getUsername() {
        return getChallengeParameter("USERNAME");
    }

    public String getUserIdForSrp() {
        return getChallengeParameter("USER_ID_FOR_SRP");
    }
}
