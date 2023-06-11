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
package org.openhab.binding.nest.internal.wwn.dto;

/**
 * Deals with the access token data that comes back from WWN when it is requested.
 *
 * @author David Bennett - Initial contribution
 * @author Wouter Born - Add equals and hashCode methods
 */
public class WWNAccessTokenData {

    private String accessToken;
    private Long expiresIn;

    public String getAccessToken() {
        return accessToken;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        WWNAccessTokenData other = (WWNAccessTokenData) obj;
        if (accessToken == null) {
            if (other.accessToken != null) {
                return false;
            }
        } else if (!accessToken.equals(other.accessToken)) {
            return false;
        }
        if (expiresIn == null) {
            if (other.expiresIn != null) {
                return false;
            }
        } else if (!expiresIn.equals(other.expiresIn)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((accessToken == null) ? 0 : accessToken.hashCode());
        result = prime * result + ((expiresIn == null) ? 0 : expiresIn.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AccessTokenData [accessToken=").append(accessToken).append(", expiresIn=").append(expiresIn)
                .append("]");
        return builder.toString();
    }
}
