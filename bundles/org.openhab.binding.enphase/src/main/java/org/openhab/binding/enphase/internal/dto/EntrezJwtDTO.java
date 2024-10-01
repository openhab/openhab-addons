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
package org.openhab.binding.enphase.internal.dto;

/**
 * Data class for Enphase Entrez Portal.
 *
 * @author Joe Inkenbrandt - Initial contribution
 */
public class EntrezJwtDTO {

    public class EntrezJwtHeaderDTO {
        private String kid;
        private String typ;
        private String alg;

        public String getKid() {
            return kid;
        }

        public void setKid(final String kid) {
            this.kid = kid;
        }

        public String getTyp() {
            return typ;
        }

        public void setTyp(final String typ) {
            this.typ = typ;
        }

        public String getAlg() {
            return alg;
        }

        public void setAlg(final String alg) {
            this.alg = alg;
        }
    }

    public class EntrezJwtBodyDTO {
        private String aud;
        private String iss;
        private String enphaseUser;
        private Long exp;
        private Long iat;
        private String jti;
        private String username;

        public String getAud() {
            return aud;
        }

        public void setAud(final String aud) {
            this.aud = aud;
        }

        public String getIss() {
            return iss;
        }

        public void setIss(final String iss) {
            this.iss = iss;
        }

        public String getEnphaseUser() {
            return enphaseUser;
        }

        public void setEnphaseUser(final String enphaseUser) {
            this.enphaseUser = enphaseUser;
        }

        public Long getExp() {
            return exp;
        }

        public void setExp(final Long exp) {
            this.exp = exp;
        }

        public Long getIat() {
            return iat;
        }

        public void setIat(final Long iat) {
            this.iat = iat;
        }

        public String getJti() {
            return jti;
        }

        public void setJti(final String jti) {
            this.jti = jti;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(final String username) {
            this.username = username;
        }
    }

    private final EntrezJwtHeaderDTO header;
    private final EntrezJwtBodyDTO body;

    public EntrezJwtDTO(final EntrezJwtHeaderDTO header, final EntrezJwtBodyDTO body) {
        this.header = header;
        this.body = body;
    }

    public boolean isValid() {
        return header == null || body == null;
    }

    public EntrezJwtBodyDTO getBody() {
        return body;
    }

    public EntrezJwtHeaderDTO getHeader() {
        return header;
    }
}
