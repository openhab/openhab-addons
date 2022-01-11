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
package org.openhab.binding.nest.internal.wwn.dto;

/**
 * The WWN meta data in the data downloads.
 *
 * @author David Bennett - Initial contribution
 * @author Wouter Born - Add equals and hashCode methods
 */
public class WWNMetadata {

    private String accessToken;
    private String clientVersion;

    public String getAccessToken() {
        return accessToken;
    }

    public String getClientVersion() {
        return clientVersion;
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
        WWNMetadata other = (WWNMetadata) obj;
        if (accessToken == null) {
            if (other.accessToken != null) {
                return false;
            }
        } else if (!accessToken.equals(other.accessToken)) {
            return false;
        }
        if (clientVersion == null) {
            if (other.clientVersion != null) {
                return false;
            }
        } else if (!clientVersion.equals(other.clientVersion)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((accessToken == null) ? 0 : accessToken.hashCode());
        result = prime * result + ((clientVersion == null) ? 0 : clientVersion.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("NestMetadata [accessToken=").append(accessToken).append(", clientVersion=")
                .append(clientVersion).append("]");
        return builder.toString();
    }
}
