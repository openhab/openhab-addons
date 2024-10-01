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

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
class GetCredentialsForIdentityResponse {
    private String identityId = "";
    private Credentials credentials = new Credentials();

    @NonNullByDefault
    static class Credentials {
        private String accessKeyId = "";
        private String secretKey = "";
        private String sessionToken = "";
        private Instant expiration = Instant.now();

        public String getAccessKeyId() {
            return accessKeyId;
        }

        public void setAccessKeyId(String accessKeyId) {
            this.accessKeyId = accessKeyId;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public String getSessionToken() {
            return sessionToken;
        }

        public void setSessionToken(String sessionToken) {
            this.sessionToken = sessionToken;
        }

        public Instant getExpiration() {
            return expiration;
        }

        public void setExpiration(Instant expiration) {
            this.expiration = expiration;
        }

        @Override
        public String toString() {
            return "Credentials{" + //
                    "accessKeyId='" + accessKeyId.hashCode() + '\'' + //
                    ", secretKey='" + secretKey.hashCode() + '\'' + //
                    ", sessionToken='" + sessionToken.hashCode() + '\'' + //
                    ", expiration=" + expiration + //
                    '}';
        }
    }

    String getIdentityId() {
        return identityId;
    }

    void setIdentityId(String identityId) {
        this.identityId = identityId;
    }

    Credentials getCredentials() {
        return credentials;
    }

    void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    @Override
    public String toString() {
        return "GetCredentialsForIdentityResponse{" + //
                "identityId='" + identityId + '\'' + //
                ", credentials=" + credentials + //
                '}';
    }
}
